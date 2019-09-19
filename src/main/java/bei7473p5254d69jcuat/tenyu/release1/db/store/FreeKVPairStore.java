package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class FreeKVPairStore
		extends ObjectivityObjectStore<FreeKVPairDBI, FreeKVPair> {
	public static final String modelName = FreeKVPair.class.getSimpleName();
	/**
	 * FreeKVPairのKeyからidへのインデックス。
	 * TODO：このようにインデックスのためのストアのせいでDBサイズが肥大化することが悩み。
	 * しかしこのストアは用途不明であり、肥大化する予定はないし、問題無い。
	 */
	private static final StoreInfo keyToId = new StoreInfo(
			modelName + "_keyToId");

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public FreeKVPairStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected FreeKVPair chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof FreeKVPair)
				return (FreeKVPair) o;
			throw new InvalidTargetObjectTypeException(
					"not FreeKVS object in FreeKVSStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(FreeKVPairDBI u)
			throws Exception {
		return util.put(keyToId, cnvS(u.getKey()), cnvL(u.getRecycleId()));
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			FreeKVPairDBI updated, FreeKVPairDBI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getKey(), old.getKey())) {
			if (getIdByKey(updated.getKey()) != null) {
				r.add(Lang.FREEKVS_KEY, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(FreeKVPairDBI u)
			throws Exception {
		return util.remove(keyToId, cnvS(u.getKey()));
	}

	@Override
	public boolean existObjectivityObjectConcrete(FreeKVPairDBI u,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByKey(u.getKey()) == null) {
			vr.add(Lang.FREEKVS_KEY, Lang.ERROR_DB_NOTFOUND,
					Lang.FREEKVS_KEY + "=" + u.getKey());
			b = false;
		}
		return b;
	}

	public Long getIdByKey(String key) {
		return getId(keyToId, cnvS(key));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(keyToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.FreeKVPairDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistObjectivityObjectConcrete(FreeKVPairDBI u,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByKey(u.getKey()) != null) {
			vr.add(Lang.FREEKVS_KEY, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(FreeKVPairDBI updated,
			FreeKVPairDBI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getKey(), old.getKey())) {
			if (old.getKey() != null) {
				if (!util.remove(keyToId, cnvS(old.getKey())))
					return false;
			}
			if (!util.put(keyToId, cnvS(updated.getKey()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return true;
	}

}
