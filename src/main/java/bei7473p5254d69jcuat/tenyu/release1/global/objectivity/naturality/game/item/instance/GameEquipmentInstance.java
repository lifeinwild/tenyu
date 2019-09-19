package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.instance;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import jetbrains.exodus.env.*;

public class GameEquipmentInstance extends GameItemInstance {
	/**
	 * オプションの最大数
	 */
	public static final int optionCountMax = 20;

	/**
	 * 1オプションの最大長
	 */
	public static final int optionLengthMax = 50;

	/**
	 * 装備の種別
	 */
	protected Long gameEquipmentId;
	/**
	 * 任意のオプションをつけれる
	 * 使うか分からないが一応設置
	 */
	protected List<String> options = null;

	public boolean addOption(String newOption) {
		if (options == null)
			options = new ArrayList<>();
		return options.add(newOption);
	}

	public Long getGameEquipmentId() {
		return gameEquipmentId;
	}

	@Override
	public GameItemClass getItemClass() {
		return Glb.getObje()
				.getGameEquipmentClass(gecs -> gecs.get(gameEquipmentId));
	}

	public List<String> getOptions() {
		return options;
	}

	public void setGameEquipmentId(Long gameEquipmentId) {
		this.gameEquipmentId = gameEquipmentId;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	private boolean validateAtCommonGameInstanceConcrete(ValidationResult r) {
		boolean b = true;
		if (gameEquipmentId == null) {
			r.add(Lang.GAMEEQUIPMENT_INSTANCE_EQUIPMENT_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(gameEquipmentId)) {
				r.add(Lang.GAMEEQUIPMENT_INSTANCE_EQUIPMENT_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (options != null) {
			if (options.size() > optionCountMax) {
				r.add(Lang.GAMEEQUIPMENT_INSTANCE_OPTIONS, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (String option : options) {
					if (option.length() > optionLengthMax) {
						r.add(Lang.GAMEEQUIPMENT_INSTANCE_OPTION,
								Lang.ERROR_TOO_LONG);
						b = false;
						break;
					}
				}
			}
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
	protected final boolean validateReferenceGameInstanceConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (new GameEquipmentClassStore(txn).get(gameEquipmentId) == null) {
			r.add(Lang.GAMEEQUIPMENT_INSTANCE_EQUIPMENT_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((gameEquipmentId == null) ? 0 : gameEquipmentId.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
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
		GameEquipmentInstance other = (GameEquipmentInstance) obj;
		if (gameEquipmentId == null) {
			if (other.gameEquipmentId != null)
				return false;
		} else if (!gameEquipmentId.equals(other.gameEquipmentId))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}
}
