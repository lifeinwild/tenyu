package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.IsOnline.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import glb.*;

/**
 * 他ノードのオンライン状態を定期的に確認する。
 * サーバ候補のノードだけが実行する。
 *
 * 他のモジュールはこのモジュールを通して他ノードのオンライン状態を確認できる。
 * 例えば現在オンラインのノードの中で1ノードだけが起動すべきサーバ
 * があり、どのノードがそのサーバを起動するか優先順位がある場合、
 * どのノードがオンラインかをチェックする必要がある。
 *
 * 典型的には引継ぎ型サーバの引継ぎ処理を実現するために利用される
 *
 * チェックされるのはノードのオンライン状態であり、
 * その情報は全モジュールで共通である。
 * 一方で他ノードのオンライン状態が変化した時に呼び出される処理はモジュール毎に異なる。
 * 各モジュールはチェック対象のノードと呼び出される処理を登録する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class OnlineChecker extends StartableObject {

	/**
	 * 他のサーバのオンライン状態をチェックするスレッド
	 */
	protected ScheduledFuture<?> onlineCheckThread = null;

	/**
	 * 他のサーバーのオンライン状態
	 * NodeIdentifierUser : online state
	 */
	protected Map<NodeIdentifierUser,
			Boolean> onlineStates = new ConcurrentHashMap<>();

	/**
	 * 他モジュールがOnlineCheckerを利用する時に要素を追加する
	 * OnlineChckerを利用するモジュールの名前:モジュール毎の設定
	 *
	 * オンライン状態のチェックは全ノードに行われるのではなく
	 * 一部ノードに対して行われ、そのチェック対象のノード一覧が
	 * OnlineCheckerを利用する側で決まり、動的にその一覧が変化しうるので、
	 * その一覧を取得する処理を利用側から登録する必要がある。
	 */
	protected Map<String,
			OnlineCheckerFuncs> callbacks = new ConcurrentHashMap<>();

	/**
	 * 他のモジュールがオンラインチェック機能を利用する場合、主にこのインターフェースを使用する
	 * @param key
	 * @param callback
	 * @return
	 */
	public boolean register(String key, OnlineCheckerFuncs callback) {
		if (callbacks.size() > 100 || key == null || callback == null
				|| !callback.validate())
			return false;
		callbacks.put(key, callback);
		return true;
	}

	/**
	 * @param identifier
	 * @return	オンラインか。未チェックならnull
	 */
	public Boolean isOnline(NodeIdentifierUser identifier) {
		return onlineStates.get(identifier);
	}

	/**
	 * 客観の値に応じてOnlineCheckerを起動する
	 * @return	このメソッド呼び出しで起動されたか
	 * もともと起動されていた場合false
	 */
	public boolean checkAndStartOrStop() {
		boolean imServer = imServer();

		if (!started && imServer) {
			//起動していなくて起動すべき場合
			return start();
		} else if (started && !imServer) {
			//起動していて起動してはいけない場合
			stop();
		}
		return false;
	}

	/**
	 * @return	チェック対象の全ユーザーID一覧
	 */
	public HashSet<NodeIdentifierUser> getAllCheckedNode() {
		HashSet<NodeIdentifierUser> r = new HashSet<>();
		for (OnlineCheckerFuncs callback : callbacks.values()) {
			List<NodeIdentifierUser> tmp = callback.getGetCheckUserIds().get();
			if (tmp == null || tmp.size() == 0)
				continue;
			r.addAll(tmp);
		}
		return r;
	}

	/**
	 * 自分は引継ぎ型サーバーの候補か
	 */
	public boolean imServer() {
		NodeIdentifierUser identifier = Glb.getMiddle()
				.getMyNodeIdentifierUser();
		return isRoleServer(identifier);
	}

	/**
	 * 全Roleを走査して、さらに各Roleの権限者一覧を走査して、
	 * さらに各権限者のUser情報の役割とノード番号の対応関係一覧を走査して、
	 * 指定されたユーザーIDとノード番号がいずれかの役割が割り当てられているか調べる。
	 * 例えば、割り当てられているならオンライン状態のチェック対象になる。
	 *
	 * @param identifier	ユーザーIDとノード番号
	 * @return	指定されたノードは引継ぎ型サーバーの候補か
	 */
	public boolean isRoleServer(NodeIdentifierUser identifier) {
		try {
			return Glb.getObje().compute(txn -> {
				try {
					UserStore us = new UserStore(txn);
					List<Role> roles = Glb.getObje()
							.getRole(rs -> rs.getAllValues());
					for (Role r : roles) {
						ids: for (Long userId : r.getAdminUserIds()) {
							if (!userId.equals(identifier.getUserId())) {
								continue;
							}
							User u = us.get(userId);
							if (u == null)
								break ids;
							if (u.isRelatedToRole(identifier.getNodeNumber())) {
								return true;
							} else {
								break ids;
							}
						}
					}
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
				return false;
			});
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return false;
	}

	/**
	 * 客観状態を参照してメインサーバーまたはサブサーバーとしての動作を開始する
	 * @return	started
	 */
	public synchronized boolean start() {
		if (!super.start())
			return false;

		Glb.getLogger().info("started as user message list sub server");

		//自分がサーバー役なら、自分のオンライン状態をtrueにする。
		NodeIdentifierUser myIdentifier = Glb.getMiddle()
				.getMyNodeIdentifierUser();
		if (myIdentifier == null)
			return false;
		Long myUserId = myIdentifier.getUserId();
		if (myUserId == null)
			return false;
		if (isRoleServer(myIdentifier)) {
			onlineStates.put(myIdentifier, true);
		}

		//定期確認スレッド
		if (onlineCheckThread != null && !onlineCheckThread.isCancelled())
			onlineCheckThread.cancel(false);
		long periodic = 5000;
		onlineCheckThread = Glb.getExecutorPeriodic()
				.scheduleAtFixedRate(() -> {
					//通信状態
					List<RequestFutureUser> states = new ArrayList<>();
					NodeIdentifierUser me = Glb.getMiddle()
							.getMyNodeIdentifierUser();
					for (NodeIdentifierUser serverNode : getAllCheckedNode()) {
						if (me.equals(serverNode))
							continue;

						//アドレス取得
						InetSocketAddress addr = serverNode.getAddrP2PPort();
						if (addr == null)
							continue;

						//オンラインか問い合わせる
						IsOnline req = new IsOnline();
						Message m = Message.build(req)
								.packaging(req.createPackage(serverNode))
								.finish();
						RequestFutureUser state = Glb.getP2p().requestAsync(m,
								serverNode);
						states.add(state);
					}

					if (procOnlineCheck(states)) {
						for (OnlineCheckerFuncs callback : callbacks.values()) {
							callback.getFuncWhenOnlineStatesChanged().run();
						}
					}
				}, periodic, periodic, TimeUnit.MILLISECONDS);

		return true;
	}

	public synchronized boolean stop() {
		if (!isStarted())
			return false;
		boolean r = super.stop();
		if (onlineCheckThread != null && !onlineCheckThread.isCancelled())
			onlineCheckThread.cancel(false);

		//自分のオンライン状態をfalseにする。
		NodeIdentifierUser myIdentifier = Glb.getMiddle()
				.getMyNodeIdentifierUser();
		if (myIdentifier != null && onlineStates.get(myIdentifier) != null) {
			onlineStates.put(myIdentifier, false);
		}
		return r;
	}

	/**
	 * 現在の値が新しい値と異なっていれば書き込む
	 *
	 * @param identifier
	 * @param online
	 * @return	変更があったか
	 */
	private boolean write(NodeIdentifierUser identifier, Boolean online) {
		Boolean current = onlineStates.get(identifier);
		if (current != null && !current.equals(online)) {
			onlineStates.put(identifier, online);
			return true;
		}
		return false;
	}

	/**
	 * オンラインチェックの通信の待機と処理
	 *
	 * @param states
	 * @return	変更があったか
	 */
	private boolean procOnlineCheck(List<RequestFutureUser> states) {
		//変更回数
		int changed = 0;
		for (int i = 0; i < states.size(); i++) {
			try {
				RequestFutureUser state = states.get(i);
				if (state.getState().isDone()) {
					states.remove(i);

					//オンライン状態を確認する問い合わせ
					IsOnline req = (IsOnline) state.getHandler().getReq();

					//相手からの返信
					Message res = state.getHandler().getReq().getRes();
					if (res == null) {
						//オフライン
						if (write(state.getIdentifier(), false)) {
							changed++;
						}
						continue;
					}

					//オンライン	しかし正しい相手が返したか確認する必要がある

					//相手からの返信の内容
					Message resM = state.getHandler().getReq().getRes();
					IsOnlineResponse resContent = (IsOnlineResponse) resM
							.getContent();

					//返信者のuserIdは問い合わせたuserIdか
					NodeIdentifierUser responder = resM.getIdentifierUser();
					if (!responder.equals(state.getIdentifier())) {
						if (write(state.getIdentifier(), false)) {
							changed++;
						}
						continue;
					}

					//送信したランダム値が返信の内容に含められているか
					if (!Arrays.equals(req.getRnd(), resContent.getRnd())) {
						if (write(state.getIdentifier(), false)) {
							changed++;
						}
						continue;
					}

					//サーバーか
					if (!Glb.getMiddle().getOnlineChecker()
							.isRoleServer(state.getIdentifier())) {
						if (onlineStates
								.remove(state.getIdentifier()) != null) {
							changed++;
						}
						continue;
					}

					//オンライン状態として設定する
					if (write(state.getIdentifier(), true)) {
						changed++;
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
		return changed > 0;
	}
}
