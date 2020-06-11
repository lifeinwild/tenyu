package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.item;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 材料の種類
 * 常駐空間ゲームサーバーによって付与される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StaticGameMaterialClass extends IndividualityObject
		implements StaticGameMaterialClassI {
	private Long staticGameId;

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (staticGameId == null) {
			r.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(staticGameId)) {
				r.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
						Lang.ERROR_INVALID, "staticGameId=" + staticGameId);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof StaticGameMaterialClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		StaticGameMaterialClass old2 = (StaticGameMaterialClass) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(staticGameId, old2.getStaticGameId())) {
			r.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
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
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public StaticGameMaterialClassStore getStore(Transaction txn) {
		return new StaticGameMaterialClassStore(txn);
	}

	@Override
	public StaticGameMaterialClassGui getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new StaticGameMaterialClassGui(guiName, cssIdPrefix);
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		StaticGameStore s = new StaticGameStore(txn);
		if (s.get(staticGameId) == null) {
			r.add(Lang.STATICGAME_MATERIAL_CLASS_STATICGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"staticGameId=" + staticGameId);
			b = false;
		}
		return b;
	}

	public StaticGame getGame() {
		return Glb.getObje().getStaticGame(s -> s.get(staticGameId));
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

	public Long getStaticGameId() {
		return staticGameId;
	}

	public void setStaticGameId(Long staticGameId) {
		this.staticGameId = staticGameId;
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
		StaticGameMaterialClass other = (StaticGameMaterialClass) obj;
		if (staticGameId == null) {
			if (other.staticGameId != null)
				return false;
		} else if (!staticGameId.equals(other.staticGameId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StaticGameMaterialClass [staticGameId=" + staticGameId + "]";
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.STATIC_GAME_MATERIAL_CLASS;
	}

}
