package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 1つのチャットメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ChatMessage implements Storable {
	/**
	 * 送信者ユーザーID
	 */
	private Long userId;

	/**
	 * チャットメッセージの内容
	 * MD記法可能
	 */
	private String message;

	/**
	 * 送信者作成日時
	 */
	private long createDate;

	/**
	 * 受信者受信日時
	 */
	private long receiveDate;

	public long getCreateDate() {
		return createDate;
	}

	public String getMessage() {
		return message;
	}

	public long getReceiveDate() {
		return receiveDate;
	}

	public Long getUserId() {
		return userId;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setReceiveDate(long receiveDate) {
		this.receiveDate = receiveDate;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (receiveDate ^ (receiveDate >>> 32));
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
		ChatMessage other = (ChatMessage) obj;
		if (createDate != other.createDate)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (receiveDate != other.receiveDate)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChatMessage [userId=" + userId + ", message=" + message
				+ ", createDate=" + createDate + ", receiveDate=" + receiveDate
				+ "]";
	}

	public static final int messageMax = 1000;

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (userId == null) {
			r.add(Lang.CHATMESSAGE, Lang.USER_ID, Lang.ERROR_EMPTY);
			b = false;
		}
		if (message == null) {
			r.add(Lang.CHATMESSAGE, Lang.MESSAGE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (message.length() > messageMax) {
				r.add(Lang.CHATMESSAGE, Lang.MESSAGE, Lang.ERROR_TOO_LONG,
						"message.length()=" + message.length());
				b = false;
			}
		}
		if (createDate < 0) {
			r.add(Lang.CHATMESSAGE, Lang.CREATE_DATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		if (receiveDate < 0) {
			r.add(Lang.CHATMESSAGE, Lang.RECEIVE_DATE, Lang.ERROR_INVALID,
					"receiveDate=" + receiveDate);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (Glb.getObje().getUser(us -> us.get(userId)) == null) {
			r.add(Lang.USER_ID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"userId=" + userId);
			b = false;
		}
		return b;
	}
}
