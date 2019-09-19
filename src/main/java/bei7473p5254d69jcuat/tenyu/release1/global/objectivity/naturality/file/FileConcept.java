package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * ファイルは何らかのコンセプトのもとに作られる。
 * コンセプトは例えばアバター名など
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class FileConcept extends Naturality implements FileConceptDBI {

	protected abstract boolean validateAtCreateFileConceptConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		return validateAtCreateFileConceptConcrete(r);
	}

	abstract protected boolean validateAtUpdateChangeFileConceptConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof FileConcept)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//FileConcept old2 = (FileConcept) old;

		boolean b = true;
		if (!validateAtUpdateChangeFileConceptConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	protected abstract boolean validateAtUpdateFileConceptConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		return validateAtCreateFileConceptConcrete(r);
	}

	abstract public boolean validateReferenceFileConceptConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		return validateReferenceFileConceptConcrete(r, txn);
	}

}
