package bei7473p5254d69jcuat.tenyu.communication.mutual;

import org.apache.commons.lang.builder.*;

import bei7473p5254d69jcuat.tenyu.communication.*;

/**
 * 分散合意。P2P的命令。
 * 相互通信情報はP2PStatementの具象クラスに定義される。
 * 他のP2PStatementとの情報のやり取りはシーケンスで管理される。
 * 基本方針として、命令の知識を必要とするデータは命令内に留まるべきで、つまり
 * 他の命令とやりとりされる情報はIPアドレスなどより普遍的であるべき。
 * アプリ性のある情報でもより大きな範囲で意識される情報であるべき。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class P2PStatement<T extends P2PSequence> {
	/**
	 * メッセージのタイミングがばらばらなタイプのP2P命令
	 * @author exceptiontenyu@gmail.com
	 *
	 * @param <T>
	 */
	public static abstract class P2PStatementFreeTiming<T extends P2PSequence>
			extends P2PStatement<T> {
		@Override
		public boolean receive(Received r, DistributedVoteMessage c) {
			return false;
		}

		@Override
		public boolean isSupport(DistributedVoteMessage m) {
			return false;
		}
	}

	/**
	 * 通信が一切ないタイプ
	 * @author exceptiontenyu@gmail.com
	 *
	 * @param <T>
	 */
	public static abstract class P2PStatementNoCommunication<
			T extends P2PSequence> extends P2PStatement<T> {
		@Override
		public boolean receive(Received r, DistributedVoteMessage c) {
			return false;
		}

		@Override
		public boolean isSupport(DistributedVoteMessage m) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(seq).append(receivable)
				.toHashCode();
	}

	@Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof P2PStatement))
			return false;
		if (o2 == this)
			return true;

		P2PStatement<?> o = (P2PStatement<?>) o2;
		return new EqualsBuilder().append(seq, o.seq)
				.append(receivable, o.receivable).isEquals();
	}

	protected T seq;
	/**
	 * 相互通信情報を受信可能な状態か
	 */
	protected volatile boolean receivable = false;

	/**
	 * @return	このステートメントを実行するのに必要な最長時間
	 */
	public abstract long getStatementTime();

	/**
	 * メッセージを受信する
	 */
	public abstract boolean receive(Received r, DistributedVoteMessage c);

	/**
	 * 命令を実行する。statementStart+statementTimeMaxまでに終わるような処理にすべき
	 * @param statementStart	命令開始日時
	 * @param counter			シーケンス上の何番目の命令か
	 */
	public abstract void run(long statementStart, int counter);

	/**
	 * 状態を初期化する
	 */
	public abstract void reset();

	/**
	 * 対応するメッセージクラスか。末端の子クラスで実装される。
	 * @param m		対象
	 * @return		対応しているか
	 */
	public abstract boolean isSupport(DistributedVoteMessage m);

	/**
	 * @return	相互通信情報の最大サイズ
	 */
	public int getSendBufferSize() {
		return 1000 * 10;
	}
}