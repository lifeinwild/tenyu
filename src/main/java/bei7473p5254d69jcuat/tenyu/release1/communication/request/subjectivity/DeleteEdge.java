package bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.P2PEdgeCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import io.netty.channel.*;

/**
 * 定期的なP2Pエッジ削除処理で削除した相手に削除した事を通知するメッセージ。
 * Response不要なのでRequestではなくContent
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class DeleteEdge extends Content
		implements P2PEdgeCommonKeyPackageContent {
	@Override
	protected final boolean validateConcreteContent(Message m) {
		return true;
	}

	/**
	 * 主観から近傍を削除し、削除された近傍にその通知を送信する。
	 * @param n		削除対象
	 * @return		削除に成功したか。削除通知の送信に失敗しても自分側で削除に成功しただけでtrueになる
	 */
	public static boolean send(P2PEdge n) {
		DeleteEdge c = new DeleteEdge();
		//削除した後では梱包処理ができないので先にメッセージを作る
		Message m = Message.build(c).packaging(c.createPackage(n)).finish();
		//次に削除する
		if (Glb.getSubje().getNeighborList().removeNeighbor(n.getEdgeId())) {
			//削除が成功したら送信する
			Glb.getP2p().sendAsync(m, n);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean received(ChannelHandlerContext con, Received validated) {
		P2PEdge n = validated.getEdgeByInnermostPackage();
		Glb.debug(() -> "DeleteEdge#received():" + n);

		//自分の方でも削除する。基本的にp2pエッジは双方向であるべきなので
		Glb.getSubje().getNeighborList()
				.removeNeighbor(n.getNode().getP2PNodeId());
		return true;
	}
}
