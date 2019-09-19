package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.vote;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface DistributedVoteResultDBI extends ObjectivityObjectDBI {
	/**
	 * @return	分散合意ID
	 */
	Long getDistributedVoteId();

	/**
	 * @return	分散合意が開始した時のヒストリーインデックス
	 */
	long getStartHistoryIndex();
}
