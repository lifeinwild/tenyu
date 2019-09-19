package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame.*;
import javafx.scene.*;

public class RatingGamePlaySearchSingleBuilder extends GuiBuilder {

	@Override
	public Node build() {
		RatingGameGui searchGui = new RatingGameGui(name(), id());

		searchGui.buildSearch(null);

		searchGui.buildExternalButton(new SubmitButtonFuncs(
				Lang.GAMEPLAY_RATINGGAME_APPLICATE_SINGLE.toString(), id(),
				gui -> {
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null));
		return searchGui.getGrid();
	}

	@Override
	public String name() {
		return Lang.GAMEPLAY_RATINGGAME_SEARCH_SINGLE.toString();
	}

	@Override
	public String id() {
		return "gamePlayRatingGameSearchSingle";
	}

}
