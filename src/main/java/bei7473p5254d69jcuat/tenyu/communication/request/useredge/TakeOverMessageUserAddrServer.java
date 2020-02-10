package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import java.net.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import glb.*;

@RequestSequenceStart
public class TakeOverMessageUserAddrServer extends AbstractTakeOverMessage {

	@SuppressWarnings("unchecked")
	@Override
	public UserAddrServer getServer() {
		return Glb.getMiddle().getUserAddrServer();
	}

	public static boolean send(NodeIdentifierUser nextServerIdentifier,
			Map<NodeIdentifierUser, InetSocketAddress> data) {
		Long nextServerUserId = nextServerIdentifier.getUserId();
		if (nextServerUserId == null)
			return false;
		TakeOverMessageUserAddrServer req = new TakeOverMessageUserAddrServer();
		req.setUserNodeToAddr(data);

		//送信
		Message reqM = Message.build(req)
				.packaging(req.createPackage(nextServerIdentifier)).finish();
		return Response
				.success(Glb.getP2p().requestSync(reqM, nextServerIdentifier));
	}

	/**
	 * 引継ぎの情報
	 */
	private Map<NodeIdentifierUser, InetSocketAddress> userNodeToAddr;

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return userNodeToAddr != null && userNodeToAddr.size() > 0;
	}

	public Map<NodeIdentifierUser, InetSocketAddress> getUserNodeToAddr() {
		return userNodeToAddr;
	}

	public void setUserNodeToAddr(
			Map<NodeIdentifierUser, InetSocketAddress> userNodeToAddr) {
		this.userNodeToAddr = userNodeToAddr;
	}
}
