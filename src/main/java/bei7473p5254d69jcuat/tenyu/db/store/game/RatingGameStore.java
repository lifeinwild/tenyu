package bei7473p5254d69jcuat.tenyu.db.store.game;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RatingGameStore
		extends IndividualityObjectStore<RatingGameI, RatingGame> {
	public static final String modelName = RatingGame.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/**
	 * ランダムにいくつか取得。BANされたゲームを除く。
	 * GUIからの利用を想定
	 * @param max
	 * @return
	 */
	public List<RatingGame> getRandomWithoutBan(int max) {
		try {
			List<RatingGame> r = getRandom(max);
			SocialityStore ss = new SocialityStore(util.getTxn());
			for (RatingGame o : r) {
				if (ss.isBan(NodeType.RATINGGAME, o.getId()))
					r.remove(o);
			}
			return r;
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	/*
	public static RatingGame getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<RatingGameStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new RatingGameStore(txn)));
	}
	*/

	public RatingGameStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected RatingGame chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof RatingGame)
				return (RatingGame) o;
			throw new InvalidTargetObjectTypeException(
					"not RatingGame object in RatingGameStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(RatingGameI game)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			RatingGameI updated, RatingGameI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(RatingGameI game)
			throws Exception {
		return true;
	}

	@Override
	public boolean existIndividualityObjectConcrete(RatingGameI game,
			ValidationResult vr) {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.RatingGameI)
			return true;
		return false;
	}

	@Override
	public boolean noExistIndividualityObjectConcrete(RatingGameI game,
			ValidationResult vr) {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(RatingGameI updated,
			RatingGameI old) throws Exception {
		return true;
	}

}
