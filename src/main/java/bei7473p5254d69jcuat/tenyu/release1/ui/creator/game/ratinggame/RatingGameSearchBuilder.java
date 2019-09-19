package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class RatingGameSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		RatingGameGui searchGui = new RatingGameGui(name(), id());
		searchGui.buildSearch(null);
		searchGui.buildExternalButton(
				//修正ページへ遷移するボタン
				new SubmitButtonFuncs(Lang.RATINGGAME_UPDATE.toString(), id(),
						a -> {
							//FXスレッドで動作させるため検証処理として登録
							try {
								String idStr = searchGui.getDetailGui()
										.getIdInput().getText();
								Long id = Long.valueOf(idStr);
								RatingGameUpdateBuilder builder = new RatingGameUpdateBuilder();
								Glb.getGui().createTab(builder.build(id),
										builder.name());
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
		return Lang.RATINGGAME_SEARCH.toString();
	}

	@Override
	public String id() {
		return "ratingGameSearch";
	}

}
