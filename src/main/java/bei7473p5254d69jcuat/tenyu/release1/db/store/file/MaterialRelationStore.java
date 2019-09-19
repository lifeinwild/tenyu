package bei7473p5254d69jcuat.tenyu.release1.db.store.file;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class MaterialRelationStore
		extends ObjectivityObjectStore<MaterialRelationDBI, MaterialRelation> {
	public static final String modelName = MaterialRelation.class
			.getSimpleName();
	private static final StoreInfo avatarIdToId = new StoreInfo(
			modelName + "_avatarIdToId_Dup", StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public MaterialRelationStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected MaterialRelation chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof MaterialRelation)
				return (MaterialRelation) o;
			throw new InvalidTargetObjectTypeException(
					"not MaterialRelation object in MaterialRelationStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(MaterialRelationDBI o)
			throws Exception {
		if (!util.put(avatarIdToId, cnvL(o.getAvatarId()),
				cnvL(o.getRecycleId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			MaterialRelationDBI updated, MaterialRelationDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getAvatarId(), old.getAvatarId())) {
			if (existByAvatar(updated.getAvatarId(), updated.getRecycleId())) {
				r.add(Lang.MATERIALRELATION_ID_BY_AVATAR, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(MaterialRelationDBI o)
			throws Exception {
		if (!util.deleteDupSingle(avatarIdToId, cnvL(o.getAvatarId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByAvatar(Long avatarId, Long materialId) {
		if (avatarId == null || materialId == null)
			return false;
		return util.getDupSingle(avatarIdToId, cnvL(avatarId), cnvL(materialId),
				(bi) -> cnvL(bi)) != null;
	}

	@Override
	public boolean existObjectivityObjectConcrete(MaterialRelationDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByAvatar(o.getAvatarId(), o.getRecycleId())) {
			vr.add(Lang.MATERIALRELATION_ID_BY_AVATAR, Lang.ERROR_DB_NOTFOUND,
					Lang.MATERIALRELATION + "=" + o.getRecycleId());
			b = false;
		}

		return b;
	}

	public List<Long> getIdsByAvatar(Long avatarId) {
		return util.getDup(avatarIdToId, cnvL(avatarId), (bi) -> cnvL(bi));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(avatarIdToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof MaterialRelationDBI;
	}

	@Override
	public boolean noExistObjectivityObjectConcrete(MaterialRelationDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByAvatar(o.getAvatarId(), o.getRecycleId())) {
			vr.add(Lang.MATERIALRELATION_ID_BY_AVATAR, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(
			MaterialRelationDBI updated, MaterialRelationDBI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getAvatarId(), old.getAvatarId())) {
			if (old.getAvatarId() != null) {
				if (!util.deleteDupSingle(avatarIdToId, cnvL(old.getAvatarId()),
						cnvL(old.getRecycleId())))
					return false;
			}
			if (!util.put(avatarIdToId, cnvL(updated.getAvatarId()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return true;
	}

}
