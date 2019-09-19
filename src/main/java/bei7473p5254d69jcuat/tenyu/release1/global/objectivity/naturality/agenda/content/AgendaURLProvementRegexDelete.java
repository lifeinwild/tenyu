package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import jetbrains.exodus.env.*;

public class AgendaURLProvementRegexDelete implements AgendaContentI {
	/**
	 * 削除されるオブジェクトのID
	 */
	private Long id;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaURLProvementRegexDelete other = (AgendaURLProvementRegexDelete) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean run(Agenda a) {
		boolean delete = Glb.getObje().compute(txn -> {
			try {
				URLProvementRegexStore s = new URLProvementRegexStore(txn);
				if (!s.delete(id))
					return false;
				return txn.commit();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				txn.abort();
				return false;
			}
		});
		if (delete) {
			Glb.getLogger().info("deleted. id=" + id);
		}
		return delete;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (id == null) {
			r.add(Lang.AGENDA_URLPROVEMENT_REGEX_DELETE_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(id)) {
				r.add(Lang.AGENDA_URLPROVEMENT_REGEX_DELETE_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		URLProvementRegexStore s = new URLProvementRegexStore(txn);
		URLProvementRegex exist = s.get(id);
		if (exist == null) {
			r.add(Lang.AGENDA_URLPROVEMENT_REGEX_DELETE_ID, Lang.ERROR_EMPTY,
					"id=" + id);
			b = false;
		}
		return b;
	}

}
