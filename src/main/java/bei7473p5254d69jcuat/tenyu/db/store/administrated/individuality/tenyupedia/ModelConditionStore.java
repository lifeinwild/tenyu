package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class ModelConditionStore
		extends IndividualityObjectStore<ModelConditionI, ModelCondition> {
	public static final String modelName = ModelCondition.class.getSimpleName();
	private static final StoreInfo localeConditionToId = new StoreInfo(
			modelName + "_localeToId_Dup", StoreConfig.WITH_DUPLICATES, true);

	private static final StoreInfo manualToId = new StoreInfo(
			modelName + "_manualToId_Dup", StoreConfig.WITH_DUPLICATES, true);
	private static final StoreInfo modelNameToId = new StoreInfo(
			modelName + "_modelNameToId_Dup", StoreConfig.WITH_DUPLICATES,
			true);
	private static final StoreInfo otherModelCoditionIdToId = new StoreInfo(
			modelName + "_otherModelCoditionIdToId_Dup",
			StoreConfig.WITH_DUPLICATES, true);
	private static final StoreInfo startSocialityIdToId = new StoreInfo(
			modelName + "_startSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);
	private static final StoreInfo registererUserIdToId = new StoreInfo(
			modelName + "_registererUserIdToId_Dup",
			StoreConfig.WITH_DUPLICATES, true);

	public static StoreInfo getLocaleConditiontoid() {
		return localeConditionToId;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public static StoreInfo getManualtoid() {
		return manualToId;
	}

	public static String getModelname() {
		return modelName;
	}

	public static StoreInfo getModelnametoid() {
		return modelNameToId;
	}

	public static StoreInfo getOtherModelCoditionidtoid() {
		return otherModelCoditionIdToId;
	}

	public static StoreInfo getStartsocialityidtoid() {
		return startSocialityIdToId;
	}

	public static StoreInfo getRegistererUseridtoid() {
		return registererUserIdToId;
	}

	public ModelConditionStore(Transaction txn) {
		super(txn);
	}

	public List<Long> getIdsByLocale(Locale loc) {
		return util.getDup(getLocaleConditiontoid(),
				cnvS(IndividualityObjectI.getLocaleStrStatic(loc)),
				v -> cnvL(v));
	}

	public List<Long> getIdsByManual(
			TenyuReferenceModelI<? extends ModelI> man) {
		return util.getDup(getManualtoid(), cnvBA(man.getStoreKeyReferenced()),
				v -> cnvL(v));
	}

	public List<Long> getIdsByModelName(String modelName) {
		return util.getDup(getModelnametoid(), cnvS(modelName), v -> cnvL(v));
	}

	public List<Long> getIdsByOtherModelCondition(Logic l,
			Long otherModelConditionId) {
		return util.getDup(
				getOtherModelCoditionidtoid(), cnvBA(ModelConditionI
						.getModelConditionStoreKey(l, otherModelConditionId)),
				v -> cnvL(v));
	}

	public List<Long> getIdsByStartSocialityId(Long startSocialityId) {
		return util.getDup(getLocaleConditiontoid(), cnvL(startSocialityId),
				v -> cnvL(v));
	}

	public List<Long> getIdsByRegistererUserIdAsCondition(Long registererUserId) {
		return util.getDup(getRegistererUseridtoid(), cnvL(registererUserId),
				v -> cnvL(v));
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
					"not ModelCondition object in ModelConditionStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(ModelConditionI o)
			throws Exception {
		for (OtherModelCondition e : o.getOtherModelConditions()) {
			Logic logic = e.getLogic();
			Long otherModelConditionId = e.getOtherModelConditionId();
			if (!util
					.put(getOtherModelCoditionidtoid(),
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
		for (TenyuReferenceModelI<? extends ModelI> e : o.getManual()) {
			if (!util.put(getManualtoid(), cnvBA(e.getStoreKeyReferenced()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Locale l : o.getLocaleConditions()) {
			if (!util.put(getLocaleConditiontoid(),
					cnvS(IndividualityObjectI.getLocaleStrStatic(l)),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Long userId : o.getUserIds()) {
			if (!util.put(getRegistererUseridtoid(), cnvL(userId),
					cnvL(o.getId()))) {
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
		for (OtherModelCondition e : o.getOtherModelConditions()) {
			Logic logic = e.getLogic();
			Long otherConditionId = e.getOtherModelConditionId();
			if (!util
					.deleteDupSingle(getOtherModelCoditionidtoid(),
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
		for (TenyuReferenceModelI<? extends ModelI> e : o.getManual()) {
			if (!util.deleteDupSingle(getManualtoid(), cnvBA(e.getStoreKeyReferenced()),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Locale l : o.getLocaleConditions()) {
			if (!util.deleteDupSingle(getLocaleConditiontoid(),
					cnvS(IndividualityObjectI.getLocaleStrStatic(l)),
					cnvL(o.getId()))) {
				return false;
			}
		}
		for (Long userId : o.getUserIds()) {
			if (!util.deleteDupSingle(getRegistererUseridtoid(), cnvL(userId),
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

	public boolean existByLocale(String locale, Long id) {
		if (locale == null || id == null)
			return false;
		return util.getDupSingle(getLocaleConditiontoid(), cnvS(locale),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByManual(byte[] manualStoreKey, Long id) {
		if (manualStoreKey == null || id == null)
			return false;
		return util.getDupSingle(getManualtoid(), cnvBA(manualStoreKey),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByModelName(String modelName, Long id) {
		if (modelName == null || id == null)
			return false;
		return util.getDupSingle(getModelnametoid(), cnvS(modelName), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public boolean existByOtherConditionId(byte[] otherConditionStoreKey,
			Long id) {
		if (otherConditionStoreKey == null || id == null)
			return false;
		return util.getDupSingle(getOtherModelCoditionidtoid(),
				cnvBA(otherConditionStoreKey), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public boolean existByStartSocialityId(Long startSocialityId, Long id) {
		if (startSocialityId == null || id == null)
			return false;
		return util.getDupSingle(getStartsocialityidtoid(),
				cnvL(startSocialityId), cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByUserId(Long userId, Long id) {
		if (userId == null || id == null)
			return false;
		return util.getDupSingle(getRegistererUseridtoid(), cnvL(userId),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(ModelConditionI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (OtherModelCondition e : o.getOtherModelConditions()) {
			Logic l = e.getLogic();
			Long id = e.getOtherModelConditionId();
			if (!existByOtherConditionId(
					ModelConditionI.getModelConditionStoreKey(l, id),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.OTHER_MODEL_CONDITION_ID,
						Lang.ERROR_DB_NOTFOUND,
						"logic=" + l + " otherModelConditionId=" + id);
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

		for (TenyuReferenceModelI<? extends ModelI> e : o.getManual()) {
			if (!existByManual(e.getStoreKeyReferenced(), o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_CONDITION_MANUAL,
						Lang.ERROR_DB_NOTFOUND, "manual=" + e);
				b = false;
				break;
			}
		}

		for (Locale l : o.getLocaleConditions()) {
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
	public String getName() {
		return modelName;
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(otherModelCoditionIdToId);
		r.add(modelNameToId);
		r.add(manualToId);
		r.add(startSocialityIdToId);
		r.add(localeConditionToId);
		r.add(registererUserIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof ModelConditionI)
			return true;
		return false;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(ModelConditionI o,
			ValidationResult vr) throws Exception {
		boolean b = true;

		for (OtherModelCondition e : o.getOtherModelConditions()) {
			Logic l = e.getLogic();
			Long id = e.getOtherModelConditionId();
			if (existByOtherConditionId(
					ModelConditionI.getModelConditionStoreKey(l, id),
					o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.OTHER_MODEL_CONDITION_ID,
						Lang.ERROR_DB_EXIST,
						"logic=" + l + " otherModelConditionId=" + id);
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

		for (TenyuReferenceModelI<? extends ModelI> e : o.getManual()) {
			if (existByManual(e.getStoreKeyReferenced(), o.getId())) {
				vr.add(Lang.MODEL_CONDITION, Lang.MODEL_CONDITION_MANUAL,
						Lang.ERROR_DB_EXIST, "manual=" + e);
				b = false;
				break;
			}
		}

		for (Locale l : o.getLocaleConditions()) {
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

		if (!updateCollectionSubIndex(getOtherModelCoditionidtoid(),
				old.getId(), updated.getId(),
				() -> updated.getOtherModelConditions(),
				() -> old.getOtherModelConditions(),
				(k) -> cnvBA(ModelConditionI.getModelConditionStoreKey(
						k.getLogic(), k.getOtherModelConditionId()))))
			return false;

		if (!updateCollectionSubIndex(getManualtoid(), old.getId(),
				updated.getId(), () -> updated.getManual(),
				() -> old.getManual(), (k) -> cnvBA(k.getStoreKeyReferenced())))
			return false;

		if (!updateCollectionSubIndex(getLocaleConditiontoid(), old.getId(),
				updated.getId(), () -> updated.getLocaleConditions(),
				() -> old.getLocaleConditions(),
				(k) -> cnvS(IndividualityObjectI.getLocaleStrStatic(k))))
			return false;

		if (!updateCollectionSubIndex(getRegistererUseridtoid(), old.getId(),
				updated.getId(), () -> updated.getUserIds(),
				() -> old.getUserIds(), (k) -> cnvL(k)))
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
}
