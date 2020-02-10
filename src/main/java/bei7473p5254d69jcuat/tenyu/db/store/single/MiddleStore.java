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

public class MiddleStore extends IdObjectStore<MiddleDBI, Middle>
		implements SingleObjectStore {
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
	protected final List<StoreInfo> getStoresIdObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createIdObjectConcrete(MiddleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIdObjectConcrete(MiddleDBI updated,
			MiddleDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIdObjectConcrete(MiddleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existIdObjectConcrete(MiddleDBI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean noExistIdObjectConcrete(MiddleDBI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean updateIdObjectConcrete(MiddleDBI updated, MiddleDBI old)
			throws Exception {
		return true;
	}

}
