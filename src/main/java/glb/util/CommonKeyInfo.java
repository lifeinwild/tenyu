package glb.util;

import java.util.*;

import javax.crypto.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import jetbrains.exodus.env.*;

public class CommonKeyInfo implements Storable {

	/**
	 * 自分からこのノードへ通知した共通鍵。受信時に使用
	 */
	protected byte[] commonKey;

	/**
	 * 自分からこのノードへのデータを共通鍵で複合化
	 * @param encrypted	暗号文
	 * @return	平文
	 */
	public byte[] decrypt(byte[] encrypted, byte[] iv) {
		return Glb.getUtil().commonKey(encrypted, Cipher.DECRYPT_MODE,
				commonKey, iv);
	}

	public static byte[] createConfirmation() {
		byte[] co = new byte[Glb.getConst().getCommonKeyConfirmationSize()];
		Glb.getRnd().nextBytes(co);
		return co;
	}

	public static CommonKeyInfo build() {
		CommonKeyInfo cki = new CommonKeyInfo();

		byte[] ck = new byte[Glb.getConst().getCommonKeySizeForCommunication()];
		Glb.getRnd().nextBytes(ck);
		cki.setCommonKey(ck);
		return cki;
	}

	/**
	 * 自分からこのノードへのデータを共通鍵で暗号化
	 * @param plain	平文
	 * @return	暗号文
	 */
	public byte[] encrypt(byte[] plain, byte[] iv) {
		return Glb.getUtil().commonKey(plain, Cipher.ENCRYPT_MODE, commonKey,
				iv);
	}

	public byte[] getCommonKey() {
		return commonKey;
	}

	public void setCommonKey(byte[] commonKey) {
		this.commonKey = commonKey;
	}

	public void reset() {
		commonKey = null;
	}

	public CommonKeyInfo clone() {
		CommonKeyInfo clone = new CommonKeyInfo();
		clone.setCommonKey(commonKey);
		return clone;
	}

	/**
	 * @param cki			共通鍵
	 * @param confirmation	鍵交換確認情報
	 * @return				ckiとconfirmationがシリアライズされたバイト配列
	 */
	public static byte[] serialize(CommonKeyInfo cki, byte[] confirmation) {
		if (confirmation == null || cki.getCommonKey() == null)
			return null;

		int keySize = Glb.getConst().getCommonKeySizeForCommunication();
		int coSize = Glb.getConst().getCommonKeyConfirmationSize();

		if (keySize != cki.getCommonKey().length
				|| coSize != confirmation.length)
			return null;

		int total = keySize + coSize;

		byte[] serialized = new byte[total];
		System.arraycopy(cki.getCommonKey(), 0, serialized, 0, keySize);
		System.arraycopy(confirmation, 0, serialized, keySize, coSize);

		return serialized;
	}

	/**
	 * @param serialized	CKI+confirmation
	 * @return				共通鍵
	 */
	public static CommonKeyInfo deserialize(byte[] serialized) {
		int keySize = Glb.getConst().getCommonKeySizeForCommunication();

		CommonKeyInfo cki = new CommonKeyInfo();
		byte[] key = new byte[keySize];
		System.arraycopy(serialized, 0, key, 0, keySize);

		cki.setCommonKey(key);
		return cki;
	}

	/**
	 * @param serialized	CKI+confirmation
	 * @return				confirmation
	 */
	public static byte[] deserializeConfirmation(byte[] serialized) {
		int keySize = Glb.getConst().getCommonKeySizeForCommunication();
		int coSize = Glb.getConst().getCommonKeyConfirmationSize();
		byte[] co = new byte[coSize];

		System.arraycopy(serialized, keySize, co, 0, coSize);

		return co;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(commonKey);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonKeyInfo other = (CommonKeyInfo) obj;
		if (!Arrays.equals(commonKey, other.commonKey))
			return false;
		return true;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (commonKey == null) {
			r.add(Lang.COMMONKEYINFO_COMMONKEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (commonKey.length != Glb.getConst()
					.getCommonKeySizeForCommunication()) {
				r.add(Lang.COMMONKEYINFO_COMMONKEY, Lang.ERROR_INVALID,
						"size=" + commonKey.length);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

}
