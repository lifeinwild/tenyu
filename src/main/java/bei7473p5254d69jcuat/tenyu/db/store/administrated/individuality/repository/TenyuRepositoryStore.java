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

public class TenyuRepositoryStore
		extends IndividualityObjectStore<TenyuRepositoryI, TenyuRepository> {
	public static final String modelName = TenyuRepository.class
			.getSimpleName();

	/**
	 * repoId : repoId
	 * 他のリポジトリとの関係性
	 */
	private static final StoreInfo relatedIdToId = new StoreInfo(
			modelName + "_relatedIdToId_Dup", StoreConfig.WITH_DUPLICATES,
			true);

	public static StoreInfo getRelatedIdtoId() {
		return relatedIdToId;
	}

	public TenyuRepositoryStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean createIndividualityObjectConcrete(TenyuRepositoryI o)
			throws Exception {
		for (Long relatedId : o.getRelatedIds()) {
			if (!util.put(getRelatedIdtoId(), cnvL(relatedId),
					cnvL(o.getId()))) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			TenyuRepositoryI updated, TenyuRepositoryI old,
			ValidationResult r) {
		boolean b = true;

		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(TenyuRepositoryI o)
			throws Exception {
		for (Long relatedId : o.getRelatedIds()) {
			if (!util.deleteDupSingle(relatedIdToId, cnvL(relatedId),
					cnvL(o.getId())))
				return false;
		}

		return true;
	}

	public boolean existByRelatedId(Long relatedId, Long id) {
		if (relatedId == null || id == null)
			return false;
		return util.getDupSingle(getRelatedIdtoId(), cnvL(relatedId), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(TenyuRepositoryI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (Long relatedId : o.getRelatedIds()) {
			if (!existByRelatedId(relatedId, o.getId())) {
				vr.add(Lang.TENYU_REPOSITORY, Lang.TENYU_REPOSITORY_RELATED_ID,
						Lang.ERROR_DB_NOTFOUND, "relatedId=" + relatedId);
				b = false;
				break;
			}
		}

		return b;
	}

	public List<Long> getIdsByRelatedId(Long relatedId) {
		return util.getDup(relatedIdToId, cnvL(relatedId), v -> cnvL(v));
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(relatedIdToId);
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(TenyuRepositoryI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (Long relatedId : o.getRelatedIds()) {
			if (existByRelatedId(relatedId, o.getId())) {
				vr.add(Lang.TENYU_REPOSITORY_RELATED_ID, Lang.ERROR_DB_EXIST,
						"relatedId=" + relatedId);
				b = false;
				break;
			}
		}

		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(
			TenyuRepositoryI updated, TenyuRepositoryI old) throws Exception {
		if (!updateCollectionSubIndex(getRelatedIdtoId(), old.getId(),
				updated.getId(), () -> updated.getRelatedIds(),
				() -> old.getRelatedIds(), k -> cnvL(k)))
			return false;

		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof TenyuRepositoryI;
	}

	@Override
	protected TenyuRepository chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof TenyuRepository)
				return (TenyuRepository) o;
			throw new InvalidTargetObjectTypeException(
					"not TenyuRepository object in TenyuRepositoryStore");
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
