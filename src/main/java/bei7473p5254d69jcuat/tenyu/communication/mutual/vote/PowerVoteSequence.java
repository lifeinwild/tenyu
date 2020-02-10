package bei7473p5254d69jcuat.tenyu.communication.mutual.vote;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import glb.*;

/**
 * 分散合意の通信処理
 *
 * ユーザーメッセージリスト反映シーケンスの反映処理段階でヒストリーインデックスが進むが
 * ヒストリーインデックス値がノード毎に異ならないように注意する必要がある。
 * つまり分散合意は拡散段階でのみ開始できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class PowerVoteSequence extends P2PSequence {
	/**
	 * 分配割合の合計
	 * 100%なので1.0
	 */
	public static final double powerTotalMax = 1.0D;

	private Long distributedVoteId;
	private transient DistributedVote cache;
	/**
	 * 分散合意の結果
	 * 処理過程や処理終了時に設定してDBに記録される
	 */
	private DistributedVoteResult result = new DistributedVoteResult();;

	/**
	 * 1段階目の処理結果
	 * 2段階目で使用する
	 */
	private PowerVoteValue myValueTmp;

	/**
	 * @return	指定された分散合意の自分の主張値を返す。
	 * 無ければ新しいオブジェクトを作成して返す。
	 * 例外が発生した場合null
	 */
	protected PowerVoteValue getOrCreateMyValue() {
		try {
			//まずユーザーによって設定された値の取得を試みる
			DistributedVoteManager man = Glb.getMiddle()
					.getDistributedManager();
			PowerVoteValue src = (PowerVoteValue) man
					.getMyValueClone(distributedVoteId);
			if (src != null)
				return src;

			//無ければ新たに作る
			PowerVoteValue myValue = new PowerVoteValue();
			myValue.setDistributedVoteId(getDistributedVoteId());
			man.putMyValue(myValue);

			//新たに作成したオブジェクトのクローンを返す
			return (PowerVoteValue) man.getMyValueClone(distributedVoteId);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public static TimerTaskList.JobAndTrigger getJob(DistributedVote v) {
		if (v == null || v.getId() == null || v.getSchedule() == null) {
			Glb.getLogger().warn("", new IllegalArgumentException());
			return null;
		}
		return TimerTaskList.getJob("" + v.getId(),
				PowerVoteSequence.class, v.getSchedule(),
				b -> b.usingJobData("distributedVoteId", v.getId()),
				b -> b.withPriority(9));
	}

	public DistributedVote getInfo() {
		if (cache == null)
			cache = Glb.getMiddle().getDistributedManager()
					.get(distributedVoteId);
		return cache;
	}

	@Override
	public String getStartSchedule() {
		return getInfo().getSchedule();
	}

	@Override
	protected void setupStatements() {
		statements.add(new PowerVoteStatement(this));
		statements.add(new PowerVoteConfirmationStatement(this));
	}

	@Override
	public void resetConcrete() {
		cache = null;
		result = new DistributedVoteResult();
		statements.clear();
		setupStatements();
	}

	@Override
	public void start() {
		/* 開始日時が偶数分10秒に固定されるという規約によって解決される
		try {
			//メッセージリスト反映シーケンスが拡散段階になるまで待つ
			//この処理は開始時ヒストリーインデックスを一致させるために必要
			Glb.getMiddle().getObjectivityUpdateSequence().waitForFinish();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		*/
		result.setStartHistoryIndex(Glb.getObje().getCore().getHistoryIndex());
		result.setDistributedVoteId(distributedVoteId);

		super.start();
		getInfo();//cacheをセット。意味的にstart時にセットしたい
		if (!getInfo().isEnable()) {
			if (!unschedule())
				Glb.getLogger().error("",
						new Exception(
								"Failed to unschedule " + getInfo().getName()
										+ " " + getInfo().getId()));
			throw new IllegalStateException("enable=false");
		}

	}

	public Long getDistributedVoteId() {
		return distributedVoteId;
	}

	public void setDistributedVoteId(Long distributedVoteId) {
		this.distributedVoteId = distributedVoteId;
	}

	/**
	 * @param fromNeighbors		近傍からの投票一覧 PowerVoteMessage前提
	 * @return					選択肢ID : (分配割合 : 合計信用)
	 */
	public static Map<Integer, Map<Double, Integer>> createCounts(
			List<Received> fromNeighbors) {
		//選択肢ID : (分配割合 : 合計信用)
		Map<Integer, Map<Double, Integer>> counts = new HashMap<>();
		for (Received r : fromNeighbors) {
			try {
				//信用の取得
				P2PEdge n = r.getEdgeByInnermostPackage();
				int credit = n.credit();

				//信用０なら計算する意味無し
				if (credit == 0)
					continue;

				//近傍の主張値
				PowerVoteI p = (PowerVoteI) r.getMessage().getContent();
				for (Entry<Integer, Double> e : p.getSenderValue().getPowers()
						.entrySet()) {
					Integer choiceId = e.getKey();
					//分配割合
					Double power = e.getValue();

					//分配割合 : 合計信用
					Map<Double, Integer> powers = counts.get(choiceId);
					if (powers == null) {
						//未登録なら新規作成
						powers = new HashMap<>();
						//登録
						counts.put(choiceId, powers);
					}

					//合計信用
					Integer totalCredit = powers.get(power);
					//合計信用が登録されていなければ０で作成
					if (totalCredit == null)
						totalCredit = 0;
					//合計信用に加算
					totalCredit += credit;
					//登録
					powers.put(power, totalCredit);
				}
			} catch (Exception e) {
				Glb.debug(e);
				continue;
			}
		}
		return counts;
	}

	public PowerVoteValue getMyValueTmp() {
		return myValueTmp;
	}

	public void setMyValueTmp(PowerVoteValue myValueTmp) {
		this.myValueTmp = myValueTmp;
	}

	public DistributedVoteResult getResult() {
		return result;
	}

	public void setResult(DistributedVoteResult result) {
		this.result = result;
	}

}
