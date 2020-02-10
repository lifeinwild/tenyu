package bei7473p5254d69jcuat.tenyutalk.reference;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import bei7473p5254d69jcuat.tenyutalk.ui.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * Tenyutalkのモデルへの参照
 *
 * ハッシュ値を含むので偽のデータを流し込まれる恐れが無い。
 * 参照オブジェクト作成時点で正しい動作を確認できた場合、
 * その後も正しく動作し続ける。
 *
 * 関連：{@link TenyutalkReferenceFlexible}
 *
 * 基本的想定として参照先オブジェクトはプログラム系。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyutalkReferenceSecure<V extends CreativeObject>
		extends TenyutalkReferenceBase<V> {

	/**
	 * 参照先オブジェクトのID
	 */
	private Long id;

	/**
	 * 参照先オブジェクトのハッシュ値
	 */
	private byte[] hash;

	/**
	 * ファイルサイズ
	 */
	private long fileSize;

	public Long getId() {
		return id;
	}

	public String toURL() {
		return getPrefix() + "id=" + getId() + getDelimiter() + "storeName="
				+ getStoreName();
	}

	/**
	 * @return	この参照のGUI表現
	 */
	public TenyutalkReferenceSecureGui<V> getGui() {
		return new TenyutalkReferenceSecureGui<V>(getName(), getCssIdPrefix());
	}

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		if (!(old instanceof TenyutalkReferenceSecure<?>)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.getClass()=" + old.getClass());
			return false;
		}
		TenyutalkReferenceSecure<?> old2 = (TenyutalkReferenceSecure<?>) old;
		boolean b = true;
		if (!super.validateAtUpdateChange(r, old)) {
			b = false;
		}
		if (Glb.getUtil().notEqual(id, old2.getId())) {
			r.add(Lang.TENYUTALK_REFERENCE_SECURE_ID, Lang.ERROR_NOT_EQUAL,
					"this.id=" + id + " old.id=" + old2.getId());
			b = false;
		}
		return b;
	}

	@Override
	public TenyutalkReferenceSecureGui<V> getGui(String guiName,
			String cssIdPrefix) {
		return new TenyutalkReferenceSecureGui<V>(getName(), cssIdPrefix);
	}

	@Override
	public V getObj() {
		if (getStoreName() == null)
			return null;
		return Glb.getObje().readRet(txn -> {
			Object tmp = getStoreName().getStore(txn);
			if (tmp == null || !(tmp instanceof IdObjectStore))
				return null;
			@SuppressWarnings("unchecked")
			IdObjectStore<? extends IdObjectDBI,
					V> s = (IdObjectStore<? extends IdObjectDBI, V>) tmp;
			return s.get(getId());
		});
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (id == null) {
			r.add(Lang.TENYUTALK_REFERENCE_SECURE_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandard(id)) {
				r.add(Lang.TENYUTALK_REFERENCE_SECURE_ID, Lang.ERROR_INVALID,
						"id=" + id);
				b = false;
			}
		}
		if (hash == null) {
			r.add(Lang.TENYUTALK_REFERENCE_SECURE_HASH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (hash.length != Glb.getConst().getHashSize()) {
				r.add(Lang.TENYUTALK_REFERENCE_SECURE_HASH, Lang.ERROR_INVALID,
						"hash.length=" + hash.length);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreateTenyutalkReferenceBaseConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdateTenyutalkReferenceBaseConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateReferenceTenyutalkReferenceBaseConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		//メンバー変数にIDがあるが他ノードの下でしか存在しないIDであり検証不可
		return true;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		result = prime * result + Arrays.hashCode(hash);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyutalkReferenceSecure other = (TenyutalkReferenceSecure) obj;
		if (fileSize != other.fileSize)
			return false;
		if (!Arrays.equals(hash, other.hash))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TenyutalkReferenceSecure [id=" + id + ", hash="
				+ Arrays.toString(hash) + ", fileSize=" + fileSize + "]";
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
