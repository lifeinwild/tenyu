package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.vote;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 影響割合を決定する分散合意の結果
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuManagerElectionResult {
	private long historyIndex;
	private Map<Long, Double> powers;

	@SuppressWarnings("unused")
	private TenyuManagerElectionResult() {
	}

	public TenyuManagerElectionResult(long historyIndex,
			Map<Long, Double> powers) {
		this.historyIndex = historyIndex;
		this.powers = powers;
	}

	@Override
	public String toString() {
		return "historyIndex=" + historyIndex + " powers=" + powers;
	}

	public boolean validate() {
		boolean r = historyIndex >= 0 && powers != null && powers.size() > 0;
		if (!r)
			return false;

		//有効なUserの影響割合合計
		double total = 0;
		for (Entry<Long, Double> e : powers.entrySet()) {
			if (e.getKey() < IdObjectDBI.getFirstRecycleId()) {
				continue;
			}

			total += e.getValue();
		}
		if (total < 0.5)
			return false;

		return true;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public Map<Long, Double> getPowers() {
		return powers;
	}
}