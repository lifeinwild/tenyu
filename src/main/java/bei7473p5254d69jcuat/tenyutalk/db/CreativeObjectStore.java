package bei7473p5254d69jcuat.tenyutalk.db;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public abstract class CreativeObjectStore<T1 extends CreativeObjectI,
		T2 extends T1> extends IndividualityObjectStore<T1, T2> {
	public static final StoreInfo getFirstIdVersionToIdStoreStatic(
			String storeName, String userName) {
		return new StoreInfo(
				storeName + "_" + userName + "_firstIdVersionToId");
	}

	public static final StoreInfo getUploaderUserIdStoreStatic(String storeName,
			String userName) {
		return new StoreInfo(
				storeName + "_" + userName + "_uploaderUserIdToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	public static final StoreInfo getPublicationTimestampStoreStatic(
			String storeName, String userName) {
		return new StoreInfo(
				storeName + "_" + userName + "_publicationTimestampToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	/**
	 * TODO KVSにおいてバージョン順にソートされるか？
	 */
	private StoreInfo firstIdVersionToId;
	private StoreInfo publicationTimestampToIds;

	protected CreativeObjectStore(Transaction txn) {
		super(txn);
		String userName = Glb.getMiddle().getMe().getName();
		firstIdVersionToId = getFirstIdVersionToIdStoreStatic(getName(),
				userName);
		publicationTimestampToIds = getPublicationTimestampStoreStatic(
				getName(), userName);
	}

	private static ByteIterable getFirstIdKey(Long firstId) {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES * 1).putLong(firstId);
		return cnvBA(buf.array());
	}

	private static ByteIterable getFirstIdMajorMinorKey(Long firstId,
			GeneralVersioning v) {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES * 2).putLong(firstId)
				.putLong(v.getMajor());
		return cnvBA(buf.array());
	}

	private static ByteIterable getFirstIdMajorKey(Long firstId,
			GeneralVersioning v) {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES * 3).putLong(firstId)
				.putLong(v.getMajor()).putLong(v.getMinor());
		return cnvBA(buf.array());
	}

	private static ByteIterable getFirstIdMajorMinorPatchKey(Long firstId,
			GeneralVersioning v) {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES * 4).putLong(firstId)
				.putLong(v.getMajor()).putLong(v.getMinor())
				.putLong(v.getPatch());
		return cnvBA(buf.array());
	}

	public List<Long> getOldIdsByFirstIdAndVersion(Long firstId,
			GeneralVersioning ver) {
		return util.getValuesByKeyPrefix(firstIdVersionToId,
				getFirstIdMajorMinorPatchKey(firstId, ver), bi -> cnvL(bi), -1);
	}

	public List<Long> getAllOldIdsByFirstId(Long firstId) {
		return util.getValuesByKeyPrefix(firstIdVersionToId,
				getFirstIdKey(firstId), bi -> cnvL(bi), -1);
	}

	/**
	 * @param firstId	この系列のファイルについて
	 * @param ver	このバージョン以前
	 * @return	firstIdの系列についてverより前のバージョンのオブジェクト一覧
	 */
	public List<T2> getOldVersions(Long firstId, GeneralVersioning ver) {
		return get(getOldIdsByFirstIdAndVersion(firstId, ver));
	}

	/**
	 * @param firstId
	 * @return	firstIdの系列の全オブジェクト
	 */
	public List<T2> getOldVersions(Long firstId) {
		return get(getAllOldIdsByFirstId(firstId));
	}

	/**
	 * @param firstId	初代ID
	 * @return	firstIdの最新版のID
	 */
	public Long getLatestIdOfFirstId(Long firstId) {
		return util.prefixSingle(firstIdVersionToId, getFirstIdKey(firstId),
				bi -> cnvL(bi));
	}

	public T2 getLatestObjOfFirstId(Long firstId) {
		return get(getLatestIdOfFirstId(firstId));
	}

	public Long getLatestIdOfFirstId(T1 o) {
		return getLatestIdOfFirstId(o.getFirstId());
	}

	public Long getLatestIdOfFirstIdMajor(Long firstId, GeneralVersioning v) {
		return util.prefixSingle(firstIdVersionToId,
				getFirstIdMajorKey(firstId, v), bi -> cnvL(bi));
	}

	public T2 getLatestObjOfFirstIdMajor(Long firstId, GeneralVersioning v) {
		return get(getLatestIdOfFirstIdMajor(firstId, v));
	}

	public Long getLatestIdOfFirstIdMajor(T1 o) {
		return getLatestIdOfFirstIdMajor(o.getFirstId(), o.getVersion());
	}

	public Long getLatestIdOfFirstIdMajorMinor(Long firstId,
			GeneralVersioning v) {
		return util.prefixSingle(firstIdVersionToId,
				getFirstIdMajorMinorKey(firstId, v), bi -> cnvL(bi));
	}

	public T2 getLatestObjOfFirstIdMajorMinor(Long firstId,
			GeneralVersioning v) {
		return get(getLatestIdOfFirstIdMajorMinor(firstId, v));
	}

	public Long getLatestIdOfFirstIdMajorMinor(T1 o) {
		return getLatestIdOfFirstIdMajorMinor(o.getFirstId(), o.getVersion());
	}

	public Long getLatestIdOfFirstIdMajorMinorPatch(Long firstId,
			GeneralVersioning v) {
		return util.prefixSingle(firstIdVersionToId,
				getFirstIdMajorMinorPatchKey(firstId, v), bi -> cnvL(bi));
	}

	public T2 getLatestObjOfFirstIdMajorMinorPatch(Long firstId,
			GeneralVersioning v) {
		return get(getLatestIdOfFirstIdMajorMinorPatch(firstId, v));
	}

	public Long getLatestIdOfFirstIdMajorMinorPatch(T1 o) {
		return getLatestIdOfFirstIdMajorMinorPatch(o.getFirstId(),
				o.getVersion());
	}

	public List<Long> getIdsByPublicationTimestamp(
			boolean publicationTimestamp) {
		return util.getDup(publicationTimestampToIds,
				cnvBo(publicationTimestamp), bi -> cnvL(bi));
	}

	public boolean existByPublicationTimestamp(boolean publicationTimestamp,
			Long id) {
		if (id == null)
			return false;
		return util.getDupSingle(publicationTimestampToIds,
				cnvBo(publicationTimestamp), cnvL(id), bi -> cnvL(bi)) != null;
	}

	abstract protected boolean createVersionedConcrete(T1 o) throws Exception;

	abstract protected boolean dbValidateAtUpdateVersionedConcrete(T1 updated,
			T1 old, ValidationResult r);

	abstract protected boolean deleteVersionedConcrete(T1 o) throws Exception;

	abstract protected boolean existVersionedConcrete(T1 o, ValidationResult vr)
			throws Exception;

	abstract public List<StoreInfo> getStoresVersionedConcrete();

	abstract protected boolean noExistVersionedConcrete(T1 o,
			ValidationResult vr) throws Exception;

	abstract protected boolean updateVersionedConcrete(T1 updated, T1 old)
			throws Exception;

	@Override
	protected boolean createIndividualityObjectConcrete(T1 o) throws Exception {
		ByteIterable idBi = cnvL(o.getId());

		if (!util.put(firstIdVersionToId,
				getFirstIdMajorMinorPatchKey(o.getFirstId(), o.getVersion()),
				idBi)) {
			throw new IOException("Failed to put. o=" + o);
		}

		if (!util.put(publicationTimestampToIds,
				cnvBo(o.isPublicationTimestamp()), idBi)) {
			throw new IOException("Failed to put. o=" + o);
		}

		if (!createVersionedConcrete(o))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getVersion(), old.getVersion())) {
			Long existId = getLatestIdOfFirstIdMajorMinorPatch(updated);
			if (existId != null) {
				r.add(Lang.VERSION, Lang.ERROR_DB_EXIST, "updated=" + updated);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.isPublicationTimestamp(),
				old.isPublicationTimestamp())) {
			if (existByPublicationTimestamp(updated.isPublicationTimestamp(),
					updated.getId())) {
				r.add(Lang.CREATIVE_OBJECT_PUBLICATION_TIMESTAMP,
						Lang.ERROR_DB_EXIST, "updated=" + updated);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(T1 o) throws Exception {
		ByteIterable idBi = cnvL(o.getId());
		if (!util.deleteDupSingle(firstIdVersionToId,
				getFirstIdMajorMinorPatchKey(o.getFirstId(), o.getVersion()),
				idBi)) {
			throw new IOException("Failed to deleteDupSingle");
		}

		if (!util.deleteDupSingle(publicationTimestampToIds,
				cnvBo(o.isPublicationTimestamp()), idBi)) {
			throw new IOException("Failed to deleteDupSingle");
		}

		if (!deleteVersionedConcrete(o))
			return false;

		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getLatestIdOfFirstId(o) == null
				|| getLatestIdOfFirstIdMajor(o) == null
				|| getLatestIdOfFirstIdMajorMinor(o) == null
				|| getLatestIdOfFirstIdMajorMinorPatch(o) == null) {
			vr.add(Lang.VERSION, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		if (!existByPublicationTimestamp(o.isPublicationTimestamp(),
				o.getId())) {
			vr.add(Lang.CREATIVE_OBJECT_PUBLICATION_TIMESTAMP,
					Lang.ERROR_DB_NOTFOUND, "o=" + o);
			b = false;
		}
		return b;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(firstIdVersionToId);
		r.add(publicationTimestampToIds);
		r.addAll(getStoresVersionedConcrete());
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getLatestIdOfFirstId(o) != null
				|| getLatestIdOfFirstIdMajor(o) != null
				|| getLatestIdOfFirstIdMajorMinor(o) != null
				|| getLatestIdOfFirstIdMajorMinorPatch(o) != null) {
			vr.add(Lang.VERSION, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByPublicationTimestamp(o.isPublicationTimestamp(),
				o.getId())) {
			vr.add(Lang.CREATIVE_OBJECT_PUBLICATION_TIMESTAMP,
					Lang.ERROR_DB_EXIST, "o=" + o);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(T1 updated, T1 old)
			throws Exception {
		ByteIterable idBi = cnvL(updated.getId());
		if (Glb.getUtil().notEqual(updated.getVersion(), old.getVersion())) {
			if (!util.deleteDupSingle(firstIdVersionToId,
					getFirstIdMajorMinorPatchKey(old.getFirstId(),
							old.getVersion()),
					idBi)) {
				throw new IOException("Failed to deleteDup. old=" + old);
			}
			if (!util.put(firstIdVersionToId, getFirstIdMajorMinorPatchKey(
					updated.getFirstId(), updated.getVersion()), idBi))
				throw new IOException("Failed to put");
		}

		if (updated.isPublicationTimestamp() != old.isPublicationTimestamp()) {
			if (!util.remove(publicationTimestampToIds,
					cnvBo(old.isPublicationTimestamp())))
				throw new IOException("Failed to remove. old=" + old);
			if (!util.put(publicationTimestampToIds,
					cnvBo(updated.isPublicationTimestamp()), idBi))
				throw new IOException("Failed to put. updated=" + updated);
		}

		if (!updateVersionedConcrete(updated, old))
			return false;

		return true;
	}

}
