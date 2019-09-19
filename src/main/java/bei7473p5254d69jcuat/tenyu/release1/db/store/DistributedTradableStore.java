package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.dtradable.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class DistributedTradableStore
		extends NaturalityStore<DistributedTradableDBI, DistributedTradable> {
	public static final String modelName = DistributedTradable.class
			.getSimpleName();
	private static final StoreInfo confirmedUserIdToId = new StoreInfo(
			modelName + "_confirmedUserIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	//ownerUserIdによる検索は実装しない。外部ツールで対応する

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public DistributedTradableStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected DistributedTradable chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof DistributedTradable)
				return (DistributedTradable) o;
			throw new InvalidTargetObjectTypeException(
					"not DistributedTradable object in DistributedTradableStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(DistributedTradableDBI o)
			throws Exception {
		if (!util.put(confirmedUserIdToId, cnvL(o.getConfirmedOwnerUserId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(
			DistributedTradableDBI updated, DistributedTradableDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getConfirmedOwnerUserId(),
				old.getConfirmedOwnerUserId())) {
			if (existByConfirmedUser(updated.getConfirmedOwnerUserId(),
					updated.getRecycleId())) {
				r.add(Lang.DISTRIBUTEDTRADABLE_ID_BY_CONFIRMEDUSER,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteNaturalityConcrete(DistributedTradableDBI o)
			throws Exception {
		if (!util.deleteDupSingle(confirmedUserIdToId,
				cnvL(o.getConfirmedOwnerUserId()), cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByConfirmedUser(Long confirmedUserId, Long rId) {
		if (confirmedUserId == null || rId == null)
			return false;
		return util.getDupSingle(confirmedUserIdToId, cnvL(confirmedUserId),
				cnvL(rId), (bi) -> cnvL(bi)) != null;
	}

	@Override
	public boolean existNaturalityConcrete(DistributedTradableDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByConfirmedUser(o.getConfirmedOwnerUserId(),
				o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
					Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId()
							+ Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER + "="
							+ o.getConfirmedOwnerUserId());
			b = false;
		}
		return b;
	}

	public List<Long> getIdsByConfirmedUser(Long confirmedUserId) {
		return util.getDup(confirmedUserIdToId, cnvL(confirmedUserId),
				(bi) -> cnvL(bi));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(confirmedUserIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.dtradable.DistributedTradableDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistNaturalityConcrete(DistributedTradableDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByConfirmedUser(o.getConfirmedOwnerUserId(),
				o.getRecycleId())) {
			vr.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateNaturalityConcrete(DistributedTradableDBI updated,
			DistributedTradableDBI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getConfirmedOwnerUserId(),
				old.getConfirmedOwnerUserId())) {
			if (old.getConfirmedOwnerUserId() != null) {
				if (!util.deleteDupSingle(confirmedUserIdToId,
						cnvL(old.getConfirmedOwnerUserId()),
						cnvL(old.getRecycleId())))
					return false;
			}
			if (!util.put(confirmedUserIdToId,
					cnvL(updated.getConfirmedOwnerUserId()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return true;
	}
}
