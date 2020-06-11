package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.vote.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyuManagerList implements ValidatableI {
	/**
	 * 全体運営者の最大数。
	 * 単に可変長データに対して最大長を設定しておくという観点から設定するだけで、
	 * 実際にこんな大勢想定しているわけではない。
	 */
	public static final int managerMax = 1000 * 100;

	/**
	 * 全体運営者一覧
	 */
	private HashSet<TenyuManager> managers = new HashSet<>();

	public synchronized boolean add(TenyuManager newManager) {
		if (managers.size() > managerMax)
			return false;
		return managers.add(newManager);
	}

	public List<Long> get51PerAdminIds() {
		List<Long> r = new ArrayList<>();
		TenyuManagerList l = Glb.getObje().getCore().getManagerList();
		for (Long id : l.getManagerIds()) {
			if (l.getManagerPower(id) > 0.51D)
				r.add(id);
		}
		return r;
	}

	public synchronized List<Long> getManagerIds() {
		List<Long> r = new ArrayList<>();
		for (TenyuManager m : managers) {
			r.add(m.getUserId());
		}
		return r;
	}

	/**
	 * @return	この公開鍵の全体運営者としての権限の強さ。
	 * 全体運営者ではない場合0
	 */
	public Double getManagerPower(KeyType keyType, byte[] pub) {
		Long userId = Glb.getObje().getUser(us -> us.getId(keyType, pub));
		return getManagerPower(userId);
	}

	/**
	 * @param userId
	 * @return			userIdの全体運営者としての影響割合。
	 * 全体運営者でなければ0D。NotNull
	 */
	public Double getManagerPower(Long userId) {
		if (userId == null)
			return 0D;
		for (TenyuManager m : getManagers()) {
			if (m.getUserId().equals(userId))
				return m.getPower();
		}
		return 0D;
	}

	public synchronized Set<TenyuManager> getManagers() {
		return Collections.unmodifiableSet(managers);
	}

	public boolean is51PerAdmin(Long userId) {
		if (userId == null)
			return false;
		Double p = getManagerPower(userId);
		if (p == null)
			return false;
		if (p < 0.51) {
			return false;
		}
		return true;
	}

	/**
		 * 全体運営者限定機能の場合、そうでないユーザーが操作しようとしたら
		 * アラートを出す。
		 */
	/*	public static void onlyAdmin(String guiName) {
			Long myUserId = Glb.getMiddle().getMyUserId();
			if (myUserId == null || Glb.getObje().getCore().getManagerList()
					.getManagerPower(myUserId) == 0D) {
				Glb.getGui().alert(AlertType.WARNING, guiName,
						Lang.ONLY_ADMINISTRATOR.toString());
			}
		}
	*/
	public boolean isIm51PerAdmin() {
		Long myUserId = Glb.getMiddle().getMyUserId();
		return is51PerAdmin(myUserId);
	}

	public boolean isManager(Long userId) {
		return getManagerIds().contains(userId);
	}

	public void setManagers(HashSet<TenyuManager> managers) {
		this.managers = managers;
	}

	/**
	 * 全全体運営者の影響割合を設定する
	 * @param newBalance
	 * @return 客観に反映されたバージョンの結果。このメソッド内で修正される場合がある
	 * なにも反映されなかった場合null
	 */
	public synchronized TenyuManagerElectionResult setPowers(
			TenyuManagerElectionResult newBalance) {
		if (newBalance == null || newBalance.getPowers() == null
				|| !newBalance.validate())
			return null;
		Map<Long, Double> powers = newBalance.getPowers();

		Glb.debug("修正前powers=" + powers);

		//保護期間なら作者ノードに全体運営者として0.51以上の影響割合を付与する
		//計算誤差等があるかもしれないので0.6程度指定しておく
		if (Glb.getConst().isProtectionPeriod()) {
			//保護期間における作者影響度割合の最低値
			double authorPowerMin = Glb.getConst().getProtectionAuthorPower();
			//作者ID
			Long authorId = Glb.getConst().getAuthor().getId();
			//設定されようとしている作者の影響度割合
			Double newAuthorPower = powers.get(authorId);

			//作者の影響度割合は十分か
			boolean enough = newAuthorPower != null
					&& newAuthorPower >= authorPowerMin;
			if (!enough) {
				//少しでも存在するならいったん削除
				if (newAuthorPower != null) {
					powers.remove(authorId);
				}
				Glb.getUtil().leveling(powers, 1.0 - authorPowerMin);
				powers.put(authorId, authorPowerMin);
			}
			/*
			double multiplier = Glb.getUtil().leveling(
					newBalance.getPowers().values(), 1.0 - authorPowerMin);

			//一覧中に既に作者の影響割合設定があり、かつ十分な割合が設定されているか
			boolean enough = false;
			for (Entry<Long, Double> e : newBalance.getPowers().entrySet()) {
				if (e.getKey().equals(authorId)) {
					newBalance.getPowers().put(e.getKey(),
							authorPowerMin + e.getValue() * multiplier);
					enough = true;
				} else {
					newBalance.getPowers().put(e.getKey(),
							e.getValue() * multiplier);
				}
			}
			if (!enough) {
				newBalance.getPowers().put(authorId, authorPowerMin);
			}
			*/
		}

		//誤差を無くす。0を消す
		for (Entry<Long, Double> e : powers.entrySet()) {
			double power = e.getValue();
			//計算で生じる誤差を消す	0.1%以下の値を無視する
			power = ((double) Math.floor(power * 1000)) / 1000;
			if (power <= 0) {
				powers.remove(e.getKey());
			} else {
				powers.put(e.getKey(), power);
			}
		}

		HashSet<TenyuManager> l = new HashSet<>();
		for (Entry<Long, Double> e : powers.entrySet())
			l.add(new TenyuManager(e.getKey(), e.getValue()));
		setManagers(l);
		return newBalance;
	}

	@Override
	public String toString() {
		return "TenyuManagerList [managers=" + managers + "]";
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (managers == null) {
			r.add(Lang.TENYU_MANAGERLIST, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (managers.size() > managerMax) {
				r.add(Lang.TENYU_MANAGERLIST, Lang.ERROR_TOO_MANY,
						"size=" + managers.size());
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (TenyuManager e : managers) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (TenyuManager e : managers) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (managers != null) {
			for (TenyuManager e : managers) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((managers == null) ? 0 : managers.hashCode());
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
		TenyuManagerList other = (TenyuManagerList) obj;
		if (managers == null) {
			if (other.managers != null)
				return false;
		} else if (!managers.equals(other.managers))
			return false;
		return true;
	}

}
