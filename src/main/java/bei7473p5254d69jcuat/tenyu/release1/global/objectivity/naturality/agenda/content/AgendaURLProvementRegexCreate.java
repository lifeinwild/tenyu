package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import jetbrains.exodus.env.*;

public class AgendaURLProvementRegexCreate implements AgendaContentI {
	private URLProvementRegex regex;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaURLProvementRegexCreate other = (AgendaURLProvementRegexCreate) obj;
		if (regex == null) {
			if (other.regex != null)
				return false;
		} else if (!regex.equals(other.regex))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
		return result;
	}

	@Override
	public boolean run(Agenda a) {
		boolean create = Glb.getObje().compute(txn -> {
			try {
				URLProvementRegexStore s = new URLProvementRegexStore(txn);
				Long created = s.create(regex);
				if (created == null) {
					return false;
				}
				return txn.commit();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				txn.abort();
				return false;
			}
		});
		if (create) {
			Glb.getLogger().info("created." + regex.toString());
		}
		return create;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (regex == null) {
			r.add(Lang.AGENDA_URLPROVEMENT_REGEX_CREATE_URLPROVEMENT_REGEX,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!regex.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (regex != null) {
			if (!regex.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}

}
