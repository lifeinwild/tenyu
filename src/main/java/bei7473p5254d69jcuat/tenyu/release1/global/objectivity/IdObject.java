package bei7473p5254d69jcuat.tenyu.release1.global.objectivity;

import java.io.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class IdObject
		implements IdObjectDBI, Serializable, UnversionableI {
	public static boolean validateIdStandard(Collection<Long> ids) {
		for (Long id : ids) {
			if (!IdObject.validateIdStandard(id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		validateReferenceIdObjectConcrete(r, txn);
		return r.isNoError();
	}

	abstract public boolean validateReferenceIdObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	/**
	 * 様々な箇所に出現するID検証の共通ロジック
	 * とはいえ、IDがnullでなければならない場合もあるし、
	 * このロジックが適合するかはいちいち考える必要がある。
	 *
	 * 全特殊IDが許可される
	 *
	 * @param id
	 * @return
	 */
	public static boolean validateIdStandard(Long id) {
		return id != null && (id >= IdObjectDBI.getFirstRecycleId()
				|| IdObjectDBI.isSpecialId(id));
	}

	/**
	 * 一部の特殊IDが許可される
	 *
	 * @param id
	 * @param acceptedSpecialIds
	 * @return
	 */
	public static boolean validateIdStandard(Long id,
			List<Long> acceptedSpecialIds) {
		if (id == null)
			return false;
		if (id >= IdObjectDBI.getFirstRecycleId())
			return true;
		if (acceptedSpecialIds != null && acceptedSpecialIds.contains(id))
			return true;

		return false;
	}

	public static boolean validateIdStandardNotSpecialId(Collection<Long> ids) {
		for (Long id : ids) {
			if (!IdObject.validateIdStandardNotSpecialId(id)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validateIdStandardNotSpecialId(Long id) {
		return id != null && id >= IdObjectDBI.getFirstRecycleId();
	}

	/**
	 * 動的なメンバー変数のような役割。
	 *
	 * このアイデアを採用すべきかずいぶん迷ったが、
	 * 使い方を間違えなければあった方が良いと思った。
	 * 利点は極一部のインスタンスだけが特殊な設定を必要とする場合や、
	 * モデルの更新無しで新たなフィールドを扱えるようになること。
	 * とはいえ、後者の利点は極力利用すべきでない。
	 * 基本的にchainversionupによって対応すべき。
	 */
	protected AdditionalSetting additionalSetting = null;

	/**
	 * 論理削除
	 * 現状ファイルアップロード関連で使われる事を想定している。
	 * 論理削除された場合、自動的なファイルのDLが停止し、同調対象にならず、
	 * ファイルのハッシュ値等のメタデータは残るものの
	 * ファイル自体は各ノードから削除される。
	 */
	protected boolean logicalDelete = false;

	/**
	 * 作成前null、作成後は非null
	 * あらゆる段階で一貫した検証処理は定義できない。
	 */
	protected Long recycleId;

	/**
	 * @return	nullが設定されたか
	 */
	public boolean deleteAdditionalSettingIfZero() {
		if (additionalSetting == null
				|| additionalSetting.getSetting() == null) {
			return true;
		}
		if (additionalSetting.getSetting().size() == 0) {
			additionalSetting = null;
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalSetting == null) ? 0
				: additionalSetting.hashCode());
		result = prime * result + (logicalDelete ? 1231 : 1237);
		result = prime * result
				+ ((recycleId == null) ? 0 : recycleId.hashCode());
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
		IdObject other = (IdObject) obj;
		if (additionalSetting == null) {
			if (other.additionalSetting != null)
				return false;
		} else if (!additionalSetting.equals(other.additionalSetting))
			return false;
		if (logicalDelete != other.logicalDelete)
			return false;
		if (recycleId == null) {
			if (other.recycleId != null)
				return false;
		} else if (!recycleId.equals(other.recycleId))
			return false;
		return true;
	}

	public AdditionalSetting getAdditionalSetting() {
		return additionalSetting;
	}

	public <R> R getAdditionalSetting(String key, Function<byte[], R> cnvVal) {
		try {
			if (additionalSetting == null
					|| additionalSetting.getSetting() == null) {
				return null;
			}
			byte[] b = additionalSetting.getSetting().get(key);
			return cnvVal.apply(b);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public Long getRecycleId() {
		return recycleId;
	}

	public boolean putAdditionalSetting(String key, byte[] val) {
		try {
			setupAdditionalSettingIfNull();
			additionalSetting.getSetting().put(key, val);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public boolean removeAdditionalSetting(String key) {
		if (additionalSetting == null)
			return false;
		try {
			return additionalSetting.getSetting().remove(key) != null;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public void setRecycleId(Long id) {
		this.recycleId = id;
	}

	public boolean setupAdditionalSettingIfNull() {
		if (additionalSetting == null
				|| additionalSetting.getSetting() == null) {
			additionalSetting = new AdditionalSetting();
			return true;
		}
		return false;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		return true;
	}

	@Override
	public final boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (recycleId != null) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_NOT_EMPTY);
			b = false;
		}
		if (!validateAtCreateCommon(r))
			b = false;
		if (!validateAtCreateIdObjectConcrete(r))
			b = false;

		return r.isNoError();
	}

	/**
	 * 返値が違うタイプ
	 * @param r
	 * @return
	 */
	public final ValidationResult validateAtCreate2(ValidationResult r) {
		validateAtCreate(r);
		return r;
	}

	private final boolean validateAtCreateCommon(ValidationResult r) {
		boolean b = true;
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	protected abstract boolean validateAtCreateIdObjectConcrete(
			ValidationResult r);

	@Override
	public final boolean validateAtCreateSpecifiedId(ValidationResult r) {
		boolean b = true;
		if (!validateIdStandard(recycleId)) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_INVALID);
			b = false;
		}
		if (!validateAtCreateCommon(r))
			b = false;
		if (!validateAtCreateIdObjectConcrete(r))
			b = false;
		return r.isNoError();
	}

	@Override
	public final boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		//特殊IDが削除されないので、特殊IDがリサイクルされることはない
		if (!validateIdStandardNotSpecialId(recycleId)) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_INVALID);
			b = false;
		}
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtDelete(r)) {
				b = false;
			}
		}
		return r.isNoError();
	}

	/**
	 * 返値が違うタイプ
	 * @param r
	 * @return
	 */
	public final ValidationResult validateAtDelete2(ValidationResult r) {
		validateAtDelete(r);
		return r;
	}

	@Override
	public final boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateIdStandardNotSpecialId(recycleId)) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_INVALID);
			b = false;
		}
		if (!validateAtCommon(r))
			b = false;
		if (!validateAtUpdateIdObjectConcrete(r))
			b = false;
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtUpdate(r)) {
				b = false;
			}
		}
		return r.isNoError();
	}

	/**
	 * 返値が違うタイプ
	 * @param r
	 * @return
	 */
	public final ValidationResult validateAtUpdate2(ValidationResult r) {
		validateAtUpdate(r);
		return r;
	}

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		if (!(old instanceof IdObject)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		IdObject old2 = (IdObject) old;

		boolean b = true;
		//IDは変更不可
		if (Glb.getUtil().notEqual(getRecycleId(), old2.getRecycleId())) {
			r.add(Lang.ID, Lang.ERROR_UNALTERABLE,
					"id=" + getRecycleId() + " oldId=" + old2.getRecycleId());
			b = false;
		}
		if (!validateAtUpdateChangeIdObjectConcrete(r, old2)) {
			b = false;
		}
		return r.isNoError();
	}

	abstract protected boolean validateAtUpdateChangeIdObjectConcrete(
			ValidationResult r, Object old);

	protected abstract boolean validateAtUpdateIdObjectConcrete(
			ValidationResult r);
}
