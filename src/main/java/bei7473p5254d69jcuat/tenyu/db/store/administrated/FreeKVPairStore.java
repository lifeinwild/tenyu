package bei7473p5254d69jcuat.tenyu.db.store.administrated;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class FreeKVPairStore
		extends AdministratedObjectStore<FreeKVPairI, FreeKVPair> {
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

	public FreeKVPairStore(Transaction txn) {
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
	protected boolean createAdministratedObjectConcrete(FreeKVPairI u)
			throws Exception {
		return util.put(keyToId, cnvS(u.getKey()), cnvL(u.getId()));
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			FreeKVPairI updated, FreeKVPairI old, ValidationResult r) {
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
	protected boolean deleteAdministratedObjectConcrete(FreeKVPairI u)
			throws Exception {
		return util.remove(keyToId, cnvS(u.getKey()));
	}

	@Override
	public boolean existAdministratedObjectConcrete(FreeKVPairI u,
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
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(keyToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.FreeKVPairI)
			return true;
		return false;
	}

	@Override
	public boolean noExistAdministratedObjectConcrete(FreeKVPairI u,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByKey(u.getKey()) != null) {
			vr.add(Lang.FREEKVS_KEY, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(FreeKVPairI updated,
			FreeKVPairI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getKey(), old.getKey())) {
			if (old.getKey() != null) {
				if (!util.remove(keyToId, cnvS(old.getKey())))
					return false;
			}
			if (!util.put(keyToId, cnvS(updated.getKey()),
					cnvL(updated.getId())))
				return false;
		}
		return true;
	}

}
