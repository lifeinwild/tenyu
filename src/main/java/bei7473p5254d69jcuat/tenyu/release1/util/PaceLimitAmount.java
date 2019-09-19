package bei7473p5254d69jcuat.tenyu.release1.util;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.env.*;

/**
 * 時間当たり増減量が制限される量を表現するために作成。
 * 粗い制限。
 * 日時の単位は何でもいいが統一されている必要がある。
 * transientメンバーは使用する直前のinit()で設定される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class PaceLimitAmount implements Storable {
	/**
	 * 現在の量
	 */
	private long amount;
	/**
	 * 流量制限対象が作成された日時を意味する情報
	 */
	private transient long createDate;
	/**
	 * 減少印象値
	 */
	private long decreaseImpression = 0;
	/**
	 * 減少印象値の最低値
	 * これを下回る更新は拒否される
	 */
	private transient long decreaseMax;
	/**
	 * createDateからこの期間の間は流量制限を受けない
	 */
	private transient long freePeriod;
	/**
	 * 増加印象値
	 */
	private long increaseImpression = 0;

	/**
	 * 増加印象値の最大値
	 * これを超える更新は拒否される
	 */
	private transient long increaseMax;

	/**
	 * 最後にリセットされた日時
	 */
	private long lastResetDate;

	/**
	 * リセット周期
	 */
	private transient long resetPeriod;

	@SuppressWarnings("unused")
	private PaceLimitAmount() {
	}

	public PaceLimitAmount(long initialAmount, long date) {
		amount = initialAmount;
		lastResetDate = date;
	}

	/**
	 * 量を変更する
	 * @param add	追加される量。負も可能
	 * @param date	追加された日時
	 * @param forcibly 無制限期間か
	 * @return		更新されたか。addが0の場合処理されないが更新されたとみなしtrueを返す
	 */
	public boolean add(long add, long date) {
		if (add == 0)
			return true;

		boolean forcibly = createDate + freePeriod > date;
		try {
			long distance = lastResetDate - date;
			if (distance >= resetPeriod) {
				//リセット周期が来たらペース制限をリセット
				increaseImpression = 0;
				decreaseImpression = 0;
				lastResetDate = date;
			}

			if (add > 0) {
				long newImpression = add + (increaseImpression / 2);
				if (newImpression > increaseMax && !forcibly) {
					return false;
				}
				increaseImpression = newImpression;
			} else {
				long newImpression = add + (decreaseImpression / 2);
				if (newImpression > decreaseMax && !forcibly) {
					return false;
				}
				decreaseImpression = newImpression;
			}

			amount += add;
			return true;
		} catch (Exception e) {
			Glb.debug(e);
			return false;
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
		PaceLimitAmount other = (PaceLimitAmount) obj;
		if (amount != other.amount)
			return false;
		if (decreaseImpression != other.decreaseImpression)
			return false;
		if (increaseImpression != other.increaseImpression)
			return false;
		if (lastResetDate != other.lastResetDate)
			return false;
		return true;
	}

	public long getAmount() {
		return amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (amount ^ (amount >>> 32));
		result = prime * result
				+ (int) (decreaseImpression ^ (decreaseImpression >>> 32));
		result = prime * result
				+ (int) (increaseImpression ^ (increaseImpression >>> 32));
		result = prime * result
				+ (int) (lastResetDate ^ (lastResetDate >>> 32));
		return result;
	}

	/**
	 * transientメンバーを設定する。
	 * 量を変化させる前に呼ぶ必要がある。
	 *
	 * @param increaseMax
	 * @param decreaseMax
	 * @param resetPeriod
	 * @param createDate
	 * @param freePeriod
	 */
	public void init(long increaseMax, long decreaseMax, long resetPeriod,
			long createDate, long freePeriod) {
		this.resetPeriod = resetPeriod;
		this.createDate = createDate;
		this.freePeriod = freePeriod;
	}

	public boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (amount < 0) {
			b = false;
		}
		if (lastResetDate < 0) {
			b = false;
		}
		if (increaseImpression < 0) {
			b = false;
		}
		if (decreaseImpression > 0) {
			b = false;
		}
		if (!b) {
			r.add(Lang.PACE_LIMIT_AMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

}
