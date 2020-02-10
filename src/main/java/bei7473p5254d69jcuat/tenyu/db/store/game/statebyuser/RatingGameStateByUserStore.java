package bei7473p5254d69jcuat.tenyu.db.store.game.statebyuser;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.statebyuser.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RatingGameStateByUserStore
		extends AdministratedObjectStore<RatingGameStateByUserDBI,
				RatingGameStateByUser> {
	public static final String modelName = RatingGameStateByUser.class
			.getSimpleName();

	/**
	 *
	 * @param ownerUserId
	 * @return
	 */
	/*
	public static RatingGameStateByUser getByRegistererSimple(Long ownerUserId,
			Long gameId) {
		return simple(s -> s.getByGameIdUserId(gameId, ownerUserId));
	}

	public getOrCreate(Long gameId, Long userId) {
		if(existByGameIdUserId(gameId,userId)) {

		}else {
			//作成
		}
	}
	*/

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	protected static Long getGameId(byte[] gameIdUserId) {
		return Glb.getUtil().getLong1(gameIdUserId);
	}

	protected static byte[] getGameIdBA(Long gameId) {
		return Glb.getUtil().toByteArray(gameId);
	}

	protected static byte[] getGameIdUserIdBA(RatingGameStateByUserDBI o) {
		return getGameIdUserIdBA(o.getRatingGameId(), o.getOwnerUserId());
	}

	protected static byte[] getGameIdUserIdBA(Long gameId, Long userId) {
		return Glb.getUtil().concat(gameId, userId);
	}

	protected static Long getUserId(byte[] gameIdUserId) {
		return Glb.getUtil().getLong2(gameIdUserId);
	}

	/**
	 * ゲームIDとユーザーIDを連結させたbyte[]からユーザー別ゲーム状態IDへ
	 */
	protected StoreInfo getGameIdUserIdToIdStore() {
		return gameIdUserIdToId;
	}

	/**
	 * 状態の所有ユーザーのIDからユーザー別ゲーム状態IDへ。重複キー
	 * @return
	 */
	protected StoreInfo getUserIdToIdStore() {
		return userIdToId;
	}

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

	public RatingGameStateByUser getByGameIdUserId(Long gameId, Long userId) {
		Long id = getIdByGameIdUserId(gameId, userId);
		if (id == null)
			return null;
		return get(id);
	}

	public boolean existByGameIdUserId(Long gameId, Long userId, Long id) {
		if (gameId == null || userId == null || id == null)
			return false;
		Long existId = getIdByGameIdUserId(gameId, userId);
		if (existId == null)
			return false;
		return id.equals(existId);
	}

	public boolean existByGameIdUserId(RatingGameStateByUserDBI o) {
		return existByGameIdUserId(o.getRatingGameId(), o.getOwnerUserId(),
				o.getId());
	}

	public boolean existByUserId(Long userId, Long rId) {
		if (userId == null || rId == null)
			return false;

		return util.getDupSingle(getUserIdToIdStore(), cnvL(userId), cnvL(rId),
				(bi) -> cnvL(bi)) != null;
	}

	private StoreInfo gameIdUserIdToId;
	private StoreInfo userIdToId;

	public RatingGameStateByUserStore(Transaction txn) {
		super(txn);
		userIdToId = new StoreInfo(getName() + "_userIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
		gameIdUserIdToId = new StoreInfo(getName() + "_gameIdUserIdToId");
	}

	@Override
	protected RatingGameStateByUser chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof RatingGameStateByUser)
				return (RatingGameStateByUser) o;
			throw new InvalidTargetObjectTypeException(
					"not RatingGameStateByUser object in RatingGameStateByUserStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean existAdministratedObjectConcrete(
			RatingGameStateByUserDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByGameIdUserId(o)) {
			vr.add(Lang.RATINGGAME_STATEBYUSER_ID_BY_RATINGGAME_ID_AND_USERID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existByUserId(o.getOwnerUserId(), o.getId())) {
			vr.add(Lang.RATINGGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		return b;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getGameIdUserIdToIdStore());
		r.add(getUserIdToIdStore());
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RatingGameDBI;
	}

	@Override
	protected boolean noExistAdministratedObjectConcrete(
			RatingGameStateByUserDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByGameIdUserId(o)) {
			vr.add(Lang.RATINGGAME_STATEBYUSER_ID_BY_RATINGGAME_ID_AND_USERID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (existByUserId(o.getOwnerUserId(), o.getId())) {
			vr.add(Lang.RATINGGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean createAdministratedObjectConcrete(
			RatingGameStateByUserDBI o) throws Exception {
		if (!util.put(getGameIdUserIdToIdStore(), cnvBA(getGameIdUserIdBA(o)),
				cnvL(o.getId()))) {
			return false;
		}
		if (!util.put(getUserIdToIdStore(), cnvL(o.getOwnerUserId()),
				cnvL(o.getId()))) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			RatingGameStateByUserDBI updated, RatingGameStateByUserDBI old,
			ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getRatingGameId();
		Long oldGameId = old.getRatingGameId();
		Long updatedUserId = updated.getOwnerUserId();
		Long oldUserId = old.getOwnerUserId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)
				|| Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByGameIdUserId(updated)) {
				r.add(Lang.RATINGGAME_STATEBYUSER_ID_BY_RATINGGAME_ID_AND_USERID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		if (Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByUserId(updatedUserId, updated.getId())) {
				r.add(Lang.RATINGGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(
			RatingGameStateByUserDBI o) throws Exception {
		if (!util.remove(getGameIdUserIdToIdStore(),
				cnvBA(getGameIdUserIdBA(o))))
			return false;
		if (!util.deleteDupSingle(getUserIdToIdStore(),
				cnvL(o.getOwnerUserId()), cnvL(o.getId())))
			return false;

		return true;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(
			RatingGameStateByUserDBI updated, RatingGameStateByUserDBI old)
			throws Exception {
		Long updatedGameId = updated.getRatingGameId();
		Long oldGameId = old.getRatingGameId();
		Long rId = old.getId();
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
