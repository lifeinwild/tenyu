package bei7473p5254d69jcuat.tenyu.release1.global.subjectivity;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Conf.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;

/**
 * P2PEdgeはP2Pネットワーク上の２ノード間の有向のエッジである。
 * 自分から相手という向きである。相手から自分への情報も一部参照できる。
 * それはfromOtherに記録される。
 * P2PEdgeの多くの情報は、通信されないが保存される。transientは適さない。
 * P2PNodeの情報は通信される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class P2PEdge extends P2PEdgeBase {

	/**
	 * 紹介者への影響 = このノードの活躍 / indirectionCost
	 */
	private static final int indirectionCost = 10;

	/**
	 * 参加客観数の上限
	 */
	private static final int objeMax = 200;

	/**
	 * 1ノードが得る最大スコア
	 */
	private static final int scoreMax = 200;

	public static int getGameMax() {
		return objeMax;
	}

	public static int getIndirectioncost() {
		return indirectionCost;
	}

	public static int getObjemax() {
		return objeMax;
	}

	public static int getScoremax() {
		return scoreMax;
	}

	public static int getScoreMax() {
		return scoreMax;
	}

	/**
	 * {@link P2PEdge#setLatestScore(int)}にスコアを設定する。
	 */
	public static void scoring(List<P2PEdge> neighbors) {
		Collections.shuffle(neighbors);
		for (P2PEdge n : neighbors) {
			n.setLatestScore(0);
		}

		//各種数値データをDoubleにキャストして同じ関数で処理する
		Map<P2PEdge, Double> latencies = new HashMap<>();
		Map<P2PEdge, Double> stabilities = new HashMap<>();
		Map<P2PEdge, Double> lasts = new HashMap<>();
		Map<P2PEdge, Double> credits = new HashMap<>();
		Map<P2PEdge, Double> introduceScores = new HashMap<>();
		//分担範囲が広いほど加算
		Map<P2PEdge, Double> rangeBreadth = new HashMap<>();
		//分担範囲が重複してないほど加算
		Map<P2PEdge, Double> rangeUniqueness = new HashMap<>();

		//経過時間は少ないほどスコアが高くなるので最大値との差をデータとする必要があり、最大値を求める
		long elapsedMax = 0;
		for (P2PEdge n : neighbors) {
			long elapsed = System.currentTimeMillis()
					- n.getLastCommunication();
			if (elapsedMax < elapsed) {
				elapsedMax = elapsed;
			}
		}

		//各種スコアを作成する。それから偏差値が決定し偏差値に応じて総合スコアが増える
		for (P2PEdge neighbor : neighbors) {
			latencies.put(neighbor, (double) neighbor.getLatency());

			long total = neighbor.getConnectionCount()
					+ neighbor.getConnectionFailedCount();
			double rate = total > 0 ? neighbor.getConnectionCount() / total : 0;
			stabilities.put(neighbor, rate);

			long elapsed = System.currentTimeMillis()
					- neighbor.getLastCommunication();
			lasts.put(neighbor, (double) (elapsedMax - elapsed));

			credits.put(neighbor, (double) neighbor.credit());

			introduceScores.put(neighbor, (double) (neighbor.getIntroduceScore()
					+ neighbor.getIntroduceScore()));

			rangeBreadth.put(neighbor,
					(double) (Glb.getConst().getHashSize() * 8 - neighbor
							.getNode().getRange().getBits().getLastBitIndex()));

			int ancestorCount = 0;
			int ancestorMax = 20;
			for (P2PEdge n2 : neighbors) {
				if (n2.getNode().getRange()
						.isAncestorOrSame(neighbor.getNode().getRange())
						&& ancestorCount < ancestorMax)
					ancestorCount++;
			}
			rangeUniqueness.put(neighbor,
					(double) (ancestorMax - ancestorCount));
		}

		//加算
		scoringByStandardScore(neighbors, latencies, 15000D, 0D,
				(double) P2P.getInitlatency(), 1, 35, 1, 2, 10);
		scoringByStandardScore(neighbors, stabilities, 100D, 0D, null, 0, 35, 1,
				2, 100);
		scoringByStandardScore(neighbors, lasts,
				(double) (1000L * 60 * 60 * 24 * 180), 0D, null, 0, 35, 1, 2,
				100);
		scoringByStandardScore(neighbors, credits, null, 0D, null, 0, 35, 1, 2,
				20);
		scoringByStandardScore(neighbors, introduceScores, null, 0D, null, 0,
				35, 1, 2, 150);
		scoringByStandardScore(neighbors, rangeBreadth, null, 0D, null, 0, 35,
				1, 2, 50);
		scoringByStandardScore(neighbors, rangeUniqueness, null, 0D, null, 0,
				35, 1, 2, 10);

	}

	/**
	 * @param neighbors		近傍リスト
	 * @param scores		高いほどノードが近傍リストに残りやすくなる数値
	 * @param data			スコアが与えられる理由となるノードの情報とノードの識別子の対応関係
	 * @param deadLineMax	データがこれより大きいと近傍リストとスコアから削除
	 * @param deadLineMin	データがこれより小さいと近傍リストとスコアから削除
	 * @param deadValue		データがこれと等しいと近傍リストとスコアから削除
	 * @param saveMax		いくつ低偏差値ノードを救済するか
	 * @param saveLineMax	救済対象となる偏差値の最大値
	 * @param saveLineMin	救済対象となる偏差値の最小値
	 * @param difference	偏差値に応じてスコアが与えられるがその格差の大きさ
	 * @param denominator	与えられるスコアがこれで割られる。その情報のスコアへの影響度を決定する
	 */
	private static void scoringByStandardScore(List<P2PEdge> neighbors,
			Map<P2PEdge, Double> data, Double deadLineMax, Double deadLineMin,
			Double deadValue, int saveMax, int saveLineMax, int saveLineMin,
			int difference, int denominator) {
		//異常値のノードのスコアを大幅に下げる
		for (Entry<P2PEdge, Double> e : data.entrySet()) {
			if ((deadLineMax != null && e.getValue() > deadLineMax)
					|| (deadLineMin != null && e.getValue() < deadLineMin)
					|| (deadValue != null && e.getValue().equals(deadValue))) {
				e.getKey().addLatestScore(-1000);
			}
		}

		//平均と分散
		Util u = Glb.getUtil();
		Double ave = u.average(data.values());
		Double devi = u.deviation(data.values());

		//偏差値に応じてスコアを加算
		int save = 0;
		for (Entry<P2PEdge, Double> e : data.entrySet()) {
			//偏差値
			int standardScore = (int) u.standardScore(ave, devi, e.getValue());

			//指数は格差を意味する
			//分母は他のスコアリング基準に比べてレイテンシの比重を決定する。
			e.getKey().addLatestScore(standardScore ^ difference / denominator);

			//低偏差値ノードを一部ハイスコアにする
			if (save < saveMax && standardScore < saveLineMax
					&& standardScore > saveLineMin) {
				save++;
				//偏差値７０相当のスコアを与える
				e.getKey().addLatestScore(70 ^ difference / denominator);
			}
		}
	}

	/**
	 * 通信成功回数
	 * 現在PeriodicNotificationを通じてのみカウントされる
	 */
	protected long connectionCount;
	/**
	 * 通信失敗回数
	 * 現在PeriodicNotificationを通じてのみカウントされる
	 */
	protected long connectionFailedCount;

	/**
	 * 作成日時
	 */
	protected long createDate;

	/**
	 * 自動削除可能か
	 */
	protected boolean dontRemoveAutomatically = false;

	/**
	 * 相手から自分へのエッジの情報。
	 */
	private P2PEdgeFromOther fromOther;

	/**
	 * このノードを紹介したノード
	 */
	protected P2PEdge introducer;

	/**
	 * ハイスコアノードを紹介したノードはこの数値が高まる
	 */
	protected int introduceScore;

	/**
	 * 自分からこのノードへのレイテンシ
	 */
	protected long latency = P2P.getInitlatency();

	/**
	 * このノードが自分に伝えた自分のグローバルアドレス
	 */
	protected byte[] myGlobalAddr;

	/**
	 * 相手
	 */
	protected P2PNode node;

	public P2PEdge() {
		createDate = System.currentTimeMillis();
		fromOther = new P2PEdgeFromOther();
		node = new P2PNode();
	}

	public P2PEdge(byte[] pubKey, int nodeNumber) {
		this();
		P2PNode n = new P2PNode();
		n.setPubKey(new ByteArrayWrapper(pubKey));
		n.setNodeNumber(nodeNumber);
		setNode(n);
	}

	public P2PEdge(byte[] pubKey, int nodeNumber, AddrInfo addr) {
		this(pubKey, nodeNumber);
		node.setAddrInfo(addr);
	}

	public P2PEdge(P2PNode n) {
		this();
		this.node = n;
	}
	/*
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof P2PEdge))
				return false;
			P2PEdge n = (P2PEdge) obj;
			return n.getEdgeId() == getEdgeId();
		}
	*/

	/**
	 * 様々な値から信用を計算する
	 * @return	信用
	 */
	public int credit() {
		//作者なら信用ボーナスを与える
		int bonus = 0;
		try {
			if (Glb.getConst().isAuthorPublicKey(node.getPubKey()))
				bonus = Glb.getConst().getAuthorCredit();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

		int credit = impression;//最大50,000
		credit += Math.pow(processorScoreTotal, 0.45);//最大10,000くらい

		//接続元の地域に応じて信用を制限する
		if (node == null || node.isDiscriminated()) {
			if (credit > 5)
				credit = 5;
		}

		return credit + bonus;
	}

	/**
	 * @return	非veteranの最大信用を制限する
	 */
	public int creditForCatchUp() {
		int r = credit();
		//veteranじゃなければ信用に上限を作る
		if (node == null || !node.isVeteran()) {
			if (r > 5)
				r = 5;
		}
		return r;
	}

	public long getConnectionCount() {
		return connectionCount;
	}

	public long getConnectionFailedCount() {
		return connectionFailedCount;
	}

	public long getCreateDate() {
		return createDate;
	}

	/**
	 * @return	タイムゾーン依存の日付文字列。人向け
	 */
	public String getCreateDateFormatForHuman() {
		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy/MM/dd HH:mm:ss", Glb.getConf().getLocale());
		ZonedDateTime zoned = ZonedDateTime.now(Glb.getConf().getTimeZone());
		return zoned.format(formatter);
	}

	public P2PEdgeFromOther getFromOther() {
		return fromOther;
	}

	public P2PEdge getIntroducer() {
		return introducer;
	}

	public int getIntroduceScore() {
		return introduceScore;
	}

	public long getLatency() {
		return latency;
	}

	/**
	 * @return	低レイテンシノードか
	 */
	public boolean isLowLatency() {
		return latency != P2P.getInitlatency() && latency < 200;
	}

	public byte[] getMyGlobalAddr() {
		return myGlobalAddr;
	}

	public P2PNode getNode() {
		return node;
	}

	/*
		@Override
		public int hashCode() {
			return node.getP2PNodeId().hashCode();
		}
	*/
	public void incrementConnectionCount() {
		connectionCount++;
	}

	public void incrementConnectionFailedCount() {
		connectionFailedCount++;
	}

	/**
	 * @param conditions	条件の一覧。
	 * @return	conditionsのいずれかの条件に一致するか
	 */
	public boolean isApplicatable(List<P2PNode> conditions) {
		for (P2PNode c : conditions) {
			if (node.is(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 条件に一致しているか
	 * @param condition	部分的に状態が与えられている。検索条件。
	 * conditionの初期値でない全状態について一致するか。
	 */
	public boolean isApplicatable(P2PNode condition) {
		return node.is(condition);
	}

	public boolean isBidirection() {
		return commonKeyExchangeState.isSucceed()
				&& fromOther.getCommonKeyExchangeState().isSucceed();//TODO:前者だけで十分か
	}

	public boolean isDontRemoveAutomatically() {
		return dontRemoveAutomatically;
	}

	public void setConnectionCount(long connectionCount) {
		this.connectionCount = connectionCount;
	}

	public void setConnectionFailedCount(long connectionFailedCount) {
		this.connectionFailedCount = connectionFailedCount;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setDontRemoveAutomatically(boolean dontRemoveAutomatically) {
		this.dontRemoveAutomatically = dontRemoveAutomatically;
	}

	public void setFromOther(P2PEdgeFromOther fromOther) {
		this.fromOther = fromOther;
	}

	/**
	 * このノードを紹介したノード。セキュリティのため必ず自分で設定した値を使う。
	 */
	public void setIntroducer(P2PEdge introducer) {
		this.introducer = introducer;
	}

	public void setIntroduceScore(int introduceScore) {
		this.introduceScore = introduceScore;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public void setMyGlobalAddr(byte[] myGlobalAddr) {
		this.myGlobalAddr = myGlobalAddr;
	}

	public void setNode(P2PNode node) {
		if (node == null || !node.validateAtCommon()) {
			Glb.getLogger().warn("invalid node", new Exception());
			return;
		}
		this.node = node;
	}

	//synchronized解除
	@Override
	public String toString() {
		if (Glb.getConf().getRunlevel().equals(RunLevel.DEV)) {
			return " edgeId=" + edgeId + " edgeIdOther=" + fromOther.getEdgeId()
					+ " port=" + node.getP2pPort() + " commonKeyExchange="
					+ commonKeyExchangeState.isSucceed() + " impression="
					+ impression + " node=" + node + " latency=" + latency;

		} else {
			return " node=" + node + " latency=" + latency + " createDate="
					+ getCreateDateFormatForHuman();
		}
	}

	/**
	 * 印象値を割合で変化させる
	 * @param reduce	低下割合	5%なら0.05
	 */
	public void updateImpression(double rate) {
		//異常値なら何もしない
		if (rate < 0 || rate > 2.5)
			return;
		int old = impression;
		impression *= rate;

		Glb.getLogger().info(Lang.NEIGHBOR_IMPRESSION_CHANGED.toString()
				+ " fqdn=" + node.getFqdn() + " isa=" + node.getISAP2PPort()
				+ " nodeNumber=" + node.getNodeNumber()
				+ Arrays.toString(node.getP2PNodeId().getIdentifier()) + " old="
				+ old + " new=" + impression + " rate=" + rate);
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!super.validateAtCreate(r)) {
			b = false;
		}

		if (connectionCount < 0) {
			r.add(Lang.P2PEDGE_CONNECTIONCOUNT, Lang.ERROR_INVALID,
					"connectionCount=" + connectionCount);
			b = false;
		}
		if (connectionFailedCount < 0) {
			r.add(Lang.P2PEDGE_CONNECTIONFAILEDCOUNT, Lang.ERROR_INVALID,
					"connectionFailedCount=" + connectionFailedCount);
			b = false;
		}
		if (createDate < 0) {
			r.add(Lang.P2PEDGE_CREATEDATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		if (introduceScore < 0) {
			r.add(Lang.P2PEDGE_INTRODUCESCORE, Lang.ERROR_INVALID,
					"introduceScore=" + introduceScore);
			b = false;
		}
		if (latency < 0) {
			r.add(Lang.P2PEDGE_LATENCY, Lang.ERROR_INVALID,
					"latency=" + latency);
			b = false;
		}

		if (node == null) {
			r.add(Lang.P2PNODE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!node.validateAtCommon()) {
				r.add(Lang.P2PNODE, Lang.ERROR_INVALID);
				b = false;
			}
		}

		return b;
	}

}
