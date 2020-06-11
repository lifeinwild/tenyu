package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;
import glb.*;

/**
 * GUI操作履歴
 * 検索機能つき
 *
 * {@link TenyuReferenceI}に依存していて、
 * それ以外の方法で作成されるGUIは履歴が残らない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuGuiHistory {
	/**
	 * 定期削除の周期
	 */
	private static final long deletePeriod = 1000 * 60;

	/**
	 * 履歴最大件数
	 */
	private static final int max = 1000 * 100;

	/**
	 * GUI操作履歴
	 * GUI上で新しいタブを開くたびに追加される。
	 * 古い履歴が自動的に削除されていく。
	 */
	private List<TenyuReferenceI> tabHistory = new ArrayList<>();

	/**
	 * 汎用検索インターフェース
	 * @param filter	任意の条件
	 * @return	検索結果
	 */
	public synchronized List<TenyuReferenceI> search(
			Function<TenyuReferenceI, Boolean> filter) {
		List<TenyuReferenceI> r = new ArrayList<>();
		for (TenyuReferenceI e : tabHistory) {
			if (filter.apply(e)) {
				r.add(e);
			}
		}
		return r;
	}

	/**
	 * 与えられた全ての検索条件に該当しない、かつモデルであるものを返す
	 * @param filters
	 * @return
	 */
	public synchronized List<TenyuReferenceI> searchIllegular(
			List<ModelCondition> filters) {
		return search(e -> {
			try {
				if (!(e instanceof TenyupediaObjectI)) {
					return false;
				}
				for (ModelCondition filter : filters) {
					if (filter.is((TenyupediaObjectI<ModelI>) e)) {
						return false;
					}
				}
				return true;
			} catch (Exception ex) {
				return false;
			}
		});
	}

	/**
	 * @param filter	任意のモデル条件。{@link User}に設定されているものを想定
	 * @return	検索結果
	 */
	public synchronized List<TenyuReferenceI> search(ModelCondition filter) {
		return search(e -> {
			try {
				if (!(e instanceof TenyupediaObjectI)) {
					return false;
				}
				return filter.is((TenyupediaObjectI<ModelI>) e);
			} catch (Exception ex) {
				return false;
			}
		});
	}

	/**
	 * 例外が発生するものを検索する
	 * @param filter		検索条件
	 * @return	検索結果
	 */
	public synchronized List<TenyuReferenceI> searchException(
			ModelCondition filter) {
		return search(e -> {
			try {
				if (!(e instanceof TenyupediaObjectI)) {
					return false;
				}
				filter.is((TenyupediaObjectI<ModelI>) e);
				return false;
			} catch (Exception ex) {
				Glb.getLogger().error("", ex);
				return true;
			}
		});
	}

	/**
	 * @return	モデルではないもの（機能系）を検索。
	 */
	public synchronized List<TenyuReferenceI> searchNotModel() {
		return search(e -> !(e instanceof TenyupediaObjectI));
	}

	/**
	 * @param historyElement	新しい履歴
	 * @return	追加されたか
	 */
	public synchronized boolean add(TenyuReferenceI historyElement) {
		if (tabHistory.size() >= max) {
			delete();
		}
		return tabHistory.add(historyElement);
	}

	/**
	 * 古い要素をいくつか削除する
	 */
	private synchronized void delete() {
		try {
			int size = tabHistory.size();
			int over = size - max;
			if (over <= 0) {
				return;
			}
			//まとめてこの件数削除する
			int deleteCount = 1000;
			if (over > deleteCount)
				deleteCount = over;
			if (deleteCount > size)
				deleteCount = size;
			//現在の最後の要素の番号
			int last = size - 1;
			//ここから削除
			int start = last - deleteCount;
			for (int count = 0; count < deleteCount; count++) {
				//同じ番号で削除すればいい
				tabHistory.remove(start);
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	/**
	 * まとめて取得
	 * @param startIndex	開始インデックス
	 * @param count			件数
	 * @return	その区間の履歴
	 */
	public synchronized List<TenyuReferenceI> get(int startIndex, int count) {
		List<TenyuReferenceI> r = new ArrayList<>();
		for (int i = startIndex; i < count; i++) {
			TenyuReferenceI e = tabHistory.get(i);
			if (e == null)
				break;
			r.add(e);
		}
		return r;
	}

	/**
	 * 定期削除処理の開始
	 */
	public void start() {
		Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> {
			delete();
			return;
		}, deletePeriod, deletePeriod, TimeUnit.MILLISECONDS);
	}
}
