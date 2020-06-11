package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.item;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.item.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class StaticGameMaterialClassStore
		extends IndividualityObjectStore<StaticGameMaterialClassI,
				StaticGameMaterialClass> {
	public static final String modelName = StaticGameMaterialClass.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	private final StoreInfo gameIdToId;

	public StaticGameMaterialClassStore(Transaction txn) {
		super(txn);
		gameIdToId = new StoreInfo(getName() + "gameIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	@Override
	protected StaticGameMaterialClass chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof StaticGameMaterialClass)
				return (StaticGameMaterialClass) o;
			throw new InvalidTargetObjectTypeException(
					"not StaticGameMaterialClass object in StaticGameMaterialClassStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(
			StaticGameMaterialClassI o) throws Exception {
		if (!util.put(getGameIdToIdStore(), cnvL(o.getStaticGameId()),
				cnvL(o.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			StaticGameMaterialClassI updated, StaticGameMaterialClassI old,
			ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getStaticGameId();
		Long oldGameId = old.getStaticGameId();
		Long rId = old.getId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)) {
			if (existByGameId(updatedGameId, rId)) {
				r.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(
			StaticGameMaterialClassI o) throws Exception {
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
			StaticGameMaterialClassI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existByGameId(o.getStaticGameId(), o.getId())) {
			vr.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
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
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getGameIdToIdStore());
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof StaticGameMaterialClassI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(
			StaticGameMaterialClassI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (existByGameId(o.getStaticGameId(), o.getId())) {
			vr.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(
			StaticGameMaterialClassI updated, StaticGameMaterialClassI old)
			throws Exception {
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
