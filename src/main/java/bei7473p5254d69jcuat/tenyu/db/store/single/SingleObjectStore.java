package bei7473p5254d69jcuat.tenyu.db.store.single;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;

/**
 * インスタンスが1個しかないクラスのストアはこれを目印として実装する。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T>
 */
public interface SingleObjectStore {
	public static Long getDefaultIdStatic() {
		return IdObjectI.getFirstId();
	}
	public static Long getDefaultHidStatic() {
		return HashStore.getFirstHid();
	}

	default Long getDefaultId() {
		return getDefaultIdStatic();
	}

	public static void setup(IdObject o) {
		o.setId(SingleObjectStore.getDefaultIdStatic());
		o.setHid(SingleObjectStore.getDefaultHidStatic());
		o.setSpecifiedId(true);
	}
}
