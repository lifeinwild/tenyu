package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialRelationDeleteBuilder extends GuiBuilder {

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
		GuiCommon.onlyAdmin(name(), exist.getAdministratorUserIdDelete());
		MaterialRelationGui built = new MaterialRelationGui(name(), id());
		built.buildDelete(exist);

		built.buildExternalButton(gui -> built.validateAtDelete(gui, exist),
				gui -> false, null, null);

		return built.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIALRELATION_DELETE.toString();
	}

	@Override
	public String id() {
		return "materialRelationDelete";
	}

}
