package bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.PlainPackage.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import io.netty.channel.*;

public class CommonKeyExchange extends Request implements PlainPackageContent {
	/**
	 * CKIが暗号化されたデータ
	 */
	private byte[] encrypted;
	/**
	 * 受信者から送信者へのエッジのID
	 */
	private long edgeIdReceiverToSender;

	public CommonKeyExchange() {
	}

	public CommonKeyExchange(byte[] encrypted, P2PEdge to) {
		this.encrypted = encrypted;
		edgeIdReceiverToSender = to.getFromOther().getEdgeId();
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof CommonKeyExchangeResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		P2PEdge n = Glb.getSubje().getUnsecureNeighborList()
				.getNeighbor(edgeIdReceiverToSender);
		if (n == null || !n.getCommonKeyExchangeState().checkAndSet())
			return false;

		//復号化	送信者が受信者の公開鍵で暗号化して送信してきている
		//ディテクターでKeyTypeを取得するようにもできるが
		//このメッセージはStandardで良い。ノードとして主張する公開鍵は1つのみ
		byte[] serialized = Glb.getConf()
				.decryptByMyStandardPrivateKey(encrypted);
		CommonKeyInfo cki = CommonKeyInfo.deserialize(serialized);
		byte[] confirmation = CommonKeyInfo.deserializeConfirmation(serialized);

		//相手が作成したck,iv,co
		CommonKeyExchangeState infoOther = n.getFromOther()
				.getCommonKeyExchangeState();
		infoOther.setCommonKeyInfo(cki);
		infoOther.setConfirmation(confirmation);

		CommonKeyInfo myCki = CommonKeyInfo.build();
		byte[] co = new byte[Glb.getConst().getCommonKeySizeForCommunication()];
		Glb.getRnd().nextBytes(co);

		byte[] resSerialized = CommonKeyInfo.serialize(myCki, co);
		byte[] resEncrypted = Glb.getUtil().encryptByPublicKey(
				n.getNode().getPubKey().getByteArray(), resSerialized);
		CommonKeyExchangeResponse res = new CommonKeyExchangeResponse(
				resEncrypted, n);

		//自分が作成したck,iv,co
		CommonKeyExchangeState info = n.getCommonKeyExchangeState();
		info.setCommonKeyInfo(myCki);
		info.setConfirmation(co);

		info.setUpdateStart();
		infoOther.setUpdateStart();
		Message m = Message.build(res).packaging(res.createPackage()).finish();
		Glb.getP2p().response(m, ctx);
		return true;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return encrypted != null && encrypted.length > 0;
	}

	public static class CommonKeyExchangeResponse extends Response
			implements PlainPackageContent {
		private byte[] encrypted;
		private long edgeIdRequesterToResponder;

		public CommonKeyExchangeResponse() {
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			return encrypted != null && encrypted.length > 0;
		}

		public CommonKeyExchangeResponse(byte[] encrypted, P2PEdge to) {
			this.encrypted = encrypted;
			edgeIdRequesterToResponder = to.getFromOther().getEdgeId();
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof CommonKeyExchange;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			P2PEdge n = Glb.getSubje().getUnsecureNeighborList()
					.getNeighbor(edgeIdRequesterToResponder);
			if (n == null)//ここではisCommonKeyUpdatableをチェックしない。既にスタート済み
				return false;
			//復号化	返信者がリクエスト送信者の公開鍵で暗号化したもの
			byte[] serialized = Glb.getConf()
					.decryptByMyStandardPrivateKey(encrypted);
			CommonKeyInfo cki = CommonKeyInfo.deserialize(serialized);
			byte[] confirmation = CommonKeyInfo
					.deserializeConfirmation(serialized);

			CommonKeyExchangeState infoFromOther = n.getFromOther()
					.getCommonKeyExchangeState();

			infoFromOther.setCommonKeyInfo(cki);
			infoFromOther.setConfirmation(confirmation);
			return true;
		}

	}

}
