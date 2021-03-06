package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.communication.packaging.UserCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;

/**
 * UserからUserへのメッセージ
 * UserMessageListと無関係で、UserEdgeによってやり取りされるもの。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class UserRequest extends Request
		implements UserCommonKeyPackageContent {

	public static abstract class UserResponse extends Response
			implements UserCommonKeyPackageContent {

	}
}
