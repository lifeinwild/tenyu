package bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
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

	public static Sociality getByIndividualityObjectIdSimple(byte[] individualityObjectId) {
		return simple((s) -> s.get(getIdByIndividualityObjectIdSimple(individualityObjectId)));
	}

	public static Sociality getByIndividualityObjectIdSimple(NodeType type,
			Long individualityObjectConcreteId) throws Exception {
		return simple((s) -> s.getByIndividualityObjectId(type, individualityObjectConcreteId));
	}

	public static Sociality getByUserIdSimple(Long individualityObjectConcreteId) {
		return getByIndividualityObjectIdSimple(Sociality
				.createIndividualityObjectId(NodeType.USER, individualityObjectConcreteId));
	}

	public static Long getIdByIndividualityObjectIdSimple(byte[] individualityObjectId) {
		return simple((s) -> s.getIdByIndividualityObject(individualityObjectId));
	}

	public static Long getIdByIndividualityObjectIdSimple(NodeType type,
			Long individualityObjectConcreteId) {
		return getIdByIndividualityObjectIdSimple(
				Sociality.createIndividualityObjectId(type, individualityObjectConcreteId));
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public static Sociality getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	public static boolean isBanStatic(NodeType type,
			Long individualityObjectConcreteId) {
		return simple(s -> {
			try {
				return s.isBan(type, individualityObjectConcreteId);
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
				return false;
			}
		});
	}

	public static boolean isBlockStatic(NodeType type,
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
		if (!util.put(individualityObjectIdToId, cnvBA(o.getIndividualityObjectId()),
				cnvL(o.getId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			SocialityI updated, SocialityI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getIndividualityObjectId(),
				old.getIndividualityObjectId())) {
			if (getIdByIndividualityObject(updated.getIndividualityObjectId()) != null) {
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID,
						Lang.ERROR_DB_EXIST);
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
			if (o.getNodeType() == NodeType.USER
					&& o.getIndividualityObjectConcreteId() == Glb.getConst()
							.getAuthor().getId())
				return false;

			//共同主体は削除不可
			if (o.getNodeType() == NodeType.COOPERATIVE_ACCOUNT)
				return false;
		}
		if (!util.remove(individualityObjectIdToId, cnvBA(o.getIndividualityObjectId())))
			return false;

		return true;
	}

	@Override
	public boolean existAdministratedObjectConcrete(SocialityI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByIndividualityObject(o.getIndividualityObjectId()) == null) {
			vr.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	public Sociality getByIndividuality(NodeType type, Long individualityObjectConcreteId) {
		return get(getIdByIndividualityObject(type, individualityObjectConcreteId));
	}

	public Sociality getByIndividualityObjectId(NodeType type,
			Long individualityObjectConcreteId) {
		return get(getIdByIndividualityObject(
				Sociality.createIndividualityObjectId(type, individualityObjectConcreteId)));
	}

	public Long getIdByIndividualityObject(byte[] individualityObjectId) {
		return getId(individualityObjectIdToId, cnvBA(individualityObjectId));
	}

	public Long getIdByIndividualityObject(NodeType type, Long individualityObjectConcreteId) {
		return getIdByIndividualityObject(
				Sociality.createIndividualityObjectId(type, individualityObjectConcreteId));
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
	 * Tenyu上で何らかの意味ある行動をする場合にBANされていないかチェックする。
	 * 型が分からない場合でも使用できる。
	 *
	 * @param o
	 * @return	行動可能なオブジェクトか
	 */
	public boolean isBan(Model o) {
		try {
			return !isBan(NodeType.getNodeType(o), o.getId());
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
			return false;
		}
	}

	/**
	 * @param type
	 * @param individualityObjectConcreteId
	 * @return	BANされているか何らかのエラーが発生した場合true
	 * @throws Exception
	 */
	public boolean isBan(NodeType type, Long individualityObjectConcreteId)
			throws Exception {
		Sociality s = getByIndividualityObjectId(type, individualityObjectConcreteId);
		//社会性が見つからない異常ユーザであっても、「BANされている」という明確な状態が見つからない限り
		//このメソッドはfalseを返す。存在しないユーザ等のチェックは他の部分でやるべき。
		if (s == null)
			return false;
		if (s.isBanned())
			return true;
		return false;
	}

	/**
	 * userIdはindividualityObjectConcreteIdの社会性においてブロックされているか
	 * @param type
	 * @param individualityObjectConcreteId
	 * @param userId
	 * @return	ブロックされているか何らかのエラーが発生したらtrue
	 * @throws Exception
	 */
	public boolean isBlock(NodeType type, Long individualityObjectConcreteId,
			Long userId) throws Exception {
		Sociality s = get(getIdByIndividualityObject(type, individualityObjectConcreteId));
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
		if (getIdByIndividualityObject(o.getIndividualityObjectId()) != null) {
			vr.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(SocialityI updated,
			SocialityI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getIndividualityObjectId(),
				old.getIndividualityObjectId())) {
			if (old.getIndividualityObjectId() != null) {
				if (!util.remove(individualityObjectIdToId,
						cnvBA(old.getIndividualityObjectId())))
					return false;
			}
			if (!util.put(individualityObjectIdToId, cnvBA(updated.getIndividualityObjectId()),
					cnvL(updated.getId())))
				return false;
		}

		return true;
	}

}
