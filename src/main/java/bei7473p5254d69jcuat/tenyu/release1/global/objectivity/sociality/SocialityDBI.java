package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface SocialityDBI extends ObjectivityObjectDBI {
	byte[] getNaturalityId();
	/**
	 * @return	naturalityIdのrecycleId部分
	 */
	Long getNaturalityConcreteRecycleId();
	NodeType getNodeType();
	Long getMainAdministratorUserId();
}
