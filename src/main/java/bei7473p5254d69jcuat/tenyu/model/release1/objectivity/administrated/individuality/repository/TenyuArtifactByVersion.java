package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository;

import java.util.*;

import com.github.zafarkhaja.semver.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.*;
import bei7473p5254d69jcuat.tenyutalk.file.TenyutalkArtifactByVersionFile.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyuArtifactByVersion extends AdministratedObject
		implements TenyuArtifactByVersionI {
	public static final int buildMax = 200;
	public static final long maxSize = 1000L * 1000 * 1000 * 2;
	public static final int normalMax = 200;
	public static final int preMax = 200;

	public static int getBuildmax() {
		return buildMax;
	}

	public static long getMaxsize() {
		return maxSize;
	}

	public static int getNormalmax() {
		return normalMax;
	}

	public static int getPremax() {
		return preMax;
	}

	public static boolean validateVersion(ValidationResult r, String verStr) {
		boolean b = true;
		if (verStr == null) {
			r.add(Lang.TENYU_ARTIFACT, Lang.VERSION, Lang.ERROR_EMPTY);
			b = false;
		} else {
			try {
				Version ver = Version.valueOf(verStr);
			} catch (Exception e) {
				r.add(Lang.TENYU_ARTIFACT, Lang.VERSION, Lang.ERROR_INVALID,
						"version exception messages=" + e);
				b = false;
			}
		}
		return b;
	}

	private String semVerStr;

	private byte[] sign;

	private long signDate;

	private FileSizeAndHash sizeAndHash;

	private Long tenyuArtifactId;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(getRegistererUserId());
		return r;
	}

	public byte[] getFileHash() {
		return sizeAndHash.getFileHash();
	}

	public long getFileSize() {
		return sizeAndHash.getFileSize();
	}

	@Override
	public TenyuArtifactByVersionGui getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new TenyuArtifactByVersionGui(guiName, cssIdPrefix);
	}

	@Override
	public String getSemVerStr() {
		return semVerStr;
	}

	@Override
	public byte[] getSign() {
		return sign;
	}

	public long getSignDate() {
		return signDate;
	}

	/**
	 * @return	成果物のサイズ
	 */
	public long getSize() {
		return sizeAndHash.getFileSize();
	}

	public FileSizeAndHash getSizeAndHash() {
		return sizeAndHash;
	}

	@Override
	public AdministratedObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new TenyuArtifactByVersionStore(txn);
	}

	@Override
	public StoreName getStoreName() {
		return StoreNameObjectivity.TENYU_ARTIFACT_BY_VERSION;
	}

	public TenyuArtifactI getTenyuArtifact() {
		return Glb.getObje().getTenyuArtifact(tas -> tas.get(tenyuArtifactId));
	}

	public TenyuArtifactI getTenyuArtifact(Transaction txn) {
		return new TenyuArtifactStore(txn).get(tenyuArtifactId);
	}

	@Override
	public Long getTenyuArtifactId() {
		return tenyuArtifactId;
	}

	public void setFromNominalSignature(NominalSignatureI s) {
		setSignDate(s.getSignDate());
		setSign(s.getSign());
	}

	public void setSemVerStr(String semVerStr) {
		this.semVerStr = semVerStr;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	public void setSignDate(long signDate) {
		this.signDate = signDate;
	}

	public void setSizeAndHash(FileSizeAndHash sizeAndHash) {
		this.sizeAndHash = sizeAndHash;
	}

	public void setTenyuArtifactId(Long tenyuArtifactId) {
		this.tenyuArtifactId = tenyuArtifactId;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (sizeAndHash != null && !sizeAndHash.validateAtCreate(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (sizeAndHash != null && !sizeAndHash.validateAtUpdate(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof TenyuArtifactByVersion))
			return false;
		boolean b = true;
		TenyuArtifactByVersion o = (TenyuArtifactByVersion) old;
		if (Glb.getUtil().notEqual(getSign(), o.getSign())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.UPLOADER_SIGN,
					Lang.ERROR_UNALTERABLE, "sign=" + Arrays.toString(sign)
							+ " old.sign=" + Arrays.toString(o.getSign()));
			b = false;
		}
		if (Glb.getUtil().notEqual(getSignDate(), o.getSignDate())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.SIGNDATE,
					Lang.ERROR_UNALTERABLE, "signDate=" + getSignDate()
							+ " old.signDate=" + o.getSignDate());
			b = false;
		}
		if (Glb.getUtil().notEqual(getTenyuArtifactId(),
				o.getTenyuArtifactId())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_UNALTERABLE, "tenyuArtifactId=" + tenyuArtifactId
							+ " old.tenyuArtifactId=" + o.getTenyuArtifactId());
			b = false;
		}
		if (Glb.getUtil().notEqual(getFileHash(), o.getFileHash())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.HASH,
					Lang.ERROR_UNALTERABLE,
					"hash=" + Arrays.toString(getFileHash()) + " old.hash="
							+ Arrays.toString(o.getFileHash()));
			b = false;
		}
		if (Glb.getUtil().notEqual(getFileSize(), o.getFileSize())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.FILE_SIZE,
					Lang.ERROR_UNALTERABLE,
					"size=" + getFileSize() + " old.size=" + o.getFileSize());
			b = false;
		}
		//署名がsemverに依存しているから変更不可
		if (Glb.getUtil().notEqual(getSemVerStr(), o.getSemVerStr())) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.VERSION,
					Lang.ERROR_UNALTERABLE, "semVer=" + getSemVerStr()
							+ " old.semVer=" + o.getSemVerStr());
			b = false;
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;

		if (signDate <= 0) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.SIGNDATE,
					Lang.ERROR_INVALID, "signDate=" + signDate);
			b = false;
		}

		if (sign == null) {
			r.add(Lang.TENYU_ARTIFACT, Lang.UPLOADER_SIGN, Lang.ERROR_EMPTY);
			b = false;
		}

		if (!validateVersion(r, semVerStr))
			b = false;

		if (!Model.validateIdStandardNotSpecialId(tenyuArtifactId)) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_INVALID, "tenyuArtifactId=" + tenyuArtifactId);
			b = false;
		}

		if (sizeAndHash == null) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.SIZE_AND_HASH,
					Lang.ERROR_EMPTY);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		TenyuArtifactStore tas = new TenyuArtifactStore(txn);
		if (tas.get(tenyuArtifactId) == null) {
			r.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"tenyuArtifactId=" + tenyuArtifactId);
			b = false;
		}

		if (sizeAndHash != null && !sizeAndHash.validateReference(r, txn))
			b = false;

		if (!searchAndVerify(txn)) {
			r.add(Lang.TENYU_ARTIFACT, Lang.UPLOADER_SIGN, Lang.ERROR_INVALID,
					"sign=" + Arrays.toString(sign));
			b = false;
		}
		return b;
	}

	@Override
	public String toString() {
		return "TenyuArtifactByVersion [semVerStr=" + semVerStr + ", sign="
				+ Arrays.toString(sign) + ", signDate=" + signDate
				+ ", sizeAndHash=" + sizeAndHash + ", tenyuArtifactId="
				+ tenyuArtifactId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((semVerStr == null) ? 0 : semVerStr.hashCode());
		result = prime * result + Arrays.hashCode(sign);
		result = prime * result + (int) (signDate ^ (signDate >>> 32));
		result = prime * result
				+ ((sizeAndHash == null) ? 0 : sizeAndHash.hashCode());
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
		TenyuArtifactByVersion other = (TenyuArtifactByVersion) obj;
		if (semVerStr == null) {
			if (other.semVerStr != null)
				return false;
		} else if (!semVerStr.equals(other.semVerStr))
			return false;
		if (!Arrays.equals(sign, other.sign))
			return false;
		if (signDate != other.signDate)
			return false;
		if (sizeAndHash == null) {
			if (other.sizeAndHash != null)
				return false;
		} else if (!sizeAndHash.equals(other.sizeAndHash))
			return false;
		if (tenyuArtifactId == null) {
			if (other.tenyuArtifactId != null)
				return false;
		} else if (!tenyuArtifactId.equals(other.tenyuArtifactId))
			return false;
		return true;
	}

}
