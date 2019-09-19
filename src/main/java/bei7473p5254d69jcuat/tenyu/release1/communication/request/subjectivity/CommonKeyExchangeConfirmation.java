package bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.P2PEdgeCommonKeyPackageUnsecure.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import io.netty.channel.*;

/**
 * CommonKeyExchangeだけでは受け取った共通鍵が攻撃者が送り込んだものである
 * 可能性があるが、いったん共通鍵で互いに情報交換に成功するとその可能性が無くなる。
 * @author exceptiontenyu@gmail.com
 *
 */
public class CommonKeyExchangeConfirmation extends Request
		implements P2PEdgeCommonKeyPackageUnsecureContent {
	private byte[] confirmation;

	public CommonKeyExchangeConfirmation() {
	}

	public CommonKeyExchangeConfirmation(byte[] confirmation) {
		this.confirmation = confirmation;
	}

	public byte[] getConfirmation() {
		return confirmation;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof CommonKeyExchangeConfirmationResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext con, Received validated) {
		P2PEdge n = validated.getEdgeByInnermostPackage();
		if (n == null) {
			Glb.getLogger().warn("p2pEdge is null", new IllegalStateException());
			return false;
		}

		CommonKeyExchangeState info = n.getCommonKeyExchangeState();
		CommonKeyExchangeState infoOther = n.getFromOther()
				.getCommonKeyExchangeState();
		if (!Arrays.equals(confirmation, info.getConfirmation()))
			return false;
		info.setSucceed(true);
		info.setUpdateEnd();
		infoOther.setSucceed(true);
		infoOther.setUpdateEnd();

		CommonKeyExchangeConfirmationResponse res = new CommonKeyExchangeConfirmationResponse(
				n.getFromOther().getCommonKeyExchangeState().getConfirmation());

		Message m = Message.build(res).packaging(res.createPackage(n)).finish();
		Glb.getP2p().response(m, con);
		if (!Recognition.createOrUpdateAndRemoveFromUnsecureList(n))
			Glb.getLogger().error(
					"createOrUpdateAndRemoveFromUnsecureList() failed",
					new Exception());
		return true;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return confirmation != null && confirmation.length == Glb.getConst()
				.getCommonKeyConfirmationSize();
	}

	public static class CommonKeyExchangeConfirmationResponse extends Response
			implements P2PEdgeCommonKeyPackageUnsecureContent {
		private byte[] confirmation;

		public CommonKeyExchangeConfirmationResponse() {
		}

		public CommonKeyExchangeConfirmationResponse(byte[] confirmation) {
			this.confirmation = confirmation;
		}

		public byte[] getConfirmation() {
			return confirmation;
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			return confirmation != null && confirmation.length == Glb.getConst()
					.getCommonKeyConfirmationSize();
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof CommonKeyExchangeConfirmation;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			P2PEdge n = validated.getEdgeByInnermostPackage();
			if (n == null) {
				Glb.getLogger().warn("p2pedge is null", new IllegalStateException());
				return false;
			}
			CommonKeyExchangeState info = n.getCommonKeyExchangeState();
			CommonKeyExchangeState infoOther = n.getFromOther()
					.getCommonKeyExchangeState();

			if (!Arrays.equals(confirmation, info.getConfirmation())) {
				Glb.getLogger().warn("confirmation is invalid", new IllegalStateException());
				return false;
			}
			info.setSucceed(true);
			info.setUpdateEnd();

			infoOther.setSucceed(true);
			infoOther.setUpdateEnd();

			return true;
		}
	}
}
