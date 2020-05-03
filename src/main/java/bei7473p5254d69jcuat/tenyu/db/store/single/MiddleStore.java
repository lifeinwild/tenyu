package bei7473p5254d69jcuat.tenyu.db.store.single;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class MiddleStore extends ModelStore<MiddleI, Middle>
		implements SingleObjectStoreI {
	public static final String modelName = Middle.class.getSimpleName();

	public MiddleStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean needCatchUp() {
		return false;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.release1.middle.Middle)
			return true;
		return false;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	protected Middle chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Middle)
				return (Middle) o;
			throw new InvalidTargetObjectTypeException(
					"not Middle object in SubjectivityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected final List<StoreInfo> getStoresModelConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createModelConcrete(MiddleI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateModelConcrete(MiddleI updated,
			MiddleI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteModelConcrete(MiddleI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existModelConcrete(MiddleI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean noExistModelConcrete(MiddleI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean updateModelConcrete(MiddleI updated, MiddleI old)
			throws Exception {
		return true;
	}

}
