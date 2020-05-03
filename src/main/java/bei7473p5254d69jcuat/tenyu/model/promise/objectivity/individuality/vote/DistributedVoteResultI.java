package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.vote;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;

public interface DistributedVoteResultI extends AdministratedObjectI {
	/**
	 * @return	分散合意ID
	 */
	Long getDistributedVoteId();

	/**
	 * @return	分散合意が開始した時のヒストリーインデックス
	 */
	long getStartHistoryIndex();
}
