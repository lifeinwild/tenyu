package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 装備
 *
 * 購入するもの
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GameEquipmentClass extends GameItemClass
		implements GameEquipmentClassDBI {
	@Override
	protected boolean validateAtCreateGameItemConcrete(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeGameItemClassConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof GameEquipmentClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//GameEquipmentClass old2 = (GameEquipmentClass) old;

		boolean b = true;
		return b;
	}

	@Override
	protected boolean validateAtUpdateGameItemConcrete(ValidationResult r) {
		return true;
	}

}
