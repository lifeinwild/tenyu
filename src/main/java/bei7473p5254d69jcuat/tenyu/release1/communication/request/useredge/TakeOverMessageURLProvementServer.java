package bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.urlprovement.*;

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
