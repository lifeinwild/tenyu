package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class StyleSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		StyleGui searchGui = new StyleGui(name(), id());
		searchGui.buildSearch(null);
		searchGui.buildExternalButton(new SubmitButtonFuncs(
				Lang.STYLE_UPDATE.toString(), id(), (a) -> {
					//FXスレッドで動作させるため検証処理として登録
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						StyleUpdateBuilder builder = new StyleUpdateBuilder();
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
		return Lang.STYLE_SEARCH.toString();
	}

	@Override
	public String id() {
		return "styleSearch";
	}

}
