package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 装備
 *
 * 購入するもの
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StaticGameEquipmentClass extends IndividualityObject
		implements StaticGameEquipmentClassDBI {
	private Long staticGameId;

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (staticGameId == null) {
			r.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(staticGameId)) {
				r.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
						Lang.ERROR_INVALID, "staticGameId=" + staticGameId);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
		} else {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof StaticGameEquipmentClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		StaticGameEquipmentClass old2 = (StaticGameEquipmentClass) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(staticGameId, old2.getStaticGameId())) {
			r.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
					Lang.ERROR_UNALTERABLE, "staticGameId=" + staticGameId
							+ " old.StaticGameId=" + staticGameId);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public StaticGameEquipmentClassStore getStore(Transaction txn) {
		return new StaticGameEquipmentClassStore(txn);
	}

	@Override
	public StaticGameEquipmentClassGui getGui(String guiName,
			String cssIdPrefix) {
		return new StaticGameEquipmentClassGui(guiName, cssIdPrefix);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		StaticGameEquipmentClass other = (StaticGameEquipmentClass) obj;
		if (staticGameId == null) {
			if (other.staticGameId != null)
				return false;
		} else if (!staticGameId.equals(other.staticGameId))
			return false;
		return true;
	}

	public Long getStaticGameId() {
		return staticGameId;
	}

	public void setStaticGameId(Long StaticGameId) {
		this.staticGameId = StaticGameId;
	}

	@Override
	public String toString() {
		return "StaticGameEquipmentClass [staticGameId=" + staticGameId + "]";
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	protected final boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (getGame(txn) == null) {
			r.add(Lang.STATICGAME_EQUIPMENT_CLASS_STATICGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"staticGameId=" + staticGameId);
			b = false;
		}
		return b;
	}

	public StaticGameStore getGameStore(Transaction txn) {
		return new StaticGameStore(txn);
	}

	public StaticGame getGame() {
		return Glb.getObje().getStaticGame(s -> s.get(staticGameId));
	}

	public StaticGame getGame(Transaction txn) {
		return new StaticGameStore(txn).get(staticGameId);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		StaticGame g = getGame();
		if (g != null) {
			r.add(g.getMainAdministratorUserId());
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

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.STATIC_GAME_EQUIPMENT_CLASS;
	}

}
