package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;

public interface RatingGameMatchI extends AdministratedObjectI {
	Long getRatingGameId();

	HashSet<Long> getPlayers();
}
