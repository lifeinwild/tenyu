package bei7473p5254d69jcuat.tenyutalk.reference;

import java.nio.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.ui.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 自動的に最新版を参照できる。
 *
 * {@link TenyutalkReferenceSecure}と比較するとこれはハッシュ値を含まないので
 * アップロード者の側で随時新しいデータを参照させれるという柔軟性が生じるが、
 * 同時にアップロード者によって悪意あるデータを流し込まれる脆弱性も生じている。
 *
 * 基本的想定として参照先オブジェクトは素材系。プログラムは危険。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <V>
 */
public class TenyutalkReferenceFlexible<V extends CreativeObjectI>
		extends TenyutalkReferenceBase<V> {
	/**
	 * オブジェクト作成時点の参照先オブジェクトのバージョン
	 */
	private GeneralVersioning version;

	/**
	 * 参照先オブジェクトの初代オブジェクトのID
	 * 参照先オブジェクトは繰り返し更新されるが、
	 * その更新歴の最初のオブジェクトのID。初代ID
	 */
	private Long firstId;

	/**
	 * メジャーバージョンを無視する
	 * 即ち最新版を使用する
	 */
	private boolean ignoreMajor = false;
	/**
	 * マイナーバージョンを無視する
	 * 同じメジャーバージョンの中で最新版を使用する
	 */
	private boolean ignoreMinor = true;
	/**
	 * パッチバージョンを無視する
	 * 同じメジャーマイナーバージョンの中で最新版を使用する
	 */
	private boolean ignorePatch = true;

	@Override
	public byte[] getStoreKey() {
		byte[] firstIdB = ByteBuffer.allocate(Long.BYTES).putLong(firstId)
				.array();
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES * 3);
		int vBytes = 0;
		if (ignoreMajor) {
			buf.putLong(version.getMajor());
			vBytes += Long.BYTES;
			if (ignoreMinor) {
				buf.putLong(version.getMinor());
				vBytes += Long.BYTES;
				if (ignorePatch) {
					buf.putLong(version.getPatch());
					vBytes += Long.BYTES;
				}
			}
		}
		byte[] vB = buf.array();
		byte[] r = new byte[firstIdB.length + vBytes];
		System.arraycopy(firstIdB, 0, r, 0, firstIdB.length);
		System.arraycopy(vB, 0, r, firstIdB.length, vBytes);
		return r;
	}

	@Override
	public Long getId() {
		return firstId;
	}

	@Override
	public TenyutalkReferenceFlexibleGui<V> getGui(String guiName,
			String cssIdPrefix) {
		return new TenyutalkReferenceFlexibleGui<V>(guiName, cssIdPrefix);
	}

	@Override
	public TenyutalkReferenceBaseGui<V> getGui() {
		return new TenyutalkReferenceFlexibleGui<V>(getName(),
				getCssIdPrefix());
	}

	@Override
	public V getObj() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (firstId == null) {
			r.add(Lang.TENYUTALK_REFERENCE_FLEXIBLE_FIRST_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandard(firstId)) {
				r.add(Lang.TENYUTALK_REFERENCE_FLEXIBLE_FIRST_ID,
						Lang.ERROR_INVALID, "firstId=" + firstId);
				b = false;
			}
		}
		if (version == null) {
			r.add(Lang.TENYUTALK_REFERENCE_FLEXIBLE_VERSION, Lang.ERROR_EMPTY);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeTenyutalkReferenceBaseConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof TenyutalkReferenceFlexible<?>)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.getClass()=" + old.getClass());
			return false;
		}
		TenyutalkReferenceFlexible<
				?> old2 = (TenyutalkReferenceFlexible<?>) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(firstId, old2.getFirstId())) {
			r.add(Lang.TENYUTALK_REFERENCE_FLEXIBLE_FIRST_ID,
					Lang.ERROR_NOT_EQUAL, "this.firstId=" + firstId
							+ " old.firstId=" + old2.getFirstId());
			b = false;
		}

		if (!version.validateAtUpdateChange(r, old2)) {
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateTenyutalkReferenceBaseConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			if (!version.validateAtCreate(r)) {
				b = false;
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateTenyutalkReferenceBaseConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			if (!version.validateAtUpdate(r)) {
				b = false;
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateReferenceTenyutalkReferenceBaseConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (validateCommon(r)) {
			if (!version.validateReference(r, txn)) {
				b = false;
			}
		} else {
			b = false;
		}
		return b;
	}

	public GeneralVersioning getVersion() {
		return version;
	}

	public void setVersion(GeneralVersioning version) {
		this.version = version;
	}

	public Long getFirstId() {
		return firstId;
	}

	public void setFirstId(Long firstId) {
		this.firstId = firstId;
	}

	public boolean isIgnoreMajor() {
		return ignoreMajor;
	}

	public void setIgnoreMajor(boolean ignoreMajor) {
		this.ignoreMajor = ignoreMajor;
	}

	public boolean isIgnoreMinor() {
		return ignoreMinor;
	}

	public void setIgnoreMinor(boolean ignoreMinor) {
		this.ignoreMinor = ignoreMinor;
	}

	public boolean isIgnorePatch() {
		return ignorePatch;
	}

	public void setIgnorePatch(boolean ignorePatch) {
		this.ignorePatch = ignorePatch;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((firstId == null) ? 0 : firstId.hashCode());
		result = prime * result + (ignoreMajor ? 1231 : 1237);
		result = prime * result + (ignoreMinor ? 1231 : 1237);
		result = prime * result + (ignorePatch ? 1231 : 1237);
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		TenyutalkReferenceFlexible other = (TenyutalkReferenceFlexible) obj;
		if (firstId == null) {
			if (other.firstId != null)
				return false;
		} else if (!firstId.equals(other.firstId))
			return false;
		if (ignoreMajor != other.ignoreMajor)
			return false;
		if (ignoreMinor != other.ignoreMinor)
			return false;
		if (ignorePatch != other.ignorePatch)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TenyutalkReferenceFlexible [version=" + version + ", firstId="
				+ firstId + ", ignoreMajor=" + ignoreMajor + ", ignoreMinor="
				+ ignoreMinor + ", ignorePatch=" + ignorePatch + "]";
	}

}
