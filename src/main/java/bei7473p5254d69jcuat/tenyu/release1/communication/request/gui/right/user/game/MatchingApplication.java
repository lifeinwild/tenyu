package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user.game;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.GameStateByUser.*;

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
	protected GameType type = GameType.RATINGGAME;
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

	public GameType getType() {
		return type;
	}

	public void setType(GameType type) {
		this.type = type;
	}

}
