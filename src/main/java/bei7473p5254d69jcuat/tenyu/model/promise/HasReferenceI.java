package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.reference.*;

/**
 * ユーザーレベルで参照されうるもの
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface HasReferenceI {
	TenyuReferenceModelI<?> getReference();
}
