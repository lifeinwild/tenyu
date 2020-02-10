package bei7473p5254d69jcuat.tenyu.communication.mutual;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.P2PEdgeCommonKeyPackage.*;
import glb.*;
import io.netty.channel.*;

/**
 * 相互通信情報
 * これ系の汎用received()実装があり、子クラスはreceivedを実装しない。
 * sequence#receive()に渡してそこでメッセージを処理することは、
 * シーケンス上で今その命令を処理している段階である場合のみ処理される事を意味する。
 * つまりメッセージが勝手にシステムの状態を変更するのではなく、
 * シーケンスの状態に応じて処理するかをシーケンス側が決定する。
 * シーケンスからさらにP2P命令に渡されてそこで処理方法が決定する場合もある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class DistributedVoteMessage extends MessageContent
		implements P2PEdgeCommonKeyPackageContent {
	private String channel;

	@Override
	protected final boolean validateConcreteContent(Message m) {
		return validateDistributedVoteConcrete(m);
	}

	protected abstract boolean validateDistributedVoteConcrete(Message m);

	@Override
	public boolean received(ChannelHandlerContext con, Received validated) {
		Glb.debug("received channel=" + channel);

		//シーケンスにメッセージを送る
		P2PSequence seq = Glb.getP2p().getChannelToSeq().get(channel);
		if (seq == null)
			Glb.debug(() -> "存在しないシーケンス channel=" + channel + ":"
					+ getClass().getSimpleName());
		if (!seq.receive(validated))
			Glb.debug(() -> "シーケンスが受信拒否" + getClass().getSimpleName());

		Glb.debug(() -> this.getClass().getSimpleName() + " received.");
		return true;
	}

	public DistributedVoteMessage() {
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
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
		DistributedVoteMessage other = (DistributedVoteMessage) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		return true;
	}

}
