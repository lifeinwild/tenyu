package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.user;

import java.security.*;
import java.security.spec.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

/**
 * {@link User}が鍵を更新した場合、古い鍵を一定件数まで保持しておく。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class KeysLog implements ValidatableI {
	/**
	 * 古い鍵の最大保持件数
	 */
	public static final int keysLogMax = 10;

	public static int getKeyslogmax() {
		return keysLogMax;
	}

	private List<ByteArrayWrapper> oldMobileKeys = new ArrayList<>();
	private List<ByteArrayWrapper> oldOffKeys = new ArrayList<>();
	private List<ByteArrayWrapper> oldPcKeys = new ArrayList<>();

	/**
	 * @param type	公開鍵の種類
	 * @return	記録されている公開鍵の数
	 */
	public int size(KeyType type) {
		switch (type) {
		case PC:
			return oldPcKeys.size();
		case MOBILE:
			return oldPcKeys.size();
		case OFFLINE:
			return oldPcKeys.size();
		default:
			throw new IllegalArgumentException();
		}
	}

	public List<PublicKey> getOldKeysPublicKey(KeyType type)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		List<PublicKey> r = new ArrayList<>();
		for (ByteArrayWrapper e : getOldKeys(type)) {
			r.add(Glb.getUtil().getPub(e.getByteArray()));
		}
		return r;
	}

	public List<ByteArrayWrapper> getOldKeys(KeyType type) {
		switch (type) {
		case PC:
			return oldPcKeys;
		case MOBILE:
			return oldPcKeys;
		case OFFLINE:
			return oldPcKeys;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * 古い鍵を追加する
	 * @param keyB	古い鍵のバイナリ表現
	 * @param type	古い鍵の種別
	 * @return	追加されたか
	 */
	public boolean add(byte[] keyB, KeyType type) {
		List<ByteArrayWrapper> olds = null;
		switch (type) {
		case MOBILE:
			olds = oldMobileKeys;
			break;
		case OFFLINE:
			olds = oldOffKeys;
			break;
		case PC:
			olds = oldPcKeys;
			break;
		default:
		}
		if (olds == null) {
			throw new IllegalStateException();
		}
		if (olds.size() >= keysLogMax) {
			olds.remove(3);//最初の3件は永久保存
		}
		ByteArrayWrapper w = new ByteArrayWrapper(keyB);
		return olds.add(w);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeysLog other = (KeysLog) obj;
		if (oldMobileKeys == null) {
			if (other.oldMobileKeys != null)
				return false;
		} else if (!oldMobileKeys.equals(other.oldMobileKeys))
			return false;
		if (oldOffKeys == null) {
			if (other.oldOffKeys != null)
				return false;
		} else if (!oldOffKeys.equals(other.oldOffKeys))
			return false;
		if (oldPcKeys == null) {
			if (other.oldPcKeys != null)
				return false;
		} else if (!oldPcKeys.equals(other.oldPcKeys))
			return false;
		return true;
	}

	public List<ByteArrayWrapper> getOldMobileKeys() {
		return oldMobileKeys;
	}

	public List<ByteArrayWrapper> getOldOffKeys() {
		return oldOffKeys;
	}

	public List<ByteArrayWrapper> getOldPcKeys() {
		return oldPcKeys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((oldMobileKeys == null) ? 0 : oldMobileKeys.hashCode());
		result = prime * result
				+ ((oldOffKeys == null) ? 0 : oldOffKeys.hashCode());
		result = prime * result
				+ ((oldPcKeys == null) ? 0 : oldPcKeys.hashCode());
		return result;
	}

	public void setOldMobileKeys(List<ByteArrayWrapper> oldMobileKeys) {
		this.oldMobileKeys = oldMobileKeys;
	}

	public void setOldOffKeys(List<ByteArrayWrapper> oldOffKeys) {
		this.oldOffKeys = oldOffKeys;
	}

	public void setOldPcKeys(List<ByteArrayWrapper> oldPcKeys) {
		this.oldPcKeys = oldPcKeys;
	}

	@Override
	public String toString() {
		return "KeysLog [oldMobileKeys=" + oldMobileKeys + ", oldPcKeys="
				+ oldPcKeys + ", oldOffKeys=" + oldOffKeys + "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		Util u = Glb.getUtil();
		//secureまたはnormalの鍵長
		int[] rsaKeyLens = new int[] { User.getRsaKeySizeByteBySecure(false),
				User.getRsaKeySizeByteBySecure(true) };

		if (oldMobileKeys == null) {
			r.add(Lang.KEYS_LOG, Lang.USER_MOBILEKEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (oldMobileKeys.size() > keysLogMax) {
				r.add(Lang.KEYS_LOG, Lang.USER_MOBILEKEY, Lang.ERROR_TOO_MANY,
						"size=" + oldMobileKeys.size());
				b = false;
			} else {
				for (ByteArrayWrapper w : oldMobileKeys) {
					if (!u.isValidRSAKey(w.getByteArray(), rsaKeyLens)) {
						r.add(Lang.KEYS_LOG, Lang.USER_MOBILEKEY,
								Lang.ERROR_INVALID, "oldMobileKey=" + w);
						b = false;
						break;
					}
				}
			}
		}

		if (oldOffKeys == null) {
			r.add(Lang.KEYS_LOG, Lang.USER_OFFKEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (oldOffKeys.size() > keysLogMax) {
				r.add(Lang.KEYS_LOG, Lang.USER_OFFKEY, Lang.ERROR_TOO_MANY,
						"size=" + oldOffKeys.size());
				b = false;
			} else {
				for (ByteArrayWrapper w : oldOffKeys) {
					if (!u.isValidRSAKey(w.getByteArray(), rsaKeyLens)) {
						r.add(Lang.KEYS_LOG, Lang.USER_OFFKEY,
								Lang.ERROR_INVALID, "oldOffKey=" + w);
						b = false;
						break;
					}
				}
			}
		}

		if (oldPcKeys == null) {
			r.add(Lang.KEYS_LOG, Lang.USER_PCKEY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (oldPcKeys.size() > keysLogMax) {
				r.add(Lang.KEYS_LOG, Lang.USER_PCKEY, Lang.ERROR_TOO_MANY,
						"size=" + oldPcKeys.size());
				b = false;
			} else {
				for (ByteArrayWrapper w : oldPcKeys) {
					if (!u.isValidRSAKey(w.getByteArray(), rsaKeyLens)) {
						r.add(Lang.KEYS_LOG, Lang.USER_PCKEY,
								Lang.ERROR_INVALID, "oldPcKey=" + w);
						b = false;
						break;
					}
				}
			}
		}

		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

}