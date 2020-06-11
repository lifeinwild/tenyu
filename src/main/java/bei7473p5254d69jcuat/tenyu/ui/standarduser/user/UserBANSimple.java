package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.scene.*;

public class UserBANSimple extends GuiBuilder {
	public Node build(Long socialityId) {
		if (socialityId == null)
			return null;
		Sociality so = SocialityStore.getSimple(socialityId);
		if (so == null)
			return null;
		GuiCommon.onlyAdmin(name(), so.getAdministratorUserIdSimpleBAN());

		SocialityGui built = new SocialityGui(name(), id());
		return built.buildSimpleBan(so);
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public String name() {
		return Lang.USER_BAN_SIMPLE.toString();
	}

	@Override
	public String id() {
		return "userBanSimple";
	}

}
