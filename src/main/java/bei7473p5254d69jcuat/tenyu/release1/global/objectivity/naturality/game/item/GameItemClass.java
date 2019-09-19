package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import jetbrains.exodus.env.*;

/**
 * ゲームのアイテム
 *
 * このクラスは基盤ソフトウェアで扱うアイテムを表現する。
 * 各ゲームは基盤ソフトウェアを通さずに管理するアイテムを作る事もできるが、
 * そのようなものはこのクラスのオブジェクトにならない。
 * 基盤ソフトウェアでアイテムを管理する事は、基盤ソフトウェアの仮想経済構想に連結する
 * 事を意味する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GameItemClass extends Naturality
		implements GameItemClassDBI {
	protected GameReference gameRef;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		Long admin = gameRef.getGameAdministratorUserId();
		if (admin != null) {
			r.add(admin);
		}
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdCreate();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministratorUserIdCreate();
	}

	public GameReference getGameRef() {
		return gameRef;
	}

	public void setGameRef(GameReference gameRef) {
		this.gameRef = gameRef;
	}

	private boolean validateAtCommonNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (gameRef == null) {
			r.add(Lang.GAMEITEMCLASS_GAMEREF, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtCreateGameItemConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtCreateNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		if (!validateAtCreateGameItemConcrete(r))
			b = false;
		if (gameRef != null) {
			if (!gameRef.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	abstract protected boolean validateAtUpdateChangeGameItemClassConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof GameItemClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		GameItemClass old2 = (GameItemClass) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(getGameRef(), old2.getGameRef())) {
			r.add(Lang.GAMEITEMCLASS_GAMEREF, Lang.ERROR_UNALTERABLE, "gameRef="
					+ getGameRef() + " oldGameRef=" + old2.getGameRef());
			b = false;
		}
		if (!validateAtUpdateChangeGameItemClassConcrete(r, old2)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtUpdateGameItemConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		if (!validateAtUpdateGameItemConcrete(r))
			b = false;
		if (gameRef != null) {
			if (!gameRef.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (gameRef != null) {
			if (!gameRef.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}

}
