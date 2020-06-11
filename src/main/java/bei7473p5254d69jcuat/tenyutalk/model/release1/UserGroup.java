package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * ユーザーのグループ
 * 非スレッドセーフ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserGroup implements ValidatableI {
	/**
	 * 重複無しユーザーID一覧
	 */
	private HashSet<Long> userIds = new LinkedHashSet<>();

	public static final int userIdsMax = 1000 * 10;

	public boolean add(Long userId) {
		return userIds.add(userId);
	}

	public boolean contains(Long userId) {
		return userIds.contains(userId);
	}

	public Set<Long> getUserIds() {
		return Collections.unmodifiableSet(userIds);
	}

	public boolean remove(Long userId) {
		return userIds.remove(userId);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (userIds == null) {
			r.add(Lang.USER_GROUP_USERIDS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (userIds.size() > userIdsMax) {
				r.add(Lang.USER_GROUP_USERIDS, Lang.ERROR_TOO_MANY,
						"userIds.size()=" + userIds.size());
				b = false;
			} else {
				if (!Model.validateIdStandard(userIds)) {
					r.add(Lang.USER_GROUP_USERIDS, Lang.ERROR_INVALID,
							"userIds=" + userIds);
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		for (Long userId : userIds) {
			UserStore us = new UserStore(txn);
			if (us.get(userId) == null) {
				r.add(Lang.USER_GROUP_USERIDS, Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"userId=" + userId);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userIds == null) ? 0 : userIds.hashCode());
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
		UserGroup other = (UserGroup) obj;
		if (userIds == null) {
			if (other.userIds != null)
				return false;
		} else if (!userIds.equals(other.userIds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserGroup [userIds=" + userIds + "]";
	}

}
