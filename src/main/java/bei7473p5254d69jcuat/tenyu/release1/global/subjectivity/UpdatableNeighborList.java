package bei7473p5254d69jcuat.tenyu.release1.global.subjectivity;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity.GetAddresses.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.P2PNode.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;

public class UpdatableNeighborList extends ReadonlyNeighborList {
	/**
	 * 近傍最大数。これを超える件数になることは無い。
	 */
	protected int neighborMax = 2000;

	/**
	 * 洗練処理においてこの件数以下まで近傍リストのノードを減らす。
	 */
	protected int neighborThreshold = 1000;

	public UpdatableNeighborList() {
	}

	/**
	 * @param under	洗練処理においてこの件数以下まで近傍リストのノードを減らす。
	 * @param max	近傍最大数。これを超える件数になることは無い。
	 */
	public UpdatableNeighborList(int under, int max) {
		super(new ConcurrentHashMap<>());
		neighborThreshold = under;
		neighborMax = max;
	}

	public UpdatableNeighborList(Map<Long, P2PEdge> neighbors, int under,
			int max) {
		super(neighbors);
		neighborThreshold = under;
		neighborMax = max;
	}

	/**
	 * @return	近傍を追加可能か
	 */
	public boolean isAddable() {
		return getNeighborsSize() < neighborMax;
	}

	public P2PEdge addNeighbor(byte[] pubKey, int nodeNumber, AddrInfo addr) {
		return addNeighbor(new P2PEdge(pubKey, nodeNumber, addr));
	}

	public P2PEdge addNeighbor(P2PEdge add) {
		if (add == null || !add.validateAtCreate(new ValidationResult())) {
			Glb.getLogger().warn("invalid edge", new Exception());
			return null;
		}

		if (!isAddable())
			return null;

		if (add.getNode().isMe())
			return null;

		//同じアドレスで、かつ最近作られたエッジなら追加しない。
		//もし2ノードが互いにほぼ同時に認識プロセスを始めるとここに引っかかる。
		//アドレスが同じというだけでは、アドレスは日々変化するので、
		//偶然他ノードがかつて使っていたアドレスと一致する可能性があり、作成日時の条件が必要
		P2PEdge sameAddr = getNeighbor(add.getNode().getAddr(),
				add.getNode().getP2pPort());
		if (sameAddr != null
				&& System.currentTimeMillis() - sameAddr.getCreateDate() < Glb
						.getSubje().getUnsecureClearInterval())
			return null;

		if (add.getEdgeId() == 0)
			add.setEdgeId(generateEdgeId());

		neighbors.put(add.getEdgeId(), add);
		add.setCreateDate(System.currentTimeMillis());
		return add;
	}

	public void batchRecognition(GetAddressesResponse res, P2PEdge introducer) {
		if (res == null || res.getNodes() == null
				|| res.getNodes().size() == 0) {
			return;
		}

		/*
		 * TODO
		 * 認識処理は、複数の通信や永続化される状態の変更があり、
		 * 折角Nettyが非同期通信に対応していても、
		 * 多数のノードに一斉に認識処理をする場面で同期的な処理になる。
		 * 以下は、マルチスレッドにしているが、あまり効果は無く、
		 * １つの接続が他の接続をブロックないし遅延させる。
		 * この処理は遅くても大きな問題は無い。
		 */
		for (P2PNode n : res.getNodes()) {
			try {
				//既知のノードか
				P2PEdge exist = Glb.getSubje().getNeighborList()
						.getNeighbor(n.getP2PNodeId());
				if (exist != null) {
					//そのノードが最近通信できているなら処理する必要無し
					if (exist.isConnectedIn8Hour()) {
						continue;
					}
					//最近通信できていないなら再認識
				}
				//GetAddressesによって知ったノード情報をログ出力
				Glb.debug(() -> Glb.getConf().getP2pPort()
						+ " is received about " + n.getP2pPort());
				Recognition.send(n.getAddr(), n.getP2pPort(), introducer);
			} catch (Exception e) {
				Glb.getLogger().info("", e);
			}
		}
	}

	@Override
	public UpdatableNeighborList copy() {
		Map<Long, P2PEdge> r = new ConcurrentHashMap<>();
		for (P2PEdge n : neighbors.values()) {
			r.put(n.getEdgeId(), n);
		}
		return new UpdatableNeighborList(r, neighborThreshold, neighborMax);
	}

	/**
	 * @return ランダムに作成されたedgeId
	 */
	public long generateEdgeId() {
		while (true) {
			long edgeId = Glb.getRnd().nextLong();
			if (edgeId == P2PEdge.getSpecialEdgeId())
				continue;
			if (edgeId == 0)
				continue;
			if (getNeighbor(edgeId) != null)
				continue;
			return edgeId;
		}
	}

