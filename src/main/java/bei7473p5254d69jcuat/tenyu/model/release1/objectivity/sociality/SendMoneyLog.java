package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class SendMoneyLog implements Storable {
	/**
	 * 送金額
	 */
	private long amount;
	/**
	 * 送金時のヒストリーインデックス
	 * historyIndexを記録するのは古い記録を破棄するため
	 */
	private long historyIndex;

	@SuppressWarnings("unused")
	private SendMoneyLog() {
	}

	public SendMoneyLog(long amount, long historyIndex) {
		if (amount < 0 || historyIndex < 0)
			throw new IllegalArgumentException();
		this.amount = amount;
		this.historyIndex = historyIndex;
	}

	public long getAmount() {
		return amount;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (amount <= 0) {
			r.add(Lang.SOCIALITY_WALLET_SENDMONEYLOG_AMOUNT,
					Lang.ERROR_INVALID, "amount=" + amount);
			b = false;
		}
		if (historyIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.SOCIALITY_WALLET_SENDMONEYLOG_HISTORYINDEX,
					Lang.ERROR_INVALID, "historyIndex=" + historyIndex);
			b = false;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (amount ^ (amount >>> 32));
		result = prime * result + (int) (historyIndex ^ (historyIndex >>> 32));
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
		SendMoneyLog other = (SendMoneyLog) obj;
		if (amount != other.amount)
			return false;
		if (historyIndex != other.historyIndex)
			return false;
		return true;
	}
}