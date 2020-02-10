package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import jetbrains.exodus.env.*;

public interface HasStore {

	/**
	 * @return	このオブジェクトを永続化するストア
	 */
	abstract ObjectStore<? extends ModelDBI, ? extends Model> getStore(
			Transaction txn);

}
