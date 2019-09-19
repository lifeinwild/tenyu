package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.vote.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 客観の一部だが特殊
 * DistributedVoteのID+開始日時を文字列にしてキーにする
 * 持続型の分散合意について各回の分散合意結果を区別するため
 * つまり1つの分散合意IDに対して複数の分散合意結果IDがあるから。
 * <分散合意ID_開始ヒストリーインデックス>をキーとする。
 * だから分散合意の開始タイミングは「HI更新があるかもしれないタイミング」の直後。
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVoteResultStore
		extends ObjectivityObjectStore<DistributedVoteResultDBI,
				DistributedVoteResult> {

	public static final String name = DistributedVoteResult.class
			.getSimpleName();
	/**
	 * byte[] : id
	 * 1:1対応
	 * byte[]はdistributedVoteIdとstartHistoryIndexを連結したもの
	 * dvIdで検索かつstartHistoryIndexでソート済みということを実現できる
	 */
	private static final StoreInfo dvIdShiToId = new StoreInfo(
			name + "dvIdShiToId");
	private static final StoreInfo startHistoryIndexToId = new StoreInfo(
			name + "historyIndexToId_Dup", StoreConfig.WITH_DUPLICATES);

	@SuppressWarnings("unused")
	private static Long getDvId(byte[] dvIdShi) {
		return Glb.getUtil().getLong1(dvIdShi);
	}

	private static byte[] getDvIdShi(DistributedVoteResultDBI o) {
		return getDvIdShi(o.getDistributedVoteId(), o.getStartHistoryIndex());
	}

	private static byte[] getDvIdShi(Long distributedVoteId,
			long startHistoryIndex) {
		return Glb.getUtil().concat(distributedVoteId, startHistoryIndex);
	}

	@SuppressWarnings("unused")
	private static long getShi(byte[] dvIdShi) {
		return Glb.getUtil().getLong2(dvIdShi);
	}

	public DistributedVoteResultStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected DistributedVoteResult chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof DistributedVoteResult)
				return (DistributedVoteResult) o;
			throw new InvalidTargetObjectTypeException(
					"not DistributedVoteResult object in DistributedVoteResultStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(
			DistributedVoteResultDBI o) throws Exception {
		if (!util.put(startHistoryIndexToId, cnvL(o.getStartHistoryIndex()),
				cnvL(o.getRecycleId())))
			return false;
		if (!util.put(dvIdShiToId, cnvBA(getDvIdShi(o)),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			DistributedVoteResultDBI updated, DistributedVoteResultDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getStartHistoryIndex(),
				old.getStartHistoryIndex())) {
			if (existByStartHistoryIndex(updated.getStartHistoryIndex(),
					updated.getRecycleId())) {
				r.add(Lang.DISTRIBUTEDVOTE_RESULT_STARTHISTORYINDEX,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getDistributedVoteId(),
				old.getDistributedVoteId())) {
			if (existByDvIdShi(updated, updated.getRecycleId())) {
				r.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(
			DistributedVoteResultDBI o) throws Exception {
		if (!util.deleteDupSingle(startHistoryIndexToId,
				cnvL(o.getStartHistoryIndex()), cnvL(o.getRecycleId())))
			return false;
		if (!util.deleteDupSingle(dvIdShiToId, cnvBA(getDvIdShi(o)),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByDvIdShi(DistributedVoteResultDBI o, Long id) {
		if (o == null || id == null)
			return false;
		return util.getDupSingle(dvIdShiToId, cnvBA(getDvIdShi(o)), cnvL(id),
				(bi) -> cnvL(bi)) != null;
	}

	public boolean existByStartHistoryIndex(long startHistoryIndex, Long id) {
		if (id == null)
			return false;
		return util.getDupSingle(startHistoryIndexToId, cnvL(startHistoryIndex),
				cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existObjectivityObjectConcrete(
			DistributedVoteResultDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByStartHistoryIndex(o.getStartHistoryIndex(),
				o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_STARTHISTORYINDEX,
					Lang.ERROR_DB_NOTFOUND, optionShi(o));
			b = false;
		}
		if (!existByDvIdShi(o, o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
					Lang.ERROR_DB_NOTFOUND, optionDvId(o));
			b = false;
		}
		return b;
	}

	public List<
			DistributedVoteResult> getByDistributedVoteIdAndStartHistoryIndex(
					DistributedVoteResultDBI o, int max) {
		List<Long> ids = getIdsByDistributedVoteIdAndStartHistoryIndex(o, max);
		List<DistributedVoteResult> r = new ArrayList<>();
		for (Long id : ids)
			r.add(get(id));
		return r;
	}

	/**
	 * @param o
	 * @param max
	 * @return	特定の分散合意IDのID一覧のうちstartHistoryIndexでソートされた最初のmax件
	 */
	public List<Long> getIdsByDistributedVoteIdAndStartHistoryIndex(
			DistributedVoteResultDBI o, int max) {
		if (o == null || o.getDistributedVoteId() == null)
			return null;

		return util.getValuesByKeyPrefix(dvIdShiToId,
				cnvBA(Glb.getUtil().toByteArray(o.getDistributedVoteId())),
				valBi -> cnvL(valBi), max);
	}

	public List<Long> getIdsByStartHistoryIndex(long startHistoryIndex) {
		return util.getDup(startHistoryIndexToId, cnvL(startHistoryIndex),
				v -> cnvL(v));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(startHistoryIndexToId);
		r.add(dvIdShiToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof DistributedVoteResultDBI;
	}

	@Override
	protected boolean noExistObjectivityObjectConcrete(
			DistributedVoteResultDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByStartHistoryIndex(o.getStartHistoryIndex(),
				o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_STARTHISTORYINDEX,
					Lang.ERROR_DB_EXIST, optionShi(o));
			b = false;
		}
		if (existByDvIdShi(o, o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
					Lang.ERROR_DB_EXIST, optionDvId(o));
			b = false;
		}
		return b;
	}

	private String optionDvId(DistributedVoteResultDBI o) {
		return Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
				+ Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX
				+ "=" + o.getDistributedVoteId();
	}

	private String optionShi(DistributedVoteResultDBI o) {
		return Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
				+ Lang.DISTRIBUTEDVOTE_RESULT_STARTHISTORYINDEX + "="
				+ o.getStartHistoryIndex();
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(
			DistributedVoteResultDBI updated, DistributedVoteResultDBI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getStartHistoryIndex(),
				old.getStartHistoryIndex())) {
			if (!util.deleteDupSingle(startHistoryIndexToId,
					cnvL(old.getStartHistoryIndex()), cnvL(old.getRecycleId())))
				throw new IOException("Failed to updateSub");
			if (!util.put(startHistoryIndexToId,
					cnvL(updated.getStartHistoryIndex()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getDistributedVoteId(),
				old.getDistributedVoteId())) {
			if (!util.deleteDupSingle(dvIdShiToId, cnvBA(getDvIdShi(old)),
					cnvL(old.getRecycleId())))
				throw new IOException("Failed to updateSub");
			if (!util.put(dvIdShiToId, cnvBA(getDvIdShi(updated)),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		return true;
	}

}
