package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import jetbrains.exodus.env.*;

public class StaticGameStateByUser extends GameStateByUser
		implements StaticGameStateByUserDBI {
	@Override
	public boolean validateAtCreateGameStateByUserConcrete(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeGameStateByUserConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof StaticGameStateByUser)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//StaticGameStateByUser old2 = (StaticGameStateByUser) old;

		boolean b = true;
		return b;
	}

	@Override
	public boolean validateAtUpdateGameStateByUserConcrete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceGameStateByUserConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		GameReference gameRef = getGameRef();
		AbstractGame game = gameRef.getGame(txn);
		if (game == null) {
			//抽象クラスでこの検証はしている
			b = false;
		} else {
			if (!(game instanceof StaticGame)) {
				r.add(Lang.STATICGAMESTATEBYUSER_GAMEREFERENCE,
						Lang.ERROR_INVALID, gameRef.toString());
				b = false;
			}
		}
		return b;
	}

}
