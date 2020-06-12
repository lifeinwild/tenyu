package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.content;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * tenyu基盤ソフトウェアのメタデータ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuPlatformSoftware implements ValidatableI {

	/**
	 * ファイルのメタデータへの参照
	 */
	private Long tenyuArtifactByVersionId;

	/**
	 * リリース番号
	 * 必ず更新のたびにインクリメントされる。
	 * これが異なっている事で今実行中のソフトウェアが最新版ではないと分かるので
	 */
	private int release;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuPlatformSoftware other = (TenyuPlatformSoftware) obj;
		if (release != other.release)
			return false;
		if (tenyuArtifactByVersionId == null) {
			if (other.tenyuArtifactByVersionId != null)
				return false;
		} else if (!tenyuArtifactByVersionId
				.equals(other.tenyuArtifactByVersionId))
			return false;
		return true;
	}

	public int getRelease() {
		return release;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + release;
		result = prime * result + ((tenyuArtifactByVersionId == null) ? 0
				: tenyuArtifactByVersionId.hashCode());
		return result;
	}

	public void setRelease(int release) {
		this.release = release;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (release <= 0) {
			r.add(Lang.TENYU_PLATFORM_FILE_RELEASE, Lang.ERROR_INVALID);
			b = false;
		}
		if (release > 1) {
			if (tenyuArtifactByVersionId == null) {
				r.add(Lang.TENYU_PLATFORM_SOFTWARE,
						Lang.TENYU_PLATFORM_SOFTWARE_ARTIFACT_BY_VERSION_ID,
						Lang.ERROR_EMPTY);
				b = false;
			} else if (!Model.validateIdStandard(tenyuArtifactByVersionId)) {
				r.add(Lang.TENYU_PLATFORM_SOFTWARE,
						Lang.TENYU_PLATFORM_SOFTWARE_ARTIFACT_BY_VERSION_ID,
						Lang.ERROR_EMPTY);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
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
		if (!validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		TenyuArtifactByVersionStore tabvs = new TenyuArtifactByVersionStore(
				txn);
		if (tabvs.get(tenyuArtifactByVersionId) == null) {
			r.add(Lang.TENYU_PLATFORM_SOFTWARE,
					Lang.TENYU_PLATFORM_SOFTWARE_ARTIFACT_BY_VERSION_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"tenyuArtifactByVersionId=" + tenyuArtifactByVersionId);
			b = false;
		}

		return b;
	}

	@Override
	public String toString() {
		return "TenyuPlatformSoftware [tenyuArtifactByVersionId="
				+ tenyuArtifactByVersionId + ", release=" + release + "]";
	}

	public TenyuArtifactByVersion getTenyuArtifactByVersion() {
		return Glb.getObje().getTenyuArtifactByVersion(
				tabvs -> tabvs.get(tenyuArtifactByVersionId));
	}

	public Long getTenyuArtifactByVersionId() {
		return tenyuArtifactByVersionId;
	}

	public void setTenyuArtifactByVersionId(Long tenyuArtifactByVersionId) {
		this.tenyuArtifactByVersionId = tenyuArtifactByVersionId;
	}

}