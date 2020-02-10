package bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.lang.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.Conf.*;

public class RandomString extends TurnBaseMessage {
	/**
	 * ランダム値を固定長にする
	 */
	private static final int strLen = 20;

	public static int getStrlen() {
		return strLen;
	}

	private String rndStr;

	public void setRndStr(String rndStr) {
		this.rndStr = rndStr;
	}

	public String getRndStr() {
		return rndStr;
	}

	@Override
	protected final boolean validateTurnBaseConcrete(Message m) {
		return rndStr != null && rndStr.length() == strLen;
	}

	public static class RandomStringStatement
			extends TurnBaseStatement<ProcessorProvementSequence> {
		private String allRndStr;

		@Override
		protected long getSendTime() {
			if (Glb.getConf().getRunlevel().equals(RunLevel.DEV)) {
				return 1000L * 5;
			}
			//ここはトレランスを小さくみないといけない
			//シーケンス開始タイミングのずれが5秒、送信の遅れが5秒程度と見積もった
			return 1000L * 10;
		}

		@Override
		public long getInteractionTime() {
			//1コアあたり何回回答を作成するか
			int expectedCount = 3;
			return CPUProvement.getComputeTimeMax() * expectedCount;
		}

		public RandomStringStatement(ProcessorProvementSequence seq) {
			this.seq = seq;
			seq.setRndStr(
					RandomStringUtils.randomAscii(RandomString.getStrlen()));
		}

		@Override
		public boolean isSupport(DistributedVoteMessage c) {
			return c instanceof RandomString;
		}

		@Override
		public List<SendData> send() {
			Glb.debug(() -> "");
			RandomString c = new RandomString();
			c.setRndStr(seq.getRndStr());
			c.setTurn(turn);
			return send(c, seq.getNeighborList().getNeighborsUnsafe());
		}

		@Override
		public void interaction(int turn, List<Received> fromNeighbors,
				long interactionStart) {
			//サーバーモードなら回答者にならない
			if (Glb.getConf().isServerMode()) {
				return;
			}

			//rndStrを送信してきた相手に分散合意の相手を限定する
			Map<Long, P2PEdge> update = new ConcurrentHashMap<>();
			for (Received r : fromNeighbors) {
				try {
					//getEdgeは主観の近傍リストから取得するので
					//主観の近傍リストとrndStrの送信者の積集合になる
					P2PEdge n = r.getEdgeByInnermostPackage();
					update.put(n.getEdgeId(), n);
				} catch (Exception e) {
					Glb.debug(e);
					continue;
				}
			}
			//もともとあったneighborsはrndStrの送信のためだけだったということになる
			seq.setNeighborList(new ReadonlyNeighborList(update));

			//全ランダム値作成
			List<String> strs = new ArrayList<String>();
			for (Received r : fromNeighbors) {
				Communicatable c = r.getMessage().getContent();
				if (c instanceof RandomString) {
					RandomString rs = (RandomString) c;
					strs.add(rs.getRndStr());
				} else {
					Glb.getLogger().error("not RandomString", new Exception());
				}
			}
			//ソートすることで全ノードで全ランダム値が同値になる
			Collections.sort(strs);
			//全ランダム値
			StringBuilder allRndStrBuilder = new StringBuilder();
			for (String s : strs)
				allRndStrBuilder.append(s);
			allRndStr = allRndStrBuilder.toString();

			if (allRndStr == null || allRndStr.length() == 0) {
				Glb.getLogger().warn("近傍からの全ランダム値が無い", new Exception());
				return;
			}
			//問題作成情報
			ProblemSrc common = new ProblemSrc(allRndStr);
			//証明処理
			//通信量的にできるだけ単純なクラスで通信するのが良い。resultは送信される
			ResultList answers = new ResultList(common);
			seq.setResult(answers);
			//別スレッドで処理する事で
			//自分の計算処理が遅れたせいで近傍の回答を検証できなくなることを防止する
			//endTimeを設定しているが、1回の計算時間が長いので、
			//P2Pシーケンスの次の段階に進んだのにまだparallelSolveから返ってこないという事態がありうる
			Glb.debug("プロセッサ証明の回答計算開始1");
			Glb.getExecutor().execute(() -> {
				CPUProvement.parallelSolve(answers,
						interactionStart + getInteractionTime(), common);
			});
			Glb.debug("プロセッサ証明の回答計算開始2");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((allRndStr == null) ? 0 : allRndStr.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RandomStringStatement other = (RandomStringStatement) obj;
			if (allRndStr == null) {
				if (other.allRndStr != null)
					return false;
			} else if (!allRndStr.equals(other.allRndStr))
				return false;
			return true;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((rndStr == null) ? 0 : rndStr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RandomString other = (RandomString) obj;
		if (rndStr == null) {
			if (other.rndStr != null)
				return false;
		} else if (!rndStr.equals(other.rndStr))
			return false;
		return true;
	}

}
