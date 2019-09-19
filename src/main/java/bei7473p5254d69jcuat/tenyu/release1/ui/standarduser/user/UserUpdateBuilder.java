package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class UserUpdateBuilder extends GuiBuilder {
	public Node build(Long userId) {
		if (userId == null)
			return null;
		GuiCommon.onlyMe(name(), userId);

		User exist = Glb.getObje().getUser(us->us.get(userId));
		if (exist == null)
			return null;
		UserGui gui = new UserGui(name(), id());
		return gui.buildUpdate(exist);

	}

	@Override
	public Node build() {
		return build(Glb.getMiddle().getMyUserId());
	}

	@Override
	public String name() {
		return Lang.USER_UPDATE.toString();
	}

	@Override
	public String id() {
		return "userUpdate";
	}

}
