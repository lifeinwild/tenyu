package glb.util;

import java.util.*;
import java.util.concurrent.*;

/**
 * レートの変化を扱うクラス
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RateTransition {
	/**
	 * 最大件数
	 */
	private int max = 200;
	/**
	 * 登録されたレート
	 */
	private Deque<Double> rates = new ConcurrentLinkedDeque<>();

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return	登録されたレートの件数
	 */
	public long size() {
		return rates.size();
	}

	/**
	 * 新しいレートを追加する
	 * @param rate
	 * @return
	 */
	public boolean add(Double rate) {
		if (size() >= max)
			return false;
		return rates.add(rate);
	}

	/**
	 * @return	平均レート
	 */
	public double ave() {
		double total = 0;
		for (Double d : rates)
			total += d;
		return total / size();
	}

	/**
	 * レートの低下傾向をダメージとして返す
	 * @return	ダメージ量。ダメージが大きいほどプラスに大きな値
	 */
	public double getDamage() {
		Double base = rates.getLast();
		double ave = ave();
		return ave - base;
	}

	/**
	 * 古いレートを削除
	 */
	public void halfReset() {
		for (int i = 0; i < size() / 2; i++) {
			rates.remove();
		}
	}
}