package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality;

import java.util.*;

public class TotalFlow {
	/**
	 * 次数とフロー。次数はフローネットワークでいくつエッジを辿ったか
	 */
	private Map<Integer, Integer> orderFlow = new HashMap<Integer, Integer>();

	public Map<Integer, Integer> getOrderFlow() {
		return orderFlow;
	}
}
