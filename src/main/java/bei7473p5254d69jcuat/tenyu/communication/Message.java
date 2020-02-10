package bei7473p5254d69jcuat.tenyu.communication;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.Package;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

/**
 * これが送受信される。
 *
 * 検証の全容
 * 送信側でMessageオブジェクトを構築したらvalidate
 * 受信側でデシリアライズされたMessageオブジェクトでvalidateAndSetup
 * いずれかを行えば内部の各オブジェクトについて検証処理が呼び出される。
 *
 * 検証メソッドvalidateはMessageオブジェクトを受け取る。
 * Messageの他の情報との組み合わせを検証する事が可能。
 *
 * {@link Package#unpackage(Message)}は、開梱処理をしながら検証処理をする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Message {
	/**
	 * Message全体をシリアライズした時のサイズ
	 * 受信時に設定される
	 */
	private transient long size = -1;
	/**
	 * 多重梱包の一番外側の梱包または一重梱包のその唯一の梱包
	 * 実際に通信される情報
	 */
	private Package load;

	/**
	 * 自分が作成したメッセージか
	 * falseなら受信したメッセージ
	 */
	private transient boolean myMessage;
	/**
	 * 一番内側の梱包に入っている内容
	 */
	private transient MessageContent content;
	/**
	 * 梱包一覧。メッセージが完成している場合、
	 * 必ず最後の要素はoutermostと同一
	 */
	private transient LinkedList<Package> packs;

	/**
	 * Kryo用
	 */
	public Message() {
	}

	public Message(MessageContent c) {
		myMessage = true;
		content = c;
		packs = new LinkedList<>();
	}

	public void setContent(MessageContent content) {
		this.content = content;
	}

	private void init() {
		content = null;
		packs = new LinkedList<>();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;//同一性比較が必須なので記述しておく
	}

	/**
	 * 送信時に呼ぶ想定
	 * transientメンバーはコンストラクタを通じて設定されている前提
	 *
	 * @return	検証に成功したか
	 */
	public final boolean validate() {
		if (content == null || packs == null || packs.size() == 0)
			return false;
		for (Package p : packs) {
			if (!p.validate(this))
				return false;
		}
		if (!content.validate(this))
			return false;
		return true;
	}

	/**
	 * 受信時に呼ぶ想定
	 * 検証しつつ開梱処理をする
	 * transientメンバーがセットアップされる
	 *
	 * @return	検証に成功したか
	 */
	public final boolean validateAndSetup() {
		init();
		//キャッシュがあるので無駄なデシリアライズ等は発生しない
		boolean r = load != null && load.unpackage(this) != null;
		//contentやpacksがセットアップされたか
		return r && content != null && packs != null && packs.size() > 0;
	}

	public Package getOutermostPack() {
		if (packs == null || packs.size() == 0)
			return null;
		return packs.getLast();
	}

	public Package getLoad() {
		return load;
	}

	/**
	 * @return	多重梱包の一番内側の梱包。一重ならその唯一の梱包。梱包一覧が無ければnull
	 */
	public Package getInnermostPack() {
		if (packs == null || packs.size() == 0)
			return null;
		return packs.getFirst();
	}

	public Package getPack(int index) {
		return packs.get(index);
	}

	public MessageContent getContent() {
		return content;
	}

	public boolean addOuter(Package p) {
		return packs.add(p);
	}

	public boolean addInner(Package p) {
		packs.addFirst(p);
		return true;
	}

	public LinkedList<Package> getPacks() {
		return packs;
	}

	public boolean isMyMessage() {
		return myMessage;
	}

	public void setMyMessage(boolean myMessage) {
		this.myMessage = myMessage;
	}

	/**
	 * 最も内側の梱包からユーザーIDの取得を試みる
	 * @return メッセージ作成者のユーザーID。メッセージによってnull
	 */
	public Long getUserId() {
		Package p = getInnermostPack();
		if (p instanceof SignedPackage) {
			SignedPackage signed = (SignedPackage) p;
			return signed.getSignerUserId();
		} else if (p instanceof UserCommonKeyPackage) {
			UserCommonKeyPackage ucp = (UserCommonKeyPackage) p;
			return ucp.getSenderUserId();
		}
		return null;
	}

	/**
	 * 最も内側の梱包からNodeIdentifierUserの取得を試みる
	 * @return
	 */
	public NodeIdentifierUser getIdentifierUser() {
		Package p = getInnermostPack();
		if (p instanceof UserCommonKeyPackage) {
			UserCommonKeyPackage ucp = (UserCommonKeyPackage) p;
			return ucp.getSender();
		}
		return null;
	}

	/**
	 * 送信に使われたP2PEdgeの特定を試みる
	 * メッセージによってP2PEdgeが使われていない場合があるのでnullの場合もある。
	 * 最も内側の梱包に依存する
	 *
	 * @return
	 */
	public P2PEdge getEdgeByInnermostPackage() {
		Package innermost = getInnermostPack();
		if (innermost instanceof P2PEdgeCommonKeyPackage) {
			P2PEdgeCommonKeyPackage p = (P2PEdgeCommonKeyPackage) innermost;
			return p.getEdge(this);
		} else if (innermost instanceof P2PEdgeCommonKeyPackageUnsecure) {
			P2PEdgeCommonKeyPackageUnsecure p = (P2PEdgeCommonKeyPackageUnsecure) innermost;
			return p.getEdge(this);
		}
		return null;
	}

	/**
	 * builderパターン
	 * @param c		1Messageにつき1つだけあるメッセージの内容
	 */
	public static Message build(MessageContent c) {
		Message m = new Message(c);
		return m;
	}

	/**
	 * 最初の梱包
	 * 最初の梱包はgetEdgeやgetUserIdに影響し、後の多重梱包と少し意味が異なる
	 */
	public Message packaging(Package p) {
		if (!p.binarizeAndSetContent(content, this))
			return null;
		addOuter(p);
		return this;
	}

	/**
	 * 多重梱包
	 */
	public Message multiplePackaging(Package p) {
		if (!p.binarizeAndSetContent(getOutermostPack(), this))
			return null;
		addOuter(p);
		return this;
	}

	/**
	 * 必ず最後に呼び出す
	 */
	public Message finish() {
		load = getOutermostPack();
		if (load == null)
			return null;
		return this;
	}

	public long getSize() {
		if (size == -1) {
			size = Glb.getUtil().sizeOf(load);
		}
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}