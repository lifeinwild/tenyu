package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 指定されたファイルを含む最後のバージョンを参照する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuReferenceArtifactByVersionFile
		extends TenyuReferenceArtifactByVersion {
	/**
	 * 参照条件となるファイル
	 * 成果物フォルダからのフォルダ名も含む
	 */
	private String dirAndFilename;

	/**
	 * 対象アーティファクト
	 */
	private Long tenyuArtifactId;

	@Override
	public ObjectGui<? extends TenyuReferenceI> getGuiMyself(String guiName,
			String cssIdPrefix) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Long getId() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactByVersionI getReferenced() {
		// TODO 自動生成されたメソッド・スタブ

		// アップロード者またはミラーノードからオブジェクトをDL

		return null;
	}

	@Override
	public TenyuArtifactByVersionI getReferenced(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getSimpleExplanationForUser() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public byte[] getStoreKeyReferenced() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean validateAtCreateTenyuReferenceArtifactConcrete(
			ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateAtRpcSyntheticTenyuReferenceArtifactConcrete(
			TenyuSingleObjectMessageI m, byte[] addr, ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateAtUpdateChangeTenyuReferenceArtifactConcrete(
			ValidationResult r, Object old) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateAtUpdateTenyuReferenceArtifactConcrete(
			ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateReferenceTenyuReferenceArtifactConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((dirAndFilename == null) ? 0 : dirAndFilename.hashCode());
		result = prime * result
				+ ((tenyuArtifactId == null) ? 0 : tenyuArtifactId.hashCode());
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
		TenyuReferenceArtifactByVersionFile other = (TenyuReferenceArtifactByVersionFile) obj;
		if (dirAndFilename == null) {
			if (other.dirAndFilename != null)
				return false;
		} else if (!dirAndFilename.equals(other.dirAndFilename))
			return false;
		if (tenyuArtifactId == null) {
			if (other.tenyuArtifactId != null)
				return false;
		} else if (!tenyuArtifactId.equals(other.tenyuArtifactId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TenyuReferenceArtifactByVersionFile [filename=" + dirAndFilename
				+ ", tenyuArtifactId=" + tenyuArtifactId + "]";
	}

	public String getDirAndFilename() {
		return dirAndFilename;
	}

	public void setDirAndFilename(String dirAndFilename) {
		this.dirAndFilename = dirAndFilename;
	}

	public Long getTenyuArtifactId() {
		return tenyuArtifactId;
	}

	public void setTenyuArtifactId(Long tenyuArtifactId) {
		this.tenyuArtifactId = tenyuArtifactId;
	}

}
