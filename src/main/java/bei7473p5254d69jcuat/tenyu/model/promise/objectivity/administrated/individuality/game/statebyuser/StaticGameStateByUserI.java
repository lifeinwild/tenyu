package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;

public interface StaticGameStateByUserI extends AdministratedObjectI {
	Long getStaticGameId();
	Long getOwnerUserId();
}
