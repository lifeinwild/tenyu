package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * String, Stringの任意のKeyValuePair
 * 任意の統一値を扱えるようにするための機能
 * この機能は特別な権限が与えられたユーザーに限定されるべき。
 * キーの接頭辞はユーザーIDまたはglobal。
 * globalは権限を持つどのユーザーでも書き込める。
 *
 * このアイデアは、汎用性を考慮して一応置いたという感じで、
 * 具体的な用途を考えていない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FreeKVPair extends ObjectivityObject implements FreeKVPairDBI {
	private static final String delimiter = ".";
	private static final String global = "global";

	public static final int keyMax = 1000;

	public static final int valueMax = 1000 * 1000;

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(FreeKVPair.class.getSimpleName()).getAdminUserIds());
	}

	public static String getDelimiter() {
		return delimiter;
	}

	private String key;

	private String value;

	private String createKey(String userId, String keyContent) {
		if (keyContent.indexOf(delimiter) != -1) {
			throw new IllegalArgumentException();
		}
		return userId + delimiter + keyContent;
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdUpdate();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		try {
			String p = getPrefix(key);
			if (p == null) {
				r.addAll(Glb.getObje()
						.getRole(rs -> rs
								.getByName(FreeKVPair.class.getSimpleName())
								.getAdminUserIds()));
			} else {
				Long id = Long.valueOf(p);
				r.add(id);
			}
		} catch (Exception e) {
			Glb.debug(e);
		}
		return r;
	}

	public String getKey() {
		return key;
	}

	public String getPrefix(String key) {
		int end = key.indexOf(delimiter);
		if (end == -1) {
			return null;
		}
		return key.substring(0, end);
	}

	public String getValue() {
		return value;
	}

	/**
	 * ユーザー毎にキーが分かれるという構想
	 * @param userId
	 * @param key	delimiter . を含む事ができない
	 */
	public void setKey(Long userId, String key) {
		this.key = createKey("" + userId, key);
	}

	public void setKeyGlobal(String key) {
		this.key = createKey(global, key);
	}

	public void setValue(String value) {
		this.value = value;
	}

	private final boolean validateAtCommonObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (key == null || key.length() == 0) {
			r.add(Lang.FREEKVS_KEY, Lang.ERROR_EMPTY);
			b = false;
		} else if (key.length() > keyMax) {
			r.add(Lang.FREEKVS_KEY, Lang.ERROR_TOO_LONG,
					key.length() + " / " + keyMax);
			b = false;
		} else {
			if (!Naturality.validateTextAllCtrlChar(Lang.FREEKVS_KEY, key, r))
				b = false;
		}
		if (value == null || value.length() == 0) {
			r.add(Lang.FREEKVS_VALUE, Lang.ERROR_EMPTY);
			b = false;
		} else if (value.length() > valueMax) {
			r.add(Lang.FREEKVS_VALUE, Lang.ERROR_TOO_LONG,
					value.length() + " / " + valueMax);
			b = false;
		} else {
			if (!Naturality.validateText(Lang.FREEKVS_VALUE, value, r))
				b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof FreeKVPair)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		FreeKVPair old2 = (FreeKVPair) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getKey(), old2.getKey())) {
			r.add(Lang.FREEKVS_KEY, Lang.ERROR_UNALTERABLE,
					"key=" + getKey() + " oldKey=" + old2.getKey());
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) {
		boolean b = true;
		try {
			String userIdStr = getPrefix(key);
			if (!global.equals(userIdStr)) {
				Long userId = Long.valueOf(userIdStr);
				User u = new UserStore(txn).get(userId);
				if (u == null) {
					r.add(Lang.FREEKVS_KEY_USERPREFIX,
							Lang.ERROR_DB_NOTFOUND_REFERENCE);
					b = false;
				}
			}
		} catch (Exception e) {
			Glb.debug(e);
			b = false;
		}
		return b;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
