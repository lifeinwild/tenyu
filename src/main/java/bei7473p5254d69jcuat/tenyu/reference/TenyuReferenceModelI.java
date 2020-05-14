package bei7473p5254d69jcuat.tenyu.reference;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia.*;

/**
 * Tenyu基盤ソフトウェアが扱うモデルデータへの参照。
 *
 * 具象クラスで返値の型を具体的なものにする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuReferenceModelI<V extends ModelI>
		extends TenyuReferenceI, TenyupediaObjectI {
	public static final int notificationMessagesMax = 200;

	/**
	 * @return	IDやストア名など、対象を一意に特定するのに必要な情報を入れたbyte[]
	 */
	byte[] getStoreKey();

	StoreName getStoreName();

	void setStoreName(StoreNameObjectivity storeName);

	Long getId();

	void setCache(V cache);

	V getCache();

	void clearCache();

	/**
	 * @return	参照先オブジェクト
	 */
	V getObj();

}
