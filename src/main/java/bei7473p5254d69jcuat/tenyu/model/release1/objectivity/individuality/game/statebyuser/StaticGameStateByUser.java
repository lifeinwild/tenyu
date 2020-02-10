package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.statebyuser;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.instance.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class StaticGameStateByUser extends AdministratedObject
		implements StaticGameStateByUserDBI {

	/**
	 * このユーザーのこのゲームにおける所持材料一覧
	 */
	protected List<StaticGameMaterialInstance> materials = new ArrayList<>();

	/**
	 * 材料の種類数の最大
	 */
	public static final int materialMax = 1000 * 100;

	/**
	 * このゲームのユーザー別状態
	 */
	private Long staticGameId;

	public static final int equipmentMax = 1000 * 100;

	public static final int stateMax = 1000 * 100;

	/**
	 * このユーザーのこのゲームにおける所持装備一覧
	 */
	protected List<StaticGameEquipmentInstance> equipments = new ArrayList<>();

	/**
	 * このユーザーがこのゲームに支払った合計額
	 */
	protected long payAmount;

	public StaticGameStateByUser() {
	}

	public StaticGameStateByUser(Long staticGameId, Long ownerUserId) {
		this.staticGameId = staticGameId;
		setMainAdministratorUserId(ownerUserId);
		setRegistererUserId(ownerUserId);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (staticGameId == null) {
			r.add(Lang.STATICGAME_STATEBYUSER_STATICGAME_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(staticGameId)) {
				r.add(Lang.STATICGAME_STATEBYUSER_STATICGAME_ID,
						Lang.ERROR_INVALID, "staticGameId=" + staticGameId);
				b = false;
			}
		}
		if (materials == null) {
			r.add(Lang.STATICGAME_STATEBYUSER_MATERIALS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (materials.size() > materialMax) {
				r.add(Lang.STATICGAME_STATEBYUSER_MATERIALS,
						Lang.ERROR_TOO_MANY);
				b = false;
			}
		}

		if (payAmount < 0) {
			r.add(Lang.STATICGAME_STATEBYUSER_PAYAMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		if (equipments == null) {
			r.add(Lang.STATICGAME_STATEBYUSER_EQUIPMENTS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (equipments.size() > equipmentMax) {
				r.add(Lang.STATICGAME_STATEBYUSER_EQUIPMENTS,
						Lang.ERROR_TOO_MANY);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		if (materials != null) {
			for (StaticGameMaterialInstance e : materials) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		if (equipments != null) {
			for (StaticGameEquipmentInstance e : equipments) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}

		return b;
	}

	public Long getStaticGameId() {
		return staticGameId;
	}

	public void setStaticGameId(Long staticGameId) {
		this.staticGameId = staticGameId;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof StaticGameStateByUser)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		StaticGameStateByUser old2 = (StaticGameStateByUser) old;
		if (Glb.getUtil().notEqual(staticGameId, old2.getStaticGameId())) {
			r.add(Lang.STATICGAME_STATEBYUSER_STATICGAME_ID,
					Lang.ERROR_UNALTERABLE, "staticGameId=" + getStaticGameId()
							+ " old.staticGameId=" + old2.getStaticGameId());
			b = false;
		}

		if (Glb.getUtil().notEqual(getOwnerUserId(), old2.getOwnerUserId())) {
			r.add(Lang.USER_ID, Lang.ERROR_UNALTERABLE, "userId="
					+ getOwnerUserId() + " oldUserId=" + old2.getOwnerUserId());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		if (materials != null) {
			for (StaticGameMaterialInstance e : materials) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		if (equipments != null) {
			for (StaticGameEquipmentInstance e : equipments) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}

		return b;
	}

	@Override
	protected boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;

		StaticGameStore s = new StaticGameStore(txn);
		if (s.get(staticGameId) == null) {
			r.add(Lang.STATICGAME_STATEBYUSER_STATICGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"staticGameId=" + staticGameId);
			b = false;
		}
		//ownerUserIdは抽象クラスでチェックされている

		for (StaticGameMaterialInstance e : materials) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		for (StaticGameEquipmentInstance e : equipments) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		return b;
	}

	@Override
	public StaticGameStateByUserGui getGui(String guiName, String cssIdPrefix) {
		return new StaticGameStateByUserGui(guiName, cssIdPrefix);
	}

	@Override
	public StaticGameStateByUserStore getStore(Transaction txn) {
		return new StaticGameStateByUserStore(txn);
	}

	public Long getOwnerUserId() {
		return getMainAdministratorUserId();
	}

	public void setOwnerUserId(Long userId) {
		setMainAdministratorUserId(userId);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//最初のデータを書き込むときに自動作成される
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(getOwnerUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//一定の手続きが行われた場合に自動的に更新される。
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((equipments == null) ? 0 : equipments.hashCode());
		result = prime * result
				+ ((materials == null) ? 0 : materials.hashCode());
		result = prime * result + (int) (payAmount ^ (payAmount >>> 32));
		result = prime * result
				+ ((staticGameId == null) ? 0 : staticGameId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticGameStateByUser other = (StaticGameStateByUser) obj;
		if (equipments == null) {
			if (other.equipments != null)
				return false;
		} else if (!equipments.equals(other.equipments))
			return false;
		if (materials == null) {
			if (other.materials != null)
				return false;
		} else if (!materials.equals(other.materials))
			return false;
		if (payAmount != other.payAmount)
			return false;
		if (staticGameId == null) {
			if (other.staticGameId != null)
				return false;
		} else if (!staticGameId.equals(other.staticGameId))
			return false;
		return true;
	}

	public List<StaticGameMaterialInstance> getMaterials() {
		return materials;
	}

	public void setMaterials(List<StaticGameMaterialInstance> materials) {
		this.materials = materials;
	}

	public static int getMaterialmax() {
		return materialMax;
	}

	@Override
	public String toString() {
		return "StaticGameStateByUser [materials=" + materials
				+ ", staticGameId=" + staticGameId + ", equipments="
				+ equipments + ", payAmount=" + payAmount + "]";
	}

	public StaticGame getGame() {
		return Glb.getObje().getStaticGame(s -> s.get(staticGameId));
	}

	public List<StaticGameEquipmentInstance> getEquipments() {
		return equipments;
	}

	public void setEquipments(List<StaticGameEquipmentInstance> equipments) {
		this.equipments = equipments;
	}

	public long getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(long payAmount) {
		this.payAmount = payAmount;
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.STATIC_GAME_STATE_BY_USER;
	}

}
