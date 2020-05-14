package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import jetbrains.exodus.env.*;

public interface HasStoreI {

	/**
	 * @return	このオブジェクトを永続化するストア
	 */
	abstract ObjectStore<? extends ModelI, ? extends Model> getStore(
			Transaction txn);

}
