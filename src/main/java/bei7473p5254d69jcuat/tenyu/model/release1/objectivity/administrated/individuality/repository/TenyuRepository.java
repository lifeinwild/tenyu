package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyuRepository extends IndividualityObject
		implements TenyuRepositoryI {
	public static final int activePublicationMax = 100;

	public static final int activePublicationMin = 0;

	public static final int relatedsMax = 1000;

	public static int getActivepublicationmax() {
		return activePublicationMax;
	}

	public static int getActivepublicationmin() {
		return activePublicationMin;
	}

	public static int getRelatedsmax() {
		return relatedsMax;
	}

	private int nodeNumber = 0;

	private List<Long> relatedIds = new ArrayList<>();

	private TenyuRepositoryType type = TenyuRepositoryType.GIT;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuRepository other = (TenyuRepository) obj;
		if (nodeNumber != other.nodeNumber)
			return false;
		if (relatedIds == null) {
			if (other.relatedIds != null)
				return false;
		} else if (!relatedIds.equals(other.relatedIds))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public TenyuRepositoryGui getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new TenyuRepositoryGui(guiName, cssIdPrefix);
	}

	@Override
	public NodeIdentifierUser getNodeIdentifier() {
		return new NodeIdentifierUser(getMainAdministratorUserId(), nodeNumber);
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	@Override
	public List<Long> getRelatedIds() {
		return relatedIds;
	}

	@Override
	public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new TenyuRepositoryStore(txn);
	}

	@Override
	public StoreName getStoreName() {
		return StoreNameObjectivity.TENYU_REPOSITORY;
	}

	@Override
	public TenyuRepositoryType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + nodeNumber;
		result = prime * result
				+ ((relatedIds == null) ? 0 : relatedIds.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	public void setRelatedIds(List<Long> relatedIds) {
		this.relatedIds = relatedIds;
	}

	public void setType(TenyuRepositoryType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "TenyuRepository [relatedIds=" + relatedIds + ", type=" + type
				+ ", nodeNumber=" + nodeNumber + "]";
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof TenyuRepository)) {
			return false;
		}
		boolean b = true;
		TenyuRepository o = (TenyuRepository) old;
		if (Glb.getUtil().notEqual(type, o.getType())) {
			r.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_TYPE,
					Lang.ERROR_UNALTERABLE,
					"type=" + type + " old.type=" + o.getType());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (relatedIds == null) {
			r.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_RELATED_IDS,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (relatedIds.size() > relatedsMax) {
				r.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_RELATED_IDS,
						Lang.ERROR_TOO_MANY,
						"relateds.size=" + relatedIds.size());
				b = false;
			} else {
				HashSet<Long> check = new HashSet<>(relatedIds);
				if (check.size() != relatedIds.size()) {
					r.add(Lang.TENYU_REPOSITORY,
							Lang.TENYU_REPOSITORY_RELATED_IDS,
							Lang.ERROR_DUPLICATE, "relateds=" + relatedIds);
					b = false;
				}

				for (Long relatedId : relatedIds) {
					if (!Model.validateIdStandardNotSpecialId(relatedId)) {
						r.add(Lang.TENYU_REPOSITORY,
								Lang.TENYU_REPOSITORY_RELATED_ID,
								Lang.ERROR_INVALID, "relatedId=" + relatedId);
						b = false;
						break;
					}
				}
			}
		}

		if (nodeNumber < 0) {
			r.add(Lang.TENYU_REPOSITORY, Lang.NODE_NUMBER, Lang.ERROR_INVALID,
					"nodeNumber=" + nodeNumber);
			b = false;
		}

		if (type == null) {
			r.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_TYPE,
					Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		TenyuRepositoryStore trs = new TenyuRepositoryStore(txn);
		for (Long relatedId : relatedIds) {
			if (trs.get(relatedId) == null) {
				r.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_RELATED_ID,
						Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"relatedId=" + relatedId);
				b = false;
				break;
			}
		}

		return b;
	}
}
