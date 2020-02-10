package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 公開範囲等に使用するユーザーの範囲
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserScope implements Storable {
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
	 */
	private boolean publication = false;

	/**
	 * @param userId
	 * @return	このスコープの範囲内のユーザーか
	 */
	public boolean contains(Long userId) {
		if (publication)
			return true;
		for (UserGroup g : accepted) {
			if (g.contains(userId))
				return true;
		}
		return false;
	}

	public boolean accept(UserGroup g) {
		if (accepted.size() > acceptedMax)
			return false;
		return accepted.add(g);
	}

	public boolean deny(UserGroup g) {
		if (denied.size() > deniedMax)
			return false;
		return denied.add(g);
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
			r.add(Lang.USER_SCOPE_ACCEPTED, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (accepted.size() > acceptedMax) {
				r.add(Lang.USER_SCOPE_ACCEPTED, Lang.ERROR_TOO_MANY);
				b = false;
			}
		}
		if (denied == null) {
			r.add(Lang.USER_SCOPE_DENIED, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (denied.size() > deniedMax) {
				r.add(Lang.USER_SCOPE_DENIED, Lang.ERROR_TOO_MANY);
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
		UserScope other = (UserScope) obj;
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
		return "UserScope [accepted=" + accepted + ", denied=" + denied
				+ ", publication=" + publication + "]";
	}

}
