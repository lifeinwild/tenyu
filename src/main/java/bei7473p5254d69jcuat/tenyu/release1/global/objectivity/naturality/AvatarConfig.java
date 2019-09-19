package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import jetbrains.exodus.env.*;

/**
 * ユーザーのアバターに関する設定
 * @author exceptiontenyu@gmail.com
 *
 */
public class AvatarConfig implements Storable {
	/**
	 * GUI等で使われる形式
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class AvatarConfigItem {
		private int priority;
		private Avatar avatar;

		public AvatarConfigItem(Long avatarId, int priority) {
			avatar = Glb.getObje().getAvatar(as -> as.get(avatarId));
			if (avatar == null)
				throw new IllegalArgumentException();
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public Avatar getAvatar() {
			return avatar;
		}
	}

	public List<AvatarConfigItem> getAvatarConfigItems() {
		List<AvatarConfigItem> r = new ArrayList<>();
		int priority = 1;
		for (Long avatarId : avatarIds) {
			r.add(new AvatarConfigItem(avatarId, priority));
			priority++;
		}
		return r;
	}

	/**
	 * アバターID一覧
	 * リストの順序が優先度になる。
	 * アバターは複数のスタイルに対応するので、
	 * あるユーザーにおいてあるスタイルに対応するアバター一覧を探した時
	 * 複数の候補が見つかる場合があるが、リストの順序で上にあるものを優先して使用する。
	 */
	private ArrayList<Long> avatarIds = new ArrayList<>();

	/**
	 * アバター最大件数
	 */
	public static final int max = 20;

	public void clear() {
		avatarIds.clear();
	}

	/**
	 * @param avatarId	設定するアバターのID
	 * @param priority	優先度
	 * @return			設定できたか
	 */
	public boolean add(Long avatarId, int priority) {
		if (avatarIds.size() > max) {
			return false;
		}

		//既に存在するならそれを除去して新たな優先度で設定
		for (Long id : avatarIds) {
			if (id.equals(avatarId)) {
				avatarIds.remove(id);
			}
		}

		if (priority > avatarIds.size()) {
			priority = avatarIds.size();
		}

		if (priority < 0)
			priority = 0;

		avatarIds.add(priority, avatarId);
		return true;
	}

	public boolean remove(Long avatarId) {
		return avatarIds.remove(avatarId);
	}

	public List<Long> getAvatarIdsCopy() {
		return new ArrayList<>(avatarIds);
	}

	public boolean isInit() {
		if (avatarIds.size() != 0)
			return false;

		return true;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (avatarIds == null) {
			r.add(Lang.USER_AVATAR_CONFIG, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (avatarIds.size() > max) {
				r.add(Lang.USER_AVATAR_CONFIG, Lang.ERROR_TOO_MANY,
						"avatarIds.size=" + avatarIds.size());
				b = false;
			} else {
				if (!IdObject.validateIdStandardNotSpecialId(avatarIds)) {
					r.add(Lang.USER_AVATAR_CONFIG, Lang.ERROR_INVALID,
							"avatarIds=" + avatarIds);
					b = false;
				}
			}
		}

		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		AvatarStore as = new AvatarStore(txn);
		for (Long avatarId : avatarIds) {
			if (as.get(avatarId) == null) {
				r.add(Lang.USER_AVATAR_CONFIG, Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"avatarId=" + avatarId);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((avatarIds == null) ? 0 : avatarIds.hashCode());
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
		AvatarConfig other = (AvatarConfig) obj;
		if (avatarIds == null) {
			if (other.avatarIds != null)
				return false;
		} else if (!avatarIds.equals(other.avatarIds))
			return false;
		return true;
	}

}
