package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.UserCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

public class CommonKeyExchangeConfirmationUser extends Request
		implements UserCommonKeyPackageContent {
	private AddrInfo addr;
	private int nodeNumber;
	private byte[] confirmation;

	public CommonKeyExchangeConfirmationUser(byte[] confirmation, AddrInfo addr,
			int nodeNumber) {
		this.confirmation = confirmation;
		this.addr = addr;
		this.nodeNumber = nodeNumber;
	}

	public AddrInfo getAddr() {
		return addr;
	}

	public byte[] getConfirmation() {
		return confirmation;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof CommonKeyExchangeConfirmationUserResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		NodeIdentifierUser requester = validated.getMessage().getIdentifierUser();
		if (requester == null) {
			Glb.debug("requesterがnull");
			return false;
		}
		UserEdge e = Glb.getMiddle().getUserEdgeList()
				.getFromUnsecure(requester);

		if (!Arrays.equals(confirmation, e.getConfirmationFromMe())) {
			Glb.debug("確認情報が違う");
			return false;
		}

		if (!Glb.getMiddle().getUserEdgeList().confirmation(requester))
			return false;

		CommonKeyExchangeConfirmationUserResponse res = new CommonKeyExchangeConfirmationUserResponse(
				e.getConfirmationFromOther());
		Message resM = Message.build(res)
				.packaging(res.createPackage(requester)).finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public void setConfirmation(byte[] confirmation) {
		this.confirmation = confirmation;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		if (confirmation == null) {
			Glb.debug("confirmation null");
			return false;
		}
		if (confirmation.length != Glb.getConst()
				.getCommonKeyConfirmationSize()) {
			Glb.debug("confirmation too long");
			return false;
		}

		if (addr == null) {
			Glb.debug("addr null");
			return false;
		}
		ValidationResult r = new ValidationResult();
		if (!addr.validateAtCreate(r)) {
			Glb.debug("addr invalid");
			return false;
		}
		if (nodeNumber < 0)
			return false;

		return true;
	}

	public static class CommonKeyExchangeConfirmationUserResponse
			extends Response implements UserCommonKeyPackageContent {
		private byte[] confirmation;
		private int nodeNumber;
		private AddrInfo addr;

		public CommonKeyExchangeConfirmationUserResponse(byte[] confirmation) {
			this.confirmation = confirmation;
		}

		public byte[] getConfirmation() {
			return confirmation;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof CommonKeyExchangeConfirmationUser;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			NodeIdentifierUser responder = validated.getMessage().getIdentifierUser();
			if (responder == null) {
				Glb.debug("responderがnull");
				return false;
			}

			UserEdge e = Glb.getMiddle().getUserEdgeList()
					.getFromUnsecure(responder);

			if (!Arrays.equals(confirmation, e.getConfirmationFromMe())) {
				Glb.debug("確認情報が違う");
				return false;
			}

			if (!Glb.getMiddle().getUserEdgeList().confirmation(responder))
				return false;

			return true;
		}

		public void setConfirmation(byte[] confirmation) {
			this.confirmation = confirmation;
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			if (confirmation == null) {
				Glb.debug("confirmation null");
				return false;
			}
			if (confirmation.length != Glb.getConst()
					.getCommonKeyConfirmationSize()) {
				Glb.debug("confirmation too long");
				return false;
			}

			if (addr == null) {
				Glb.debug("addr null");
				return false;
			}
			ValidationResult r = new ValidationResult();
			if (!addr.validateAtCreate(r)) {
				Glb.debug("addr invalid");
				return false;
			}

			if (nodeNumber < 0)
				return false;

			return true;
		}

	}
}
