package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;

/**
 * {@link ModelStore}にオブジェクトが１件しか登録されない。
 * そのオブジェクトのIDは{@link ModelI#getFirstId()}。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface SingleObjectI extends ModelI{
	@Override
	default boolean isWarningValidation() {
		return true;//Single系はセーブできないと逆にまずい。
	}
}
