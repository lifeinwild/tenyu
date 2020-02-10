package bei7473p5254d69jcuat.tenyu.communication.request.gui.right.user.game;

/**
 * マッチング申請
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class MatchingApplication {
	/**
	 * このゲームタイトルの試合に参加する
	 */
	protected Long gameId;
	/**
	 * 申請者
	 */
	protected Long userId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

}
