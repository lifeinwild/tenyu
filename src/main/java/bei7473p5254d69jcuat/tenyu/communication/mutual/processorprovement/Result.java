package bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement;

/**
 * CPU証明の回答。通信される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Result {
	private Solve solve;
	private ProblemSrc problemSrc;

	public Result() {
	}

	public Result(Solve solve, ProblemSrc problemSrc) {
		this.solve = solve;
		this.problemSrc = problemSrc;
	}

	public ProblemSrc getProblemSrc() {
		return problemSrc;
	}

	public Solve getSolve() {
		return solve;
	}

	public boolean isNotNullDeep() {
		if (solve == null || problemSrc == null)
			return false;
		return solve.isNotNullDeep() && problemSrc.isNotNullDeep();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Result))
			return false;
		Result r = (Result) o;
		return solve.equals(r.getSolve())
				&& problemSrc.equals(r.getProblemSrc());
	}

	public static int getMaxSize() {
		return 1000;
	}
}
