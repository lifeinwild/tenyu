package bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class EdgeLogStore
		extends AdministratedObjectStore<EdgeLogI, EdgeLog> {
	public static final String modelName = EdgeLog.class.getSimpleName();
	private static final StoreInfo fromSocialityIdToId = new StoreInfo(
			modelName + "_fromSocialityIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);
	private static final StoreInfo toSocialityIdToId = new StoreInfo(
			modelName + "_toSocialityIdToId_Dup", StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public EdgeLogStore(Transaction txn) {
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
	protected boolean createAdministratedObjectConcrete(EdgeLogI o)
			throws Exception {
		if (!util.put(fromSocialityIdToId, cnvL(o.getFromSocialityId()),
				cnvL(o.getId())))
			return false;
		if (!util.put(toSocialityIdToId, cnvL(o.getToSocialityId()),
				cnvL(o.getId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			EdgeLogI updated, EdgeLogI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getFromSocialityId(),
				old.getFromSocialityId())) {
			if (existByFromSocialityId(updated.getFromSocialityId(),
					updated.getId())) {
				r.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getToSocialityId(),
				old.getToSocialityId())) {
			if (existByToSocialityId(updated.getToSocialityId(),
					updated.getId())) {
				r.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(EdgeLogI o)
			throws Exception {
		if (!util.deleteDupSingle(fromSocialityIdToId,
				cnvL(o.getFromSocialityId()), cnvL(o.getId())))
			return false;
		if (!util.deleteDupSingle(toSocialityIdToId, cnvL(o.getToSocialityId()),
				cnvL(o.getId())))
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
	protected boolean existAdministratedObjectConcrete(EdgeLogI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByFromSocialityId(o.getFromSocialityId(), o.getId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.ID + "=" + o.getId() + " "
							+ Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID + "="
							+ o.getFromSocialityId());
			b = false;
		}
		if (!existByToSocialityId(o.getToSocialityId(), o.getId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND,
					Lang.ID + "=" + o.getId() + " "
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
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(fromSocialityIdToId);
		r.add(toSocialityIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof EdgeLogI;
	}

	@Override
	protected boolean noExistAdministratedObjectConcrete(EdgeLogI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByFromSocialityId(o.getFromSocialityId(), o.getId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_FROM_SOCIALITYID,
					Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (existByToSocialityId(o.getToSocialityId(), o.getId())) {
			vr.add(Lang.SOCIALITY_EDGELOG_TO_SOCIALITYID, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(EdgeLogI updated,
			EdgeLogI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getFromSocialityId(),
				old.getFromSocialityId())) {
			if (old.getFromSocialityId() != null) {
				if (!util.deleteDupSingle(fromSocialityIdToId,
						cnvL(old.getFromSocialityId()), cnvL(old.getId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(fromSocialityIdToId,
					cnvL(updated.getFromSocialityId()), cnvL(updated.getId())))
				throw new IOException("Failed to updateSub");
		}
		if (Glb.getUtil().notEqual(updated.getToSocialityId(),
				old.getToSocialityId())) {
			if (old.getToSocialityId() != null) {
				if (!util.deleteDupSingle(toSocialityIdToId,
						cnvL(old.getToSocialityId()), cnvL(old.getId())))
					throw new IOException("Failed to updateSub");
			}
			if (!util.put(toSocialityIdToId, cnvL(updated.getToSocialityId()),
					cnvL(updated.getId())))
				throw new IOException("Failed to updateSub");
		}
		return true;
	}


}
