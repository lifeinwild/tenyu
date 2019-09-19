package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.beans.property.*;

public class RatingGameStateByUserTableItem
		implements TableRow<RatingGameStateByUser> {
	private RatingGameStateByUser srcSociality;
	private User srcUser;
	private StringProperty userName = new SimpleStringProperty();
	private IntegerProperty ratingTeam = new SimpleIntegerProperty();
	private IntegerProperty ratingSingle = new SimpleIntegerProperty();
	private IntegerProperty matchCountTeam = new SimpleIntegerProperty();
	private IntegerProperty matchCountSingle = new SimpleIntegerProperty();

	public RatingGameStateByUserTableItem(
			RatingGameStateByUser srcSociality, User srcUser) {
		this.srcSociality = srcSociality;
		this.srcUser = srcUser;
		update();
	}

	@Override
	public RatingGameStateByUser getSrc() {
		return srcSociality;
	}

	@Override
	public void update() {
		if (srcSociality != null) {
			setRatingSingle(srcSociality.getSingleRating());
			setRatingTeam(srcSociality.getTeamRating());
			setMatchCountSingle(srcSociality.getMatchCountSingle());
			setMatchCountTeam(srcSociality.getMatchCountTeam());
		}
		if (srcUser != null)
			setUserName(srcUser.getName());
	}

	public String getUserName() {
		return userName.get();
	}

	public void setUserName(String name) {
		this.userName.set(name);
	}

	public Integer getRatingTeam() {
		return ratingTeam.get();
	}

	public void setRatingTeam(Integer ratingTeam) {
		this.ratingTeam.set(ratingTeam);
	}

	public Integer getRatingSingle() {
		return ratingSingle.get();
	}

	public void setRatingSingle(Integer ratingSingle) {
		this.ratingSingle.set(ratingSingle);
	}

	public Integer getMatchCountTeam() {
		return matchCountTeam.get();
	}

	public void setMatchCountTeam(Integer matchCountTeam) {
		this.matchCountTeam.set(matchCountTeam);
	}

	public Integer getMatchCountSingle() {
		return matchCountSingle.get();
	}

	public void setMatchCountSingle(Integer matchCountSingle) {
		this.matchCountSingle.set(matchCountSingle);
	}
}
