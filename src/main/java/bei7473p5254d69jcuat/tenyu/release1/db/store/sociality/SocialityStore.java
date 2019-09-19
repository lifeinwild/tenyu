package bei7473p5254d69jcuat.tenyu.release1.db.store.sociality;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.function.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Conf.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class SocialityStore
		extends ObjectivityObjectStore<SocialityDBI, Sociality> {
	public static final String modelName = Sociality.class.getSimpleName();
	/**
	 * naturalityIdとsocialityIdは1:1対応
	 */
	private static final StoreInfo naturalityIdToId = new StoreInfo(
			modelName + "_naturalityIdToId");

	public static Sociality getByNaturalityIdSimple(byte[] naturalityId) {
		return simple((s) -> s.get(getIdByNaturalityIdSimple(naturalityId)));
	}

	public static Sociality getByNaturalityIdSimple(NodeType type,
			Long naturalityConcreteId) throws Exception {
		return simple((s) -> s.getByNaturalityId(type, naturalityConcreteId));
	}

	public static Sociality getByUserIdSimple(Long naturalityConcreteId) {
		return getByNaturalityIdSimple(Sociality
				.createNaturalityId(NodeType.USER, naturalityConcreteId));
	}

	public static Long getIdByNaturalityIdSimple(byte[] naturalityId) {
		return simple((s) -> s.getIdByNaturality(naturalityId));
	}

	public static Long getIdByNaturalityIdSimple(NodeType type,
			Long naturalityConcreteId) {
		return getIdByNaturalityIdSimple(
				Sociality.createNaturalityId(type, naturalityConcreteId));
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public static Sociality getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	public static boolean isBanStatic(NodeType type,
			Long naturalityConcreteId) {
		return simple(s -> {
			try {
				return s.isBan(type, naturalityConcreteId);
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
				return false;
			}
		});
	}

	public static boolean isBlockStatic(NodeType type,
			Long naturalityConcreteId, Long userId) {
		return simple((s) -> {
			try {
				return s.isBlock(type, naturalityConcreteId, userId);
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

	public SocialityStore(Transaction txn) throws NoSuchAlgorithmException {
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
	protected boolean createObjectivityObjectConcrete(SocialityDBI o)
			throws Exception {
		if (!util.put(naturalityIdToId, cnvBA(o.getNaturalityId()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			SocialityDBI updated, SocialityDBI old, ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getNaturalityId(),
				old.getNaturalityId())) {
			if (getIdByNaturality(updated.getNaturalityId()) != null) {
				r.add(Lang.SOCIALITY_NATURALITY_CONCRETE_ID,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(SocialityDBI o)
			throws Exception {
		if (Glb.getConf().getRunlevel() == RunLevel.RELEASE) {
			//作者は削除不可
			if (o.getNodeType() == NodeType.USER
					&& o.getNaturalityConcreteRecycleId() == Glb.getConst()
							.getAuthor().getRecycleId())
				return false;

			//共同主体は削除不可
			if (o.getNodeType() == NodeType.COOPERATIVE_ACCOUNT)
				return false;
		}
		if (!util.remove(naturalityIdToId, cnvBA(o.getNaturalityId())))
			return false;

		return true;
	}

	@Override
	public boolean existObjectivityObjectConcrete(SocialityDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByNaturality(o.getNaturalityId()) == null) {
			vr.add(Lang.SOCIALITY_NATURALITY_CONCRETE_ID,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	public Sociality getByNaturality(NodeType type, Long naturalityConcreteId) {
		return get(getIdByNaturality(type, naturalityConcreteId));
	}

	public Sociality getByNaturalityId(NodeType type,
			Long naturalityConcreteId) {
		return get(getIdByNaturality(
				Sociality.createNaturalityId(type, naturalityConcreteId)));
	}

	public Long getIdByNaturality(byte[] naturalityId) {
		return getId(naturalityIdToId, cnvBA(naturalityId));
	}

	public Long getIdByNaturality(NodeType type, Long naturalityConcreteId) {
		return getIdByNaturality(
				Sociality.createNaturalityId(type, naturalityConcreteId));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(naturalityIdToId);
		return r;
	}

	/**
	 * Tenyu上で何らかの意味ある行動をする場合にBANされていないかチェックする。
	 * 型が分からない場合でも使用できる。
	 *
	 * @param o
	 * @return	行動可能なオブジェクトか
	 */
	public boolean isBan(IdObject o) {
		try {
			return !isBan(NodeType.getNodeType(o), o.getRecycleId());
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
			return false;
		}
	}

	/**
	 * @param type
	 * @param naturalityConcreteId
	 * @return	BANされているか何らかのエラーが発生した場合true
	 * @throws Exception
	 */
	public boolean isBan(NodeType type, Long naturalityConcreteId)
			throws Exception {
		Sociality s = getByNaturalityId(type, naturalityConcreteId);
		//社会性が見つからない異常ユーザであっても、「BANされている」という明確な状態が見つからない限り
		//このメソッドはfalseを返す。存在しないユーザ等のチェックは他の部分でやるべき。
		if (s == null)
			return false;
		if (s.isBanned())
			return true;
		return false;
	}

	/**
	 * userIdはnaturalityConcreteIdの社会性においてブロックされているか
	 * @param type
	 * @param naturalityConcreteId
	 * @param userId
	 * @return	ブロックされているか何らかのエラーが発生したらtrue
	 * @throws Exception
	 */
	public boolean isBlock(NodeType type, Long naturalityConcreteId,
			Long userId) throws Exception {
		Sociality s = get(getIdByNaturality(type, naturalityConcreteId));
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
		return o instanceof SocialityDBI;
	}

	@Override
	public boolean noExistObjectivityObjectConcrete(SocialityDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByNaturality(o.getNaturalityId()) != null) {
			vr.add(Lang.SOCIALITY_NATURALITY_CONCRETE_ID, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(SocialityDBI updated,
			SocialityDBI old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getNaturalityId(),
				old.getNaturalityId())) {
			if (old.getNaturalityId() != null) {
				if (!util.remove(naturalityIdToId,
						cnvBA(old.getNaturalityId())))
					return false;
			}
			if (!util.put(naturalityIdToId, cnvBA(updated.getNaturalityId()),
					cnvL(updated.getRecycleId())))
				return false;
		}

		return true;
	}

}
