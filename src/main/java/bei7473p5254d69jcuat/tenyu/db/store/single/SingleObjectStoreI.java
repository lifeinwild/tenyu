package bei7473p5254d69jcuat.tenyu.db.store.single;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;

/**
 * インスタンスが1個しかないクラスのストアはこれを目印として実装する。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T>
 */
public interface SingleObjectStoreI {
	public static Long getDefaultIdStatic() {
		return ModelI.getFirstId();
	}
	public static Long getDefaultHidStatic() {
		return HashStore.getFirstHid();
	}

	default Long getDefaultId() {
		return getDefaultIdStatic();
	}

	public static void setup(Model o) {
		o.setId(SingleObjectStoreI.getDefaultIdStatic());
		o.setHid(SingleObjectStoreI.getDefaultHidStatic());
		o.setSpecifiedId(true);
	}
}
