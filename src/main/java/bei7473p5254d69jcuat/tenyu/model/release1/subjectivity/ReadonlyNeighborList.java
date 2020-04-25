package bei7473p5254d69jcuat.tenyu.model.release1.subjectivity;

import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.P2PNode.*;
import glb.*;
import glb.util.Util.*;

/**
 * このクラスだけなら読み込み系メソッドしかないが
 * 子クラスが書き込み系メソッドを持ちうるので
 * スレッドセーフにするにはConcurrentHashMapをコンストラクタに渡す。
 *
 * 近傍リストを読み取り専用で使う場合、このクラスを使用する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ReadonlyNeighborList {

	/**
	 * エッジID to ノード情報。
	 * 近傍。どんなノードが居るかという知識。
	 * レイテンシと自分からのCPU証明スコアは削除判定の確率を決定し、
	 * GetAddresses等での貢献や相手からのCPU証明スコアは削除試行に耐える回数を決定する。
	 */
	protected volatile Map<Long, P2PEdge> neighbors;

	public ReadonlyNeighborList() {
	}

	public ReadonlyNeighborList(List<P2PEdge> neighbors) {
		Map<Long, P2PEdge> m = new ConcurrentHashMap<>();
		for (P2PEdge e : neighbors) {
			m.put(e.getEdgeId(), e);
		}
		this.neighbors = m;
	}

	public ReadonlyNeighborList(Map<Long, P2PEdge> neighbors) {
		this.neighbors = neighbors;
	}

	public List<P2PEdge> getNeighborsRecentlyConnected() {
		List<P2PEdge> r = new ArrayList<>();
		for (P2PEdge e : getNeighborsCopy()) {
			if (e.isConnectedIn5Minute()) {
				r.add(e);
			}
		}
		return r;
	}

	/**
	 * 接続可能な近傍を乱択してアドレスのみにして返す
	 * @param num	取得するノードの数
	 * @return		ランダムに選択されたノード一覧
	 */
	public List<InetSocketAddress> getAddrRandom(int num) {
		List<InetSocketAddress> r = new ArrayList<InetSocketAddress>();
		List<P2PEdge> nodes = getNeighborRandom(num, true);
		for (P2PEdge n : nodes) {
			InetSocketAddress isa = n.getNode().getISAP2PPort();
			if (isa != null)
				r.add(isa);
		}
		return r;
	}

	/**
	 * 近傍取得条件を作る
	 */
	public List<P2PNode> getConditionOfGetAddresses() {
		//条件 これをセットアップして返す
		List<P2PNode> conditions = new ArrayList<>();

		//全近傍における欠落範囲を求める
		int searchDegree = 300;
		int max = 20;
		//対象とする近傍一覧を設定
		ReadonlyNeighborList l = copy();

		//範囲一覧
		List<AssignedRange> ranges = new ArrayList<>();
		//自分の範囲を加える
		ranges.add(Glb.getSubje().getMe().getRange());
		//近傍一覧から範囲を取得
		for (P2PEdge n : l.getNeighborsUnsafe()) {
			if (n.getNode().getRange() != null)
				ranges.add(n.getNode().getRange());
		}
		List<AssignedRange> lacks = AssignedRange.searchLack(ranges,
				searchDegree, max);
		//条件に加える
		for (AssignedRange lack : lacks) {
			P2PNode n = new P2PNode();
			n.setRange(lack);
			conditions.add(n);
		}
		return conditions;
	}

	/**
	 * 現在の近傍一覧とその分担範囲から欠落している範囲を返す
	 * @param conditions		欠落範囲が設定される
	 * @param searchDegree		探索強度。欠落範囲は網羅的に見つけられず探索次第。
	 * @param max				欠落範囲の数。
	 */
	/*
	private void getLackRangeCondition(List<P2PEdge> conditions,
			int searchDegree, int max) {

	}
	*/

	public ReadonlyNeighborList extract(P2PNode condition) {
		Map<Long, P2PEdge> r = new ConcurrentHashMap<>();
		for (P2PEdge n : neighbors.values()) {
			if (n.isApplicatable(condition))
				r.put(n.getEdgeId(), n);
		}
		return new ReadonlyNeighborList(r);
	}

	/**
	 * @return	複製。内部実装はConcurrentHashMap
	 */
	public ReadonlyNeighborList copy() {
		Map<Long, P2PEdge> r = new ConcurrentHashMap<>();
		for (P2PEdge n : neighbors.values()) {
			r.put(n.getEdgeId(), n);
		}
		return new ReadonlyNeighborList(r);
	}

	/**
	 * 各近傍ノードのプロセッサ証明スコアに応じて投票を行い、
	 * 近傍から通知された自分のグローバルアドレスから最も信ぴょう性が高いアドレスを決定する。
	 * @return	自分のグローバルアドレス
	 */
	public byte[] getMyGlobalAddr() {
		if (neighbors.size() == 0)
			return null;

		Map<ByteArrayWrapper, Integer> votes = new HashMap<>();
		for (P2PEdge n : neighbors.values()) {
			byte[] addr = n.getMyGlobalAddr();
			if (addr == null)
				continue;
			ByteArrayWrapper addrw = new ByteArrayWrapper(addr);
			Integer score = n.getImpression();
			if (score <= 0)
				continue;
			Integer total = votes.get(addrw);
			if (total == null)
				total = 0;
			votes.put(addrw, total + score);
		}
		ByteArrayWrapper r = Glb.getUtil().majority(votes);
		if (r == null)
			return null;
		return r.getByteArray();
		/*
				//addr : score SortedMapはkeySet()時に昇順ソートを行う。
				SortedMap<ByteArrayWrapper, Long> scores = new TreeMap<>();
				byte[] myGlobalAddr = null;
				for (P2PEdge n : neighbors.values()) {
					byte[] addr = n.getMyGlobalAddr();
					if (addr == null)
						continue;
					ByteArrayWrapper addrw = new ByteArrayWrapper(addr);
					Integer score = n.getImpression();
					if (score <= 0)
						continue;
					Long total = scores.get(addrw);
					scores.put(addrw, total + score);
				}

				//scoresの最後の要素が最大スコアを獲得したアドレス
				for (ByteArrayWrapper a : scores.keySet()) {
					myGlobalAddr = a.getByteArray();
				}

				return myGlobalAddr;
				*/
	}

	/**
	 * @param identifier	探索対象のノード
	 * @return	identifierに対応する近傍ノード
	 */
	public P2PEdge getNeighbor(NodeIdentifierUser identifier) {
		try {
			User u = Glb.getObje()
					.getUser(us -> us.get(identifier.getUserId()));
			for (P2PEdge n : neighbors.values()) {
				//ノードが設定している鍵タイプに応じて
				//Userのどの鍵と比較するかを決める
				byte[] userPub = null;
				switch (n.getNode().getType()) {
				case MOBILE:
					userPub = u.getMobilePublicKey();
					break;
				case PC:
					userPub = u.getPcPublicKey();
					break;
				case OFFLINE:
					userPub = u.getOfflinePublicKey();
					break;
				default:
				}
				if (!Arrays.equals(userPub,
						n.getNode().getPubKey().getByteArray())) {
					continue;
				}
				if (n.getNode().getNodeNumber() != identifier.getNodeNumber()) {
					continue;
				}
				return n;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;

	}

	public P2PEdge getNeighbor(byte[] p2pNodeId) {
		return getNeighbor(new NodeIdentifierP2PEdge(p2pNodeId));
	}

	public P2PEdge getNeighbor(NodeIdentifierP2PEdge p2pNodeId) {
		for (P2PEdge n : neighbors.values()) {
			if (n.getNode().getPubKey() != null
					&& n.getNode().getP2PNodeId().equals(p2pNodeId))
				return n;
		}
		return null;
	}

	public P2PEdge getNeighbor(byte[] addr, int p2pPort) {
		for (P2PEdge n : neighbors.values()) {
			if (Arrays.equals(n.getNode().getAddr(), addr)
					&& n.getNode().getP2pPort() == p2pPort)
				return n;
		}
		return null;
	}

	public P2PEdge getNeighbor(long edgeIdFromMe) {
		return neighbors.get(edgeIdFromMe);
	}

	/**
	 * @return	最近接続したノードの中で最も低レイテンシなノード
	 */
	public P2PEdge getNeighborLowLatency() {
		List<P2PEdge> l = getNeighborsCopy();

		Collections.sort(l, Comparator.comparing(P2PEdge::getLatency));

		for (P2PEdge n : l) {
			if (!n.isConnectedIn5Minute())
				continue;
			return n;
		}
		return null;
	}

	/**
	 * @return	レイテンシ昇順の近傍一覧のコピー
	 */
	public List<P2PEdge> getNeighborsLowLatency() {
		List<P2PEdge> l = getNeighborsCopy();
		Collections.sort(l, Comparator.comparing(P2PEdge::getLatency));
		return l;
	}

	/**
	 * @return	レイテンシソートされた最近5分以内に接続があった近傍の一覧
	 */
	public List<P2PEdge> getNeighborsLowLatencySortConnectedIn5Minute() {
		List<P2PEdge> l = getNeighborsRecentlyConnected();
		Collections.sort(l, Comparator.comparing(P2PEdge::getLatency));
		return l;
	}

	/**
	 * @return	加重乱択で選ばれた近傍
	 */
	public P2PEdge getNeighborRandomWeight() {
		Map<P2PEdge, Integer> candidates = new HashMap<>();
		for (P2PEdge n : neighbors.values()) {
			candidates.put(n, n.credit());
		}
		return Glb.getUtil().rndSelect(candidates);
	}

	/**
	 * 近傍を乱択。指定された件数以下であることのみが保証される。
	 * @return 新規リストに元リストのオブジェクトを入れたもの
	 */
	public List<P2PEdge> getNeighborRandom(int num,
			boolean connectedRecentlyOnly) {
		return getNeighborRandom(num, null, connectedRecentlyOnly);
	}

	private List<P2PEdge> getNeighborRandom(int num, List<P2PNode> conditions,
			boolean connectedRecentlyOnly) {
		List<P2PEdge> r = new ArrayList<P2PEdge>();
		List<P2PEdge> momentNeighbors = getNeighborsCopy();

		for (int i = 0; i < num && momentNeighbors.size() > 0; i++) {
			//判定ノードを選択
			int index = Glb.getRnd().nextInt(momentNeighbors.size());
			//削除する事で同じノードが何度も判定されることを防ぐ
			P2PEdge removed = momentNeighbors.remove(index);

			//最近通信したノードのみの場合
			if (connectedRecentlyOnly) {
				//接続可能でなければ何もせず次の判定へ
				if (!removed.isConnectedIn5Minute())
					continue;
			}

			//条件が指定されていた場合
			if (conditions != null && conditions.size() > 0) {
				//条件に一致しなければ何もせず次の判定へ
				if (!removed.isApplicatable(conditions)) {
					continue;
				}
			}

			//結果に加える
			r.add(removed);
		}
		return r;
	}

	/**
	 * @param condition	条件
	 * @param connectedRecentlyOnly	最近接続したノードのみ
	 * @return	条件に一致する全近傍
	 */
	public List<P2PEdge> getNeighborsByHashConnectedIn5Minute(byte[] hash,
			boolean connectedRecentlyOnly) {
		List<P2PEdge> r = new ArrayList<>();
		for (P2PEdge e : getNeighborsCopy()) {
			if (e.getNode() == null || e.getNode().getRange() == null) {
				continue;
			}
			if (connectedRecentlyOnly && !e.isConnectedIn5Minute()) {
				continue;
			}
			if (e.getNode().getRange().support(hash)) {
				r.add(e);
			}
		}
		return r;
	}

	/**
	 * 主観情報を排除したノード情報をランダムに返す
	 * @param num	取得件数
	 * @param conditions 条件 null可
	 * @return	読み取り専用
	 */
	public List<P2PNode> getNeighborRandomForCommunication(int num,
			List<P2PNode> conditions) {
		List<P2PEdge> edges = getNeighborRandom(num, conditions, true);
		List<P2PNode> nodes = new ArrayList<>();
		for (P2PEdge edge : edges) {
			nodes.add(edge.getNode());
		}
		Glb.debug(
				() -> "getNeighborRandomForCommunication size=" + nodes.size());
		if (conditions != null)
			Glb.debug(() -> " conditions size=" + conditions.size());
		return nodes;
	}

	/**
	 * 一時点の近傍一覧のコピーが欲しい場合に使う。
	 * 返されたリストは要素を削除しても元のリストに影響しない。
	 * @return	近傍一覧のシャローコピー
	 */
	public List<P2PEdge> getNeighborsCopy() {
		return new ArrayList<>(neighbors.values());
	}

	public ReadonlyNeighborList getNeighborsCopyList() {
		return new ReadonlyNeighborList(new ArrayList<>(neighbors.values()));
	}

	/**
	 * 返されたリストは要素を削除すると元のリストに影響する。
	 * コンストラクタに入れたMapの実装によって並列処理の安全性が変わる。
	 * @return	内部の近傍を直接返す。
	 */
	public Collection<P2PEdge> getNeighborsUnsafe() {
		return neighbors.values();
	}

	public Set<Entry<Long, P2PEdge>> getNeighborListEntrySetUnsafe() {
		return neighbors.entrySet();
	}

	/**
	 * 知っているIPアドレスの件数
	 */
	public int size() {
		return neighbors.size();
	}

	public String logNeighbors() {
		try {
			StringBuilder sb = new StringBuilder();
			for (P2PEdge n : neighbors.values())
				sb.append(n + ":");
			return sb.toString();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return "";
		}
	}

	/**
	 * 近傍に関する情報をログに出力する。
	 */
	public void logNeighbors(String prefix) {
		Glb.getLogger().info(prefix + Lang.NEIGHBORS + ":" + logNeighbors());
	}

	/**
	 * @return	近傍のオンライン率
	 * ５分以内に通信した近傍のみオンラインとみなす。
	 */
	public double getOnlineRate() {
		int sum = 0;
		int size = neighbors.size();
		if(size == 0)
			return 0;
		for (P2PEdge e : neighbors.values()) {
			if (e.isConnectedIn5Minute()) {
				sum++;
			}
		}
		return sum / size;
	}

}
