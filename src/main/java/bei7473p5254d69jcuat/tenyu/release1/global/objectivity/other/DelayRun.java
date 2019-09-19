package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.env.*;

/**
 * 遅延実行
 * 処理をストアに登録し、
 * 予約されたヒストリーインデックスを過ぎたら順次実行される。
 * 1ヒストリーインデックスあたりに実行される処理数は上限があるので、
 * 予約された全ての処理が処理されると限らない。
 * 残ったものは次回以降へ持ち越される。
 *
 * 具象クラスが様々作られ、具象クラス毎にストアが用意される。
 * 具象クラス毎に処理優先度を与える事ができる。
 * 処理優先度は単に遅延実行される時にどの具象クラスから順に処理するかで決まる。
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class DelayRun extends ObjectivityObject
		implements DelayRunDBI {

	/**
	 * 登録されたヒストリーインデックス
	 */
	private long registerHistoryIndex;

	/**
	 * これを過ぎたら実行される可能性がある
	 * 必ずすぐに実行されると限らない
	 */
	private long runHistoryIndex;

	public long getRegisterHistoryIndex() {
		return registerHistoryIndex;
	}

	public long getRunHistoryIndex() {
		return runHistoryIndex;
	}

	public void setRegisterHistoryIndex(long registerHistoryIndex) {
		this.registerHistoryIndex = registerHistoryIndex;
	}

	public void setRunHistoryIndex(long runHistoryIndex) {
		this.runHistoryIndex = runHistoryIndex;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (runHistoryIndex < 0) {
			r.add(Lang.DELAYRUN_RUNHISTORYINDEX, Lang.ERROR_INVALID);
			b = false;
		}
		if (registerHistoryIndex < 0) {
			r.add(Lang.DELAYRUN_REGISTERHISTORYINDEX, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtCreateDelayRunConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		if (!validateAtCreateDelayRunConcrete(r))
			b = false;
		return b;
	}

	abstract protected boolean validateAtUpdateChangeDelayRunConcrete(
			ValidationResult r, Object old);

	@Override
	protected final boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof DelayRun)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		DelayRun old2 = (DelayRun) old;

		boolean b = true;
		//現状変更は想定されない
		if (Glb.getUtil().notEqual(getRunHistoryIndex(),
				old2.getRunHistoryIndex())) {
			r.add(Lang.DELAYRUN_RUNHISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"runHistoryIndex=" + getRunHistoryIndex()
							+ " oldRunHistoryIndex="
							+ old2.getRunHistoryIndex());
			b = false;
		}
		if (Glb.getUtil().notEqual(getRegisterHistoryIndex(),
				old2.getRegisterHistoryIndex())) {
			r.add(Lang.DELAYRUN_REGISTERHISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"registerHistoryIndex=" + getRegisterHistoryIndex()
							+ " oldRegisterHistoryIndex="
							+ old2.getRegisterHistoryIndex());
			b = false;
		}
		if (!validateAtUpdateChangeDelayRunConcrete(r, old2)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtUpdateDelayRunConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		if (!validateAtUpdateDelayRunConcrete(r))
			b = false;
		return b;
	}

	abstract public boolean validateReferenceDelayRunConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {

		return validateReferenceDelayRunConcrete(r, txn);
	}

}
