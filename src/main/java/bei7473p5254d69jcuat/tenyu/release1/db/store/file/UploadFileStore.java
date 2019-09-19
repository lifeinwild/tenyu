package bei7473p5254d69jcuat.tenyu.release1.db.store.file;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import jetbrains.exodus.env.*;

public abstract class UploadFileStore<T1 extends UploadFileDBI, T2 extends T1>
		extends NaturalityStore<T1, T2> {
	protected UploadFileStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected boolean createNaturalityConcrete(T1 o) throws Exception {
		if (!util.put(getFileHashStore(), cnvBA(o.getFileHash()),
				cnvL(o.getRecycleId()))) {
			return false;
		}
		return true;
	}

	abstract protected boolean createSubUploadFileConcrete(T1 o)
			throws Exception;

	abstract protected boolean dbValidateAtUpdateUploadFileConcrete(T1 updated,
			T1 old, ValidationResult r);

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(T1 updated, T1 old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getFileHash(), old.getFileHash())) {
			Long id = getIdByFileHash(updated.getFileHash());
			if (id != null) {
				r.add(Lang.UPLOADFILE_FILEHASH, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (!dbValidateAtUpdateUploadFileConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean deleteNaturalityConcrete(T1 o) throws Exception {
		if (!util.remove(getFileHashStore(), cnvBA(o.getFileHash())))
			return false;
		return true;
	}

	abstract protected boolean deleteSubUploadFileConcrete(T1 o)
			throws Exception;

	@Override
	protected boolean existNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (getIdByFileHash(o.getFileHash()) == null) {
			vr.add(Lang.UPLOADFILE_FILEHASH, Lang.ERROR_DB_NOTFOUND,
					Lang.NATURALITY_NAME + "=" + o.getName());
			b = false;
		}

		if (!existSubUploadFileConcrete(o, vr))
			b = false;
		return b;
	}

	abstract protected boolean existSubUploadFileConcrete(T1 o,
			ValidationResult vr) throws Exception;

	protected StoreInfo getFileHashStore() {
		return new StoreInfo(getName() + "_FileHashToId");
	}

	public Long getIdByFileHash(byte[] fileHash) {
		return getId(getFileHashStore(), cnvBA(fileHash));
	}

	abstract protected List<StoreInfo> getStoresUploadFileConcrete();

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getFileHashStore());
		r.addAll(getStoresUploadFileConcrete());
		return r;
	}

	@Override
	protected boolean noExistNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (getIdByFileHash(o.getFileHash()) != null) {
			vr.add(Lang.UPLOADFILE_FILEHASH, Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (!noExistSubUploadFileConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean noExistSubUploadFileConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected boolean updateNaturalityConcrete(T1 updated, T1 old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getFileHash(), old.getFileHash())) {
			if (old.getFileHash() != null) {
				if (!util.remove(getFileHashStore(), cnvBA(old.getFileHash())))
					return false;
			}
			if (!util.put(getFileHashStore(), cnvBA(updated.getFileHash()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return updateSubUploadFileConcrete(updated, old);
	}

	abstract protected boolean updateSubUploadFileConcrete(T1 updated, T1 old)
			throws Exception;

}
