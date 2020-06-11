package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;

public class UserRegistrationBuilder extends GuiBuilder {

	@Override
	public javafx.scene.Node build() {
		UserGui scr = new UserGui(name(), id());
		scr.buildCreate();
		final Conf cf = Glb.getConf();
		//登録
		scr.buildExternalButton(new SubmitButtonFuncs(gui -> {
			return scr.validateAtCreate(gui);
		}, gui -> {
			//登録申請を作る
			UserRegistrationInfo info = new UserRegistrationInfo();
			info.setNewUser(scr.getModelCache());

			//作成日時を設定
			info.getNewUser().setSubmitDate(System.currentTimeMillis());

			info.setMobileByOffSign(cf.getKeys().getMyMobileKeySignByOffB());
			info.setPcByOffSign(cf.getKeys().getMyPcKeySignByOffB());
			info.setOffByMobileSign(cf.getKeys().getMyOffKeySignByMobB());
			info.setOffByPcSign(cf.getKeys().getMyOffKeySignByPcB());

			return UserRegistrationIntroduceOffer.send(info, scr.getInviter());
		}, gui -> scr.clear(), null));
		return scr.getGrid();
	}

	@Override
	public String id() {
		return "userRegistration";
	}

	@Override
	public String name() {
		return Lang.USER_REGISTRATION.toString();
	}

}
