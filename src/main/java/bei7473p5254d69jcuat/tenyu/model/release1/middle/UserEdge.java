package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 各ユーザーと自分の間に作られた情報。例えば共通鍵
 * 非客観
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserEdge implements StorableI {
	/**
	 * 相手ノードを特定する識別子
	 */
	private NodeIdentifierUser identifier;

	/**
	 * ノード番号。同じユーザーIDで多数のノードを起動するため。
	 */
	private int nodeNumber = -1;

	/**
	 * 最後にこの相手と通信した日時
	 * TODO 適切に更新されていない
	 */
	private long accessDate;

	/**
	 * 自分が送った確認情報
	 */
	private transient byte[] confirmationFromMe;

	/**
	 * 相手が送った確認情報1
	 */
	private transient byte[] confirmationFromOther;

	/**
	 * 作成日時
	 */
	private long createDate;

	/**
	 * 相手から受信する場合の共通鍵
	 */
	private CommonKeyExchangeState receiveKey;

	/**
	 * 自分から送信する場合の共通鍵
	 */
	private CommonKeyExchangeState sendKey;

	/**
	 * このユーザーのIPアドレス。できるだけ最新にするが最新と限らない。
	 * UserのAddrInfoと情報が被るかもしれないが、更新の方法が異なるので
	 * 一応重複しても記録している。
	 */
	private AddrInfo addr;

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;

		if (identifier == null) {
			r.add(Lang.NODE_IDENTIFIER_USERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!identifier.validate(r)) {
				b = false;
			} else if (!Model
					.validateIdStandardNotSpecialId(identifier.getUserId())) {
				r.add(Lang.USER_ID, Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (nodeNumber < 0) {
			r.add(Lang.NODE_NUMBER, Lang.ERROR_INVALID,
					"nodeNumber=" + nodeNumber);
			b = false;
		}

		if (accessDate < 0) {
			r.add(Lang.USER_EDGE_ACCESSDATE, Lang.ERROR_INVALID);
			b = false;
		}

		if (createDate < 0) {
			r.add(Lang.USER_EDGE_CREATEDATE, Lang.ERROR_INVALID);
			b = false;
		}

		if (receiveKey == null) {
			r.add(Lang.USER_EDGE_RECEIVE_KEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!receiveKey.validateAtCreate(r)) {
				b = false;
			}
		}

		if (sendKey == null) {
			r.add(Lang.USER_EDGE_SEND_KEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!sendKey.validateAtCreate(r)) {
				b = false;
			}
		}

		if (addr == null) {
			r.add(Lang.USERADDR, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!addr.validateAtCreate(r)) {
				b = false;
			}
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
		if (identifier != null) {
			Long userId = identifier.getUserId();
			//この参照検証はtxnに依存させてはいけない。DBが違うから。
			if (Glb.getObje().getUser(us -> us.get(userId)) == null) {
				r.add(Lang.USER_ID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"userId=" + userId);
				b = false;
			}
		}
		return b;
	}

	public UserEdge() {
		createDate = System.currentTimeMillis();
	}

	public boolean isCommunicatedIn10Minutes() {
		long elapsed = System.currentTimeMillis() - accessDate;
		return elapsed < 1000 * 60 * 10;
	}

	public long getAccessDate() {
		return accessDate;
	}

	public byte[] getConfirmationFromMe() {
		return confirmationFromMe;
	}

	public byte[] getConfirmationFromOther() {
		return confirmationFromOther;
	}

	public CommonKeyExchangeState getReceiveKey() {
		return receiveKey;
	}

	public CommonKeyExchangeState getSendKey() {
		return sendKey;
	}

	public boolean isStartedIn1Minute() {
		long elapsed = System.currentTimeMillis() - createDate;
		return elapsed < 1000L * 60;
	}

	public void setConfirmationFromMe(byte[] confirmationFromMe) {
		this.confirmationFromMe = confirmationFromMe;
	}

	public void setConfirmationFromOther(byte[] confirmationFromOther) {
		this.confirmationFromOther = confirmationFromOther;
	}

	public void setReceiveKey(CommonKeyExchangeState receiveKey) {
		ValidationResult vr = new ValidationResult();
		if (receiveKey == null || !receiveKey.validateAtCreate(vr)) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return;
		}
		this.receiveKey = receiveKey;
	}

	public void setSendKey(CommonKeyExchangeState sendKey) {
		ValidationResult vr = new ValidationResult();
		if (sendKey == null || !sendKey.validateAtCreate(vr)) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return;
		}
		this.sendKey = sendKey;
	}

	public void updateAccessDate() {
		this.accessDate = System.currentTimeMillis();
	}

	public AddrInfo getAddr() {
		return addr;
	}

	public void setAddr(AddrInfo addr) {
		ValidationResult vr = new ValidationResult();
		if (addr == null || !addr.validateAtCommon(vr)) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return;
		}
		this.addr = addr;
	}

	/*
	public Long getUserId() {
		return identifier.getUserId();
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	*/

	public NodeIdentifierUser getIdentifier() {
		return identifier;
	}

	public void setIdentifier(NodeIdentifierUser identifier) {
		this.identifier = identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (accessDate ^ (accessDate >>> 32));
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + nodeNumber;
		result = prime * result
				+ ((receiveKey == null) ? 0 : receiveKey.hashCode());
		result = prime * result + ((sendKey == null) ? 0 : sendKey.hashCode());
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
		UserEdge other = (UserEdge) obj;
		if (accessDate != other.accessDate)
			return false;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		if (createDate != other.createDate)
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (nodeNumber != other.nodeNumber)
			return false;
		if (receiveKey == null) {
			if (other.receiveKey != null)
				return false;
		} else if (!receiveKey.equals(other.receiveKey))
			return false;
		if (sendKey == null) {
			if (other.sendKey != null)
				return false;
		} else if (!sendKey.equals(other.sendKey))
			return false;
		return true;
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

}