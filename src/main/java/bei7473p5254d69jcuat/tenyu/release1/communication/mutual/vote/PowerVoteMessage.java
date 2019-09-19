package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.vote;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;

public class PowerVoteMessage extends TurnBaseMessage implements PowerVoteI {
	private PowerVoteValue senderValue;

	@Override
	protected boolean validateTurnBaseConcrete(Message m) {
		ValidationResult r = new ValidationResult();
		senderValue.validateAtCreate(r);
		return senderValue != null && r.isNoError();
	}

	public PowerVoteValue getSenderValue() {
		return senderValue;
	}

	public void setSenderValue(PowerVoteValue senderValue) {
		this.senderValue = senderValue;
	}

	public static final int max = 1000 * 100;

	public static boolean validatePowers(HashMap<Integer, Double> powers) {
		if (powers == null || powers.size() == 0 || powers.size() > max)
			return false;
		double total = 0;
		for (Double p : powers.values())
			total += p;
		double distance = Math.abs(1.0 - total);
		double tolerance = 0.1;
		Glb.debug(() -> "distance:" + distance);
		return distance < tolerance;
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
		PowerVoteMessage other = (PowerVoteMessage) obj;
		if (senderValue == null) {
			if (other.senderValue != null)
				return false;
		} else if (!senderValue.equals(other.senderValue))
			return false;
		return true;
	}
}
