package bei7473p5254d69jcuat.tenyutalk.reference;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import bei7473p5254d69jcuat.tenyutalk.ui.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link CreativeObject}以降のモデルを参照するクラス群の抽象クラス
 * つまりこれ以下のクラスでTenyutalk系モデルの参照を実現する。
 *
 * 取得されたオブジェクトはアップロード者の電子署名等が検証され、
 * 不整合があれば拒否する。
 * つまりこれら参照系クラスのDLメソッドにおいてTenyutalkのセキュリティが記述される。
 *
 * 特に創作物等のファイルをDLする流れについて説明する。
 * {@link TenyutalkReferenceBase}系（参照）から
 * {@link TenyutalkFile}（ファイルメタデータオブジェクト）を入手して、
 * {@link TenyuFile}（非モデルで部品的に利用されるファイルメタデータオブジェクト）を作って、
 * {@link Downloader}でファイルをDLする。
 *
 * ファイルの中には創作物でありモデルであるものもあるが、
 * ゲームプレイのリプレイファイルなどモデルではないものもある。
 * 前者は{@link TenyutalkFile}だが、
 * 後者は{@link TenyuFile}として何らかのモデルの中の１メンバー変数となる。
 *
 * モデルは様々な意味を持つ。
 * モデルは継承構造の一部で、{@link Model}以下のクラス。
 * モデルはそれに対応するGUI、ストアがある。
 * さらにTenyutalk系モデルは更新履歴やバージョン情報がある。
 *
 * 具体的には{@link MultiplayerObjectI}のリプレイファイルがモデルではなさそうなので
 * モデルとして扱われるファイルと非モデルのファイルを区別した。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TenyutalkReferenceBase<V extends CreativeObjectI>
		implements StorableI, TenyuReference<V> {
	public static final int cssIdPrefixMax = 50;

	public static final int nameLenMax = 200;

	/**
	 * optionの最大長
	 */
	public static final int optionMax = 2000;

	public static int getCssidprefixmax() {
		return cssIdPrefixMax;
	}

	public static int getOptionmax() {
		return optionMax;
	}

	/**
	 * この参照を使用してコンテンツを取得した人はそれをキャッシュすべきか。
	 * 他にもキャッシュの動作を制御する方法はあるが、
	 * これは参照を保持するクラスによる制御。
	 */
	private boolean cache = false;

	/**
	 * この参照をGUI表示した時の各GUI部品のcssIdの接頭辞
	 * null可
	 */
	private String cssIdPrefix;

	/**
	 * 言語
	 */
	private Locale locale = null;

	/**
	 * この参照の名前
	 */
	private String name;

	/**
	 * 問い合わせ先ノード一覧
	 * uploaderUserIdの０番ノードは含まない。
	 * デフォルトで想定される。
	 */
	private List<NodeIdentifierUser> nodes = new ArrayList<>();
	/**
	 * オプション値。被参照オブジェクトがGUI表示する時場合によって活用する。
	 */
	private String option;

	/**
	 * 参照先オブジェクトを格納するストアの名前
	 */
	private StoreName storeName;

	/**
	 * 参照先オブジェクトをアップロードしたユーザーのID
	 */
	private Long uploaderUserId;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyutalkReferenceBase other = (TenyutalkReferenceBase) obj;
		if (cache != other.cache)
			return false;
		if (cssIdPrefix == null) {
			if (other.cssIdPrefix != null)
				return false;
		} else if (!cssIdPrefix.equals(other.cssIdPrefix))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
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

	public String getCssId() {
		return getCssIdPrefix() + "_" + uploaderUserId + "_" + storeName + "_"
				+ name;
	}

	public String getCssIdPrefix() {
		return cssIdPrefix;
	}

	/**
	 * @return	アップロード者のノード識別子
	 */
	public NodeIdentifierUser getDefaultUploaderNode() {
		return new NodeIdentifierUser(uploaderUserId, 0);
	}

	/**
	 * @return この参照オブジェクトを表示するGUI
	 */
	abstract public TenyutalkReferenceBaseGui<V> getGui();

	@Override
	abstract public TenyutalkReferenceBaseGui<V> getGui(String guiName,
			String cssIdPrefix);

	public Locale getLocale() {
		return locale;
	}

	public String getName() {
		return name;
	}

	public List<NodeIdentifierUser> getNodes() {
		return nodes;
	}

	@Override
	public String getNotificationMessage() {
		return name;
	}

	public String getOption() {
		return option;
	}

	public StoreName getStoreName() {
		return storeName;
	}

	public Long getUploaderUserId() {
		return uploaderUserId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cache ? 1231 : 1237);
		result = prime * result
				+ ((cssIdPrefix == null) ? 0 : cssIdPrefix.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((option == null) ? 0 : option.hashCode());
		result = prime * result
				+ ((storeName == null) ? 0 : storeName.hashCode());
		result = prime * result
				+ ((uploaderUserId == null) ? 0 : uploaderUserId.hashCode());
		return result;
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

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setName(String name) {
		this.name = name;
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
	public void setStoreName(StoreNameObjectivity storeName) {
		this.storeName = storeName;
	}

	public void setUploaderUserId(Long uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
	}

	@Override
	public String toString() {
		return "TenyutalkReferenceBase [cache=" + cache + ", cssIdPrefix="
				+ cssIdPrefix + ", locale=" + locale + ", name=" + name
				+ ", nodes=" + nodes + ", option=" + option + ", storeName="
				+ storeName + ", uploaderUserId=" + uploaderUserId + "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtCreateTenyutalkReferenceBaseConcrete(
			ValidationResult r);

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
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
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof TenyutalkReferenceBase<?>)) {
			return false;
		}
		TenyutalkReferenceBase<?> old2 = (TenyutalkReferenceBase<?>) old;
		if (!uploaderUserId.equals(old2.getUploaderUserId())) {
			b = false;
		}

		if (!validateAtUpdateChangeTenyutalkReferenceBaseConcrete(r, old2))
			b = false;

		return b;
	}

	abstract protected boolean validateAtUpdateChangeTenyutalkReferenceBaseConcrete(
			ValidationResult r, Object old);

	abstract protected boolean validateAtUpdateTenyutalkReferenceBaseConcrete(
			ValidationResult r);

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

		if (name == null) {
			r.add(Lang.TENYUTALK_REFERENCE_BASE, Lang.NAME, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (name.length() > nameLenMax) {
				r.add(Lang.TENYUTALK_REFERENCE_BASE, Lang.NAME,
						Lang.ERROR_TOO_LONG, "length=" + name.length());
				b = false;
			}
		}

		if (locale == null) {
			r.add(Lang.TENYUTALK_REFERENCE_BASE, Lang.LOCALE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (locale.getLanguage() == null) {
				r.add(Lang.TENYUTALK_REFERENCE_BASE, Lang.LOCALE,
						Lang.ERROR_EMPTY, "locale=" + locale);
				b = false;
			}
		}

		return b;

	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		return b;
	}

	abstract protected boolean validateReferenceTenyutalkReferenceBaseConcrete(
			ValidationResult r, Transaction txn) throws Exception;

}
