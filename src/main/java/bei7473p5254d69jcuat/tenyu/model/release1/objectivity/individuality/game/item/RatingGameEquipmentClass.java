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
 * 購入対象
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameEquipmentClass extends IndividualityObject
		implements RatingGameEquipmentClassDBI {
	private Long ratingGameId;

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (ratingGameId == null) {
			r.add(Lang.RATINGGAME_EQUIPMENT_CLASS_RATINGGAME_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(ratingGameId)) {
				r.add(Lang.RATINGGAME_EQUIPMENT_CLASS_RATINGGAME_ID,
						Lang.ERROR_INVALID, "ratingGameId=" + ratingGameId);
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
		if (!(old instanceof RatingGameEquipmentClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		RatingGameEquipmentClass old2 = (RatingGameEquipmentClass) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(ratingGameId, old2.getRatingGameId())) {
			r.add(Lang.RATINGGAME_EQUIPMENT_CLASS_RATINGGAME_ID,
					Lang.ERROR_UNALTERABLE, "ratingGameId=" + ratingGameId
							+ " old.ratingGameId=" + ratingGameId);
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
	public final boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (getGame(txn) == null) {
			r.add(Lang.RATINGGAME_EQUIPMENT_CLASS_RATINGGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"ratingGameId=" + ratingGameId);
			b = false;
		}
		return b;
	}

	@Override
	public RatingGameEquipmentClassStore getStore(Transaction txn) {
		return new RatingGameEquipmentClassStore(txn);
	}

	@Override
	public RatingGameEquipmentClassGui getGui(String guiName,
			String cssIdPrefix) {
		return new RatingGameEquipmentClassGui(guiName, cssIdPrefix);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((ratingGameId == null) ? 0 : ratingGameId.hashCode());
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
		RatingGameEquipmentClass other = (RatingGameEquipmentClass) obj;
		if (ratingGameId == null) {
			if (other.ratingGameId != null)
				return false;
		} else if (!ratingGameId.equals(other.ratingGameId))
			return false;
		return true;
	}

	public Long getRatingGameId() {
		return ratingGameId;
	}

	public void setRatingGameId(Long ratingGameId) {
		this.ratingGameId = ratingGameId;
	}

	@Override
	public String toString() {
		return "RatingGameEquipmentClass [ratingGameId=" + ratingGameId + "]";
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	public RatingGameStore getGameStore(Transaction txn) {
		return new RatingGameStore(txn);
	}

	public RatingGame getGame() {
		return Glb.getObje().getRatingGame(s -> s.get(ratingGameId));
	}

	public RatingGame getGame(Transaction txn) {
		return new RatingGameStore(txn).get(ratingGameId);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		RatingGame g = getGame();
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
		return StoreNameObjectivity.RATING_GAME_EQUIPMENT_CLASS;
	}

}
