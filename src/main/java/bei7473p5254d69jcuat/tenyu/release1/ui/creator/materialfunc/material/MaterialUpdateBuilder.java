package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

/**
 * 単品更新
 * @author exceptiontenyu@gmail.com
 *
 */
public class MaterialUpdateBuilder extends GuiBuilder {

	@Override
	public Node build() {
		return null;
	}

	public Node build(Long id) {
		if (id == null)
			return null;
		Material exist = Glb.getObje().getMaterial(ms -> ms.get(id));
		if (exist == null)
			return null;
		GuiCommon.onlyAdmin(name(), exist.getAdministratorUserIdUpdate());

		MaterialGui built1 = new MaterialGui(name(), id());
		built1.buildUpdate(exist);
		built1.set(exist);
		built1.buildExternalButton(new SubmitButtonFuncs(
				gui -> built1.validateAtUpdate(gui, exist), gui -> true,
				gui -> built1.clear(), null));
		return built1.getGrid();
	}

	@Override
	public String id() {
		return "materialUpdate";
	}

	@Override
	public String name() {
		return Lang.MATERIAL_UPDATE.toString();
	}

}
