package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.dtradable.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class DistributedTradableStore
		extends IndividualityObjectStore<DistributedTradableI, DistributedTradable> {
	public static final String modelName = DistributedTradable.class
			.getSimpleName();
	private static final StoreInfo confirmedUserIdToId = new StoreInfo(
			modelName + "_confirmedUserIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	//ownerUserIdによる検索は実装しない。外部ツールで対応する

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public DistributedTradableStore(Transaction txn) {
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
	protected boolean createIndividualityObjectConcrete(DistributedTradableI o)
			throws Exception {
		if (!util.put(confirmedUserIdToId, cnvL(o.getConfirmedOwnerUserId()),
				cnvL(o.getId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			DistributedTradableI updated, DistributedTradableI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getConfirmedOwnerUserId(),
				old.getConfirmedOwnerUserId())) {
			if (existByConfirmedUser(updated.getConfirmedOwnerUserId(),
					updated.getId())) {
				r.add(Lang.DISTRIBUTEDTRADABLE_ID_BY_CONFIRMEDUSER,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(DistributedTradableI o)
			throws Exception {
		if (!util.deleteDupSingle(confirmedUserIdToId,
				cnvL(o.getConfirmedOwnerUserId()), cnvL(o.getId())))
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
	public boolean existIndividualityObjectConcrete(DistributedTradableI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByConfirmedUser(o.getConfirmedOwnerUserId(),
				o.getId())) {
			vr.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
					Lang.ERROR_DB_NOTFOUND,
					Lang.ID + "=" + o.getId()
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
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(confirmedUserIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.dtradable.DistributedTradableI)
			return true;
		return false;
	}

	@Override
	public boolean noExistIndividualityObjectConcrete(DistributedTradableI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByConfirmedUser(o.getConfirmedOwnerUserId(),
				o.getId())) {
			vr.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
					Lang.ERROR_DB_EXIST);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(DistributedTradableI updated,
			DistributedTradableI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getConfirmedOwnerUserId(),
				old.getConfirmedOwnerUserId())) {
			if (old.getConfirmedOwnerUserId() != null) {
				if (!util.deleteDupSingle(confirmedUserIdToId,
						cnvL(old.getConfirmedOwnerUserId()),
						cnvL(old.getId())))
					return false;
			}
			if (!util.put(confirmedUserIdToId,
					cnvL(updated.getConfirmedOwnerUserId()),
					cnvL(updated.getId())))
				return false;
		}
		return true;
	}

}
