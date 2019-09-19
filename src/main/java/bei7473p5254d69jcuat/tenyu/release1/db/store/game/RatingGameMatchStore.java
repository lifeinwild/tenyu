package bei7473p5254d69jcuat.tenyu.release1.db.store.game;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * レーティングゲームの試合情報
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameMatchStore
		extends ObjectivityObjectStore<RatingGameMatchDBI, RatingGameMatch> {
	public static final String modelName = RatingGameMatch.class
			.getSimpleName();
	private static final StoreInfo ratingGameIdToId = new StoreInfo(
			modelName + "_ratingGameIdToId_Dup", StoreConfig.WITH_DUPLICATES);
	//playerUserIdのサブインデックスは性能的問題がありそうなのでやめた
	//	private static final StoreInfo playerUserIdToId = new StoreInfo(
	//			name + "_userIdToId_Dup", StoreConfig.WITH_DUPLICATES, true);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}
/*
	public static RatingGameMatch getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<RatingGameMatchStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new RatingGameMatchStore(txn)));
	}
*/
	public RatingGameMatchStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected RatingGameMatch chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof RatingGameMatch)
				return (RatingGameMatch) o;
			throw new InvalidTargetObjectTypeException(
					"not RatingGameMatch object in RatingGameMatchStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(RatingGameMatchDBI o)
			throws Exception {
		if (!util.put(ratingGameIdToId, cnvL(o.getRatingGameId()),
				cnvL(o.getRecycleId())))
			return false;
		/*
		for (Long playerUserId : o.getPlayers()) {
			if (!util.put(playerUserIdToId, cnvL(playerUserId),
					cnvL(o.getRecycleId())))
				return false;
		}
		*/
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			RatingGameMatchDBI updated, RatingGameMatchDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getRatingGameId(),
				old.getRatingGameId())) {
			r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_UNALTERABLE);
			b = false;
		}
		/*
		if (Glb.getUtil().notEqual(updated.getPlayers(), old.getPlayers())) {
			r.add(Lang.RATINGGAME_MATCH_PLAYERUSERIDS, Lang.ERROR_UNALTERABLE);
		}
		*/

		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(RatingGameMatchDBI o)
			throws Exception {
		if (!util.deleteDupSingle(ratingGameIdToId, cnvL(o.getRatingGameId()),
				cnvL(o.getRecycleId())))
			return false;
		/*
		for (Long playerUserId : o.getPlayers()) {
			if (!util.deleteDupSingle(playerUserIdToId, cnvL(playerUserId),
					cnvL(o.getRecycleId())))
				return false;
		}		*/

		return true;
	}

	public boolean existByRatingGameId(Long ratingGameId, Long id) {
		if (ratingGameId == null || id == null)
			return false;
		return util.getDupSingle(ratingGameIdToId, cnvL(ratingGameId), cnvL(id),
				(bi) -> cnvL(bi)) != null;
	}
	/*
		public boolean existByPlayerUserId(Long playerUserId, Long id) {
			if (playerUserId == null || id == null)
				return false;
			return util.getDupSingle(playerUserIdToId, cnvL(playerUserId), cnvL(id),
					(bi) -> cnvL(bi)) != null;
		}
			*/

	@Override
	protected boolean existObjectivityObjectConcrete(RatingGameMatchDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByRatingGameId(o.getRatingGameId(), o.getRecycleId())) {
			vr.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.RATINGGAME_MATCH_RATINGGAME_ID + "="
							+ o.getRatingGameId());
			b = false;
		}
		/*
		for (Long playerUserId : o.getPlayers()) {
			if (!existByPlayerUserId(playerUserId, o.getRecycleId())) {
				vr.add(Lang.RATINGGAME_MATCH_PLAYERUSERID,
						Lang.ERROR_DB_NOTFOUND,
						Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
								+ Lang.RATINGGAME_MATCH_PLAYERUSERIDS + "="
								+ playerUserId);
				break;
			}
		}		*/

		return b;
	}

	public List<Long> getIdsByRatingGameId(Long ratingGameId) {
		return util.getDup(ratingGameIdToId, cnvL(ratingGameId), v -> cnvL(v));
	}
	/*
		public List<Long> getIdsByPlayerUserId(Long playerUserId) {
			return util.getDup(playerUserIdToId, cnvL(playerUserId), v -> cnvL(v));
		}
			*/

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(ratingGameIdToId);
		//	r.add(playerUserIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RatingGameMatchDBI;
	}

	@Override
	protected boolean noExistObjectivityObjectConcrete(RatingGameMatchDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByRatingGameId(o.getRatingGameId(), o.getRecycleId())) {
			vr.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_DB_EXIST,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.RATINGGAME_MATCH_RATINGGAME_ID + "="
							+ o.getRatingGameId());
			b = false;
		}
		/*
		for (Long playerUserId : o.getPlayers()) {
			if (existByPlayerUserId(playerUserId, o.getRecycleId())) {
				vr.add(Lang.RATINGGAME_MATCH_PLAYERUSERID, Lang.ERROR_DB_EXIST,
						Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
								+ Lang.RATINGGAME_MATCH_PLAYERUSERIDS + "="
								+ playerUserId);
				break;
			}
		}
		*/

		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(
			RatingGameMatchDBI updated, RatingGameMatchDBI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getRatingGameId(),
				old.getRatingGameId())) {
			if (old.getRatingGameId() != null) {
				if (!util.deleteDupSingle(ratingGameIdToId,
						cnvL(old.getRatingGameId()), cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(ratingGameIdToId, cnvL(updated.getRatingGameId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		/*
				HashSet<Long> updatedPlayers = updated.getPlayers();
				HashSet<Long> oldPlayers = old.getPlayers();
				if (Glb.getUtil().notEqual(updatedPlayers, oldPlayers)) {
					//追加されたプレイヤー一覧
					List<Long> add = Glb.getUtil().getExtra(updatedPlayers,
							new HashSet<>(oldPlayers));
					//削除されたプレイヤー一覧
					List<Long> removed = Glb.getUtil().getExtra(oldPlayers,
							new HashSet<>(updatedPlayers));
					//削除されたプレイヤーのサブインデックスを削除
					for (Long playerUserId : removed) {
						if (!util.deleteDupSingle(playerUserIdToId, cnvL(playerUserId),
								cnvL(old.getRecycleId())))
							throw new IOException("Failed to updateSub");
					}
					//追加されたプレイヤーのサブインデックスを作成
					for (Long playerUserId : add) {
						if (!util.put(playerUserIdToId, cnvL(playerUserId),
								cnvL(updated.getRecycleId())))
							throw new IOException("Failed to updateSub");
					}
				}
				*/

		return true;
	}
}
