package bei7473p5254d69jcuat.tenyu.release1.db.store.game.item;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import jetbrains.exodus.env.*;

public abstract class GameItemClassStore<T1 extends GameItemClassDBI,
		T2 extends T1> extends NaturalityStore<T1, T2> {

	protected GameItemClassStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected boolean createNaturalityConcrete(T1 o) throws Exception {
		if (!util.put(getGameIdToIdStore(), cnvL(o.getGameRef().getGameId()),
				cnvL(o.getRecycleId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(T1 updated, T1 old,
			ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getGameRef().getGameId();
		Long oldGameId = old.getGameRef().getGameId();
		Long rId = old.getRecycleId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)) {
			if (existByGameId(updatedGameId, rId)) {
				r.add(Lang.GAMEITEMCLASS_GAMEREF, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteNaturalityConcrete(T1 o) throws Exception {
		if (!util.deleteDupSingle(getGameIdToIdStore(),
				cnvL(o.getGameRef().getGameId()), cnvL(o.getRecycleId())))
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
	protected boolean existNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existByGameId(o.getGameRef().getGameId(), o.getRecycleId())) {
			vr.add(Lang.GAMEITEMCLASS_GAMEREF, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	/**
	 * ゲームIDからGameItemClassのIDへ。重複キー
	 */
	protected StoreInfo getGameIdToIdStore() {
		return new StoreInfo(getName() + "gameIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	public List<Long> getIdsByGameId(Long gameId) {
		return util.getDup(getGameIdToIdStore(), cnvL(gameId),
				(bi) -> cnvL(bi));
	}

	abstract public List<StoreInfo> getStoresGameItemClassConcrete();

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getGameIdToIdStore());
		r.addAll(getStoresGameItemClassConcrete());
		return r;
	}

	@Override
	protected boolean noExistNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (existByGameId(o.getGameRef().getGameId(), o.getRecycleId())) {
			vr.add(Lang.GAMEITEMCLASS_GAMEREF, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateNaturalityConcrete(T1 updated, T1 old)
			throws Exception {
		Long updatedGameId = updated.getGameRef().getGameId();
		Long oldGameId = old.getGameRef().getGameId();
		Long rId = old.getRecycleId();
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
