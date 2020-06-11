package bei7473p5254d69jcuat.tenyu.model.promise.reference;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import jetbrains.exodus.env.*;

/**
 * Tenyu基盤ソフトウェアが扱うモデルデータへの参照。
 *
 * @author exceptiontenyu@gmail.com
 * @param <V>	返値の型
 *
 */
public interface TenyuReferenceModelI<V extends ObjectI>
		extends TenyuReferenceI, TenyupediaObjectI<V> {
	public static final int notificationMessagesMax = 200;

	@Override
	default ObjectGui<?> getGuiReferenced(String guiName, String cssIdPrefix) {
		V m = getReferenced();
		if (m == null)
			return null;
		return m.getGuiReferenced(guiName, cssIdPrefix);
	}

	@Override
	default V getObj() {
		return getReferenced();
	}

	/**
	 * @return	IDやストア名など参照先を一意に特定するのに必要な情報を入れたbyte[]
	 */
	byte[] getStoreKeyReferenced();

	StoreName getStoreName();

	Long getId();

	default void setCache(V cache) {

	}

	default V getCache() {
		return null;
	}

	default void clearCache() {

	}

	/**
	 * @return	参照先オブジェクト
	 */
	V getReferenced();

	V getReferenced(Transaction txn);

}
