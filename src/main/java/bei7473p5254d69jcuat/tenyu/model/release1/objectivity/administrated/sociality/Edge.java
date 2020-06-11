package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class Edge implements ValidatableI {
	/**
	 * ここへのエッジ
	 */
	private Long destSocialityId;
	private EdgeType type;

	/**
	 * エッジの重み
	 */
	private int weight;
	/**
	 * 重みの最大値
	 */
	public static final int weightMax = 1000 * 100;

	/**
	 * @return	重みを割合にして返す
	 */
	public float getWeightPercentage() {
		return weight / weightMax;
	}

	public boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (type == null) {
			vr.add(Lang.EDGE_TYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (destSocialityId == null) {
			vr.add(Lang.EDGE_DESTSOCIALITYID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(destSocialityId)) {
				vr.add(Lang.EDGE_DESTSOCIALITYID, Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (weight < 0) {
			vr.add(Lang.EDGE_WEIGHT, Lang.ERROR_TOO_LITTLE, "weight=" + weight);
			b = false;
		} else if (weight > weightMax) {
			vr.add(Lang.EDGE_WEIGHT, Lang.ERROR_TOO_BIG, "weight=" + weight);
			b = false;
		}
		return b;
	}

	@SuppressWarnings("unused")
	private Edge() {
	}

	public Edge(Long destSocialityId, EdgeType type) {
		this.destSocialityId = destSocialityId;
		this.type = type;
	}

	/**
	 * @param add	変動量
	 * パッケージプライベート
	 */
	void add(Edge add) {
		weight += add.getWeight();
	}

	public Long getDestSocialityId() {
		return destSocialityId;
	}

	public EdgeType getType() {
		return type;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		if (old == null || !(old instanceof Edge)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Edge old2 = (Edge) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getDestSocialityId(),
				old2.getDestSocialityId())) {
			r.add(Lang.EDGE_DESTSOCIALITYID, Lang.ERROR_UNALTERABLE,
					"destSocialityId=" + getDestSocialityId()
							+ " oldDestSocialityId="
							+ old2.getDestSocialityId());
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		SocialityStore sos = new SocialityStore(txn);
		if (sos.get(destSocialityId) == null) {
			r.add(Lang.EDGE_DESTSOCIALITYID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"destSocialityId=" + destSocialityId);
			b = false;
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((destSocialityId == null) ? 0 : destSocialityId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (int) (weight ^ (weight >>> 32));
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
		Edge other = (Edge) obj;
		if (destSocialityId == null) {
			if (other.destSocialityId != null)
				return false;
		} else if (!destSocialityId.equals(other.destSocialityId))
			return false;
		if (type != other.type)
			return false;
		if (weight != other.weight)
			return false;
		return true;
	}
}
