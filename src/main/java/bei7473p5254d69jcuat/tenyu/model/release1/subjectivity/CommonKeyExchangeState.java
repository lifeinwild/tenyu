package bei7473p5254d69jcuat.tenyu.model.release1.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 共通鍵交換はこの状態をセットアップする
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CommonKeyExchangeState implements Storable {
	private CommonKeyInfo commonKeyInfo = new CommonKeyInfo();
	/**
	 * 共通鍵交換の時に設定され、その後の確認処理で利用される。
	 * この情報をCommonKeyPackageで返せたら共通鍵交換に成功したことを確認できる。
	 * もし共通鍵やＩＶをこの情報の代わりに確認処理に使うと、
	 * それらの情報はＲＳＡの強度でしか通信されていないのに、
	 * 共通鍵の強度で通信されてしまう。共通鍵はできるだけ高速化したくて、強度をぎりぎりまで落としたい。
	 */
	protected byte[] confirmation;

	/**
	 * 共通鍵交換後のテスト通信に成功したか。
	 * それまで、得られた共通鍵が攻撃者が送りこんだものである可能性がある。
	 */
	private boolean succeed = false;

	/**
	 * 共通鍵交換に最後に成功した日時
	 */
	private long updateEnd = 0;

	/**
	 * 共通鍵交換が最後に開始された日時
	 */
	private long updateStart = 0;

	/**
	 * @return	更新可能なら更新開始日時を設定しtrueを返す
	 */
	public synchronized boolean checkAndSet() {
		if (!isUpdatable())
			return false;
		setSucceed(false);
		setUpdateStart();
		return true;
	}

	/**
	 * 共通鍵交換の通信シーケンスの最大時間
	 */
	public static long exchangeWaitMax = 1000L * 20;

	/**
	 * @return	共通鍵情報を交換中か
	 */
	public boolean isDuringExchange() {
		long elapsed = System.currentTimeMillis() - updateStart;
		if (elapsed > exchangeWaitMax)
			return false;
		return updateStart > updateEnd;
	}

	public CommonKeyExchangeState clone() {
		CommonKeyExchangeState r = new CommonKeyExchangeState();
		r.setConfirmation(getConfirmation());
		if (commonKeyInfo != null)
			r.setCommonKeyInfo(commonKeyInfo.clone());
		r.setUpdateStart(getUpdateStart());
		r.setUpdateEnd(getUpdateEnd());
		r.setSucceed(isUpdatable());
		return r;
	}

	/**
	 * 共通鍵の状態をリセットする。日時系はリセットされない。
	 */
	public void commonKeyReset() {
		setSucceed(false);
		setConfirmation(null);
		if (commonKeyInfo != null)
			commonKeyInfo.reset();
	}

	public CommonKeyInfo getCommonKeyInfo() {
		return commonKeyInfo;
	}

	public byte[] getConfirmation() {
		return confirmation;
	}

	public long getUpdateEnd() {
		return updateEnd;
	}

	public long getUpdateStart() {
		return updateStart;
	}

	public boolean isSucceed() {
		return succeed;
	}

	/**
	 * 最近更新を試みたものをまた更新するのを防ぐ。
	 * さらに、2ノードの間で共通鍵交換が発生するが、両方同時に交換リクエストを
	 * する状況が発生する頻度を減らせる。完全には無くせない。
	 * @return 前回の更新から十分に時間が経ったか
	 */
	public boolean isUpdatable() {
		long elapsed = System.currentTimeMillis() - getUpdateStart();
		return elapsed > 1000L * 60 * 3;
	}

	public void setCommonKeyInfo(CommonKeyInfo commonKeyInfo) {
		this.commonKeyInfo = commonKeyInfo;
	}

	public void setConfirmation(byte[] confirmation) {
		this.confirmation = confirmation;
	}

	public void setSucceed(boolean succeed) {
		this.succeed = succeed;
	}

	public void setUpdateEnd() {
		this.updateEnd = System.currentTimeMillis();
	}

	public void setUpdateEnd(long updateEnd) {
		this.updateEnd = updateEnd;
	}

	public void setUpdateStart() {
		this.updateStart = System.currentTimeMillis();
	}

	public void setUpdateStart(long updateStart) {
		this.updateStart = updateStart;
	}

	public boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (!commonKeyInfo.validateAtCreate(r)) {
			b = false;
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
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commonKeyInfo == null) ? 0 : commonKeyInfo.hashCode());
		result = prime * result + Arrays.hashCode(confirmation);
		result = prime * result + (succeed ? 1231 : 1237);
		result = prime * result + (int) (updateEnd ^ (updateEnd >>> 32));
		result = prime * result + (int) (updateStart ^ (updateStart >>> 32));
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
		CommonKeyExchangeState other = (CommonKeyExchangeState) obj;
		if (commonKeyInfo == null) {
			if (other.commonKeyInfo != null)
				return false;
		} else if (!commonKeyInfo.equals(other.commonKeyInfo))
			return false;
		if (!Arrays.equals(confirmation, other.confirmation))
			return false;
		if (succeed != other.succeed)
			return false;
		if (updateEnd != other.updateEnd)
			return false;
		if (updateStart != other.updateStart)
			return false;
		return true;
	}
}
