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

public class EdgeLogStore extends ObjectivityObjectStore<EdgeLogDBI, EdgeLog> {
	public static final String modelName = EdgeLog.class.getSimpleName();
	private static final StoreInfo fromSocialityIdToId = new StoreInfo(
			modelName + "_fromSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);
	private static final StoreInfo toSocialityIdToId = new StoreInfo(
			modelName + "_toSocialityIdToId_Dup", StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public EdgeLogStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected EdgeLog chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof EdgeLog)
				return (EdgeLog) o;
			throw new InvalidTargetObjectTypeException(
					"not EdgeLog object in EdgeLogStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(EdgeLogDBI o)
			throws Exception {
		if (!util.put(fromSocialityIdToId, cnvL(o.getFromSocialityId()),
				cnvL(o.getRecycleId())))
			return false;
		if (!util.put(toSocialityIdToId, cnvL(o.getToSocialityId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			EdgeLogDBI updated, EdgeLogDBI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getFromSocialityId(),
				old.getFromSocialityId())) {
			if (existByFromSocialityId(updated.getFromSocialityId(),
					updated.getRecycleId())) {
				r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getToSocialityId(),
				old.getToSocialityId())) {
			if (existByToSocialityId(updated.getToSocialityId(),
					updated.getRecycleId())) {
				r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(EdgeLogDBI o)
			throws Exception {
		if (!util.deleteDupSingle(fromSocialityIdToId,
				cnvL(o.getFromSocialityId()), cnvL(o.getRecycleId())))
			return false;
		if (!util.deleteDupSingle(toSocialityIdToId, cnvL(o.getToSocialityId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByFromSocialityId(Long fromSocialityId, Long id) {
		if (fromSocialityId == null || id == null)
			return false;
		return util.getDupSingle(fromSocialityIdToId, cnvL(fromSocialityId),
				cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	public boolean existByToSocialityId(Long toSocialityId, Long id) {
		if (toSocialityId == null || id == null)
			return false;
		return util.getDupSingle(toSocialityIdToId, cnvL(toSocialityId),
				cnvL(id), (bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existObjectivityObjectConcrete(EdgeLogDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByFromSocialityId(o.getFromSocialityId(), o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID + "="
							+ o.getFromSocialityId());
			b = false;
		}
		if (!existByToSocialityId(o.getToSocialityId(), o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId() + " "
							+ Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID + "="
							+ o.getToSocialityId());
			b = false;
		}
		return b;
	}

	public List<Long> getIdsByFromSocialityId(Long fromSocialityId) {
		return util.getDup(fromSocialityIdToId, cnvL(fromSocialityId),
				v -> cnvL(v));
	}

	public List<Long> getIdsByToSocialityId(Long toSocialityId) {
		return util.getDup(toSocialityIdToId, cnvL(toSocialityId),
				v -> cnvL(v));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(fromSocialityIdToId);
		r.add(toSocialityIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof EdgeLogDBI;
	}

	@Override
	protected boolean noExistObjectivityObjectConcrete(EdgeLogDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByFromSocialityId(o.getFromSocialityId(), o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByToSocialityId(o.getToSocialityId(), o.getRecycleId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(EdgeLogDBI updated,
			EdgeLogDBI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getFromSocialityId(),
				old.getFromSocialityId())) {
			if (old.getFromSocialityId() != null) {
				if (!util.deleteDupSingle(fromSocialityIdToId,
						cnvL(old.getFromSocialityId()),
						cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(fromSocialityIdToId,
					cnvL(updated.getFromSocialityId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getToSocialityId(),
				old.getToSocialityId())) {
			if (old.getToSocialityId() != null) {
				if (!util.deleteDupSingle(toSocialityIdToId,
						cnvL(old.getToSocialityId()), cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(toSocialityIdToId, cnvL(updated.getToSocialityId()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to updateSub");
		}
		return true;
	}

}
