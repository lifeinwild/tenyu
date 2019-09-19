package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialDeleteBuilder extends GuiBuilder {
	public Node build(Long id) {
		Material n = Glb.getObje().getMaterial(ms -> ms.get(id));
		if (n == null)
			return null;

		GuiCommon.onlyRegisterer(name(), n.getRegistererUserId());

		MaterialGui built = new MaterialGui(name(), id());
		built.buildDelete(n);
		return built.getGrid();
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public String name() {
		return Lang.MATERIAL_DELETE.toString();
	}

	@Override
	public String id() {
		return "materialDelete";
	}

}
