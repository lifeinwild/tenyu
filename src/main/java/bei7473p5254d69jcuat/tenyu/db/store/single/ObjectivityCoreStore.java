package bei7473p5254d69jcuat.tenyu.db.store.single;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class ObjectivityCoreStore
		extends IndividualityObjectStore<ObjectivityCoreDBI, ObjectivityCore>
		implements SingleObjectStore {

	private static transient ObjectivityCore cache;
	private static transient ByteIterable cacheBi;

	public ObjectivityCoreStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean needCatchUp() {
		return false;
	}

	public static final String modelName = ObjectivityCore.class
			.getSimpleName();

	@Override
	public String getName() {
		return modelName;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	protected ObjectivityCore chainversionup(ByteIterable bi) {
		//デシリアライズを省略するためキャッシュがあればそれを返す
		if (cache != null) {
			if (bi == cacheBi) {
				return cache;
			}
		}
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (!(o instanceof ObjectivityCore)) {
				throw new InvalidTargetObjectTypeException(
						"not PlatformObjectivity object in PlatformObjectivityStore");
			}
			ObjectivityCore r = (ObjectivityCore) o;
			cache = r;
			cacheBi = bi;
			return r;
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.release1.objectivity.ObjectivityCore)
			return true;
		return false;
	}

	@Override
	protected boolean createIndividualityObjectConcrete(ObjectivityCoreDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			ObjectivityCoreDBI updated, ObjectivityCoreDBI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(ObjectivityCoreDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(ObjectivityCoreDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(ObjectivityCoreDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(ObjectivityCoreDBI updated,
			ObjectivityCoreDBI old) throws Exception {
		return true;
	}


}
