package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class IdObject extends Model implements IdObjectDBI {
	public static boolean validateIdStandard(Collection<Long> ids) {
		for (Long id : ids) {
			if (!IdObject.validateIdStandard(id)) {
				return false;
			}
		}
		return true;
	}

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
		return id != null && (id >= IdObjectDBI.getFirstId()
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
		if (id >= IdObjectDBI.getFirstId())
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
		return id != null && id >= IdObjectDBI.getFirstId();
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
	protected Long id;

	/**
	 * HashStore上のid
	 * IdObjectに含めるのが妥当と判断した。
	 */
	protected Long hid;

	/**
	 * 同調処理で得たオブジェクトか
	 */
	private transient boolean catchUp = false;

	/**
	 * IDがDB（ストアクラス）に頼らず設定されるオブジェクトか
	 */
	private transient boolean specifiedId = false;

	/**
	 * リサイクルされたIDか
	 */
	private transient boolean recycleHid = false;

	public void clearId() {
		setId(null);
		setHid(null);
		setSpecifiedId(false);
	}

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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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

	abstract public IdObjectGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix);

	public Long getHid() {
		return hid;
	}

	public Long getId() {
		return id;
	}

	@Override
	abstract public IdObjectStore<? extends IdObjectDBI,
			? extends IdObjectDBI> getStore(Transaction txn);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalSetting == null) ? 0
				: additionalSetting.hashCode());
		result = prime * result + (logicalDelete ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean isCatchUp() {
		return catchUp;
	}

	public boolean isRecycleHid() {
		return recycleHid;
	}

	public boolean isSpecifiedId() {
		return specifiedId;
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

	public void setCatchUp(boolean catchUp) {
		this.catchUp = catchUp;
	}

	public void setHid(Long hid) {
		this.hid = hid;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRecycleHid(boolean recycleHid) {
		this.recycleHid = recycleHid;
	}

	public void setSpecifiedId(boolean specifiedId) {
		this.specifiedId = specifiedId;
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
		boolean b = true;
		return b;
	}

	@Override
	protected boolean validateAtCreateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (isSpecifiedId()) {
			if (!validateIdStandard(id)) {
				r.add(Lang.IDOBJECT_ID, Lang.ERROR_INVALID);
				b = false;
			}
			if (!HashStore.validateHid(hid)) {
				r.add(Lang.IDOBJECT_HID, Lang.ERROR_INVALID);
				b = false;
			}
		} else {
			//この場合id系はnull
			if (id != null) {
				r.add(Lang.IDOBJECT_ID, Lang.ERROR_NOT_EMPTY);
				b = false;
			}
			if (hid != null) {
				r.add(Lang.IDOBJECT_HID, Lang.ERROR_NOT_EMPTY);
				b = false;
			}
		}
		if (!validateAtCreateCommon(r))
			b = false;
		if (!validateAtCreateIdObjectConcrete(r))
			b = false;

		return b && r.isNoError();
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

	private final boolean validateAtCreateSpecifiedId(ValidationResult r) {
		boolean b = true;
		if (!validateIdStandard(id)) {
			r.add(Lang.IDOBJECT_ID, Lang.ERROR_INVALID);
			b = false;
		}
		if (!validateAtCreateCommon(r))
			b = false;
		if (!validateAtCreateIdObjectConcrete(r))
			b = false;
		return b && r.isNoError();
	}

	@Override
	public final boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		//特殊IDは削除されない
		if (!validateIdStandardNotSpecialId(id)) {
			r.add(Lang.IDOBJECT_ID, Lang.ERROR_INVALID);
			b = false;
		}
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtDelete(r)) {
				b = false;
			}
		}
		return b && r.isNoError();
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
	protected boolean validateAtUpdateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateIdStandardNotSpecialId(id)) {
			r.add(Lang.IDOBJECT_ID, Lang.ERROR_INVALID);
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
		return b && r.isNoError();
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

		@SuppressWarnings("unused")
		boolean b = true;
		//IDは変更不可
		if (Glb.getUtil().notEqual(getId(), old2.getId())) {
			r.add(Lang.ID, Lang.ERROR_UNALTERABLE,
					"id=" + getId() + " oldId=" + old2.getId());
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

	abstract public boolean validateReferenceIdObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	protected final boolean validateReferenceModelConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		//同調処理の場合参照検証を一切行わない
		//なおこのクラスより上（Model)においてメンバー変数に他モデルへの参照はないので参照検証はない
		//この位置に参照検証の拒否コードが書かれている事は
		//実際にModelに他モデルへの参照が無い事を前提として成立している
		if (isCatchUp()) {
			return true;
		}
		if (!validateReferenceIdObjectConcrete(r, txn))
			b = false;
		return b;
	}

	@Override
	public TenyuReference<? extends IdObject> getReference() {
		return new TenyuReferenceSimple<>(id, getStoreName());
	}

}
