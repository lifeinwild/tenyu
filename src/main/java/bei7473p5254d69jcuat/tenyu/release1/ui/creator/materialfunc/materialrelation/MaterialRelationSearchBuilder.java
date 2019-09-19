package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class MaterialRelationSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		MaterialRelationGui searchGui = new MaterialRelationGui(name(), id());
		searchGui.buildSearch(null);

		//修正ボタン
		searchGui.buildExternalButton(new SubmitButtonFuncs(
				Lang.MATERIALRELATION_UPDATE.toString(), id(), (a) -> {
					//FXスレッドで動作させるため検証処理として登録
					try {
						String idStr = searchGui.getIdInput().getText();
						Long id = Long.valueOf(idStr);
						MaterialRelationUpdateBuilder builder = new MaterialRelationUpdateBuilder();
						Glb.getGui().createTab(builder.build(id),
								builder.name());
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null));

		/*
		CommonBuilder.buildSubmitButton(grid, elapsed,
				Lang.MATERIALRELATION_DELETE.toString(), id(), (a) -> {
					try {
						String idStr = searchGui.getIdInput().getText();
						Long id = Long.valueOf(idStr);
						MaterialRelationDeleteBuilder builder = new MaterialRelationDeleteBuilder();
						Glb.getGui().createTab(builder.build(id),
								builder.name());
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null);
		elapsed += 1;
		*/
		return searchGui.getGrid();
	}

	@Override
	public String name() {
		return Lang.MATERIALRELATION_SEARCH.toString();
	}

	@Override
	public String id() {
		return "materialRelationSearch";
	}

}
