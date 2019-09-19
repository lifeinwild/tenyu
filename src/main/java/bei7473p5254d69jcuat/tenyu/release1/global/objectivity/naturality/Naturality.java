package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import java.util.regex.*;

import com.ibm.icu.text.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.env.*;

/**
 * 自然性
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class Naturality extends ObjectivityObject
		implements NaturalityDBI {

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
		Lang e = Lang.NATURALITY_EXPLANATION;
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
					vr.add(Lang.NATURALITY_NAME, Lang.ERROR_PARENT_PATH);
				}

				/*
				String head1 = name.substring(0, 1);
				if (!StringUtils.isAlpha(head1)) {
					vr.add(n, Lang.ERROR_FIRST_CHARACTER_SHOULD_ALPHABET);
				}
				*/
				if (!validateTextAllCtrlChar(Lang.NATURALITY_NAME, name, vr))
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
		return validateName(Lang.NATURALITY_NAME, name, vr, nameMax);
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

		if (Naturality.textIllegalCharacters.matcher(text).find()) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((explanation == null) ? 0 : explanation.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Naturality other = (Naturality) obj;
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

	private final boolean validateAtCommonObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateName(name, r, getNameMax()))
			b = false;
		if (!validateNameSub(r))
			b = false;
		if (!validateExplanation(explanation, r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateNaturalityConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		if (!validateAtCreateNaturalityConcrete(r))
			b = false;
		return b;
	}

	abstract protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Naturality)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Naturality old2 = (Naturality) old;

		boolean b = true;

		if (Glb.getUtil().notEqual(getName(), old2.getName())) {
			//現状nameは変更不可とする。理由は、nameは人が記憶する識別子だから。
			//もし許可するなら、ここで許可するだけでシステム的には対応できるはず。
			r.add(Lang.NATURALITY_NAME, Lang.ERROR_UNALTERABLE);
			b = false;
		}

		if (!validateAtUpdateChangeNaturalityConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	protected abstract boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		if (!validateAtUpdateNaturalityConcrete(r))
			b = false;
		return b;
	}

	/**
	 * 具象クラスでファイルパスやURL等をnameにするとき、ここに適切な検証処理を追加する。
	 * @return
	 */
	abstract public boolean validateNameSub(ValidationResult r);

	abstract public boolean validateReferenceNaturalityConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (!validateReferenceNaturalityConcrete(r, txn))
			b = false;
		return b;
	}
}
