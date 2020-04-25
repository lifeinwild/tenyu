package bei7473p5254d69jcuat.tenyu.model.release1.middle.vote;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.vote.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 全分散合意のスケジューリング、自分の主張値の設定、結果をDBへ書き込み
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVoteManager {
	/**
	 * 分散合意ID:自分の主張値
	 * いつでも基盤GUIから設定できる
	 */
	private Map<Long,
			VoteValue> distributedIdToMyValue = new ConcurrentHashMap<>();

	public DistributedVote get(Long distributedVoteId) {
		return Glb.getObje()
				.getDistributedVote(dvs -> dvs.get(distributedVoteId));
	}

	/**
	 * 分散合意を登録または更新する
	 * 例えば新しい分散合意の登録または
	 * 既存の分散合意の論理削除に使用される。
	 *
	 * @param vote
	 * @return
	 */
	public boolean put(DistributedVote vote) {
		try {
			//メッセージ反映処理と被るのはまずいし、拡散段階で客観が変わるのもまずいので。
			while (!ObjectivityUpdateApplyAndDelayRun
					.isNotLongAppliedAndApplying()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}

		return Glb.getObje().compute(txn -> {
			try {
				DistributedVoteStore s = new DistributedVoteStore(txn);
				return s.save(vote);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	private void procManagerElection(DistributedVoteResult result,
			PowerVoteSequence seq) {
		Glb.getMiddle().getProcFromOtherModules().addProcFromOtherModules(
				new DistributedVoteProcManagerElection(result, seq));
	}

	public static class DistributedVoteProcManagerElection
			extends DistributedVoteProc {
		public DistributedVoteProcManagerElection(DistributedVoteResult result,
				PowerVoteSequence seq) {
			super(result, seq);
		}

		/**
		 * @param result
		 * @return	過去一週間の影響度割合の合計を返す
		 */
		private Map<Long, Double> getTotalPowersLast1Week(Transaction txn,
				DistributedVoteResult result) {
			try {
				Map<Long, Double> totalPowersLast1Week = new HashMap<>();
				DistributedVoteResultStore dvrs = new DistributedVoteResultStore(
						txn);
				int last1Week = 3 * 7 - 1;//1日3回なので

				//ここ過去の全体運営者投票の結果を全件取得するので徐々に重くなると思われるが
				//10年分蓄積されても処理可能な重さ。
				List<DistributedVoteResult> pastDvrs = dvrs
						.getByDistributedVoteIdAndStartHistoryIndex(result,
								last1Week);
				if (pastDvrs == null || pastDvrs.size() == 0) {
					Glb.getLogger()
							.info("no past results of tenyu manager election.");
					return totalPowersLast1Week;
				}

				if (pastDvrs.size() > last1Week) {
					Glb.getLogger().warn(
							"too many past results. size=" + pastDvrs.size());
				}
				for (DistributedVoteResult pastDvr : pastDvrs) {
					if (!pastDvr.getDistributedVoteId()
							.equals(managerElectionId)) {
						Glb.getLogger().error(
								"Bug of getByDistributedVoteIdAndStartHistoryIndex",
								new Exception());
						continue;
					}
					if (!(pastDvr.getMajority() instanceof PowerVoteValue)) {
						Glb.getLogger().error(
								"Unknown implementation of VoteValue",
								new Exception());
						continue;
					}
					PowerVoteValue pastDvrPowers = (PowerVoteValue) pastDvr
							.getMajority();
					add(totalPowersLast1Week,
							pastDvrPowers.cnvLongOptionToPower());
				}

				return totalPowersLast1Week;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		}

		/**
		 * マップの合成みたいな処理
		 * @param powers1	ここに足す
		 * @param powers2	これを足す
		 */
		private void add(Map<Long, Double> powers1, Map<Long, Double> powers2) {
			if (powers1 == null || powers2 == null)
				return;
			for (Entry<Long, Double> e : powers2.entrySet()) {
				Double power1 = powers1.get(e.getKey());
				if (power1 == null) {
					power1 = 0D;
				}
				Double power2 = e.getValue();
				if (power2 == null)
					continue;
				powers1.put(e.getKey(), power1 + power2);
			}
		}

		@Override
		public boolean apply(Transaction txn, long historyIndex)
				throws Exception {
			//MVCCトランザクションなのでやり直しが生じる事に留意
			//DistributedVote v = get(result.getDistributedVoteId());
			//全体運営者投票だけ特殊な処理をする
			//多数派による全体運営者選出の結果
			PowerVoteValue res = (PowerVoteValue) result.getMajority();
			//userId : power
			Map<Long, Double> powersVoteResult = res.cnvLongOptionToPower();
			Glb.debug("voteResultThisTime=" + powersVoteResult);

			//過去七日分の分散合意の結果と合算し合計値が1.0となるよう再計算する。
			Map<Long, Double> totalPowersPast = getTotalPowersLast1Week(txn,
					result);
			Map<Long, Double> powersDependOnPast = new HashMap<>();
			add(totalPowersPast, powersVoteResult);
			double for1 = Glb.getUtil().leveling(totalPowersPast.values(), 1.0);
			for (Entry<Long, Double> e : totalPowersPast.entrySet()) {
				powersDependOnPast.put(e.getKey(), e.getValue() * for1);
			}
			Glb.debug("voteResultLast7days=" + powersDependOnPast);
			ObjectivityCore core = Glb.getObje().getCore();
			//新しい全体運営者の影響力バランス
			TenyuManagerElectionResult newBalance = new TenyuManagerElectionResult(
					core.getHistoryIndex(), powersDependOnPast);
			//実際に設定されたバランス。補正を受ける場合がある
			TenyuManagerElectionResult actualBalance = core.getManagerList()
					.setPowers(newBalance);
			if (actualBalance == null)
				return false;
			Glb.debug("actualBalance=" + actualBalance);

			new ObjectivityCoreStore(txn).save(core);

			//setPowersで内部的に情報が修正されて反映される場合がある
			//しかし、分散合意の結果は分散合意の結果として、修正前の情報を結果として記録する

			/* このコードはメッセージ受付サーバという概念を用いていた時のもの
			 * 現在は不要
			//反映サーバーに追加する
			List<Entry<Long, Double>> sortedUserIdPowers = new ArrayList<>(
					powersVoteResult.entrySet());
			//値でソート
			Collections.sort(sortedUserIdPowers,
					new Comparator<Entry<Long, Double>>() {
						public int compare(Entry<Long, Double> o1,
								Entry<Long, Double> o2) {
							return o2.getValue().compareTo(o1.getValue());
						}
					});
			//DBに書き込み
			RoleStore rs = new RoleStore(txn);
			Role umls = rs
					.getByName(UserMessageListServer.class.getSimpleName());
			if (umls == null)
				return false;
			for (Entry<Long, Double> userIdPower : sortedUserIdPowers) {
				if (!umls.getAdminUserIds().contains(userIdPower.getKey())) {
					umls.addAdmin(userIdPower.getKey());
				}
			}
			if (!rs.update(umls))
				return false;
			*/
			boolean r = txn.commit();
			if (r) {
				//失敗していたら致命的問題
				Glb.getLogger().info(
						"election of TenyuManager write to RoleStore result="
								+ r);
			}
			return r;
		}
	}

	public static class DistributedVoteProc
			implements ObjectivityUpdateDataElement {
		protected DistributedVoteResult result;
		protected transient PowerVoteSequence seq;

		public DistributedVoteProc(DistributedVoteResult result,
				PowerVoteSequence seq) {
			super();
			this.result = result;
			this.seq = seq;
		}

		@Override
		public int compareTo(ObjectivityUpdateDataElement o) {
			int parent = ObjectivityUpdateDataElement.super.compareTo(o);
			if (parent != 0)
				return parent;

			//親クラスでクラス名比較が入っているので
			//ここに来た場合必ず同じクラス
			if (o == null || !(o instanceof DistributedVoteProc))
				return 1;
			DistributedVoteProc o2 = (DistributedVoteProc) o;

			if (result == null && o2.getResult() == null)
				return 0;

			if (result.getDistributedVoteId() == null
					&& o2.getResult().getDistributedVoteId() == null)
				return 0;

			return result.getDistributedVoteId()
					.compareTo(o2.getResult().getDistributedVoteId());
		}

		public DistributedVoteResult getResult() {
			return result;
		}

		@Override
		public boolean apply(Transaction txn, long historyIndex)
				throws Exception {
			DistributedVoteResultStore s = new DistributedVoteResultStore(txn);
			boolean r = s.create(result) != null;
			if (r) {
				Glb.getMiddle().getEventManager().fire(result);
			}
			return r;
		}

		@Override
		public boolean isDiffused() {
			DistributedVote dv = Glb.getObje()
					.readTryW(txn -> new DistributedVoteStore(txn)
							.get(result.getDistributedVoteId()));
			if (dv == null) {
				Glb.getLogger().warn(new Exception("dv is null"));
				return false;
			}
			long start = getCreateDate();
			long now = Glb.getUtil().now();
			long elapsed = now - start;
			long tolerance = seq.getSequenceTime();

			return elapsed > seq.getSequenceTime() + tolerance;
		}

		@Override
		public long getCreateDate() {
			return Glb.getUtil().getEpochMilliSpecifiedSecond(
					seq.getStartDate(), DistributedVote.startSecond);
		}

		@Override
		public boolean isOld() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((this.result == null) ? 0 : this.result.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DistributedVoteProc other = (DistributedVoteProc) obj;
			if (result == null) {
				if (other.result != null)
					return false;
			} else if (!result.equals(other.result))
				return false;
			return true;
		}

	}

	/**
	 * 分散合意の結果を登録する
	 * @param result
	 * @return
	 */
	public boolean putResult(DistributedVoteResult result,
			PowerVoteSequence seq) {
		if (result.getDistributedVoteId().equals(managerElectionId)) {
			procManagerElection(result, seq);
		}

		//後に実行される客観更新処理の登録
		Glb.getMiddle().getProcFromOtherModules()
				.addProcFromOtherModules(new DistributedVoteProc(result, seq));

		return true;
	}

	/**
	 * 全体運営者選出投票の分散合意のID
	 */
	public static final Long managerElectionId = IdObjectI
			.getFirstId();

	public void start() {
		Glb.getObje().writeSystemVoteToDB();

		//全分散合意をスケジューリングする
		for (DistributedVote e : Glb.getObje()
				.getDistributedVote(dvs -> dvs.getAllValues())) {
			if (!e.isEnable()) {
				continue;
			}
			registerSequence(e);
		}
	}

	public boolean registerSequence(DistributedVote e) {
		return Glb.getP2p().addSeq(PowerVoteSequence.getJob(e));
	}

	public void stop() {

	}

	public boolean putMyValue(VoteValue myValue) {
		ValidationResult r = new ValidationResult();
		if (!myValue.validateAtCreate(r)) {
			return false;
		}
		distributedIdToMyValue.put(myValue.getDistributedVoteId(), myValue);

		//全体運営者投票に設定した場合
		if (managerElectionId.equals(myValue.getDistributedVoteId())) {
			//設定日時を設定する
			Glb.getSubje().getMe()
					.setLastVoteDateTenyuManger(System.currentTimeMillis());
		}

		return true;
	}

	/**
	 * @return	Mapに登録されているオブジェクトのクローン
	 * 無ければnull
	 */
	public VoteValue getMyValueClone(Long distributedVoteId) {
		VoteValue r = distributedIdToMyValue.get(distributedVoteId);
		if (r == null)
			return null;
		return r.clone();
	}

	//	public Map<Long, VoteValue> getDistributedIdToMyValue() {
	//		return Collections.unmodifiableMap(distributedIdToMyValue);
	//	}

}
