package bei7473p5254d69jcuat.tenyutalk.model.release1.other;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 多人数参加コンテンツのチャット
 * このクラスは{@link MultiplayerObjectI}毎にある。
 *
 * チャットメッセージは極めて高頻度に書き込みと読み込みが行われ、
 * サイズも巨大になる事を考慮する必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Chat implements StorableI {
	private static final int windowMax = 20;

	private static final int messageMax = 1000 * 1000 * 20;

	/**
	 * チャットウィンドウのリスト
	 */
	private List<ChatWindow> windows = new ArrayList<>();

	/**
	 * チャットのID
	 * このIDはストアのIDと異なり、このチャットオブジェクト内で一意
	 */
	private AtomicLong idGenerator = new AtomicLong();

	/**
	 * ID：チャットメッセージ
	 */
	private ConcurrentMap<Long,
			ChatMessage> chatMessages = new ConcurrentHashMap<>();

	/**
	 * チャットメッセージを追加する
	 * @param m	追加されるメッセージ
	 * @return	追加に成功したか
	 */
	public boolean add(ChatMessage m) {
		if (m == null)
			return false;
		ValidationResult r = new ValidationResult();
		if (!m.validateAtCreate(r))
			return false;
		if (!Glb.getTenyutalk().readTryW(txn -> m.validateReference(r, txn))) {
			return false;
		}
		Long id = idGenerator.getAndIncrement();
		m.setId(id);
		chatMessages.put(id, m);
		return true;
	}

	/**
	 * チャットメッセージの内容をクリアする
	 * ホストだけが呼び出すべき
	 *
	 * @param ChatMessaged
	 * @return
	 */
	public boolean delete(Long ChatMessaged) {
		if (ChatMessaged == null)
			return false;
		ChatMessage m = chatMessages.get(ChatMessaged);
		if (m == null)
			return false;
		return m.delete();
	}

	/**
	 * @param requestorUserId	削除を要求したユーザー
	 * @param ChatMessaged	削除されるメッセージのID
	 * @return	削除できたか
	 */
	public boolean delete(Long requestorUserId, Long ChatMessaged) {
		if (ChatMessaged == null)
			return false;
		ChatMessage m = chatMessages.get(ChatMessaged);
		if (m == null)
			return false;
		if (m.getUserId() == null || requestorUserId == null)
			return false;
		if (!m.getUserId().equals(requestorUserId))
			return false;
		return m.delete();
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
		if (chatMessages == null) {
			if (other.chatMessages != null)
				return false;
		} else if (!chatMessages.equals(other.chatMessages))
			return false;
		if (idGenerator == null) {
			if (other.idGenerator != null)
				return false;
		} else if (!idGenerator.equals(other.idGenerator))
			return false;
		if (windows == null) {
			if (other.windows != null)
				return false;
		} else if (!windows.equals(other.windows))
			return false;
		return true;
	}

	public ConcurrentMap<Long, ChatMessage> getChatMessages() {
		return chatMessages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((chatMessages == null) ? 0 : chatMessages.hashCode());
		result = prime * result
				+ ((idGenerator == null) ? 0 : idGenerator.hashCode());
		result = prime * result + ((windows == null) ? 0 : windows.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Chat [windows=" + windows + ", idGenerator=" + idGenerator
				+ ", chatMessages=" + chatMessages + "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			for (ChatWindow m : windows) {
				if (!m.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
			for (Entry<Long, ChatMessage> e : chatMessages.entrySet()) {
				if (!e.getValue().validateAtCreate(r)) {
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
			for (ChatWindow m : windows) {
				if (!m.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
			for (Entry<Long, ChatMessage> e : chatMessages.entrySet()) {
				if (!e.getValue().validateAtUpdate(r)) {
					b = false;
					break;
				}
			}

		} else {
			b = false;
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (windows == null) {
			r.add(Lang.CHAT, Lang.WINDOWS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (windows.size() > windowMax) {
				r.add(Lang.CHAT, Lang.WINDOWS, Lang.ERROR_TOO_MANY,
						"size=" + windows.size());
				b = false;
			}
		}
		if (chatMessages == null) {
			r.add(Lang.CHAT, Lang.MESSAGES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (chatMessages.size() > messageMax) {
				r.add(Lang.CHAT, Lang.MESSAGES, Lang.ERROR_TOO_MANY,
						"size=" + chatMessages.size());
				b = false;
			} else {
				for (Entry<Long, ChatMessage> e : chatMessages.entrySet()) {
					if (!Model.validateIdStandardNotSpecialId(e.getKey())) {
						r.add(Lang.CHAT, Lang.ID, Lang.ERROR_INVALID,
								"key:id=" + e.getKey());
						b = false;
						break;
					}
				}
			}
		}
		if (idGenerator == null) {
			r.add(Lang.CHAT, Lang.ID_GENERATOR, Lang.ERROR_EMPTY);
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		for (ChatWindow m : windows) {
			if (!m.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		for (Entry<Long, ChatMessage> e : chatMessages.entrySet()) {
			if (!e.getValue().validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}
}
