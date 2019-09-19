package bei7473p5254d69jcuat.tenyu.release1.ui.admin.other;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class InventorProvementPasswordBuilder extends GuiBuilder {

	@Override
	public Node build() {
		int elapsed = 0;
		GridPane grid = grid2();
		elapsed += 1;

		Label l = new Label(name());
		TextField input = new TextField();

		grid.add(l, 0, elapsed);
		grid.add(input, 1, elapsed);
		elapsed += 1;

		GuiCommon.buildSubmitButton(grid, elapsed, name(), id(),
				gui -> input.getText() != null && input.getText().length() > 0,
				gui -> Glb.getConst().isInventorPassword(input.getText()), null,
				null);

		return grid;
	}

	@Override
	public String name() {
		return Lang.OTHER_INVENTORPROVEMENTPASSWORD.toString();
	}

	@Override
	public String id() {
		return "investorProvementPassword";
	}

}
