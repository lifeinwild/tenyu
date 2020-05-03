package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;

public interface StaticGameStateByUserI extends AdministratedObjectI {
	Long getStaticGameId();
	Long getOwnerUserId();
}
