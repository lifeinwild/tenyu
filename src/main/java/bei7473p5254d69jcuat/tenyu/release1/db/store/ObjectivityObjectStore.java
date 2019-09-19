package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.env.*;

public abstract class ObjectivityObjectStore<T1 extends ObjectivityObjectDBI,
		T2 extends T1> extends IdObjectStore<T1, T2> {

	/*
	public static <S extends ObjectivityObjectStore<T1, T2>,
			T1 extends ObjectivityObjectDBI,
			T2 extends T1> List<Long> getIdByRegistererSimple(
					Function<Transaction, S> getStoreFunc,
					Long registererUserId) {
		return simple(getStoreFunc,
				(s) -> s.getIdsByRegisterer(registererUserId));
	}
	*/

	/*
	public static <S extends ObjectivityObjectStore<T1, T2>,
			T1 extends ObjectivityObjectDBI,
			T2 extends T1> List<Long> getIdByAdministratorSimple(
					Function<Transaction, S> getStoreFunc,
					Long administratorUserId) {
		return simple(getStoreFunc,
				(s) -> s.getIdsByAdministrator(administratorUserId));
	}
	*/

	/**
	 * 管理者ID:socialityID
	 * n:m対応
	 */
	public static final StoreInfo getMainAdministratorStoreStatic(
			String storeName) {
		return new StoreInfo(storeName + "_mainAdministratorToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	/**
	 * 登録者ユーザーID：登録された情報のID
	 * 登録者ユーザーIDはストア内で重複可能で、取得時の返値はリストになる。
	 */
	public static final StoreInfo getRegistererStoreStatic(String storeName) {
		return new StoreInfo(storeName + "_registererToId_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	protected ObjectivityObjectStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected final boolean createIdObjectConcrete(T1 o) throws Exception {
		if (o.getRegistererUserId() == null)
			return false;
		if (!util.put(getRegistererStore(), cnvL(o.getRegistererUserId()),
				cnvL(o.getRecycleId()))) {
			throw new IOException("Failed to createSub");
		}
		if (!util.put(getMainAdministratorStore(),
				cnvL(o.getMainAdministratorUserId()), cnvL(o.getRecycleId())))
			return false;

		return createObjectivityObjectConcrete(o);
	}

	abstract protected boolean createObjectivityObjectConcrete(T1 o)
			throws Exception;

	@Override
	protected final boolean dbValidateAtUpdateIdObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getRegistererUserId(),
				old.getRegistererUserId())) {
			if (existByRegisterer(updated.getRegistererUserId(),
					updated.getRecycleId())) {
				r.add(Lang.OBJECTIVITYOBJECT_REGISTERER, Lang.ERROR_DB_EXIST);
				b = false;
			}
			//登録者も変更される場合があるので変更した
			//r.add(Lang.OBJECTIVITYOBJECT_REGISTERER, Lang.ERROR_UNALTERABLE);
		}
		if (Glb.getUtil().notEqual(updated.getMainAdministratorUserId(),
				old.getMainAdministratorUserId())) {
			if (existByAdministrator(updated.getMainAdministratorUserId(),
					updated.getRecycleId())) {
				r.add(Lang.OBJECTIVITYOBJECT_ADMINISTRATOR,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (!dbValidateAtUpdateObjectivityObjectConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			T1 updated, T1 old, ValidationResult r);

	@Override
	protected final boolean deleteIdObjectConcrete(T1 o) throws Exception {
		if (!util.deleteDupSingle(getRegistererStore(),
				cnvL(o.getRegistererUserId()), cnvL(o.getRecycleId())))
			throw new IOException("Failed to deleteSub");
		if (!util.deleteDupSingle(getMainAdministratorStore(),
				cnvL(o.getMainAdministratorUserId()), cnvL(o.getRecycleId())))
			return false;

		return deleteObjectivityObjectConcrete(o);
	}

	abstract protected boolean deleteObjectivityObjectConcrete(T1 o)
			throws Exception;

	public boolean existByAdministrator(Long administratorUserId,
			Long socialityId) {
		if (administratorUserId == null || socialityId == null)
			return false;
		return util.getDupSingle(getMainAdministratorStore(),
				cnvL(administratorUserId), cnvL(socialityId),
				(bi) -> cnvL(bi)) != null;
	}

	public boolean existByRegisterer(Long registererUserId,
			Long objectivityObjectId) {
		if (registererUserId == null || objectivityObjectId == null)
			return false;
		return util.getDupSingle(getRegistererStore(), cnvL(registererUserId),
				cnvL(objectivityObjectId), (bi) -> cnvL(bi)) != null;
	}

	@Override
	public final boolean existIdObjectConcrete(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByRegisterer(o.getRegistererUserId(), o.getRecycleId())) {
			vr.add(Lang.OBJECTIVITYOBJECT_REGISTERER, Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.OBJECTIVITYOBJECT_REGISTERER + "="
							+ o.getRegistererUserId());
			b = false;
		}
		if (noExistByAdministrator(o.getMainAdministratorUserId(),
				o.getRecycleId())) {
			vr.add(Lang.OBJECTIVITYOBJECT_ID_BY_ADMINISTRATOR,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existObjectivityObjectConcrete(o, vr))
			b = false;
		return b;
	}

	abstract protected boolean existObjectivityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	public List<Long> getIdsByAdministrator(Long mainAdministratorUserId) {
		return util.getDup(getMainAdministratorStore(),
				cnvL(mainAdministratorUserId), (bi) -> cnvL(bi));
	}

	public List<Long> getIdsByRegisterer(Long registererUserId) {
		return util.getDup(getRegistererStore(), cnvL(registererUserId),
				(bi) -> cnvL(bi));
	}

	public final StoreInfo getMainAdministratorStore() {
		return getMainAdministratorStoreStatic(getName());
	}

	public final StoreInfo getRegistererStore() {
		return getRegistererStoreStatic(getName());
	}

	@Override
	public List<StoreInfo> getStoresIdObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getRegistererStore());
		r.add(getMainAdministratorStore());
		r.addAll(getStoresObjectivityObjectConcrete());
		return r;
	}

	abstract public List<StoreInfo> getStoresObjectivityObjectConcrete();

	public boolean noExistByAdministrator(Long administratorUserId,
			Long socialityId) {
		return !existByAdministrator(administratorUserId, socialityId);
	}

	@Override
	public final boolean noExistIdObjectConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (existByRegisterer(o.getRegistererUserId(), o.getRecycleId())) {
			vr.add(Lang.OBJECTIVITYOBJECT_REGISTERER, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByAdministrator(o.getMainAdministratorUserId(),
				o.getRecycleId())) {
			vr.add(Lang.OBJECTIVITYOBJECT_ID_BY_ADMINISTRATOR,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (!noExistObjectivityObjectConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean noExistObjectivityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected final boolean updateIdObjectConcrete(T1 updated, T1 old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getRegistererUserId(),
				old.getRegistererUserId())) {
			if (old.getRegistererUserId() != null) {
				if (!util.deleteDupSingle(getRegistererStore(),
						cnvL(old.getRegistererUserId()),
						cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(getRegistererStore(),
					cnvL(updated.getRegistererUserId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getMainAdministratorUserId(),
				old.getMainAdministratorUserId())) {
			if (!util.deleteDupSingle(getMainAdministratorStore(),
					cnvL(old.getMainAdministratorUserId()),
					cnvL(old.getRecycleId())))
				return false;
			if (!util.put(getMainAdministratorStore(),
					cnvL(updated.getMainAdministratorUserId()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return updateObjectivityObjectConcrete(updated, old);
	}

	abstract protected boolean updateObjectivityObjectConcrete(T1 updated,
			T1 old) throws Exception;

}
