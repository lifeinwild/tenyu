package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality;

import java.util.*;
import java.util.regex.*;

import com.ibm.icu.text.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 個性系オブジェクト
 *
 * human readableな名前や説明を持つオブジェクト。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class IndividualityObject extends AdministratedObject
		implements IndividualityObjectI {

	/**
	 * アルファベット、数字、一部記号
	 * .-_/は許可
	 * 空白文字は禁止
	 */
	public static final Pattern alphabeticCharacters = Pattern
			.compile("[^a-zA-Z0-9\\.\\-_\\/]+");

	public static final int explanationMax = 1000;

	public static final String illegalCharacters = "(){}<>;";

	public static final int nameMax = 50;

	public static final int nameMin = 3;
	public static String nameSpecialCharacters = " !”#$%&'()*+,:;<=>?@\\[\\]^`\\{|\\}~\\\\";
	private static final int optionMax = 20;
	/**
	 * スクリプト的な文字列を拒否するため
	 * とはいえ、スクリプトとして解釈される部分は基本的に無い
	 */
	public static final Pattern textIllegalCharacters = Pattern
			.compile("[" + Pattern.quote(illegalCharacters) + "]+");

	public static boolean validateExplanation(String explanation,
			ValidationResult vr) {
		boolean b = true;
		Lang e = Lang.INDIVIDUALITY_OBJECT_EXPLANATION;
		try {
			if (explanation == null || explanation.length() <= 0) {
				vr.add(e, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (explanation.length() > explanationMax) {
					vr.add(e, Lang.ERROR_TOO_LONG,
							explanation.length() + " / " + explanationMax);
					b = false;
				}
				if (!validateText(e, explanation, vr))
					b = false;
			}
		} catch (Exception ex) {
			vr.add(e, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	/**
	 * タグ一覧を検証する
	 * @param tags
	 * @param vr
	 * @return
	 */
	public static boolean validateTag(HashSet<String> tags,
			ValidationResult vr) {
		if (tags != null) {
			for (String tag : tags) {
				if (!validateTag(tag, vr)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * １個のタグを検証する
	 * @param tag
	 * @param vr
	 * @return
	 */
	public static boolean validateTag(String tag, ValidationResult vr) {
		boolean b = true;
		Lang e = Lang.INDIVIDUALITY_OBJECT_TAGS;
		try {
			if (tag == null || tag.length() <= 0) {
				vr.add(e, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (tag.length() > IndividualityObjectI.tagLenMax) {
					vr.add(e, Lang.ERROR_TOO_LONG, tag.length() + " / "
							+ IndividualityObjectI.tagLenMax);
					b = false;
				}
				if (!validateText(e, tag, vr))
					b = false;
			}
		} catch (Exception ex) {
			vr.add(e, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	public static boolean validateName(Lang n, String name,
			ValidationResult vr) {
		return validateName(n, name, vr, nameMax);
	}

	public static boolean validateName(Lang n, String name, ValidationResult vr,
			int nameMax) {
		boolean b = true;
		try {
			if (name == null || name.length() == 0) {
				vr.add(n, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (name.length() < nameMin) {
					vr.add(n, Lang.ERROR_TOO_SHORT, "" + name);
					b = false;
				}
				if (name.length() > nameMax) {
					String head = name.substring(0, nameMax);
					vr.add(n, Lang.ERROR_TOO_LONG,
							name.length() + " / " + nameMax + " " + head);
					b = false;
				}
				//頭だけでなくパスの任意の位置において..は許されない
				//一時期ファイルパスをnameにするモデルだけでこの検証処理をすべきかとも思っていたが
				//ユーザー名をフォルダ名の一部にしたりとかするかもしれないので
				//全nameがこの検証を受けるべきと判断
				if (name.contains("..") || name.startsWith("/")) {
					vr.add(Lang.INDIVIDUALITY_OBJECT_NAME,
							Lang.ERROR_PARENT_PATH);
				}

				/*
				String head1 = name.substring(0, 1);
				if (!StringUtils.isAlpha(head1)) {
					vr.add(n, Lang.ERROR_FIRST_CHARACTER_SHOULD_ALPHABET);
				}
				*/
				if (!validateTextAllCtrlChar(Lang.INDIVIDUALITY_OBJECT_NAME,
						name, vr))
					b = false;

			}
		} catch (Exception e) {
			vr.add(n, Lang.ERROR_INVALID);
		}
		return b;
	}

	public static boolean validateName(String name, ValidationResult vr) {
		return validateName(name, vr, nameMax);
	}

	public static boolean validateName(String name, ValidationResult vr,
			int nameMax) {
		return validateName(Lang.INDIVIDUALITY_OBJECT_NAME, name, vr, nameMax);
	}

	/**
	 * CR,LF,HT等が許可される
	 * @param n
	 * @param text
	 * @param vr
	 * @return
	 */
	public static boolean validateText(Lang n, String text,
			ValidationResult vr) {
		boolean b = true;
		if (!validateTextCommon(n, text, vr))
			b = false;

		//一部制御文字禁止
		if (Glb.getUtil().hasControlCharExceptLF_CR_HT(text)) {
			vr.add(n, Lang.ERROR_CONTROL_CHARACTERS);
			b = false;
		}
		return b;
	}

	/**
	 * トリムされているか、ユニコード最適化されているか
	 * CR,LF,HT等も禁止
	 * @param n
	 * @param text
	 * @param vr
	 * @return
	 */
	public static boolean validateTextAllCtrlChar(Lang n, String text,
			ValidationResult vr) {
		boolean b = true;
		if (!validateTextCommon(n, text, vr))
			b = false;
		//制御文字禁止
		if (Glb.getUtil().hasControlChar(text)) {
			vr.add(n, Lang.ERROR_CONTROL_CHARACTERS,
					Glb.getUtil().option(text, optionMax));
			b = false;
		}
		return b;
	}

	/**
	 * トリムされているか、ユニコード最適化されているか
	 * @param n
	 * @param text
	 * @param vr
	 * @return
	 */
	private static boolean validateTextCommon(Lang n, String text,
			ValidationResult vr) {
		boolean b = true;
		if (text == null || vr == null)
			throw new IllegalArgumentException();
		if (!text.equals(text.trim())) {
			vr.add(n, Lang.ERROR_TRIM);
			b = false;
		}

		//ユニコード最適化
		String normalized = Normalizer2.getNFKCInstance().normalize(text);
		if (!text.equals(normalized)) {
			vr.add(n, Lang.ERROR_NOT_NORMALIZED,
					Glb.getUtil().option(text, optionMax));
			b = false;
		}

		if (IndividualityObject.textIllegalCharacters.matcher(text).find()) {
			vr.add(n, Lang.ERROR_SPECIAL_CHARACTERS, illegalCharacters + " "
					+ Glb.getUtil().option(text, optionMax));
			b = false;
		}
		return b;
	}

	/**
	 * このオブジェクトの説明
	 * このオブジェクトに関するURLを含める事を強く想定
	 */
	protected String explanation;

	/**
	 * このオブジェクトの名前
	 */
	protected String name;

	/**
	 * タグ一覧
	 */
	private HashSet<String> tags;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((explanation == null) ? 0 : explanation.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
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
		IndividualityObject other = (IndividualityObject) obj;
		if (explanation == null) {
			if (other.explanation != null)
				return false;
		} else if (!explanation.equals(other.explanation))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		return true;
	}

	public String getExplanation() {
		return explanation;
	}

	public String getName() {
		return name;
	}

	public int getNameMax() {
		return nameMax;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public void setName(String name) {
		this.name = name;
	}

	private final boolean validateAtCommonAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateName(name, r, getNameMax()))
			b = false;
		if (!validateNameSub(r))
			b = false;
		if (!validateExplanation(explanation, r))
			b = false;
		if (!validateTag(tags, r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAdministratedObjectConcrete(r))
			b = false;
		if (!validateAtCreateIndividualityObjectConcrete(r))
			b = false;
		return b;
	}

	abstract protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof IndividualityObject)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		IndividualityObject old2 = (IndividualityObject) old;

		boolean b = true;

		if (Glb.getUtil().notEqual(getName(), old2.getName())) {
			//現状nameは変更不可とする。理由は、nameは人が記憶する識別子だから。
			//もし許可するなら、ここで許可するだけでシステム的には対応できるはず。
			r.add(Lang.INDIVIDUALITY_OBJECT_NAME, Lang.ERROR_UNALTERABLE);
			b = false;
		}

		if (!validateAtUpdateChangeIndividualityObjectConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	protected abstract boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAdministratedObjectConcrete(r))
			b = false;
		if (!validateAtUpdateIndividualityObjectConcrete(r))
			b = false;
		return b;
	}

	/**
	 * 具象クラスでファイルパスやURL等をnameにするとき、ここに適切な検証処理を追加する。
	 * @return
	 */
	abstract protected boolean validateNameSub(ValidationResult r);

	abstract protected boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	protected final boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (!validateReferenceIndividualityObjectConcrete(r, txn))
			b = false;
		return b;
	}

	@Override
	abstract public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn);

	@Override
	abstract public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGui(
			String guiName, String cssIdPrefix);

	@Override
	public HashSet<String> getTags() {
		return tags;
	}

	/**
	 * @param tag
	 * @return	追加されたか
	 */
	public boolean addTag(String tag) {
		if (tag == null)
			return false;
		if (tags == null)
			tags = new LinkedHashSet<>();//順序保障のためLinked必須
		return tags.add(tag);
	}

	public void setTags(HashSet<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "IndividualityObject [explanation=" + explanation + ", name="
				+ name + ", tags=" + tags + "]";
	}
}
