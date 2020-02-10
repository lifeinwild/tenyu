package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 全体運営者
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuManager implements Storable {
	/**
	 * 全体の意思決定への影響割合
	 * 1.0-0
	 * ただし全全体運営者の合計値は1.0を超える可能性がある
	 */
	private Double power;
	private Long userId;

	public TenyuManager() {
	}

	public TenyuManager(Long userId, Double power) {
		if (userId == null)
			throw new IllegalArgumentException();
		if (power == null || power < 0)
			throw new IllegalArgumentException();
		this.userId = userId;
		this.power = power;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuManager other = (TenyuManager) obj;
		if (power == null) {
			if (other.power != null)
				return false;
		} else if (!power.equals(other.power))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	public Double getPower() {
		return power;
	}

	public Long getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((power == null) ? 0 : power.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	public void setPower(Double power) {
		this.power = power;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "TenyuManager [power=" + power + ", userId=" + userId + "]";
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (power == null) {
			r.add(Lang.TENYU_MANAGER_POWER, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (power < 0) {
				r.add(Lang.TENYU_MANAGER_POWER, Lang.ERROR_TOO_FEW,
						"power=" + power);
				b = false;
			}
		}
		if (userId == null) {
			r.add(Lang.TENYU_MANAGER_USERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(userId)) {
				r.add(Lang.TENYU_MANAGER_USERID, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(userId) == null) {
			r.add(Lang.TENYU_MANAGER_USERID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"userId=" + userId);
			b = false;
		}
		return b;
	}

}