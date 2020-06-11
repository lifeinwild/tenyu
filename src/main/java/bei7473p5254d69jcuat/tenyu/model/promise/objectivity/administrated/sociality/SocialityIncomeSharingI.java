package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;

public interface SocialityIncomeSharingI extends AdministratedObjectI {

	Long getSenderSocialityId();

	Long getReceiverSocialityId();

	long getSharingNumber();
}
