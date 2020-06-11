package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core.*;
import glb.*;
import io.netty.channel.*;

/**
 * 客観コアを取得するメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetCore extends P2PEdgeCommonKeyRequest {

	@Override
	protected boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return true;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetCoreResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		GetCoreResponse res = new GetCoreResponse();
		res.setCore(Glb.getObje().getCore());
		Message resM = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class GetCoreResponse extends P2PEdgeCommonKeyResponse {
		private ObjectivityCore core;

		public void setCore(ObjectivityCore core) {
			this.core = core;
		}

		public ObjectivityCore getCore() {
			return core;
		}

		@Override
		protected boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			return core != null;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetCore;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}
	}

}
