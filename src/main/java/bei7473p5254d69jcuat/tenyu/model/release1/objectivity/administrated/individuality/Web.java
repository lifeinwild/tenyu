package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.web.*;
import glb.*;
import glb.util.*;
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
public class Web extends IndividualityObject implements ChainVersionup, WebI {
	private static final int urlMax = 2083;

	public static boolean createSequence(Transaction txn, Web u,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, null, u.getRegistererUserId(),
				StoreNameObjectivity.WEB);
	}

	public static boolean deleteSequence(Transaction txn, Web u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				StoreNameObjectivity.WEB);
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
	public boolean isMainAdministratorChangable() {
		return true;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		//対応する社会性の管理者が管理者
		List<Long> r = new ArrayList<>();
		Sociality s;
		try {
			s = SocialityStore.getByIndividualityObjectStatic(
					StoreNameObjectivity.WEB, getId());
			if (s != null) {
				r.add(s.getMainAdministratorUserId());
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}

		return r;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		//nullIdの場合もある。URL証明前
		return ModelI.getNullId();
	}

	@Override
	public String getUrl() {
		return getName();
	}

	public void setUrl(String url) {
		setName(url);
	}

	private final boolean validateCommon(ValidationResult r) {
		return true;
	}

	@Override
	protected final boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
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
	protected final boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		boolean b = true;
		if (getName() != null) {
			/* Individualityで検証済み
			if (getName().length() > urlMax) {
				r.add(Lang.WEB_URL, Lang.ERROR_TOO_LONG);
				b = false;
			}
			*/
			if (!Glb.getUtil().isValidURL(getName())) {
				r.add(Lang.WEB_URL, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public int getNameMax() {
		return urlMax;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		return true;
	}

	@Override
	public WebGui getGuiReferenced(String guiName, String cssIdPrefix) {
		return new WebGui(guiName, cssIdPrefix);
	}

	@Override
	public WebStore getStore(Transaction txn) {
		return new WebStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.WEB;
	}

}
