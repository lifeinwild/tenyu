package bei7473p5254d69jcuat.tenyu.reference;

import java.nio.*;
import java.nio.charset.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * UserやWeb等客観を参照するためのクラス
 * どのノードも持っている（久々にネットワークに参加して同調しきる前とか例外もある）データ。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <V>
 */
public class TenyuReferenceSimple<V extends ModelI>
		implements TenyuReference<V> {

	/**
	 * オブジェクトのストア内ID
	 */
	private Long id;

	/**
	 * ストア名
	 */
	private StoreNameEnum storeName;

	@SuppressWarnings("unused")
	private TenyuReferenceSimple() {
	}

	public TenyuReferenceSimple(Long id, StoreNameEnum storeName) {
		this.id = id;
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
		@SuppressWarnings("rawtypes")
		TenyuReferenceSimple other = (TenyuReferenceSimple) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (storeName != other.storeName)
			return false;
		return true;
	}

	@Override
	public ObjectGui<V> getGui(String guiName, String cssIdPrefix) {
		//return new TenyuReferenceSimple(guiname, cssIdPrefix);
		return null;//TODO
	}

	public Long getId() {
		return id;
	}

	@Override
	public String getNotificationMessage() {
		StringBuilder sb = new StringBuilder();
		//Tenyutalk系では通知メッセージ表示でgetObjはしたくないが
		//これは客観系なので問題ない
		V o = getObj();
		String d = Glb.getUtil().getLocalDateStr(o.getCreateDate());
		sb.append(" ").append(d);
		if (o instanceof IndividualityObjectI) {
			IndividualityObjectI io = (IndividualityObjectI) o;
			sb.append(io.getName()).append(" ").append(io.getExplanation());
		} else {
			sb.append("id=").append(o.getId()).append(" ")
					.append(o.getClass().getSimpleName());
		}
		return sb.substring(0, notificationMessagesMax);
	}

	@Override
	public V getObj() {
		if (storeName == null)
			return null;
		return Glb.getObje().readRet(txn -> {
			Object tmp = storeName.getStore(txn);
			if (tmp == null || !(tmp instanceof ModelStore))
				return null;
			@SuppressWarnings("unchecked")
			ModelStore<? extends ModelI,
					V> s = (ModelStore<? extends ModelI, V>) tmp;
			return s.get(getId());
		});
	}

	@Override
	public byte[] getStoreKey() {
		byte[] snB = storeName.getModelName()
				.getBytes(Charset.forName("UTF-8"));
		byte[] idB = ByteBuffer.allocate(Long.BYTES).putLong(id).array();

		byte[] r = new byte[snB.length + idB.length];
		System.arraycopy(snB, 0, r, 0, snB.length);
		System.arraycopy(idB, 0, r, snB.length, idB.length);

		return r;
	}

	public StoreName getStoreName() {
		if (storeName == null)
			return StoreNameObjectivity.USER;
		return storeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((storeName == null) ? 0 : storeName.hashCode());
		return result;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setStoreName(StoreNameObjectivity storeName) {
		this.storeName = storeName;
	}

	@Override
	public String toString() {
		return "TenyuReferenceSimple [storeName=" + storeName + ", id=" + id
				+ "]";
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
			r.add(Lang.TENYU_REFERENCE_SIMPLE_STORENAME, Lang.ERROR_EMPTY);
			b = false;
		}
		if (id == null) {
			r.add(Lang.TENYU_REFERENCE_SIMPLE_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandard(id)) {
				r.add(Lang.TENYU_REFERENCE_SIMPLE_ID, Lang.ERROR_INVALID,
						"id=" + id);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		ModelStore<?, ?> s = (ModelStore<?, ?>) storeName.getStore(txn);
		if (s.get(id) == null) {
			b = false;
		}
		return b;
	}

}
