package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyuArtifact extends IndividualityObject
		implements TenyuArtifactI {
	private int activePublication = 0;

	private Long tenyuRepositoryId;

	@Override
	public int getActivePublication() {
		return activePublication;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	@Override
	public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new TenyuArtifactGui(guiName, cssIdPrefix);
	}

	@Override
	public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new TenyuArtifactStore(txn);
	}

	@Override
	public StoreName getStoreName() {
		return StoreNameObjectivity.TENYU_ARTIFACT;
	}

	public Long getTenyuRepositoryId() {
		return tenyuRepositoryId;
	}

	public void setActivePublication(int activePublication) {
		this.activePublication = activePublication;
	}

	public void setTenyuRepositoryId(Long tenyuRepositoryId) {
		this.tenyuRepositoryId = tenyuRepositoryId;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof TenyuArtifact))
			return false;
		TenyuArtifact o = (TenyuArtifact) old;
		if (Glb.getUtil().notEqual(tenyuRepositoryId, o.getTenyuRepository())) {
			r.add(Lang.TENYU_ARTIFACT, Lang.TENYU_REPOSITORY_ID,
					Lang.ERROR_UNALTERABLE, "repositoryId=" + tenyuRepositoryId
							+ " old.repositoryId=" + o.getTenyuRepositoryId());
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
		if (!Model.validateIdStandardNotSpecialId(tenyuRepositoryId)) {
			r.add(Lang.TENYU_ARTIFACT, Lang.TENYU_REPOSITORY_ID,
					Lang.ERROR_INVALID, "repositoryId=" + tenyuRepositoryId);
			b = false;
		}
		if (activePublication < 0 || activePublication > 100) {
			r.add(Lang.TENYU_ARTIFACT, Lang.TENYU_ARTIFACT_ACTIVE_PUBLICATION,
					Lang.ERROR_INVALID,
					"activePublication=" + activePublication);
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
		if (trs.get(tenyuRepositoryId) == null) {
			r.add(Lang.TENYU_ARTIFACT, Lang.TENYU_REPOSITORY_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"tenyuRepositoryId=" + tenyuRepositoryId);
		}

		return b;
	}

}
