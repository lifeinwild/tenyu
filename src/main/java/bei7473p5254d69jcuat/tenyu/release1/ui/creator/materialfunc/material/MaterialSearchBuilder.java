package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		MaterialGui searchGui = new MaterialGui(name(), id());
		searchGui.buildSearch(null);

		//修正ボタン
		searchGui.buildExternalButton(new SubmitButtonFuncs(
				Lang.MATERIAL_UPDATE.toString(), id(), (a) -> {
					//FXスレッドで動作させるため検証処理として登録
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						MaterialUpdateBuilder builder = new MaterialUpdateBuilder();
						Glb.getGui().createTab(builder.build(id),
								builder.name());
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null));
		return searchGui.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIAL_SEARCH.toString();
	}

	@Override
	public String id() {
		return "materialSearch";
	}

}
