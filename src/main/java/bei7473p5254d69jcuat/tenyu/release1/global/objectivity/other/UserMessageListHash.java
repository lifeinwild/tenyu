package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.env.*;

/**
 * メッセージリストのハッシュ値は全て記録される。
 * メッセージリスト自体は各ノードがランダムに記録し統一値に含まれない。
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserMessageListHash extends ObjectivityObject
		implements UserMessageListHashDBI {
	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;//自動的に作成される
	}

	/**
	 * UserMessageListのハッシュ値
	 */
	private byte[] hash;

	/**
	 * 対応するメッセージリストが反映された直後のヒストリーインデックス
	 */
	protected long historyIndex;

	public UserMessageListHash() {
	}

	public UserMessageListHash(UserMessageList l, long historyIndex) {
		this.hash = l.hash();
		this.historyIndex = historyIndex;
		this.registererUserId = IdObjectDBI.getSystemId();
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();//自動的に作成される
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return null;//削除されない
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return null;//更新されない
	}

	public byte[] getHash() {
		return hash;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectDBI.getNullId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectDBI.getSystemId();
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	private final boolean validateAtCommonObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (hash == null || hash.length != Glb.getConst().getHashSize()) {
			r.add(Lang.USERMESSAGELISTHASH_HASH, Lang.ERROR_INVALID);
			b = false;
		}
		if (historyIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof UserMessageListHash)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		UserMessageListHash old2 = (UserMessageListHash) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getHash(), old2.getHash())) {
			r.add(Lang.USERMESSAGELISTHASH_HASH, Lang.ERROR_UNALTERABLE);
			b = false;
		}
		if (Glb.getUtil().notEqual(getHistoryIndex(), old2.getHistoryIndex())) {
			r.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"historyIndex=" + getHistoryIndex() + " oldHistoryIndex="
							+ old2.getHistoryIndex());
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		return true;
	}

}
