package bei7473p5254d69jcuat.tenyu.sample;

import java.util.*;

/**
 * これは2015年に書いたサンプルコード。
 * 多分こちらの方が分かり易い。私の最初の素朴な理解がこれだったから。
 * 同値型（異常排除型）と平均型（選挙型）がありどちらも動作する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVoteSample2015 {

	public static void main(String[] args) {
		// 同値型（異常排除型）。いわゆる多数決
		// 数ターンで値が1種類に統一（収束）される。
		// コメントアウトしているがこれも動作する
		//synctest();

		// 平均型（選挙型）。
		// vari(分散)が0になる事は全ノードが1つの値に収束した事を意味する
		// しかもその値は全体で多数決をした場合と近い値となる
		avetest();
	}

	/**
	 * @param nodes	ノード一覧
	 * @return	平均
	 */
	private static double ave(List<Node> nodes) {
		long nodesum = 0;
		for (Node n : nodes) {
			nodesum += n.getData();
		}
		return nodesum / nodes.size();
	}

	/**
	 * @param nodes		ノード一覧
	 * @param nodeave	これらノードの平均
	 * @return		分散
	 */
	private static double vari(List<Node> nodes, double nodeave) {
		double varisum = 0;
		for (Node n : nodes) {
			varisum += Math.pow(nodeave - n.getData(), 2);
		}
		return varisum / nodes.size();
	}

	private static double devi(double data, double ave, double vari) {
		return (10 * (data - ave) / Math.sqrt(vari)) + 50;
	}

	/*
	 * 距離的なデータ（数値）について平均化のような局所的多数決（相互作用関数）
	 */
	private static void avetest() {
		// 全体ノード作成
		List<Node> nodes = new ArrayList<>();
		Random r = new Random();
		// ノードの所持データをランダムに作成
		for (int i = 0; i < 2000; i++) {
			Node n = new Node(new Long(r.nextInt(200000000)));
			nodes.add(n);
		}
		// 各ノードの近傍をランダムに作成
		for (Node n : nodes) {
			n.setSynchronizedData(n.getData());
			for (int j = 0; j < r.nextInt(200) + 20; j++) {
				Node neighbor = nodes.get(r.nextInt(nodes.size() - 1));
				if (neighbor != null)
					n.add(neighbor);
			}
		}
		// 繰り返し近傍の平均値を自分の値とする。
		int maxturn = 20;
		for (int turn = 0; turn < maxturn; turn++) {
			System.out.println("turn:" + turn);

			// 全体の平均と分散
			double nodesAve = ave(nodes);
			double nodesVari = vari(nodes, nodesAve);
			System.out.println("ave:" + (long) nodesAve);
			System.out.println("vari:" + (long) nodesVari + (nodesVari == 0 ? " 収束完了" : ""));

			// 最終ターンでthresholdが0。
			// 有効近傍が偏差値50限定になり全体分散０が保証される。
			double threshold = maxturn - turn - 1;

			// 距離同調。近傍で高偏差値を無視しながら平均化し分散を収束させる。
			for (Node n : nodes) {
				// 近傍の平均と分散
				long neighave = (long) ave(n.getNeighbors());
				double neighvari = vari(n.getNeighbors(), neighave);

				//	System.out.println("分散：" + neighvari);

				// 有効近傍の平均。自分のデータも反映させる
				List<Node> valid = new ArrayList<>();
				valid.add(n);
				int ignored = 0;
				for (Node neighbor : n.getNeighbors()) {
					// 異常値を無視
					double score = Math
							.abs(devi(neighbor.getData(), neighave, neighvari));
					//					System.out.println("偏差値:"+score);
					if ((score - 50) > threshold) {
						ignored++;
						continue;
					}
					valid.add(neighbor);
				}
				//	System.out.println("検証を通過しなかった値の数:"+ignored);
				//	System.out.println("検証を通過した値の数:"+valid.size());
				n.setSynchronizedData((long) ave(valid));
			}
			// 一斉にこのターンで得たデータを自身のデータとする
			for (Node n : nodes) {
				n.setData(n.getSynchronizedData());
			}
		}
		System.out.println("end");
	}

	/*
	 * ノード２０００エッジ２００で同調５回で統一
	 * ノード２００００エッジ２００で同調６回で統一
	 * ノード２０００００エッジ２００で同調７回で統一
	 * 実際にはデータが均等にばらけることは無いのでもっと早い
	 * 実際にはエッジはランダムではなく安定したノードに集中する
	 */
	private static void synctest() {
		// 全体ノード作成
		List<Node> nodes = new ArrayList<>();
		Random r = new Random();
		// ノードの所持データをランダムに作成
		for (int i = 0; i < 2000; i++) {
			Node n = new Node(new Long(r.nextInt(10)));
			nodes.add(n);
		}
		// 各ノードの近傍をランダムに作成
		for (Node n : nodes) {
			n.setSynchronizedData(n.getData());
			for (int j = 0; j < r.nextInt(200) + 20; j++) {
				Node neighbor = nodes.get(r.nextInt(nodes.size() - 1));
				if (neighbor != null)
					n.add(neighbor);
			}
		}
		// 繰り返し相互作用する
		for (int turn = 0; turn < 10; turn++) {
			// モニタリング用。全ノードで　データ：ノード数
			Map<Long, Integer> moni = new HashMap<Long, Integer>();
			for (Node n : nodes) {
				Long data = n.getData();
				Integer count = moni.get(data);
				if (count == null)
					count = 0;
				count++;
				moni.put(data, count);
			}
			System.out.println(turn);
			System.out.println(moni + (moni.size() == 1 ? "収束完了" : ""));
			// 近傍で多数決
			for (Node n : nodes) {
				// 近傍で　データ：ノード数
				Map<Long, Integer> m = new HashMap<Long, Integer>();
				// 自分の票
				int max = 1;// 最大ノード数
				m.put(n.getData(), max);
				// 近傍の票
				for (Node neighbor : n.getNeighbors()) {
					Long data = neighbor.getData();
					Integer count = m.get(data);
					if (count == null)
						count = 0;
					count++;
					m.put(data, count);
					if (count > max) {
						max = count;
					}
				}
				// 最大ノード数の全データをリスト化
				List<Long> tmp = new ArrayList<Long>();
				for (Long key : m.keySet()) {
					Integer count = m.get(key);
					if (max == count) {
						tmp.add(key);
					}
				}
				// 複数のデータが同数の場合、ランダムに選択
				Long sync = tmp.get(0);
				if (tmp.size() > 1) {
					sync = tmp.get(r.nextInt(tmp.size()));
				}
				n.setSynchronizedData(sync);
			}
			// 一斉にこのターンで得たデータを自身のデータとする
			for (Node n : nodes) {
				n.setData(n.getSynchronizedData());
			}
		}
	}

	private static class Node {
		/**
		 * 各ノードは1つ自分の値を持つ
		 */
		private Long data = 0L;
		/**
		 * 近傍から来た値と自分の値で作成した値
		 * 各ターンの結果
		 */
		private Long synchronizedData = 0L;
		/**
		 * 近傍
		 */
		private List<Node> neighbors = new ArrayList<>();

		public Node(Long data) {
			this.data = data;
		}

		public void add(Node n) {
			if (neighbors.indexOf(n) == -1)
				this.neighbors.add(n);
		}

		public void setSynchronizedData(Long synchronizedData) {
			this.synchronizedData = synchronizedData;
		}

		public Long getSynchronizedData() {
			return synchronizedData;
		}

		public List<Node> getNeighbors() {
			return neighbors;
		}

		public void setData(Long data) {
			this.data = data;
		}

		public Long getData() {
			return data;
		}

		public String toString() {
			return Long.toString(synchronizedData);
		}
	}

}
