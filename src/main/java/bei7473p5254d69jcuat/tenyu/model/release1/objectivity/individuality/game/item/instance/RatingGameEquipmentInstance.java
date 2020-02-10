package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.instance;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class RatingGameEquipmentInstance implements Storable {
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
	protected Long ratingGameEquipmentClassId;

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

	public Long getRatingGameEquipmentClassId() {
		return ratingGameEquipmentClassId;
	}

	public RatingGameEquipmentClass getItemClass() {
		return Glb.getObje().getRatingGameEquipmentClass(
				s -> s.get(ratingGameEquipmentClassId));
	}

	public List<String> getOptions() {
		return options;
	}

	public void setRatingGameEquipmentClassId(Long gameEquipmentId) {
		this.ratingGameEquipmentClassId = gameEquipmentId;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (ratingGameEquipmentClassId == null) {
			r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_CLASS_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(
					ratingGameEquipmentClassId)) {
				r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_CLASS_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (options != null) {
			if (options.size() > optionCountMax) {
				r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_OPTIONS,
						Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (String option : options) {
					if (option.length() > optionLengthMax) {
						r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_OPTION,
								Lang.ERROR_TOO_LONG);
						b = false;
						break;
					}
				}
			}
		}
		if (purchaseHistoryIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_PURCHASE_HISTORYINDEX,
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
		if (new RatingGameEquipmentClassStore(txn)
				.get(ratingGameEquipmentClassId) == null) {
			r.add(Lang.RATINGGAME_EQUIPMENT_INSTANCE_CLASS_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result
				+ (int) (purchaseHistoryIndex ^ (purchaseHistoryIndex >>> 32));
		result = prime * result + ((ratingGameEquipmentClassId == null) ? 0
				: ratingGameEquipmentClassId.hashCode());
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
		RatingGameEquipmentInstance other = (RatingGameEquipmentInstance) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (purchaseHistoryIndex != other.purchaseHistoryIndex)
			return false;
		if (ratingGameEquipmentClassId == null) {
			if (other.ratingGameEquipmentClassId != null)
				return false;
		} else if (!ratingGameEquipmentClassId
				.equals(other.ratingGameEquipmentClassId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RatingGameEquipmentInstance [ratingGameEquipmentClassId="
				+ ratingGameEquipmentClassId + ", options=" + options
				+ ", purchaseHistoryIndex=" + purchaseHistoryIndex + "]";
	}
}
