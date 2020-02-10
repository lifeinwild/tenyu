package bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.scene.*;

public class RatingGameRegisterBuilder extends GuiBuilder {

	@Override
	public Node build() {
		RatingGameGui built = new RatingGameGui(name(), id());
		return built.buildCreate();
	}

	@Override
	public String name() {
		return Lang.RATINGGAME_REGISTER.toString();
	}

	@Override
	public String id() {
		return "ratingGameRegister";
	}

}
