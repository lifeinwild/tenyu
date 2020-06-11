package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 特定の成果物の特定のバージョンを参照する。
 *
 * 関連：{@link TenyuReferenceArtifactByVersionMajor}
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuReferenceArtifactByVersionSecure
		extends TenyuReferenceArtifactByVersion {

	/**
	 * 参照先オブジェクトのハッシュ値
	 * 客観にもハッシュ値があるが客観が改ざんされるリスクに関係無く
	 * 正しいハッシュ値か確認できる。
	 */
	private byte[] hash;
	private Long tenuArtifactByVersionId;

	@Override
	public Long getId() {
		return tenuArtifactByVersionId;
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion(Transaction txn) {
		TenyuArtifactByVersionStore tabvs = new TenyuArtifactByVersionStore(
				txn);
		TenyuArtifactByVersionI r = tabvs.get(tenuArtifactByVersionId);
		isValid(r, hash);
		return r;
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion() {
		TenyuArtifactByVersionI r = Glb.getObje().getTenyuArtifactByVersion(
				tabvs -> tabvs.get(tenuArtifactByVersionId));
		isValid(r, hash);
		return r;
	}

	public static boolean isValid(TenyuArtifactByVersionI r, byte[] hash) {
		if (!Arrays.equals(r.getFileHash(), hash)) {
			throw new IllegalStateException("invalid hash. from reference="
					+ Arrays.toString(hash) + " from db=" + r.getFileHash());
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuReferenceArtifactByVersionSecure other = (TenyuReferenceArtifactByVersionSecure) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	/**
	 * @return	この参照のGUI表現
	 */
	public TenyuReferenceArtifactSecureGui getGui() {
		return new TenyuReferenceArtifactSecureGui(
				getTenyuArtifactByVersion().getNameByVersion(),
				this.getClass().getSimpleName());
	}

	public byte[] getHash() {
		return hash;
	}

	@Override
	public TenyuArtifactByVersionI getReferenced() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactByVersionI getReferenced(Transaction txn) {
		if (getCache() != null)
			return getCache();

		if (getStoreName() == null)
			return null;

		Object tmp = getStoreName().getStore(txn);
		if (tmp == null || !(tmp instanceof ModelStore))
			return null;
		@SuppressWarnings("unchecked")
		ModelStore<? extends ModelI,
				TenyuArtifactByVersionI> s = (ModelStore<? extends ModelI,
						TenyuArtifactByVersionI>) tmp;
		TenyuArtifactByVersionI r = s.get(getId());

		setCache(r);
		return getCache();
	}

	@Override
	public TenyuReferenceArtifactSecureGui getGuiMyself(String guiName,
			String cssIdPrefix) {
		return new TenyuReferenceArtifactSecureGui(
				TenyuReferenceArtifactByVersionSecure.class.getSimpleName(),
				cssIdPrefix);
	}

	@Override
	public String getSimpleExplanationForUser() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/*
	@Override
	public byte[] getStoreKey() {
		return ByteBuffer.allocate(Long.BYTES + hash.length).putLong(getId())
				.put(hash).array();
	}
	*/

	@Override
	public byte[] getStoreKeyReferenced() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(hash);
		return result;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return "TenyuReferenceArtifactSecure [hash=" + Arrays.toString(hash)
				+ "]";
	}

	@Override
	public boolean validateAtCreateTenyuReferenceArtifactConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtRpcSyntheticTenyuReferenceArtifactConcrete(
			TenyuSingleObjectMessageI m, byte[] addr, ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeTenyuReferenceArtifactConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof TenyuReferenceArtifactByVersionSecure)) {
			return false;
		}
		TenyuReferenceArtifactByVersionSecure o = (TenyuReferenceArtifactByVersionSecure) old;
		boolean b = true;

		if (!Arrays.equals(hash, o.hash)) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_SECURE, Lang.HASH,
					Lang.ERROR_UNALTERABLE, "hash=" + Arrays.toString(hash)
							+ " o.hash=" + Arrays.toString(o.getHash()));
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdateTenyuReferenceArtifactConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;

		if (hash == null) {
			r.add(Lang.HASH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (hash.length != Glb.getConst().getHashSize()) {
				r.add(Lang.HASH, Lang.ERROR_INVALID,
						"hash.length=" + hash.length);
				b = false;
			}
		}

		if (!Model.validateIdStandard(tenuArtifactByVersionId)) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION, Lang.ID,
					Lang.ERROR_INVALID,
					"tenyuArtifactId=" + tenuArtifactByVersionId);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateReferenceTenyuReferenceArtifactConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		TenyuArtifactStore tas = new TenyuArtifactStore(txn);

		if (tas.get(tenuArtifactByVersionId) == null) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION, Lang.ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"tenyuArtifactId=" + tenuArtifactByVersionId);
			b = false;
		}
		return b;
	}

}
