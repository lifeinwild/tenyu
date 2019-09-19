package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * アバター名
 * 1つのアバターは、リアル、トゥーン等各スタイル毎にファイルグループを持つ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Avatar extends FileConcept implements AvatarDBI {

	public static boolean createSequence(Transaction txn, Avatar u,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, new AvatarStore(txn), u.getRegistererUserId(),
				u.getRegistererUserId(), NodeType.AVATAR);
	}

	public static boolean deleteSequence(Transaction txn, Avatar u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u, new AvatarStore(txn),
				NodeType.AVATAR);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(Avatar.class.getSimpleName()).getAdminUserIds());
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(registererUserId);
		return r;
	}

	@Override
	protected final boolean validateAtCreateFileConceptConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeFileConceptConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Avatar)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//Avatar old2 = (Avatar) old;

		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateFileConceptConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceFileConceptConcrete(ValidationResult r,
			Transaction txn) {
		return true;
	}

}
