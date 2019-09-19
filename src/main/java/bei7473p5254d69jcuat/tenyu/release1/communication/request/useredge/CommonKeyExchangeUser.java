package bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge;

import java.net.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.SignedPackage.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.CommonKeyExchangeConfirmationUser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import io.netty.channel.*;

/**
 * User間での共通鍵交換
 * C/S的
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class CommonKeyExchangeUser extends Request
		implements SignedPackageContent {
	/**
	 * 送信に使った鍵タイプ
	 */
	private KeyType keyType;
	/**
	 * CKIが暗号化されたデータ
	 */
	private byte[] encrypted;
	/**
	 * この鍵タイプで返信する
	 */
	private KeyType resKeyType;

	/**
	 * 送信側ノード
	 */
	private NodeIdentifierUser sender;

	public CommonKeyExchangeUser(byte[] encrypted, KeyType keyType,
			KeyType resKeyType, NodeIdentifierUser sender) {
		super();
		this.keyType = keyType;
		this.encrypted = encrypted;
		this.resKeyType = resKeyType;
		this.sender = sender;
	}

	public CommonKeyExchangeUser() {
	}

	/**
	 * ユーザーとの共通鍵交換
	 * {@link UserCommonKeyPackage}が使用可能になる
	 * @param identifier	このユーザーと共通鍵を交換する
	 * @return
	 */
	public static boolean send(NodeIdentifierUser identifier) {
		if (identifier == null)
			return false;
		User u = Glb.getObje().getUser(us -> us.get(identifier.getUserId()));
		return send(identifier,
				u.tryToGetISAP2PPort(identifier.getNodeNumber()));
	}

	public static boolean send(NodeIdentifierUser identifier,
			InetSocketAddress addr) {
		NodeIdentifierUser sender = Glb.getMiddle().getMyNodeIdentifierUser();
		if (identifier == null || addr == null || sender == null)
			return false;
		UserEdgeList l = Glb.getMiddle().getUserEdgeList();
		//最近交換を開始していたなら新たに開始しない
		if (l.isStartedRecently(identifier))
			return false;

		//共通鍵交換を開始する。既に交換済みの共通鍵がある場合上書きされる

		//交換相手のユーザー情報を取得
		User u = Glb.getObje().getUser(us -> us.get(identifier.getUserId()));

		//共通鍵作成
		CommonKeyInfo cki = CommonKeyInfo.build();
		if (cki == null)
			return false;
		byte[] co = CommonKeyInfo.createConfirmation();
		if (co == null)
			return false;
		byte[] serialized = CommonKeyInfo.serialize(cki, co);
		if (serialized == null)
			return false;
		byte[] encrypted = Glb.getUtil().encryptByPublicKey(u.getPcPublicKey(),
				serialized);
		if (encrypted == null)
			return false;

		//送信する共通鍵情報を登録
		if (!l.sendReport(identifier, cki))
			return false;

		//リクエスト作成
		CommonKeyExchangeUser req = new CommonKeyExchangeUser(encrypted,
				KeyType.PC, KeyType.PC, sender);
		Message reqM = Message.build(req).packaging(req.createPackage())
				.finish();

		//送信と返信の受信
		Message resM = Glb.getP2p().requestSync(reqM, addr);
		if (Response.fail(resM) || !(resM
				.getContent() instanceof CommonKeyExchangeUserResponse))
			return false;

		//resM.getContent()に関する処理はres#receivedで行われている

		UserEdge e = l.getFromUnsecure(identifier);

		//確認処理	リクエスト作成
		CommonKeyExchangeConfirmationUser reqCo = new CommonKeyExchangeConfirmationUser(
				e.getConfirmationFromOther(), Glb.getSubje().getMyAddrInfo(),
				Glb.getSubje().getMe().getNodeNumber());
		Message reqCoM = Message.build(reqCo)
				.packaging(reqCo.createPackage(identifier)).finish();

		//送信と返信の受信
		Message resCoM = Glb.getP2p().requestSync(reqCoM, addr);
		if (resCoM == null || !(resCoM
				.getContent() instanceof CommonKeyExchangeConfirmationUserResponse))
			return false;
		return true;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		Content tmp = m.getContent();
		if (tmp == null || !(tmp instanceof CommonKeyExchangeUser))
			return false;
		CommonKeyExchangeUser c = (CommonKeyExchangeUser) tmp;
		Long signerUserId = m.getUserId();
		if (signerUserId == null
				|| !signerUserId.equals(c.getSender().getUserId()))
			return false;
		return keyType != null && encrypted != null && encrypted.length > 0
				&& resKeyType != null;
	}

	public NodeIdentifierUser getSender() {
		return sender;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof CommonKeyExchangeUserResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		//受信した情報
		Long signerUserId = validated.getMessage().getUserId();
		User signer = Glb.getObje().getUser(us -> us.get(signerUserId));
		byte[] serialized = Glb.getConf().decryptByPrivateKey(keyType,
				encrypted);
		CommonKeyInfo cki = CommonKeyInfo.deserialize(serialized);

		//相手からの共通鍵を仮リストに登録
		if (!Glb.getMiddle().getUserEdgeList().receive(sender, cki))
			return false;

		//返信情報作成
		CommonKeyInfo resCki = CommonKeyInfo.build();
		byte[] co = new byte[Glb.getConst().getCommonKeySizeForCommunication()];
		Glb.getRnd().nextBytes(co);
		byte[] resSerialized = CommonKeyInfo.serialize(resCki, co);
		byte[] resEncrypted = Glb.getUtil().encryptByPublicKey(
				signer.getPubKey(resKeyType), resSerialized);
		CommonKeyExchangeUserResponse res = new CommonKeyExchangeUserResponse(
				resKeyType, resEncrypted,
				Glb.getMiddle().getMyNodeIdentifierUser());
		Message m = Message.build(res).packaging(res.createPackage()).finish();

		//自分からの共通鍵を仮リストに登録
		if (!Glb.getMiddle().getUserEdgeList().sendReport(sender, resCki))
			return false;

		//返信
		return Glb.getP2p().response(m, ctx);
	}

	public static class CommonKeyExchangeUserResponse extends Response
			implements SignedPackageContent {
		private KeyType keyType;
		private byte[] encrypted;
		private NodeIdentifierUser responder;

		public CommonKeyExchangeUserResponse() {
		}

		public CommonKeyExchangeUserResponse(KeyType keyType, byte[] encrypted,
				NodeIdentifierUser responder) {
			this.keyType = keyType;
			this.encrypted = encrypted;
			this.responder = responder;
		}

		public NodeIdentifierUser getResponder() {
			return responder;
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			Content tmp = m.getContent();
			if (tmp == null || !(tmp instanceof CommonKeyExchangeUserResponse))
				return false;
			CommonKeyExchangeUserResponse c = (CommonKeyExchangeUserResponse) tmp;
			Long signerUserId = m.getUserId();
			if (signerUserId == null
					|| !signerUserId.equals(c.getResponder().getUserId()))
				return false;

			return keyType != null && encrypted != null && encrypted.length > 0;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof CommonKeyExchangeUser;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			//受信した情報
			byte[] decrypted = Glb.getConf().decryptByPrivateKey(keyType,
					encrypted);
			CommonKeyInfo cki = CommonKeyInfo.deserialize(decrypted);

			//仮リストに登録
			return Glb.getMiddle().getUserEdgeList().receive(responder, cki);
		}

	}

}
