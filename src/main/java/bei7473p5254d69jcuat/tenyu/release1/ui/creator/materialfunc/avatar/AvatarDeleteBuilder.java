package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class AvatarDeleteBuilder extends GuiBuilder {
	public Node build(Long id) {
		if (id == null)
			return null;
		Avatar o = Glb.getObje().getAvatar(as -> as.get(id));
		if (o == null)
			return null;
		GuiCommon.onlyAdmin(name(), o.getAdministratorUserIdDelete());

		AvatarGui built = new AvatarGui(name(), id());
		built.buildDelete(o);

		built.buildExternalButton(
				new SubmitButtonFuncs(gui -> built.validateAtDelete(gui, o),
						gui -> true, null, null));
		return built.getGrid();
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public String name() {
		return Lang.AVATAR_DELETE.toString();
	}

	@Override
	public String id() {
		return "avatarDelete";
	}

}
