package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality;

import java.util.regex.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 正規表現と複数の該当箇所のうちどれを選択するかという情報
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RegexAndSelect implements ValidatableI {
	/**
	 * 入力された文字列に適用される正規表現
	 */
	private String regex;
	/**
	 * この回数だけfindを飛ばす
	 * 0なら1回目のfindの結果が使用される
	 */
	private int skip = 0;
	/**
	 * キャプチャされた部分文字列のどれを使用するか
	 * 通常1
	 */
	private int groupIndex = 1;

	/**
	 * @param src	元となる文字列
	 * @return	この正規表現と設定値で抽出された部分文字列
	 */
	public String extract(String src) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(src);
		int count = 0;
		while (m.find()) {
			if (count < skip)
				continue;
			//キャプチャされた部分文字列
			return m.group(groupIndex);
		}
		//見つからなかった場合
		return null;
	}

	@Override
	public String toString() {
		return regex + " skip=" + skip + " groupIndex=" + groupIndex;
	}

	private final boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (regex == null || regex.length() == 0) {
			vr.add(Lang.REGEX_AND_SELECT_REGEX, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (regex.length() > Glb.getConst().getRegexMax()) {
				vr.add(Lang.REGEX_AND_SELECT_REGEX, Lang.ERROR_TOO_LONG,
						"size=" + regex.length());
				b = false;
			} else if (!Glb.getUtil().validateRegex(regex)) {
				vr.add(Lang.REGEX_AND_SELECT_REGEX, Lang.ERROR_INVALID,
						"regex=" + regex);
				b = false;
			}
		}
		if (skip < 0) {
			vr.add(Lang.REGEX_AND_SELECT_SKIP, Lang.ERROR_TOO_FEW,
					"skip=" + skip);
			b = false;
		}
		if (groupIndex < 0) {
			vr.add(Lang.REGEX_AND_SELECT_GROUPINDEX, Lang.ERROR_TOO_FEW,
					"groupIndex=" + groupIndex);
			b = false;
		}
		return b;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	public void setGroupIndex(int groupIndex) {
		this.groupIndex = groupIndex;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupIndex;
		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
		result = prime * result + skip;
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
		RegexAndSelect other = (RegexAndSelect) obj;
		if (groupIndex != other.groupIndex)
			return false;
		if (regex == null) {
			if (other.regex != null)
				return false;
		} else if (!regex.equals(other.regex))
			return false;
		if (skip != other.skip)
			return false;
		return true;
	}

}