package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;

/**
 * ユーザー登録紹介依頼とユーザー登録メッセージの共通情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserRegistrationInfo {
	/**
	 * 登録される人
	 */
	protected User me;
	protected byte[] signMobileByOff;
	protected byte[] signOffByMobile;
	protected byte[] signOffByPc;
	protected byte[] signPcByOff;
	/**
	 * meへのPC鍵による電子署名
	 */
	protected byte[] signMeByPC;

	/**
	 * 紹介依頼者の元で呼ぶ
	 */
	public void signMeByPC() {
		try {
			signMeByPC = Glb.getConf().sign(getNominal(), KeyType.PC,
					getSignTarget());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	public byte[] getSignTarget() {
		try {
			return Glb.getUtil().toKryoBytesForCommunication(me);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public byte[] getSignMeByPC() {
		return signMeByPC;
	}

	public void setSignMeByPC(byte[] signMeByPC) {
		this.signMeByPC = signMeByPC;
	}

	public String getNominal() {
		return "signMeByPC";
	}

	/**
	 * @return
	 */
	public final boolean validate() {
		if (me == null || me.getRegistererUserId() == null || signMeByPC == null
				|| signMobileByOff == null || signOffByMobile == null
				|| signOffByPc == null || signPcByOff == null)
			return false;

		if (!Glb.getUtil().verify(getNominal(), signMeByPC, me.getPcPublicKey(),
				getSignTarget()))
			return false;

		if (!Conf.verifyKeys(me.getPcPublicKey(), me.getMobilePublicKey(),
				me.getOfflinePublicKey(), signPcByOff, signMobileByOff,
				signOffByPc, signOffByMobile))
			return false;

		return true;
	}

	public User getMe() {
		return me;
	}

	public void setMe(User me) {
		this.me = me;
	}

	public byte[] getMobileByOffSign() {
		return signMobileByOff;
	}

	public void setMobileByOffSign(byte[] mobileByOffSign) {
		this.signMobileByOff = mobileByOffSign;
	}

	public byte[] getOffByMobileSign() {
		return signOffByMobile;
	}

	public void setOffByMobileSign(byte[] offByMobileSign) {
		this.signOffByMobile = offByMobileSign;
	}

	public byte[] getOffByPcSign() {
		return signOffByPc;
	}

	public void setOffByPcSign(byte[] offByPcSign) {
		this.signOffByPc = offByPcSign;
	}

	public byte[] getPcByOffSign() {
		return signPcByOff;
	}

	public void setPcByOffSign(byte[] pcByOffSign) {
		this.signPcByOff = pcByOffSign;
	}
}
