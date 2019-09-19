package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface SocialityIncomeSharingDBI extends ObjectivityObjectDBI {

	Long getSenderSocialityId();

	Long getReceiverSocialityId();

	long getSharingNumber();
}
