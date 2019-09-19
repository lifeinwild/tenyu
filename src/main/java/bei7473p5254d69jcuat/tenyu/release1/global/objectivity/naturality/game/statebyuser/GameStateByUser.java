package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.instance.*;
import jetbrains.exodus.env.*;

/**
 * naturalityパッケージ配下にあるが自然性ではない
 * 登録者＝所有者
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GameStateByUser extends ObjectivityObject
		implements GameStateByUserDBI {
	public static final int equipmentMax = 1000 * 100;

	/**
	 * 材料の種類数の最大
	 */
	public static final int materialMax = 1000 * 100;

	public static final int stateMax = 1000 * 100;

	/**
	 * このユーザーのこのゲームにおける所持装備一覧
	 */
	protected List<GameEquipmentInstance> equipments = new ArrayList<>();

	protected GameReference gameRef;
	/**
	 * このユーザーのこのゲームにおける所持材料一覧
	 */
	protected List<GameMaterialInstance> materials = new ArrayList<>();

	/**
	 * このユーザーがこのゲームに支払った仮想通貨合計額
	 */
	protected long payAmount;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//最初のデータを書き込むときに自動作成される
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		Long admin = gameRef.getGameAdministratorUserId();
		if (admin != null) {
			r.add(admin);
		}
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//一定の手続きが行われた場合に自動的に更新される。
	}

	public GameReference getGameRef() {
		return gameRef;
	}

	/**
	 * この状態を所持するユーザー
	 */
	public Long getOwnerUserId() {
		return registererUserId;//TODO mainAdministratorにすべき？
	}

	public void setGameRef(GameReference gameRef) {
		this.gameRef = gameRef;
	}

	private boolean validateAtCommonObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (payAmount < 0) {
			r.add(Lang.GAMESTATEBYUSER_PAYAMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		if (gameRef == null) {
			r.add(Lang.GAMESTATEBYUSER_GAMEREF, Lang.ERROR_EMPTY);
			b = false;
		}
		if (equipments == null) {
			r.add(Lang.GAMESTATEBYUSER_EQUIPMENTS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (equipments.size() > equipmentMax) {
				r.add(Lang.GAMESTATEBYUSER_EQUIPMENTS, Lang.ERROR_TOO_MANY);
				b = false;
			}
		}
		if (materials == null) {
			r.add(Lang.GAMESTATEBYUSER_MATERIALS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (materials.size() > materialMax) {
				r.add(Lang.GAMESTATEBYUSER_MATERIALS, Lang.ERROR_TOO_MANY);
				b = false;
			}
		}
		return b;
	}

	public abstract boolean validateAtCreateGameStateByUserConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r)) {
			b = false;
		} else {
			for (GameEquipmentInstance e : equipments) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
			for (GameMaterialInstance e : materials) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		if (!validateAtCreateGameStateByUserConcrete(r))
			b = false;
		if (gameRef != null) {
			if (!gameRef.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	abstract protected boolean validateAtUpdateChangeGameStateByUserConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof GameStateByUser)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		GameStateByUser old2 = (GameStateByUser) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getGameRef(), old2.getGameRef())) {
			r.add(Lang.GAMESTATEBYUSER_GAMEREF, Lang.ERROR_UNALTERABLE,
					"gameRef=" + getGameRef() + " oldGameRef="
							+ old2.getGameRef());
			b = false;
		}

		if (Glb.getUtil().notEqual(getOwnerUserId(), old2.getOwnerUserId())) {
			r.add(Lang.GAMESTATEBYUSER_USER_ID, Lang.ERROR_UNALTERABLE,
					"userId=" + getOwnerUserId() + " oldUserId="
							+ old2.getOwnerUserId());
			b = false;
		}

		if (!validateAtUpdateChangeGameStateByUserConcrete(r, old2)) {
			b = false;
		}
		return b;
	}

	public abstract boolean validateAtUpdateGameStateByUserConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r)) {
			b = false;
		} else {
			for (GameEquipmentInstance e : equipments) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
			for (GameMaterialInstance e : materials) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		if (!validateAtUpdateGameStateByUserConcrete(r))
			b = false;
		if (gameRef != null) {
			if (!gameRef.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	public abstract boolean validateReferenceGameStateByUserConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;

		if (!gameRef.validateReference(r, txn)) {
			b = false;
		}

		if (!validateReferenceGameStateByUserConcrete(r, txn))
			b = false;

		for (GameEquipmentInstance e : equipments) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		for (GameMaterialInstance e : materials) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		return b;
	}

	public static enum GameType {
		RATINGGAME(1), STATICGAME(2);
		private final int id;

		private GameType(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

}
