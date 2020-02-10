package bei7473p5254d69jcuat.tenyu.communication.packaging;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;

/**
 * 特定のユーザーに共通鍵暗号化をした上で情報を送信する。
 * その共通鍵暗号化はP2PEdgeではなくUserEdgeの共通鍵で行われる。
 *
 * TODO ノードID対応？UserIDだけでは通信相手を決定できない。
 * 同じユーザーIDで複数のノードを使っている場合がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserCommonKeyPackage extends CommonKeyPackage {
	@Override
	protected boolean isValidType(Object content) {
		return content instanceof UserCommonKeyPackageContent;
	}

	public static interface UserCommonKeyPackageContent {
		default boolean setupState(NodeIdentifierUser receiver) {
			try {
				CommonKeyExchangeState uCki = Glb.getMiddle().getUserEdgeList()
						.getSendKey(Glb.getMiddle().getMyNodeIdentifierUser());
				if (uCki == null || uCki.isUpdatable()) {
					return CommonKeyExchangeUser.send(receiver);
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
			return false;
		}

		default Package createPackage(NodeIdentifierUser receiver) {
			//UserEdgeが無い場合、共通鍵交換シーケンスを開始する
			//TODO このコードをここに書くのは直感的ではない
			//オブジェクトを取得するだけのつもりが通信が発生する。
			//じゃあどこに書くべきか？思いつかない。P2Pクラスか？
			//そこなら通信が発生する事を当然想定して呼び出す。
			if (!setupState(receiver)) {
				Glb.getLogger().error("Failed to setup UserEdge",
						new Exception());
				return null;
			}
			return new UserCommonKeyPackage(
					Glb.getMiddle().getMyNodeIdentifierUser(), receiver);
		}
	}

	@SuppressWarnings("unused")
	private UserCommonKeyPackage() {
	}

	/**
	 * @param sender		送信者
	 * @param receiver	受信者
	 */
	public UserCommonKeyPackage(NodeIdentifierUser sender,
			NodeIdentifierUser receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}

	/**
	 * 送信者
	 */
	private NodeIdentifierUser sender;
	/**
	 * 受信者
	 */
	private transient NodeIdentifierUser receiver;

	@Override
	protected CommonKeyInfo getCki(Message m) {
		CommonKeyExchangeState r = null;
		if (m.isMyMessage()) {
			r = Glb.getMiddle().getUserEdgeList().getSendKey(receiver);
		} else {
			r = Glb.getMiddle().getUserEdgeList().getReceiveKey(sender);
		}
		if (r == null)
			return null;
		return r.getCommonKeyInfo();
	}

	public NodeIdentifierUser getSender() {
		return sender;
	}

	public Long getSenderUserId() {
		if(sender == null)
			return null;
		return sender.getUserId();
	}

	public Long getReceiverUserId() {
		if(receiver == null)
			return null;
		return receiver.getUserId();
	}

	public void setSender(NodeIdentifierUser sender) {
		this.sender = sender;
	}

	public NodeIdentifierUser getReceiver() {
		return receiver;
	}

	public void setReceiver(NodeIdentifierUser receiver) {
		this.receiver = receiver;
	}

}
