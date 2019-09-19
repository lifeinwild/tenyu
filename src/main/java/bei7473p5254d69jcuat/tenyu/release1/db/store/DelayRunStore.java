package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.env.*;

public abstract class DelayRunStore<T1 extends DelayRunDBI, T2 extends T1>
		extends ObjectivityObjectStore<T1, T2> {
	protected DelayRunStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	abstract protected boolean createDelayRunConcrete(DelayRunDBI o);

	@Override
	protected final boolean createObjectivityObjectConcrete(DelayRunDBI o)
			throws Exception {
		if (!util.put(getRunHistoryIndexToIdStore(),
				cnvL(o.getRunHistoryIndex()), cnvL(o.getRecycleId())))
			return false;

		return createDelayRunConcrete(o);
	}

	abstract protected boolean dbValidateAtUpdateDelayRunConcrete(
			DelayRunDBI updated, DelayRunDBI old, ValidationResult r);

	@Override
	protected final boolean dbValidateAtUpdateObjectivityObjectConcrete(
			DelayRunDBI updated, DelayRunDBI old, ValidationResult r) {
		boolean b = true;

		if (Glb.getUtil().notEqual(updated.getRunHistoryIndex(),
				old.getRunHistoryIndex())) {
			if (existByRunHistoryIndex(updated.getRunHistoryIndex(),
					updated.getRecycleId())) {
				r.add(Lang.DELAYRUN_RUNHISTORYINDEX, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		if (!dbValidateAtUpdateDelayRunConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean deleteDelayRunConcrete(DelayRunDBI o);

	@Override
	protected final boolean deleteObjectivityObjectConcrete(DelayRunDBI o)
			throws Exception {
		if (!util.deleteDupSingle(getRunHistoryIndexToIdStore(),
				cnvL(o.getRunHistoryIndex()), cnvL(o.getRecycleId())))
			return false;
		return deleteDelayRunConcrete(o);
	}

	public boolean existByRunHistoryIndex(Long runHistoryIndex, Long id) {
		if (runHistoryIndex == null || id == null)
			return false;
		return util.getDupSingle(getRunHistoryIndexToIdStore(),
				cnvL(runHistoryIndex), cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	abstract protected ValidationResult existDelayRunConcrete(DelayRunDBI o,
			ValidationResult vr);

	@Override
	protected final boolean existObjectivityObjectConcrete(DelayRunDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByRunHistoryIndex(o.getRunHistoryIndex(), o.getRecycleId())) {
			vr.add(Lang.DELAYRUN_RUNHISTORYINDEX, Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.DELAYRUN_RUNHISTORYINDEX + "="
							+ o.getRunHistoryIndex());
			b = false;
		}
		return b;
	}

	public List<Long> getAllHistoryIndex() {
		return util.getAllKeysNoDup(getRunHistoryIndexToIdStore(),
				bi -> cnvL(bi));
	}

	public List<Long> getIdsByRunHistoryIndex(long runHistoryIndex) {
		return util.getDup(getRunHistoryIndexToIdStore(), cnvL(runHistoryIndex),
				v -> cnvL(v));
	}

	protected StoreInfo getRunHistoryIndexToIdStore() {
		return new StoreInfo(getName() + "_getRunHistoryIndexToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	abstract public List<StoreInfo> getStoresDelayRunConcrete();

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getRunHistoryIndexToIdStore());
		r.addAll(getStoresDelayRunConcrete());
		return r;
	}

	abstract protected boolean noExistDelayRunConcrete(DelayRunDBI o,
			ValidationResult vr);

	@Override
	protected final boolean noExistObjectivityObjectConcrete(DelayRunDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByRunHistoryIndex(o.getRunHistoryIndex(), o.getRecycleId())) {
			vr.add(Lang.DELAYRUN_RUNHISTORYINDEX, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (!noExistDelayRunConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean updateDelayRunConcrete(DelayRunDBI updated,
			DelayRunDBI old);

	@Override
	protected final boolean updateObjectivityObjectConcrete(
			DelayRunDBI updated, DelayRunDBI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getRunHistoryIndex(),
				old.getRunHistoryIndex())) {
			if (!util.deleteDupSingle(getRunHistoryIndexToIdStore(),
					cnvL(old.getRunHistoryIndex()), cnvL(old.getRecycleId())))
				throw new IOException("Failed to updateSub");
			if (!util.put(getRunHistoryIndexToIdStore(),
					cnvL(updated.getRunHistoryIndex()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		return updateDelayRunConcrete(updated, old);
	}

}
