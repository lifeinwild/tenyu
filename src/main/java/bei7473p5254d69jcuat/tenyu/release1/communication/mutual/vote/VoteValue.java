package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.vote;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 名目でequalsを実装する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface VoteValue extends Storable {
	/**
	 * @return 投票の名目、この投票が対応するDistributedVote#getName()と同値
	 */
	//String getVoteName();

	VoteValue clone();

	Long getDistributedVoteId();

	boolean equals(Object o);

	int hashCode();

	/**
	 * @return	多数派の値
	 */
	//VoteContent calculateMajority(Collection<VoteContent> votes);

	/**
	 * 確認処理段階、言い換えれば収束段階の多数派の値の計算
	 * 実装クラスによって、calculateMajorityと同じで良い。
	 *
	 * @param votes
	 * @return
	 */
	//VoteContent calculateMajorityConvergence(Collection<VoteContent> votes);

}