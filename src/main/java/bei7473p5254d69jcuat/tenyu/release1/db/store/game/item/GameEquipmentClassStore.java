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

public class GameEquipmentClassStore
		extends GameItemClassStore<GameEquipmentClassDBI, GameEquipmentClass> {
	public static final String modelName = GameEquipmentClass.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static GameEquipmentClass getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<GameEquipmentClassStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new GameEquipmentClassStore(txn)));
	}
	*/

	public GameEquipmentClassStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected GameEquipmentClass chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof GameEquipmentClass)
				return (GameEquipmentClass) o;
			throw new InvalidTargetObjectTypeException(
					"not GameEquipmentClass object in GameEquipmentClassStore");
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
		return o instanceof GameEquipmentClassDBI;
	}
}
