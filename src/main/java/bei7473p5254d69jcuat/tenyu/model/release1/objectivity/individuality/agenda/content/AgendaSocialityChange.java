package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.content;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 社会性の管理者を議決によって変更する。
 *
 * 任意のモデルの管理者を変更できるようにしてもいいかもしれないが、
 * セキュリティや運用ミスによるシステムの誤作動のリスクが上がる。
 * 社会性の管理者の変更は、騙り等によって他人の成果で相互評価フローネットワーク上で
 * 不当な評価を得た人が居た場合、そのノードの管理権限を正しい人に移すため。
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
			if (!Model.validateIdStandardNotSpecialId(newAdminUserId)) {
				r.add(Lang.AGENDA_SOCIALITYCHANGE_NEWADMINUSERID,
						Lang.ERROR_INVALID, "newAdminUserId=" + newAdminUserId);
				b = false;
			}
		}
		if (socialityId == null) {
			r.add(Lang.AGENDA_SOCIALITYCHANGE_SOCIALITYID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(socialityId)) {
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
	public boolean run(Transaction txn, long nextHistoryIndex, Agenda a)
			throws Exception {
		SocialityStore sos = new SocialityStore(txn);
		Sociality so = sos.get(socialityId);
		so.setMainAdministratorUserId(newAdminUserId);
		if (registerer) {
			so.setRegistererUserId(newAdminUserId);
		}
		if (!sos.update(so))
			return false;
		return true;
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
