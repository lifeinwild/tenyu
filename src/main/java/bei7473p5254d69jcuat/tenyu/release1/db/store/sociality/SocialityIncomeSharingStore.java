package bei7473p5254d69jcuat.tenyu.release1.db.store.sociality;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class SocialityIncomeSharingStore
		extends ObjectivityObjectStore<SocialityIncomeSharingDBI,
				SocialityIncomeSharing> {
	public static final String modelName = SocialityIncomeSharing.class
			.getSimpleName();
	private static final StoreInfo receiverSocialityIdToId = new StoreInfo(
			modelName + "_receiverSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);
	private static final StoreInfo senderSocialityIdToId = new StoreInfo(
			modelName + "_senderSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public SocialityIncomeSharingStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected SocialityIncomeSharing chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof SocialityIncomeSharing)
				return (SocialityIncomeSharing) o;
			throw new InvalidTargetObjectTypeException(
					"not SocialityIncomeSharing object in SocialityIncomeSharingStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(
			SocialityIncomeSharingDBI o) throws Exception {
		if (!util.put(senderSocialityIdToId, cnvL(o.getSenderSocialityId()),
				cnvL(o.getRecycleId())))
			return false;
		if (!util.put(receiverSocialityIdToId, cnvL(o.getReceiverSocialityId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			SocialityIncomeSharingDBI updated, SocialityIncomeSharingDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getSenderSocialityId(),
				old.getSenderSocialityId())) {
			if (existBySenderSocialityId(updated.getSenderSocialityId(),
					updated.getRecycleId())) {
				r.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
						Lang.ERROR_DB_EXIST,
						"senderSocialityId=" + updated.getSenderSocialityId());
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getReceiverSocialityId(),
				old.getReceiverSocialityId())) {
			if (existByReceiverSocialityId(updated.getReceiverSocialityId(),
					updated.getRecycleId())) {
				r.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
						Lang.ERROR_DB_EXIST, "receiverSocialityId="
								+ updated.getReceiverSocialityId());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(
			SocialityIncomeSharingDBI o) throws Exception {
		if (!util.deleteDupSingle(senderSocialityIdToId,
				cnvL(o.getSenderSocialityId()), cnvL(o.getRecycleId())))
			return false;
		if (!util.deleteDupSingle(receiverSocialityIdToId,
				cnvL(o.getReceiverSocialityId()), cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByReceiverSocialityId(Long receiverSocialityId,
			Long id) {
		if (receiverSocialityId == null || id == null)
			return false;
		return util.getDupSingle(receiverSocialityIdToId,
				cnvL(receiverSocialityId), cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	public boolean existBySenderSocialityId(Long senderSocialityId, Long id) {
		if (senderSocialityId == null || id == null)
			return false;
		return util.getDupSingle(senderSocialityIdToId, cnvL(senderSocialityId),
				cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existObjectivityObjectConcrete(
			SocialityIncomeSharingDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existBySenderSocialityId(o.getSenderSocialityId(),
				o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID
							+ "=" + o.getSenderSocialityId());
			b = false;
		}
		if (!existByReceiverSocialityId(o.getReceiverSocialityId(),
				o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID
							+ "=" + o.getReceiverSocialityId());
			b = false;
		}
		return b;
	}

	public List<Long> getIdsByReceiverSocialityId(Long receiverSocialityId) {
		return util.getDup(receiverSocialityIdToId, cnvL(receiverSocialityId),
				v -> cnvL(v));
	}

	public List<Long> getIdsBySenderSocialityId(Long senderSocialityId) {
		return util.getDup(senderSocialityIdToId, cnvL(senderSocialityId),
				v -> cnvL(v));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(senderSocialityIdToId);
		r.add(receiverSocialityIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof SocialityIncomeSharingDBI;
	}

	@Override
	protected boolean noExistObjectivityObjectConcrete(
			SocialityIncomeSharingDBI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (existBySenderSocialityId(o.getSenderSocialityId(),
				o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByReceiverSocialityId(o.getReceiverSocialityId(),
				o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(
			SocialityIncomeSharingDBI updated, SocialityIncomeSharingDBI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getSenderSocialityId(),
				old.getSenderSocialityId())) {
			if (old.getSenderSocialityId() != null) {
				if (!util.deleteDupSingle(senderSocialityIdToId,
						cnvL(old.getSenderSocialityId()),
						cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(senderSocialityIdToId,
					cnvL(updated.getSenderSocialityId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getReceiverSocialityId(),
				old.getReceiverSocialityId())) {
			if (old.getReceiverSocialityId() != null) {
				if (!util.deleteDupSingle(receiverSocialityIdToId,
						cnvL(old.getReceiverSocialityId()),
						cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(receiverSocialityIdToId,
					cnvL(updated.getReceiverSocialityId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		return true;
	}

}
