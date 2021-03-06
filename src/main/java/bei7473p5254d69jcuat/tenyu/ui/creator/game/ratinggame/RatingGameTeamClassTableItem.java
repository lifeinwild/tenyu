package bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.RatingGame.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import javafx.beans.property.*;

public class RatingGameTeamClassTableItem implements TableRow<TeamClass> {
	private TeamClass src;
	private StringProperty teamName = new SimpleStringProperty();
	private IntegerProperty teamMemberCount = new SimpleIntegerProperty();

	public RatingGameTeamClassTableItem(TeamClass src) {
		this.src = src;
		update();
	}

	public Integer getTeamMemberCount() {
		return teamMemberCount.get();
	}

	public void setTeamMemberCount(int memberCount) {
		this.teamMemberCount.set(memberCount);
	}

	public String getTeamName() {
		return teamName.get();
	}

	public void setTeamName(String name) {
		this.teamName.set(name);
	}

	@Override
	public TeamClass getSrc() {
		return src;
	}

	@Override
	public void update() {
		updateTeamTableItem();
	}

	public void updateTeamTableItem() {
		setTeamMemberCount(src.getMemberCount());
		setTeamName(src.getName());
	}
}