package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user.game;

public class MatchingApplicationTeam extends MatchingApplication {
	/**
	 * このチームに所属する
	 */
	private Long teamId;
	/**
	 * チームに所属するためのパスワード
	 */
	private String password;

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
