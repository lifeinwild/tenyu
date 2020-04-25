package bei7473p5254d69jcuat.tenyu.db.store.game.item;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class StaticGameEquipmentClassStore
		extends IndividualityObjectStore<StaticGameEquipmentClassI,
				StaticGameEquipmentClass> {
	public static final String modelName = StaticGameEquipmentClass.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	private final StoreInfo gameIdToId;

	public StaticGameEquipmentClassStore(Transaction txn) {
		super(txn);
		gameIdToId = new StoreInfo(getName() + "gameIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	@Override
	protected StaticGameEquipmentClass chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof StaticGameEquipmentClass)
				return (StaticGameEquipmentClass) o;
			throw new InvalidTargetObjectTypeException(
					"not GameEquipmentClass object in GameEquipmentClassStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(
			StaticGameEquipmentClassI o) throws Exception {
		if (!util.put(getGameIdToIdStore(), cnvL(o.getStaticGameId()),
				cnvL(o.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			StaticGameEquipmentClassI updated,
			StaticGameEquipmentClassI old, ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getStaticGameId();
		Long oldGameId = old.getStaticGameId();
		Long rId = old.getId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)) {
			if (existByGameId(updatedGameId, rId)) {
				r.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(
			StaticGameEquipmentClassI o) throws Exception {
		if (!util.deleteDupSingle(getGameIdToIdStore(),
				cnvL(o.getStaticGameId()), cnvL(o.getId())))
			return false;
		return true;
	}

	/**
	 * gameIdとrIdで結果は一意になる
	 * このメソッドは更新前の検証処理において必要になる。
	 *
	 * @param gameId
	 * @param rId		GameItemClassのid
	 * @return
	 */
	public boolean existByGameId(Long gameId, Long rId) {
		if (gameId == null || rId == null)
			return false;

		return util.getDupSingle(getGameIdToIdStore(), cnvL(gameId), cnvL(rId),
				(bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(
			StaticGameEquipmentClassI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existByGameId(o.getStaticGameId(), o.getId())) {
			vr.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	/**
	 * ゲームIDからGameItemClassのIDへ。重複キー
	 */
	protected StoreInfo getGameIdToIdStore() {
		return gameIdToId;
	}

	public List<Long> getIdsByGameId(Long gameId) {
		return util.getDup(getGameIdToIdStore(), cnvL(gameId),
				(bi) -> cnvL(bi));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getGameIdToIdStore());
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof StaticGameEquipmentClassI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(
			StaticGameEquipmentClassI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (existByGameId(o.getStaticGameId(), o.getId())) {
			vr.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(
			StaticGameEquipmentClassI updated,
			StaticGameEquipmentClassI old) throws Exception {
		Long updatedGameId = updated.getStaticGameId();
		Long oldGameId = old.getStaticGameId();
		Long rId = old.getId();
		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)) {
			if (oldGameId != null) {
				if (!util.deleteDupSingle(getGameIdToIdStore(), cnvL(oldGameId),
						cnvL(rId)))
					return false;
			}
			if (!util.put(getGameIdToIdStore(), cnvL(updatedGameId), cnvL(rId)))
				return false;
		}
		return true;
	}

}
