package bei7473p5254d69jcuat.tenyu.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * このオブジェクトは署名対象元情報(signTargetOrig)をメンバー変数に保持しないので、
 * 署名時または検証時に外部から与える必要がある。
 * この仕様は１つのデータに対して大勢が署名する場合効率的。
 *
 * {@link #setSignTargetOrig(byte[])}を呼び出さないといくつかの
 * インターフェースは動作しない。
 * ただし{@link #sign(byte[])}と{@link #searchAndVerify(byte[])}は動作する。
 *
 * {@link NominalSignatureI}の１実装。
 * 非スレッドセーフ。
 * 署名や検証の前に{@link #init(byte[])}を呼び出す必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class NominalSignature implements NominalSignatureI {
	/**
	 * 署名に使用された鍵の種別 {@link KeyType}
	 */
	private KeyType keyType;
	/**
	 * 署名の名目
	 */
	private String nominal;
	/**
	 * 電子署名
	 */
	private byte[] sign;

	/**
	 * 署名日時
	 */
	private long signDate;

	/**
	 * 署名者
	 */
	private Long signerUserId;

	private transient byte[] signTargetOrig;

	public NominalSignature() {
	}

	public NominalSignature(KeyType keyType, String nominal,
			Long signerUserId) {
		this.keyType = keyType;
		this.nominal = nominal;
		this.signerUserId = signerUserId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NominalSignature other = (NominalSignature) obj;
		if (keyType != other.keyType)
			return false;
		if (nominal == null) {
			if (other.nominal != null)
				return false;
		} else if (!nominal.equals(other.nominal))
			return false;
		if (!Arrays.equals(sign, other.sign))
			return false;
		if (signDate != other.signDate)
			return false;
		if (signerUserId == null) {
			if (other.signerUserId != null)
				return false;
		} else if (!signerUserId.equals(other.signerUserId))
			return false;
		return true;
	}

	@Override
	public KeyType getKeyType() {
		return keyType;
	}

	@Override
	public String getNominal() {
		return nominal;
	}

	@Override
	public byte[] getSign() {
		return sign;
	}

	@Override
	public long getSignDate() {
		return signDate;
	}

	@Override
	public Long getSignerUserId() {
		return signerUserId;
	}

	/*
		public PublicKey getPub() {
			try {
				User u = Glb.getObje().getUser(us -> us.get(signerUserId));
				return Glb.getUtil().getPub(u.getPubKey(keyType));
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		}
	*/
	/**
	 * @param signTargetOrig	署名対象の元となるデータ
	 * @return	このオブジェクトが持つ署名データが妥当か
	 */
	/*
	public boolean validateSign(byte[] signTargetOrig) {
		try {
			byte[] signTarget = getSignTarget(signDate, signTargetOrig);
			return Glb.getUtil().verify(getNominal(), getSign(), getPub(),
					signTarget);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}
	*/

	@Override
	public byte[] getSignTargetOrig() {
		if (signTargetOrig == null)
			throw new IllegalStateException("set signTargetOrig before use interfaces of "
					+ getClass().getSimpleName());
		return signTargetOrig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyType == null) ? 0 : keyType.hashCode());
		result = prime * result + ((nominal == null) ? 0 : nominal.hashCode());
		result = prime * result + Arrays.hashCode(sign);
		result = prime * result + (int) (signDate ^ (signDate >>> 32));
		result = prime * result
				+ ((signerUserId == null) ? 0 : signerUserId.hashCode());
		return result;
	}

	/**
	 * transientメンバーのセット
	 *
	 * @param signTargetOrig
	 */
	public void init(byte[] signTargetOrig) {
		setSignTargetOrig(signTargetOrig);
	}

	public boolean searchAndVerify(byte[] signTargetOrig) {
		setSignTargetOrig(signTargetOrig);
		boolean r = searchAndVerify();
		return r;
	}

	@Override
	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}

	@Override
	public void setNominal(String nominal) {
		this.nominal = nominal;
	}

	@Override
	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	@Override
	public void setSignDate(long signDate) {
		this.signDate = signDate;
	}

	@Override
	public void setSignerUserId(Long signerUserId) {
		this.signerUserId = signerUserId;
	}

	public void setSignTargetOrig(byte[] signTargetOrig) {
		this.signTargetOrig = signTargetOrig;
	}

	@Override
	public String toString() {
		return "NominalSignature [signerUserId=" + signerUserId + ", keyType="
				+ keyType + ", signDate=" + signDate + ", nominal=" + nominal
				+ ", sign=" + Arrays.toString(sign) + "]";
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
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (signerUserId == null) {
			r.add(Lang.NOMINAL_SIGNATURE, Lang.SIGNERUSERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandard(signerUserId)) {
				r.add(Lang.NOMINAL_SIGNATURE, Lang.SIGNERUSERID,
						Lang.ERROR_INVALID, "signerUserId=" + signerUserId);
				b = false;
			}
		}
		if (keyType == null) {
			r.add(Lang.NOMINAL_SIGNATURE, Lang.KEYTYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (signDate <= 0) {
			r.add(Lang.NOMINAL_SIGNATURE, Lang.SIGNDATE, Lang.ERROR_INVALID);
			b = false;
		}
		if (sign == null) {
			r.add(Lang.NOMINAL_SIGNATURE, Lang.UPLOADER_SIGN, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (sign.length > Glb.getConst().getSignMaxRough()) {
				r.add(Lang.NOMINAL_SIGNATURE, Lang.UPLOADER_SIGN, Lang.ERROR_TOO_LONG,
						"sign.length=" + sign.length);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(signerUserId) == null) {
			r.add(Lang.NOMINAL_SIGNATURE, Lang.SIGNERUSERID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"signerUserId=" + signerUserId);
			b = false;
		}

		return b;
	}
}
