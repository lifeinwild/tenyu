package bei7473p5254d69jcuat.tenyu.release1.global.middle;

import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Glb.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.urlprovement.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
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
public class Middle extends IdObject implements GlbMemberDynamicState {
	public static Middle loadOrCreate() {
		return Glb.getDb(Glb.getFile().getMiddleDBPath())
				.computeInTransaction((txn) -> {
					Middle r = null;
					try {
						r = new MiddleStore(txn).get();
					} catch (Exception e) {
						Glb.getLogger().error("", e);
					}
					//ロード成功したらロードしたものを、失敗したら新しいのをrに設定
					r = r == null ? new Middle() : r;
					r.init();
					return r;
				});
	}

	private DistributedVoteManager distributedManager = new DistributedVoteManager();
	private EventManager eventManager = new EventManager();

	/**
	 * 自分のuserIdをオンメモリにしておく
	 */
	private transient Long myUserId;//微妙だがtransient。客観が同調処理で変化するので、設定されたidが間違いだった可能性がある。

	private transient ObjectivityCatchUp objeCatchUp;

	/**
	 * メインサーバのオンライン状態を定期的に確認し必要に応じてメインサーバを起動する
	 */
	private transient OnlineChecker onlineChecker = new OnlineChecker();

	private transient RatingGameMatchingServer ratingGameMatchingServer = new RatingGameMatchingServer();

	/**
	 * Role系サーバーの通信に成功したサーバーのユーザーIDのキャッシュ
	 */
	private Map<String,
			NodeIdentifierUser> serverCache = new ConcurrentHashMap<>();

	private transient URLProvementServer urlProvementServer = new URLProvementServer();

	/**
	 * ユーザーとアドレス・ポートの対応関係を全ノードに提供する
	 */
	private transient UserAddrServer userAddrServer;

	private UserEdgeList userEdgeList;

	/**
	 * ユーザーメッセージリストの拡散反映を行うP2Pシーケンス
	 */
	private transient ObjectivityUpdateSequence objectivityUpdateSequence;
	/**
	 * 主な客観更新手段としてUserMessageListがあり、
	 * ここに登録されるのはそれ以外の例外的な客観更新処理。
	 * 他のモジュールがここに処理を登録し、定期的に処理される。
	 */
	private ObjectivityUpdateElementList procFromOtherModules = new ObjectivityUpdateElementList();

	public void resetProcFromOtherModules() {
	}

	/**
	 * ユーザーメッセージを受け付けるメインサーバ
	 */
	private transient UserMessageListServer userMessageListServer;

	private UserRegistrationOfferList userRegistrationIntroduceOfferList;

	public ObjectivityUpdateElementList getProcFromOtherModules() {
		return procFromOtherModules;
	}

	private Middle() {
	}

	/**
	 * 自分のノード識別子のキャッシュ
	 * ノード識別子は内部にDBアクセスがありUserのキャッシュがあるので
	 * 使いまわすとDBアクセスが少し減る
	 */
	private transient NodeIdentifierUser myNodeIdentifier;

	public NodeIdentifierUser getMyNodeIdentifierUser() {
		if (myNodeIdentifier == null) {
			myNodeIdentifier = new NodeIdentifierUser(getMyUserId(),
					Glb.getConf().getNodeNumber());
		}
		return myNodeIdentifier;
	}

	/**
	 * @param roleName	Role名
	 * @return	キャッシュされたRole系サーバー
	 */
	public NodeIdentifierUser getCachedServer(String roleName) {
		return serverCache.get(roleName);
	}

	public DistributedVoteManager getDistributedManager() {
		return distributedManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public User getMe() {
		//userIdと異なりUser情報は変更される可能性があるのでキャッシュ不可
		return Glb.getObje().getUser(us -> us.get(getMyUserId()));
	}

	public Long getMyUserId() {
		if (myUserId == null)
			myUserId = UserStore.getMyIdSimple();
		return myUserId;
	}

	public ObjectivityCatchUp getObjeCatchUp() {
		return objeCatchUp;
	}

	public OnlineChecker getOnlineChecker() {
		return onlineChecker;
	}

	public RatingGameMatchingServer getRatingGameMatchingServer() {
		return ratingGameMatchingServer;
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

	public ObjectivityUpdateSequence getObjectivityUpdateSequence() {
		return objectivityUpdateSequence;
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

	public boolean save() {
		return Glb.getDb(Glb.getFile().getMiddleDBPath())
				.computeInTransaction((txn) -> {
					try {
						return new MiddleStore(txn).save(this);
					} catch (NoSuchAlgorithmException e) {
						Glb.getLogger().error("", e);
						return false;
					}
				});
	}

	public void setDistributedManager(
			DistributedVoteManager distributedManager) {
		this.distributedManager = distributedManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public void setObjeCatchUp(ObjectivityCatchUp objeCatchUp) {
		this.objeCatchUp = objeCatchUp;
	}

	public void setOnlineChecker(OnlineChecker onlineChecker) {
		this.onlineChecker = onlineChecker;
	}

	public void setRatingGameMatchingServer(
			RatingGameMatchingServer ratingGameMatchingServer) {
		this.ratingGameMatchingServer = ratingGameMatchingServer;
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

	public void setObjectivityUpdateSequence(
			ObjectivityUpdateSequence objectivityUpdateSequence) {
		this.objectivityUpdateSequence = objectivityUpdateSequence;
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

	@Override
	protected final boolean validateAtCreateIdObjectConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeIdObjectConcrete(ValidationResult r,
			Object old) {
		return true;
	}

	@Override
	protected final boolean validateAtUpdateIdObjectConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIdObjectConcrete(ValidationResult r,
			Transaction txn) {
		return true;
	}

	/**
	 * 客観状況
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static enum ObjectivityCircumstance {
		/**
		 * 混沌。客観は近傍において割れている。
		 * メッセージリストを拡散、反映しない。同調に専念する。
		 */
		CHAOS,
		DEFAULT,
		/**
		 * 平時。客観は近傍において概ね統一されている。
		 * メッセージリストを拡散、反映する。
		 */
		UNITY,
	}
}
