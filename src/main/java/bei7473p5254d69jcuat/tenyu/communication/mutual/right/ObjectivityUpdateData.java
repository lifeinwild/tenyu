package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import java.util.*;

import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 客観更新に用いられた情報
 *
 * 客観更新で使用されるし、履歴を記録するためでもある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateData implements ObjectivityUpdateDataI {
	private List<ObjectivityUpdateDataElement> procFromOtherModules;
	private UserMessageList messages;
	private List<ObjectivityUpdateDataElement> processedSuperiors;
	private List<ObjectivityUpdateDataElement> processedInferiors;
	private long historyIndex;
	private StringBuilder log;

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
		result = prime * result + (int) (historyIndex ^ (historyIndex >>> 32));
		result = prime * result + ((log == null) ? 0 : log.hashCode());
		result = prime * result
				+ ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + ((procFromOtherModules == null) ? 0
				: procFromOtherModules.hashCode());
		result = prime * result + ((processedInferiors == null) ? 0
				: processedInferiors.hashCode());
		result = prime * result + ((processedSuperiors == null) ? 0
				: processedSuperiors.hashCode());
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
		if (historyIndex != other.historyIndex)
			return false;
		if (log == null) {
			if (other.log != null)
				return false;
		} else if (!log.equals(other.log))
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
		if (processedInferiors == null) {
			if (other.processedInferiors != null)
				return false;
		} else if (!processedInferiors.equals(other.processedInferiors))
			return false;
		if (processedSuperiors == null) {
			if (other.processedSuperiors != null)
				return false;
		} else if (!processedSuperiors.equals(other.processedSuperiors))
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

	@Override
	public String toString() {
		return "ObjectivityUpdateData [procFromOtherModules="
				+ procFromOtherModules + ", messages=" + messages
				+ ", processedSuperiors=" + processedSuperiors
				+ ", processedInferiors=" + processedInferiors
				+ ", historyIndex=" + historyIndex + ", log=" + log + "]";
	}

	public StringBuilder getLog() {
		return log;
	}

	public void setLog(StringBuilder log) {
		this.log = log;
	}

	public void setProcessedSuperiors(
			List<ObjectivityUpdateDataElement> processedSuperiors) {
		this.processedSuperiors = processedSuperiors;
	}

	public void setProcessedInferiors(
			List<ObjectivityUpdateDataElement> processedInferiors) {
		this.processedInferiors = processedInferiors;
	}

}