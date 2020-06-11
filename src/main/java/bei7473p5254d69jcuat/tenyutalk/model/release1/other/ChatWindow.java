package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class ChatWindow implements ValidatableI {
	/**
	 * このウィンドウに表示されるチャットメッセージのカテゴリー一覧
	 */
	private List<String> supportCategories = new CopyOnWriteArrayList<>();

	public boolean isSupported(ChatMessage mes) {
		if (supportCategories == null || supportCategories.size() == 0)
			return true;
		return supportCategories.contains(mes.getCategory());
	}

	public List<String> getSupportCategories() {
		return supportCategories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((supportCategories == null) ? 0
				: supportCategories.hashCode());
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
		ChatWindow other = (ChatWindow) obj;
		if (supportCategories == null) {
			if (other.supportCategories != null)
				return false;
		} else if (!supportCategories.equals(other.supportCategories))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChatWindow [supportCategories=" + supportCategories + "]";
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (supportCategories == null)
			b = false;
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}
}