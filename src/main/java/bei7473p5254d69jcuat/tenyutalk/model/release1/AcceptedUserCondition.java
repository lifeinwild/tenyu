package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 何かを限定公開する際に使用する。アクセス可能な（{@link User}の条件。
 *
 * {@link ModelConditionI}は他人が修正するので、
 * セキュリティ目的での個人的なアクセス制限には使用できない。
 * さらに、{@link ModelConditionI}のようなあらゆる条件を記述できるように設計された
 * クラスは、設定が難しく、個人的なアクセス制限に適さない。
 * そこでこのクラスが必要で、しかもある程度単純な設定内容になっている。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AcceptedUserCondition implements ValidatableI {
	public static final int acceptedMax = 1000 * 10;
	public static final int deniedMax = 1000 * 10;

	/**
	 * 閲覧可能ユーザー一覧
	 */
	private HashSet<UserGroup> accepted = new LinkedHashSet<>();

	/**
	 * 閲覧禁止ユーザー一覧
	 *
	 * 閲覧可能ユーザーに含められていてもここに含められていたら閲覧禁止
	 * publication設定でも拒否できる
	 */
	private HashSet<UserGroup> denied = new LinkedHashSet<>();

	/**
	 * 全員に公開されるか
	 * これがtrueだと他の条件は無視される
	 */
	private boolean publication = false;

	/**
	 * @param userId
	 * @return	アクセス可能なユーザーか
	 */
	public boolean contains(Long userId) {
		if (publication)
			return true;

		User u = Glb.getObje().getUser(us -> us.get(userId));
		if (u == null)
			return false;

		Sociality so = Glb.getObje().getSociality(sos -> sos
				.getByIndividualityObject(StoreNameObjectivity.USER, userId));
		if (so == null || so.isBanned())
			return false;

		//拒否
		for (UserGroup g : denied) {
			if (g.contains(userId))
				return false;
		}

		//許可
		for (UserGroup g : accepted) {
			if (g.contains(userId))
				return true;
		}

		return false;
	}

	/**
	 * 許可グループにユーザーグループを追加する
	 * @param g	追加されるユーザーグループ
	 * @return	追加に成功したか
	 */
	public boolean accept(UserGroup g) {
		if (g == null)
			return false;
		if (accepted.size() > acceptedMax)
			return false;
		return accepted.add(g);
	}

	public boolean accept(User u) {
		if (u == null)
			return false;
		UserGroup g = new UserGroup();
		g.add(u.getId());
		return accept(u);
	}

	/**
	 * 拒否グループにユーザーグループを追加する
	 * @param g	追加されるユーザーグループ
	 * @return	追加に成功したか
	 */
	public boolean deny(UserGroup g) {
		if (g == null)
			return false;
		if (denied.size() > deniedMax)
			return false;
		return denied.add(g);
	}

	public boolean deny(User u) {
		if (u == null)
			return false;
		UserGroup g = new UserGroup();
		g.add(u.getId());
		return deny(g);
	}

	public boolean removeFromAccepted(UserGroup g) {
		return accepted.remove(g);
	}

	public boolean removeFromDenied(UserGroup g) {
		return denied.remove(g);
	}

	public Set<UserGroup> getAccepted() {
		return Collections.unmodifiableSet(accepted);
	}

	public Set<UserGroup> getDenied() {
		return Collections.unmodifiableSet(denied);
	}

	public boolean isPublication() {
		return publication;
	}

	public void setPublication(boolean publication) {
		this.publication = publication;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (accepted == null) {
			r.add(Lang.ACCESSIBLE_USER_CONDITION_ACCEPTED_USERIDS,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (accepted.size() > acceptedMax) {
				r.add(Lang.ACCESSIBLE_USER_CONDITION_ACCEPTED_USERIDS,
						Lang.ERROR_TOO_MANY, "size=" + accepted.size());
				b = false;
			}
		}

		if (denied == null) {
			r.add(Lang.ACCESSIBLE_USER_CONDITION_DENIED_USERIDS,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (denied.size() > deniedMax) {
				r.add(Lang.ACCESSIBLE_USER_CONDITION_DENIED_USERIDS,
						Lang.ERROR_TOO_MANY, "size=" + denied.size());
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (UserGroup ug : accepted) {
				if (!ug.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
			for (UserGroup ug : denied) {
				if (!ug.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (UserGroup ug : accepted) {
				if (!ug.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}

			for (UserGroup ug : denied) {
				if (!ug.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		for (UserGroup g : accepted) {
			if (!g.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		for (UserGroup g : denied) {
			if (!g.validateReference(r, txn)) {
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
		result = prime * result
				+ ((accepted == null) ? 0 : accepted.hashCode());
		result = prime * result + ((denied == null) ? 0 : denied.hashCode());
		result = prime * result + (publication ? 1231 : 1237);
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
		AcceptedUserCondition other = (AcceptedUserCondition) obj;
		if (accepted == null) {
			if (other.accepted != null)
				return false;
		} else if (!accepted.equals(other.accepted))
			return false;
		if (denied == null) {
			if (other.denied != null)
				return false;
		} else if (!denied.equals(other.denied))
			return false;
		if (publication != other.publication)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccessibleUserCondition [accepted=" + accepted + ", denied="
				+ denied + ", publication=" + publication + "]";
	}

}
