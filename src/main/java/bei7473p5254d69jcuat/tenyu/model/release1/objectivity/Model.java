package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
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
public abstract class Model implements ModelI {
	public static boolean validateIdStandard(Collection<Long> ids) {
		for (Long id : ids) {
			if (!Model.validateIdStandard(id)) {
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
		return id != null
				&& (id >= ModelI.getFirstId() || ModelI.isSpecialId(id));
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
		if (id >= ModelI.getFirstId())
			return true;
		if (acceptedSpecialIds != null && acceptedSpecialIds.contains(id))
			return true;

		return false;
	}

	public static boolean validateIdStandardNotSpecialId(Collection<Long> ids) {
		for (Long id : ids) {
			if (!Model.validateIdStandardNotSpecialId(id)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validateIdStandardNotSpecialId(Long id) {
		return id != null && id >= ModelI.getFirstId();
	}

	/**
	 * 作成HI
	 */
	private long createHistoryIndex = defaultHistoryIndex;
	/**
	 * 更新HI
	 */
	private long updateHistoryIndex = defaultHistoryIndex;
	/**
	 * 作成日時
	 */
	private long createDate = defaultDate;
	/**
	 * 更新日時
	 */
	private long updateDate = defaultDate;

	/**
	 * このオブジェクトが対応するストアにおいて最後のキー(ID)であるという保証があるか
	 * 最後のキー（連番の最後のID）であるという保証がある文脈でDBを利用する場合に
	 * DBで効率的に処理される。
	 * falseでも機能は問題ない。性能が違うだけ
	 */
	private transient boolean lastKey = false;

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
		Model other = (Model) obj;
		if (additionalSetting == null) {
			if (other.additionalSetting != null)
				return false;
		} else if (!additionalSetting.equals(other.additionalSetting))
			return false;
		if (createDate != other.createDate)
			return false;
		if (createHistoryIndex != other.createHistoryIndex)
			return false;
		if (hid == null) {
			if (other.hid != null)
				return false;
		} else if (!hid.equals(other.hid))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (logicalDelete != other.logicalDelete)
			return false;
		if (updateDate != other.updateDate)
			return false;
		if (updateHistoryIndex != other.updateHistoryIndex)
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

	public long getCreateDate() {
		return createDate;
	}

	public long getCreateHistoryIndex() {
		return createHistoryIndex;
	}

	abstract public ModelGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix);

	public Long getHid() {
		return hid;
	}

	public Long getId() {
		return id;
	}

	@Override
	public TenyuReference<? extends ModelI> getReference() {
		StoreName storeName = getStoreName();
		if (!(storeName instanceof StoreNameEnum)) {
			throw new IllegalArgumentException(
					"storeName.class = " + storeName.getClass());
		}
		return new TenyuReferenceSimple<>(id, (StoreNameEnum) storeName);
	}

	abstract public ModelStore<? extends ModelI, ? extends ModelI> getStore(
			Transaction txn);

	public long getUpdateDate() {
		return updateDate;
	}

	public long getUpdateHistoryIndex() {
		return updateHistoryIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalSetting == null) ? 0
				: additionalSetting.hashCode());
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result
				+ (int) (createHistoryIndex ^ (createHistoryIndex >>> 32));
		result = prime * result + ((hid == null) ? 0 : hid.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (logicalDelete ? 1231 : 1237);
		result = prime * result + (int) (updateDate ^ (updateDate >>> 32));
		result = prime * result
				+ (int) (updateHistoryIndex ^ (updateHistoryIndex >>> 32));
		return result;
	}

	public boolean isCatchUp() {
		return catchUp;
	}

	public boolean isLastKey() {
		return lastKey;
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

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setCreateHistoryIndex(long createHistoryIndex) {
		this.createHistoryIndex = createHistoryIndex;
	}

	public void setHid(Long hid) {
		this.hid = hid;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLastKey(boolean lastId) {
		this.lastKey = lastId;
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

	@Override
	public void setupAtCreate() {
		//もし独自の日時が設定されていたら上書きしない
		if (getCreateHistoryIndex() == defaultHistoryIndex)
			setCreateHistoryIndex(Glb.getObje().getCore().getHistoryIndex());
		if (getCreateDate() == defaultDate)
			setCreateDate(Glb.getObje().getGlobalCurrentTime());
		//作成当初、更新日時は作成日時がセットされる
		if (getUpdateDate() == defaultDate)
			setUpdateDate(getCreateDate());
		if (getUpdateHistoryIndex() == defaultHistoryIndex)
			setUpdateHistoryIndex(getCreateHistoryIndex());
	}

	@Override
	public void setupAtDelete() {
	}

	@Override
	public void setupAtUpdate() {
		setUpdateHistoryIndex(Glb.getObje().getCore().getHistoryIndex());
		setUpdateDate(Glb.getObje().getGlobalCurrentTime());
	}

	public void setUpdateDate(long updateDate) {
		this.updateDate = updateDate;
	}

	public void setUpdateHistoryIndex(long updateHistoryIndex) {
		this.updateHistoryIndex = updateHistoryIndex;
	}

	@Override
	public String toString() {
		return "Model [createHistoryIndex=" + createHistoryIndex
				+ ", updateHistoryIndex=" + updateHistoryIndex + ", createDate="
				+ createDate + ", updateDate=" + updateDate
				+ ", additionalSetting=" + additionalSetting
				+ ", logicalDelete=" + logicalDelete + ", id=" + id + ", hid="
				+ hid + "]";
	}

	@Override
	public final boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtCreate(r)) {
				b = false;
			}
		}

		if (isSpecifiedId()) {
			if (!validateIdStandard(id)) {
				r.add(Lang.MODEL, Lang.ID, Lang.ERROR_INVALID);
				b = false;
			}
			//hidはnullの場合があるが、それはObjectivityCoreなど
			//特殊なモデルに限られ、
			//SingleObjectStoreI#setup
			//によってhidが（使用されないが）セットされる
			if (!HashStore.validateHid(hid)) {
				r.add(Lang.MODEL, Lang.HID, Lang.ERROR_INVALID);
				b = false;
			}
		} else {
			//この場合id系はnull
			if (id != null) {
				r.add(Lang.MODEL, Lang.ID, Lang.ERROR_NOT_EMPTY);
				b = false;
			}
			if (hid != null) {
				r.add(Lang.MODEL, Lang.HID, Lang.ERROR_NOT_EMPTY);
				b = false;
			}
		}

		if (!validateAtCreateModelConcrete(r))
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

	protected abstract boolean validateAtCreateModelConcrete(
			ValidationResult r);

	@Override
	public final boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		//特殊IDは削除されない
		if (!validateIdStandardNotSpecialId(id)) {
			r.add(Lang.MODEL, Lang.ID, Lang.ERROR_INVALID);
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
	public final boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (additionalSetting != null) {
			if (!additionalSetting.validateAtUpdate(r)) {
				b = false;
			}
		}

		if (!validateIdStandardNotSpecialId(id)) {
			r.add(Lang.MODEL, Lang.ID, Lang.ERROR_INVALID);
			b = false;
		}

		if (!validateAtUpdateModelConcrete(r))
			b = false;
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
		if (!(old instanceof Model)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Model old2 = (Model) old;

		boolean b = true;
		//IDは変更不可
		if (Glb.getUtil().notEqual(getId(), old2.getId())) {
			r.add(Lang.MODEL, Lang.ID, Lang.ERROR_UNALTERABLE,
					"id=" + getId() + " oldId=" + old2.getId());
			b = false;
		}

		//時間系
		//作成日時は変更不可
		if (createDate != old2.createDate) {
			r.add(Lang.MODEL, Lang.CREATE_DATE, Lang.ERROR_UNALTERABLE,
					"createDate=" + createDate + " old.createDate="
							+ old2.createDate);
			b = false;
		}
		if (createHistoryIndex != old2.createHistoryIndex) {
			r.add(Lang.MODEL, Lang.CREATE_HISTORY_INDEX, Lang.ERROR_UNALTERABLE,
					"createHistoryIndex=" + createHistoryIndex
							+ " old.createHistoryIndex="
							+ old2.createHistoryIndex);
			b = false;
		}
		//更新日時は以前より新しくなる
		if (updateDate < old2.updateDate) {
			r.add(Lang.MODEL, Lang.UPDATE_DATE, Lang.ERROR_TOO_LITTLE,
					"updateDate=" + updateDate + " old.updateDate="
							+ old2.updateDate);
			b = false;
		}
		if (updateHistoryIndex < old2.updateHistoryIndex) {
			r.add(Lang.MODEL, Lang.UPDATE_HISTORY_INDEX, Lang.ERROR_UNALTERABLE,
					"updateHistoryIndex=" + updateHistoryIndex
							+ " old.updateHistoryIndex="
							+ old2.updateHistoryIndex);
			b = false;
		}

		//子クラス系
		if (!validateAtUpdateChangeModelConcrete(r, old2)) {
			b = false;
		}

		return b && r.isNoError();
	}

	abstract protected boolean validateAtUpdateChangeModelConcrete(
			ValidationResult r, Object old);

	protected abstract boolean validateAtUpdateModelConcrete(
			ValidationResult r);

	private final boolean validateCommon(ValidationResult r) {
		boolean b = true;

		if (createDate <= defaultDate) {
			r.add(Lang.MODEL, Lang.CREATE_DATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		if (createHistoryIndex <= defaultHistoryIndex) {
			r.add(Lang.MODEL, Lang.CREATE_HISTORY_INDEX, Lang.ERROR_INVALID,
					"createHistoryIndex=" + createHistoryIndex);
			b = false;
		}
		if (updateDate <= defaultDate || updateDate < createDate) {
			r.add(Lang.MODEL, Lang.UPDATE_DATE, Lang.ERROR_INVALID,
					"updateDate=" + updateDate + " createDate=" + createDate);
			b = false;
		}
		if (updateHistoryIndex <= defaultHistoryIndex
				|| updateHistoryIndex < createHistoryIndex) {
			r.add(Lang.MODEL, Lang.UPDATE_HISTORY_INDEX, Lang.ERROR_INVALID,
					"updateHistoryIndex=" + updateHistoryIndex
							+ " createHistoryIndex=" + createHistoryIndex);
			b = false;
		}
		return b;
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		//同調処理の場合参照検証を一切行わない
		if (isCatchUp()) {
			return true;
		}
		boolean b = true;
		if (!validateReferenceModelConcrete(r, txn))
			b = false;
		return b && r.isNoError();
	}

	abstract public boolean validateReferenceModelConcrete(ValidationResult r,
			Transaction txn) throws Exception;

}
