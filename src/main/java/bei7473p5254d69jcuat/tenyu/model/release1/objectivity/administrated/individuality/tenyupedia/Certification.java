package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.tenyupedia.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class Certification extends IndividualityObject
		implements CertificationI {
	/**
	 * 認定されたもの一覧
	 */
	private List<TenyuReferenceModelI<
			? extends ModelI>> certificateds = new ArrayList<>();
	private static final int refsMax = 1000 * 1000;

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	@Override
	public StoreName getStoreName() {
		return StoreNameObjectivity.CERTIFICATION;
	}

	@Override
	public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new CertificationGui(guiName, cssIdPrefix);
	}

	@Override
	public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new CertificationStore(txn);
	}

	private boolean validateCommmon(ValidationResult r) {
		boolean b = true;
		if (certificateds == null) {
			r.add(Lang.CERTIFICATION, Lang.CERTIFICATEDS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (certificateds.size() > refsMax) {
				r.add(Lang.CERTIFICATION, Lang.CERTIFICATEDS, Lang.ERROR_TOO_MANY,
						"size=" + certificateds.size());
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommmon(r)) {
			b = false;
		} else {
			for (TenyuReferenceModelI<? extends ModelI> e : certificateds) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommmon(r)) {
			b = false;
		} else {
			for (TenyuReferenceModelI<? extends ModelI> e : certificateds) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
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
		for (TenyuReferenceModelI<? extends ModelI> e : certificateds) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return null;
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

	public List<TenyuReferenceModelI<? extends ModelI>> getCertificateds() {
		return certificateds;
	}

	public void setCertificateds(
			List<TenyuReferenceModelI<? extends ModelI>> certificateds) {
		this.certificateds = certificateds;
	}

	public static int getRefsmax() {
		return refsMax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((certificateds == null) ? 0 : certificateds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Certification other = (Certification) obj;
		if (certificateds == null) {
			if (other.certificateds != null)
				return false;
		} else if (!certificateds.equals(other.certificateds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Certification [refs=" + certificateds + "]";
	}

}
