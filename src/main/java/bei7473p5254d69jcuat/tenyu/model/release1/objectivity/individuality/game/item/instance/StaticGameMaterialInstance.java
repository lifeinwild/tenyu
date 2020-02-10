package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.instance;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class StaticGameMaterialInstance implements Storable {
	/**
	 * 材料の種別
	 */
	private Long gameMaterialClassId;
	/**
	 * 量
	 */
	private long amount;

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void addAmount(long add) {
		amount += add;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (gameMaterialClassId == null) {
			r.add(Lang.STATICGAME_MATERIAL_INSTANCE_CLASS_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(gameMaterialClassId)) {
				r.add(Lang.STATICGAME_MATERIAL_INSTANCE_CLASS_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (amount <= 0) {
			r.add(Lang.STATICGAME_MATERIAL_INSTANCE_AMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	public Long getGameMaterialClassId() {
		return gameMaterialClassId;
	}

	public void setGameMaterialClassId(Long gameMaterialClassId) {
		this.gameMaterialClassId = gameMaterialClassId;
	}

	public StaticGameMaterialClass getItemClass() {
		return Glb.getObje()
				.getStaticGameMaterialClass(gmcs -> gmcs.get(gameMaterialClassId));
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (new StaticGameMaterialClassStore(txn).get(gameMaterialClassId) == null) {
			r.add(Lang.STATICGAME_MATERIAL_INSTANCE_CLASS_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (amount ^ (amount >>> 32));
		result = prime * result
				+ ((gameMaterialClassId == null) ? 0 : gameMaterialClassId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticGameMaterialInstance other = (StaticGameMaterialInstance) obj;
		if (amount != other.amount)
			return false;
		if (gameMaterialClassId == null) {
			if (other.gameMaterialClassId != null)
				return false;
		} else if (!gameMaterialClassId.equals(other.gameMaterialClassId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StaticGameMaterialInstance [gameMaterialClassId="
				+ gameMaterialClassId + ", amount=" + amount + "]";
	}

}
