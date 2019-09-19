package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.vote;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.vote.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import jetbrains.exodus.env.*;

/**
 * 分配割合型の投票値
 * @author exceptiontenyu@gmail.com
 *
 */
public class PowerVoteValue implements VoteValue {
	public static final int max = Subjectivity.neighborMax;
	private Long distributedVoteId;
	private Map<Integer, Double> powers = new HashMap<>();

	public PowerVoteValue clone() {
		PowerVoteValue r = new PowerVoteValue();
		r.setPowers(new HashMap<>(powers));
		r.setDistributedVoteId(distributedVoteId);
		return r;
	}

	@Override
	public String toString() {
		return "powers=" + powers;
	}

	public Map<Long, Double> cnvLongOptionToPower() {
		DistributedVote v = Glb.getObje()
				.getDistributedVote(dvs -> dvs.get(distributedVoteId));
		if (v == null)
			return null;
		Map<Long, Double> r = new HashMap<>();
		//LongOptionは現状ユーザーIDとして使われる
		//選択肢IDからLongOptionに直して選出結果として扱う
		for (Entry<Integer, Double> e : getPowers().entrySet()) {
			try {
				DistributedVoteChoice c = v.getChoices().get(e.getKey());
				Double power = e.getValue();
				r.put(c.getOptionLong(), power);
			} catch (Exception ex2) {
				Glb.getLogger().error("", ex2);
			}
		}
		return r;
	}

	public Long getDistributedVoteId() {
		return distributedVoteId;
	}

	public Map<Integer, Double> getPowers() {
		return powers;
	}

	public void setDistributedVoteId(Long distributedVoteId) {
		this.distributedVoteId = distributedVoteId;
	}

	public void setPowers(HashMap<Integer, Double> powers) {
		this.powers = powers;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		DistributedVoteStore dvs = new DistributedVoteStore(txn);
		if (dvs.get(distributedVoteId) == null) {
			r.add(Lang.DISTRIBUTEDVOTE_VOTEVALUE_DISTRIBUTEDVOTE_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"distributedVoteId=" + distributedVoteId);
			b = false;
		}
		return b;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (powers == null || powers.size() == 0) {
			r.add(Lang.DISTRIBUTEDVOTE_POWERVOTE_POWERS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (powers.size() > max) {
				r.add(Lang.DISTRIBUTEDVOTE_POWERVOTE_POWERS,
						Lang.ERROR_TOO_MANY);
				b = false;
			}
		}

		if (distributedVoteId == null) {
			r.add(Lang.DISTRIBUTEDVOTE_VOTEVALUE_DISTRIBUTEDVOTE_ID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(distributedVoteId)) {
				r.add(Lang.DISTRIBUTEDVOTE_VOTEVALUE_DISTRIBUTEDVOTE_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distributedVoteId == null) ? 0
				: distributedVoteId.hashCode());
		result = prime * result + ((powers == null) ? 0 : powers.hashCode());
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
		PowerVoteValue other = (PowerVoteValue) obj;
		if (distributedVoteId == null) {
			if (other.distributedVoteId != null)
				return false;
		} else if (!distributedVoteId.equals(other.distributedVoteId))
			return false;
		if (powers == null) {
			if (other.powers != null)
				return false;
		} else if (!powers.equals(other.powers))
			return false;
		return true;
	}

}