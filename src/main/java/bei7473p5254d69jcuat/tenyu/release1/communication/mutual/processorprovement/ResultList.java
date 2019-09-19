package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement;

import java.util.*;

/**
 * 単純にList<Result>を通信するとサイズが大きすぎるので
 * いくつかの値が同値であることを利用して情報が共通化された構造体を用意する。
 * @author exceptiontenyu@gmail.com
 *
 */
public class ResultList {
	/**
	 * 全問題作成情報の共通部分
	 * 並列番号は設定されていても無意味
	 */
	private ProblemSrc common;
	/**
	 * 並列番号一覧
	 */
	private List<ParallelNumberAndSolve> solves = new ArrayList<>();

	public ResultList() {
	}

	public ResultList(ProblemSrc common) {
		this.common = common;
	}

	public synchronized void register(Integer parallelNumber, Solve s) {
		solves.add(new ParallelNumberAndSolve(parallelNumber, s));
	}

	public ProblemSrc getCommon() {
		return common;
	}

	public List<ParallelNumberAndSolve> getSolves() {
		return solves;
	}

	/**
	 * 並列番号と計算結果
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class ParallelNumberAndSolve {
		private int parallelNumber;
		private Solve solve;

		public ParallelNumberAndSolve() {
		}

		public ParallelNumberAndSolve(int parallelNumber, Solve solve) {
			this.parallelNumber = parallelNumber;
			this.solve = solve;
		}

		public Solve getSolve() {
			return solve;
		}

		public int getParallelNumber() {
			return parallelNumber;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + parallelNumber;
			result = prime * result + ((solve == null) ? 0 : solve.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ParallelNumberAndSolve other = (ParallelNumberAndSolve) obj;
			if (parallelNumber != other.parallelNumber)
				return false;
			if (solve == null) {
				if (other.solve != null)
					return false;
			} else if (!solve.equals(other.solve))
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((common == null) ? 0 : common.hashCode());
		result = prime * result + ((solves == null) ? 0 : solves.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultList other = (ResultList) obj;
		if (common == null) {
			if (other.common != null)
				return false;
		} else if (!common.equals(other.common))
			return false;
		if (solves == null) {
			if (other.solves != null)
				return false;
		} else if (!solves.equals(other.solves))
			return false;
		return true;
	}
}
