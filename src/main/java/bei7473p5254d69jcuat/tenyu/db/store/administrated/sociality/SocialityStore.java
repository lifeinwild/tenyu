package bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.Conf.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class SocialityStore
		extends AdministratedObjectStore<SocialityI, Sociality> {
	public static final String modelName = Sociality.class.getSimpleName();
	/**
	 * individualityObjectIdとsocialityIdは1:1対応
	 */
	private static final StoreInfo individualityObjectIdToId = new StoreInfo(
			modelName + "_individualityObjectIdToId");

	public static Sociality getByIndividualityObjectStatic(
			StoreNameObjectivity type, Long individualityObjectConcreteId)
			throws Exception {
		return simple((s) -> s.getByIndividualityObject(
				new TenyuReferenceModelSimple<>(individualityObjectConcreteId,
						type)));
	}

	public static Sociality getByIndividualityObjectStatic(StoreNameSingle type)
			throws Exception {
		return simple((s) -> s.getByIndividualityObject(
				new TenyuReferenceModelSingle<>(type)));
	}

	public static Sociality getByIndividualityObjectStatic(
			byte[] individualityObjectStoreKey) {
		return simple((s) -> s.get(
				getIdByIndividualityObjectStatic(individualityObjectStoreKey)));
	}

	public static Sociality getByUserIdStatic(
			Long individualityObjectConcreteId) {
		return getByIndividualityObjectStatic(
				new TenyuReferenceModelSimple<>(individualityObjectConcreteId,
						StoreNameObjectivity.USER).getStoreKeyReferenced());
	}

	public static Long getIdByIndividualityObjectStatic(
			StoreNameObjectivity type, Long individualityObjectConcreteId) {
		return getIdByIndividualityObjectStatic(new TenyuReferenceModelSimple<>(
				individualityObjectConcreteId, type));
	}

	public static Long getIdByIndividualityObjectStatic(
			TenyuReferenceModelI<? extends IndividualityObjectI> ref) {
		return simple((s) -> s.getIdByIndividualityObject(ref.getStoreKeyReferenced()));
	}

	public static Long getIdByIndividualityObjectRefSingleStatic(
			StoreNameSingle type) {
		return getIdByIndividualityObjectStatic(
				new TenyuReferenceModelSingle<>(type));
	}

	public static Long getIdByIndividualityObjectStatic(
			byte[] individualityObjectStoreKey) {
		return simple(
				s -> s.getIdByIndividualityObject(individualityObjectStoreKey));
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public static Sociality getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	public static boolean isBanStatic(StoreNameObjectivity type,
			Long individualityObjectConcreteId) {
		return simple(s -> {
			try {
				return s.isBan(new TenyuReferenceModelSimple<>(
						individualityObjectConcreteId, type));
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
				return false;
			}
		});
	}

	public static boolean isBanStatic(StoreNameSingle type) {
		if (type != StoreNameSingle.OBJECTIVITY_CORE)
			throw new IllegalArgumentException();
		return simple(s -> {
			try {
				return s.isBan(new TenyuReferenceModelSingle<>(type));
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
				return false;
			}
		});
	}

	public static boolean isBlockStatic(StoreNameObjectivity type,
			Long individualityObjectConcreteId, Long userId) {
		return simple((s) -> {
			try {
				return s.isBlock(type, individualityObjectConcreteId, userId);
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
				return false;
			}
		});
	}

	private static <T, R> R simple(Function<SocialityStore, R> f) {
		return Glb.getObje().readRet(txn -> {
			try {
				SocialityStore s = new SocialityStore(txn);
				return f.apply(s);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	public SocialityStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected Sociality chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Sociality)
				return (Sociality) o;
			throw new InvalidTargetObjectTypeException(
					"not Sociality object in SocialityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createAdministratedObjectConcrete(SocialityI o)
			throws Exception {
		if (!util.put(individualityObjectIdToId,
				cnvBA(o.getIndividualityObjectStoreKey()), cnvL(o.getId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			SocialityI updated, SocialityI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getIndividualityObjectStoreKey(),
				old.getIndividualityObjectStoreKey())) {
			if (getIdByIndividualityObject(
					updated.getIndividualityObjectStoreKey()) != null) {
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
						Lang.ERROR_DB_EXIST, "individualityObjectConcreteRef="
								+ updated.getIndividualityObjectConcreteRef());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(SocialityI o)
			throws Exception {
		if (Glb.getConf().getRunlevel() == RunLevel.RELEASE) {
			//作者は削除不可
			if (o.getNodeType() == StoreNameObjectivity.USER
					&& o.getIndividualityObjectConcreteRef().getId() == Glb
							.getConst().getAuthor().getId())
				return false;

			//共同主体は削除不可
			if (o.getNodeType() == StoreNameSingle.OBJECTIVITY_CORE)
				return false;
		}
		if (!util.delete(individualityObjectIdToId,
				cnvBA(o.getIndividualityObjectStoreKey())))
			return false;

		return true;
	}

	@Override
	public boolean existAdministratedObjectConcrete(SocialityI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByIndividualityObject(
				o.getIndividualityObjectStoreKey()) == null) {
			vr.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	public Sociality getByIndividualityObject(
			byte[] individualityObjectStoreKey) {
		return get(getId(individualityObjectIdToId,
				cnvBA(individualityObjectStoreKey)));
	}

	public Sociality getByIndividualityObject(StoreNameObjectivity type,
			Long individualityObjectId) {
		return get(getIdByIndividualityObject(type, individualityObjectId));
	}

	public Sociality getByIndividualityObject(
			TenyuReferenceModelI<? extends IndividualityObjectI> ref) {
		return get(getIdByIndividualityObject(ref.getStoreKeyReferenced()));
	}

	public Long getIdByIndividualityObject(byte[] individualityObjectStoreKey) {
		return getId(individualityObjectIdToId,
				cnvBA(individualityObjectStoreKey));
	}

	public Long getIdByIndividualityObject(StoreNameObjectivity type,
			Long individualityObjectId) {
		return getIdByIndividualityObjectId(
				new TenyuReferenceModelSimple<>(individualityObjectId, type));
	}

	public Long getIdByIndividualityObjectId(
			TenyuReferenceModelI<? extends IndividualityObjectI> ref) {
		return getIdByIndividualityObject(ref.getStoreKeyReferenced());
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(individualityObjectIdToId);
		return r;
	}

	/**
	 * @param o	客観のモデル
	 * @return	行動可能なオブジェクトか
	 */
	public boolean isBan(IndividualityObjectI o) {
		try {
			return !isBan(o.getReference());
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
			return false;
		}
	}

	/**
	 * @param type	客観のモデル
	 * @param individualityObjectConcreteId
	 * @return	BANされているか何らかのエラーが発生した場合true
	 * @throws Exception
	 */
	public boolean isBan(StoreNameObjectivity type,
			Long individualityObjectConcreteId) throws Exception {
		return isBan(new TenyuReferenceModelSimple<>(
				individualityObjectConcreteId, type));
	}

	public boolean isBan(
			TenyuReferenceModelI<? extends IndividualityObjectI> ref)
			throws Exception {
		Sociality s = getByIndividualityObject(ref);
		//社会性が見つからない異常ユーザであっても、「BANされている」という明確な状態が見つからない限り
		//このメソッドはfalseを返す。存在しないユーザ等のチェックは他の部分でやるべき。
		if (s == null)
			return false;
		if (s.isBanned())
			return true;
		return false;
	}

	public boolean isBlock(StoreNameObjectivity type,
			Long individualityObjectConcreteId, Long userId) throws Exception {
		return isBlock(new TenyuReferenceModelSimple<>(
				individualityObjectConcreteId, type), userId);
	}

	/**
	 * userIdはindividualityObjectConcreteIdの社会性においてブロックされているか
	 * @param type
	 * @param individualityObjectConcreteId
	 * @param userId
	 * @return	ブロックされているか何らかのエラーが発生したらtrue
	 * @throws Exception
	 */
	public boolean isBlock(
			TenyuReferenceModelI<? extends IndividualityObjectI> ref,
			Long userId) throws Exception {
		Sociality s = getByIndividualityObject(ref);
		if (s == null) {
			Glb.getLogger().warn("", new IllegalStateException());
			return true;
		}
		if (s.getBlackList() == null)
			return false;//最初必ずnull
		if (s.getBlackList().contains(userId))
			return true;
		return false;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof SocialityI;
	}

	@Override
	public boolean noExistAdministratedObjectConcrete(SocialityI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByIndividualityObject(
				o.getIndividualityObjectStoreKey()) != null) {
			vr.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
					Lang.ERROR_DB_EXIST, "individualityObjectConcreteRef="
							+ o.getIndividualityObjectConcreteRef());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(SocialityI updated,
			SocialityI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getIndividualityObjectStoreKey(),
				old.getIndividualityObjectStoreKey())) {
			if (old.getIndividualityObjectStoreKey() != null) {
				if (!util.delete(individualityObjectIdToId,
						cnvBA(old.getIndividualityObjectStoreKey())))
					return false;
			}
			if (!util.put(individualityObjectIdToId,
					cnvBA(updated.getIndividualityObjectStoreKey()),
					cnvL(updated.getId())))
				return false;
		}

		return true;
	}

}
