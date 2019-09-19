package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * ユーザーその他オブジェクトについてBANする
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaBAN implements AgendaContentI {
	/**
	 * 可変長データの最大長制限
	 */
	public static final int socialityIdMax = 1000 * 1000;
	/**
	 * 個別BAN
	 */
	private List<Long> banSocialityIds = new ArrayList<>();
	/**
	 * 社会性ID一覧
	 * これら社会性がBANされ、さらに
	 * これら社会性が対応する自然性がメイン管理者である全てのオブジェクトが
	 * これらのIDがメイン管理者である全ての社会性をBANする
	 * 子孫がBANされない
	 */
	private List<Long> banSocialityIdsUser = new ArrayList<>();

	/**
	 * 自然性がユーザーかつ連鎖型のBAN
	 * ここに指定されたユーザーおよびそのユーザーが紹介したユーザー
	 * さらにそれらユーザーが紹介したユーザーについて
	 * メイン管理者である全ての社会性をBANする
	 *
	 * つまりあるユーザー以下の全子孫をBANする
	 */
	private List<Long> banSocialityIdsUserChain = new ArrayList<>();

	private void ban(SocialityStore store, Long soId) {
		ban(store, store.get(soId));
	}

	private void ban(SocialityStore store, Sociality s) {
		try {
			if (s.isBanned())
				return;
			s.setBanned(true);
			if (!store.update(s))
				Glb.getLogger().error(Lang.AGENDA_BAN.toString(),
						new Exception("Failed to update"));
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	/**
	 * ネストが深いので分けた
	 * @param txn
	 * @return
	 */
	private boolean banProc(Transaction txn) {
		try {
			SocialityStore store = new SocialityStore(txn);
			//単純なBAN。指定された社会性IDについてBANする
			for (Long id : banSocialityIds) {
				Sociality so = store.get(id);
				if (so == null)
					continue;
				ban(store, so);
			}
			//ユーザー型の社会性IDを指定し、そのユーザーが登録者である全オブジェクトも連鎖的にBANする
			for (Long userSocialityId : banSocialityIdsUser) {
				Sociality so = store.get(userSocialityId);
				if (so == null || so.getNodeType() != NodeType.USER)
					continue;
				ban(store, so);
				relatedObjsBan(store, so, txn);
			}
			//ユーザー型の社会性IDを指定し、そのユーザーが紹介した全子孫ユーザーについて連鎖的にBANする
			for (Long userSocialityId : banSocialityIdsUserChain) {
				Sociality so = store.get(userSocialityId);
				if (so == null || so.getNodeType() != NodeType.USER)
					continue;
				ban(store, so);
				relatedObjsBan(store, so, txn);
				UserStore uStore = new UserStore(txn);
				childChainBan(store, uStore,
						so.getNaturalityConcreteRecycleId(), txn);
			}

			return txn.commit();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			txn.abort();
			return false;
		}
	}

	private void childChainBan(SocialityStore store, UserStore uStore,
			Long parentUserId, Transaction txn) {
		for (Long childUserId : uStore.getIdsByRegisterer(parentUserId)) {
			Sociality so = store.getByNaturality(NodeType.USER, childUserId);
			ban(store, so);
			relatedObjsBan(store, so, txn);
			childChainBan(store, uStore, childUserId, txn);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaBAN other = (AgendaBAN) obj;
		if (banSocialityIds == null) {
			if (other.banSocialityIds != null)
				return false;
		} else if (!banSocialityIds.equals(other.banSocialityIds))
			return false;
		if (banSocialityIdsUser == null) {
			if (other.banSocialityIdsUser != null)
				return false;
		} else if (!banSocialityIdsUser.equals(other.banSocialityIdsUser))
			return false;
		if (banSocialityIdsUserChain == null) {
			if (other.banSocialityIdsUserChain != null)
				return false;
		} else if (!banSocialityIdsUserChain
				.equals(other.banSocialityIdsUserChain))
			return false;
		return true;
	}

	public List<Long> getBanSocialityIds() {
		return banSocialityIds;
	}

	public List<Long> getBanSocialityIdsUser() {
		return banSocialityIdsUser;
	}

	public List<Long> getBanSocialityIdsUserChain() {
		return banSocialityIdsUserChain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((banSocialityIds == null) ? 0 : banSocialityIds.hashCode());
		result = prime * result + ((banSocialityIdsUser == null) ? 0
				: banSocialityIdsUser.hashCode());
		result = prime * result + ((banSocialityIdsUserChain == null) ? 0
				: banSocialityIdsUserChain.hashCode());
		return result;
	}

	/**
	 * ユーザーをBANするとき、そのユーザーが登録した全オブジェクトの社会性を
	 * BANする。フロー計算等からそのユーザーの影響が完全に取り除かれる。
	 * @param store
	 * @param so		NodeType.Userを想定
	 * @param txn
	 */
	private void relatedObjsBan(SocialityStore store, Sociality so,
			Transaction txn) {
		//soはユーザーの社会性を想定する
		if (so == null || so.getNodeType() != NodeType.USER)
			return;
		//全ノードタイプの社会性について処理する
		for (NodeType type : NodeType.values()) {
			//ノードタイプに対応する自然性ストアを取得
			Object tmp = type.getStore(txn);
			if (!(tmp instanceof ObjectivityObjectStore)) {
				Glb.getLogger().warn("",
						new Exception("not ObjectivityObjectStore"));
				continue;
			}
			ObjectivityObjectStore<?,
					?> nStore = (ObjectivityObjectStore<?, ?>) tmp;
			//このユーザーが登録者である全オブジェクトについて
			List<Long> relatedObjs = nStore
					.getIdsByRegisterer(so.getNaturalityConcreteRecycleId());
			//そのオブジェクトが社会性を持つならbanする
			for (Long relatedObjId : relatedObjs) {
				Long soId = store.getIdByNaturality(type, relatedObjId);
				if (soId == null)
					continue;
				ban(store, soId);
			}
		}
	}

	@Override
	public boolean run(Agenda a) {
		Boolean r = Glb.getObje().readRet(txn -> banProc(txn));
		if (r == null)
			return false;
		return r;
	}

	public void setBanSocialityIds(List<Long> banSocialityIds) {
		this.banSocialityIds = banSocialityIds;
	}

	public void setBanSocialityIdsUser(List<Long> banSocialityIdsUser) {
		this.banSocialityIdsUser = banSocialityIdsUser;
	}

	public void setBanSocialityIdsUserChain(
			List<Long> banSocialityIdsUserChain) {
		this.banSocialityIdsUserChain = banSocialityIdsUserChain;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (banSocialityIds == null || banSocialityIds.size() == 0) {
			r.add(Lang.AGENDA_BAN_IDS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (banSocialityIds.size() > socialityIdMax) {
				r.add(Lang.AGENDA_BAN_IDS, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				if (!IdObject.validateIdStandard(banSocialityIds)) {
					r.add(Lang.AGENDA_BAN_IDS, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		if (banSocialityIdsUser == null || banSocialityIdsUser.size() == 0) {
			r.add(Lang.AGENDA_BAN_IDS_USER, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (banSocialityIdsUser.size() > socialityIdMax) {
				r.add(Lang.AGENDA_BAN_IDS_USER, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				if (!IdObject.validateIdStandard(banSocialityIdsUser)) {
					r.add(Lang.AGENDA_BAN_IDS_USER, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		if (banSocialityIdsUserChain == null
				|| banSocialityIdsUserChain.size() == 0) {
			r.add(Lang.AGENDA_BAN_IDS_USER_CHAIN, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (banSocialityIdsUserChain.size() > socialityIdMax) {
				r.add(Lang.AGENDA_BAN_IDS_USER_CHAIN, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				if (!IdObject.validateIdStandard(banSocialityIdsUserChain)) {
					r.add(Lang.AGENDA_BAN_IDS_USER_CHAIN, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn) {
		boolean b = true;
		try {
			SocialityStore ss = new SocialityStore(txn);
			for (Long id : banSocialityIds) {
				Sociality so = ss.get(id);
				if (so == null) {
					r.add(Lang.AGENDA_BAN_IDS, Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"id=" + id);
					b = false;
					break;
				} else {
					if (so.getNodeType() != NodeType.USER) {
						r.add(Lang.AGENDA_BAN_IDS, Lang.ERROR_INVALID_REFERENCE,
								"id=" + id);
						b = false;
						break;
					}
				}
			}
			//ユーザー型の社会性IDを指定し、そのユーザーが登録者である全オブジェクトも連鎖的にBANする
			for (Long id : banSocialityIdsUser) {
				Sociality so = ss.get(id);
				if (so == null) {
					r.add(Lang.AGENDA_BAN_IDS_USER,
							Lang.ERROR_DB_NOTFOUND_REFERENCE, "id=" + id);
					b = false;
					break;
				} else {
					if (so.getNodeType() != NodeType.USER) {
						r.add(Lang.AGENDA_BAN_IDS_USER,
								Lang.ERROR_INVALID_REFERENCE, "id=" + id);
						b = false;
						break;
					}
				}
			}
			//ユーザー型の社会性IDを指定し、そのユーザーが紹介した全子孫ユーザーについて連鎖的にBANする
			for (Long id : banSocialityIdsUserChain) {
				Sociality so = ss.get(id);
				if (so == null) {
					r.add(Lang.AGENDA_BAN_IDS_USER_CHAIN,
							Lang.ERROR_DB_NOTFOUND_REFERENCE, "id=" + id);
					b = false;
					break;
				} else {
					if (so.getNodeType() != NodeType.USER) {
						r.add(Lang.AGENDA_BAN_IDS_USER_CHAIN,
								Lang.ERROR_INVALID_REFERENCE, "id=" + id);
						b = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			b = false;
		}
		return b;
	}

}
