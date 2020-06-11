package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * Tenyupediaの通知モジュール
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Tenyupedia {
	/**
	 * １回で処理される通知対象の最大数
	 * 多すぎるとメモリを圧迫するので
	 *
	 * Middleにおいている事、Xodusに１レコードあたりのサイズ制限８MBがある事から、
	 * Tenyupediaインスタンスのサイズはある程度制限され、
	 * この最大値も制限される。
	 */
	private static final long candidatesMax = 1000 * 2;

	/**
	 * updateに伴って処理される最大件数
	 */
	private static final long procUnit = 1000;

	/**
	 * 次にチェックすべき判定候補
	 * ここから検索条件で該当したものが通知される
	 */
	private ConcurrentLinkedQueue<
			Candidate> candidates = new ConcurrentLinkedQueue<>();

	/**
	 * メモリを圧迫しないようにcandidatesに上限を作るためにある
	 * ある程度粗い制限でいい
	 * だからスレッドセーフにする必要もあまりない
	 */
	private long candidatesSizeRough;

	/**
	 * 通知一覧
	 *
	 * このユーザーが設定している検索条件に該当したもの
	 */
	private List<Notification> notifications = new ArrayList<>();

	public boolean addCandidate(TenyupediaObjectI<? extends ModelI> ref) {
		return addCandidate(ref, false);
	}

	/**
	 * store系インターフェースを妨害してはいけないので、synchronized不可
	 *
	 * 次にチェックすべき判定候補を追加する。
	 * このユーザーが設定している検索条件に該当した場合、通知される。
	 * モデルが作成または更新された時に追加する。
	 *
	 * 例外を投げない事が保証されるので呼び出し元の失敗率を引き上げない。
	 *
	 * @param ref	追加される判定候補
	 * @param forcibly	通知件数が上限に達していても追加する
	 * @return	追加されたか
	 */
	public boolean addCandidate(TenyupediaObjectI<? extends ModelI> ref,
			boolean forcibly) {
		try {
			if (!forcibly && candidatesSizeRough > candidatesMax)
				return false;
			Candidate c = new Candidate();
			c.setObj(ref);
			if (candidates.add(c)) {
				candidatesSizeRough++;
				return true;
			}
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenyupedia other = (Tenyupedia) obj;
		if (candidates == null) {
			if (other.candidates != null)
				return false;
		} else if (!candidates.equals(other.candidates))
			return false;
		if (candidatesSizeRough != other.candidatesSizeRough)
			return false;
		if (notifications == null) {
			if (other.notifications != null)
				return false;
		} else if (!notifications.equals(other.notifications))
			return false;
		return true;
	}

	/**
	 * 全通知から一部を抜き出す
	 *
	 * @param start	この位置から
	 * @param n	件数
	 * @param view GUI表示するか
	 * @return	通知リスト
	 */
	public synchronized List<Notification> get(int start, int n, boolean view) {
		List<Notification> r = new ArrayList<>();
		for (int i = start; i < start + n; i++) {
			try {
				Notification notifi = notifications.get(i);
				if (notifi == null)
					continue;
				r.add(notifi);
				if (view)
					notifi.view();
			} catch (Exception e) {
			}
		}
		return r;
	}

	public List<Long> getMyModelConditionIds() {
		User me = Glb.getMiddle().getMe();
		if (me == null)
			return new ArrayList<>();
		return me.getModelConditionIds();
	}

	public List<ModelCondition> getMyModelConditions(List<Long> mcIds,
			Transaction txn) {
		ModelConditionStore mcs = new ModelConditionStore(txn);
		return mcs.get(mcIds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((candidates == null) ? 0 : candidates.hashCode());
		result = prime * result
				+ (int) (candidatesSizeRough ^ (candidatesSizeRough >>> 32));
		result = prime * result
				+ ((notifications == null) ? 0 : notifications.hashCode());
		return result;
	}

	@Override
	public synchronized String toString() {
		return "Tenyupedia [candidatesSizeRough=" + candidatesSizeRough
				+ ", notifications.size()=" + notifications.size() + "]";
	}

	/**
	 * 判定候補を判定し、該当した候補を通知に追加する。
	 * 通知済みの古い通知を削除する。
	 *
	 * @param procTimeMaxMillis	呼び出すたびに最長でこの時間だけ処理する。
	 */
	public synchronized void update(long procTimeMaxMillis) {
		Glb.getObje().readTryW(txn -> {
			//自分が設定している全検索条件のリストを取得
			List<ModelCondition> mcL = getMyModelConditions(
					getMyModelConditionIds(), txn);
			if (mcL == null || mcL.size() == 0)
				return null;

			candidatesSizeRough = candidates.size();

			Glb.getUtil().proc(procTimeMaxMillis / 2, () -> {
				if (candidatesSizeRough <= 0)
					return false;
				//全候補について
				for (long i = 0; i < procUnit; i++) {
					Candidate c = candidates.poll();
					if (c == null)
						return false;//proc中止
					candidatesSizeRough--;

					//cが該当する検索条件mcIdを探す
					Long mcId = null;
					for (ModelCondition mc : mcL) {
						if (mc.is(c.getObj())) {
							mcId = mc.getId();
							break;
						}
					}
					//該当する検索条件が無かったら通知に追加しない
					if (mcId == null)
						continue;

					//通知に追加
					if (!notifications.add(new Notification(mcId, c))) {
						Glb.getLogger().warn("", new Exception(
								"Failed to add to notifications"));
						return false;
					}
				}

				//proc続行
				return true;
			});

			//通知更新処理終了
			return null;
		});

		Glb.getUtil().proc(procTimeMaxMillis / 2, () -> {
			if (notifications.size() == 0)
				return false;
			int index = 0;
			for (int loopCount = 0; loopCount < procUnit; loopCount++) {
				Notification n = notifications.get(index);
				if (n == null)
					return false;
				if (n.isDeletable()) {
					//add(int i)とかremoveが他で行われない事で成立する
					//その前提が無いとindexが指定するオブジェクトがnじゃなくなる
					Notification removed = notifications.remove(index);
					if (removed != n) {
						Glb.getLogger().error("",
								new Exception("removed != n"));
					}
				}
			}
			return true;
		});
	}

	private static class Candidate {
		private TenyupediaObjectI<? extends ModelI> obj;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Candidate other = (Candidate) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			return true;
		}

		public TenyupediaObjectI<? extends ModelI> getObj() {
			return obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			return result;
		}

		public void setObj(TenyupediaObjectI<? extends ModelI> obj) {
			this.obj = obj;
		}
	}

	/**
	 * 検索条件と、それに該当したオブジェクト、
	 * 最初の表示日時を含む。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class Notification {
		/**
		 * 削除されるまでの期間
		 * ２週間
		 */
		private static final long deleteTime = 1000 * 60 * 60 * 24 * 7 * 2;

		public static long getDeletetime() {
			return deleteTime;
		}

		/**
		 * どの検索条件がこのobjを該当としたか
		 */
		private Long modelConditionId;

		/**
		 * 通知対象
		 */
		private TenyupediaObjectI<? extends ModelI> obj;

		/**
		 * 最初に表示した日時
		 * 0未満なら未表示
		 */
		private long viewDate = -1L;

		public Notification() {
		}

		public Notification(Long modelConditionId, Candidate c) {
			obj = c.getObj();
			this.modelConditionId = modelConditionId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Notification other = (Notification) obj;
			if (modelConditionId == null) {
				if (other.modelConditionId != null)
					return false;
			} else if (!modelConditionId.equals(other.modelConditionId))
				return false;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (viewDate != other.viewDate)
				return false;
			return true;
		}

		public Long getModelConditionId() {
			return modelConditionId;
		}

		public TenyupediaObjectI<? extends ModelI> getObj() {
			return obj;
		}

		public long getViewDate() {
			return viewDate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((modelConditionId == null) ? 0
					: modelConditionId.hashCode());
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			result = prime * result + (int) (viewDate ^ (viewDate >>> 32));
			return result;
		}

		/**
		 * @return	削除すべきか
		 */
		public boolean isDeletable() {
			return isOld() && isViewed();
		}

		/**
		 * @return	削除期間を過ぎたか
		 */
		public boolean isOld() {
			long now = Glb.getUtil().getEpochMilli();
			return (now - viewDate) > deleteTime;
		}

		public boolean isViewed() {
			return viewDate > 0;
		}

		public void setModelConditionId(Long modelConditionId) {
			this.modelConditionId = modelConditionId;
		}

		public void setObj(TenyupediaObjectI<? extends ModelI> obj) {
			this.obj = obj;
		}

		public void setViewDate(long viewDate) {
			this.viewDate = viewDate;
		}

		@Override
		public String toString() {
			return "Notification [modelConditionId=" + modelConditionId
					+ ", obj=" + obj + ", viewDate=" + viewDate + "]";
		}

		public boolean view() {
			if (viewDate >= 0)
				return false;
			setViewDate(Glb.getUtil().getEpochMilli());
			return true;
		}
	}

}
