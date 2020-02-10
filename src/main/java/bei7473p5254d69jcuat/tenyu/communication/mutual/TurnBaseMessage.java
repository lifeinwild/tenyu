package bei7473p5254d69jcuat.tenyu.communication.mutual;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.Conf.*;

/**
 * ターンベース
 *
 * P2P命令が全ノード同時に同じものが実行され、
 * ターンベースメッセージはさらに通信されるメッセージについても
 * タイミングが制限される。
 * 1P2P命令の中に複数ターンがある。
 * 相互通信がメッセージ交換のタイミングで、
 * それで得た近傍からの多数のメッセージを処理する段階が相互作用である。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TurnBaseMessage extends DistributedVoteMessage {
	/**
	 * 分散合意のターン
	 */
	private int turn;
	private static final int turnMax = 1000;

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	@Override
	protected final boolean validateDistributedVoteConcrete(Message m) {
		return turn >= 0 && turn < turnMax && validateTurnBaseConcrete(m);
	}

	protected abstract boolean validateTurnBaseConcrete(Message m);

	public static abstract class TurnBaseStatement<T extends P2PSequence>
			extends P2PStatement<T> {

		/**
		 * @return	1ターンの時間
		 */
		protected long getTurnTime() {
			return getSendTime() + getInteractionTime() + getTurnOtherTime();
		}

		protected long getTurnOtherTime() {
			return 1000;
		}

		public long getStatementTime() {
			return getTurnTime() * getTurnCountMax() + getStartTime()
					+ getEndTime() + P2PSequence.getLaunchTimingTolerance() * 2;
		}

		/**
		 * 相互通信にかかる時間。
		 * 各具象クラスでその相互通信情報の大きさに応じて十分に長い時間を設定する。
		 * トレランスを大きくみて良い場合もあるし、そうでない場合もある。
		 */
		protected long getSendTime() {
			if (Glb.getConf().getRunlevel().equals(RunLevel.DEV)) {
				return 1000L * 2;
			}
			return 1000L * 15;
		}

		/**
		 * 繰り返し回数
		 */
		public int getTurnCountMax() {
			return 1;
		}

		protected int turn = 0;

		public int getTurn() {
			return turn;
		}

		/**
		 * 1回の相互作用の時間
		 * 各具象クラスでその相互作用関数の内容に応じて十分に長い時間を設定する。
		 * 基本的にsuper.getWaitInteraction()に加算して返すべき。
		 */
		public long getInteractionTime() {
			if (Glb.getConf().getRunlevel().equals(RunLevel.DEV)) {
				return 1000L * 5;
			}
			return 1000L * 10;
		}

		/**
		 * 他ノードにメッセージを送る
		 * RestrictedTimingMessageをP2P#kryoSetup()に追記する必要がある。
		 */
		public abstract List<SendData> send();

		/**
		 * sendToにメッセージcを送る
		 */
		protected List<SendData> send(TurnBaseMessage c,
				Collection<P2PEdge> sendTo) {
			Glb.debug(() -> c.getClass().getSimpleName() + " sendTo=" + sendTo);

			List<SendData> r = new ArrayList<>();
			for (P2PEdge a : sendTo)
				r.add(new SendData(a, c));
			return r;
		}

		@Override
		public void reset() {
			votes.clear();
		}

		@Override
		public boolean receive(Received r, DistributedVoteMessage c) {
			Glb.debug(() -> "TurnBaseStatement#receive()");
			if (!receivable)
				return false;

			if (votes.size() > Glb.getSubje().getNeighborList()
					.getNeighborMax())
				return false;

			if (!(c instanceof TurnBaseMessage))
				return false;

			TurnBaseMessage message = (TurnBaseMessage) c;
			Glb.debug(() -> "message_turn=" + message.getTurn()
					+ " sequence_turn=" + turn);
			if (message.getTurn() != turn)
				return false;

			votes.put(r.getEdgeByInnermostPackage().getEdgeId(), r);
			return true;
		}

		/**
		 * その命令に対応するメッセージが近傍から送られてくる。
		 * 次のsend()を準備、または命令の結果値を準備
		 * interactionStart + getInteractionTime()までに終わるような処理であるべき
		 * @param turn				現在のターン
		 * @param fromNeighbors		近傍からきた相互通信情報一覧
		 * @param interactionStart	全ノードがこの時間に開始したと考えられる日時
		 */
		public abstract void interaction(int turn, List<Received> fromNeighbors,
				long interactionStart);

		/**
		 * p2pを通じて送られてきたメッセージ。毎ターンリセットされる。
		 * Mapにしているので、同じ公開鍵で複数のメッセージを同じターンに入れられない。
		 */
		protected Map<Long, Received> votes = new ConcurrentHashMap<>();

		/**
		 * 命令開始時に呼ばれる
		 */
		protected void start(long statementStart) {
		}

		/**
		 * 命令終了時に呼ばれる
		 * 相互作用関数が1ターンしか実行されない場合、
		 * interaction()の最後にend()の内容を書くことができる。
		 * 複数ターン実行される場合、interaction()は複数回呼ばれるが、
		 * end()は1回しか呼ばれない。これはstart()も同様である。
		 */
		protected void end(long statementStart) {
		}

		public long getStartTime() {
			return 0;
		}

		public long getEndTime() {
			return 0;
		}

		@Override
		public void run(long statementStart, int counter) {
			long elapsed = 0;
			start(statementStart);
			if (getStartTime() > 0) {
				elapsed += getStartTime();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"start()後同期" + seq.log());
			}
			for (turn = 0; turn < getTurnCountMax(); turn++) {
				Glb.debug(() -> "turn:" + turn);
				//遅れていたらターンを飛ばす	ラグ対策
				int currentTurn = (int) ((elapsed - getStartTime())
						/ getTurnTime());
				if (currentTurn >= getTurnCountMax())
					break;
				if (currentTurn > turn)
					turn = currentTurn;

				//直前までの近傍からの相互通信情報をクリア
				votes.clear();

				Glb.debug("turn=" + turn + " statementStart=" + statementStart
						+ " elapsed=" + elapsed + " currentTimeMillis="
						+ System.currentTimeMillis());

				elapsed += getTurnOtherTime();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"turn前後の雑多な処理の同期" + seq.log());
				//受信可能に	TODO:本当はturn前後の雑多な処理の同期の後に
				//受信可能にすべきだが、それだと最速で処理するノードが最も遅いノードに
				//送信した場合、受信可能になる前に送信してしまう。
				//問題はシーケンスの開始タイミングがノードによって最大で4秒ほどずれる事にある。
				receivable = true;
				elapsed += P2PSequence.getLaunchTimingTolerance();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"受信可能直後待機。起動タイミングのずれに対応するため" + seq.log());

				//相互通信情報の送信
				List<SendData> send = send();

				if (send != null && send.size() > 0) {
					for (SendData e : send) {
						TurnBaseMessage c = e.getMessage();
						if (c == null || e.getEdge() == null) {
							Glb.debug(() -> "送信先がnull");
							continue;
						}
						c.setChannel(seq.getChannel());
						c.setTurn(turn);
						P2PEdge to = e.getEdge();
						Message m = Message.build(c)
								.packaging(c.createPackage(to)).finish();
						Glb.getP2p().sendAsync(m, to);
					}
				}
				elapsed += getSendTime();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"相互通信の同期" + seq.log());

				elapsed += P2PSequence.getLaunchTimingTolerance();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"受信不可直前待機。起動タイミングのずれに対応するため" + seq.log());

				//受信不可に	このタイミングはトレランス不要
				receivable = false;

				List<Received> votesTmp = null;
				Glb.debug("votesTmp");
				votesTmp = new ArrayList<Received>(votes.values());

				if (Glb.getConf().isDevOrTest()) {
					StringBuilder sb = new StringBuilder();
					for (Received r : votesTmp) {
						P2PEdge n = r.getEdgeByInnermostPackage();
						sb.append("- edgeId=" + n.getEdgeId() + " isa="
								+ n.getNode().getISAP2PPort()
								+ System.lineSeparator());
					}
					Glb.debug(
							getClass().getSimpleName() + " 近傍からの相互通信情報:" + sb);
				}

				//近傍から相互通信情報が0件だったら何もしない
				if (votesTmp.size() > 0) {
					//相互作用関数
					interaction(turn, votesTmp, statementStart + elapsed);
				}
				elapsed += getInteractionTime();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"相互作用関数の同期" + seq.log());
			}
			end(statementStart);
			if (getEndTime() > 0) {
				elapsed += getEndTime();
				Glb.getUtil().sleepUntil(statementStart + elapsed,
						"end()後同期" + seq.log());
			}
		}

		public static class SendData {
			private P2PEdge edge;
			private TurnBaseMessage message;

			public SendData(P2PEdge n, TurnBaseMessage m) {
				this.edge = n;
				this.message = m;
			}

			public P2PEdge getEdge() {
				return edge;
			}

			public TurnBaseMessage getMessage() {
				return message;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + turn;
			result = prime * result + ((votes == null) ? 0 : votes.hashCode());
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
			TurnBaseStatement<?> other = (TurnBaseStatement<?>) obj;
			if (turn != other.turn)
				return false;
			if (votes == null) {
				if (other.votes != null)
					return false;
			} else if (!votes.equals(other.votes))
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + turn;
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
		TurnBaseMessage other = (TurnBaseMessage) obj;
		if (turn != other.turn)
			return false;
		return true;
	}
}
