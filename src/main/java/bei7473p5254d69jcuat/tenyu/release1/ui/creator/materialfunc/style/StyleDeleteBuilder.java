package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class StyleDeleteBuilder extends GuiBuilder {

	public Node build(Long id) {
		if (id == null)
			return null;
		Style exist = Glb.getObje().getStyle(ss -> ss.get(id));

		GuiCommon.onlyAdmin(name(), exist.getAdministratorUserIdDelete());
		StyleGui built = new StyleGui(name(), id());
		built.buildDelete(exist);
		built.buildExternalButton(
				new SubmitButtonFuncs(gui -> built.validateAtUpdate(gui, exist),
						gui -> true, null, null));
		return built.getGrid();
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public String name() {
		return Lang.STYLE_DELETE.toString();
	}

	@Override
	public String id() {
		return "styleDelete";
	}

}
