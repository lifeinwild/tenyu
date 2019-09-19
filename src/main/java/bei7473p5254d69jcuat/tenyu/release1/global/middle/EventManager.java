package bei7473p5254d69jcuat.tenyu.release1.global.middle;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * イベントとリスナー一覧を扱う。
 * アプリ全体の様々な箇所からリスナーを登録し、
 * 同様に様々な箇所からイベントを受け取る。
 *
 * イベントの例：
 * ユーザーメッセージ反映シーケンスで反映されたメッセージ一覧
 * 分散合意の結果
 *
 * Middleを通じて永続化される。古いリスナーが登録されていてもタイムアウトしていれば実行されず除去される。
 *
 * イベントの種類はカテゴライズされておらず、
 * 発生した全てのイベントがすべてのリスナーに対して打診される。
 * 性能は悪いが完全に汎用的。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class EventManager {
	private List<Listener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * アプリの様々な箇所で実装し、そのオブジェクトをイベントマネージャーに登録する。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static abstract class Listener {
		private long createDate = System.currentTimeMillis();

		/**
		 * @param o
		 * @return	このイベントに反応するか
		 */
		abstract public boolean isSupport(Object ev);

		/**
		 * イベントに対する処理
		 * @param ev
		 */
		abstract public void run(Object ev);

		/**
		 * @return	何ミリ秒でタイムアウトするか
		 */
		abstract public long getTimeLimit();

		public boolean isTimeout() {
			long current = System.currentTimeMillis();
			long elapsed = current - createDate;
			return elapsed > getTimeLimit();
		}

		public long getCreateDate() {
			return createDate;
		}

		public void setCreateDate(long createDate) {
			this.createDate = createDate;
		}
	}

	public void fire(Object event) {
		Glb.debug("fired " + event.getClass().getSimpleName() + " " + event);
		if (event == null)
			return;
		List<Object> l = new ArrayList<>();
		l.add(event);
		fire(l);
	}

	/**
	 * 非同期。イベントを通知する
	 * @param events
	 */
	public void fire(List<?> events) {
		Glb.debug("fired");
		Glb.getExecutor().execute(() -> {
			for (Listener l : listeners) {
				if (l.isTimeout()) {
					remove(l);
				}
				for (Object o : events) {
					if (l.isSupport(o)) {
						l.run(o);
					}
				}
			}
		});
	}

	/**
	 * リスナーを削除する
	 * @param l	削除対象
	 * @return	削除されたか
	 */
	public boolean remove(Listener l) {
		return listeners.remove(l);
	}

	/**
	 * リスナーを登録する
	 * @param l	登録対象
	 * @return	登録されたか
	 */
	public boolean add(Listener l) {
		return listeners.add(l);
	}

	/**
	 * @return	読み取り専用リスナー一覧
	 */
	public List<Listener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}
}
