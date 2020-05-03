package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.tenyupedia.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class ModelCondition extends IndividualityObject
		implements ModelConditionI {

	private How how = new How();

	private What what = new What();

	private When when = new When();

	private Where where = new Where();

	private Who who = new Who();

	@Override
	public List<Long> getTagIds() {
		HashSet<Long> r = new HashSet<>();
		r.addAll(super.getTagIds());
		r.addAll(what.getTagIds());
		r.addAll(who.getTagIds());
		return new ArrayList<>(r);
	}

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return null;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix) {
		return new ModelConditionGui(guiName, cssIdPrefix);
	}

	/**
	 * @return	他の条件との関係性
	 */
	public How getHow() {
		return how;
	}

	@Override
	public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new ModelConditionStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.MODEL_CONDITION;
	}

	/**
	 * @return	この条件が該当する対象の条件
	 */
	public What getWhat() {
		return what;
	}

	/**
	 * @return	この条件が該当する時間条件
	 */
	public When getWhen() {
		return when;
	}

	/**
	 * @return	この条件が該当する{@link SocialityI}的な条件
	 * 相互評価フローネットワークがネットワーク上の位置を提供しうることから、
	 * それをwhereと解釈している。
	 */
	public Where getWhere() {
		return where;
	}

	/**
	 * @return	この条件が該当する{@link UserI}的な条件
	 */
	public Who getWho() {
		return who;
	}

	/**
	 * @return	何のために
	 */
	public Why getWhy() {
		return Glb.getObje().getUser(us -> {
			return null;//TODO 通知システムの実装後
		});
	}

	/**
	 * これだけオブジェクトの状態として保持せず
	 * {@link User}に保存された通知条件から検索された{@link User}が
	 * その状態となる。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static final class Why implements ModelConditionElementI {
		private List<Long> userIds;
		public static final long userIdsMax = 1000 * 1000 * 1000 * 10;

		public void setUserIds(List<Long> userIds) {
			this.userIds = userIds;
		}

		public List<Long> getUserIds() {
			return userIds;
		}

		@Override
		public boolean is(ModelI m) {
			//TODO 通知システムを通してmとuserIdsの関係を判定する
			//とはいえこのメソッドを呼び出す事は無さそう
			throw new UnsupportedOperationException("");
		}

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (userIds == null) {
				r.add(Lang.WHY, Lang.USER_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (userIds.size() > userIdsMax) {
					r.add(Lang.WHY, Lang.USER_IDS, Lang.ERROR_TOO_MANY,
							"size=" + userIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(userIds)) {
						r.add(Lang.WHY, Lang.USER_IDS, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
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
	}

	@Override
	public boolean is(ModelI m) {
		if (when != null) {
			if (!when.is(m)) {
				return false;
			}
		}
		return true;
	}

	public void setHow(How how) {
		this.how = how;
	}

	public void setWhat(What what) {
		this.what = what;
	}

	public void setWhen(When when) {
		this.when = when;
	}

	public void setWhere(Where where) {
		this.where = where;
	}

	public void setWho(Who who) {
		this.who = who;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (how == null) {
			r.add(Lang.MODEL_CONDITION, Lang.HOW, Lang.ERROR_EMPTY);
			b = false;
		}
		if (what == null) {
			r.add(Lang.MODEL_CONDITION, Lang.WHAT, Lang.ERROR_EMPTY);
			b = false;
		}
		if (when == null) {
			r.add(Lang.MODEL_CONDITION, Lang.WHEN, Lang.ERROR_EMPTY);
			b = false;
		}
		if (where == null) {
			r.add(Lang.MODEL_CONDITION, Lang.WHERE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (who == null) {
			r.add(Lang.MODEL_CONDITION, Lang.HOW, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			if (!how.validateAtCreate(r)) {
				b = false;
			}
			if (!what.validateAtCreate(r)) {
				b = false;
			}
			if (!when.validateAtCreate(r)) {
				b = false;
			}
			if (!where.validateAtCreate(r)) {
				b = false;
			}
			if (!who.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			if (!how.validateAtUpdate(r)) {
				b = false;
			}
			if (!what.validateAtUpdate(r)) {
				b = false;
			}
			if (!when.validateAtUpdate(r)) {
				b = false;
			}
			if (!where.validateAtUpdate(r)) {
				b = false;
			}
			if (!who.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (!how.validateReference(r, txn)) {
			b = false;
		}
		if (!what.validateReference(r, txn)) {
			b = false;
		}
		if (!when.validateReference(r, txn)) {
			b = false;
		}
		if (!where.validateReference(r, txn)) {
			b = false;
		}
		if (!who.validateReference(r, txn)) {
			b = false;
		}
		return b;
	}

	/**
	 * 各要素は{@link Logic}で積、和、否定などが制御される。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class How implements ModelConditionElementI {
		/**
		 * 他の条件リスト
		 * 和集合または積集合など集合演算を指定できる
		 */
		private Map<Logic, Long> otherConditionIds = new HashMap<>();

		private static final int otherConditionIdsMax = 2500;

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (otherConditionIds == null) {
				r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
						Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (otherConditionIds.size() > otherConditionIdsMax) {
					r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
							Lang.ERROR_TOO_MANY,
							"size=" + otherConditionIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(otherConditionIds.values())) {
						r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
								Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			} else {
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			}
			return b;
		}

		public static int getOtherconditionidsmax() {
			return otherConditionIdsMax;
		}

		@Override
		public boolean validateReference(ValidationResult r, Transaction txn)
				throws Exception {
			boolean b = true;
			ModelConditionStore mcs = new ModelConditionStore(txn);
			for (Long cId : otherConditionIds.values()) {
				ModelCondition mc = mcs.get(cId);
				if (mc == null) {
					r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
							Lang.ERROR_DB_NOTFOUND_REFERENCE, "cId=" + cId);
					b = false;
					break;
				}
			}
			return b;
		}

		private boolean checkOtherCondition(ModelI m) {
			if (otherConditionIds == null)
				return true;
			return Glb.getObje().getModelCondition(mcs -> {
				for (Entry<Logic, Long> e : otherConditionIds.entrySet()) {
					ModelCondition mc = mcs.get(e.getValue());
					if (mc == null)
						continue;
					switch (e.getKey()) {
					case AND:
						if (!mc.is(m))
							return false;
						break;
					case NAND:
						if (mc.is(m))
							return false;
						break;
					case OR:
						if (mc.is(m))
							return true;
						break;
					case NOR:
						if (!mc.is(m))
							return true;
						break;
					default:
					}
				}
				return true;
			});

		}

		@Override
		public boolean is(ModelI m) {
			if (m == null)
				return false;

			return checkOtherCondition(m);
		}

		/**
		 * 実装上NOTよりNOR＋NANDの方が自然だったのでこうした。
		 *
		 * @author exceptiontenyu@gmail.com
		 *
		 */
		public static enum Logic {
			AND(1), NAND(2), NOR(3), OR(4);
			private int id;

			private Logic(int id) {
				this.id = id;
			}

			public int getId() {
				return id;
			}
		}

		public Map<Logic, Long> getOtherConditionIds() {
			return otherConditionIds;
		}

		public void setOtherConditionIds(Map<Logic, Long> otherConditionIds) {
			this.otherConditionIds = otherConditionIds;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((otherConditionIds == null) ? 0
					: otherConditionIds.hashCode());
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
			How other = (How) obj;
			if (otherConditionIds == null) {
				if (other.otherConditionIds != null)
					return false;
			} else if (!otherConditionIds.equals(other.otherConditionIds))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "How [otherConditionIds=" + otherConditionIds + "]";
		}

	}

	/**
	 * 各要素は和集合。つまり条件を追加するほど該当するものが増える。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class What implements ModelConditionElementI {
		/**
		 * 任意のものを具体的に指定して追加できる。
		 * ここに指定されたものは他の条件に関係無く常に検索結果に含められる。
		 *
		 * このオブジェクトが通知システムで利用される事から、
		 * これはこの検索条件の管理者による手動の通知を実現する。
		 * 設定時、それが通知済みでなかった場合、通知される。
		 */
		private List<
				TenyuReference<? extends ModelI>> manual = new ArrayList<>();

		/**
		 * この中のいずれかのモデルである事
		 * store名だが実質モデル名
		 */
		private List<StoreName> storeNames = new ArrayList<>();

		/**
		 * 対象がこの中のいずれかのタグを持つ事
		 */
		private List<Long> tagIds = new ArrayList<>();

		private boolean checkManual(ModelI m) {
			if (manual == null)
				return true;
			for (TenyuReference<?> r : manual) {
				if (m.getStoreName().equals(r.getStoreName())
						&& m.getId().equals(r.getId())) {
					return true;
				}
			}
			return false;
		}

		private boolean checkModel(ModelI m) {
			if (storeNames == null)
				return false;
			for (StoreName sn : storeNames) {
				String mn = sn.getModelName();
				if (m.getClass().getSimpleName().equals(mn)) {
					return true;
				}
			}
			return false;
		}

		private boolean checkTagIds(ModelI m) {
			if (tagIds == null)
				return true;
			if (!(m instanceof IndividualityObjectI)) {
				return false;
			}
			IndividualityObjectI i = (IndividualityObjectI) m;
			for (Long tagId : tagIds) {
				for (Long mTagId : i.getTagIds()) {
					if (tagId.equals(mTagId)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean is(ModelI m) {
			if (m == null)
				return false;

			if (checkModel(m))
				return false;
			if (!checkManual(m))
				return false;
			if (!checkTagIds(m))
				return false;
			return true;
		}

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (manual == null) {
				r.add(Lang.WHAT, Lang.MODEL_CONDITION_MANUAL, Lang.ERROR_EMPTY);
				b = false;
			}
			if (storeNames == null) {
				r.add(Lang.WHAT, Lang.MODEL_NAME, Lang.ERROR_EMPTY);
				b = false;
			}
			if (tagIds == null) {
				r.add(Lang.WHAT, Lang.TAG_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (tagIds.size() > tagIdsMax) {
					r.add(Lang.WHAT, Lang.TAG_IDS, Lang.ERROR_TOO_MANY,
							"size=" + tagIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(tagIds)) {
						r.add(Lang.WHAT, Lang.TAG_IDS, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			} else {
				for (TenyuReference<? extends ModelI> e : manual) {
					if (!e.validateAtCreate(r)) {
						b = false;
						break;
					}
				}
				for (StoreName e : storeNames) {
					if (!e.validateAtCreate(r)) {
						b = false;
						break;
					}
				}
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			} else {
				for (TenyuReference<? extends ModelI> e : manual) {
					if (!e.validateAtUpdate(r)) {
						b = false;
						break;
					}
				}
				for (StoreName e : storeNames) {
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
			for (TenyuReference<? extends ModelI> e : manual) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
			for (StoreName e : storeNames) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
			return b;
		}

		public List<TenyuReference<?>> getManual() {
			return manual;
		}

		public void setManual(List<TenyuReference<?>> manual) {
			this.manual = manual;
		}

		public List<StoreName> getStoreNames() {
			return storeNames;
		}

		public void setModelNames(List<StoreName> modelNames) {
			this.storeNames = modelNames;
		}

		public List<Long> getTagIds() {
			return tagIds;
		}

		public void setTagIds(List<Long> tagIds) {
			this.tagIds = tagIds;
		}

		@Override
		public String toString() {
			return "What [manual=" + manual + ", storeNames=" + storeNames
					+ ", tagIds=" + tagIds + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((manual == null) ? 0 : manual.hashCode());
			result = prime * result
					+ ((storeNames == null) ? 0 : storeNames.hashCode());
			result = prime * result
					+ ((tagIds == null) ? 0 : tagIds.hashCode());
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
			What other = (What) obj;
			if (manual == null) {
				if (other.manual != null)
					return false;
			} else if (!manual.equals(other.manual))
				return false;
			if (storeNames == null) {
				if (other.storeNames != null)
					return false;
			} else if (!storeNames.equals(other.storeNames))
				return false;
			if (tagIds == null) {
				if (other.tagIds != null)
					return false;
			} else if (!tagIds.equals(other.tagIds))
				return false;
			return true;
		}

	}

	/**
	 * ２つしか条件が無い。一応各要素は積集合的と言える
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class When implements ModelConditionElementI {
		/**
		 * 各メンバー変数は初期値のままなら条件として使用されない
		 */
		public static final long init = -1L;
		/**
		 * createかupdateか
		 */
		private boolean create = true;
		private long endDate = init;
		private long startDate = init;

		public boolean is(ModelI m) {
			if (m == null)
				return false;

			long date = create ? m.getCreateDate() : m.getUpdateDate();
			if (startDate != init) {
				if (date < startDate) {
					return false;
				}
			}
			if (endDate != init) {
				if (date > endDate) {
					return false;
				}
			}
			return true;
		}

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (endDate == init) {
				r.add(Lang.WHEN, Lang.END_DATE, Lang.ERROR_INVALID,
						"endDate=" + endDate);
				b = false;
			}
			if (startDate == init) {
				r.add(Lang.WHEN, Lang.START_DATE, Lang.ERROR_INVALID,
						"startDate=" + startDate);
				b = false;
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
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

		public boolean isCreate() {
			return create;
		}

		public void setCreate(boolean create) {
			this.create = create;
		}

		public long getEndDate() {
			return endDate;
		}

		public void setEndDate(long endDate) {
			this.endDate = endDate;
		}

		public long getStartDate() {
			return startDate;
		}

		public void setStartDate(long startDate) {
			this.startDate = startDate;
		}

		public static long getInit() {
			return init;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (create ? 1231 : 1237);
			result = prime * result + (int) (endDate ^ (endDate >>> 32));
			result = prime * result + (int) (startDate ^ (startDate >>> 32));
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
			When other = (When) obj;
			if (create != other.create)
				return false;
			if (endDate != other.endDate)
				return false;
			if (startDate != other.startDate)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "When [create=" + create + ", endDate=" + endDate
					+ ", startDate=" + startDate + "]";
		}
	}

	/**
	 * 起点を１か所しか指定できないのがどうかと思ったが、
	 * もし複数指定したい場合は{@link How}を通じて解決できる。
	 *
	 * ３つメンバー変数があるがそれら全体で１つの検索条件を意味するので
	 * 積とも和とも言えない。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class Where implements ModelConditionElementI {
		/**
		 * 辿る次数
		 */
		private int orderMax;
		/**
		 * フローの閾値
		 */
		private double threshold;
		/**
		 * 起点となる{@link SocialityI}
		 */
		private Long startSocialityId;

		@Override
		public boolean is(ModelI m) {
			if (m == null)
				return false;
			if (startSocialityId == null)
				return true;
			if (threshold < 0)
				return false;
			if (orderMax < 0)
				return false;
			//起点から閾値内でたどれる範囲に居るか
			SocialityI so = Glb.getObje()
					.getSociality(sos -> sos.get(startSocialityId));
			if (so == null)
				return false;

			// TODO フロー計算モジュールがまだ未実装

			// TODO 自動生成されたメソッド・スタブ
			return false;
		}

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (orderMax < 0) {
				r.add(Lang.WHERE, Lang.END_DATE, Lang.ERROR_INVALID,
						"orderMax=" + orderMax);
				b = false;
			}
			if (threshold < 0) {
				r.add(Lang.WHERE, Lang.THRESHOLD, Lang.ERROR_INVALID,
						"threshold=" + threshold);
				b = false;
			}
			if (startSocialityId == null) {
				r.add(Lang.WHERE, Lang.START_SOCIALITY_ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (!Model.validateIdStandard(startSocialityId)) {
					r.add(Lang.WHERE, Lang.THRESHOLD, Lang.ERROR_INVALID,
							"threshold=" + threshold);
					b = false;
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
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

		public int getOrderMax() {
			return orderMax;
		}

		public void setOrderMax(int orderMax) {
			this.orderMax = orderMax;
		}

		public double getThreshold() {
			return threshold;
		}

		public void setThreshold(double threshold) {
			this.threshold = threshold;
		}

		public Long getStartSocialityId() {
			return startSocialityId;
		}

		public void setStartSocialityId(Long startSocialityId) {
			this.startSocialityId = startSocialityId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + orderMax;
			result = prime * result + ((startSocialityId == null) ? 0
					: startSocialityId.hashCode());
			long temp;
			temp = Double.doubleToLongBits(threshold);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			Where other = (Where) obj;
			if (orderMax != other.orderMax)
				return false;
			if (startSocialityId == null) {
				if (other.startSocialityId != null)
					return false;
			} else if (!startSocialityId.equals(other.startSocialityId))
				return false;
			if (Double.doubleToLongBits(threshold) != Double
					.doubleToLongBits(other.threshold))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Where [orderMax=" + orderMax + ", threshold=" + threshold
					+ ", startSocialityId=" + startSocialityId + "]";
		}
	}

	/**
	 * 各要素は和集合的
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class Who implements ModelConditionElementI {
		/**
		 * この中のいずれかの言語である事
		 * nullなら全言語許可
		 */
		private List<Locale> locales = new ArrayList<>();

		/**
		 * 作成者Userがこの中のいずれかのタグを持つ事
		 */
		private List<Long> tagIds = new ArrayList<>();

		/**
		 * この中の誰かによる事
		 * nullなら全員許可
		 */
		private List<Long> userIds = new ArrayList<>();

		private boolean checkLocales(ModelI m) {
			if (locales == null)
				return true;

			if (!(m instanceof IndividualityObject)) {
				return false;
			}
			IndividualityObject c = (IndividualityObject) m;

			for (Locale l : locales) {
				if (l.equals(c.getLocale())) {
					return true;
				}
			}
			return false;
		}

		private boolean checkTagIds(ModelI m) {
			if (tagIds == null)
				return true;
			if (!(m instanceof IndividualityObjectI)) {
				return false;
			}
			IndividualityObjectI i = (IndividualityObjectI) m;
			User u = Glb.getObje()
					.getUser(us -> us.get(i.getRegistererUserId()));
			if (u == null || u.getTagIds() == null)
				return false;
			for (Long tagId : tagIds) {
				for (Long mTagId : u.getTagIds()) {
					if (tagId.equals(mTagId)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean checkUserIds(ModelI m) {
			if (userIds == null)
				return true;

			if (!(m instanceof AdministratedObjectI)) {
				return false;
			}
			AdministratedObjectI a = (AdministratedObjectI) m;

			Long uploader = a.getRegistererUserId();
			if (uploader == null)
				return false;

			for (Long uId : userIds) {
				if (uploader.equals(uId)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean is(ModelI m) {
			if (m == null)
				return false;
			if (!checkUserIds(m)) {
				return false;
			}

			if (!checkLocales(m)) {
				return false;
			}

			if (!checkTagIds(m)) {
				return false;
			}

			return true;
		}

		private static final int localesMax = 500;
		private static final int tagIdsMax = 1000;
		private static final int userIdsMax = 1000;

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (locales == null) {
				r.add(Lang.WHO, Lang.LOCALES, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (locales.size() < localesMax) {
					r.add(Lang.WHO, Lang.LOCALES, Lang.ERROR_INVALID,
							"locales.size()=" + locales.size());
					b = false;
				}
			}
			if (tagIds == null) {
				r.add(Lang.WHO, Lang.TAG_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (tagIds.size() < tagIdsMax) {
					r.add(Lang.WHO, Lang.TAG_IDS, Lang.ERROR_INVALID,
							"tagIds.size()=" + tagIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(tagIds)) {
						r.add(Lang.WHO, Lang.TAG_IDS, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			if (userIds == null) {
				r.add(Lang.WHO, Lang.USER_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (userIds.size() < userIdsMax) {
					r.add(Lang.WHO, Lang.USER_IDS, Lang.ERROR_INVALID,
							"userIds.size()=" + userIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(userIds)) {
						r.add(Lang.WHO, Lang.USER_IDS, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
				b = false;
			}
			return b;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			boolean b = true;
			if (!validateCommon(r)) {
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

		public List<Locale> getLocales() {
			return locales;
		}

		public void setLocales(List<Locale> locales) {
			this.locales = locales;
		}

		public List<Long> getTagIds() {
			return tagIds;
		}

		public void setTagIds(List<Long> tagIds) {
			this.tagIds = tagIds;
		}

		public List<Long> getUserIds() {
			return userIds;
		}

		public void setUserIds(List<Long> userIds) {
			this.userIds = userIds;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((locales == null) ? 0 : locales.hashCode());
			result = prime * result
					+ ((tagIds == null) ? 0 : tagIds.hashCode());
			result = prime * result
					+ ((userIds == null) ? 0 : userIds.hashCode());
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
			Who other = (Who) obj;
			if (locales == null) {
				if (other.locales != null)
					return false;
			} else if (!locales.equals(other.locales))
				return false;
			if (tagIds == null) {
				if (other.tagIds != null)
					return false;
			} else if (!tagIds.equals(other.tagIds))
				return false;
			if (userIds == null) {
				if (other.userIds != null)
					return false;
			} else if (!userIds.equals(other.userIds))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Who [locales=" + locales + ", tagIds=" + tagIds
					+ ", userIds=" + userIds + "]";
		}
	}

	@Override
	public Map<Logic, Long> getOtherModelConditionIds() {
		return how.getOtherConditionIds();
	}

	@Override
	public List<StoreName> getStoreNames() {
		return what.getStoreNames();
	}

	@Override
	public List<TenyuReference<?>> getManual() {
		return what.getManual();
	}

	@Override
	public Long getStartSocialityId() {
		return where.getStartSocialityId();
	}

	@Override
	public List<Locale> getLocales() {
		return who.getLocales();
	}

	@Override
	public List<Long> getUserIds() {
		return who.getUserIds();
	}

}
