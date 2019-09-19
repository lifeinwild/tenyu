package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.vote;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;

public class PowerVoteConfirmationMessage extends TurnBaseMessage implements PowerVoteI{
	private PowerVoteValue senderValue;

	@Override
	protected boolean validateTurnBaseConcrete(Message m) {
		return senderValue != null
				&& senderValue.validateAtCreate(new ValidationResult());
	}

	public PowerVoteValue getSenderValue() {
		return senderValue;
	}

	public void setSenderValue(PowerVoteValue senderValue) {
		this.senderValue = senderValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((senderValue == null) ? 0 : senderValue.hashCode());
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
		PowerVoteConfirmationMessage other = (PowerVoteConfirmationMessage) obj;
		if (senderValue == null) {
			if (other.senderValue != null)
				return false;
		} else if (!senderValue.equals(other.senderValue))
			return false;
		return true;
	}

}
