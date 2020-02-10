package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import glb.*;
import io.netty.channel.*;

@RequestSequenceStart
public abstract class AbstractTakeOverMessage extends UserRequest {

	/**
	 * TODO Mの代わりに具象クラスを意味するキーワードを使いたい。
	 * 具象クラスをAbstractTakeOverMessageの総称型引数に指定するようにすると、
	 * やはりそこでも警告が出る。
	 *
	 * @return
	 */
	abstract public <M extends AbstractTakeOverMessage,
			S extends TakeOverServer<M>> S getServer();

	@Override
	public boolean isValid(Response res) {
		return res instanceof AbstractStandardResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		NodeIdentifierUser sender = validated.getMessage().getIdentifierUser();
		Glb.getLogger().info("senderUserId=" + sender);

		//送信者はサーバー候補か
		if (sender == null
				|| Glb.getMiddle().getOnlineChecker().isRoleServer(sender))
			return false;

		NodeIdentifierUser nextServer = getServer()
				.getMainServerUserIdSimple(sender);

		//myUserId == receiverUserId
		NodeIdentifierUser me = Glb.getMiddle().getMyNodeIdentifierUser();
		if (nextServer == null || me == null) {
			//客観コアの更新が遅れている可能性があるので少し待って再判定
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			nextServer = getServer().getMainServerUserIdSimple(sender);
			me = Glb.getMiddle().getMyNodeIdentifierUser();
			if (nextServer == null || me == null) {
				Glb.debug("次のメインサーバ役または自分のノード識別子が見つからない。客観不整合の可能性もある",
						new IllegalStateException());
				return false;
			}
		}

		//次のメインサーバが自分じゃなければ
		if (!nextServer.equals(me)) {
			//次のメインサーバに引継ぎ
			return getServer().sendInheritingMessage(nextServer);
		}

		//引継ぎを受け入れる
		getServer().takeover(this);

		//サーバー起動処理。このメソッドは他のタイミングでも呼ばれるが問題無い。
		getServer().checkAndStartOrStop();

		//StandardResponseを標準型とするクラスで、SUCCESSの場合自分で送信する必要がある。
		StandardResponse res = new StandardResponse(ResultCode.SUCCESS);
		Message resM = Message.build(res).packaging(res.createPackage())
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}
}
