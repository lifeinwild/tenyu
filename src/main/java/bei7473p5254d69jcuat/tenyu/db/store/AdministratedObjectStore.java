package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public abstract class AdministratedObjectStore<
		T1 extends AdministratedObjectI,
		T2 extends T1> extends IdObjectStore<T1, T2> {

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

	private final StoreInfo mainAdministratorToId;

	private final StoreInfo registerToId;

	protected AdministratedObjectStore(Transaction txn) {
		super(txn);
		mainAdministratorToId = getMainAdministratorStoreStatic(getName());
		registerToId = getRegistererStoreStatic(getName());
	}

	abstract protected boolean createAdministratedObjectConcrete(T1 o)
			throws Exception;

	@Override
	protected final boolean createIdObjectConcrete(T1 o) throws Exception {
		if (o.getRegistererUserId() == null)
			return false;
		if (!util.put(getRegistererStore(), cnvL(o.getRegistererUserId()),
				cnvL(o.getId()))) {
			throw new IOException("Failed to createSub");
		}
		if (!util.put(getMainAdministratorStore(),
				cnvL(o.getMainAdministratorUserId()), cnvL(o.getId())))
			return false;

		return createAdministratedObjectConcrete(o);
	}

	abstract protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			T1 updated, T1 old, ValidationResult r);

	@Override
	protected final boolean dbValidateAtUpdateIdObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getRegistererUserId(),
				old.getRegistererUserId())) {
			if (existByRegisterer(updated.getRegistererUserId(),
					updated.getId())) {
				r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER, Lang.ERROR_DB_EXIST);
				b = false;
			}
			//登録者も変更される場合があるので変更した
			//r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER, Lang.ERROR_UNALTERABLE);
		}
		if (Glb.getUtil().notEqual(updated.getMainAdministratorUserId(),
				old.getMainAdministratorUserId())) {
			if (existByAdministrator(updated.getMainAdministratorUserId(),
					updated.getId())) {
				r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (!dbValidateAtUpdateAdministratedObjectConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean deleteAdministratedObjectConcrete(T1 o)
			throws Exception;

	@Override
	protected final boolean deleteIdObjectConcrete(T1 o) throws Exception {
		if (!util.deleteDupSingle(getRegistererStore(),
				cnvL(o.getRegistererUserId()), cnvL(o.getId())))
			throw new IOException("Failed to deleteSub");
		if (!util.deleteDupSingle(getMainAdministratorStore(),
				cnvL(o.getMainAdministratorUserId()), cnvL(o.getId())))
			return false;

		return deleteAdministratedObjectConcrete(o);
	}

	abstract protected boolean existAdministratedObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	public boolean existByAdministrator(Long administratorUserId,
			Long socialityId) {
		if (administratorUserId == null || socialityId == null)
			return false;
		return util.getDupSingle(getMainAdministratorStore(),
				cnvL(administratorUserId), cnvL(socialityId),
				(bi) -> cnvL(bi)) != null;
	}

	public boolean existByRegisterer(Long registererUserId,
			Long administratedObjectId) {
		if (registererUserId == null || administratedObjectId == null)
			return false;
		return util.getDupSingle(getRegistererStore(), cnvL(registererUserId),
				cnvL(administratedObjectId), (bi) -> cnvL(bi)) != null;
	}

	/* (非 Javadoc)
	 * GUI等においてDBとの整合性を検証するとき、
	 * まだIDが割り振られていないのでexist()を呼べないので、
	 * これをpublicにして直接呼び出す。
	 * @see bei7473p5254d69jcuat.tenyu.db.store.IdObjectStore#existIdObjectConcrete(bei7473p5254d69jcuat.tenyu.db.store.IdObjectI, glb.util.ValidationResult)
	 */
	@Override
	protected final boolean existIdObjectConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existByRegisterer(o.getRegistererUserId(), o.getId())) {
			vr.add(Lang.ADMINISTRATEDOBJECT_REGISTERER, Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_ID + "=" + o.getId() + " "
							+ Lang.ADMINISTRATEDOBJECT_REGISTERER + "="
							+ o.getRegistererUserId());
			b = false;
		}
		if (noExistByAdministrator(o.getMainAdministratorUserId(), o.getId())) {
			vr.add(Lang.ADMINISTRATEDOBJECT_ID_BY_ADMINISTRATOR,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}

		if (!existAdministratedObjectConcrete(o, vr))
			b = false;
		return b;
	}

	public List<Long> getIdsByAdministrator(Long mainAdministratorUserId) {
		return util.getDup(getMainAdministratorStore(),
				cnvL(mainAdministratorUserId), (bi) -> cnvL(bi));
	}
	public List<Long> getIdsByRegisterer(Long registererUserId) {
		return util.getDup(getRegistererStore(), cnvL(registererUserId),
				(bi) -> cnvL(bi));
	}

	public final StoreInfo getMainAdministratorStore() {
		return mainAdministratorToId;
	}

	public final StoreInfo getRegistererStore() {
		return registerToId;
	}

	abstract protected List<StoreInfo> getStoresAdministratedObjectConcrete();

	@Override
	protected List<StoreInfo> getStoresIdObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getRegistererStore());
		r.add(getMainAdministratorStore());
		r.addAll(getStoresAdministratedObjectConcrete());
		return r;
	}

	abstract protected boolean noExistAdministratedObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	protected boolean noExistByAdministrator(Long administratorUserId,
			Long socialityId) {
		return !existByAdministrator(administratorUserId, socialityId);
	}

	/* (非 Javadoc)
	 * @see bei7473p5254d69jcuat.tenyu.db.store.IdObjectStore#noExistIdObjectConcrete(bei7473p5254d69jcuat.tenyu.db.store.IdObjectI, glb.util.ValidationResult)
	 * GUI等においてDBとの整合性を検証するとき、
	 * まだIDが割り振られていないのでexist()を呼べないので、
	 * これをpublicにして直接呼び出す。
	 */
	@Override
	public final boolean noExistIdObjectConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (existByRegisterer(o.getRegistererUserId(), o.getId())) {
			vr.add(Lang.ADMINISTRATEDOBJECT_REGISTERER, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByAdministrator(o.getMainAdministratorUserId(), o.getId())) {
			vr.add(Lang.ADMINISTRATEDOBJECT_ID_BY_ADMINISTRATOR,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		if (!noExistAdministratedObjectConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean updateAdministratedObjectConcrete(T1 updated,
			T1 old) throws Exception;

	@Override
	protected final boolean updateIdObjectConcrete(T1 updated, T1 old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getRegistererUserId(),
				old.getRegistererUserId())) {
			if (old.getRegistererUserId() != null) {
				if (!util.deleteDupSingle(getRegistererStore(),
						cnvL(old.getRegistererUserId()), cnvL(old.getId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(getRegistererStore(),
					cnvL(updated.getRegistererUserId()), cnvL(updated.getId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getMainAdministratorUserId(),
				old.getMainAdministratorUserId())) {
			if (!util.deleteDupSingle(getMainAdministratorStore(),
					cnvL(old.getMainAdministratorUserId()), cnvL(old.getId())))
				return false;
			if (!util.put(getMainAdministratorStore(),
					cnvL(updated.getMainAdministratorUserId()),
					cnvL(updated.getId())))
				return false;
		}
		return updateAdministratedObjectConcrete(updated, old);
	}

}
