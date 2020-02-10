package bei7473p5254d69jcuat.tenyutalk.reference;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import bei7473p5254d69jcuat.tenyutalk.ui.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link CreativeObject}以降のモデルを参照するクラス群の抽象クラス
 *
 * 取得されたオブジェクトとアップロード者の電子署名を検証し、
 * 不整合があれば拒否する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TenyutalkReferenceBase<V extends CreativeObject>
		implements Storable, TenyuReference<V> {
	/**
	 * 参照先オブジェクトを格納するストアの名前
	 */
	private StoreName storeName;

	/**
	 * 参照先オブジェクトをアップロードしたユーザーのID
	 */
	private Long uploaderUserId;

	/**
	 * 問い合わせ先ノード一覧
	 * uploaderUserIdの０番ノードは含まない。
	 * デフォルトで想定される。
	 */
	private List<NodeIdentifierUser> nodes = new ArrayList<>();

	/**
	 * この参照を使用してコンテンツを取得した人はそれをキャッシュすべきか。
	 * 他にもキャッシュの動作を制御する方法はあるが、
	 * これは参照を保持するクラスによる制御。
	 */
	private boolean cache = false;

	/**
	 * オプション値。被参照オブジェクトがGUI表示する時場合によって活用する。
	 */
	private String option;

	/**
	 * optionの最大長
	 */
	public static final int optionMax = 2000;

	/**
	 * この参照をGUI表示した時の各GUI部品のcssIdの接頭辞
	 * null可
	 */
	private String cssIdPrefix;

	public static final int cssIdPrefixMax = 50;

	/**
	 * この参照の名前
	 */
	private String name;

	public String getCssId() {
		return getCssIdPrefix() + "_" + uploaderUserId + "_" + storeName + "_"
				+ name;
	}

	@Override
	abstract public TenyutalkReferenceBaseGui<V> getGui(String guiName,
			String cssIdPrefix);

	abstract public TenyutalkReferenceBaseGui<V> getGui();

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof TenyutalkReferenceBase<?>)) {
			return false;
		}
		TenyutalkReferenceBase<?> old2 = (TenyutalkReferenceBase<?>) old;
		if (!uploaderUserId.equals(old2.getUploaderUserId())) {
			b = false;
		}
		return b;
	}

	public Long getUploaderUserId() {
		return uploaderUserId;
	}

	/**
	 * @return	アップロード者のノード識別子
	 */
	public NodeIdentifierUser getDefaultUploaderNode() {
		return new NodeIdentifierUser(uploaderUserId, 0);
	}

	public String getCssIdPrefix() {
		return cssIdPrefix;
	}

	public List<NodeIdentifierUser> getNodes() {
		return nodes;
	}

	public String getOption() {
		return option;
	}

	public StoreName getStoreName() {
		return storeName;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public void setCssIdPrefix(String cssIdPrefix) {
		this.cssIdPrefix = cssIdPrefix;
	}

	public void setNodes(List<NodeIdentifierUser> nodes) {
		this.nodes = nodes;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public void setStoreName(StoreName storeName) {
		this.storeName = storeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cache ? 1231 : 1237);
		result = prime * result
				+ ((cssIdPrefix == null) ? 0 : cssIdPrefix.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((option == null) ? 0 : option.hashCode());
		result = prime * result
				+ ((storeName == null) ? 0 : storeName.hashCode());
		result = prime * result
				+ ((uploaderUserId == null) ? 0 : uploaderUserId.hashCode());
		return result;
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
		TenyutalkReferenceBase other = (TenyutalkReferenceBase) obj;
		if (cache != other.cache)
			return false;
		if (cssIdPrefix == null) {
			if (other.cssIdPrefix != null)
				return false;
		} else if (!cssIdPrefix.equals(other.cssIdPrefix))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		if (option == null) {
			if (other.option != null)
				return false;
		} else if (!option.equals(other.option))
			return false;
		if (storeName == null) {
			if (other.storeName != null)
				return false;
		} else if (!storeName.equals(other.storeName))
			return false;
		if (uploaderUserId == null) {
			if (other.uploaderUserId != null)
				return false;
		} else if (!uploaderUserId.equals(other.uploaderUserId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TenyutalkReferenceBase [storeName=" + storeName
				+ ", uploaderUserId=" + uploaderUserId + ", nodes=" + nodes
				+ ", cache=" + cache + ", option=" + option + ", cssIdPrefix="
				+ cssIdPrefix + "]";
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (storeName == null) {
			r.add(Lang.TENYUTALK_REFERENCE_STORENAMES, Lang.ERROR_EMPTY);
			b = false;
		}
		if (nodes == null) {
			r.add(Lang.STORENAMES_OBJECTIVITY, Lang.ERROR_EMPTY);
			b = false;
		}
		if (option != null) {
			if (option.length() > optionMax) {
				r.add(Lang.TENYUTALK_REFERENCE_OPTION, Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				if (!IndividualityObject.validateTextAllCtrlChar(
						Lang.TENYUTALK_REFERENCE_OPTION, option, r)) {
					r.add(Lang.TENYUTALK_REFERENCE_OPTION, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}

		if (cssIdPrefix != null) {
			if (cssIdPrefix.length() > cssIdPrefixMax) {
				r.add(Lang.TENYUTALK_REFERENCE_CSSIDPREFIX,
						Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				if (!IndividualityObject.validateTextAllCtrlChar(
						Lang.TENYUTALK_REFERENCE_CSSIDPREFIX, cssIdPrefix, r)) {
					r.add(Lang.TENYUTALK_REFERENCE_CSSIDPREFIX,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
		}

		return b;

	}

	abstract protected boolean validateAtCreateTenyutalkReferenceBaseConcrete(
			ValidationResult r);

	abstract protected boolean validateAtUpdateTenyutalkReferenceBaseConcrete(
			ValidationResult r);

	abstract protected boolean validateReferenceTenyutalkReferenceBaseConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		return b;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getOptionmax() {
		return optionMax;
	}

	public static int getCssidprefixmax() {
		return cssIdPrefixMax;
	}

	public void setUploaderUserId(Long uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
	}

}
