package bei7473p5254d69jcuat.tenyu.release1.global.objectivity;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * これはPaceLimitAmountが量的情報に関する権限者の不正行為を防止するアイデア
 * である事と対になるもので、非量的情報に関する不正行為を防止する。
 *
 * 権限者が設定を変更しても一定時間が経過しないと有効にならない。
 * その間に異常な設定を排除できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TimeActivationInfo {
	/**
	 * @return	作成日時。System.currentTimeMillis()
	 */
	long getCreateDate();

	/**
	 * @return	このオブジェクトは有効化されたか
	 */
	default boolean isActivated() {
		long period = Glb.getObje().getCore().getConfig().getActivatePeriod();
		long current = System.currentTimeMillis();
		long distance = current - getCreateDate();
		return distance > period;
	}
}