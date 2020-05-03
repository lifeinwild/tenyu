package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import bei7473p5254d69jcuat.tenyutalk.ui.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * @author exceptiontenyu@gmail.com
 *
 * @param <L>
 */
public abstract class MultiplayerObject extends CreativeObject
		implements MultiplayerObjectI {

	private static final long participantUserIdsMax = 1000 * 1000 * 1000;

	/**
	 * テキストチャット
	 */
	private Chat chat = new Chat();

	/**
	 * ペイントツール
	 */
	private Paint paint = null;

	/**
	 * ホストのユーザーID
	 */
	private Long hostUserId;

	/**
	 * 参加者一覧
	 * 参加者毎に立場が異なる可能性があるがそれは具象クラスで管理する
	 *
	 */
	private List<Long> participantUserIds = Collections
			.synchronizedList(new ArrayList<>());

	/**
	 * チャット可能か
	 */
	private boolean chatable = true;

	/**
	 * 終了したか
	 * 終了に伴い新たなチャット発言が不可能になり、
	 * チャットウィンドウの設定が変更不可能になる。
	 */
	private boolean end = false;

	public boolean isEnd() {
		return end;
	}

	/**
	 * 終了させる
	 */
	public void end() {
		end = true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiplayerObject other = (MultiplayerObject) obj;
		if (chat == null) {
			if (other.chat != null)
				return false;
		} else if (!chat.equals(other.chat))
			return false;
		if (chatable != other.chatable)
			return false;
		if (end != other.end)
			return false;
		if (hostUserId == null) {
			if (other.hostUserId != null)
				return false;
		} else if (!hostUserId.equals(other.hostUserId))
			return false;
		if (paint == null) {
			if (other.paint != null)
				return false;
		} else if (!paint.equals(other.paint))
			return false;
		if (participantUserIds == null) {
			if (other.participantUserIds != null)
				return false;
		} else if (!participantUserIds.equals(other.participantUserIds))
			return false;
		return true;
	}

	public Chat getChat() {
		return chat;
	}

	@Override
	abstract public MultiplayerObjectGui<?, ?, ?, ?, ?, ?> getGui(
			String guiName, String cssIdPrefix);

	public Long getHostUserId() {
		return hostUserId;
	}

	public User getHost() {
		return Glb.getObje().getUser(us -> us.get(hostUserId));
	}

	public Paint getPaint() {
		return paint;
	}

	public List<Long> getParticipantUserIds() {
		return participantUserIds;
	}

	@Override
	abstract public MultiplayerObjectStore<? extends MultiplayerObjectI,
			? extends MultiplayerObjectI> getStore(Transaction txn);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((chat == null) ? 0 : chat.hashCode());
		result = prime * result + (chatable ? 1231 : 1237);
		result = prime * result + (end ? 1231 : 1237);
		result = prime * result
				+ ((hostUserId == null) ? 0 : hostUserId.hashCode());
		result = prime * result + ((paint == null) ? 0 : paint.hashCode());
		result = prime * result + ((participantUserIds == null) ? 0
				: participantUserIds.hashCode());
		return result;
	}

	public boolean isChatable() {
		return chatable;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public void setChatable(boolean chatable) {
		this.chatable = chatable;
	}

	public void setHostUserId(Long hostUserId) {
		this.hostUserId = hostUserId;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	public void setParticipantUserIds(List<Long> participantUserIds) {
		this.participantUserIds = participantUserIds;
	}

	@Override
	public String toString() {
		return "MultiplayerObject [chat=" + chat + ", paint=" + paint
				+ ", hostUserId=" + hostUserId + ", participantUserIds="
				+ participantUserIds + ", chatable=" + chatable + ", end=" + end
				+ "]";
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (chat == null) {
			r.add(Lang.MULTIPLAYER, Lang.CHAT, Lang.ERROR_EMPTY);
			b = false;
		}

		if (hostUserId == null) {
			r.add(Lang.MULTIPLAYER, Lang.HOST_USER_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(hostUserId)) {
				r.add(Lang.MULTIPLAYER, Lang.HOST_USER_ID, Lang.ERROR_INVALID,
						"hostUserId=" + hostUserId);
				b = false;
			}
		}

		if (participantUserIds == null) {
			r.add(Lang.MULTIPLAYER, Lang.PARTICIPANT_USER_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (participantUserIds.size() > participantUserIdsMax) {
				r.add(Lang.MULTIPLAYER, Lang.PARTICIPANT_USER_IDS,
						Lang.ERROR_TOO_MANY,
						"size=" + participantUserIds.size());
				b = false;
			} else {
				for (Long participantUserId : participantUserIds) {
					if (!Model.validateIdStandardNotSpecialId(
							participantUserId)) {
						r.add(Lang.MULTIPLAYER, Lang.PARTICIPANT_USER_ID,
								Lang.ERROR_INVALID,
								"participantUserId=" + participantUserId);
						b = false;
						break;
					}
				}
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateCreativeObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;

		//作成時に既に終わっている事はない
		if (end)
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeCreativeObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!(old instanceof MultiplayerObject)) {
			r.add(Lang.MULTIPLAYER, Lang.ERROR_INVALID,
					"old=" + old.getClass());
			return false;
		}

		MultiplayerObject o = (MultiplayerObject) old;
		if (!hostUserId.equals(o.getHostUserId())) {
			r.add(Lang.MULTIPLAYER, Lang.HOST_USER_ID, Lang.ERROR_NOT_EQUAL,
					"hostUserId=" + hostUserId + " o.hostUserId="
							+ o.getHostUserId());
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtUpdateCreativeObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceCreativeObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(hostUserId) == null) {
			r.add(Lang.MULTIPLAYER, Lang.HOST_USER_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"hostUserId=" + hostUserId);
			b = false;
		}
		for (Long participantUserId : participantUserIds) {
			if (us.get(participantUserId) == null) {
				r.add(Lang.MULTIPLAYER, Lang.PARTICIPANT_USER_ID,
						Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"participantUserId=" + participantUserId);
				b = false;
				break;
			}
		}

		if (!chat.validateReference(r, txn)) {
			b = false;
		}

		if (!chat.validateReference(r, txn)) {
			b = false;
		}

		return b;
	}

}
