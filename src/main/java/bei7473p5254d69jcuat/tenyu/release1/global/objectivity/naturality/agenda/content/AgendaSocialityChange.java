package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 社会性の管理者を議決によって変更する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaSocialityChange implements AgendaContentI {
	/**
	 * 新しい管理者のユーザーID
	 */
	private Long newAdminUserId;
	/**
	 * 変更される社会性のID
	 */
	private Long socialityId;

	/**
	 * 登録者も変更するか
	 */
	private boolean registerer = false;

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (newAdminUserId == null) {
			r.add(Lang.AGENDA_SOCIALITYCHANGE_NEWADMINUSERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(newAdminUserId)) {
				r.add(Lang.AGENDA_SOCIALITYCHANGE_NEWADMINUSERID,
						Lang.ERROR_INVALID, "newAdminUserId=" + newAdminUserId);
				b = false;
			}
		}
		if (socialityId == null) {
			r.add(Lang.AGENDA_SOCIALITYCHANGE_SOCIALITYID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(socialityId)) {
				r.add(Lang.AGENDA_SOCIALITYCHANGE_SOCIALITYID,
						Lang.ERROR_INVALID, "socialityId=" + socialityId);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(newAdminUserId) == null) {
			r.add(Lang.AGENDA_SOCIALITYCHANGE_NEWADMINUSERID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"newAdminUserId=" + newAdminUserId);
			b = false;
		}
		SocialityStore sos = new SocialityStore(txn);
		if (sos.get(socialityId) == null) {
			r.add(Lang.AGENDA_SOCIALITYCHANGE_SOCIALITYID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"socialityId=" + socialityId);
			b = false;
		}
		return b;
	}

	@Override
	public boolean run(Agenda a) throws Exception {
		return Glb.getObje().compute(txn -> {
			try {
				SocialityStore sos = new SocialityStore(txn);
				Sociality so = sos.get(socialityId);
				so.setMainAdministratorUserId(newAdminUserId);
				if (registerer) {
					so.setRegistererUserId(newAdminUserId);
				}
				if (!sos.update(so))
					return false;
				return txn.commit();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	public Long getNewAdminUserId() {
		return newAdminUserId;
	}

	public void setNewAdminUserId(Long newAdminUserId) {
		this.newAdminUserId = newAdminUserId;
	}

	public Long getSocialityId() {
		return socialityId;
	}

	public void setSocialityId(Long socialityId) {
		this.socialityId = socialityId;
	}

	public boolean isRegisterer() {
		return registerer;
	}

	public void setRegisterer(boolean registerer) {
		this.registerer = registerer;
	}

}
