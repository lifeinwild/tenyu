package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda;

import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;

/**
 * 投票権:全ユーザー
 * 差別化:到達フロー
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserVote extends AgendaVoteType {

	@Override
	public double getPower(Long userId) {
		Sociality so = SocialityStore.getByUserIdSimple(userId);
		if (so == null)
			return 0;
		return so.getFlowFromCooperativeAccount();
	}

	@Override
	public boolean isVotable(Long userId) {
		try {
			return !SocialityStore.isBanStatic(NodeType.USER, userId);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

}