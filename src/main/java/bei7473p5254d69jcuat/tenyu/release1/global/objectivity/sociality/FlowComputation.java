package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.release1.global.Glb.*;

/**
 * 内部状態を持たないフロー計算用のユーティリティクラス。
 * @author exceptiontenyu@gmail.com
 *
 */
public class FlowComputation implements GlbMemberDynamicState{
	/**
	 * フロー計算
	 * @param source ここから各ノードへのフローが計算される
	 * @param man 全エッジ情報
	 * @return
	 */
	/* TODO エラーを消したかったので一旦コメントアウト
	public Map<Node, Double> compute(Node source, EdgeManager man) {
		Map<Node, Double> flows = new HashMap<>();
		Collection<Edge> edges = man.getEdges(source);

		recursiveCompute(flows, source, edges, 1D, 0, 6, man);

		return flows;
	}

	private void recursiveCompute(Map<Node, Double> flows, Node from,
			Collection<Edge> edges, Double input, int order, final int maxOrder,
			final EdgeManager man) {
		if (order >= maxOrder)
			return;
		if (input == null || flows == null || from == null || edges == null)
			return;
		if (input <= 0)
			return;

		//fromからのエッジの合計重みを計算する
		int total = 0;
		for (Edge e : edges) {
			Integer w = e.getWeight();
			if (w == null)
				continue;
			if (w <= 0)
				continue;
			total += w;
		}
		if (total <= 0) {
			return;
		}

		//fromからエッジを受けている全ノードにスコアを加算する
		for (Edge e : edges) {
			//データを揃える
			Node to = e.getPubToAddr();
			Integer w = e.getWeight();
			if (to == null || w == null)
				continue;
			if (to.equals(from) || w <= 0)
				continue;

			//スコア加算
			double percent = e.getWeight() / total;
			Double score = flows.get(to);
			if (score == null) {
				score = 0D;
			}
			score += input / percent;

			//深さ優先
			recursiveCompute(flows, to, man.getEdges(to), score, order + 1,
					maxOrder, man);
		}
	}
	*/
}
