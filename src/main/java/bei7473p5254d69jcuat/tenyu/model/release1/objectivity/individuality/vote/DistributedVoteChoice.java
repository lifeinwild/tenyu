package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 分散合意の投票における選択肢
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVoteChoice implements Storable {
	/**
	 * GUIに選択肢として表示される文字列
	 */
	private String name;
	/**
	 * 何らかのシステム的動作に繋げる場合、任意の情報を設定できる
	 * システムが作成した分散合意のみ使用する想定
	 */
	private Long optionLong;
	/**
	 * 任意の情報を置けるように
	 */
	private byte[] option2;

	public static final int maxOption2 = 1000 * 100;

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (name == null || name.length() == 0) {
			vr.add(Lang.CHOICE_NAME, Lang.ERROR_EMPTY);
			b = false;
		}

		if (option2 != null) {
			if (option2.length > maxOption2) {
				vr.add(Lang.CHOICE_OPTION2, Lang.ERROR_TOO_LONG);
				b = false;
			}
		}

		return b;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getOptionLong() {
		return optionLong;
	}

	public void setOptionLong(Long optionLong) {
		this.optionLong = optionLong;
	}

	public byte[] getOption2() {
		return option2;
	}

	public void setOption2(byte[] option2) {
		this.option2 = option2;
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(option2);
		result = prime * result
				+ ((optionLong == null) ? 0 : optionLong.hashCode());
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
		DistributedVoteChoice other = (DistributedVoteChoice) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(option2, other.option2))
			return false;
		if (optionLong == null) {
			if (other.optionLong != null)
				return false;
		} else if (!optionLong.equals(other.optionLong))
			return false;
		return true;
	}

}