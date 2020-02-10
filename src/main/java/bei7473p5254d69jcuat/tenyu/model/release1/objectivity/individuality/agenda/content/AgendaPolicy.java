package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.content;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.*;
import glb.*;
import glb.util.*;
import javafx.scene.control.Alert.*;
import jetbrains.exodus.env.*;

/**
 * 方針策定
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaPolicy implements AgendaContentI {
	public static final int policyMax = 1000 * 100;
	private String content;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaPolicy other = (AgendaPolicy) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	public String getContent() {
		return content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean run(Transaction txn, long nextHistoryIndex, Agenda a) {
		//全体運営者の間で認知されるだけで、システムの変化はない。
		try {
			if (Glb.getObje().getCore().getManagerList()
					.isManager(Glb.getMiddle().getMyUserId())) {
				Glb.getGui().alert(AlertType.INFORMATION,
						Lang.AGENDA_POLICY_ADDED.toString(), content);
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return true;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (content == null || content.length() == 0) {
			r.add(Lang.AGENDA_POLICY_CONTENT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (content.length() > policyMax) {
				r.add(Lang.AGENDA_POLICY_CONTENT, Lang.ERROR_TOO_LONG);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn) {
		return true;
	}

}
