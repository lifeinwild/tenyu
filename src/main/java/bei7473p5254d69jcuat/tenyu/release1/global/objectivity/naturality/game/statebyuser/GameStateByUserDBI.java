package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface GameStateByUserDBI extends ObjectivityObjectDBI {
	GameReference getGameRef();

	Long getOwnerUserId();
}