package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import glb.*;

/**
 * ユーザーメッセージリストのメインサーバの引継ぎ
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class TakeOverMessageUserMessageListServer
		extends AbstractTakeOverMessage {
	@SuppressWarnings("unchecked")
	@Override
	public UserMessageListServer getServer() {
		return Glb.getMiddle().getUserMessageListServer();
	}

	public static boolean send(NodeIdentifierUser nextServerNode,
			UserMessageList data) {
		TakeOverMessageUserMessageListServer req = new TakeOverMessageUserMessageListServer();
		req.setMessages(data);

		//送信
		Message reqM = Message.build(req)
				.packaging(req.createPackage(nextServerNode)).finish();
		return Response.success(Glb.getP2p().requestSync(reqM, nextServerNode));
	}

	/**
	 * 引継ぎの情報
	 */
	private UserMessageList messages;

	public TakeOverMessageUserMessageListServer() {
	}

	public void setMessages(UserMessageList messages) {
		this.messages = messages;
	}

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return messages != null && messages.size() > 0;
	}

	public UserMessageList getMessages() {
		return messages;
	}

}
