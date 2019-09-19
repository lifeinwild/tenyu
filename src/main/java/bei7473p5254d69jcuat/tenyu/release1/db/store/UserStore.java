package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Conf.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * ユーザーはオフライン、PC、モバイル鍵でインデックスされる。
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserStore extends NaturalityStore<UserDBI, User> {
	/**
	 * Constの値を使わない。変更は考えられない。
	 */
	private static final String hashAlgorithm = "SHA-512";

	public static final String modelName = User.class.getSimpleName();

	private static final StoreInfo mobileToId = new StoreInfo(
			modelName + "_mobileToId");
	private static final StoreInfo offToId = new StoreInfo(
			modelName + "_offToId");

	//各鍵のハッシュ値：ユーザーID
	private static final StoreInfo pcToId = new StoreInfo(
			modelName + "_pcToId");

	/*
	public static User getByNameSimple(String name) {
		return simple((s) -> s.getByName(name));
	}
	*/

	public User getByName(String name) {
		Long id = getIdByName(name.trim());
		if (id == null)
			return null;
		return get(id);
	}

	/**
	 * 名前でユーザーIDを検索する
	 * @param name	トリムされる
	 * @return	該当したリサイクルID、無ければnull
	 */
	/*
	public static Long getIdByNameSimple(String name) {
		return simple((s) -> s.getIdByName(name.trim()));
	}
	*/

	/*
	public static Long getIdByPubKeySimple(KeyType type, byte[] pubKey) {
		return simple((s) -> s.getId(type, pubKey));
	}
	*/

	/**
	 * DBからuserIdを探し、あれば返す。
	 * @return
	 */
	/*
	public static Long getIdSimple(byte[] pubKey) {
		return simple((s) -> s.getIdByAny(pubKey));
	}
	*/

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/**
	 * @return	このノードのユーザーID
	 */
	public Long getMyId() {
		if (Glb.getConf() == null
				|| Glb.getConf().getMyOfflinePublicKey() == null)
			return null;
		return getId(KeyType.OFFLINE,
				Glb.getConf().getMyOfflinePublicKey().getEncoded());
	}

	public static Long getMyIdSimple() {
		if (Glb.getConf() == null
				|| Glb.getConf().getMyOfflinePublicKey() == null)
			return null;
		return Glb.getObje().getUser(us -> us.getId(KeyType.OFFLINE,
				Glb.getConf().getMyOfflinePublicKey().getEncoded()));
	}

	/*
	public static User getSimple(Long id) {
		return simple((s) -> s.get(id));
	}
	*/

	public static List<StoreInfo> getUserStoresStatic() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(mobileToId);
		r.add(offToId);
		r.add(pcToId);
		return r;
	}

	/*
	public static Map<String, Long> prefixSearchByNameSimple(String prefix,
			int max) {
		return simple((s) -> s.prefixSearchByNameRough(prefix.trim(), max));
	}
	*/
	/*
	private static <R> R simple(Function<UserStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new UserStore(txn)));
	}
	*/

	/**
	 * 外部で開始されたトランザクションを受け取る。内部で閉じない
	 *
	 * @throws NoSuchAlgorithmException
	 */
	public UserStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected User chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof User)
				return (User) o;
			throw new InvalidTargetObjectTypeException(
					"not User object in UserStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(UserDBI u) throws IOException {
		MessageDigest md = getMD();
		//鍵の書き込み
		byte[] pcHash = md.digest(u.getPcPublicKey());
		md.reset();
		byte[] mobileHash = md.digest(u.getMobilePublicKey());
		md.reset();
		byte[] offHash = md.digest(u.getOfflinePublicKey());
		md.reset();

		if (!util.put(pcToId, cnvBA(pcHash), cnvL(u.getRecycleId()))) {
			return false;
		}
		if (!util.put(mobileToId, cnvBA(mobileHash), cnvL(u.getRecycleId()))) {
			return false;
		}
		if (!util.put(offToId, cnvBA(offHash), cnvL(u.getRecycleId()))) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(UserDBI updated,
			UserDBI old, ValidationResult r) {
		boolean b = true;
		MessageDigest md = getMD();
		if (Glb.getUtil().notEqual(updated.getPcPublicKey(),
				old.getPcPublicKey())) {
			byte[] pcHashUpdated = md.digest(updated.getPcPublicKey());
			md.reset();
			Long id = getIdByPc(pcHashUpdated);
			if (id != null) {
				r.add(Lang.USER_PCKEY, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getMobilePublicKey(),
				old.getMobilePublicKey())) {
			byte[] mobileHashUpdated = md.digest(updated.getMobilePublicKey());
			md.reset();
			Long id = getIdByMobile(mobileHashUpdated);
			if (id != null) {
				r.add(Lang.USER_MOBILEKEY, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		if (Glb.getUtil().notEqual(updated.getOfflinePublicKey(),
				old.getOfflinePublicKey())) {
			byte[] offHashUpdated = md.digest(updated.getOfflinePublicKey());
			md.reset();
			Long id = getIdByOff(offHashUpdated);
			if (id != null) {
				r.add(Lang.USER_OFFKEY, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteNaturalityConcrete(UserDBI u) throws Exception {
		if (Glb.getConf().getRunlevel() == RunLevel.RELEASE) {
			//開発者ユーザーを削除してしまうと起動時に問題が出るので不可能に
			if (u.getRecycleId() == null || u.getRecycleId()
					.equals(Glb.getConst().getAuthor().getRecycleId()))
				return false;
		}
		MessageDigest md = getMD();
		byte[] pcHash = md.digest(u.getPcPublicKey());
		md.reset();
		byte[] mobileHash = md.digest(u.getMobilePublicKey());
		md.reset();
		byte[] offHash = md.digest(u.getOfflinePublicKey());
		md.reset();

		if (!util.remove(pcToId, cnvBA(pcHash)))
			return false;
		if (!util.remove(mobileToId, cnvBA(mobileHash)))
			return false;
		if (!util.remove(offToId, cnvBA(offHash)))
			return false;
		return true;
	}

	@Override
	public boolean existNaturalityConcrete(UserDBI u, ValidationResult vr) {
		boolean b = true;
		if (getIdByPc(u.getPcPublicKey()) == null) {
			vr.add(Lang.USER_PCKEY, Lang.ERROR_DB_NOTFOUND,
					Lang.NATURALITY_NAME + "=" + u.getName());
			b = false;
		}
		if (getIdByMobile(u.getMobilePublicKey()) == null) {
			vr.add(Lang.USER_MOBILEKEY, Lang.ERROR_DB_NOTFOUND,
					Lang.NATURALITY_NAME + "=" + u.getName());
			b = false;
		}
		if (getIdByOff(u.getOfflinePublicKey()) == null) {
			vr.add(Lang.USER_OFFKEY, Lang.ERROR_DB_NOTFOUND,
					Lang.NATURALITY_NAME + "=" + u.getName());
			b = false;
		}

		return b;
	}

	/**
	 * 引数の公開鍵を持つユーザーのIDを返す。
	 * @param type		条件とする公開鍵の種類
	 * @param pubKey	条件とする公開鍵
	 * @return			pubKeyを持つユーザーのID
	 */
	public Long getId(KeyType type, byte[] pubKey) {
		if (pubKey == null || type == null)
			return null;

		Long result = null;
		switch (type) {
		case PC:
			result = getIdByPc(pubKey);
			break;
		case MOBILE:
			result = getIdByMobile(pubKey);
			break;
		case OFFLINE:
			result = getIdByOff(pubKey);
			break;
		default:
		}
		return result;
	}

	/**
	 * DBからデータを読み出すのにいちいちexecuteInTransaction系を呼ぶのが嫌なので
	 * Simple系メソッドを用意する
	 *
	 * TODO:static以外に良い実装方法があるか？
	 * Store系クラスはコンストラクタでTransactionを受け取っているが、
	 * Simple系はTransactionを自前で作りコンストラクタを使う必要は無いのでstaticである。
	 * 抽象クラスでこのようなSimpleメソッドの規約化ができないという問題がある。
	 * 実際、それはさほど大きな問題ではない。
	 * Simple系メソッドは必要が生じたものだけ実装すれば十分だろう。
	 *
	 */

	/**
	 * 引数の公開鍵を持つユーザーのIDを返す。
	 * ユーザーは３種の公開鍵を持つがそれぞれの種類で順次検索していき
	 * 見つかったら返すので、KeyType指定型より効率が悪い。
	 * @param pubKey	条件とする公開鍵
	 * @return			条件を満たしたユーザーのID
	 */
	public Long getIdByAny(byte[] pubKey) {
		Long id = getIdByPc(Objects.requireNonNull(pubKey));
		if (id != null) {
			return id;
		}
		id = getIdByMobile(pubKey);
		if (id != null) {
			return id;
		}
		id = getIdByOff(pubKey);
		if (id != null) {
			return id;
		}
		return id;//null
	}

	/**
	 * モバイル鍵でユーザーを検索
	 * @param mobile
	 * @return
	 */
	public Long getIdByMobile(byte[] mobile) {
		MessageDigest md = getMD();
		byte[] hash = md.digest(mobile);
		md.reset();
		return getId(mobileToId, cnvBA(hash));
	}

	/**
	 * オフライン鍵でユーザーを検索
	 * @param off
	 * @return
	 */
	public Long getIdByOff(byte[] off) {
		MessageDigest md = getMD();
		byte[] hash = md.digest(off);
		md.reset();
		return getId(offToId, cnvBA(hash));
	}

	/**
	 * PC鍵でユーザーを検索
	 * @param pc
	 * @return
	 */
	public Long getIdByPc(byte[] pc) {
		MessageDigest md = getMD();
		byte[] hash = md.digest(pc);
		md.reset();
		return getId(pcToId, cnvBA(hash));
	}

	private MessageDigest getMD() {
		try {
			return MessageDigest.getInstance(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		return getUserStoresStatic();
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof UserDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistNaturalityConcrete(UserDBI u, ValidationResult vr) {
		boolean b = true;
		//ByAnyで検索する。例えばPC鍵がモバイル鍵ストアの方で重複してもいけない
		if (getIdByAny(u.getPcPublicKey()) != null) {
			vr.add(Lang.USER_PCKEY, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (getIdByAny(u.getMobilePublicKey()) != null) {
			vr.add(Lang.USER_MOBILEKEY, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (getIdByAny(u.getOfflinePublicKey()) != null) {
			vr.add(Lang.USER_OFFKEY, Lang.ERROR_DB_EXIST);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateNaturalityConcrete(UserDBI updated, UserDBI old)
			throws Exception {
		MessageDigest md = getMD();
		if (Glb.getUtil().notEqual(updated.getPcPublicKey(),
				old.getPcPublicKey())) {
			if (old.getPcPublicKey() != null) {
				byte[] pcHashOld = md.digest(old.getPcPublicKey());
				md.reset();
				if (!util.remove(pcToId, cnvBA(pcHashOld)))
					return false;
			}
			byte[] pcHashUpdated = md.digest(updated.getPcPublicKey());
			md.reset();
			if (!util.put(pcToId, cnvBA(pcHashUpdated),
					cnvL(updated.getRecycleId())))
				return false;
		}
		if (Glb.getUtil().notEqual(updated.getMobilePublicKey(),
				old.getMobilePublicKey())) {
			if (old.getMobilePublicKey() != null) {
				byte[] mobileHashOld = md.digest(old.getMobilePublicKey());
				md.reset();
				if (!util.remove(mobileToId, cnvBA(mobileHashOld)))
					return false;
			}
			byte[] mobileHashUpdated = md.digest(updated.getMobilePublicKey());
			md.reset();
			if (!util.put(mobileToId, cnvBA(mobileHashUpdated),
					cnvL(updated.getRecycleId())))
				return false;
		}
		if (Glb.getUtil().notEqual(updated.getOfflinePublicKey(),
				old.getOfflinePublicKey())) {
			if (old.getOfflinePublicKey() != null) {
				byte[] offHashOld = md.digest(old.getOfflinePublicKey());
				md.reset();
				if (!util.remove(offToId, cnvBA(offHashOld)))
					return false;
			}
			byte[] offHashUpdated = md.digest(updated.getOfflinePublicKey());
			md.reset();
			if (!util.put(offToId, cnvBA(offHashUpdated),
					cnvL(updated.getRecycleId())))
				return false;

		}

		return true;
	}
}
