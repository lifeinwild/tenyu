package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
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
		Sociality so = SocialityStore.getByUserIdStatic(userId);
		if (so == null)
			return 0;
		return so.getFlowFromCooperativeAccount();
	}

	@Override
	public boolean isVotable(Long userId) {
		try {
			return !SocialityStore.isBanStatic(StoreNameObjectivity.USER,
					userId);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

}