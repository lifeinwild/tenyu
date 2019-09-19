package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.env.*;

/**
 * 客観を更新する情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateData implements ObjectivityUpdateDataDBI {
	private List<ObjectivityUpdateDataElement> procFromOtherModules;
	private UserMessageList messages;
	private List<DelayRunDBI> delayRuns;
	private long historyIndex;

	public long getHistoryIndex() {
		return historyIndex;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((delayRuns == null) ? 0 : delayRuns.hashCode());
		result = prime * result + (int) (historyIndex ^ (historyIndex >>> 32));
		result = prime * result
				+ ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + ((procFromOtherModules == null) ? 0
				: procFromOtherModules.hashCode());
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
		ObjectivityUpdateData other = (ObjectivityUpdateData) obj;
		if (delayRuns == null) {
			if (other.delayRuns != null)
				return false;
		} else if (!delayRuns.equals(other.delayRuns))
			return false;
		if (historyIndex != other.historyIndex)
			return false;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		if (procFromOtherModules == null) {
			if (other.procFromOtherModules != null)
				return false;
		} else if (!procFromOtherModules.equals(other.procFromOtherModules))
			return false;
		return true;
	}

	public List<ObjectivityUpdateDataElement> getProcFromOtherModules() {
		return procFromOtherModules;
	}

	public void setProcFromOtherModules(
			List<ObjectivityUpdateDataElement> procFromOtherModules) {
		this.procFromOtherModules = procFromOtherModules;
	}

	public UserMessageList getMessages() {
		return messages;
	}

	public void setMessages(UserMessageList messages) {
		this.messages = messages;
	}

	public List<DelayRunDBI> getDelayRuns() {
		return delayRuns;
	}

	public void setDelayRuns(List<DelayRunDBI> delayRuns) {
		this.delayRuns = delayRuns;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (messages != null) {
			if (!messages.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (messages != null) {
			if (!messages.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		if (messages != null) {
			if (!messages.validateAtDelete(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (messages != null) {
			if (!messages.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}

}