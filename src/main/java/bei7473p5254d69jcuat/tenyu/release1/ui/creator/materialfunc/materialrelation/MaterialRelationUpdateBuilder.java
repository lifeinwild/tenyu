package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialRelationUpdateBuilder extends GuiBuilder {

	@Override
	public Node build() {
		return null;
	}

	public Node build(Long id) {
		if (id == null)
			return null;
		MaterialRelation exist = Glb.getObje()
				.getMaterialRelation(mrs -> mrs.get(id));
		if (exist == null)
			return null;
		GuiCommon.onlyAdmin(name(), exist.getAdministratorUserIdUpdate());
		MaterialRelationGui built = new MaterialRelationGui(name(), id());
		built.buildUpdate(exist);

		built.buildExternalButton(gui -> built.validateAtUpdate(gui, exist),
				gui -> false, null, null);
		return built.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIALRELATION_UPDATE.toString();
	}

	@Override
	public String id() {
		return "materialRelationUpdate";
	}

}
