package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialUpdateBatchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		GuiCommon.onlyUser(name());

		MaterialListGui builtList = new MaterialListGui(name(), id());
		builtList.buildUpdateBatch();

		//最終送信ボタン
		builtList.buildExternalButton(new SubmitButtonFuncs(gui -> {
			try {
				return builtList.validate(gui);
			} catch (Exception e) {
				Glb.debug(e);
				gui.message(Lang.EXCEPTION.toString());
				return false;
			}
		}, gui -> {
			try {
				if (!MaterialListGui.moveFiles(builtList, gui)) {
					return false;
				}
				//TODO リクエスト作成と送信
				return true;
			} catch (Exception e) {
				Glb.debug(e);
				gui.message(Lang.EXCEPTION.toString());
				return false;
			}
		}, gui -> {
			builtList.clear();
		}, gui -> builtList.getTmpObjs().clear()));

		return builtList.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIAL_UPDATE_BATCH.toString();
	}

	@Override
	public String id() {
		return "materialUpdateBatch";
	}

}
