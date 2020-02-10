package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;

public interface SocialityIncomeSharingDBI extends AdministratedObjectDBI {

	Long getSenderSocialityId();

	Long getReceiverSocialityId();

	long getSharingNumber();
}
