package bei7473p5254d69jcuat.tenyu.release1.db.store.game.item;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class GameMaterialClassStore
		extends GameItemClassStore<GameMaterialClassDBI, GameMaterialClass> {
	public static final String modelName = GameMaterialClass.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static GameMaterialClass getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<GameMaterialClassStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new GameMaterialClassStore(txn)));
	}
	*/

	public GameMaterialClassStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected GameMaterialClass chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof GameMaterialClass)
				return (GameMaterialClass) o;
			throw new InvalidTargetObjectTypeException(
					"not GameMaterialClass object in GameMaterialClassStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresGameItemClassConcrete() {
		return new ArrayList<>();
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof GameMaterialClassDBI;
	}

}
