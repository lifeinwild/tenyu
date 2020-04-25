package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.other.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import bei7473p5254d69jcuat.tenyutalk.ui.other.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class Comment extends AdministratedObject implements CommentI {
	private List<Long> goodUserIds = new ArrayList<>();
	private List<Long> badUserIds = new ArrayList<>();
	private String content;
	private Long parentCommentId;
	private Long creativeObjectId;

	public Long getParentCommentId() {
		return parentCommentId;
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameTenyutalk.COMMENT;
	}

	public boolean addGoodUserId(Long userId) {
		if (goodUserIds.contains(userId))
			return false;
		if (badUserIds.contains(userId)) {
			removeBadUserId(userId);
		}
		return goodUserIds.add(userId);
	}

	public boolean removeGoodUserId(Long userId) {
		return goodUserIds.remove(userId);
	}

	public boolean addBadUserId(Long userId) {
		if (badUserIds.contains(userId))
			return false;
		if (goodUserIds.contains(userId)) {
			removeGoodUserId(userId);
		}
		return badUserIds.add(userId);
	}

	public boolean removeBadUserId(Long userId) {
		return badUserIds.remove(userId);
	}

	private List<Long> getAdministrators() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return getAdministrators();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministrators();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministrators();
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (content == null || content.length() == 0) {
			r.add(Lang.COMMENT, Lang.CONTENT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (content.length() > contentMax) {
				r.add(Lang.COMMENT, Lang.CONTENT, Lang.ERROR_TOO_LONG,
						"length=" + content.length());
				b = false;
			}
		}
		if (goodUserIds == null) {
			r.add(Lang.COMMENT, Lang.GOOD_USER_IDS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (goodUserIds.size() > goodMax) {
				r.add(Lang.COMMENT, Lang.GOOD_USER_IDS, Lang.ERROR_TOO_LONG,
						"size=" + goodUserIds.size());
				b = false;
			}
		}
		if (badUserIds == null) {
			r.add(Lang.COMMENT, Lang.BAD_USER_IDS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (badUserIds.size() > badMax) {
				r.add(Lang.COMMENT, Lang.BAD_USER_IDS, Lang.ERROR_TOO_LONG,
						"size=" + badUserIds.size());
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		boolean good = Glb.getObje().getUser(us -> {
			boolean valid = true;
			for (Long userId : goodUserIds) {
				User u = us.get(userId);
				if (u == null) {
					r.add(Lang.COMMENT, Lang.GOOD_USER_IDS,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"userId=" + userId);
					valid = false;
				}
			}
			return valid;
		});
		if (!good) {
			b = false;
		}

		boolean bad = Glb.getObje().getUser(us -> {
			boolean valid = true;
			for (Long userId : badUserIds) {
				User u = us.get(userId);
				if (u == null) {
					r.add(Lang.COMMENT, Lang.BAD_USER_IDS,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"userId=" + userId);
					valid = false;
				}
			}
			return valid;
		});
		if (!bad) {
			b = false;
		}

		//以下のループ処理は祖先に自分を参照しているコメントが無い事等を確認する
		Long myId = getId();
		Long nextParentId = parentCommentId;
		boolean proc = true;
		int parentCount = 0;
		while (proc && parentCount < parentMax) {
			if (nextParentId != null) {
				//IDに対応するオブジェクトが存在するか
				Comment parent = Glb.getTenyutalk()
						.getComment(cs -> cs.get(parentCommentId));
				if (parent == null) {
					//IDが設定されていてオブジェクトが見つからないならエラー
					r.add(Lang.COMMENT, Lang.PARENT_COMMENT_ID,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"parentCommentId=" + parentCommentId);
					b = false;
					proc = false;
				} else {
					//myIdは作成時にはnull
					if (myId != null) {
						//更新時
						//オブジェクトがあってもその親IDが自分だったらエラー
						if (myId.equals(parent.getParentCommentId())) {
							r.add(Lang.COMMENT, Lang.PARENT_COMMENT_ID,
									Lang.ERROR_INVALID,
									"myId=" + myId + " parentCommentId="
											+ parentCommentId + " parentId="
											+ parent.getId());
							b = false;
							proc = false;
						} else {
							nextParentId = parent.getParentCommentId();
							parentCount++;
						}
					}
				}
			} else {
				proc = false;
			}
		}

		return b;
	}

	@Override
	public AdministratedObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new CommentStore(txn);
	}

	@Override
	public AdministratedObjectGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix) {
		return new CommentGui(guiName, cssIdPrefix);
	}

	@Override
	public CommentI getParent() {
		return Glb.getTenyutalk().getComment(cs -> cs.get(parentCommentId));
	}

	@Override
	public String getContent() {
		return content;
	}

	public static int getContentmax() {
		return contentMax;
	}

	public List<Long> getGoodUserIds() {
		return Collections.unmodifiableList(goodUserIds);
	}

	public List<Long> getBadUserIds() {
		return Collections.unmodifiableList(badUserIds);
	}

	public void setGoodUserIds(List<Long> goodUserIds) {
		this.goodUserIds = goodUserIds;
	}

	public void setBadUserIds(List<Long> badUserIds) {
		this.badUserIds = badUserIds;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setParentCommentId(Long parentCommentId) {
		this.parentCommentId = parentCommentId;
	}

	public Long getCreativeObjectId() {
		return creativeObjectId;
	}

	public void setCreativeObjectId(Long creativeObjectId) {
		this.creativeObjectId = creativeObjectId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((badUserIds == null) ? 0 : badUserIds.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((creativeObjectId == null) ? 0
				: creativeObjectId.hashCode());
		result = prime * result
				+ ((goodUserIds == null) ? 0 : goodUserIds.hashCode());
		result = prime * result
				+ ((parentCommentId == null) ? 0 : parentCommentId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Comment other = (Comment) obj;
		if (badUserIds == null) {
			if (other.badUserIds != null)
				return false;
		} else if (!badUserIds.equals(other.badUserIds))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (creativeObjectId == null) {
			if (other.creativeObjectId != null)
				return false;
		} else if (!creativeObjectId.equals(other.creativeObjectId))
			return false;
		if (goodUserIds == null) {
			if (other.goodUserIds != null)
				return false;
		} else if (!goodUserIds.equals(other.goodUserIds))
			return false;
		if (parentCommentId == null) {
			if (other.parentCommentId != null)
				return false;
		} else if (!parentCommentId.equals(other.parentCommentId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Comment [goodUserIds=" + goodUserIds + ", badUserIds="
				+ badUserIds + ", content=" + content + ", parentCommentId="
				+ parentCommentId + ", creativeObjectId=" + creativeObjectId
				+ "]";
	}

}
