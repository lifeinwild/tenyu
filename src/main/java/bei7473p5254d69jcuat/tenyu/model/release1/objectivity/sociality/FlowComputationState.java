package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

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

	public Long getLastSocialityId() {
		return lastSocialityId;
	}

	public void setLastSocialityId(Long lastSocialityId) {
		this.lastSocialityId = lastSocialityId;
	}

}
