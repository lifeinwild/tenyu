package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.RatingGame.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.beans.property.*;

public class RatingGameTeamTableItem implements TableRow<Team> {
	private Team src;
	private TeamClass tc;
	private RatingGame g;
	private StringProperty teamClassName = new SimpleStringProperty();
	private StringProperty teamName = new SimpleStringProperty();
	private BooleanProperty usePassword = new SimpleBooleanProperty();
	private IntegerProperty currentMemberCount = new SimpleIntegerProperty();
	private IntegerProperty teamClassMemberCount = new SimpleIntegerProperty();
	private IntegerProperty teamRating = new SimpleIntegerProperty();
	private IntegerProperty singleRating = new SimpleIntegerProperty();

	public RatingGameTeamTableItem(Team src) {
		this.src = src;
		g = src.getGame();
		tc = g.getTeamClass(src.getTeamClassId());
		update();
	}

	@Override
	public Team getSrc() {
		return src;
	}

	@Override
	public void update() {
		setTeamClassName(tc.getName());
		setTeamName(src.getName());
		setUsePassword(src.getPasswordHash() != null);
	}

	public String getTeamClassName() {
		return teamClassName.get();
	}

	public void setTeamClassName(String teamClassName) {
		this.teamClassName.set(teamClassName);
	}

	public String getTeamName() {
		return teamName.get();
	}

	public void setTeamName(String name) {
		this.teamName.set(name);
	}

	public Boolean getUsePassword() {
		return usePassword.get();
	}

	public void setUsePassword(Boolean name) {
		this.usePassword.set(name);
	}

	public Integer getCurrentMemberCount() {
		return currentMemberCount.get();
	}

	public void setCurrentMemberCount(int currentMemberCount) {
		this.currentMemberCount.set(currentMemberCount);
	}

	public Integer getTeamRating() {
		return teamRating.get();
	}

	public void setTeamRating(int teamRating) {
		this.teamRating.set(teamRating);
	}

	public Integer getSingleRating() {
		return singleRating.get();
	}

	public void setSingleRating(int teamRating) {
		this.singleRating.set(teamRating);
	}

	public Integer getTeamClassMemberCount() {
		return teamClassMemberCount.get();
	}

	public void setTeamClassMemberCount(int maxMemberSize) {
		this.teamClassMemberCount.set(maxMemberSize);
	}
}
