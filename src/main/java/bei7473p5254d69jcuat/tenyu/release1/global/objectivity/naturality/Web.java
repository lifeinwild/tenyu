package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * URLとそのWEBページの説明
 * 社会性が伴い、相互評価フローネットワーク上のノードになる。
 *
 * 最初の登録者は誰でもなれるが
 * URL証明によってそのWEBページの管理者が制御を獲得できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Web extends Naturality implements ChainVersionup, WebDBI {
	private static final int urlMax = 2083;

	public static boolean createSequence(Transaction txn, Web u,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, new WebStore(txn), null, u.getRegistererUserId(),
				NodeType.WEB);
	}

	public static boolean deleteSequence(Transaction txn, Web u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u, new WebStore(txn),
				NodeType.WEB);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(Web.class.getSimpleName()).getAdminUserIds());
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdUpdate();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		//対応する社会性の管理者が管理者
		List<Long> r = new ArrayList<>();
		Sociality s = SocialityStore.getByNaturalityIdSimple(
				Sociality.createNaturalityId(NodeType.WEB, getRecycleId()));
		if (s != null) {
			r.add(s.getMainAdministratorUserId());
		}

		return r;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		//nullIdの場合もある。URL証明前
		return IdObjectDBI.getNullId();
	}

	@Override
	public String getUrl() {
		return getName();
	}

	@Override
	public void setUrl(String url) {
		setName(url);
	}

	private final boolean validateAtCommonNaturalityConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		return validateAtCommonNaturalityConcrete(r);
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Web)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//Web old2 = (Web) old;
		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		return validateAtCommonNaturalityConcrete(r);
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		boolean b = true;
		if (getName() != null) {
			if (getName().length() > urlMax) {
				r.add(Lang.WEB_URL, Lang.ERROR_TOO_LONG);
				b = false;
			}
			if (!Glb.getUtil().isValidURL(getName())) {
				r.add(Lang.WEB_URL, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		return true;
	}
}
