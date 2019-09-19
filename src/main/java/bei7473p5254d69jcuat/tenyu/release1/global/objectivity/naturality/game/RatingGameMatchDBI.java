package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;

public interface RatingGameMatchDBI extends ObjectivityObjectDBI {
	Long getRatingGameId();

	HashSet<Long> getPlayers();
}
