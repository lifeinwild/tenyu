package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.instance;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class StaticGameEquipmentInstance implements Storable {
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
	protected Long gameEquipmentClassId;
	/**
	 * 任意のオプションをつけれる
	 * 使うか分からないが一応設置
	 */
	protected List<String> options = null;

	/**
	 * 購入した時のHI
	 * このデータがある事で装備に有効期限を作ることができる。
	 */
	protected long purchaseHistoryIndex;

	public long getPurchaseHistoryIndex() {
		return purchaseHistoryIndex;
	}

	public void setPurchaseHistoryIndex(long purchaseHistoryIndex) {
		this.purchaseHistoryIndex = purchaseHistoryIndex;
	}

	public boolean addOption(String newOption) {
		if (options == null)
			options = new ArrayList<>();
		return options.add(newOption);
	}

	public Long getGameEquipmentClassId() {
		return gameEquipmentClassId;
	}

	public StaticGameEquipmentClass getItemClass() {
		return Glb.getObje().getStaticGameEquipmentClass(
				gecs -> gecs.get(gameEquipmentClassId));
	}

	public List<String> getOptions() {
		return options;
	}

	public void setGameEquipmentClassId(Long gameEquipmentClassId) {
		this.gameEquipmentClassId = gameEquipmentClassId;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (gameEquipmentClassId == null) {
			r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_CLASS_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject
					.validateIdStandardNotSpecialId(gameEquipmentClassId)) {
				r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_CLASS_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (options != null) {
			if (options.size() > optionCountMax) {
				r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_OPTIONS,
						Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (String option : options) {
					if (option.length() > optionLengthMax) {
						r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_OPTION,
								Lang.ERROR_TOO_LONG);
						b = false;
						break;
					}
				}
			}
		}
		if (purchaseHistoryIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_PURCHASE_HISTORYINDEX,
					Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (new StaticGameEquipmentClassStore(txn)
				.get(gameEquipmentClassId) == null) {
			r.add(Lang.STATICGAME_EQUIPMENT_INSTANCE_CLASS_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameEquipmentClassId == null) ? 0
				: gameEquipmentClassId.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result
				+ (int) (purchaseHistoryIndex ^ (purchaseHistoryIndex >>> 32));
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
		StaticGameEquipmentInstance other = (StaticGameEquipmentInstance) obj;
		if (gameEquipmentClassId == null) {
			if (other.gameEquipmentClassId != null)
				return false;
		} else if (!gameEquipmentClassId.equals(other.gameEquipmentClassId))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (purchaseHistoryIndex != other.purchaseHistoryIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StaticGameEquipmentInstance [gameEquipmentId="
				+ gameEquipmentClassId + ", options=" + options
				+ ", purchaseHistoryIndex=" + purchaseHistoryIndex + "]";
	}
}
