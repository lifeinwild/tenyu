package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality;

import java.util.*;
import java.util.regex.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.web.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * URL証明のWebページからURL証明コードを探すサイト固有の正規表現
 * DB上のオブジェクトとして設計することでソフトウェアの更新無しで追加、修正、削除できる。
 * 対応すべきWebサイトが多岐に渡る事、その更新頻度が
 * 他サイト依存でこちらで予測できないことから、
 * そのように設計すべきと判断した。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class URLProvementRegex extends IndividualityObject
		implements URLProvementRegexI {
	/**
	 * contentRegexesの最大件数
	 */
	public static final int contentRegexesMax = 1000;
	public static final int fqdnMax = 1000;
	/**
	 * FQDN一覧の最大件数
	 */
	public static final int fqdnsMax = 1000 * 10;

	/**
	 * HTML等Webページの内容に適用する正規表現。
	 * 0番目から順に適用され、キャプチャされた文字列に
	 * 次の正規表現が適用され、最後のキャプチャされた文字列がURL証明コードを
	 * 含んでいなければならない。
	 */
	private List<RegexAndSelect> contentRegexes = new ArrayList<>();

	/**
	 * このクラスのオブジェクトをFQDNで検索できるようになる。
	 * fqdnsのどのfqdnでも検索できる。
	 * 他のオブジェクトとfqdnが重複しても問題無い。
	 */
	private List<String> fqdns = new ArrayList<>();

	/**
	 * contentRegexesがどのURLに適用されるべきかを決定する。
	 * URLに適用される正規表現。
	 */
	private String urlRegex;

	public boolean addContentRegex(RegexAndSelect contentRegex) {
		return contentRegexes.add(contentRegex);
	}

	public boolean addFqdn(String fqdn) {
		return fqdns.add(fqdn);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//議決
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//議決
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//議決
	}

	public List<RegexAndSelect> getContentRegexes() {
		return Collections.unmodifiableList(contentRegexes);
	}

	public List<String> getFqdns() {
		return Collections.unmodifiableList(fqdns);
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectI.getVoteId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectI.getVoteId();
	}

	public String getUrlRegex() {
		return urlRegex;
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	/**
	 * @param url
	 * @return	このオブジェクトが対応するurlか
	 */
	public boolean isSupport(String url) {
		if (url == null || url.length() == 0)
			return false;
		try {
			Matcher m = Pattern.compile(urlRegex).matcher(url);
			return m.matches();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * 正規表現一覧を適用して部分文字列を抽出する
	 * @param src
	 * @return
	 */
	public String parseAdminArea(String src) {
		for (RegexAndSelect regex : contentRegexes) {
			src = regex.extract(src);
			if (src == null)
				return src;
		}
		return src;
	}

	public void setContentRegexes(List<RegexAndSelect> contentRegexes) {
		this.contentRegexes = contentRegexes;
	}

	public void setFqdns(List<String> fqdns) {
		this.fqdns = fqdns;
	}

	public void setUrlRegex(String urlRegex) {
		this.urlRegex = urlRegex;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(URLProvementRegex.class.getSimpleName() + " ");
		sb.append("fqdns=" + fqdns.toString() + " ");
		sb.append("urlRegex=" + urlRegex + " ");
		sb.append("contentRegexes=" + contentRegexes + " ");
		return sb.toString();
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (fqdns == null || fqdns.size() == 0) {
			r.add(Lang.URLPROVEMENT_REGEX_FQDNS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (fqdns.size() > fqdnsMax) {
				r.add(Lang.URLPROVEMENT_REGEX_FQDNS, Lang.ERROR_TOO_MANY,
						"size=" + fqdns.size());
				b = false;
			} else if (fqdns.size() != new HashSet<>(fqdns).size()) {
				r.add(Lang.URLPROVEMENT_REGEX_FQDNS, Lang.ERROR_DUPLICATE,
						"fqdns=" + fqdns);
				b = false;
			} else {
				for (String fqdn : fqdns) {
					if (fqdn == null || fqdn.length() == 0) {
						r.add(Lang.URLPROVEMENT_REGEX_FQDN, Lang.ERROR_EMPTY);
						b = false;
						break;
					} else if (fqdn.length() > fqdnMax) {
						r.add(Lang.URLPROVEMENT_REGEX_FQDN, Lang.ERROR_TOO_LONG,
								"fqdn.length=" + fqdn.length());
						b = false;
					} else if (!IndividualityObject.validateTextAllCtrlChar(
							Lang.URLPROVEMENT_REGEX_FQDN, fqdn, r)) {
						b = false;
						break;
					}
				}
			}
		}

		if (urlRegex == null || urlRegex.length() == 0) {
			r.add(Lang.URLPROVEMENT_REGEX_URLREGEX, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (urlRegex.length() > Glb.getConst().getRegexMax()) {
				r.add(Lang.URLPROVEMENT_REGEX_URLREGEX, Lang.ERROR_TOO_LONG,
						"size=" + urlRegex.length());
				b = false;
			} else if (!Glb.getUtil().validateRegex(urlRegex)) {
				r.add(Lang.URLPROVEMENT_REGEX_URLREGEX, Lang.ERROR_INVALID,
						"urlRegex=" + urlRegex);
				b = false;
			}
		}

		if (contentRegexes == null || contentRegexes.size() == 0) {
			r.add(Lang.URLPROVEMENT_REGEX_CONTENTREGEXES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (contentRegexes.size() > contentRegexesMax) {
				r.add(Lang.URLPROVEMENT_REGEX_CONTENTREGEXES,
						Lang.ERROR_TOO_MANY, "size=" + contentRegexes.size());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (RegexAndSelect e : contentRegexes) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof URLProvementRegex)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//URLProvementRegex old2 = (URLProvementRegex) old;

		boolean b = true;
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (RegexAndSelect e : contentRegexes) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		for (RegexAndSelect e : contentRegexes) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((contentRegexes == null) ? 0 : contentRegexes.hashCode());
		result = prime * result + ((fqdns == null) ? 0 : fqdns.hashCode());
		result = prime * result
				+ ((urlRegex == null) ? 0 : urlRegex.hashCode());
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
		URLProvementRegex other = (URLProvementRegex) obj;
		if (contentRegexes == null) {
			if (other.contentRegexes != null)
				return false;
		} else if (!contentRegexes.equals(other.contentRegexes))
			return false;
		if (fqdns == null) {
			if (other.fqdns != null)
				return false;
		} else if (!fqdns.equals(other.fqdns))
			return false;
		if (urlRegex == null) {
			if (other.urlRegex != null)
				return false;
		} else if (!urlRegex.equals(other.urlRegex))
			return false;
		return true;
	}

	@Override
	public URLProvementRegexGui getGui(String guiName, String cssIdPrefix) {
		return new URLProvementRegexGui(guiName, cssIdPrefix);
	}

	@Override
	public URLProvementRegexStore getStore(Transaction txn) {
		return new URLProvementRegexStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.URL_PROVEMENT_REGEX_STORE;
	}

}
