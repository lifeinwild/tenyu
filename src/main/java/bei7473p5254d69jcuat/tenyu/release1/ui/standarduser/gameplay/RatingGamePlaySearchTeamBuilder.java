package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class RatingGamePlaySearchTeamBuilder extends GuiBuilder {

	/**
	 * 自分が参加しているチーム一覧
	 */
	private List<Team> myTeams = new ArrayList<>();


	@Override
	public Node build() {
		//チーム参加
		TeamParticipationGui gui = new TeamParticipationGui(name(), id());
		return gui.buildCreate();
	}

	@Override
	public String name() {
		return Lang.GAMEPLAY_RATINGGAME_SEARCH_TEAM.toString();
	}

	@Override
	public String id() {
		return "gamePlayRatingGameSearchTeam";
	}

}
