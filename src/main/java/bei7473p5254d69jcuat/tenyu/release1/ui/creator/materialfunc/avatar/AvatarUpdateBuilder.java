package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class AvatarUpdateBuilder extends GuiBuilder {
	public Node build(Long id) {
		if (id == null)
			return null;

		Avatar exist = Glb.getObje().getAvatar(as -> as.get(id));
		if (exist == null)
			return null;

		AvatarGui built = new AvatarGui(name(), id());
		built.buildUpdate(exist);

		//登録
		built.buildExternalButton(
				new SubmitButtonFuncs(gui -> built.validateAtUpdate(gui, exist),
						gui -> true, null, null));

		return built.getGrid();
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public String name() {
		return Lang.AVATAR_UPDATE.toString();
	}

	@Override
	public String id() {
		return "avatarUpdate";
	}

}
