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

public class StaticGameStateByUserStore extends
		GameStateByUserStore<StaticGameStateByUserDBI, StaticGameStateByUser> {
	public static final String modelName = StaticGameStateByUser.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public StaticGameStateByUserStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
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
	protected boolean existGameStateByUserConcrete(StaticGameStateByUserDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected String getGameType() {
		return GameType.STATICGAME.toString();
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
		return o instanceof StaticGameDBI;
	}

	@Override
	protected boolean noExistGameStateByUserConcrete(StaticGameStateByUserDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}
}
