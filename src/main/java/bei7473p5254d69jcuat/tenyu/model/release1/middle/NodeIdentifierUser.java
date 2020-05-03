package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import java.net.*;
import java.nio.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * ユーザーベースのノード識別子
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class NodeIdentifierUser implements NodeIdentifier, StorableI {
	private Long userId;
	private int nodeNumber;
	private transient byte[] identifier = null;
	private transient User user;

	/**
	 * Userはtransientフィールドにキャッシュされる。
	 * 外部でUserを既に持っていた場合、Identifierにセットして
	 * 色々なモジュール間ではNodeIdentiferUserでやり取りする。
	 *
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "NodeIdentifierUser [userId=" + userId + ", nodeNumber="
				+ nodeNumber + "]";
	}

	@SuppressWarnings("unused")
	private NodeIdentifierUser() {
	}

	/**
	 * ノード番号を指定しないタイプ。DBで検索のときに使うだけ。
	 * 特定のユーザーの全ノード番号について検索するため。
	 *
	 * @param userId
	 */
	public NodeIdentifierUser(Long userId) {
		identifier = getIdentifierPrefix(userId);
	}

	/**
	 * 基本的にこのコンストラクタを使う
	 * @param userId
	 * @param nodeNumber	基本的に0
	 */
	public NodeIdentifierUser(Long userId, int nodeNumber) {
		super();
		this.userId = userId;
		this.nodeNumber = nodeNumber;
	}

	public NodeIdentifierUser(User u, int nodeNumber) {
		super();
		this.user = u;
		this.userId = u.getId();
		this.nodeNumber = nodeNumber;
	}

	public NodeIdentifierUser(byte[] identifier) {
		this.identifier = identifier;
	}

	@Override
	public byte[] getIdentifier() {
		if (identifier == null) {
			identifier = getIdentifier(userId, nodeNumber);
		}

		return identifier;
	}

	public User getUser() {
		if (user == null) {
			user = Glb.getObje().getUser(us->us.get(userId));
		}
		return user;
	}

	public Long getUserId() {
		return userId;
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	public static Long getUserId(byte[] identifier) {
		return ByteBuffer.wrap(identifier).getLong();
	}

	public static int getNodeNumber(byte[] identifier) {
		return ByteBuffer.wrap(identifier).getInt(Long.BYTES);
	}

	public static byte[] getIdentifier(Long userId, int nodeNumber) {
		if (userId == null || nodeNumber < 0)
			return null;
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES + Integer.BYTES);
		buf.putLong(userId);
		buf.putInt(nodeNumber);
		return buf.array();
	}

	public static byte[] getIdentifierPrefix(Long userId) {
		if (userId == null)
			return null;
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
		buf.putLong(userId);
		return buf.array();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeNumber;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeIdentifierUser other = (NodeIdentifierUser) obj;
		if (nodeNumber != other.nodeNumber)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	public boolean validate() {
		return validate(new ValidationResult());
	}

	public boolean validate(ValidationResult vr) {
		boolean b = true;
		if (!Model.validateIdStandard(userId)) {
			vr.add(Lang.USER_ID, Lang.ERROR_INVALID, "userId=" + userId);
			b = false;
		}
		if (nodeNumber < 0) {
			vr.add(Lang.NODE_NUMBER, Lang.ERROR_INVALID,
					"nodeNumber=" + nodeNumber);
			b = false;
		}
		return b;
	}

	public InetSocketAddress getAddrP2PPort() {
		AddrInfo addr = getAddrWithCommunication();
		if (addr == null)
			return null;
		return addr.getISAP2PPort();
	}

	@Override
	public AddrInfo getAddr() {
		UserEdge ue = Glb.getMiddle().getUserEdgeList().getEdge(this);
		if (ue == null)
			return null;
		return ue.getAddr();
	}

	@Override
	public AddrInfo getAddrWithCommunication() {
		User u = getUser();
		if (u != null) {
			return u.tryToGetAddr(nodeNumber);
		}
		return null;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validate(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		User u = us.get(userId);
		if (u == null) {
			r.add(Lang.NODE_IDENTIFIER_USERID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"userId=" + userId);
			b = false;
		}
		return b;
	}
}