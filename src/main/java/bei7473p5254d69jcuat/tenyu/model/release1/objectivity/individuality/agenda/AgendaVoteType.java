package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda;

/**
 * 議決への参加者や投票権の差別化方法を設定する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AgendaVoteType {

	/**
	 * @param userId
	 * @return	このユーザーの投票権
	 */
	public abstract double getPower(Long userId);

	/**
	 * @param userId
	 * @return	このユーザーは投票可能か
	 */
	public abstract boolean isVotable(Long userId);

}