	/**
	 * GetAddressesメッセージを送る。そのreceived()で近傍追加が行われる
	 */
	public void getAddresses() {
		//レイテンシ更新
		for (P2PEdge n : neighbors.values())
			updateCommunicationInfo(n);

		//近傍が多すぎれば削除
		rankingAndRemove();

		//特に必要な近傍を取得
		List<P2PNode> conditions = getConditionOfGetAddresses();

		if (conditions != null && conditions.size() > 0) {
			//条件取得
			GetAddresses.send(conditions, this);
			Glb.debug("neighbors after conditional getAddresses:"
					+ logNeighbors());
		}

		//無条件取得
		if (isAddable())
			GetAddresses.send(null, this);
		Glb.debug("neighbors after getAddresses:" + logNeighbors());
	}

	/**
	 * @return 近傍一覧の印象値の平均
	 */
	public long getImpressionAve() {
		long r = 0;
		for (P2PEdge n : neighbors.values())
			r += n.getImpression();
		return r;
	}

	public int getNeighborMax() {
		return neighborMax;
	}

	public int getNeighborThreshold() {
		return neighborThreshold;
	}

	/**
	 * 近傍を様々な条件で評価して一部削除
	 */
	public void rankingAndRemove() {
		//削除する近傍一覧
		List<P2PEdge> removeList = new ArrayList<>();
		int over = getNeighborsSize() - neighborThreshold;

		//総件数が閾値以下なら何も削除しない
		if (over <= 0)
			return;

		//コピーされた瞬間の近傍一覧
		List<P2PEdge> momentNeighbors = null;
		//並べ替え等の更新操作があるので新しいリストにする
		momentNeighbors = new ArrayList<>(neighbors.values());

		//ノードにスコアを設定する
		P2PEdge.scoring(momentNeighbors);

		//自動削除不可近傍を除外
		for (int i = 0; i < momentNeighbors.size(); i++) {
			P2PEdge n = momentNeighbors.get(i);
			if (n.isDontRemoveAutomatically()) {
				momentNeighbors.remove(i);
			}
		}

		//スコアでランキング	スコア昇順
		Collections.sort(momentNeighbors,
				Comparator.comparing(P2PEdge::getLatestScore));

		//削除するノードを削除一覧に入れる
		int momentSize = momentNeighbors.size();
		for (int i = 0; i < over && i < momentSize; i++) {
			removeList.add(momentNeighbors.get(0));
		}

		int deleteCount = 0;
		//削除処理
		for (P2PEdge n : removeList) {
			if (DeleteEdge.send(n))
				deleteCount++;
		}

		Glb.debug("neighbors deleted:" + deleteCount);

	}

	public boolean removeNeighbor(byte[] p2pNodeId) {
		return removeNeighbor(new NodeIdentifierP2PEdge(p2pNodeId));
	}

	/**
	 * 近傍を削除
	 */
	public boolean removeNeighbor(NodeIdentifierP2PEdge p2pNodeId) {
		for (P2PEdge n : neighbors.values()) {
			NodeIdentifierP2PEdge hereP2PNodeId = n.getNode().getP2PNodeId();
			if (hereP2PNodeId != null && hereP2PNodeId.equals(p2pNodeId)) {
				removeNeighbor(n.getEdgeId());
				return true;
			}
		}
		return false;
	}

	public boolean removeNeighbor(long edgeId) {
		return neighbors.remove(edgeId) != null;
	}

	public void setNeighborMax(int neighborMax) {
		this.neighborMax = neighborMax;
	}

	public void setNeighborThreshold(int neighborThreshold) {
		this.neighborThreshold = neighborThreshold;
	}

	/**
	 * レイテンシやFQDNによるIPアドレスの更新
	 */
	private void updateCommunicationInfo(P2PEdge n) {
		//FQDNがあればIPアドレスを更新
		n.getNode().updateAddrByFqdn();

		//レイテンシ更新
		LatencyTestResult r = Glb.getP2p()
				.latencyTest(n.getNode().getISAP2PPort(), 10);
		n.setLatency(r.getAverageLatency());
		return;
	}

	/**
	 * 新しい近傍を作成するか既存の近傍のアドレスを更新する
	 * @return	近傍に新しく登録されたかアドレスが更新されたノード。
	 * nullは不正な値が渡されたことを意味する。
	 * 近傍リストに登録されたオブジェクト。
	 */
	public P2PEdge updateOrCreateNeighbor(byte[] pub, int nodeNumber,
			AddrInfo addr) {
		P2PNode n = new P2PNode();
		n.setPubKey(new ByteArrayWrapper(pub));
		n.setNodeNumber(nodeNumber);
		n.setAddrInfo(addr);
		return updateOrCreateNeighbor(n);
	}

	public P2PEdge updateOrCreateNeighbor(P2PEdge latest) {
		if (latest.getNode().getPubKey() == null)
			return null;
		P2PEdge n;
		if ((n = neighbors.get(latest.getEdgeId())) == null) {
			n = addNeighbor(latest);
		} else {
			n.getNode().update(latest.getNode());
		}
		return n;
	}

	public P2PEdge updateOrCreateNeighbor(P2PNode latest) {
		return updateOrCreateNeighbor(new P2PEdge(latest));
	}

}
