package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.role;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.admin.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 審査役やサーバー役など特殊な権限がどのユーザーに与えられているか。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Role extends IndividualityObject implements RoleI {
	/**
	 * admins最大件数
	 */
	public static final int adminsMax = 1000 * 100;

	/**
	 * 権限を持つユーザーの一覧
	 */
	protected List<Long> adminUserIds = new ArrayList<>();

	/**
	 * システムが自動的に作成した権限か
	 */
	private boolean system = true;

	public boolean addAdmin(Long userId) {
		if (adminUserIds.contains(userId))
			return false;
		return adminUserIds.add(userId);
	}

	public boolean addAdmins(List<Long> userIds) {
		for (Long userId : userIds) {
			if (!addAdmin(userId)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//議決
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//議決
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//議決
	}

	public List<Long> getAdminUserIds() {
		return Collections.unmodifiableList(adminUserIds);
	}

	/**
	 * @return	このRoleが割り当てられた全ノード
	 */
	public List<NodeIdentifierUser> getAdminNodes() {
		List<NodeIdentifierUser> r = new ArrayList<>();
		Glb.getObje().execute(txn -> {
			try {
				UserStore us = new UserStore(txn);
				for (Long userId : getAdminUserIds()) {
					User u = us.get(userId);
					if (u == null) {
						Glb.getLogger().warn("user is null. userId=" + userId,
								new Exception());
						continue;
					}
					r.add(u.getNodeIdentifierByRole(getId()));
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});
		return r;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectI.getVoteId();
	}

	@Override
	public List<Long> getSpecialMainAdministratorIds() {
		List<Long> r = new ArrayList<>();
		r.add(IdObjectI.getVoteId());
		return r;
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectI.getSystemId();
	}

	public boolean isAdmin(Long userId) {
		if (adminUserIds == null || adminUserIds.size() == 0 || userId == null)
			return false;
		return adminUserIds.contains(userId);
	}

	/**
	 * 場合によって同じ役割を持つユーザーの中でメインか否かが問題になる。
	 * リストの順序をそのまま優先度と捉え、最前列のユーザーをメイン管理者とする。
	 * 指定されたユーザーIDはリストの最前列にあるか。
	 * @return
	 */
	public boolean isMainAdmin(Long userId) {
		if (adminUserIds == null || adminUserIds.size() == 0 || userId == null)
			return false;
		Long main = adminUserIds.get(0);
		return main.equals(userId);
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;//議決限定
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;//議決限定
	}

	public boolean isSystem() {
		return system;
	}

	public boolean remove(Long userId) {
		return adminUserIds.remove(userId);
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	protected boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (adminUserIds == null) {
			r.add(Lang.ROLE_ADMINS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (adminUserIds.size() > adminsMax) {
				r.add(Lang.ROLE_ADMINS, Lang.ERROR_TOO_MANY,
						"size=" + adminUserIds.size());
				b = false;
			} else if (!IdObject.validateIdStandardNotSpecialId(adminUserIds)) {
				r.add(Lang.ROLE_ADMINS, Lang.ERROR_INVALID,
						"admins=" + adminUserIds);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Role)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Role old2 = (Role) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(isSystem(), old2.isSystem())) {
			r.add(Lang.ROLE_SYSTEM, Lang.ERROR_UNALTERABLE,
					"system=" + isSystem() + " oldSystem=" + old2.isSystem());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		for (Long id : adminUserIds) {
			User u = us.get(id);
			if (u == null) {
				r.add(Lang.ROLE_ADMINS, Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"adminId=" + id);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public RoleGui getGui(String guiName, String cssIdPrefix) {
		return new RoleGui(guiName, cssIdPrefix);
	}

	@Override
	public RoleStore getStore(Transaction txn) {
		return new RoleStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.ROLE;
	}

}
