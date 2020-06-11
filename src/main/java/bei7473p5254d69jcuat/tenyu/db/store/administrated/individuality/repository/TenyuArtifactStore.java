package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class TenyuArtifactStore
		extends IndividualityObjectStore<TenyuArtifactI, TenyuArtifact> {
	public static final String modelName = TenyuArtifact.class.getSimpleName();
	private static final StoreInfo tenyuRepositoryIdToId = new StoreInfo(
			modelName + "_tenyuRepositoryIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	public TenyuArtifactStore(Transaction txn) {
		super(txn);
	}

	public static StoreInfo getTenyurepositoryidtoid() {
		return tenyuRepositoryIdToId;
	}


	@Override
	protected boolean noExistIndividualityObjectConcrete(TenyuArtifactI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByTenyuRepositoryId(o.getTenyuRepositoryId(), o.getId())) {
			vr.add(Lang.TENYU_ARTIFACT, Lang.TENYU_REPOSITORY_ID,
					Lang.ERROR_DB_EXIST,
					"tenyuRepositoryId=" + o.getTenyuRepositoryId());
			b = false;
		}

		return b;
	}

	public List<Long> getIdsByTenyuRepositoryId(Long tenyuRepositoryId) {
		return util.getDup(tenyuRepositoryIdToId, cnvL(tenyuRepositoryId),
				v -> cnvL(v));
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(TenyuArtifactI o)
			throws Exception {
		if (!util.deleteDupSingle(getTenyurepositoryidtoid(),
				cnvL(o.getTenyuRepositoryId()), cnvL(o.getId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			TenyuArtifactI updated, TenyuArtifactI old, ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(TenyuArtifactI updated,
			TenyuArtifactI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getTenyuRepositoryId(),
				old.getTenyuRepositoryId())) {
			if (!util.deleteDupSingle(tenyuRepositoryIdToId,
					cnvL(old.getTenyuRepositoryId()), cnvL(old.getId())))
				return false;
			if (!util.put(tenyuRepositoryIdToId,
					cnvL(updated.getTenyuRepositoryId()),
					cnvL(updated.getId())))
				return false;
		}
		return true;
	}

	@Override
	protected boolean createIndividualityObjectConcrete(TenyuArtifactI o)
			throws Exception {
		if (!util.put(getTenyurepositoryidtoid(),
				cnvL(o.getTenyuRepositoryId()), cnvL(o.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(TenyuArtifactI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByTenyuRepositoryId(o.getTenyuRepositoryId(), o.getId())) {
			vr.add(Lang.TENYU_ARTIFACT, Lang.TENYU_REPOSITORY_ID,
					Lang.ERROR_DB_NOTFOUND,
					"tenyuRepositoryId=" + o.getTenyuRepositoryId());
			b = false;
		}
		return b;
	}

	public boolean existByTenyuRepositoryId(Long tenyuRepositoryId, Long id) {
		if (tenyuRepositoryId == null || id == null)
			return false;
		return util.getDupSingle(getTenyurepositoryidtoid(),
				cnvL(tenyuRepositoryId), cnvL(id), bi -> cnvL(bi)) != null;
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(tenyuRepositoryIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof TenyuArtifactI;
	}

	@Override
	protected TenyuArtifact chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof TenyuArtifact)
				return (TenyuArtifact) o;
			throw new InvalidTargetObjectTypeException(
					"not TenyuArtifact object in TenyuArtifactStore");
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
