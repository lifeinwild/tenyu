package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import jdk.nashorn.internal.ir.debug.*;
import jetbrains.exodus.env.*;

/**
 * 負荷設定
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class LoadSetting implements Storable {
	/**
	 * シリアライズした時のデータ増加量の想定値
	 */
	private static final int toleranceSizeSerialize = 2000;

	public static int getToleranceSizeSerialize() {
		return toleranceSizeSerialize;
	}

	/**
	 * 反映負荷数値の合計の最大値
	 * 基本的に1メッセージのコストが1なので、2分あたり何件処理するか。
	 */
	private int userMessageListApplySizeMax = 1000 * 30;

	/**
	 * 想定される最低限の帯域。B/s
	 */
	private int bandwidthMin = 1000 * 2500;

	/**
	 * メッセージ１件あたりの最大サイズ
	 * longにしておかないといけない。
	 * {@link ObjectSizeCalculator#getObjectSize(Object)}がlongを返すが、
	 * long < int
	 * という比較を行うと、long値がint値にキャストされ、左辺の方が大きいのにtrueになる場合がある。
	 */
	private long messageSizeMax = 1000L * 600;

	/**
	 * 1メッセージリスト中の1ユーザー当たりのメッセージ件数
	 * サーバーの場合
	 */
	private int serverCountMax = 25;

	/**
	 * 1メッセージリスト中の1ユーザー当たりのメッセージ件数
	 * 全体運営者の場合
	 */
	private int userMessageCountMaxAdmin = 25;

	/**
	 * 1メッセージリスト中の1ユーザー当たりのメッセージ件数
	 * 作者の場合
	 */
	private int userMessageCountMaxAuthor = 50;

	/**
	 * 1メッセージリスト中の1ユーザー当たりのメッセージ件数
	 */
	private int userMessageCountMaxStandard = 5;

	/**
	 * 1個のユーザーメッセージリストの件数上限
	 * 全体運営者によって設定される
	 * 適切な数値は参加者のコンピューターおよび回線の標準的な性能に依存する
	 *
	 * 実際のメッセージの処理ペースによって調節する。
	 */
	//private long userMessageListCountMax = 1000L * 30;	userMessageListApplySizeMaxに変えた

	/**
	 * ヒストリーインデックスのずれをいくつまで許容するか
	 */
	private long userMessageListHistoryIndexTolerance = 3;

	/**
	 * 1個のユーザーメッセージリストのシリアライズ時の容量上限
	 * 全体運営者によって設定される
	 * 適切な数値は参加者のコンピューターおよび回線の標準的な性能に依存する
	 */
	private long userMessageListSizeMax = 1000L * 100 * 125;

	public int getAdminCountMax() {
		return userMessageCountMaxAdmin;
	}

	public int getUserMessageListApplySizeMax() {
		return userMessageListApplySizeMax;
	}

	public int getAuthorCountMax() {
		return userMessageCountMaxAuthor;
	}

	public int getBandwidthMin() {
		return bandwidthMin;
	}

	public long getMessageSizeMax() {
		return messageSizeMax;
	}

	public int getServerCountMax() {
		return serverCountMax;
	}

	public int getUserMessageListCountMaxStandard() {
		return userMessageCountMaxStandard;
	}

	public long getUserMessageListHistoryIndexTolerance() {
		return userMessageListHistoryIndexTolerance;
	}

	public long getUserMessageListSizeMax() {
		return userMessageListSizeMax;
	}

	public void setUserMessageListApplySizeMax(
			int userMessageListApplySizeMax) {
		this.userMessageListApplySizeMax = userMessageListApplySizeMax;
	}

	public void setBandwidthMin(int bandwidthMin) {
		this.bandwidthMin = bandwidthMin;
	}

	public void setMessageSizeMax(long messageSizeMax) {
		this.messageSizeMax = messageSizeMax;
	}

	public void setServerCountMax(int serverCountMax) {
		this.serverCountMax = serverCountMax;
	}

	public void setUserMessageListCountMaxAdmin(int userMessageCountMaxAdmin) {
		this.userMessageCountMaxAdmin = userMessageCountMaxAdmin;
	}

	public void setUserMessageListCountMaxAuthor(
			int userMessageCountMaxAuthor) {
		this.userMessageCountMaxAuthor = userMessageCountMaxAuthor;
	}

	public void setUserMessageListCountMaxStandard(
			int userMessageCountMaxStandard) {
		this.userMessageCountMaxStandard = userMessageCountMaxStandard;
	}

	public void setUserMessageListHistoryIndexTolerance(
			long userMessageListHistoryIndexTolerance) {
		this.userMessageListHistoryIndexTolerance = userMessageListHistoryIndexTolerance;
	}

	public void setUserMessageListSizeMax(long userMessageListSizeMax) {
		this.userMessageListSizeMax = userMessageListSizeMax;
	}

	/**
	 * @return	指定されたサイズを転送するのに必要な平均秒数
	 */
	public long transferTime(long size) {
		return size / getBandwidthMin();
	}

	public boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (userMessageListApplySizeMax < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_USERMESSAGELIST_APPLYSIZEMAX,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (bandwidthMin < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_BANDWIDTHMIN,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (messageSizeMax < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_MESSAGESIZEMAX,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (serverCountMax < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_SERVERCOUNTMAX,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (userMessageCountMaxAdmin < 0
				|| userMessageCountMaxAdmin > userMessageListApplySizeMax) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_USERMESSAGELIST_COUNTMAX_ADMIN,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (userMessageCountMaxAuthor < 0
				|| userMessageCountMaxAdmin > userMessageListApplySizeMax) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_USERMESSAGELIST_COUNTMAX_AUTHOR,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (userMessageCountMaxStandard < 0
				|| userMessageCountMaxAdmin > userMessageListApplySizeMax) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_USERMESSAGELIST_COUNTMAX_STANDARD,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (userMessageListSizeMax < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING_USERMESSAGELIST_SIZEMAX,
					Lang.ERROR_INVALID);
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
		result = prime * result + bandwidthMin;
		result = prime * result
				+ (int) (messageSizeMax ^ (messageSizeMax >>> 32));
		result = prime * result + serverCountMax;
		result = prime * result + userMessageCountMaxAdmin;
		result = prime * result + userMessageCountMaxAuthor;
		result = prime * result + userMessageCountMaxStandard;
		result = prime * result + userMessageListApplySizeMax;
		result = prime * result + (int) (userMessageListHistoryIndexTolerance
				^ (userMessageListHistoryIndexTolerance >>> 32));
		result = prime * result + (int) (userMessageListSizeMax
				^ (userMessageListSizeMax >>> 32));
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
		LoadSetting other = (LoadSetting) obj;
		if (bandwidthMin != other.bandwidthMin)
			return false;
		if (messageSizeMax != other.messageSizeMax)
			return false;
		if (serverCountMax != other.serverCountMax)
			return false;
		if (userMessageCountMaxAdmin != other.userMessageCountMaxAdmin)
			return false;
		if (userMessageCountMaxAuthor != other.userMessageCountMaxAuthor)
			return false;
		if (userMessageCountMaxStandard != other.userMessageCountMaxStandard)
			return false;
		if (userMessageListApplySizeMax != other.userMessageListApplySizeMax)
			return false;
		if (userMessageListHistoryIndexTolerance != other.userMessageListHistoryIndexTolerance)
			return false;
		if (userMessageListSizeMax != other.userMessageListSizeMax)
			return false;
		return true;
	}

}