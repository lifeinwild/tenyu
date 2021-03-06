package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.other.*;
import glb.*;
import glb.Glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 主観と客観の中間。ここにある値はここにある間は主観値だが、
 * 後に客観値になるものが多い。
 * グローバル状態の一つ。
 *
 * 客観同調処理も、同調中の状態は主観値であり、最後に客観値になる。
 * 近傍リスト等の主観値とも区別されるのでここに入る。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Middle extends Model implements GlbMemberDynamicState, MiddleI {
	public static Middle loadOrCreate() {
		return Glb.getDb(Glb.getFile().getMiddleDBDir())
				.computeInTransaction((txn) -> {
					Middle r = null;
					try {
						MiddleStore s = new MiddleStore(txn);
						r = s.get(s.getDefaultId());
					} catch (Exception e) {
						Glb.getLogger().error("", e);
					}
					//ロード成功したらロードしたものを、失敗したら新しいのをrに設定
					r = r == null ? new Middle() : r;
					r.init();
					return r;
				});
	}

	private DistributedVoteManager distributedVoteManager = new DistributedVoteManager();

	private EventManager eventManager = new EventManager();

	private TenyuGuiHistory guiHistory = new TenyuGuiHistory();

	/**
	 * 自分のノード識別子のキャッシュ
	 * ノード識別子は内部にDBアクセスがありUserのキャッシュがあるので
	 * 使いまわすとDBアクセスが少し減る
	 */
	private transient NodeIdentifierUser myNodeIdentifier;

	/**
	 * 自分のuserIdをオンメモリにしておく
	 */
	private transient Long myUserId;//微妙だがtransient。客観が同調処理で変化するので、設定されたidが間違いだった可能性がある。

	private transient ObjectivityCatchUp objeCatchUp;

	/**
	 * ユーザーメッセージリストの拡散反映を行うP2Pシーケンス
	 */
	private transient ObjectivityUpdateSequence objectivityUpdateSequence;

	/**
	 * メインサーバのオンライン状態を定期的に確認し必要に応じてメインサーバを起動する
	 */
	private transient OnlineChecker onlineChecker = new OnlineChecker();

	/**
	 * 主な客観更新手段としてUserMessageListがあり、
	 * ここに登録されるのはそれ以外の例外的な客観更新処理。
	 * 他のモジュールがここに処理を登録し、定期的に処理される。
	 */
	private ObjectivityUpdateElementList procFromOtherModules = new ObjectivityUpdateElementList();

	private transient RatingGameMatchingServer ratingGameMatchingServer = new RatingGameMatchingServer();

	/**
	 * Role系サーバーの通信に成功したサーバーのユーザーIDのキャッシュ
	 */
	private Map<String,
			NodeIdentifierUser> serverCache = new ConcurrentHashMap<>();

	private Tenyupedia tenyupedia = new Tenyupedia();
	private transient URLProvementServer urlProvementServer = new URLProvementServer();

	/**
	 * ユーザーとアドレス・ポートの対応関係を全ノードに提供する
	 */
	private transient UserAddrServer userAddrServer;

	private UserEdgeList userEdgeList;

	/**
	 * ユーザーメッセージを受け付けるメインサーバ
	 */
	private transient UserMessageListServer userMessageListServer;

	private UserRegistrationOfferList userRegistrationIntroduceOfferList;

	private Middle() {
		setId(SingleObjectStoreI.getDefaultIdStatic());
		setHid(SingleObjectStoreI.getDefaultHidStatic());
	}

	/**
	 * @param roleName	Role名
	 * @return	キャッシュされたRole系サーバー
	 */
	public NodeIdentifierUser getCachedServer(String roleName) {
		return serverCache.get(roleName);
	}

	public DistributedVoteManager getDistributedVoteManager() {
		return distributedVoteManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	@Override
	public ModelGui<?, ?, ?, ?, ?, ?> getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new MiddleGui(guiName, cssIdPrefix);
	}

	public TenyuGuiHistory getGuiHistory() {
		return guiHistory;
	}

	public User getMe() {
		//userIdと異なりUser情報は変更される可能性があるのでキャッシュ不可
		return Glb.getObje().getUser(us -> us.get(getMyUserId()));
	}

	public NodeIdentifierUser getMyNodeIdentifierUser() {
		if (myNodeIdentifier == null) {
			myNodeIdentifier = new NodeIdentifierUser(getMyUserId(),
					Glb.getConf().getNodeNumber());
		}
		return myNodeIdentifier;
	}

	public Long getMyUserId() {
		if (myUserId == null)
			myUserId = UserStore.getMyIdSimple();
		return myUserId;
	}

	public Long getMyUserId(Transaction txn) {
		return new UserStore(txn).getMyId();
	}

	public ObjectivityCatchUp getObjeCatchUp() {
		return objeCatchUp;
	}

	public ObjectivityUpdateSequence getObjectivityUpdateSequence() {
		return objectivityUpdateSequence;
	}

	public OnlineChecker getOnlineChecker() {
		return onlineChecker;
	}

	public ObjectivityUpdateElementList getProcFromOtherModules() {
		return procFromOtherModules;
	}

	public RatingGameMatchingServer getRatingGameMatchingServer() {
		return ratingGameMatchingServer;
	}

	@Override
	public TenyuReferenceModelSingle<Middle> getReference() {
		return new TenyuReferenceModelSingle<>(StoreNameSingle.MIDDLE);
	}

	@Override
	public MiddleStore getStore(Transaction txn) {
		return new MiddleStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameSingle.MIDDLE;
	}

	public Tenyupedia getTenyupedia() {
		return tenyupedia;
	}

	public URLProvementServer getUrlProvementServer() {
		return urlProvementServer;
	}

	public UserAddrServer getUserAddrServer() {
		return userAddrServer;
	}

	public UserEdgeList getUserEdgeList() {
		return userEdgeList;
	}

	public UserMessageListServer getUserMessageListServer() {
		return userMessageListServer;
	}

	public UserRegistrationOfferList getUserRegistrationIntroduceList() {
		return userRegistrationIntroduceOfferList;
	}

	public void init() {
		if (userMessageListServer == null)
			userMessageListServer = new UserMessageListServer();
		if (onlineChecker == null)
			onlineChecker = new OnlineChecker();
		if (userEdgeList == null)
			userEdgeList = new UserEdgeList();
		if (userRegistrationIntroduceOfferList == null)
			userRegistrationIntroduceOfferList = new UserRegistrationOfferList();
		if (objeCatchUp == null)
			objeCatchUp = new ObjectivityCatchUp();
	}

	public void putCachedServer(String roleName, NodeIdentifierUser server) {
		serverCache.put(roleName, server);
	}

	public boolean removeCachedServer(String roleName,
			NodeIdentifierUser server) {
		return serverCache.remove(roleName, server);
	}

	public void resetProcFromOtherModules() {
	}

	public boolean save() {
		return Glb.getDb(Glb.getFile().getMiddleDBDir())
				.computeInTransaction((txn) -> new MiddleStore(txn).save(this));
	}

	public void setDistributedManager(
			DistributedVoteManager distributedManager) {
		this.distributedVoteManager = distributedManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public void setGuiHistory(TenyuGuiHistory guiHistory) {
		this.guiHistory = guiHistory;
	}

	public void setMyUserId(Long myUserId) {
		this.myUserId = myUserId;
	}

	public void setObjeCatchUp(ObjectivityCatchUp objeCatchUp) {
		this.objeCatchUp = objeCatchUp;
	}

	public void setObjectivityUpdateSequence(
			ObjectivityUpdateSequence objectivityUpdateSequence) {
		this.objectivityUpdateSequence = objectivityUpdateSequence;
	}

	public void setOnlineChecker(OnlineChecker onlineChecker) {
		this.onlineChecker = onlineChecker;
	}

	public void setRatingGameMatchingServer(
			RatingGameMatchingServer ratingGameMatchingServer) {
		this.ratingGameMatchingServer = ratingGameMatchingServer;
	}

	public void setTenyupedia(Tenyupedia tenyupedia) {
		this.tenyupedia = tenyupedia;
	}

	public void setUrlProvementServer(URLProvementServer urlProvementServer) {
		this.urlProvementServer = urlProvementServer;
	}

	public void setUserAddrServer(UserAddrServer userAddrServer) {
		this.userAddrServer = userAddrServer;
	}

	public void setUserEdgeList(UserEdgeList userEdgeList) {
		this.userEdgeList = userEdgeList;
	}

	public void setUserMessageListServer(
			UserMessageListServer userMessageListServer) {
		this.userMessageListServer = userMessageListServer;
	}

	public void setUserRegistrationIntroduceOfferList(
			UserRegistrationOfferList userRegistrationIntroduceOfferList) {
		this.userRegistrationIntroduceOfferList = userRegistrationIntroduceOfferList;
	}

	public void start() {
		if (userEdgeList != null)
			userEdgeList.start();
		if (objeCatchUp != null)
			objeCatchUp.start();
		if (guiHistory != null)
			guiHistory.start();
		startRoleServers();
	}

	/**
	 * Role系サーバーについて、起動すべきものを起動する
	 * 各サーバーでオンラインチェッカーへの登録処理をすると
	 * 他のサーバ候補のオンライン状態の変化に応じてサーバの起動処理が呼び出される。
	 * そこでも起動処理が呼び出される可能性があるが、
	 * それとは別に、全サーバについて自分がサーバ候補ならアプリ起動直後起動する。
	 * もし他に自分より優先度が高い候補がオンラインであることが分かったら、
	 * オンラインチェッカーから呼び出される起動処理の中で適切に終了される。
	 */
	public void startRoleServers() {
		if (onlineChecker != null) {
			if (onlineChecker.checkAndStartOrStop()) {
				if (userMessageListServer != null) {
					userMessageListServer.registerToOnlineChecker();
					userMessageListServer.startIfImCandidate();
				}
				if (ratingGameMatchingServer != null) {
					ratingGameMatchingServer.registerToOnlineChecker();
					ratingGameMatchingServer.startIfImCandidate();
				}
				if (urlProvementServer != null) {
					urlProvementServer.registerToOnlineChecker();
					urlProvementServer.startIfImCandidate();
				}
			}
		}
	}

	public void stop() {
		if (userMessageListServer != null)
			userMessageListServer.stop();
		if (ratingGameMatchingServer != null)
			ratingGameMatchingServer.stop();
		if (urlProvementServer != null)
			urlProvementServer.stop();
		if (onlineChecker != null)
			onlineChecker.stop();
		if (userEdgeList != null)
			userEdgeList.stop();
		if (objeCatchUp != null)
			objeCatchUp.stop();
		save();
	}

	private final boolean validateCommon(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtCreateModelConcrete(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeModelConcrete(ValidationResult r,
			Object old) {
		return true;
	}

	@Override
	protected final boolean validateAtUpdateModelConcrete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceModelConcrete(ValidationResult r,
			Transaction txn) {
		return true;
	}

	/**
	 * 客観の分裂が起きている事が予想されるか。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static enum ObjectivityCircumstance {
		/**
		 * 混沌。P2Pネットワークは分断しており
		 * 客観の分裂（矛盾した２つ以上の客観がそれぞれ多数のノードに信じられている状況）
		 * が生じていると予想される。
		 *
		 * この場合、メッセージ受付サーバによって客観更新が停止し、
		 * メッセージリストを拡散、反映しない。
		 * しばらくネットワークの回復を待ち同調に専念する。
		 */
		CHAOS,
		DEFAULT,
		/**
		 * 平時。客観はほぼ全ノードにおいて統一されている。
		 * 客観更新が可能であり、メッセージリストを拡散、反映する。
		 */
		UNITY,
	}
}
