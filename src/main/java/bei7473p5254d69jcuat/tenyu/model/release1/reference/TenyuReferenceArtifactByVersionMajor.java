package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 自動的に最新版を参照できる。設定次第でメジャーバージョンは無視されない。
 *
 * このフレキシブルな参照は
 * {@link TenyuArtifactByVersionI#getTenyuArtifactId()}が同じ範囲で
 * 最新版を参照する。
 *
 * 関連：{@link TenyuReferenceArtifactByVersionSecure}
 *
 * @author exceptiontenyu@gmail.com
 */
public class TenyuReferenceArtifactByVersionMajor
		extends TenyuReferenceArtifactByVersion {
	/**
	 * 想定するメジャーバージョン
	 * 初期値ならメジャーバージョンの違いを無視した最新版を参照する
	 * 1以上の数値が指定された場合、そのメジャーバージョンの中の最新版が参照される
	 */
	private int major = 0;

	/**
	 * この成果物の最新版が取得される
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
		return tenyuArtifactId;
	}

	public int getMajor() {
		return major;
	}

	@Override
	public TenyuArtifactByVersionI getReferenced() {
		return getTenyuArtifactByVersion();
	}

	@Override
	public TenyuArtifactByVersionI getReferenced(Transaction txn) {
		return getTenyuArtifactByVersion(txn);
	}

	@Override
	public String getSimpleExplanationForUser() {
		return "Secure Ref: " + getTenyuArtifactByVersion().getNameByVersion();
	}

	@Override
	public byte[] getStoreKeyReferenced() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion(Transaction txn) {
		TenyuArtifactByVersionStore tabvs = new TenyuArtifactByVersionStore(
				txn);
		if (major > 0) {
			return tabvs.getLastVersion();
		} else {
			return tabvs.getLastVersion();
		}
	}

	@Override
	public TenyuArtifactByVersionI getTenyuArtifactByVersion() {
		if (major > 0) {
			return Glb.getObje()
					.getTenyuArtifactByVersion(tabvs -> tabvs.getLatest(major));
		} else {
			return Glb.getObje()
					.getTenyuArtifactByVersion(tabvs -> tabvs.getLastVersion());
		}

		/*
		List<TenyuArtifactByVersionI> candidates = Glb.getObje()
				.getTenyuArtifactByVersion(
						tabvs -> tabvs.getByTenyuArtifactId(tenyuArtifactId));
		if (candidates == null || candidates.size() == 0)
			return null;
		//バージョンでソート
		Collections.sort(candidates, new Comparator<TenyuArtifactByVersionI>() {
			@Override
			public int compare(TenyuArtifactByVersionI o1,
					TenyuArtifactByVersionI o2) {
				Version v1 = null;
				try {
					v1 = Version.valueOf(o1.getSemVerStr());
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					return -1;
				}

				Version v2 = null;
				try {
					v2 = Version.valueOf(o2.getSemVerStr());
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					return 1;
				}

				int majorDistance = v1.getMajorVersion() - v2.getMajorVersion();
				if (majorDistance != 0)
					return majorDistance;

				int minorDistance = v1.getMinorVersion() - v2.getMinorVersion();
				if (minorDistance != 0)
					return minorDistance;

				int patchDistance = v1.getPatchVersion() - v2.getPatchVersion();
				if (patchDistance != 0)
					return patchDistance;

				return 0;
			}
		});

		//最新版を返す
		return candidates.get(candidates.size() - 1);
		*/
	}

	public boolean isIgnoreMajor() {
		return major == 0;
	}

	public void setMajor(int major) {
		this.major = major;
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
		TenyuReferenceArtifactByVersionMajor o = (TenyuReferenceArtifactByVersionMajor) old;
		boolean b = true;
		/*
		if (!getFirstId().equals(o.getFirstId())) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_FLEXIBLE, Lang.FIRST_ID,
					Lang.ERROR_UNALTERABLE,
					"firstId=" + getFirstId() + " o.firstId=" + o.getFirstId());
			b = false;
		}
		*/

		return b;
	}

	@Override
	public boolean validateAtUpdateTenyuReferenceArtifactConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (!Model.validateIdStandard(tenyuArtifactId)) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_FLEXIBLE,
					Lang.TENYU_ARTIFACT_ID, Lang.ERROR_INVALID,
					"firstId=" + tenyuArtifactId);
			b = false;
		}
		if (major <= 0) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_FLEXIBLE, Lang.VERSION_MAJOR,
					Lang.ERROR_INVALID, "major=" + major);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateReferenceTenyuReferenceArtifactConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		TenyuArtifactStore tas = new TenyuArtifactStore(txn);
		if (tas.get(tenyuArtifactId) == null) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_FLEXIBLE,
					Lang.TENYU_ARTIFACT_ID, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"tenyuArtifactId=" + tenyuArtifactId);
			b = false;
		}

		return b;
	}

	public Long getTenyuArtifactId() {
		return tenyuArtifactId;
	}

	public void setTenyuArtifactId(Long tenyuArtifactId) {
		this.tenyuArtifactId = tenyuArtifactId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + major;
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
		TenyuReferenceArtifactByVersionMajor other = (TenyuReferenceArtifactByVersionMajor) obj;
		if (major != other.major)
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
		return "TenyuReferenceArtifactByVersionMajor [major=" + major
				+ ", tenyuArtifactId=" + tenyuArtifactId + "]";
	}

}
