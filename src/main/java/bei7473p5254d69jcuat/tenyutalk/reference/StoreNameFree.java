package bei7473p5254d69jcuat.tenyutalk.reference;

import org.apache.commons.lang.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * 実行時に追加されていくストアを参照するクラス。
 * コンパイル時に確定していないストアクラスを参照できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StoreNameFree implements StoreName {
	/**
	 * 参照先ストア名
	 * 実行時に追加されていくストアを想定しているのでenumにできない。
	 */
	private String storeName;

	@Override
	public CreativeObjectStore<?, ?> getStore(Transaction txn) {
		try {
			Class<?> storeClass = Class.forName(storeName);
			if (!CreativeObjectStore.class.isInstance(storeClass)) {
				throw new IllegalClassException(
						"not VersionedStore. storeName=" + storeName);
			}
			return (CreativeObjectStore<?, ?>) storeClass
					.getConstructor(Transaction.class).newInstance(txn);
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
			return null;
		}
	}

	@Override
	public String getModelName() {
		return storeName;
	}

}