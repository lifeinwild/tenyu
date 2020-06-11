package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.vote;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.vote.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.vote.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 持続型の分散合意は結果が蓄積されていく
 * その1回1回の結果を表現するクラス
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVoteResult extends AdministratedObject
		implements DistributedVoteResultI {
	protected Long distributedVoteId;

	/**
	 * 分散合意が成功したか
	 */
	protected boolean finished = false;
	/**
	 * 多数派の値
	 */
	protected VoteValue majority;

	/**
	 * この分散合意が開始した時点のヒストリーインデックス
	 */
	private long startHistoryIndex;

	public DistributedVoteResult() {
		registererUserId = ModelI.getSystemId();
		mainAdministratorUserId = ModelI.getVoteId();
	}

	/**
	 * 完了したら呼ぶ
	 * @param finished	正常に完了したか
	 */
	public void finished(boolean finished) {
		if (!finished)
			return;
		this.finished = finished;
	}

	private List<Long> getAdministratorCommon() {
		List<Long> r = new ArrayList<>();
		r.add(ModelI.getSystemId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorCommon();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorCommon();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministratorCommon();
	}

	public Long getDistributedVoteId() {
		return distributedVoteId;
	}

	public VoteValue getMajority() {
		return majority;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return ModelI.getVoteId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return ModelI.getSystemId();
	}

	@Override
	public long getStartHistoryIndex() {
		return startHistoryIndex;
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	public void setDistributedVoteId(Long distributedVoteId) {
		this.distributedVoteId = distributedVoteId;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setMajority(VoteValue majority) {
		this.majority = majority;
	}

	public void setStartHistoryIndex(long startHistoryIndex) {
		this.startHistoryIndex = startHistoryIndex;
	}

	@Override
	public String toString() {
		return "distributedVoteId=" + distributedVoteId + majority;
	}

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (distributedVoteId == null) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(distributedVoteId)) {
				vr.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (!finished) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_FINISHED, Lang.ERROR_INVALID);
			b = false;
		}
		if (majority == null) {
			vr.add(Lang.DISTRIBUTEDVOTE_RESULT_MAJORITY, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!majority.getDistributedVoteId().equals(distributedVoteId)) {
				vr.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
						Lang.ERROR_NOT_EQUAL,
						"distributedVoteId=" + distributedVoteId
								+ " majorityDistributedVoteId="
								+ majority.getDistributedVoteId());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		if (majority != null) {
			if (!majority.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof DistributedVoteResult)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		DistributedVoteResult old2 = (DistributedVoteResult) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getDistributedVoteId(),
				old2.getDistributedVoteId())) {
			r.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID,
					Lang.ERROR_UNALTERABLE);
			b = false;
		}
		if (Glb.getUtil().notEqual(getMajority(), old2.getMajority())) {
			r.add(Lang.DISTRIBUTEDVOTE_RESULT_MAJORITY, Lang.ERROR_UNALTERABLE);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		if (majority != null) {
			if (!majority.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		DistributedVoteStore dvs = new DistributedVoteStore(txn);
		if (dvs.get(distributedVoteId) == null) {
			r.add(Lang.DISTRIBUTEDVOTE_RESULT_DISTRIBUTEDVOTE_ID_AND_STARTHISTORYINDEX,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"distributedVoteId=" + distributedVoteId);
			b = false;
		}

		if (!majority.validateReference(r, txn)) {
			b = false;
		}

		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((distributedVoteId == null) ? 0
				: distributedVoteId.hashCode());
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result
				+ ((majority == null) ? 0 : majority.hashCode());
		result = prime * result
				+ (int) (startHistoryIndex ^ (startHistoryIndex >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DistributedVoteResult other = (DistributedVoteResult) obj;
		if (distributedVoteId == null) {
			if (other.distributedVoteId != null)
				return false;
		} else if (!distributedVoteId.equals(other.distributedVoteId))
			return false;
		if (finished != other.finished)
			return false;
		if (majority == null) {
			if (other.majority != null)
				return false;
		} else if (!majority.equals(other.majority))
			return false;
		if (startHistoryIndex != other.startHistoryIndex)
			return false;
		return true;
	}

	@Override
	public DistributedVoteResultGui getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new DistributedVoteResultGui(guiName, cssIdPrefix);
	}

	@Override
	public DistributedVoteResultStore getStore(Transaction txn) {
		return new DistributedVoteResultStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.DISTRIBUTED_VOTE_RESULT;
	}

}