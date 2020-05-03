package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class ModelConditionStore
		extends IndividualityObjectStore<ModelConditionI, ModelCondition> {
	public static final String modelName = ModelCondition.class.getSimpleName();

	private static final StoreInfo otherModelCoditionIdToId = new StoreInfo(
			modelName + "_otherCoditionIdToId_Dup", StoreConfig.WITH_DUPLICATES,
			true);
	private static final StoreInfo modelNameToId = new StoreInfo(
			modelName + "_modelNameToId_Dup", StoreConfig.WITH_DUPLICATES,
			true);
	private static final StoreInfo manualToId = new StoreInfo(
			modelName + "_manualToId_Dup", StoreConfig.WITH_DUPLICATES, true);
	private static final StoreInfo localeToId = new StoreInfo(
			modelName + "_localeToId_Dup", StoreConfig.WITH_DUPLICATES, true);
	private static final StoreInfo userIdToId = new StoreInfo(
			modelName + "_userIdToId_Dup", StoreConfig.WITH_DUPLICATES, true);
	private static final StoreInfo startSocialityIdToId = new StoreInfo(
			modelName + "_startSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public ModelConditionStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean createIndividualityObjectConcrete(ModelConditionI o)
			throws Exception {
		for (Entry<Logic, Long> e : o.getOtherModelConditionIds().entrySet()) {
			Logic logic = e.getKey();
			Long otherModelConditionId = e.getValue();
			if (!util
					.put(getOthercoditionidtoid(),
							cnvBA(ModelConditionI.getModelConditionStoreKey(
									logic, otherModelConditionId)),
							cnvL(o.getId()))) {
				return false;
			}
		}
		for (StoreName sn : o.getStoreNames()) {
			if (!util.put(getModelnametoid(), cnvS(sn.getModelName()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (TenyuReference<? extends ModelI> e : o.getManual()) {
			if (!util.put(getManualtoid(), cnvBA(e.getStoreKey()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Locale l : o.getLocales()) {
			if (!util.put(getLocaletoid(),
					cnvS(IndividualityObjectI.getLocaleStrStatic(l)),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Long userId : o.getUserIds()) {
			if (!util.put(getUseridtoid(), cnvL(userId), cnvL(o.getId()))) {
				return false;
			}
		}
		if (!util.put(getStartsocialityidtoid(), cnvL(o.getStartSocialityId()),
				cnvL(o.getId()))) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			ModelConditionI updated, ModelConditionI old, ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(ModelConditionI o)
			throws Exception {
		for (Entry<Logic, Long> e : o.getOtherModelConditionIds().entrySet()) {
			Logic logic = e.getKey();
			Long otherConditionId = e.getValue();
			if (!util
					.deleteDupSingle(getOthercoditionidtoid(),
							cnvBA(ModelConditionI.getModelConditionStoreKey(
									logic, otherConditionId)),
							cnvL(o.getId()))) {
				return false;
			}
		}
		for (StoreName sn : o.getStoreNames()) {
			if (!util.deleteDupSingle(getModelnametoid(),
					cnvS(sn.getModelName()), cnvL(o.getId()))) {
				return false;
			}
		}
		for (TenyuReference<? extends ModelI> e : o.getManual()) {
			if (!util.deleteDupSingle(getManualtoid(), cnvBA(e.getStoreKey()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Locale l : o.getLocales()) {
			if (!util.deleteDupSingle(getLocaletoid(),
					cnvS(IndividualityObjectI.getLocaleStrStatic(l)),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Long userId : o.getUserIds()) {
			if (!util.deleteDupSingle(getUseridtoid(), cnvL(userId),
					cnvL(o.getId()))) {
				return false;
			}
		}
		if (!util.deleteDupSingle(getStartsocialityidtoid(),
				cnvL(o.getStartSocialityId()), cnvL(o.getId()))) {
			return false;
		}
		return true;
	}

	public boolean existByOtherConditionId(byte[] otherConditionStoreKey,
			Long id) {
		if (otherConditionStoreKey == null || id == null)
			return false;
		return util.getDupSingle(getOthercoditionidtoid(),
				cnvBA(otherConditionStoreKey), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public boolean existByModelName(String modelName, Long id) {
		if (modelName == null || id == null)
			return false;
		return util.getDupSingle(getModelnametoid(), cnvS(modelName), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public boolean existByManual(byte[] manualStoreKey, Long id) {
		if (manualStoreKey == null || id == null)
			return false;
		return util.getDupSingle(getManualtoid(), cnvBA(manualStoreKey),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByStartSocialityId(Long startSocialityId, Long id) {
		if (startSocialityId == null || id == null)
			return false;
		return util.getDupSingle(getStartsocialityidtoid(),
				cnvL(startSocialityId), cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByLocale(String locale, Long id) {
		if (locale == null || id == null)
			return false;
		return util.getDupSingle(getLocaletoid(), cnvS(locale), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public boolean existByUserId(Long userId, Long id) {
		if (userId == null || id == null)
			return false;
		return util.getDupSingle(getUseridtoid(), cnvL(userId), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(ModelConditionI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (Entry<Logic, Long> e : o.getOtherModelConditionIds().entrySet()) {
			if (!existByOtherConditionId(ModelConditionI
					.getModelConditionStoreKey(e.getKey(), e.getValue()),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.OTHER_MODEL_CONDITION_IDS,
						Lang.ERROR_DB_NOTFOUND, "logic=" + e.getKey()
								+ " otherModelConditionId=" + e.getValue());
				b = false;
				break;
			}
		}

		for (StoreName si : o.getStoreNames()) {
			String mn = si.getModelName();
			if (!existByModelName(mn, o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_NAME,
						Lang.ERROR_DB_NOTFOUND, "modelName=" + mn);
				b = false;
				break;
			}
		}

		for (TenyuReference<? extends ModelI> e : o.getManual()) {
			if (!existByManual(e.getStoreKey(), o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_CONDITION_MANUAL,
						Lang.ERROR_DB_NOTFOUND, "manual=" + e);
				b = false;
				break;
			}
		}

		for (Locale l : o.getLocales()) {
			if (!existByLocale(IndividualityObjectI.getLocaleStrStatic(l),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.LOCALES,
						Lang.ERROR_DB_NOTFOUND, "locale=" + l);
				b = false;
				break;
			}
		}

		for (Long userId : o.getUserIds()) {
			if (!existByUserId(userId, o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.USER_ID,
						Lang.ERROR_DB_NOTFOUND, "userId=" + userId);
				b = false;
				break;
			}
		}

		if (!existByStartSocialityId(o.getStartSocialityId(), o.getId())) {
			vr.add(Lang.MODEL_CONDITION, Lang.START_SOCIALITY_ID,
					Lang.ERROR_DB_NOTFOUND,
					"startSocialityId=" + o.getStartSocialityId());
			b = false;
		}

		return b;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(otherModelCoditionIdToId);
		r.add(modelNameToId);
		r.add(manualToId);
		r.add(startSocialityIdToId);
		r.add(localeToId);
		r.add(userIdToId);
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(ModelConditionI o,
			ValidationResult vr) throws Exception {
		boolean b = true;

		for (Entry<Logic, Long> e : o.getOtherModelConditionIds().entrySet()) {
			if (existByOtherConditionId(ModelConditionI
					.getModelConditionStoreKey(e.getKey(), e.getValue()),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.OTHER_MODEL_CONDITION_IDS,
						Lang.ERROR_DB_EXIST, "logic=" + e.getKey()
								+ " otherModelConditionId=" + e.getValue());
				b = false;
				break;
			}
		}

		for (StoreName si : o.getStoreNames()) {
			String mn = si.getModelName();
			if (existByModelName(mn, o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_NAME,
						Lang.ERROR_DB_EXIST, "modelName=" + mn);
				b = false;
				break;
			}
		}

		for (TenyuReference<? extends ModelI> e : o.getManual()) {
			if (existByManual(e.getStoreKey(), o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_CONDITION_MANUAL,
						Lang.ERROR_DB_EXIST, "manual=" + e);
				b = false;
				break;
			}
		}

		for (Locale l : o.getLocales()) {
			if (existByLocale(IndividualityObjectI.getLocaleStrStatic(l),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.LOCALES, Lang.ERROR_DB_EXIST,
						"locale=" + l);
				b = false;
				break;
			}
		}

		for (Long userId : o.getUserIds()) {
			if (existByUserId(userId, o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.USER_ID, Lang.ERROR_DB_EXIST,
						"userId=" + userId);
				b = false;
				break;
			}
		}

		if (existByStartSocialityId(o.getStartSocialityId(), o.getId())) {
			vr.add(Lang.MODEL_CONDITION, Lang.START_SOCIALITY_ID,
					Lang.ERROR_DB_EXIST,
					"startSocialityId=" + o.getStartSocialityId());
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(ModelConditionI updated,
			ModelConditionI old) throws Exception {
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;
		if (!updateCollectionSubIndex(getModelnametoid(), old.getId(),
				updated.getId(), () -> updated.getStoreNames(),
				() -> old.getStoreNames(), (k) -> cnvS(k.getModelName())))
			return false;

		Long updatedStartSocialityId = updated.getStartSocialityId();
		Long oldStartSocialityId = old.getStartSocialityId();
		if (Glb.getUtil().notEqual(updatedStartSocialityId,
				oldStartSocialityId)) {
			if (oldStartSocialityId != null) {
				if (!util.deleteDupSingle(getStartsocialityidtoid(),
						cnvL(oldStartSocialityId), cnvL(old.getId())))
					return false;
			}
			if (!util.put(getStartsocialityidtoid(),
					cnvL(updatedStartSocialityId), cnvL(updated.getId())))
				return false;

		}

		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof ModelConditionI)
			return true;
		return false;
	}

	@Override
	protected ModelCondition chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof ModelCondition)
				return (ModelCondition) o;
			throw new InvalidTargetObjectTypeException(
					"not User object in ModelConditionStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

	public static String getModelname() {
		return modelName;
	}

	public static StoreInfo getOthercoditionidtoid() {
		return otherModelCoditionIdToId;
	}

	public static StoreInfo getModelnametoid() {
		return modelNameToId;
	}

	public static StoreInfo getManualtoid() {
		return manualToId;
	}

	public static StoreInfo getStartsocialityidtoid() {
		return startSocialityIdToId;
	}

	public static StoreInfo getLocaletoid() {
		return localeToId;
	}

	public static StoreInfo getUseridtoid() {
		return userIdToId;
	}
}
