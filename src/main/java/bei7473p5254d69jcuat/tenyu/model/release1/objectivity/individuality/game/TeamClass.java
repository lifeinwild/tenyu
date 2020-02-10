package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * レーティングゲームにおいて全ゲーム共通のレーティング計算や仮想通貨分配量を決定する計算
 * をするためにチーム構造の定義方法を共通化する必要があり、
 * ほぼあらゆるゲームが複数のチームクラスによって参加者を規定しているとみなし、
 * このクラスは1つのチームクラスに対応する。
 * チームクラスの例は、例えばDeceitなら感染者チームクラスと生存者チームクラス、
 * 将棋なら先手チームクラスと後手チームクラスとなるだろう。
 * チームクラスのメンバー数は1の場合がある。
 * クラスと言っているのは対応するインスタンスという概念を考えられるからで、
 * 実際のチームのメンバー等がチームインスタンスである。
 * チームクラスはゲームの構造を抽象的に定義する際に用いられる概念。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TeamClass implements Storable {
	public static final int memberCountMax = 1000 * 100;
	public static final int roleNameMax = 200;
	private int memberCount;
	/**
	 * TeamClass名
	 * 勢力A、勢力B、殺人者役など
	 * 勢力名や役割名と言っても良かったかもしれない
	 */
	private String name;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + memberCount;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		TeamClass other = (TeamClass) obj;
		if (memberCount != other.memberCount)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public String getName() {
		return name;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public void setName(String roleName) {
		this.name = roleName;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (name == null || name.length() == 0) {
			r.add(Lang.RATINGGAME_TEAMCLASS_NAME, Lang.ERROR_EMPTY);
			b = false;
		} else if (name.length() > roleNameMax) {
			r.add(Lang.RATINGGAME_TEAMCLASS_NAME, Lang.ERROR_TOO_LONG,
					name.length() + " / " + roleNameMax);
			b = false;
		} else {
			if (!IndividualityObject.validateText(Lang.RATINGGAME_TEAMCLASS_NAME, name,
					r)) {
				b = false;
			}
			//TODO IndividualityObject.validateName(name, vr,roleNameMax)に変えるべき？どちらでも良さそう
		}
		if (memberCount <= 0) {
			r.add(Lang.RATINGGAME_TEAMCLASS_MEMBER_COUNT, Lang.ERROR_TOO_FEW);
			b = false;
		} else if (memberCount > memberCountMax) {
			r.add(Lang.RATINGGAME_TEAMCLASS_MEMBER_COUNT, Lang.ERROR_TOO_MANY,
					memberCount + " / " + memberCountMax);
			b = false;
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
		return true;
	}
}