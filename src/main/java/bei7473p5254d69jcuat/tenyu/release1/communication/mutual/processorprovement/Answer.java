package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement.ProcessorProvementSequence.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement.ResultList.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Conf.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;

/**
 * 回答
 * @author exceptiontenyu@gmail.com
 *
 */
public class Answer extends TurnBaseMessage {
	/**
	 * 近傍の答え
	 */
	private ResultList answer;

	public ResultList getAnswer() {
		return answer;
	}

	public void setAnswer(ResultList answer) {
		this.answer = answer;
	}

	@Override
	protected final boolean validateTurnBaseConcrete(Message m) {
		return answer != null;
	}

	/**
	 * 回答受付。
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class AnswerStatement
			extends TurnBaseStatement<ProcessorProvementSequence> {
		@Override
		public long getInteractionTime() {
			//想定される最低コア数	全コアを使う事を考えないので３コア
			int defaultCoreNumber = 4 - 1;
			long tolerance = 1000L * 10;
			if (Glb.getConf().getRunlevel().equals(RunLevel.DEV))
				return (CPUProvement.getVerifytime() * P2PEdge.getScoreMax())
						/ defaultCoreNumber + tolerance;
			//検証処理の最長時間
			long verifyTime = (CPUProvement.getVerifytime() //1回の検証時間
					* P2PEdge.getScoreMax() //一人あたりの最大回答件数
					* Glb.getSubje().getNeighborList().getNeighborMax()) //最大近傍数
					/ defaultCoreNumber + tolerance; //コア数で割る、トレランス足す

			return verifyTime;
		}

		public AnswerStatement(ProcessorProvementSequence seq) {
			this.seq = seq;
		}

		@Override
		public boolean isSupport(DistributedVoteMessage c) {
			return c instanceof Answer;
		}

		@Override
		public List<SendData> send() {
			if (seq.getResult() == null)
				return null;
			ResultList r = seq.getResult();
			synchronized (r) {
				if (r == null || r.getSolves().size() == 0)
					return null;
			}
			Answer c = new Answer();
			c.setAnswer(r);
			return send(c, seq.getNeighborList().getNeighborsUnsafe());
		}

		@Override
		public void interaction(int turn, List<Received> fromNeighbors,
				long interactionStart) {
			Glb.debug(() -> "fromNeighbors:" + fromNeighbors);

			//ノード検証処理の定義
			MultiThreadTask t = new MultiThreadTask() {
				@Override
				public void call(int taskId) throws Exception {
					if (fromNeighbors.size() <= taskId) {
						throw new InterruptedException();
					}

					//1ノードの回答を取得
					Received r = fromNeighbors.get(taskId);
					Glb.debug(() -> "taskId=" + taskId + " p2pPort="
							+ r.getEdgeByInnermostPackage().getNode().getP2pPort());

					//エッジを特定できるか
					P2PEdge n = r.getEdgeByInnermostPackage();
					if (n == null)
						return;

					//1ノードを検証しスコアを得る
					int score = verifyNode(r);
					if (score <= 0)
						return;

					//証明成功通知用
					seq.addScore(n, score);
				}
			};

			//並列処理で検証する
			Glb.getUtil().parallelTask(interactionStart + getInteractionTime(),
					P2PEdge.getScoreMax(), t);
			Glb.debug("Scores:" + seq.getScores());

			if (seq.getScores() == null || seq.getScores().size() == 0)
				return;

			//スコア承認
			//スコアを獲得したノードにスコアを与えるだけではなく、
			//スコアを獲得しなかったノードに0点を与える必要がある。
			for (P2PEdge n : Glb.getSubje().getNeighborList()
					.getNeighborsUnsafe()) {
				int score = 0;
				for (Notification s : seq.getScores()) {
					if (s.getEdge().equals(n)) {
						score = s.getScore();
					}
				}
				n.updateProcessorScore(score);
			}
		}

		/**
		 * @param received		近傍から提出された回答一覧を含むメッセージ
		 */
		private int verifyNode(Received received) {
			Communicatable content = received.getMessage().getContent();
			if (!(content instanceof Answer)) {
				return 0;
			}
			Answer a = (Answer) content;
			byte[] creatorP2PNodeId = received.getEdgeByInnermostPackage().getNode()
					.getP2PNodeId().getIdentifier();

			try {
				//検証開始日時
				long start = System.currentTimeMillis();
				//1人当たりの最長検証時間
				long time = CPUProvement.getVerifytime()
						* P2PEdge.getScoreMax();
				//検証終了日時
				long endTime = start + time;

				//このノードが獲得するスコア
				int score = 0;

				//重複チェック用
				Map<ByteArrayWrapper, ProblemSrc> dup = new HashMap<>();

				//検証
				//問題作成情報の共通部分
				ProblemSrc common = a.getAnswer().getCommon();
				//並列に解かれた各答えの検証
				for (ParallelNumberAndSolve ps : a.getAnswer().getSolves()) {
					//1ノードが大量の回答を寄せると他のノードがスコアを得られないので
					//1ノードあたりに使う時間を制限する
					//endTimeはこのノードに与えられた時間制限
					if (System.currentTimeMillis() > endTime)
						break;
					ProblemSrc p = new ProblemSrc(common,
							ps.getParallelNumber());

					//自分が送信したランダム値が含まれているか
					if (!p.getRndStr().contains(seq.getRndStr())) {
						Glb.getLogger().warn(Lang.INVALID_RESULT + ":rndStr");
						continue;
					}
					//この回答はいくつのノードに証明しようとしているか、制限以内か
					int nodeCount = p.getRndStr().length()
							/ RandomString.getStrlen();
					if (nodeCount > seq.getNodeMax()) {
						Glb.getLogger().warn(
								Lang.INVALID_RESULT + ":nodeCount" + nodeCount);
						continue;
					}

					//問題作成情報が依存している日時情報は現在日時か
					Calendar c = Calendar.getInstance(Locale.JAPAN);
					final int year = c.get(Calendar.YEAR);
					final int month = c.get(Calendar.MONTH);
					final int date = c.get(Calendar.DATE);
					final int hour = c.get(Calendar.HOUR);
					if (p.getYear() != year || p.getMonth() != month
							|| p.getDay() != date || p.getHour() != hour) {
						Glb.getLogger().warn(Lang.INVALID_RESULT + ":date");
						continue;
					}

					//問題作成情報を作成したノードIDはAnswerを送信したノードと一致するか
					if (!Arrays.equals(p.getP2PNodeId(), creatorP2PNodeId)) {
						Glb.getLogger()
								.warn(Lang.INVALID_RESULT + ":p2pNodeId");
						continue;
					}

					//この回答は既に評価済みか
					//※この処理によって、同じ公開鍵で多数のノードを作成し
					//1ノードに1つの公開鍵で多数のP2Pエッジを作成し
					//1つの回答で多量のスコアを得ようとするタイプの不正行為を防げる
					MessageDigest md = MessageDigest
							.getInstance(Glb.getConst().getDigestAlgorithm());
					ByteArrayWrapper dupKey = new ByteArrayWrapper(
							p.createHash(md));
					Object o = dup.get(dupKey);
					if (o != null)
						continue;
					dup.put(dupKey, p);

					//回答を検証
					try {
						if (new CPUProvement(p).verify(ps.getSolve())) {
							score += 1;
							Glb.debug("検証完了 parallelNumber="
									+ ps.getParallelNumber() + " "
									+ (System.currentTimeMillis() - start)
									+ "ms");
						} else {
							Glb.getLogger()
									.warn(Lang.INVALID_RESULT + ":solve");
						}
					} catch (Exception e) {
						Glb.getLogger().error("", e);
					}
				}
				return score;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return 0;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
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
		Answer other = (Answer) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		return true;
	}

}
