package bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import javafx.beans.property.*;

public class RatingGameStateByUserTableItem extends
		AdministratedObjectTableItem<RatingGameStateByUserI,
				RatingGameStateByUser>
		implements TableRow<RatingGameStateByUser> {
	private User srcUser;
	private StringProperty userName = new SimpleStringProperty();
	private IntegerProperty ratingTeam = new SimpleIntegerProperty();
	private IntegerProperty ratingSingle = new SimpleIntegerProperty();
	private IntegerProperty matchCountTeam = new SimpleIntegerProperty();
	private IntegerProperty matchCountSingle = new SimpleIntegerProperty();

	public RatingGameStateByUserTableItem(RatingGameStateByUser srcSociality,
			User srcUser) {
		super(srcSociality);
		this.srcUser = srcUser;
		update();
	}

	@Override
	public void update() {
		if (src != null) {
			setRatingSingle(src.getSingleRating());
			setRatingTeam(src.getTeamRating());
			setMatchCountSingle(src.getMatchCountSingle());
			setMatchCountTeam(src.getMatchCountTeam());
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
