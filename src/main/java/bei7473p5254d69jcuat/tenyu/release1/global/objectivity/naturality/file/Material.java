package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.env.*;

/**
 * 分担値の素材ファイル
 * @author exceptiontenyu@gmail.com
 *
 */
public class Material extends UploadFile implements MaterialDBI {
	private static final int userLimitMax = 2000;

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;
	}

	public static int getUserlimitmax() {
		return userLimitMax;
	}

	public static boolean validateUserLimit(List<Long> userLimit) {
		return IdObject.validateIdStandardNotSpecialId(userLimit)
				&& userLimit.size() <= userLimitMax;
	}

	/**
	 * バージョン的な数値
	 * 更新操作のたびにインクリメントされる
	 */
	protected int updateCount = 0;

	/**
	 * 使用可能ユーザー一覧
	 * nullなら全員使用可能
	 */
	private List<Long> userLimitation;

	public Material() {
	}

	public Material(List<Long> userLimitation, String name, String exp,
			long fileSize, byte[] fileHash) {
		this.userLimitation = userLimitation;
		setName(name);
		setExplanation(exp);
		setFileHash(fileHash);
		setFileSize(fileSize);
	}

	@Override
	public String getBaseDir() {
		return Glb.getFile().getUploadFileDirSingle()
				+ Glb.getFile().getMaterialDirSingle();
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdUpdate();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(registererUserId);
		return r;
	}

	public int getUpdateCount() {
		return updateCount;
	}

	public List<Long> getUserLimitation() {
		return userLimitation;
	}

	public void incrementUpdateCount() {
		updateCount++;
	}

	public void setUpdateCount(int updateCount) {
		this.updateCount = updateCount;
	}

	public void setUserLimitation(List<Long> userLimitation) {
		this.userLimitation = userLimitation;
	}

	private final boolean validateAtCommonUploadFileConcrete(
			ValidationResult r) {
		boolean b = true;
		if (userLimitation == null) {
			//ユーザー制限しないならnullで問題無し
		} else {
			if (userLimitation.size() > userLimitMax) {
				r.add(Lang.MATERIAL_USER_LIMITATION, Lang.ERROR_TOO_MANY,
						"size=" + userLimitation.size());
				b = false;
			} else if (!validateUserLimit(userLimitation)) {
				r.add(Lang.MATERIAL_USER_LIMITATION, Lang.ERROR_INVALID,
						"userLimitation=" + userLimitation);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateUploadFileConcrete(
			ValidationResult r) {
		return validateAtCommonUploadFileConcrete(r);
	}

	@Override
	protected boolean validateAtUpdateChangeUploadFileConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Material)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//Material old2 = (Material) old;

		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateUploadFileConcrete(
			ValidationResult r) {
		return validateAtCommonUploadFileConcrete(r);
	}

	@Override
	public boolean validateReferenceUploadFileConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (userLimitation != null) {
			for (Long id : userLimitation) {
				if (us.get(id) == null) {
					r.add(Lang.MATERIAL_USER_LIMITATION,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							Lang.MATERIAL_USER_LIMITATION.toString() + " id="
									+ id);
					b = false;
					break;
				}
			}
		}
		return b;
	}

}
