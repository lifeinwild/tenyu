package bei7473p5254d69jcuat.tenyu.release1.db.store.file;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import jetbrains.exodus.env.*;

public abstract class FileConceptStore<T1 extends FileConceptDBI, T2 extends T1>
		extends NaturalityStore<T1, T2> {

	protected FileConceptStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	abstract protected boolean createDynamicFileConcrete(T1 o)
			throws Exception;

	@Override
	protected boolean createNaturalityConcrete(T1 o) throws Exception {
		return createDynamicFileConcrete(o);
	}

	abstract protected boolean dbValidateAtUpdateFileConceptConcrete(T1 updated,
			T1 old, ValidationResult r);

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(T1 updated, T1 old,
			ValidationResult r) {
		return dbValidateAtUpdateFileConceptConcrete(updated, old, r);
	}

	abstract protected boolean deleteDynamicFileConcrete(T1 o)
			throws Exception;

	@Override
	protected boolean deleteNaturalityConcrete(T1 o) throws Exception {
		return deleteDynamicFileConcrete(o);
	}

	abstract protected boolean existDynamicFileConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected boolean existNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		return existDynamicFileConcrete(o, vr);
	}

	abstract protected List<StoreInfo> getStoresFileConceptConcrete();

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		return getStoresFileConceptConcrete();
	}

	abstract protected boolean noExistDynamicFileConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected boolean noExistNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		return noExistDynamicFileConcrete(o, vr);
	}

	abstract protected boolean updateDynamicFileConcrete(T1 updated, T1 old)
			throws Exception;

	@Override
	protected boolean updateNaturalityConcrete(T1 updated, T1 old)
			throws Exception {
		return updateDynamicFileConcrete(updated, old);
	}

}
