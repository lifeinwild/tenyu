package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import glb.*;

/**
 * ユーザー登録紹介依頼とユーザー登録メッセージの共通情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserRegistrationInfo {
	/**
	 * 登録される人
	 */
	protected User newUser;
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
			signMeByPC = Glb.getConf().getKeys().sign(getNominal(), KeyType.PC,
					getSignTarget());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	public byte[] getSignTarget() {
		try {
			return Glb.getUtil().toKryoBytesForCommunication(newUser);
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
		if (newUser == null || newUser.getRegistererUserId() == null || signMeByPC == null
				|| signMobileByOff == null || signOffByMobile == null
				|| signOffByPc == null || signPcByOff == null)
			return false;

		if (!Glb.getUtil().verify(getNominal(), signMeByPC, newUser.getPcPublicKey(),
				getSignTarget()))
			return false;

		if (!Keys.verifyKeys(newUser.getPcPublicKey(), newUser.getMobilePublicKey(),
				newUser.getOfflinePublicKey(), signPcByOff, signMobileByOff,
				signOffByPc, signOffByMobile))
			return false;

		return true;
	}

	public User getNewUser() {
		return newUser;
	}

	public void setNewUser(User newUser) {
		this.newUser = newUser;
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
