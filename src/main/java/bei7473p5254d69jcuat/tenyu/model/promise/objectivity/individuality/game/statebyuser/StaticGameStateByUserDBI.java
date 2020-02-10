package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.db.store.*;

public interface StaticGameStateByUserDBI extends AdministratedObjectDBI {
	Long getStaticGameId();
	Long getOwnerUserId();
}
