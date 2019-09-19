package bei7473p5254d69jcuat.tenyu.release1.db.store.game.statebyuser;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.GameStateByUser.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RatingGameStateByUserStore extends
		GameStateByUserStore<RatingGameStateByUserDBI, RatingGameStateByUser> {
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
/*
	public static RatingGameStateByUser getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<RatingGameStateByUserStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new RatingGameStateByUserStore(txn)));
	}
*/
	public RatingGameStateByUserStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
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
	protected boolean existGameStateByUserConcrete(RatingGameStateByUserDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected String getGameType() {
		return GameType.RATINGGAME.toString();
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresGameStateByUserConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RatingGameDBI;
	}

	@Override
	protected boolean noExistGameStateByUserConcrete(RatingGameStateByUserDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}
}
