package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;

public interface SocialityDBI extends AdministratedObjectDBI {
	byte[] getIndividualityObjectId();
	/**
	 * @return	対応オブジェクトのID
	 */
	Long getIndividualityObjectConcreteId();
	NodeType getNodeType();
	Long getMainAdministratorUserId();
}
