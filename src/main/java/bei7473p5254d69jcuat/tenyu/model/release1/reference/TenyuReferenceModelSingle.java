package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import java.nio.charset.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyu.ui.model.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link StoreNameEnum}のうち
 * {@link StoreNameObjectivity}または{@link StoreNameSingle}のいずれか
 * のデータを参照する。
 * どのノードも持っている（久々にネットワークに参加して同調しきる前とか例外もある）データ。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <V>
 */
public class TenyuReferenceModelSingle<V extends ModelI>
		implements TenyuReferenceModelI<V>,SimpleReferenceI {

	/**
	 * ストア名
	 */
	private StoreNameEnum storeName;

	public Long getId() {
		return ModelI.getFirstId();
	}

	@Override
	public boolean validateAtRpcSynthetic(TenyuSingleObjectMessageI m,
			byte[] addr, ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public ObjectGui<? extends TenyuReferenceI> getGuiMyself(String guiName,
			String cssIdPrefix) {
		return new TenyuReferenceModelSingleGui(guiName, cssIdPrefix);
	}

	public void clearCache() {
	}

	public void setCache(V cache) {
		throw new UnsupportedOperationException();
	}

	public V getCache() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unused")
	private TenyuReferenceModelSingle() {
	}

	public TenyuReferenceModelSingle(StoreNameSingle storeName) {
		this.storeName = storeName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuReferenceModelSingle other = (TenyuReferenceModelSingle) obj;
		if (storeName == null) {
			if (other.storeName != null)
				return false;
		} else if (!storeName.equals(other.storeName))
			return false;
		return true;
	}

	@Override
	public String getSimpleExplanationForUser() {
		return "storeName=" + storeName.name();
	}

	@Override
	public V getReferenced() {
		if (storeName == null)
			return null;
		return Glb.getObje().readRet(txn -> {
			return getReferenced(txn);
		});
	}

	@Override
	public V getReferenced(Transaction txn) {
		if (storeName == null)
			return null;

		ObjectStore<?, ?> tmp = storeName.getStore(txn);
		if (tmp == null || !(tmp instanceof ModelStore))
			return null;
		@SuppressWarnings("unchecked")
		ModelStore<? extends ModelI,
				V> s = (ModelStore<? extends ModelI, V>) tmp;
		return s.get(getId());
	}

	@Override
	public byte[] getStoreKeyReferenced() {
		return storeName.getModelName().getBytes(Charset.forName("UTF-8"));
	}

	public StoreName getStoreName() {
		return storeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((storeName == null) ? 0 : storeName.hashCode());
		return result;
	}

	public void setStoreName(StoreNameEnum storeName) {
		this.storeName = storeName;
	}

	@Override
	public String toString() {
		return "TenyuReferenceModelSingle [storeName=" + storeName + "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (storeName == null) {
			r.add(Lang.TENYU_REFERENCE_MODEL_SIMPLE_STORENAME,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!(storeName instanceof StoreNameSingle)) {
				r.add(Lang.TENYU_REFERENCE_MODEL_SIMPLE_STORENAME,
						Lang.ERROR_INVALID, "storeName=" + storeName);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (getReferenced(txn) == null) {
			r.add(Lang.TENYU_REFERENCE_MODEL_SIMPLE_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE, toString());
			b = false;
		}
		return b;
	}

	@Override
	public long getUpdateDate() {
		V obj = getReferenced();
		return obj.getUpdateDate();
	}

	@Override
	public long getCreateDate() {
		V obj = getReferenced();
		return obj.getCreateDate();
	}

}
