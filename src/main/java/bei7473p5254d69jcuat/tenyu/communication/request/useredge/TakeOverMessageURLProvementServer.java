package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement.*;
import glb.*;

public class TakeOverMessageURLProvementServer extends AbstractTakeOverMessage {

	@SuppressWarnings("unchecked")
	@Override
	public URLProvementServer getServer() {
		return Glb.getMiddle().getUrlProvementServer();
	}

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return true;
	}

	public static boolean send(NodeIdentifierUser nextServer) {
		if (nextServer == null)
			return false;
		TakeOverMessageURLProvementServer req = new TakeOverMessageURLProvementServer();
		//送信
		Message reqM = Message.build(req)
				.packaging(req.createPackage(nextServer)).finish();
		return Response.success(Glb.getP2p().requestSync(reqM, nextServer));
	}

}
