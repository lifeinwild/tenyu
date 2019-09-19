package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.env.*;

/**
 * 議題は締め切り日時が来た時実行されなければならない。
 * ステータスの変更や可決時の処理呼び出し等をする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaProc extends DelayRun implements AgendaProcDBI {
	private Long agendaId;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//システムが作成
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//削除は想定されない
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//更新は想定されない
	}

	public Long getAgendaId() {
		return agendaId;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectDBI.getVoteId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectDBI.getVoteId();
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	@Override
	public boolean run(Transaction txn) throws Exception {
		//受付締め切り日時が来た時の処理
		Agenda a = Glb.getObje().getAgenda(ags -> ags.get(agendaId));
		if (a == null)
			return false;
		if (a.isOverThreshold()) {
			Glb.getLogger().info("AgendaProc accepted agenda=" + a);
			a.setStatus(AgendaStatus.ACCEPTED);
			a.getContent().run(a);
			return true;
		} else {
			Glb.getLogger().info("AgendaProc denied agenda=" + a);
			a.setStatus(AgendaStatus.DENIED);
			return false;
		}
	}

	public void setAgendaId(Long agendaId) {
		this.agendaId = agendaId;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (agendaId == null) {
			r.add(Lang.AGENDA_PROC_AGENDA_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(agendaId)) {
				r.add(Lang.AGENDA_PROC_AGENDA_ID, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;

	}

	@Override
	protected boolean validateAtCreateDelayRunConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeDelayRunConcrete(ValidationResult r,
			Object old) {
		if (!(old instanceof AgendaProc)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		AgendaProc old2 = (AgendaProc) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getAgendaId(), old2.getAgendaId())) {
			r.add(Lang.AGENDA_PROC_AGENDA_ID, Lang.ERROR_UNALTERABLE,
					"agendaId=" + getAgendaId());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateDelayRunConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateReferenceDelayRunConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		Agenda a = new AgendaStore(txn).get(agendaId);
		if (a == null) {
			r.add(Lang.AGENDA_PROC_AGENDA_ID, Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

}
