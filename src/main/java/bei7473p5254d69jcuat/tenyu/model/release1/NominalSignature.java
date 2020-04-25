package bei7473p5254d69jcuat.tenyu.model.release1;

import java.nio.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 任意の名目で使用される署名クラス。
 * 名目と日時情報は文脈によらず必須とみなした。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class NominalSignature implements Storable {
	/**
	 * 署名者
	 */
	private Long signerUserId;
	/**
	 * 署名対象。主にハッシュ値
	 */
	private byte[] signTarget;
	/**
	 * 署名日時
	 */
	private long signDate;

	/**
	 * 署名の名目
	 */
	private String nominal;

	/**
	 * 電子署名
	 */
	private byte[] sign;

	/**
	 * @param nominal	署名名目
	 * @param signTargetOrig	署名対象データの元となるデータ。これに日時が加えられる。
	 * @return	署名されたか
	 */
	public boolean sign(String nominal, byte[] signTargetOrig) {
		try {
			//日時情報を必ずつける事にした
			long date = System.currentTimeMillis();
			ByteBuffer buffer = ByteBuffer
					.allocate(Long.BYTES + signTargetOrig.length);
			buffer.put(signTargetOrig);
			buffer.putLong(date);
			byte[] signTarget = Glb.getUtil().hashSecure(buffer.array());

			//署名
			byte[] sign = Glb.getConf().getKeys().sign(nominal, signTarget);
			if (sign == null)
				return false;

			//署名に成功した場合その署名に関連した情報にこのオブジェクトを更新する。
			this.sign = sign;
			this.signerUserId = Glb.getMiddle().getMyUserId();
			this.signTarget = signTarget;
			this.nominal = nominal;
			this.signDate = date;
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (signerUserId == null) {
			r.add(Lang.NOMINAL_SIGNATURE_SIGNERUSERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandard(signerUserId)) {
				r.add(Lang.NOMINAL_SIGNATURE_SIGNERUSERID, Lang.ERROR_INVALID,
						"signerUserId=" + signerUserId);
				b = false;
			}
		}
		if (signTarget == null) {
			r.add(Lang.NOMINAL_SIGNATURE_SIGNTARGET, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (signTarget.length != Glb.getConst().getHashSize()) {
				r.add(Lang.NOMINAL_SIGNATURE_SIGNTARGET, Lang.ERROR_INVALID,
						"signTarget.length=" + signTarget.length);
				b = false;
			}
		}
		if (signDate <= 0) {
			r.add(Lang.NOMINAL_SIGNATURE_SIGNDATE, Lang.ERROR_INVALID);
			b = false;
		}
		if (sign == null) {
			r.add(Lang.NOMINAL_SIGNATURE_SIGN, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (sign.length > Glb.getConst().getSignMaxRough()) {
				r.add(Lang.NOMINAL_SIGNATURE_SIGN, Lang.ERROR_TOO_LONG,
						"sign.length=" + sign.length);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(signerUserId) == null) {
			r.add(Lang.NOMINAL_SIGNATURE_SIGNERUSERID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"signerUserId=" + signerUserId);
			b = false;
		}

		return b;
	}

	public Long getSignerUserId() {
		return signerUserId;
	}

	public void setSignerUserId(Long signerUserId) {
		this.signerUserId = signerUserId;
	}

	public long getSignDate() {
		return signDate;
	}

	public void setSignDate(long signDate) {
		this.signDate = signDate;
	}

	public String getNominal() {
		return nominal;
	}

	public void setNominal(String nominal) {
		this.nominal = nominal;
	}

	public byte[] getSignTarget() {
		return signTarget;
	}

	public void setSignTarget(byte[] signTarget) {
		this.signTarget = signTarget;
	}

	public byte[] getSign() {
		return sign;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

}
