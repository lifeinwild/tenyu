package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.MatchingStateByGameTeam.TeamCount.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.beans.property.*;

public class RatingGameTeamCountTableItem implements TableRow<TeamCountEntry> {
	private TeamCountEntry src;
	private RatingGame g;

	private StringProperty teamClassName = new SimpleStringProperty();
	private IntegerProperty fullTeamCount = new SimpleIntegerProperty();

	public RatingGameTeamCountTableItem(TeamCountEntry src) {
		this.src = src;
		g = (RatingGame) src.getGameRef().getGame();
	}

	@Override
	public TeamCountEntry getSrc() {
		return src;
	}

	@Override
	public void update() {
		if (g == null || src == null || src.getTeamClassId() == null)
			return;
		setTeamClassName(g.getTeamClass(src.getTeamClassId()).getName());
		setFullTeamCount(src.getCount());
	}

	public String getTeamClassName() {
		return teamClassName.get();
	}

	public void setTeamClassName(String teamClassName) {
		this.teamClassName.set(teamClassName);
	}

	public Integer getFullTeamCount() {
		return fullTeamCount.get();
	}

	public void setFullTeamCount(Integer count) {
		this.fullTeamCount.set(count);
	}

}
