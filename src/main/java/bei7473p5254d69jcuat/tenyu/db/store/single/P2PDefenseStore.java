package bei7473p5254d69jcuat.tenyu.db.store.single;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class P2PDefenseStore extends IdObjectStore<P2PDefenseI, P2PDefense>
		implements SingleObjectStore {
	public static final String modelName = P2PDefense.class.getSimpleName();

	public P2PDefenseStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean needCatchUp() {
		return false;
	}

	@Override
	public String getName() {
		return modelName;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	protected P2PDefense chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof P2PDefense)
				return (P2PDefense) o;
			throw new InvalidTargetObjectTypeException(
					"not P2PDefense object in P2PDefenseStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.communication.P2PDefense)
			return true;
		return false;
	}

	@Override
	protected final List<StoreInfo> getStoresIdObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createIdObjectConcrete(P2PDefenseI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIdObjectConcrete(
			P2PDefenseI updated, P2PDefenseI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIdObjectConcrete(P2PDefenseI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIdObjectConcrete(P2PDefenseI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean noExistIdObjectConcrete(P2PDefenseI o,
			ValidationResult r) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIdObjectConcrete(P2PDefenseI updated,
			P2PDefenseI old) throws Exception {
		return true;
	}

}
