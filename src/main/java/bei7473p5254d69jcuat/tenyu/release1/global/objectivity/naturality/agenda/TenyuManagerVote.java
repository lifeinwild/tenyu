package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 投票権:全体運営者
 * 差別化:全体運営者の影響度割合
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuManagerVote extends AgendaVoteType {

	@Override
	public double getPower(Long userId) {
		return Glb.getObje().getCore().getManagerList()
				.getManagerPower(userId);
	}

	@Override
	public boolean isVotable(Long userId) {
		return Glb.getObje().getCore().getManagerList().getManagerIds()
				.contains(userId);
	}

}