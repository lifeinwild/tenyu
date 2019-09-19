package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class StyleRegisterBuilder extends GuiBuilder {

	@Override
	public Node build() {
		GuiCommon.onlyAdmin(name(), Style.getAdministratorUserIdCreateStatic());
		StyleGui built = new StyleGui(name(), id());
		built.buildCreate();

		//登録
		built.buildExternalButton(
				new SubmitButtonFuncs(gui -> built.validateAtCreate(gui),
						gui -> true, gui -> built.clear(), null));
		return built.getGrid();
	}

	@Override
	public String name() {
		return Lang.STYLE_REGISTER.toString();
	}

	@Override
	public String id() {
		return "styleRegister";
	}

}
