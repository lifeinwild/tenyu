package bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement;

import java.util.*;

/**
 * CPU証明の回答に含まれる出力値及び探索された引数
 * @author exceptiontenyu@gmail.com
 *
 */
public class Solve {
	private byte[] output;
	private long argL;
	private double argD;

	public Solve() {
	}

	public Solve(byte[] output, long argL, double argD) {
		this.output = output;
		this.argL = argL;
		this.argD = argD;
	}

	public double getArgD() {
		return argD;
	}

	public long getArgL() {
		return argL;
	}

	public byte[] getOutput() {
		return output;
	}

	public boolean isNotNullDeep() {
		if (output == null)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(argD);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (argL ^ (argL >>> 32));
		result = prime * result + Arrays.hashCode(output);
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
		Solve other = (Solve) obj;
		if (Double.doubleToLongBits(argD) != Double
				.doubleToLongBits(other.argD))
			return false;
		if (argL != other.argL)
			return false;
		if (!Arrays.equals(output, other.output))
			return false;
		return true;
	}
}
