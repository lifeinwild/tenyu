package bei7473p5254d69jcuat.tenyu.model.release1;

import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public abstract class Model implements ModelI {
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
	 * 最後のキーであるという保証があるか
	 * 新たなモデルが作成された場合など、
	 * 最後のキー（連番の最後のID）であるという保証がある文脈でDBを利用する場合に
	 * DBで効率的に処理される。
	 * falseでも機能は問題ない。性能が違うだけ
	 */
	private transient boolean lastKey = false;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Model other = (Model) obj;
		if (createDate != other.createDate)
			return false;
		if (createHistoryIndex != other.createHistoryIndex)
			return false;
		if (updateDate != other.updateDate)
			return false;
		if (updateHistoryIndex != other.updateHistoryIndex)
			return false;
		return true;
	}

	public long getCreateDate() {
		return createDate;
	}

	public long getCreateHistoryIndex() {
		return createHistoryIndex;
	}

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
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result
				+ (int) (createHistoryIndex ^ (createHistoryIndex >>> 32));
		result = prime * result + (int) (updateDate ^ (updateDate >>> 32));
		result = prime * result
				+ (int) (updateHistoryIndex ^ (updateHistoryIndex >>> 32));
		return result;
	}

	public boolean isLastKey() {
		return lastKey;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setCreateHistoryIndex(long createHistoryIndex) {
		this.createHistoryIndex = createHistoryIndex;
	}

	public void setLastKey(boolean lastId) {
		this.lastKey = lastId;
	}

	@Override
	public void setupAtCreate() {
		setCreateHistoryIndex(Glb.getObje().getCore().getHistoryIndex());
		setCreateDate(Glb.getObje().getCore().getCreateDate());
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
				+ createDate + ", updateDate=" + updateDate + "]";
	}

	abstract protected boolean validateAtCreateModelConcrete(
			ValidationResult r);

	@Override
	public final boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (!validateAtCreateModelConcrete(r))
			b = false;
		return b;
	}

	abstract protected boolean validateAtUpdateModelConcrete(
			ValidationResult r);

	@Override
	public final boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;
		if (!validateAtUpdateModelConcrete(r))
			b = false;

		return b;
	}

	private final boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (createDate < defaultDate) {
			r.add(Lang.MODEL_CREATE_DATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}
		if (createHistoryIndex < defaultHistoryIndex) {
			r.add(Lang.MODEL_CREATE_HISTORY_INDEX, Lang.ERROR_INVALID,
					"createHistoryIndex=" + createHistoryIndex);
			b = false;
		}
		if (updateDate < defaultDate) {
			r.add(Lang.MODEL_UPDATE_DATE, Lang.ERROR_INVALID,
					"updateDate=" + updateDate);
			b = false;
		}
		if (updateHistoryIndex < defaultHistoryIndex) {
			r.add(Lang.MODEL_UPDATE_HISTORY_INDEX, Lang.ERROR_INVALID,
					"updateHistoryIndex=" + updateHistoryIndex);
			b = false;
		}
		return b;
	}

	@Override
	public final boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		validateReferenceModelConcrete(r, txn);
		return r.isNoError();
	}

	abstract protected boolean validateReferenceModelConcrete(
			ValidationResult r, Transaction txn) throws Exception;

}
