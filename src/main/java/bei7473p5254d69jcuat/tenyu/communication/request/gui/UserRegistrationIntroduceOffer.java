package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.P2PEdgeCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;

/**
 * ユーザー登録
 * 厳密にはUserRightMessageじゃない。Userになる前だから。
 * しかし多重継承できないから適切な構造が無い。
 * UserRightMessageとして何も機能しないようにしておく。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class UserRegistrationIntroduceOffer extends AbstractOffer
		implements P2PEdgeCommonKeyPackageContent {
	public static final boolean secure = false;
	private UserRegistrationInfo info;

	public UserRegistrationIntroduceOffer() {
	}

	public UserRegistrationIntroduceOffer(UserRegistrationInfo info) {
		this.info = info;
	}

	public static boolean send(UserRegistrationInfo info, P2PEdge e) {
		UserRegistrationIntroduceOffer req = new UserRegistrationIntroduceOffer(
				info);

		//送信
		Message m = Message.build(req).packaging(req.createPackage(e)).finish();
		return Response.success(Glb.getP2p().requestSync(m, e));
	}

	public UserRegistrationInfo getInfo() {
		return info;
	}

	@Override
	protected ResultCode receivedConcrete(Received validated) {
		ResultCode code;
		if (Glb.getMiddle().getUserRegistrationIntroduceList()
				.receive(validated.getMessage())) {
			code = ResultCode.SUCCESS;
		} else {
			code = ResultCode.FAIL;
		}
		return code;
	}

	@Override
	public String getName() {
		return Lang.USER_REGISTRATION_INTRODUCE.toString();
	}

	@Override
	protected boolean validateAbstractOfferConcrete(Message m) {
		if (!info.validate())
			return false;

		//自称している鍵の整合性を検証する。オフライン秘密鍵による署名があるか等
		Util u = Glb.getUtil();
		String nominal = Keys.getSignKeyNominal();
		boolean mobileByOff = u.verify(nominal, info.getMobileByOffSign(),
				info.getNewUser().getOfflinePublicKey(),
				info.getNewUser().getMobilePublicKey());
		if (!mobileByOff) {
			return false;
		}
		boolean offByMobile = u.verify(nominal, info.getOffByMobileSign(),
				info.getNewUser().getMobilePublicKey(),
				info.getNewUser().getOfflinePublicKey());
		if (!offByMobile) {
			return false;
		}
		boolean offByPc = u.verify(nominal, info.getOffByPcSign(),
				info.getNewUser().getPcPublicKey(),
				info.getNewUser().getOfflinePublicKey());
		if (!offByPc) {
			return false;
		}
		boolean pcByOff = u.verify(nominal, info.getPcByOffSign(),
				info.getNewUser().getOfflinePublicKey(),
				info.getNewUser().getPcPublicKey());
		if (!pcByOff) {
			return false;
		}

		//既に登録されている鍵は登録してはならないが、
		//未登録かはUserStoreでcreate前に判定される。
		//ここで判定すると、DBアクセスである上に二度手間

		//オブジェクトの内容に不備が無いか
		ValidationResult vr = new ValidationResult();
		info.getNewUser().validateAtOffer(vr);
		return info.getNewUser().getId() == null
				&& info.getNewUser().getInviter() != null && vr.isNoError();
	}

}
