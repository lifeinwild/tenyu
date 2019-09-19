package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * グラフィックのスタイル。例えばリアル、トゥーン、SDなど。
 * 基本的に想定されるのはMMD標準、など。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Style extends Naturality implements StyleDBI {
	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;
	}

	public static List<Long> getAdministratorUserIdDeleteStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(Style.class.getSimpleName()).getAdminUserIds());
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdDeleteStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(registererUserId);
		return r;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Style)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//Style old2 = (Style) old;

		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		return true;
	}
}
