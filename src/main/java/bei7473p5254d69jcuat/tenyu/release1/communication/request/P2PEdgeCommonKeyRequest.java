package bei7473p5254d69jcuat.tenyu.release1.communication.request;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.P2PEdgeCommonKeyPackage.*;

/**
 * P2PEdgeを通した共通鍵暗号化のリクエスト
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class P2PEdgeCommonKeyRequest extends Request
		implements P2PEdgeCommonKeyPackageContent {

	protected abstract boolean validateP2PEdgeCommonKeyConcrete(Message m);

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return validateP2PEdgeCommonKeyConcrete(m);
	}

	public static abstract class P2PEdgeCommonKeyResponse extends Response
			implements P2PEdgeCommonKeyPackageContent {
		protected abstract boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m);

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			return validateP2PEdgeCommonKeyResponseConcrete(m);
		}
	}

}
