package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyutalk.ui.other.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * チャットメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ChatMessage implements StorableI {
	public static final int categoryMax = 50;
	public static final String defaultCategory = "default";

	public static final int messageMax = 1000;

	public static int getCategorymax() {
		return categoryMax;
	}

	public static String getDefaultcategory() {
		return defaultCategory;
	}

	public static int getMessagemax() {
		return messageMax;
	}

	/**
	 * 分類
	 * 表示される{@link ChatWindow}が変わる。
	 */
	private String category;

	/**
	 * 発言内容
	 */
	private String content;

	/**
	 * 作成日時
	 */
	private long createDate = -1L;

	/**
	 * 削除（クリア）された日時
	 */
	private long deleteDate = -1L;

	/**
	 * 所属する多人数参加コンテンツにおいて一意なID
	 */
	private Long id;

	/**
	 * 受信日時
	 */
	private long receiveDate = -1L;

	/**
	 * 発言者
	 * 削除権限を持つ
	 */
	private Long userId;

	public boolean delete() {
		deleteDate = Glb.getUtil().getEpochMilli();
		content = "";
		return true;
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
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (createDate != other.createDate)
			return false;
		if (deleteDate != other.deleteDate)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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

	public String getCategory() {
		return category;
	}

	public String getContent() {
		return content;
	}

	public long getCreateDate() {
		return createDate;
	}

	public long getDeleteDate() {
		return deleteDate;
	}

	public ChatMessageGui getGui(String guiName, String cssIdPrefix) {
		return new ChatMessageGui(guiName, cssIdPrefix);
	}

	public Long getId() {
		return id;
	}

	public long getReceiveDate() {
		return receiveDate;
	}

	public Long getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result + (int) (deleteDate ^ (deleteDate >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (receiveDate ^ (receiveDate >>> 32));
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	public boolean isDeleted() {
		return deleteDate >= 0;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setDeleteDate(long deleteDate) {
		this.deleteDate = deleteDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setReceiveDate(long receiveDate) {
		this.receiveDate = receiveDate;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "ChatMessage [id=" + id + ", category=" + category + ", content="
				+ content + ", userId=" + userId + ", createDate=" + createDate
				+ ", receiveDate=" + receiveDate + ", deleteDate=" + deleteDate
				+ "]";
	}

	/**
	 * 発言者が送信直前に検証する
	 * @param r
	 * @return
	 */
	public boolean validateAtCommunication(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon2(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		if (!validateCommon2(r))
			b = false;

		if (deleteDate < 0) {
			r.add(Lang.CHAT_MESSAGE, Lang.DELETE_DATE, Lang.ERROR_INVALID,
					"deleteDate=" + deleteDate);
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof ChatMessage)) {
			r.add(Lang.CHAT_MESSAGE, Lang.ERROR_INVALID, "old=" + old);
			b = false;
		} else {
			ChatMessage o = (ChatMessage) old;
			if (!this.getId().equals(o.getId())) {
				r.add(Lang.CHAT_MESSAGE, Lang.ID, Lang.ERROR_NOT_EQUAL,
						"this.id=" + getId() + " o.id=" + o.getId());
				b = false;
			}
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (id == null) {
			r.add(Lang.CHAT_MESSAGE, Lang.ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (Model.validateIdStandardNotSpecialId(id)) {
				r.add(Lang.CHAT_MESSAGE, Lang.ID, Lang.ERROR_INVALID,
						"id=" + id);
				b = false;
			}
		}
		if (userId == null) {
			r.add(Lang.CHAT_MESSAGE, Lang.USER_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (Model.validateIdStandardNotSpecialId(userId)) {
				r.add(Lang.CHAT_MESSAGE, Lang.USER_ID, Lang.ERROR_INVALID,
						"userId=" + userId);
				b = false;
			}
		}
		if (content == null) {
			r.add(Lang.CHAT_MESSAGE, Lang.CONTENT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (content.length() > messageMax) {
				r.add(Lang.CHAT_MESSAGE, Lang.CONTENT, Lang.ERROR_TOO_LONG,
						"message.length()=" + content.length());
				b = false;
			}
		}
		if (category == null) {
			r.add(Lang.CHAT_MESSAGE, Lang.CATEGORY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (category.length() > categoryMax) {
				r.add(Lang.CHAT_MESSAGE, Lang.CATEGORY, Lang.ERROR_TOO_LONG,
						"message.length()=" + content.length());
				b = false;
			}
		}

		if (createDate < 0) {
			r.add(Lang.CHAT_MESSAGE, Lang.CREATE_DATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		return b;
	}

	/**
	 * 共通の検証処理を２段階に分ける
	 * 一部の文脈でやるべき検証処理が少し違うので
	 *
	 * @param r
	 * @return
	 */
	private boolean validateCommon2(ValidationResult r) {
		boolean b = true;
		b = validateCommon(r);
		if (receiveDate < 0) {
			r.add(Lang.CHAT_MESSAGE, Lang.RECEIVE_DATE, Lang.ERROR_INVALID,
					"receiveDate=" + receiveDate);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (!Glb.getObje().getUser(us -> us.get(userId) != null)) {
			b = false;
		}
		return b;
	}
}
