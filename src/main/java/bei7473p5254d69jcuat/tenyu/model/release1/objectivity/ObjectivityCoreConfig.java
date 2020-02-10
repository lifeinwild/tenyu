package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 議決で設定可能な客観コアの値
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityCoreConfig implements Storable {
	/**
	 * 権限者によって作成または更新される非量的情報は
	 * この期間が過ぎてから有効になる。
	 * ミリ秒。全体運営者によって設定可能
	 *
	 * 例えばファイルアップロードが承認されたとして、
	 * 実際にP2Pネットワークに拡散されていくのは、
	 * この数値が1000に設定されていたら承認された時からヒストリーインデックスが
	 * +1000されてから。その間に承認されたファイルが妥当か誰もがチェックできるし、
	 * 妥当でないなら全体運営者は承認を取り消せる。
	 * 非量的情報と言っているのは、PaceLimitAmountが量的情報に関する
	 * 権限者による不正行為を防止するためにあり、
	 * 残りの非量的情報に関する防止策ということ。
	 *
	 * 全体運営者は、そのような非量的情報の規模と審査ペースを鑑みて
	 * この数値を設定しなければならない。
	 */
	private long activatePeriod = 1000L * 60 * 60 * 24 * 2;

	/**
	 * ヒストリーインデックスの単位。
	 * 様々な処理で利用される。
	 *
	 * 理想的には1日でヒストリーインデックスがいくつ進むかという数値だが、
	 * 必ずしも1日の進行量と一致するわけではない。例えばアップデート等で
	 * 更新が停止した場合など。
	 *
	 * 最初は極端に低い数値を設定しておく。
	 * この数値は全体運営者によって随時一週間を意味する程度に設定される。
	 *
	 * 本プログラムは単純にミリ秒等を用いれない場合があるのでこのような値が必要になる。
	 * 全ノードで完全一致する日時的情報はヒストリーインデックスくらいしかない。
	 */
	private int historyIndexDayRough = 100;//720

	/**
	 * 1ヒストリーインデックスが何ミリ秒か
	 * TODO:現状変更不可。UserMessageListSequenceの実行間隔が静的だから。
	 * crontabの記述をこの設定値に連動させれたら変更可能になる。
	 */
	private int historyIndexMillis = 1000 * 60 * 2;

	/**
	 * 全体運営者が設定する各種負荷に影響する設定値
	 */
	private LoadSetting loadSetting = new LoadSetting();

	/**
	 * 毎週共同主体の所持金の何％を分配するか
	 */
	private double sharingRate = 0.2D;

	/**
	 * ユーザー登録を受け付けるか。
	 * 全体運営者によって一時停止できる。
	 * 次々と機械的にユーザー登録される状況になった場合、
	 * 一旦登録を停止して、不正な紹介者を全てBANした後、登録を再開する。
	 */
	private boolean userRegistrationActivate = true;

	/**
	 * 抽象ノードの全エッジの重みの変化ペースの最大
	 * 割合
	 */
	private double edgeChangePaceAbstractNominal = 0.1;

	/**
	 * デフォルトの重み変化の無制限期間
	 */
	private long edgeChangePaceFreePeriodDefault = 700 * 90;

	private long edgeChangePaceIncreaseMaxDefault = Edge.weightMax;
	private long edgeChangePaceDecreaseMaxDefault = -Edge.weightMax;
	/**
	 * 多くのノードタイプのエッジ最大数
	 */
	private int edgeCountMaxDefault = 2000;

	public synchronized ObjectivityCoreConfig clone() {
		try {
			byte[] serialized = Glb.getUtil().toKryoBytesForPersistence(this);
			return (ObjectivityCoreConfig) Glb.getUtil()
					.fromKryoBytesForPersistence(serialized);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
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
		ObjectivityCoreConfig other = (ObjectivityCoreConfig) obj;
		if (activatePeriod != other.activatePeriod)
			return false;
		if (historyIndexDayRough != other.historyIndexDayRough)
			return false;
		if (historyIndexMillis != other.historyIndexMillis)
			return false;
		if (loadSetting == null) {
			if (other.loadSetting != null)
				return false;
		} else if (!loadSetting.equals(other.loadSetting))
			return false;
		if (Double.doubleToLongBits(sharingRate) != Double
				.doubleToLongBits(other.sharingRate))
			return false;
		if (userRegistrationActivate != other.userRegistrationActivate)
			return false;
		return true;
	}

	public long getActivatePeriod() {
		return activatePeriod;
	}

	public double getEdgeChangePaceAbstractNominal() {
		return edgeChangePaceAbstractNominal;
	}

	public long getEdgeChangePaceDecreaseMaxDefault() {
		return edgeChangePaceDecreaseMaxDefault;
	}

	public long getEdgeChangePaceFreePeriodDefault() {
		return edgeChangePaceFreePeriodDefault;
	}

	public long getEdgeChangePaceIncreaseMaxDefault() {
		return edgeChangePaceIncreaseMaxDefault;
	}

	public int getEdgeCountMaxDefault() {
		return edgeCountMaxDefault;
	}

	public int getHistoryIndexDayRough() {
		return historyIndexDayRough;
	}

	public int getHistoryIndexMillis() {
		return historyIndexMillis;
	}

	public int getHistoryIndexWeekRough() {
		return historyIndexDayRough * 7;
	}

	public LoadSetting getLoadSetting() {
		return loadSetting;
	}

	public double getSharingRate() {
		return sharingRate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (activatePeriod ^ (activatePeriod >>> 32));
		result = prime * result + historyIndexDayRough;
		result = prime * result + historyIndexMillis;
		result = prime * result
				+ ((loadSetting == null) ? 0 : loadSetting.hashCode());
		long temp;
		temp = Double.doubleToLongBits(sharingRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (userRegistrationActivate ? 1231 : 1237);
		return result;
	}

	public boolean isUserRegistrationActivate() {
		return userRegistrationActivate;
	}

	public void setActivatePeriod(long activatePeriod) {
		this.activatePeriod = activatePeriod;
	}

	public void setEdgeChangePaceAbstractNominal(
			double edgeChangePaceAbstractNominal) {
		this.edgeChangePaceAbstractNominal = edgeChangePaceAbstractNominal;
	}

	public void setEdgeChangePaceDecreaseMaxDefault(
			long edgeChangePaceDecreaseMaxDefault) {
		this.edgeChangePaceDecreaseMaxDefault = edgeChangePaceDecreaseMaxDefault;
	}

	public void setEdgeChangePaceFreePeriodDefault(
			long edgeChangePaceFreePeriodDefault) {
		this.edgeChangePaceFreePeriodDefault = edgeChangePaceFreePeriodDefault;
	}

	public void setEdgeChangePaceIncreaseMaxDefault(
			long edgeChangePaceIncreaseMaxDefault) {
		this.edgeChangePaceIncreaseMaxDefault = edgeChangePaceIncreaseMaxDefault;
	}

	public void setEdgeCountMaxDefault(int edgeCountMaxDefault) {
		this.edgeCountMaxDefault = edgeCountMaxDefault;
	}

	public void setHistoryIndexDayRough(int historyIndexDayRough) {
		this.historyIndexDayRough = historyIndexDayRough;
	}

	public synchronized void setHistoryIndexMillis(int historyIndexMillis) {
		this.historyIndexMillis = historyIndexMillis;
	}

	public void setLoadSetting(LoadSetting loadSetting) {
		this.loadSetting = loadSetting;
	}

	public synchronized void setSharingRate(double sharingRate) {
		this.sharingRate = sharingRate;
	}

	public void setUserRegistrationActivate(boolean userRegistrationActivate) {
		this.userRegistrationActivate = userRegistrationActivate;
	}

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (activatePeriod < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_ACTIVATE_PERIOD,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (historyIndexDayRough < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_HISTORYINDEXDAYROUGH,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (historyIndexMillis < 0) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_HISTORYINDEXMILLIS,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (loadSetting == null) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_LOADSETTING, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!loadSetting.validateAtCommon(vr)) {
				b = false;
			}
		}
		if (sharingRate < 0 || sharingRate > 1D) {
			vr.add(Lang.OBJECTIVITY_CORE_CONFIG_SHARINGRATE,
					Lang.ERROR_INVALID);
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	public String toString() {
		return "ObjectivityCoreConfig [activatePeriod=" + activatePeriod
				+ ", historyIndexDayRough=" + historyIndexDayRough
				+ ", historyIndexMillis=" + historyIndexMillis
				+ ", loadSetting=" + loadSetting + ", sharingRate="
				+ sharingRate + ", userRegistrationActivate="
				+ userRegistrationActivate + ", edgeChangePaceAbstractNominal="
				+ edgeChangePaceAbstractNominal
				+ ", edgeChangePaceFreePeriodDefault="
				+ edgeChangePaceFreePeriodDefault
				+ ", edgeChangePaceIncreaseMaxDefault="
				+ edgeChangePaceIncreaseMaxDefault
				+ ", edgeChangePaceDecreaseMaxDefault="
				+ edgeChangePaceDecreaseMaxDefault + ", edgeCountMaxDefault="
				+ edgeCountMaxDefault + "]";
	}

}