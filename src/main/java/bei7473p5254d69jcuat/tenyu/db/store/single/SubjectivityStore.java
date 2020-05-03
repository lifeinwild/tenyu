package bei7473p5254d69jcuat.tenyu.db.store.single;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class SubjectivityStore
		extends ModelStore<SubjectivityI, Subjectivity>
		implements SingleObjectStoreI {
	public static final String modelName = Subjectivity.class.getSimpleName();

	public SubjectivityStore(Transaction txn) {
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
	protected Subjectivity chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Subjectivity)
				return (Subjectivity) o;
			throw new InvalidTargetObjectTypeException(
					"not Subjectivity object in SubjectivityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.Subjectivity)
			return true;
		return false;
	}

	@Override
	protected final List<StoreInfo> getStoresModelConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createModelConcrete(SubjectivityI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateModelConcrete(
			SubjectivityI updated, SubjectivityI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteModelConcrete(SubjectivityI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existModelConcrete(SubjectivityI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean noExistModelConcrete(SubjectivityI o,
			ValidationResult r) throws Exception {
		return true;
	}

	@Override
	protected boolean updateModelConcrete(SubjectivityI updated,
			SubjectivityI old) throws Exception {
		return true;
	}

}
