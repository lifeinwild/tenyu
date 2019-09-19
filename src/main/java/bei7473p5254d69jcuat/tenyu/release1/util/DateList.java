package bei7473p5254d69jcuat.tenyu.release1.util;

import org.apache.commons.collections4.queue.*;

/**
 * 日付リスト。
 * 何らかのタイミングで日付を追加していって、
 * そのタイミングの発生ペースを調べる場合に使う。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DateList {
	private CircularFifoQueue<Long> dates = new CircularFifoQueue<>(1000);

	public synchronized boolean add() {
		return dates.add(System.currentTimeMillis());
	}

	private CircularFifoQueue<Long> getDates() {
		return dates;
	}

	/**
	 * otherにthisの内容をコピー
	 * @param other
	 */
	public synchronized void copyTo(DateList other) {
		other.getDates().addAll(dates);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		DateList r = new DateList();
		copyTo(r);
		return r;
	}

	/**
	 * @return	1秒あたりいくつ追加されるか
	 */
	public synchronized double pace() {
		if (dates.size() == 0)
			return 0;
		long first = dates.get(0) / 1000;
		long current = System.currentTimeMillis() / 1000;
		return (double) dates.size() / (current - first);
	}
}