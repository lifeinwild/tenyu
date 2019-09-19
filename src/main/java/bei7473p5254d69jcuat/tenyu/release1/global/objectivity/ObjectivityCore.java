package bei7473p5254d69jcuat.tenyu.release1.global.objectivity;

import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 客観のうち、サイズが限定的でストアクラスを持たないデータの集まり。
 *
 * 現状ObjectivityCoreは共同主体の自然性を意味している。
 * DBに入れるほどではないデータをここに置いているだけだが、
 * しかし共同主体とObjectivityCoreを分ける十分な理由も見つからない。
 *
 * 処理内容が非常に単純で深いメソッド呼び出しをしない、デッドロックの可能性が無い、
 * と考えれるpublicメソッドはsynchronizedをつける。
 * そうでないメソッドはデッドロックのリスクを考慮してsynchronizedをつけない。
 * もしかしたら並列処理の問題で仕様上不可能な状態を見せる場合があるかもしれないが、
 * 本アプリの分散合意や同調の性質から、問題無い。
 *
 * 対応する社会性が存在する事からNaturalityを継承している。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityCore extends Naturality implements ChainVersionup {
	public static final long firstHistoryIndex = 0;

	/**
	 * HistoryIndexをインクリメントする。
	 * @return	インクリメントされたHistoryIndex
	 */
	public long incrementHistoryIndex() {
		return ++historyIndex;
	}

	/**
	 * 初期通貨発行量
	 */
	private static final long initCurrencyAmount = 1000L * 1000;

	/**
	 * 1週間あたりの最大通貨発行量
	 */
	private static final float maxCreateCurrencyPercentWeekly = 0.1F;

	/**
	 * 客観コアもノードで、その種類
	 */
	public static final NodeType nodeType = NodeType.COOPERATIVE_ACCOUNT;

	/**
	 * IdObjectにするため
	 */
	private static final long objeId = 0;

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;
	}

	public static long getInitcurrencyamount() {
		return initCurrencyAmount;
	}

	public static float getMaxcreatecurrencypercentweekly() {
		return maxCreateCurrencyPercentWeekly;
	}

	public static long getObjeid() {
		return objeId;
	}

	/**
	 * DBからロードするか新規作成して返す
	 * @return	ロードされた客観
	 */
	public static ObjectivityCore loadOrCreate(Transaction txn) {
		ObjectivityCore r = null;
		//DBにあれば読み出す
		try {
			r = new ObjectivityCoreStore(txn).get();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

		//DBに無ければ新規作成
		if (r == null) {
			r = new ObjectivityCore();
		}
		return r;
	}

	/**
	 * 議決で設定可能な値
	 */
	private ObjectivityCoreConfig config = new ObjectivityCoreConfig();

	/**
	 * 共同主体の所持金
	 */
	private Wallet cooperativeCapitalWallet = new Wallet();

	/**
	 * 分散取引可能物のIDジェネレータ
	 */
	private AtomicLong distributedTradableIdGenerator = new AtomicLong();

	/**
	 * メッセージリストを反映するたびにインクリメントされる
	 */
	private long historyIndex = 0L;

	/**
	 * 最新の可決された基盤ソフトウェアの情報
	 */
	private TenyuPlatformSoftware latestAcceptedPlatformSoftware;

	/**
	 * 全体運営者一覧
	 */
	private TenyuManagerList managerList = new TenyuManagerList();

	/**
	 * loadOrCreate()を使うのでコンストラクタは外部から使われない
	 */
	protected ObjectivityCore() {
		name = "Cooperative Account";
		explanation = "The central node of flow network";
		recycleId = getObjeid();
		latestAcceptedPlatformSoftware = new TenyuPlatformSoftware();
		latestAcceptedPlatformSoftware.setRelease(1);
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#clone()
	 *
	 * クローンに書き込んでトランザクションが成功したら置き換える。
	 * DBじゃないのでトランザクション機構が無いので
	 */
	public synchronized ObjectivityCore clone() {
		try {
			byte[] serialized = Glb.getUtil().toKryoBytesForPersistence(this);
			return (ObjectivityCore) Glb.getUtil()
					.fromKryoBytesForPersistence(serialized);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return null;//削除されない
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return managerList.getManagerIds();
	}

	public ObjectivityCoreConfig getConfig() {
		return config;
	}

	public Wallet getCooperativeCapitalWallet() {
		return cooperativeCapitalWallet;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public TenyuPlatformSoftware getLatestAcceptedPlatformSoftware() {
		return latestAcceptedPlatformSoftware;
	}

	public TenyuManagerList getManagerList() {
		return managerList;
	}

	public long getNextDistributedTradableId() {
		return distributedTradableIdGenerator.incrementAndGet();
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectDBI.getNullId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectDBI.getNullId();
	}

	/**
	 * @return	客観コアのハッシュ値
	 * このメソッドの実行中に客観コアが更新されるとまずい
	 *
	 */
	public synchronized byte[] hash() {
		return Glb.getUtil().hash(this);
	}

	/**
	 * @param userId
	 * @return	userIdは一部のサーバである事を理由として
	 * ユーザーメッセージ受付サーバに重い負荷をかける事が認められるか
	 */
	public boolean isHeavyLoadServerToUserMessageListServer(Long userId) {
		boolean r = Glb.getObje().compute(txn -> {
			try {
				RoleStore rs = new RoleStore(txn);

				//レーティングゲームのマッチングサーバーか
				Role matching = rs.getByName(
						RatingGameMatchingServer.class.getSimpleName());
				if (matching.isAdmin(userId))
					return true;

				//常駐空間ゲームのサーバーか
				StaticGameStore sgs = new StaticGameStore(txn);
				List<Long> gameServers = sgs.getIdsByServerUserId(userId);
				if (gameServers != null && gameServers.size() > 0) {
					return true;
				}

				return false;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
		return r;
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	/**
	 * 客観をDBに記録する。
	 * @return	記録に成功したか
	 */
	/* トランザクションを無意味にするようなコードを書きやすそうなので廃止
	public boolean save() {
		return Glb.getObje().compute(txn -> save(txn));
	}
	*/

	public boolean save(Transaction txn) {
		ObjectivityCore c = this;
		try {
			return new ObjectivityCoreStore(txn).save(c);
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public void setConfig(ObjectivityCoreConfig config) {
		this.config = config;
	}

	public synchronized void setCooperativeCapitalWallet(
			Wallet cooperativeCapitalWallet) {
		this.cooperativeCapitalWallet = cooperativeCapitalWallet;
	}

	public synchronized void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public void setLatestAcceptedPlatformSoftware(
			TenyuPlatformSoftware latestAcceptedPlatformSoftware) {
		this.latestAcceptedPlatformSoftware = latestAcceptedPlatformSoftware;
	}

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (config == null) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG, Lang.ERROR_EMPTY);
			b = false;
		}
		if (cooperativeCapitalWallet == null) {
			vr.add(Lang.OBJECTIVITY_CORE_WALLET, Lang.ERROR_EMPTY);
			b = false;
		}
		if (distributedTradableIdGenerator == null) {
			vr.add(Lang.OBJECTIVITY_CORE_DISTRIBUTEDTRADABLE_IDGENERATOR,
					Lang.ERROR_EMPTY);
			b = false;
		}
		if (historyIndex < firstHistoryIndex) {
			vr.add(Lang.OBJECTIVITY_CORE_HISTORYINDEX, Lang.ERROR_EMPTY);
			b = false;
		}
		if (latestAcceptedPlatformSoftware == null) {
			vr.add(Lang.OBJECTIVITY_CORE_LATESTACCEPTED_PLATFORMSOFTWARE,
					Lang.ERROR_EMPTY);
			b = false;
		}
		if (managerList == null) {
			vr.add(Lang.OBJECTIVITY_CORE_MANAGERLIST, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	public final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		/*	falseが返ると記録されないので、検証処理をしない
		if (roleManager == null || managers == null || sharingRate < 0
				|| historyIndex < 0)
			return false;
		 */
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			if (!config.validateAtCreate(r)) {
				b = false;
			}
			if (!cooperativeCapitalWallet.validateAtCreate(r)) {
				b = false;
			}
			if (!latestAcceptedPlatformSoftware.validateAtCreate(r)) {
				b = false;
			}
			if (!managerList.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdateChangeNaturalityConcrete(ValidationResult r,
			Object old) {
		if (!(old instanceof ObjectivityCore)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//ObjectivityCore old2 = (ObjectivityCore) old;

		boolean b = true;
		return b;
	}

	@Override
	public final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			if (!config.validateAtUpdate(r)) {
				b = false;
			}
			if (!cooperativeCapitalWallet.validateAtUpdate(r)) {
				b = false;
			}
			if (!latestAcceptedPlatformSoftware.validateAtUpdate(r)) {
				b = false;
			}
			if (!managerList.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		/*	同調処理を考えるとコアは参照検証できない。
		if (!config.validateReference(r, txn)) {
			b = false;
		}
		if (!cooperativeCapitalWallet.validateReference(r, txn)) {
			b = false;
		}
		if (!latestAcceptedPlatformSoftware.validateReference(r, txn)) {
			b = false;
		}
		if (!managerList.validateReference(r, txn)) {
			b = false;
		}
		*/
		return b;
	}

}
