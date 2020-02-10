package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 仮想通貨残高の管理や送金処理等。
 *
 * synchronizedが多いが単純な処理しかないので問題無い。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public strictfp class Wallet implements Storable {

	/**
	 * 初期残高
	 */
	private static final double balanceDefault = 0;
	/**
	 * 一週間の送金最大額の初期値
	 */
	private static final long limitDefault = 10000L;

	private static final int sendLogMax = 1000 * 50;

	/**
	 * 送金処理。
	 *
	 * @param sender
	 * @param receiver
	 * @param amount
	 * @param historyIndex
	 * @return
	 */
	public static boolean send(Wallet sender, Wallet receiver, long amount,
			long historyIndex) {
		//送金者から出金
		if (!sender.out(amount, historyIndex))
			return false;
		//受金者に入金
		if (!receiver.in(amount)) {
			//入金に失敗した場合、送金者に入金して元に戻す
			sender.in(amount);
			return false;
		}
		//送金成功
		return true;
	}

	/**
	 * 残高
	 * doubleになっているのは意図的。環境による計算結果の違いなど注意しないといけない。
	 * doubleであることで報酬分配において微小な報酬となっても振り込まれるので
	 * 例えば相互評価フローネットワークのフロー計算で格差を是正するような仕組みを入れる必要がなくなる。
	 */
	private  double balance = balanceDefault;

	/**
	 * 共同主体への寄付総額
	 * この数値をもとにある種の資格を与えることは、
	 * ダミーアカウント排除のために仕様設計において有効な選択肢になる場合がある。
	 */
	private double donationAmount = 0;

	/**
	 * 一週間の最大送金額
	 */
	private long limit = limitDefault;
	/**
	 * 最新の記録のみを残す
	 */
	private List<SendMoneyLog> sendMoneyLogs = new ArrayList<>();

	/**
	 * 古い送金記録を削除
	 */
	private synchronized void clearOldSendLog() {
		//猶予期間
		long period = Glb.getObje().getCore().getConfig()
				.getHistoryIndexWeekRough();
		for (int i = 0; i < sendMoneyLogs.size(); i++) {
			SendMoneyLog sendLog = sendMoneyLogs.get(i);
			//最新のヒストリーインデックスとこの送金記録が作成された時のヒストリーインデックスの差
			long distance = Glb.getObje().getCore().getHistoryIndex()
					- sendLog.getHistoryIndex();
			//古い記録だったら削除
			if (distance > period) {
				sendMoneyLogs.remove(i);
			}
		}
	}

	public synchronized long getAmountRecentlySend() {
		long total = 0;
		for (SendMoneyLog log : sendMoneyLogs)
			total += log.getAmount();
		return total;
	}

	public double getBalance() {
		return balance;
	}

	public long getLimit() {
		return limit;
	}

	public synchronized List<SendMoneyLog> getSendLogs() {
		return sendMoneyLogs;
	}

	/**
	 * 入金
	 * @param in	入金額
	 * @return		入金に成功したか
	 */
	public synchronized boolean in(long in) {
		if (in < 0)
			return false;
		balance += in;
		return true;
	}

	/**
	 * @return	初期値か
	 */
	public synchronized boolean isDefault() {
		if (balance != balanceDefault)
			return false;
		if (limit != limitDefault)
			return false;
		if (sendMoneyLogs.size() != 0)
			return false;
		return true;
	}

	/**
	 * 出金
	 * @param out				出金額
	 * @param historyIndex		この出金処理が行われたMessageListのid
	 * @return					出金に成功したか
	 */
	public synchronized boolean out(long out, Long historyIndex) {
		if (out < 0 || historyIndex == null || historyIndex < 0)
			return false;

		clearOldSendLog();

		//最近の送金総額が制限を超えていたら送金失敗
		if (getAmountRecentlySend() > limit)
			return false;

		balance -= out;
		return true;
	}

	public void setAmount(long amount) {
		this.balance = amount;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;

		if (balance < 0) {
			r.add(Lang.SOCIALITY_WALLET_BALANCE, Lang.ERROR_INVALID,
					"balance=" + balance);
			b = false;
		}
		if (limit < 0) {
			r.add(Lang.SOCIALITY_WALLET_LIMIT, Lang.ERROR_INVALID,
					"limit=" + limit);
			b = false;
		}
		if (sendMoneyLogs == null) {
			r.add(Lang.SOCIALITY_WALLET_SENDMONEYLOGS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			//動的設定と静的設定のうち大きい方を採用する
			int max = sendLogMax;
			if (sendLogMax < Glb.getObje().getCore().getConfig()
					.getHistoryIndexWeekRough())
				max = Glb.getObje().getCore().getConfig()
						.getHistoryIndexWeekRough();
			if (sendMoneyLogs.size() > max) {
				r.add(Lang.SOCIALITY_WALLET_SENDMONEYLOGS, Lang.ERROR_TOO_MANY,
						"size=" + sendMoneyLogs.size());
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
			if (sendMoneyLogs.size() != 0) {
				r.add(Lang.SOCIALITY_WALLET_SENDMONEYLOGS,
						Lang.ERROR_NOT_DEFAULT, "size=" + sendMoneyLogs.size());
				b = false;
			}
		}
		if (balance != balanceDefault) {
			r.add(Lang.SOCIALITY_WALLET_BALANCE, Lang.ERROR_NOT_DEFAULT,
					"balance=" + balance);
			b = false;
		}
		if (limit != limitDefault) {
			r.add(Lang.SOCIALITY_WALLET_LIMIT, Lang.ERROR_NOT_DEFAULT,
					"limit=" + limit);
			b = false;
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
			for (SendMoneyLog e : sendMoneyLogs) {
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
		for (SendMoneyLog e : sendMoneyLogs) {
			if (!e.validateReference(r, txn)) {
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
		long temp;
		temp = Double.doubleToLongBits(balance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (limit ^ (limit >>> 32));
		result = prime * result
				+ ((sendMoneyLogs == null) ? 0 : sendMoneyLogs.hashCode());
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
		Wallet other = (Wallet) obj;
		if (Double.doubleToLongBits(balance) != Double
				.doubleToLongBits(other.balance))
			return false;
		if (limit != other.limit)
			return false;
		if (sendMoneyLogs == null) {
			if (other.sendMoneyLogs != null)
				return false;
		} else if (!sendMoneyLogs.equals(other.sendMoneyLogs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Wallet [balance=" + balance + ", limit=" + limit
				+ ", sendMoneyLogs=" + sendMoneyLogs + "]";
	}

	public double getDonationAmount() {
		return donationAmount;
	}

	public void setDonationAmount(double donationAmount) {
		this.donationAmount = donationAmount;
	}
}
