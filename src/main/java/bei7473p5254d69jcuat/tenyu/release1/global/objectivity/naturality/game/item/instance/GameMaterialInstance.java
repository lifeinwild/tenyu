package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.instance;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import jetbrains.exodus.env.*;

public class GameMaterialInstance extends GameItemInstance {
	/**
	 * 材料の種別
	 */
	private Long gameMaterialId;
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

	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (new GameMaterialClassStore(txn).get(gameMaterialId) == null) {
			r.add(Lang.GAMEMATERIAL_INSTANCE_MATERIAL_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	private boolean validateAtCommonGameInstanceConcrete(ValidationResult r) {
		boolean b = true;
		if (gameMaterialId == null) {
			r.add(Lang.GAMEMATERIAL_INSTANCE_MATERIAL_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(gameMaterialId)) {
				r.add(Lang.GAMEMATERIAL_INSTANCE_MATERIAL_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (amount <= 0) {
			r.add(Lang.GAMEMATERIAL_INSTANCE_AMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	public Long getGameMaterialId() {
		return gameMaterialId;
	}

	public void setGameMaterialId(Long gameMaterialId) {
		this.gameMaterialId = gameMaterialId;
	}

	@Override
	public GameMaterialClass getItemClass() {
		return Glb.getObje()
				.getGameMaterialClass(gmcs -> gmcs.get(gameMaterialId));
	}

	@Override
	protected boolean validateReferenceGameInstanceConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (new GameMaterialClassStore(txn).get(gameMaterialId) == null) {
			r.add(Lang.GAMEMATERIAL_INSTANCE_MATERIAL_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateGameInstanceConcrete(ValidationResult r) {
		return validateAtCommonGameInstanceConcrete(r);
	}

	@Override
	protected boolean validateAtUpdateGameInstanceConcrete(ValidationResult r) {
		return validateAtCommonGameInstanceConcrete(r);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (amount ^ (amount >>> 32));
		result = prime * result
				+ ((gameMaterialId == null) ? 0 : gameMaterialId.hashCode());
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
		GameMaterialInstance other = (GameMaterialInstance) obj;
		if (amount != other.amount)
			return false;
		if (gameMaterialId == null) {
			if (other.gameMaterialId != null)
				return false;
		} else if (!gameMaterialId.equals(other.gameMaterialId))
			return false;
		return true;
	}

}
