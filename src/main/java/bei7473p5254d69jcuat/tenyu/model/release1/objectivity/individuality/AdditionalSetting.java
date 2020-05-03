package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality;

import java.nio.charset.*;
import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 追加的な設定。
 * バージョンアップ無しで対応できる事を増やす。
 * しかしその追加された設定値を利用するコードが無いので、現状このアイデアは無意味。
 *
 * 例えばUser本人または全体運営者が手動で情報を追加していって、
 * その情報を外部アプリが利用するなどの仕組みを考える必要がある。
 *
 * もう一つ、極一部のインスタンスだけが必要とする情報もあり、
 * この方法で対応するとDBのサイズを小さくできる可能性が高い。
 */
public class AdditionalSetting implements StorableI {
	public static final int maxKeyLen = 100;
	public static final int maxSize = 20;
	public static final int maxValLen = 100;

	/**
	 * キーは利用側のクラス名を想定
	 * バリューは任意
	 */
	private Map<String, byte[]> setting = new HashMap<>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdditionalSetting other = (AdditionalSetting) obj;
		if (setting == null) {
			if (other.setting != null)
				return false;
		} else if (!setting.equals(other.setting))
			return false;
		return true;
	}

	private static final Charset charset = Charset.forName("UTF-8");

	public String addSetting(String key, String val) {
		byte[] old = setting.put(key, val.getBytes(charset));
		if (old == null)
			return null;
		return new String(old, charset);
	}

	public String getSetting(String key) {
		byte[] val = setting.get(key);
		if (val == null)
			return null;
		return new String(val, charset);
	}

	public byte[] addSetting(String key, byte[] val) {
		return setting.put(key, val);
	}

	public Map<String, byte[]> getSetting() {
		return Collections.unmodifiableMap(setting);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((setting == null) ? 0 : setting.hashCode());
		return result;
	}

	public void setSetting(Map<String, byte[]> setting) {
		this.setting = setting;
	}

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (setting == null) {
			vr.add(Lang.USER_ADDITIONALSETTING, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (setting.size() > maxSize) {
				vr.add(Lang.USER_ADDITIONALSETTING, Lang.ERROR_TOO_BIG);
				b = false;
			} else {
				for (Entry<String, byte[]> e : setting.entrySet()) {
					int keyLen = e.getKey().length();
					if (keyLen > maxKeyLen) {
						vr.add(Lang.USER_ADDITIONALSETTING_KEY,
								Lang.ERROR_TOO_LONG,
								e.getKey().substring(0, maxKeyLen));
						b = false;
						break;
					}
					if (e.getValue().length > maxValLen) {
						vr.add(Lang.USER_ADDITIONALSETTING_VALUE,
								Lang.ERROR_TOO_LONG);
						b = false;
						break;
					}
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

}
