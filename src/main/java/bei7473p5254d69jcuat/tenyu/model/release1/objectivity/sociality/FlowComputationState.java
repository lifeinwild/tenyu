package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import glb.Glb.*;

/**
 * フロー計算が現在どこまで進んだか
 * @author exceptiontenyu@gmail.com
 *
 */
public class FlowComputationState implements GlbMemberDynamicState {
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
