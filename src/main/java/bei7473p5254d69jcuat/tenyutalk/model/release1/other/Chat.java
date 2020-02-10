package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 簡易なチャット機能
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Chat implements Storable {
	/**
	 * チャットメッセージ一覧
	 */
	private List<ChatMessage> messages = new ArrayList<>();

	public ChatMessage remove(int id) {
		return messages.remove(id);
	}

	public boolean add(ChatMessage mes) {
		return messages.add(mes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((messages == null) ? 0 : messages.hashCode());
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
		Chat other = (Chat) obj;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Chat [messages=" + messages + "]";
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (messages == null) {
			r.add(Lang.CHAT, Lang.MESSAGES, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (ChatMessage m : messages) {
				if (!m.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (ChatMessage m : messages) {
				if (!m.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (validateCommon(r)) {
			for (ChatMessage m : messages) {
				if (!m.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		} else {
			b = false;
		}
		return b;
	}
}
