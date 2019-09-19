package bei7473p5254d69jcuat.tenyu.release1.db.store.game.statebyuser;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import jetbrains.exodus.env.*;

public abstract class GameStateByUserStore<T1 extends GameStateByUserDBI,
		T2 extends T1> extends ObjectivityObjectStore<T1, T2> {

	protected static Long getGameId(byte[] gameIdUserId) {
		return Glb.getUtil().getLong1(gameIdUserId);
	}

	protected static byte[] getGameIdBA(Long gameId) {
		return Glb.getUtil().toByteArray(gameId);
	}

	protected static byte[] getGameIdUserIdBA(GameStateByUserDBI o) {
		return getGameIdUserIdBA(o.getGameRef().getGameId(),
				o.getOwnerUserId());
	}

	protected static byte[] getGameIdUserIdBA(Long gameId, Long userId) {
		return Glb.getUtil().concat(gameId, userId);
	}

	protected static Long getUserId(byte[] gameIdUserId) {
		return Glb.getUtil().getLong2(gameIdUserId);
	}

	protected GameStateByUserStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected boolean createObjectivityObjectConcrete(T1 o) throws Exception {
		if (!util.put(getGameIdUserIdToIdStore(), cnvBA(getGameIdUserIdBA(o)),
				cnvL(o.getRecycleId()))) {
			return false;
		}
		if (!util.put(getUserIdToIdStore(), cnvL(o.getOwnerUserId()),
				cnvL(o.getRecycleId()))) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getGameRef().getGameId();
		Long oldGameId = old.getGameRef().getGameId();
		Long updatedUserId = updated.getOwnerUserId();
		Long oldUserId = old.getOwnerUserId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)
				|| Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByGameIdUserId(updated)) {
				r.add(Lang.GAMESTATEBYUSER_GAMEID_AND_USERID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		if (Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByUserId(updatedUserId, updated.getRecycleId())) {
				r.add(Lang.GAMESTATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(T1 o) throws Exception {
		if (!util.remove(getGameIdUserIdToIdStore(),
				cnvBA(getGameIdUserIdBA(o))))
			return false;
		if (!util.deleteDupSingle(getUserIdToIdStore(),
				cnvL(o.getOwnerUserId()), cnvL(o.getRecycleId())))
			return false;

		return true;
	}

	public boolean existByGameIdUserId(Long gameId, Long userId, Long rId) {
		if (gameId == null || userId == null || rId == null)
			return false;
		Long id = getIdByGameIdUserId(gameId, userId);
		return rId.equals(id);
	}

	public boolean existByGameIdUserId(T1 o) {
		return existByGameIdUserId(o.getGameRef().getGameId(),
				o.getOwnerUserId(), o.getRecycleId());
	}

	public boolean existByUserId(Long userId, Long rId) {
		if (userId == null || rId == null)
			return false;

		return util.getDupSingle(getUserIdToIdStore(), cnvL(userId), cnvL(rId),
				(bi) -> cnvL(bi)) != null;
	}

	abstract protected boolean existGameStateByUserConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected boolean existObjectivityObjectConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existByGameIdUserId(o)) {
			vr.add(Lang.GAMESTATEBYUSER_ID_BY_GAMEID_AND_USERID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existByUserId(o.getOwnerUserId(), o.getRecycleId())) {
			vr.add(Lang.GAMESTATEBYUSER_USER_ID, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existGameStateByUserConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	public T2 getByGameIdUserId(Long gameId, Long userId) {
		Long id = getIdByGameIdUserId(gameId, userId);
		if (id == null)
			return null;
		return get(id);
	}

	/**
	 * ゲームIDとユーザーIDを連結させたbyte[]からユーザー別ゲーム状態IDへ
	 */
	protected StoreInfo getGameIdUserIdToIdStore() {
		return new StoreInfo(getName() + "_gameIdUserIdToId");
	}

	abstract protected String getGameType();

	public Long getIdByGameIdUserId(Long gameId, Long userId) {
		try {
			return cnvL(util.get(getGameIdUserIdToIdStore(),
					cnvBA(getGameIdUserIdBA(gameId, userId))));
		} catch (Exception e) {
			return null;
		}
	}

	public List<Long> getIdsByUserId(Long userId) {
		return util.getDup(getUserIdToIdStore(), cnvL(userId),
				(bi) -> cnvL(bi));
	}

	public List<Long> getIdsByGameId(Long gameId) {
		try {
			return util.getValuesByKeyPrefix(getGameIdUserIdToIdStore(),
					cnvBA(getGameIdBA(gameId)), bi -> cnvL(bi), -1);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	abstract public List<StoreInfo> getStoresGameStateByUserConcrete();

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getGameIdUserIdToIdStore());
		r.add(getUserIdToIdStore());
		r.addAll(getStoresGameStateByUserConcrete());
		return r;
	}

	/**
	 * 状態の所有ユーザーのIDからユーザー別ゲーム状態IDへ。重複キー
	 * @return
	 */
	protected StoreInfo getUserIdToIdStore() {
		return new StoreInfo(getName() + "_userIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	abstract protected boolean noExistGameStateByUserConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected boolean noExistObjectivityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByGameIdUserId(o)) {
			vr.add(Lang.GAMESTATEBYUSER_ID_BY_GAMEID_AND_USERID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (existByUserId(o.getOwnerUserId(), o.getRecycleId())) {
			vr.add(Lang.GAMESTATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (!noExistGameStateByUserConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(T1 updated, T1 old)
			throws Exception {
		Long updatedGameId = updated.getGameRef().getGameId();
		Long oldGameId = old.getGameRef().getGameId();
		Long rId = old.getRecycleId();
		Long updatedUserId = updated.getOwnerUserId();
		Long oldUserId = old.getOwnerUserId();
		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)
				|| Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			Glb.debug("updatedGameId=" + updatedGameId + " oldGameId="
					+ oldGameId + " rId=" + rId);
			Glb.debug("updatedUserId=" + updatedUserId + " oldUserId="
					+ oldUserId);
			if (!util.remove(getGameIdUserIdToIdStore(),
					cnvBA(getGameIdUserIdBA(old))))
				return false;
			if (!util.put(getGameIdUserIdToIdStore(),
					cnvBA(getGameIdUserIdBA(updated)), cnvL(rId)))
				return false;
		}
		if (Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			Glb.debug("updatedUserId=" + updatedUserId + " oldUserId="
					+ oldUserId);

			if (oldUserId != null) {
				if (!util.deleteDupSingle(getUserIdToIdStore(), cnvL(oldUserId),
						cnvL(rId)))
					return false;
			}
			if (!util.put(getUserIdToIdStore(), cnvL(updatedUserId), cnvL(rId)))
				return false;
		}
		return true;
	}

}
