package bei7473p5254d69jcuat.tenyutalk.reference;

import org.apache.commons.lang.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
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
	 * 完全修飾名
	 */
	private String storeName;

	@Override
	public ObjectStore<?, ?> getStore(Transaction txn) {
		try {
			Class<?> storeClass = Class.forName(storeName);
			if (!ObjectStore.class.isInstance(storeClass)) {
				throw new IllegalClassException(
						"not VersionedStore. storeName=" + storeName);
			}
			return (ObjectStore<?, ?>) storeClass
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

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (storeName == null) {
			r.add(Lang.STORE_NAME_FREE, Lang.STORE_NAME, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return validateCommon(r);
	}

}
