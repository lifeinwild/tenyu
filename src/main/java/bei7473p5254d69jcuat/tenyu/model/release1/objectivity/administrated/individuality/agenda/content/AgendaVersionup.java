package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.content;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 新しいバージョンを承認する。
 * 新しいバージョンのファイル名やハッシュ値が含められていて、
 * 可決されるとそのファイルを保持しているノードから徐々に拡散していく。
 * 可決されていなければどのノードもファイルを受け取らない。
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaVersionup implements AgendaContentI {

	private TenyuPlatformSoftware platformSoftware;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaVersionup other = (AgendaVersionup) obj;
		if (platformSoftware == null) {
			if (other.platformSoftware != null)
				return false;
		} else if (!platformSoftware.equals(other.platformSoftware))
			return false;
		return true;
	}

	public TenyuPlatformSoftware getPlatformSoftware() {
		return platformSoftware;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((platformSoftware == null) ? 0
				: platformSoftware.hashCode());
		return result;
	}

	@Override
	public boolean run(Transaction txn, long nextHistoryIndex, Agenda a) {
		if (a == null || !(a.getContent() instanceof AgendaVersionup)) {
			return false;
		}
		AgendaVersionup av = (AgendaVersionup) a.getContent();
		return Glb.getObje().updateCore(txn, c -> c
				.setLatestAcceptedPlatformSoftware(av.getPlatformSoftware()));
	}

	public void setPlatformSoftware(TenyuPlatformSoftware platformSoftware) {
		this.platformSoftware = platformSoftware;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (platformSoftware == null) {
			b = false;
		} else {
			if (!platformSoftware.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (platformSoftware == null) {
			b = false;
		} else {
			if (!platformSoftware.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public String toString() {
		return "AgendaVersionup [platformSoftware=" + platformSoftware + "]";
	}

}
