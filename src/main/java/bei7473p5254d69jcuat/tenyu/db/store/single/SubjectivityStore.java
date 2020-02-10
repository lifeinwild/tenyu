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
		extends IdObjectStore<SubjectivityDBI, Subjectivity>
		implements SingleObjectStore {
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
	protected final List<StoreInfo> getStoresIdObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createIdObjectConcrete(SubjectivityDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIdObjectConcrete(
			SubjectivityDBI updated, SubjectivityDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIdObjectConcrete(SubjectivityDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIdObjectConcrete(SubjectivityDBI o, ValidationResult r)
			throws Exception {
		return true;
	}

	@Override
	protected boolean noExistIdObjectConcrete(SubjectivityDBI o,
			ValidationResult r) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIdObjectConcrete(SubjectivityDBI updated,
			SubjectivityDBI old) throws Exception {
		return true;
	}

}
