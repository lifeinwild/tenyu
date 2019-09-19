package bei7473p5254d69jcuat.tenyu.release1.util;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 粗い帯域制限。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <K>	equals, hashCodeを実装した何らかのクラス
 */
public class ThroughputLimit<K> {
	private static transient final List<
			ThroughputLimit<?>> instances = new CopyOnWriteArrayList<>();
	private static transient Thread t = null;

	public static void start() {
		long period = 1000L * 20;
		Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> {
			long currentDate = System.currentTimeMillis();
			for (ThroughputLimit<?> e : instances) {
				try {
					long dif = currentDate - e.getLastClearDate();
					if (dif > e.getClearPeriod()) {
						e.reset();
					}
				} catch (Exception e1) {
					Glb.getLogger().error("", e1);
				}
			}
		}, period, period, TimeUnit.MILLISECONDS);
	}

	public static void stop() {
		if (t != null)
			t.interrupt();
		t = null;
	}

	/**
	 * カウントされた帯域量をリセットする間隔
	 */
	private final long clearPeriod;

	/**
	 * 通信されるオブジェクトまたはそれに対応づくもの : 量
	 * 量はバイト数の場合もあるし、件数制限をしたい場合は件数の場合もある。
	 *
	 * keyToCountは可変長データだが、
	 * clearPeriodとmaxの指定によって最大件数が制限される。
	 */
	private Map<K, Long> keyToCount = new ConcurrentHashMap<>();

	/**
	 * 最後に帯域量がリセットされた日時
	 */
	private long lastClearDate = System.currentTimeMillis();

	/**
	 * 制限時間内にmax件を超えると制限される"可能性が生じる"。
	 * 最大で制限時間内にmaxの2倍までのスループットがありうる。
	 */
	private final long max;

	public ThroughputLimit() {
		this(1000L * 20, 1000L * 1000);
	}

	/**
	 * @param clearPeriod	定期的なクリアの周期
	 * @param max			最大件数
	 */
	public ThroughputLimit(long clearPeriod, long max) {
		this.clearPeriod = clearPeriod;
		this.max = max;
		instances.add(this);
	}

	public long getClearPeriod() {
		return clearPeriod;
	}

	public long getLastClearDate() {
		return lastClearDate;
	}

	/**
	 * @param key
	 * @return		最大件数を超えているか
	 */
	public boolean isOverCount(K key) {
		return isOverCount(key, 1L);
	}

	public boolean isOverCount(K key, long size) {
		return isOverCount(key, size, 1D);
	}

	/**
	 * 最大件数を超えているかチェックし、超えていなければ加算する
	 * @param key
	 * @param addCount	増加量
	 * @param maxMultiplier	maxをキー別に増加させるため
	 * @return		最大件数を超えているか
	 */
	public boolean isOverCount(K key, long addCount, double maxMultiplier) {
		if (key == null || addCount < 0)
			return true;

		Long count = keyToCount.get(key);
		if (count == null)
			count = 0L;

		if (count + addCount > max * maxMultiplier) {
			return true;
		}
		count += addCount;

		keyToCount.put(key, count);
		return false;

	}

	private void reset() {
		keyToCount.clear();
		lastClearDate = System.currentTimeMillis();
	}

	public long size() {
		return keyToCount.size();
	}
}
