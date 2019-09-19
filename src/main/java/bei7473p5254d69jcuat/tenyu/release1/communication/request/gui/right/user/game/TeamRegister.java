package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user.game;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.GameStateByUser.*;

public class TeamRegister {
	private String name;
	private String password;
	private Long gameId;
	private GameType type = GameType.RATINGGAME;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
