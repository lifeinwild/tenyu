package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;

public interface SocialityIncomeSharingI extends AdministratedObjectI {

	Long getSenderSocialityId();

	Long getReceiverSocialityId();

	long getSharingNumber();
}
