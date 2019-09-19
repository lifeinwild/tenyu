package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class AvatarRegisterBuilder extends GuiBuilder {

	@Override
	public Node build() {
		AvatarGui built = new AvatarGui(name(), id());
		built.buildCreate();
		built.buildExternalButton(
				new SubmitButtonFuncs(gui -> built.validateAtCreate(gui),
						gui -> true, gui -> built.clear(), null));
		return built.getGrid();
	}

	@Override
	public String name() {
		return Lang.AVATAR_REGISTER.toString();
	}

	@Override
	public String id() {
		return "avatarRegister";
	}

}
