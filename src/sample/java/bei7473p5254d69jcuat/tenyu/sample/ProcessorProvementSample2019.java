package bei7473p5254d69jcuat.tenyu.sample;

import java.util.*;
import java.util.Map.*;

import org.apache.commons.lang.*;

import bei7473p5254d69jcuat.tenyu.sample.CPUProvementSample2018.*;

/**
 * 分散合意とプロセッサ証明の相補的動作をできるだけ単純化して示す
 *
 * ダミーノード攻撃
 * 誰かが1台のPCで大量にP2Pソフトウェアを起動し、つまり大量のダミーノードを作り
 * ネットワークにそれらノードを潜り込ませ不正値を主張して攻撃する。
 *
 * スパム攻撃
 * 近傍関係が無いノード（ランダム値を送ってきてない相手）に送信して信用を得ようとする攻撃。
 *
 * 過剰近傍攻撃
 * 膨大なノードと近傍関係を作り分散合意の収束値を操作しようとする攻撃
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ProcessorProvementSample2019 {
	/**
	 * 正常値
	 */
	private static final Long goodVal = 1L;

	/**
	 * 不正値
	 */
	private static final Long evilVal = 0L;
	/**
	 * 近傍に送信するランダム値の長さ
	 */
	private static final int rndStrLen = 5;

	/**
	 * 全ﾉｰﾄﾞ数
	 */
	private static final int allNodeCount = 100;

	/**
	 * 最大近傍数
	 * 本番用の実装でも最大近傍数はソフトウェアに定数として設定される
	 */
	private static final int neighborMax = (int) (allNodeCount * 0.10);

	/**
	 * 全ノード一覧
	 */
	private static final List<Node> allNodes = new ArrayList<>();

	//善意のノードの数
	private static final int goodNodeCount = (int) (allNodeCount * 0.25);

	/**
	 * @return	正常値を持っている善意ノード数 / 善意ノード数
	 */
	private static double getRate() {
		//正常値を持っている善意ノード数
		int goodCount = 0;
		for (Node n : allNodes) {
			//悪意ノードを除外
			if (n.isDummy() || n.isOverCount() || n.isSpam()) {
				continue;
			}
			if (n.getData().equals(goodVal)) {
				goodCount++;
			}
		}

		return (double) goodCount / goodNodeCount;
	}

	public static void main(String[] args) {
		sampleCase();
	}

	/**
	 * ﾌﾟﾛｾｯｻ証明
	 */
	private static void processorProvement() {
		//近傍へのランダム値送信。それに依存して問題関数が作られる
		for (Node n : allNodes) {
			for (Edge e : n.getEdges()) {
				String rndStr = RandomStringUtils.randomAscii(rndStrLen);
				//誰にどんなランダム値を送ったか記録しておく
				e.setRndStr(rndStr);
				//ランダム値送信
				e.getNeighbor().receive(n, rndStr);
			}
		}

		//近傍から届いたランダム値で問題関数を作成して回答する。成功すれば信用を得る
		System.out.println("プロセッサ証明開始");
		for (Node n : allNodes) {
			n.interactionProcessorProvement();
		}
		System.out.println("プロセッサ証明終了");
	}

	private static double sampleCase() {
		Random r = new Random();

		//善意のﾉｰﾄﾞ	全体の25%しか占めない	正常値を主張
		//最後に善意のﾉｰﾄﾞが正しく1を保持している率を返す
		for (int i = 0; i < goodNodeCount; i++) {
			Node n = new Node(goodVal);
			allNodes.add(n);
		}
		//悪意のﾉｰﾄﾞ	全体の75%を占める	不正値を主張｡
		for (int i = 0; i < allNodeCount - goodNodeCount; i++) {
			Node n = new Node(evilVal);

			//下の方の検出ログのコメントアウトを外してから
			//ここの悪意ノード設定をコメントアウトすると
			//攻撃のON/OFF及びその検出を確認できる
			//3種類の攻撃パターンがあり、悪意ノードを3分割する
			switch (i % 3) {
			case 0:
				//ﾀﾞﾐｰﾉｰﾄﾞ攻撃
				n.setDummy(true);
				break;
			case 1:
				//スパム攻撃
				n.setSpam(true);
				break;
			case 2:
				//過剰近傍攻撃
				n.setOverCount(true);
				break;
			}
			allNodes.add(n);
		}

		//次の近傍関係作成においてネットワークトポロジーをランダム化するためシャッフル
		Collections.shuffle(allNodes);

		// 各ﾉｰﾄﾞの近傍をﾗﾝﾀﾞﾑに作成｡
		for (Node n : allNodes) {
			int max = neighborMax;
			if (n.isOverCount()) {
				//過剰近傍攻撃ノードなら制限を超えて近傍を作る
				max *= 3;
			}
			//普通のノードは最大近傍数の半分～最大までの近傍を持つ
			for (; n.getEdges().size() < max / 2;) {
				//ランダムに近傍を選択
				Node neighbor = allNodes.get(r.nextInt(allNodes.size() - 1));
				//相手側の近傍数が限界なら他のノードを探す
				if (neighbor == null || neighbor.getEdges().size() >= max)
					continue;
				//近傍関係作成
				if (neighbor != null) {
					n.add(neighbor);
				}
				//逆方向からも作る。プロセッサ証明ではこれが必須
				//単方向前提なら自分が知らないところで自分を近傍としているノードが居るわけで
				//スパム攻撃を防ぐ論理を失う
				neighbor.add(n);
			}
		}

		for (Node n : allNodes) {
			System.out.println(n.getEdges().size());
		}

		System.out.println("before sync 善意のﾉｰﾄﾞにおける正常値率=" + getRate());

		//日々ﾌﾟﾛｾｯｻ証明を通じて各近傍の信用値を算出する。
		//ﾀﾞﾐｰﾉｰﾄﾞは演算量を出せないので、ﾌﾟﾛｾｯｻ証明はﾀﾞﾐｰﾉｰﾄﾞに排除圧を与える。
		processorProvement();
		//1日3回程度ﾌﾟﾛｾｯｻ証明が行われる
		processorProvement();
		processorProvement();

		//日々のﾌﾟﾛｾｯｻ証明を通じて近傍は相互に主観的な信用値を持っている

		//各ノードの主張値を同値にするため異常排除型の分散合意をする
		sync();

		double rate = getRate();
		System.out.println("after sync 善意のﾉｰﾄﾞにおける正常値率=" + rate);
		//少し正常値率が低下するのはスパム攻撃ノードが正しく近傍に回答を送信する場合があるから。
		//その場合真っ当に演算量証明できているので善意のノードに影響する
		return rate;
	}

	/**
	 * 各ノードのdataを同調する。異常排除型。
	 * ここで示されているのはプロセッサ証明で得た信用値に基づいて
	 * ダミーノードの意見が無視され
	 * 全ノードの過半数がダミーノードであったとしても
	 * 善意のノードが不正値を信じ込まされる事が無い事を示している。
	 * その性質自体は選挙型であれ異常排除型であれ存在する。
	 */
	private static void sync() {
		//異常排除型ではターンは必須ではなく、各ノード任意のタイミングで近傍から
		//現在の値を取得して自分の値を改めていく、という動作でも全ノードでの同調は達成される。
		//選挙型ではターンが必須となる。
		System.out.println("異常排除型分散合意　開始");
		for (int turn = 0; turn < 20; turn++) {
			//全ての近傍に自分の相互通信情報を送信
			for (Node n : allNodes) {
				for (Edge e : n.getEdges()) {
					//e(近傍関係)で繋がっているgetNeighbor（相手ノード）へn(自分）がgetData（主張値）を送信
					e.getNeighbor().receive(n, n.getData());
				}
			}

			// 局所的多数決（相互作用関数）実行
			for (Node n : allNodes) {
				n.interactionSync(turn);
			}
		}
		System.out.println("異常排除型分散合意　終了");
	}

	/**
	 * 近傍関係
	 */
	private static class Edge {
		/**
		 * 相手ノード
		 */
		private Node neighbor;

		/**
		 * 信用
		 */
		private long credit = 0;
		/**
		 * この近傍に送信したランダム値
		 */
		private String rndStr;

		/**
		 * この近傍から受信したランダム値
		 */
		private String rndStrFromNeighbor;

		public Edge(Node n) {
			neighbor = n;
		}

		public void addCredit(long add) {
			this.credit += add;
		}

		public long getCredit() {
			return credit;
		}

		public Node getNeighbor() {
			return neighbor;
		}

		public String getRndStr() {
			return rndStr;
		}

		public String getRndStrFromNeighbor() {
			return rndStrFromNeighbor;
		}

		public void setRndStr(String rndStr) {
			this.rndStr = rndStr;
		}

		public void setRndStrFromNeighbor(String rndStrFromNeighbor) {
			this.rndStrFromNeighbor = rndStrFromNeighbor;
		}
	}

	/**
	 * P2Pﾈｯﾄﾜｰｸのﾉｰﾄﾞ｡ﾀﾞﾐｰﾉｰﾄﾞ等の問題を無視すれば､
	 * 1つのｺﾝﾋﾟｭｰﾀｰであると捉えておおよそ問題無い｡
	 */
	private static class Node {
		/**
		 * 最低限これだけの信用がないと受信しない
		 */
		private static final int creditThreshold = 20;

		/**
		 * 主張する値｡各ﾀｰﾝでこれが各ﾉｰﾄﾞの相互通信情報として送信されている
		 * とみなせる｡
		 */
		private Long data = 0L;

		/**
		 * 近傍ﾘｽﾄ
		 */
		private List<Edge> edges = new ArrayList<>();

		/**
		 * ダミーノードか
		 */
		private boolean dummy = false;

		/**
		 * 過剰に多くの近傍を持つノードか
		 */
		private boolean overCount = false;

		/**
		 * 近傍以外に送信するノードか
		 */
		private boolean spam = false;

		/**
		 * 近傍からの相互通信情報を一時的に格納する
		 */
		private List<Long> dataFromNeighbors = new ArrayList<Long>();

		public Node(Long data) {
			this.data = data;
		}

		public void add(Node n) {
			if (getEdge(n) == null) {
				Edge e = new Edge(n);
				this.edges.add(e);
			}
		}

		/**
		 * @return thisが主張する値
		 */
		public Long getData() {
			return data;
		}

		public Edge getEdge(Node n) {
			for (Edge e : edges)
				//実際は公開鍵や共通鍵経由でチェックするが
				//サンプルコードでは単純化して同一性チェックで済ませる
				if (e.getNeighbor() == n)
					return e;
			return null;
		}

		public List<Edge> getEdges() {
			return edges;
		}

		/**
		 * 各近傍から受け取ったランダム値で問題関数を作成、回答する
		 */
		public void interactionProcessorProvement() {
			try {
				//近傍からのランダム値をつなげる。
				StringBuilder joinedRndStr = new StringBuilder();
				for (Edge e : edges) {
					if (e.getRndStrFromNeighbor() == null)
						continue;
					joinedRndStr.append(e.getRndStrFromNeighbor());
				}

				//総合された問題作成情報
				ProblemSrcSample totalProblemSrc = new ProblemSrcSample(
						joinedRndStr.toString());
				//そこから問題関数を作成する。
				CPUProvementSample2018 problem = new CPUProvementSample2018(
						totalProblemSrc);

				//回答
				ResultSample result;

				//引数探索（演算量証明）
				if (isDummy()) {
					//System.out.println("ダミーノード攻撃");
					//ダミーノードは回答計算できないのででたらめな答えを作成
					SolveSample dummySolve = new SolveSample();
					result = new ResultSample(dummySolve, totalProblemSrc);
				} else {
					//正常なノードは回答計算する
					result = problem.solve();
				}

				//その1つの回答を全近傍に送信しまとめて演算量証明する
				if (isSpam()) {
					//System.out.println("スパム攻撃");
					//スパム攻撃ノードの場合手あたり次第送る
					for (Node n : allNodes) {
						n.receive(this, result);
					}
				} else {
					//善意のノードの場合近傍のみに送る
					for (Edge e : edges) {
						e.getNeighbor().receive(this, result);
					}
				}
			} catch (Exception e) {
			} finally {
				//一時情報をクリア
				for (Edge e : edges) {
					e.setRndStr(null);
					e.setRndStrFromNeighbor(null);
				}
			}

		}

		/**
		 * 異常排除型の局所的多数決
		 */
		public void interactionSync(int turn) {
			//本番用の動作では信用に基づいて重みをつけて（票数を差別化して）多数決するが
			//サンプルコードではそれをしていない。
			//このサンプルコードはいかにしてダミーノードが低信用となるかを示すものなので

			// 多数決の集計用Map
			Map<Long, Integer> counts = new HashMap<Long, Integer>();
			// 最大得票数
			int max = 0;
			//自分のデータを加える
			dataFromNeighbors.add(data);
			for (Long dataFromNeighbor : dataFromNeighbors) {
				Integer count = counts.get(dataFromNeighbor);
				if (count == null)
					count = 0;
				count += 1;
				counts.put(dataFromNeighbor, count);
				if (count > max)
					max = count;
			}

			// 最大得票数のデータ　同数で複数あるかもしれないのでリストに
			List<Long> topList = new ArrayList<Long>();
			for (Entry<Long, Integer> count : counts.entrySet()) {
				if (max == count.getValue()) {
					topList.add(count.getKey());
				}
			}
			//今回の局所的多数決の結果
			Long output = topList.get(0);
			// 複数のﾃﾞｰﾀが同数の場合､ﾗﾝﾀﾞﾑに選択
			if (topList.size() > 1) {
				Random r = new Random();
				output = topList.get(r.nextInt(topList.size()));
			}

			//自分の主張値にする
			data = output;
			//次のターンの準備
			dataFromNeighbors.clear();
		}

		public boolean isDummy() {
			return dummy;
		}

		public boolean isOverCount() {
			return overCount;
		}

		public boolean isSpam() {
			return spam;
		}

		public void receive(Node sender, Long data) {
			Edge e = getEdge(sender);
			//近傍ではない、または低信用なら受け取らない
			if (e == null || e.getCredit() < creditThreshold)
				return;
			dataFromNeighbors.add(data);
		}

		/**
		 * @param responder	回答者
		 * @param result	回答
		 */
		public void receive(Node responder, ResultSample result) {
			//回答者が近傍一覧にあるかチェックする
			Edge edge = getEdge(responder);
			//さらに自分が送ったランダム値に依存した問題を解いたかをチェックする
			//これで相手が”今作成された問題に回答した”ことも確認できる
			if (edge == null || !result.getProblemSrc().getRndStr()
					.contains(edge.getRndStr())) {
				//ここに来た場合、スパム攻撃あるいは予め回答を計算済みの問題を解いたという事
				//System.out.println("スパム攻撃検出");
				return;
			}

			//近傍の近傍数（同時に演算量証明しようとしているノードの数）
			int neighborNeighborCount = result.getProblemSrc().getRndStr()
					.length() / rndStrLen;
			//近傍の近傍数が多すぎる場合拒否する
			if (neighborNeighborCount > neighborMax) {
				//System.out.println("過剰近傍攻撃検出");
				return;
			}

			try {
				//問題関数を再現する
				CPUProvementSample2018 problem = new CPUProvementSample2018(
						result.getProblemSrc());

				//回答を検証する
				if (problem.verify(result.getSolve())) {
					//信用を追加する。実際は多数の問題を同時に解く等してまとめて大量の信用を獲得できる
					edge.addCredit(10);
					Node n = responder;
					if(n.isDummy() || n.isOverCount() || n.isSpam()) {
						//スパム攻撃は近傍にも答えを送るので正常に信用を獲得できる場合がある
						//スパム攻撃が拒否されるのは近傍以外に送った場合
						//System.out.println("悪意ノードのデータが通過");
					}
				} else {
					//System.out.println("ダミーノード攻撃検出");
					//間違っていたら不正行為へのチャレンジとみなし信用を低下させる
					edge.addCredit(-1);
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 近傍からランダム値を受信する
		 */
		public void receive(Node sender, String rndStr) {
			//ランダム値の長さはソフトウェアの定数によって制限される
			//この制限が全ランダム値の長さから近傍の近傍数の取得を可能にする
			if(rndStr == null || rndStr.length() != rndStrLen)
				return;
			Edge e = getEdge(sender);
			if (e == null)
				return;
			e.setRndStrFromNeighbor(rndStr);
		}

		public void setDummy(boolean dummy) {
			this.dummy = dummy;
		}

		public void setOverCount(boolean overCount) {
			this.overCount = overCount;
		}

		public void setSpam(boolean spam) {
			this.spam = spam;
		}
	}
}
