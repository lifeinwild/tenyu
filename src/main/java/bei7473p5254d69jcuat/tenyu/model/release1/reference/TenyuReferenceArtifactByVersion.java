package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link TenyuArtifactByVersionI}への参照
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TenyuReferenceArtifactByVersion
		implements TenyuReferenceModelI<TenyuArtifactByVersionI> {

	public static final int mediaTypeMax = 250;
	public static final int mirrorNodesMax = 200;
	public static final int optionMax = 100;
	public static final int optionsMax = 100;

	public static int getMediatypemax() {
		return mediaTypeMax;
	}

	public static int getOptionmax() {
		return optionMax;
	}

	public static int getOptionsmax() {
		return optionsMax;
	}

	/**
	 * tikaで識別されたコンテンツのタイプ
	 */
	private String mediaType;

	/**
	 * キャッシュノード一覧
	 * uploaderUserIdの０番ノードはデフォルトで想定されるので含まない。
	 *
	 * ただしキャッシュノードは最新版を判定する能力を持たない。
	 */
	private List<NodeIdentifierUser> cacheNodes = new ArrayList<>();

	/**
	 * オプション値。被参照オブジェクトがGUI表示する時場合によって活用する。
	 * Nullable
	 */
	private Map<String, String> options;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuReferenceArtifactByVersion other = (TenyuReferenceArtifactByVersion) obj;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (cacheNodes == null) {
			if (other.cacheNodes != null)
				return false;
		} else if (!cacheNodes.equals(other.cacheNodes))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}

	@Override
	public final long getCreateDate() {
		return getTenyuArtifactByVersion().getCreateDate();
	}

	@Override
	public final long getUpdateDate() {
		return getTenyuArtifactByVersion().getUpdateDate();
	}

	@Override
	public final StoreName getStoreName() {
		return StoreNameObjectivity.TENYU_ARTIFACT_BY_VERSION;
	}

	public String getMediaType() {
		return mediaType;
	}

	public List<NodeIdentifierUser> getCacheNodes() {
		return cacheNodes;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	abstract public TenyuArtifactByVersionI getTenyuArtifactByVersion();

	abstract public TenyuArtifactByVersionI getTenyuArtifactByVersion(
			Transaction txn);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result
				+ ((cacheNodes == null) ? 0 : cacheNodes.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		return result;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public void setCacheNodes(List<NodeIdentifierUser> cacheNodes) {
		this.cacheNodes = cacheNodes;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "TenyuReferenceArtifactByVersion [mediaType=" + mediaType
				+ ", cacheNodes=" + cacheNodes + ", options=" + options + "]";
	}

	@Override
	public final boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (NodeIdentifierUser n : cacheNodes) {
				if (!n.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}

		if (!validateAtCreateTenyuReferenceArtifactConcrete(r))
			b = false;

		return b;
	}

	abstract protected boolean validateAtCreateTenyuReferenceArtifactConcrete(
			ValidationResult r);

	@Override
	public final boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public final boolean validateAtRpcSynthetic(TenyuSingleObjectMessageI m,
			byte[] addr, ValidationResult r) {
		boolean b = true;
		if (!validateAtCreateTenyuReferenceArtifactConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtRpcSyntheticTenyuReferenceArtifactConcrete(
			TenyuSingleObjectMessageI m, byte[] addr, ValidationResult r);

	@Override
	public final boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (NodeIdentifierUser n : cacheNodes) {
				if (!n.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		if (!validateAtUpdateTenyuReferenceArtifactConcrete(r))
			b = false;

		return b;
	}

	@Override
	public final boolean validateAtUpdateChange(ValidationResult r,
			Object old) {
		boolean b = true;
		if (!(old instanceof TenyuReferenceArtifactByVersion)) {
			return false;
		}
		TenyuReferenceArtifactByVersion old2 = (TenyuReferenceArtifactByVersion) old;
		if (Glb.getUtil().notEqual(getId(), old2.getId())) {
			r.add(Lang.ID, Lang.ERROR_NOT_EQUAL,
					"this.id=" + getId() + " old.id=" + old2.getId());
			b = false;
		}
		if (!validateAtUpdateChangeTenyuReferenceArtifactConcrete(r, old2))
			b = false;

		return b;
	}

	abstract protected boolean validateAtUpdateChangeTenyuReferenceArtifactConcrete(
			ValidationResult r, Object old);

	abstract protected boolean validateAtUpdateTenyuReferenceArtifactConcrete(
			ValidationResult r);

	private final boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (cacheNodes == null) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION, Lang.MIRROR_NODES,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (cacheNodes.size() > mirrorNodesMax) {
				r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION,
						Lang.MIRROR_NODES, Lang.ERROR_TOO_MANY,
						"size=" + cacheNodes.size());
				b = false;
			}
		}
		if (options != null) {
			if (options.size() > optionsMax) {
				r.add(Lang.OPTIONS, Lang.ERROR_TOO_MANY,
						"size=" + options.size());
				b = false;
			} else {
				for (String optionKey : options.keySet()) {
					if (optionKey.length() > optionMax) {
						r.add(Lang.OPTION_KEY, Lang.ERROR_TOO_LONG);
						b = false;
					} else {
						if (!IndividualityObject.validateTextAllCtrlChar(
								Lang.OPTION_KEY, optionKey, r)) {
							r.add(Lang.OPTION_KEY, Lang.ERROR_INVALID);
							b = false;
						}
					}
				}
				for (String option : options.values()) {
					if (option.length() > optionMax) {
						r.add(Lang.OPTION_VAL, Lang.ERROR_TOO_LONG);
						b = false;
					} else {
						if (!IndividualityObject.validateTextAllCtrlChar(
								Lang.OPTION_VAL, option, r)) {
							r.add(Lang.OPTION_VAL, Lang.ERROR_INVALID);
							b = false;
						}
					}
				}
			}
		}

		if (mediaType == null) {
			r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION, Lang.MEDIA_TYPE,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (mediaType.length() > mediaTypeMax) {
				r.add(Lang.TENYU_REFERENCE_ARTIFACT_BY_VERSION, Lang.MEDIA_TYPE,
						Lang.ERROR_TOO_LONG,
						"mediaType.length()=" + mediaType.length());
				b = false;
			}
		}

		return b;
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		for (NodeIdentifierUser n : cacheNodes) {
			if (!n.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		return b;
	}

	abstract protected boolean validateReferenceTenyuReferenceArtifactConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	public static int getMirrornodesmax() {
		return mirrorNodesMax;
	}
}
