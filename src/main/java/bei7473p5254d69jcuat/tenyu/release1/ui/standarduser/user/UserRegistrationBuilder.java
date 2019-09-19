package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;

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
			info.setMe(scr.getModelCache());

			//作成日時を設定
			info.getMe().setCreateDate(System.currentTimeMillis());

			info.setMobileByOffSign(cf.getMyMobileKeySignByOffB());
			info.setPcByOffSign(cf.getMyPcKeySignByOffB());
			info.setOffByMobileSign(cf.getMyOffKeySignByMobB());
			info.setOffByPcSign(cf.getMyOffKeySignByPcB());

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
