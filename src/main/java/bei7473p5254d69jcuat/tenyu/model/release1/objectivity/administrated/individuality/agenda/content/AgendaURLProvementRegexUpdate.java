package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.content;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class AgendaURLProvementRegexUpdate implements AgendaContentI {
	/**
	 * 新しい値
	 */
	private URLProvementRegex updated;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaURLProvementRegexUpdate other = (AgendaURLProvementRegexUpdate) obj;
		if (updated == null) {
			if (other.updated != null)
				return false;
		} else if (!updated.equals(other.updated))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		return result;
	}

	@Override
	public boolean run(Transaction txn, long nextHistoryIndex, Agenda a)
			throws Exception {
		URLProvementRegexStore s = new URLProvementRegexStore(txn);
		if (!s.update(updated))
			return false;
		Glb.getLogger().info("updated." + updated.toString());
		return true;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (updated == null) {
			r.add(Lang.AGENDA_URLPROVEMENT_REGEX_UPDATE_URLPROVEMENT_REGEX,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!updated.validateAtUpdate(r)) {
				b = false;
			}
		}

		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;

		if (updated != null) {
			if (!updated.validateReference(r, txn)) {
				b = false;
			}
		}

		return b;
	}

}
