package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * ゲームサーバーによって付与されるもの
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GameMaterialClass extends GameItemClass
		implements GameMaterialClassDBI {

	@Override
	protected boolean validateAtCreateGameItemConcrete(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeGameItemClassConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof GameMaterialClass)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//GameMaterialClass old2 = (GameMaterialClass) old;

		boolean b = true;
		return b;
	}

	@Override
	protected boolean validateAtUpdateGameItemConcrete(ValidationResult r) {
		return true;
	}

}
