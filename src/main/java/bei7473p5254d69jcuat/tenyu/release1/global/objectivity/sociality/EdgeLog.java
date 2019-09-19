package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * Edgeはフロー計算を考慮するとできるだけサイズを抑えたいので
 * コメント等はこちらに記録して分ける。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class EdgeLog extends ObjectivityObject implements EdgeLogDBI {
	public static final int commentMax = 1000;

	public static int getCommentmax() {
		return commentMax;
	}

	/**
	 * 変動量
	 */
	private int add;

	/**
	 * コメント
	 */
	private String comment;
	/**
	 * 行われた操作の種類
	 */
	private EdgeCRUD crudType;
	/**
	 * ここからのエッジ
	 */
	private Long fromSocialityId;

	/**
	 * ここへのエッジ
	 */
	private Long toSocialityId;

	public int getAdd() {
		return add;
	}

	private List<Long> getAdmin() {
		List<Long> r = new ArrayList<>();
		r.add(fromSocialityId);
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return getAdmin();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdmin();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdmin();
	}

	public String getComment() {
		return comment;
	}

	public EdgeCRUD getCrudType() {
		return crudType;
	}

	public Long getFromSocialityId() {
		return fromSocialityId;
	}

	public Long getToSocialityId() {
		return toSocialityId;
	}

	public void setAdd(int add) {
		this.add = add;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCrudType(EdgeCRUD crudType) {
		this.crudType = crudType;
	}

	public void setFromSocialityId(Long fromSocialityId) {
		this.fromSocialityId = fromSocialityId;
	}

	public void setToSocialityId(Long toSocialityId) {
		this.toSocialityId = toSocialityId;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (fromSocialityId == null) {
			r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(fromSocialityId)) {
				r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (toSocialityId == null) {
			r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(toSocialityId)) {
				r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (comment != null) {
			if (comment.length() > commentMax) {
				r.add(Lang.SOCIALITY_EDGELOG_COMMENT, Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				if (!Naturality.validateText(Lang.SOCIALITY_EDGELOG_COMMENT,
						comment, r)) {
					b = false;
				}
			}
		}

		if (crudType == null) {
			r.add(Lang.SOCIALITY_EDGELOG_CRUDTYPE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (crudType == EdgeCRUD.UNKNOWN) {
				r.add(Lang.SOCIALITY_EDGELOG_CRUDTYPE, Lang.ERROR_INVALID);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof EdgeLog)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		EdgeLog old2 = (EdgeLog) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getFromSocialityId(),
				old2.getFromSocialityId())) {
			r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_UNALTERABLE,
					"fromSocialityId=" + getFromSocialityId()
							+ " oldFromSocialityId="
							+ old2.getFromSocialityId());
			b = false;
		}

		if (Glb.getUtil().notEqual(getAdd(), old2.getAdd())) {
			r.add(Lang.SOCIALITY_EDGELOG_ADD, Lang.ERROR_UNALTERABLE,
					"add=" + getAdd() + " oldAdd=" + old2.getAdd());
			b = false;
		}

		if (Glb.getUtil().notEqual(getToSocialityId(),
				old2.getToSocialityId())) {
			r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID, Lang.ERROR_UNALTERABLE,
					"toSocialityId=" + getToSocialityId() + " oldToSocialityId="
							+ old2.getToSocialityId());
			b = false;
		}

		if (Glb.getUtil().notEqual(getCrudType(), old2.getCrudType())) {
			r.add(Lang.SOCIALITY_EDGELOG_CRUDTYPE, Lang.ERROR_UNALTERABLE,
					"crudType=" + getCrudType() + " oldCrudType="
							+ old2.getCrudType());
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		SocialityStore s = new SocialityStore(txn);

		Sociality so1 = s.get(fromSocialityId);
		if (so1 == null) {
			r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		Sociality so2 = s.get(toSocialityId);
		if (so2 == null) {
			r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		return b;
	}

	public static enum EdgeCRUD {
		CREATE, DELETE, UNKNOWN, UPDATE
	}

}
