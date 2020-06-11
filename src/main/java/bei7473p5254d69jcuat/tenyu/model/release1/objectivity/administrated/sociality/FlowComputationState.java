package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality;

import glb.Glb.*;

/**
 * フロー計算が現在どこまで進んだか
 * @author exceptiontenyu@gmail.com
 *
 */
public class FlowComputationState implements GlbMemberDynamicState {
	/**
	 * 各ノードへの到達フローは、便宜的には、
	 * 共同主体から出た一次フローの合計を１とした時の相対値として計算されるが、
	 * 実際にはdoubleの値の範囲を活用するためにmultiplierがかけられる。
	 */
	public static final double multiplier = 10E100;

	/**
	 * 計算が完了した最後の社会性ID
	 */
	private Long lastSocialityId;

	/**
	 * @return	次に計算すべき社会性ID
	 */
	public Long nextSocialityId() {
		if (lastSocialityId == null)
			return 0L;
		return lastSocialityId + 1;
	}

	public Long getLastSocialityId() {
		return lastSocialityId;
	}

	public void setLastSocialityId(Long lastSocialityId) {
		this.lastSocialityId = lastSocialityId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lastSocialityId == null) ? 0 : lastSocialityId.hashCode());
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
		FlowComputationState other = (FlowComputationState) obj;
		if (lastSocialityId == null) {
			if (other.lastSocialityId != null)
				return false;
		} else if (!lastSocialityId.equals(other.lastSocialityId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FlowComputationState [lastSocialityId=" + lastSocialityId + "]";
	}

}
