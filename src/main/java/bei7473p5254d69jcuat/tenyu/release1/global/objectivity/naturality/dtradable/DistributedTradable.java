package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.dtradable;

import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * 所有権を分散更新可能なもの。
 * 現状のハードウェアでは分散更新は実装不可能だと思うが一応実装しておく。
 *
 * 本質的問題としてユーザーがBANされるとそのユーザーを経由していた取引履歴は
 * それ以降無効になってしまう。
 * 確認された所有者を更新する事で古い取引履歴を不要にできるので、
 * それを利用して無効化された取引履歴を有効化したり、
 * BAN直前に問題が生じる取引履歴について確認された所有者を更新するなどの対応ができる可能性がある。
 *
 * TODO:書いただけで少しもテストしていない。そもそもこのあたりは構想が煮詰まっていない。
 * 当面やる予定もない。下書きのようなものに過ぎない。
 *
 * 権限を持つユーザーが新規オブジェクトを作成する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedTradable extends Naturality
		implements DistributedTradableDBI {

	/**
	 * 所有権移転時に現在の所有者はこの文字列に署名する
	 * @param id	分散取引対象のid
	 * @param currentOwnerUserId	現在の所有者
	 * @param nextOwnerUserId		次の所有者
	 * @return	署名対象文字列
	 */
	public static byte[] generateSignTarget(Long id, Long currentOwnerUserId,
			Long nextOwnerUserId) {
		byte[] r = new byte[Long.BYTES * 3];
		ByteBuffer.wrap(r).putLong(id).putLong(currentOwnerUserId)
				.putLong(nextOwnerUserId);
		return r;
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(
				rs -> rs.getByName(DistributedTradable.class.getSimpleName()))
				.getAdminUserIds();
	}

	public static String getNominal() {
		return Glb.getConst().getAppName() + "_"
				+ DistributedTradable.class.getSimpleName() + "_";
	}

	public static boolean verify(DistributedTradable o, Long latestOwner,
			List<TradeLog> log) throws Exception {
		if (o == null || latestOwner == null || log == null)
			return false;

		//取引履歴が空なら確認されたユーザー自身が所有者
		if (log.size() == 0) {
			return o.getConfirmedOwnerUserId().equals(latestOwner);
		}

		//取引履歴上の確認されたユーザーから移転された箇所を探す
		int start = 0;
		boolean found = false;
		for (; start < log.size(); start++) {
			TradeLog trade = log.get(start);
			if (trade.getCurrentOwnerUserId() == null || !trade.validate())
				return false;
			if (log.get(start).getCurrentOwnerUserId()
					.equals(o.getConfirmedOwnerUserId())) {
				found = true;
				break;
			}
		}

		//見つからなかったら不正
		if (!found)
			return false;

		//見つかった箇所から先の取引履歴が正しいか確認
		String nominal = getNominal();
		User currentOwner = Glb.getObje()
				.getUser(us -> us.get(o.getConfirmedOwnerUserId()));
		if (currentOwner == null)
			throw new Exception("確認された所有者がDBから見つからなかった。");

		for (; start < log.size(); start++) {
			TradeLog trade = log.get(start);
			if (trade.getNextOwnerUserId() == null || !trade.validate())
				return false;
			User nextOwner = Glb.getObje()
					.getUser(us -> us.get(trade.getNextOwnerUserId()));
			if (nextOwner == null)
				return false;

			byte[] target = generateSignTarget(o.getRecycleId(),
					currentOwner.getRecycleId(), trade.getNextOwnerUserId());
			if (!Glb.getUtil().verify(nominal, trade.getSign(),
					nextOwner.getPubKey(trade.getType()), target))
				return false;

			currentOwner = nextOwner;
		}

		//指定された最新の所有者と確認された最後の所有者が一致するか
		return latestOwner.equals(currentOwner);
	}

	/**
	 * 確認された所有者
	 * 取引履歴はここから先だけで十分
	 */
	private Long confirmedOwnerUserId;
	/**
	 * 最初の所有者
	 * 確認された所有者が設定されたら、この情報は実用上必要無くなるだろう
	 */
	private Long firstOwnerUserId;

	@SuppressWarnings("unused")
	private DistributedTradable() {
	}

	public DistributedTradable(Long firstOwnerUserId, String name,
			String explanation) {
		setFirstOwnerUserId(firstOwnerUserId);
		setConfirmedOwnerUserId(firstOwnerUserId);
		setName(name);
		setExplanation(explanation);
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(firstOwnerUserId);
		return r;
	}

	public Long getConfirmedOwnerUserId() {
		return confirmedOwnerUserId;
	}

	public Long getFirstOwnerUserId() {
		return firstOwnerUserId;
	}

	public void setConfirmedOwnerUserId(Long confirmedOwnerUserId) {
		this.confirmedOwnerUserId = confirmedOwnerUserId;
	}

	public void setFirstOwnerUserId(Long firstOwnerUserId) {
		this.firstOwnerUserId = firstOwnerUserId;
	}

	private final boolean validateAtCommonNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (confirmedOwnerUserId == null) {
			r.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject
					.validateIdStandardNotSpecialId(confirmedOwnerUserId)) {
				r.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (firstOwnerUserId == null) {
			r.add(Lang.DISTRIBUTEDTRADABLE_FIRST_OWNER, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(firstOwnerUserId)) {
				r.add(Lang.DISTRIBUTEDTRADABLE_FIRST_OWNER, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof DistributedTradable)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		DistributedTradable old2 = (DistributedTradable) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getFirstOwnerUserId(),
				old2.getFirstOwnerUserId())) {
			r.add(Lang.DISTRIBUTEDTRADABLE_FIRST_OWNER, Lang.ERROR_UNALTERABLE,
					"firstOwnerUserId=" + getFirstOwnerUserId()
							+ " oldFirstOwnerUserId="
							+ old2.getFirstOwnerUserId());
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		if (us.get(confirmedOwnerUserId) == null) {
			r.add(Lang.DISTRIBUTEDTRADABLE_CONFIRMED_OWNER,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		if (us.get(firstOwnerUserId) == null) {
			r.add(Lang.DISTRIBUTEDTRADABLE_FIRST_OWNER,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		return b;
	}

	/**
	 * 取引記録
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class TradeLog {
		private Long currentOwnerUserId;
		private Long nextOwnerUserId;
		private byte[] sign;
		private KeyType type;

		public Long getCurrentOwnerUserId() {
			return currentOwnerUserId;
		}

		public Long getNextOwnerUserId() {
			return nextOwnerUserId;
		}

		public byte[] getSign() {
			return sign;
		}

		public KeyType getType() {
			return type;
		}

		public void setCurrentOwnerUserId(Long currentOwnerUserId) {
			this.currentOwnerUserId = currentOwnerUserId;
		}

		public void setNextOwnerUserId(Long nextOwnerUserId) {
			this.nextOwnerUserId = nextOwnerUserId;
		}

		public void setSign(byte[] sign) {
			this.sign = sign;
		}

		public void setType(KeyType type) {
			this.type = type;
		}

		public boolean validate() {
			return currentOwnerUserId != null && nextOwnerUserId != null
					&& sign != null && type != null
					&& sign.length < Glb.getConst().getSignMaxRough();
		}
	}
}
