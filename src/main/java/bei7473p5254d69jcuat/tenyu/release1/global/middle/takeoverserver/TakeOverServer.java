package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;

/**
 * 引継ぎサーバ
 *
 * P2Pネットワーク上の一部のノードが特別な役割を担い、
 * その役割のためのサーバを起動する。
 *
 * 引継ぎサーバは直接的にはRoleに依存していないが、
 * 実質的にRoleによってサーバの種類が決まり、
 * Roleオブジェクトのサーバ候補一覧を使用して
 * 現在どのノードがメインサーバになるか、次のメインサーバはどれで
 * どのノードに引き継げばいいかなどを決定する。
 * とはいえ、直接的にはRoleに依存せず、具象クラスでRoleに依存する。
 * だから極論すれば具象クラス次第でRole以外の手段でサーバ候補を与える事もできる。
 *
 * この領域で必要とされる機能の例
 * ・サーバ候補ノードの優先順位
 * 多くの場合、Roleのサーバ一覧で前にあるユーザーが優先される。
 * ・定期的なサーバ候補ノードのオンライン状態チェック
 * OnlineCheckerによって解決される。
 * ・引継ぎ処理
 * TakeOverMessage等のメッセージクラス
 * ・あるユーザーIDがサーバー候補か、現在サーバをすべきか
 * そのようなメソッドはここに定義される
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TakeOverServer<M extends AbstractTakeOverMessage>
		extends StartableObject {

	/**
	 * 引継ぎ処理
	 * @param message
	 */
	abstract public void takeover(M message);

	/**
	 * もし自分がサーバ候補なら起動する
	 * @return	起動されたか
	 */
	public boolean startIfImCandidate() {
		if (imCandidate()) {
			return start();
		}
		return false;
	}

	public boolean imCandidate() {
		NodeIdentifierUser me = Glb.getMiddle().getMyNodeIdentifierUser();
		List<NodeIdentifierUser> servers = getServerCandidates();
		//自分が候補に入っていなかったら違う
		if (servers == null || servers.isEmpty() || !servers.contains(me))
			return false;
		return true;
	}

	/**
	 * @return	自分は現在この種類のサーバにおいてメインサーバになるべきか
	 */
	public boolean imCurrentServer() {
		NodeIdentifierUser me = Glb.getMiddle().getMyNodeIdentifierUser();
		List<NodeIdentifierUser> servers = getServerCandidates();
		//自分が候補に入っていなかったら違う
		if (servers == null || servers.isEmpty() || !servers.contains(me))
			return false;

		//オンラインのサーバ候補が居なければ自分がやる
		servers = getOnlineServerCandidates(servers);
		if (servers == null || servers.isEmpty())
			return true;

		//このRoleのロジックに従って選択する
		servers = getCurrentServers(servers);
		if (servers == null)
			return false;
		return servers.contains(me);
	}

	/**
	 * 客観の値に応じて適切な状態に遷移する
	 * TODO デッドロックの可能性。ネットワークが絡むので検討しづらい。
	 * あったとしてもネットワークが絡む非常にまれなケースのはず。
	 */
	public synchronized void checkAndStartOrStop() {
		boolean imCurrentServer = imCurrentServer();

		if (started && !imCurrentServer) {
			//起動していて起動すべきではない場合
			stop();
		} else if (!started && imCurrentServer) {
			//起動していなくて起動すべき場合
			start();
		}
	}

	/**
	 * @return	サーバー候補一覧
	 * オフラインノードをも含めた一覧
	 */
	abstract public List<NodeIdentifierUser> getServerCandidates();

	public List<NodeIdentifierUser> getServerCandidates(
			NodeIdentifierUser exceptNode) {
		List<NodeIdentifierUser> exceptNodes = new ArrayList<>();
		exceptNodes.add(exceptNode);
		return getServerCandidates(exceptNodes);
	}

	public List<NodeIdentifierUser> getServerCandidates(
			Collection<NodeIdentifierUser> exceptNodes) {
		List<NodeIdentifierUser> r = getServerCandidates();
		r.removeAll(exceptNodes);
		return r;
	}

	/**
	 * メインサーバーを開始する
	 * @return	開始したか
	 */
	public synchronized boolean start() {
		if (isStarted())
			return false;

		Glb.getLogger().info(getModuleName() + "started");

		return super.start();
	}

	abstract public void registerToOnlineChecker();

	abstract public String getModuleName();

	public synchronized boolean stop() {
		//開始していなければ何もしない
		if (!isStarted())
			return false;

		//これで新規メッセージの受付は停止する
		super.stop();

		//新規メッセージの受付が停止してから引継ぎ処理
		inheritProc();
		return true;
	}

	/**
	 * 引継ぎメッセージを送信する。
	 *
	 * @param nextServerUserId
	 * @return	送信に成功したか
	 */
	abstract public boolean sendInheritingMessage(
			NodeIdentifierUser nextServerNode);

	/**
	 * @param states		サーバー候補のオンライン状態一覧
	 * @param candidates	オフラインも含めたサーバー候補一覧
	 * @return	オンラインサーバー候補一覧
	 */
	public List<NodeIdentifierUser> getOnlineServerCandidates(
			List<NodeIdentifierUser> serverCandidates) {
		List<NodeIdentifierUser> candidatesTmp = serverCandidates;
		List<NodeIdentifierUser> onlineServerCandidates = new ArrayList<>();
		for (NodeIdentifierUser identifier : candidatesTmp) {
			try {
				Boolean online = Glb.getMiddle().getOnlineChecker()
						.isOnline(identifier);
				if (online != null && online) {
					onlineServerCandidates.add(identifier);
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
		return onlineServerCandidates;
	}

	/**
	 * オンラインサーバ候補一覧から現在メインサーバーとして稼働すべきノード一覧を決定する
	 * Roleによって同時にただ1つのメインサーバが稼働する場合もあれば、
	 * 複数のメインサーバーが同時稼働する場合もあるので、
	 * メソッドシグネチャとしては一覧にしている。
	 * しかしRoleによって同時に1つのメインサーバのみが稼働する場合、
	 * 返値のサイズは最大1になる。
	 *
	 * オンラインサーバ候補一覧から現在どのノードがサーバをすべきかを決定する
	 * ロジックはRoleによって違う。
	 * このデフォルト実装としては同時に1つのサーバだけが稼働するという前提で書かれているので、
	 * Roleによって必要なら具象クラスでオーバーライドする。
	 *
	 * @param onlineServerCandidates	サーバー候補のうちオンラインのもの
	 * @return	現在サーバーをすべきノード一覧
	 */
	public List<NodeIdentifierUser> getCurrentServers(
			List<NodeIdentifierUser> onlineServerCandidates) {
		if (onlineServerCandidates == null
				|| onlineServerCandidates.size() == 0)
			return null;
		//大抵のRoleではただ1つのノードだけがサーバーをする
		NodeIdentifierUser mainServer = onlineServerCandidates.get(0);
		List<NodeIdentifierUser> r = new ArrayList<>();
		r.add(mainServer);
		return r;
	}

	public List<NodeIdentifierUser> getCurrentServers() {
		return getCurrentServers(
				getOnlineServerCandidates(getServerCandidates()));
	}

	/**
	 * オンライン状態に照らして現在メインサーバをやるべきユーザーを特定する
	 * @param exceptNode	このノードを除く
	 * @return
	 */
	public NodeIdentifierUser getMainServerUserIdSimple(
			NodeIdentifierUser exceptNode) {
		List<NodeIdentifierUser> servers = getCurrentServers(
				getOnlineServerCandidates(getServerCandidates(exceptNode)));
		if (servers == null || servers.size() == 0)
			return null;
		return servers.get(0);
	}

	/**
	 * メインサーバが1つの役割の場合のみ機能する
	 * @return	メインサーバ
	 */
	public NodeIdentifierUser getMainServerUserIdSimple() {
		List<NodeIdentifierUser> servers = getCurrentServers(
				getOnlineServerCandidates(getServerCandidates()));
		if (servers == null || servers.size() == 0)
			return null;
		return servers.get(0);
	}

	/**
	 *
	 * @param identifier
	 * @return			identifierのノードは現在サーバとして動作すべきか
	 */
	public boolean isCurrentServer(NodeIdentifierUser identifier) {
		if (identifier == null)
			return false;

		List<NodeIdentifierUser> servers = getCurrentServers();
		return servers != null && servers.contains(identifier);
	}

	/**
	 * 引継ぎ先の決定
	 *
	 * 1種類のRoleにつき複数のサーバが稼働している場合があり、
	 * その場合、各サーバごとに引継ぎ先が異なる場合がある。
	 * つまり実行するノード毎に異なる結果になる可能性がある。
	 *
	 * @param currentServers	現在のサーバー一覧
	 * @return			引継ぎ先
	 */
	public NodeIdentifierUser getNextServer(
			List<NodeIdentifierUser> currentServers) {
		if (currentServers == null)
			return null;
		//同時に稼働するサーバーが１ならこの実装で十分
		if (currentServers.size() >= 1)
			return currentServers.get(0);
		return null;
	}

	public NodeIdentifierUser getNextServer() {
		return getNextServer(getCurrentServers(
				getOnlineServerCandidates(getServerCandidates())));
	}

	public NodeIdentifierUser getNextServerExceptId(
			NodeIdentifierUser exceptNode) {
		List<NodeIdentifierUser> exceptIds = new ArrayList<>();
		exceptIds.add(exceptNode);
		return getNextServerExceptIds(exceptIds);
	}

	public NodeIdentifierUser getNextServerExceptIds(
			Collection<NodeIdentifierUser> exceptNodes) {
		return getNextServer(getCurrentServers(
				getOnlineServerCandidates(getServerCandidates(exceptNodes))));
	}

	protected void inheritProc() {
		NodeIdentifierUser me = Glb.getMiddle().getMyNodeIdentifierUser();
		//OnlineChecker oc = Glb.getMiddle().getOnlineChecker();
		// 残っていたメッセージリストを次のメインサーバに渡す
		//失敗を想定してある程度繰り返す
		//引継ぎ処理のため、何らかのサーバー役を担っているユーザーは
		//ソフトウェアの終了処理が1分間ほど長くなる。
		int count = 5;
		for (int i = 0; i < count; i++) {
			try {
				List<NodeIdentifierUser> exceptNodes = new ArrayList<>();
				exceptNodes.add(me);

				NodeIdentifierUser nextServerNode = getNextServerExceptId(me);
				if (nextServerNode == null) {
					Glb.getLogger().error("No next server");
					return;
				}

				//引継ぎ
				if (sendInheritingMessage(nextServerNode)) {
					return;//引継ぎ完了
				}

				//引継ぎ失敗	少し待ってから再試行
				try {
					Thread.sleep(1000L * 10);
				} catch (InterruptedException e) {
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
	}
}
