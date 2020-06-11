package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;

public interface SocialityI extends AdministratedObjectI {
	byte[] getIndividualityObjectStoreKey();

	/**
	 * @return	この社会性が対応するモデルへの参照
	 */
	TenyuReferenceModelI<
			? extends IndividualityObjectI> getIndividualityObjectConcreteRef();

	/**
	 * @return	相互評価フローネットワークのノードの種類
	 */
	StoreName getNodeType();

	Long getMainAdministratorUserId();
}
