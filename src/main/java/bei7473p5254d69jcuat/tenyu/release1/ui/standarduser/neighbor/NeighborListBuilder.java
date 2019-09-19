package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class NeighborListBuilder extends GuiBuilder {
	@Override
	public Node build() {
		NeighborListGui gui = new NeighborListGui(name(), id());
		return gui.buildDelete();
	}

	@Override
	public String name() {
		return Lang.NEIGHBOR_LIST.toString();
	}

	@Override
	public String id() {
		return "neighborList";
	}

}
