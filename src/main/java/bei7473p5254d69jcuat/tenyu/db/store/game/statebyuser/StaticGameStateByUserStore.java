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

public class StaticGameStateByUserStore
		extends AdministratedObjectStore<StaticGameStateByUserI,
				StaticGameStateByUser> {
	public static final String modelName = StaticGameStateByUser.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	protected static Long getGameId(byte[] gameIdUserId) {
		return Glb.getUtil().getLong1(gameIdUserId);
	}

	protected static byte[] getGameIdBA(Long gameId) {
		return Glb.getUtil().toByteArray(gameId);
	}

	protected static byte[] getGameIdUserIdBA(StaticGameStateByUserI o) {
		return getGameIdUserIdBA(o.getStaticGameId(), o.getOwnerUserId());
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

	public StaticGameStateByUser getByGameIdUserId(Long gameId, Long userId) {
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

	public boolean existByGameIdUserId(StaticGameStateByUserI o) {
		return existByGameIdUserId(o.getStaticGameId(), o.getOwnerUserId(),
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

	public StaticGameStateByUserStore(Transaction txn) {
		super(txn);
		userIdToId = new StoreInfo(getName() + "_userIdToId_Dup",
				StoreConfig.WITH_DUPLICATES);
		gameIdUserIdToId = new StoreInfo(getName() + "_gameIdUserIdToId");
	}

	@Override
	protected StaticGameStateByUser chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof StaticGameStateByUser)
				return (StaticGameStateByUser) o;
			throw new InvalidTargetObjectTypeException(
					"not StaticGameStateByUser object in StaticGameStateByUserStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean existAdministratedObjectConcrete(
			StaticGameStateByUserI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByGameIdUserId(o)) {
			vr.add(Lang.STATICGAME_STATEBYUSER_ID_BY_STATICGAME_ID_AND_USERID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existByUserId(o.getOwnerUserId(), o.getId())) {
			vr.add(Lang.STATICGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_NOTFOUND,
					"ownerUserId=" + o.getOwnerUserId());
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
		return o instanceof StaticGameI;
	}

	@Override
	protected boolean noExistAdministratedObjectConcrete(
			StaticGameStateByUserI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByGameIdUserId(o)) {
			vr.add(Lang.STATICGAME_STATEBYUSER_ID_BY_STATICGAME_ID_AND_USERID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (existByUserId(o.getOwnerUserId(), o.getId())) {
			vr.add(Lang.STATICGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST,
					"ownerUserId=" + o.getOwnerUserId());
			b = false;
		}

		return b;
	}

	@Override
	protected boolean createAdministratedObjectConcrete(
			StaticGameStateByUserI o) throws Exception {
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
			StaticGameStateByUserI updated, StaticGameStateByUserI old,
			ValidationResult r) {
		boolean b = true;
		Long updatedGameId = updated.getStaticGameId();
		Long oldGameId = old.getStaticGameId();
		Long updatedUserId = updated.getOwnerUserId();
		Long oldUserId = old.getOwnerUserId();

		if (Glb.getUtil().notEqual(updatedGameId, oldGameId)
				|| Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByGameIdUserId(updated)) {
				r.add(Lang.STATICGAME_STATEBYUSER_ID_BY_STATICGAME_ID_AND_USERID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		if (Glb.getUtil().notEqual(updatedUserId, oldUserId)) {
			if (existByUserId(updatedUserId, updated.getId())) {
				r.add(Lang.STATICGAME_STATEBYUSER_USER_ID, Lang.ERROR_DB_EXIST,
						"ownerUserId=" + updated.getOwnerUserId());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(
			StaticGameStateByUserI o) throws Exception {
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
			StaticGameStateByUserI updated, StaticGameStateByUserI old)
			throws Exception {

		Long updatedGameId = updated.getStaticGameId();
		Long oldGameId = old.getStaticGameId();
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
