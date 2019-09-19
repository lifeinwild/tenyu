package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.RatingGame.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
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