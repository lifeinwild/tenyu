package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.layout.*;

public class NeighborManualAddBuilder extends GuiBuilder {

	@Override
	public Pane build() {
		ISAGui gui = new ISAGui(name(), id());
		return gui.buildCreate();
	}

	@Override
	public String name() {
		return Lang.NEIGHBOR_MANUAL_ADD.toString();
	}

	@Override
	public String id() {
		return "neighborManualAdd";
	}

}
