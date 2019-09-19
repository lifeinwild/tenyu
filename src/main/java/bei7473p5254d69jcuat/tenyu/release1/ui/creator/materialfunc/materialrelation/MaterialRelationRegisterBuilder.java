package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialRelationRegisterBuilder extends GuiBuilder {
	@Override
	public Node build() {
		GuiCommon.onlyUser(name());

		MaterialRelationGui built = new MaterialRelationGui(name(), id());
		built.buildCreate();
		built.buildExternalButton(gui -> built.validateAtCreate(gui),
				gui -> false, null, null);

		return built.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIALRELATION_REGISTER.toString();
	}

	@Override
	public String id() {
		return "materialRelationRegister";
	}

}
