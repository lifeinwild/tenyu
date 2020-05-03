package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class CertificationStore
		extends IndividualityObjectStore<CertificationI, Certification> {
	public static final String modelName = Certification.class.getSimpleName();

	private static final StoreInfo refToId = new StoreInfo(
			modelName + "_startSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES, true);

	public CertificationStore(Transaction txn) {
		super(txn);
	}

	public static StoreInfo getReftoid() {
		return refToId;
	}

	@Override
	protected boolean createIndividualityObjectConcrete(CertificationI o)
			throws Exception {
		for (TenyuReference<? extends ModelI> sn : o.getRefs()) {
			if (!util.put(getReftoid(), cnvBA(sn.getStoreKey()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			CertificationI updated, CertificationI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(CertificationI o)
			throws Exception {
		for (TenyuReference<? extends ModelI> sn : o.getRefs()) {
			if (!util.deleteDupSingle(getReftoid(), cnvBA(sn.getStoreKey()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(CertificationI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (TenyuReference<? extends ModelI> e : o.getRefs()) {
			if (!existByRef(e, o.getId())) {
				vr.add(Lang.CERTIFICATION, Lang.REFS, Lang.ERROR_DB_NOTFOUND,
						"ref=" + e);
				b = false;
				break;
			}
		}
		return b;
	}

	public boolean existByRef(TenyuReference<? extends ModelI> ref, Long id) {
		if (ref == null || id == null)
			return false;
		return util.getDupSingle(getReftoid(), cnvBA(ref.getStoreKey()),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(refToId);
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(CertificationI o,
			ValidationResult vr) throws Exception {
		boolean b = true;

		for (TenyuReference<? extends ModelI> e : o.getRefs()) {
			if (existByRef(e, o.getId())) {
				vr.add(Lang.CERTIFICATION, Lang.REFS, Lang.ERROR_DB_EXIST,
						"ref=" + e);
				b = false;
				break;
			}
		}

		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(CertificationI updated,
			CertificationI old) throws Exception {
		if (!updateCollectionSubIndex(getReftoid(), old.getId(),
				updated.getId(), () -> updated.getRefs(), () -> old.getRefs(),
				k -> cnvBA(k.getStoreKey())))
			return false;

		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof Certification)
			return true;
		return false;
	}

	@Override
	protected Certification chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Certification)
				return (Certification) o;
			throw new InvalidTargetObjectTypeException(
					"not User object in CertificationStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

}
