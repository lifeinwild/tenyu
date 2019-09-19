package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class UserRegistrationIntroduceBuilder extends GuiBuilder {

	@Override
	public Node build() {
		UserRegistrationGui built = new UserRegistrationGui(name(), id());
		built.buildCreate();

		SubmitButtonGui okButton = built.buildExternalButton(
				Lang.USER_REGISTRATION_INTRODUCE_OK.toString(), id() + "Ok",
				gui -> {
					if (built.getOfferTableSelect().getSelectedItem() == null) {
						//登録申請が未選択の場合
						gui.message(Lang.USER_REGISTRATION_INTRODUCE_OFFER_EMPTY
								.toString());
						return false;
					}
					UserRegistrationIntroduceOfferTableItem offerGui = built
							.getOfferTableSelect().getSelectedItem();
					if (offerGui == null || offerGui.getSrc() == null
							|| offerGui.getEdge() == null) {
						gui.message(Lang.CHOICE_ONE.toString());
						return false;
					}
					Content c = offerGui.getSrc().getContent();
					if (!(c instanceof UserRegistrationIntroduceOffer))
						return false;
					UserRegistrationIntroduceOffer offer = (UserRegistrationIntroduceOffer) c;
					offer.getInfo().signMeByPC();
					if (!offer.getInfo().validate())
						return false;

					return true;
				}, gui -> {
					//紹介処理
					UserRegistrationIntroduceOfferTableItem offerGui = built
							.getOfferTableSelect().getSelectedItem();
					P2PEdge e = offerGui.getEdge();
					Content c = offerGui.getSrc().getContent();
					UserRegistrationIntroduceOffer offer = (UserRegistrationIntroduceOffer) c;

					//受付サーバに送信
					Message resM = Glb.getP2p().requestUserRightMessage(to -> {
						UserRegistration right = new UserRegistration();
						right.setInfo(offer.getInfo());
						return right;
					});
					if (Response.fail(resM))
						return false;

					//紹介一覧から削除
					Glb.getMiddle().getUserRegistrationIntroduceList()
							.remove(offerGui.getSrc());
					//被紹介者に紹介した事を通知。この処理は失敗しても致命的ではないので成功通知を表示する
					Message resM2 = Glb.getP2p().requestSyncGuiMessage(
							Lang.USER_REGISTRATION_INTRODUCE_OFFER.toString(),
							Lang.USER_REGISTRATION_INTRODUCE_OFFER_ACCEPTED
									.toString(),
							e);
					return true;
				}, gui -> built.getDetailUser().clear(), null);

		SubmitButtonGui cancelButton = built.buildExternalButton(
				Lang.USER_REGISTRATION_INTRODUCE_CANCEL.toString(),
				id() + "Cancel", gui -> {
					if (built.getOfferTableSelect().getSelectedItem() == null) {
						gui.message(Lang.USER_REGISTRATION_INTRODUCE_OFFER_EMPTY
								.toString());
						return false;
					}
					return true;
				}, gui -> {
					//最近選択されたアイテムを取得
					UserRegistrationIntroduceOfferTableItem offerGui = built
							.getOfferTableSelect().getSelectedItem();
					if (offerGui == null || offerGui.getSrc() == null) {
						gui.message(Lang.CHOICE_ONE.toString());
						return false;
					}

					//削除
					Glb.getMiddle().getUserRegistrationIntroduceList()
							.remove(offerGui.getSrc());

					offerGui.getSrc().getContent();
					P2PEdge e = offerGui.getEdge();
					if (e == null)
						return false;

					Glb.getP2p().requestAsyncGuiMessage(
							Lang.USER_REGISTRATION_INTRODUCE_OFFER.toString(),
							Lang.USER_REGISTRATION_INTRODUCE_OFFER_DENIED
									.toString(),
							e);
					return true;
				}, null, null);
		return built.getGrid();
	}

	@Override
	public String id() {
		return "userRegistrationIntroduce";
	}

	@Override
	public String name() {
		return Lang.USER_REGISTRATION_INTRODUCE.toString();
	}

}
