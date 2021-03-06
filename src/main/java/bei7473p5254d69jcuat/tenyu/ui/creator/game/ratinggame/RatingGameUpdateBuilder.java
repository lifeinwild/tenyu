package bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.scene.*;

public class RatingGameUpdateBuilder extends GuiBuilder {
	public Node build(Long id) {
		RatingGame rg = Glb.getObje().getRatingGame(rgs -> rgs.get(id));
		if (rg == null)
			return null;

		RatingGameGui built = new RatingGameGui(name(), id());
		return built.buildUpdate(rg);
	}

	@Override
	public Node build() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String name() {
		return Lang.RATINGGAME.toString();
	}

	@Override
	public String id() {
		return "ratingGameUpdate";
	}

}
