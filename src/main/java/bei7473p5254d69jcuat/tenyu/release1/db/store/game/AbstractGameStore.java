package bei7473p5254d69jcuat.tenyu.release1.db.store.game;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import jetbrains.exodus.env.*;

public abstract class AbstractGameStore<T1 extends AbstractGameDBI,
		T2 extends T1> extends NaturalityStore<T1, T2> {
	protected AbstractGameStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	abstract protected boolean createAbstractGameConcrete(T1 o)
			throws Exception;

	@Override
	protected final boolean createNaturalityConcrete(T1 o) throws Exception {
		boolean b = true;
		if (!createAbstractGameConcrete(o)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean dbValidateAtUpdateAbstractGameConcrete(
			T1 updated, T1 old, ValidationResult r);

	@Override
	protected final boolean dbValidateAtUpdateNaturalityConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;
		if (!dbValidateAtUpdateAbstractGameConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean deleteAbstractGameConcrete(T1 o)
			throws Exception;

	@Override
	protected final boolean deleteNaturalityConcrete(T1 o) throws Exception {
		boolean b = true;
		if (!deleteAbstractGameConcrete(o)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean existAbstractGameConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected final boolean existNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!existAbstractGameConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract public List<StoreInfo> getStoresAbstractGameConcrete();

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		return getStoresAbstractGameConcrete();
	}

	abstract protected boolean noExistAbstractGameConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	protected final boolean noExistNaturalityConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (!noExistAbstractGameConcrete(o, vr)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean updateAbstractGameConcrete(T1 updated, T1 old)
			throws Exception;

	@Override
	protected final boolean updateNaturalityConcrete(T1 updated, T1 old)
			throws Exception {
		boolean b = true;
		if (!updateAbstractGameConcrete(updated, old)) {
			b = false;
		}
		return b;
	}
}
