package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
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
	public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGuiReferenced(String guiName,
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
		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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

		public static long getUseridsmax() {
			return userIdsMax;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Why other = (Why) obj;
			if (userIds == null) {
				if (other.userIds != null)
					return false;
			} else if (!userIds.equals(other.userIds))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Why [userIds=" + userIds + "]";
		}
	}

	@Override
	public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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
		private List<
				OtherModelCondition> otherModelConditions = new ArrayList<>();

		public static class OtherModelCondition implements ValidatableI {
			private Logic logic;
			private Long otherModelConditionId;

			public Logic getLogic() {
				return logic;
			}

			public void setLogic(Logic logic) {
				this.logic = logic;
			}

			public Long getOtherModelConditionId() {
				return otherModelConditionId;
			}

			public void setOtherModelConditionId(Long otherModelConditionId) {
				this.otherModelConditionId = otherModelConditionId;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((logic == null) ? 0 : logic.hashCode());
				result = prime * result + ((otherModelConditionId == null) ? 0
						: otherModelConditionId.hashCode());
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
				OtherModelCondition other = (OtherModelCondition) obj;
				if (logic != other.logic)
					return false;
				if (otherModelConditionId == null) {
					if (other.otherModelConditionId != null)
						return false;
				} else if (!otherModelConditionId
						.equals(other.otherModelConditionId))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "OtherModelCondition [logic=" + logic
						+ ", otherModelConditionId=" + otherModelConditionId
						+ "]";
			}

			private boolean validateCommon(ValidationResult r) {
				boolean b = true;
				if (logic == null) {
					r.add(Lang.HOW, Lang.LOGIC, Lang.ERROR_EMPTY);
					b = false;
				}
				if (!Model.validateIdStandard(otherModelConditionId)) {
					r.add(Lang.HOW, Lang.HOW_ID, Lang.ERROR_INVALID,
							"otherModelConditionId=" + otherModelConditionId);
					b = false;
				}
				return b;
			}

			@Override
			public boolean validateAtCreate(ValidationResult r) {
				return validateCommon(r);
			}

			@Override
			public boolean validateAtUpdate(ValidationResult r) {
				return validateCommon(r);
			}

			@Override
			public boolean validateReference(ValidationResult r,
					Transaction txn) throws Exception {
				boolean b = true;
				ModelConditionStore mcs = new ModelConditionStore(txn);
				ModelCondition mc = mcs.get(otherModelConditionId);
				if (mc == null) {
					r.add(Lang.HOW, Lang.HOW_ID,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"otherModelConditionId=" + otherModelConditionId);
					b = false;
				}
				return b;
			}
		}

		private static final int otherConditionIdsMax = 2500;

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (otherModelConditions == null) {
				r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
						Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (otherModelConditions.size() > otherConditionIdsMax) {
					r.add(Lang.HOW, Lang.OTHER_MODEL_CONDITION_IDS,
							Lang.ERROR_TOO_MANY,
							"size=" + otherModelConditions.size());
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
			} else {
				for (OtherModelCondition omc : otherModelConditions) {
					if (!omc.validateAtCreate(r)) {
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
				for (OtherModelCondition omc : otherModelConditions) {
					if (!omc.validateAtUpdate(r)) {
						b = false;
						break;
					}
				}
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
			for (OtherModelCondition omc : otherModelConditions) {
				if (!omc.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
			return b;
		}

		private boolean checkOtherCondition(
				TenyupediaObjectI<? extends ModelI> m) {
			if (otherModelConditions == null)
				return true;
			return Glb.getObje().getModelCondition(mcs -> {
				for (OtherModelCondition e : otherModelConditions) {
					ModelCondition mc = mcs.get(e.getOtherModelConditionId());
					if (mc == null)
						continue;
					switch (e.getLogic()) {
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
		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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
			AND(0), NAND(1), NOR(2), OR(3);
			private int id;

			private Logic(int id) {
				this.id = id;
			}

			public int getId() {
				return id;
			}

			public static Logic getRandom() {
				return getRandom(Glb.getRnd());
			}

			public static Logic getRandom(Random rnd) {
				int max = Logic.values().length - 1;
				if (max < 0) {
					Glb.getLogger().warn("", new IllegalStateException());
					return null;
				}
				return get(rnd.nextInt(max));
			}

			public static Logic get(int id) {
				for (Logic l : Logic.values()) {
					if (l.getId() == id)
						return l;
				}
				Glb.getLogger().warn("id=" + id,
						new IllegalArgumentException());
				return null;
			}
		}

		public List<OtherModelCondition> getOtherModelConditions() {
			return otherModelConditions;
		}

		public void setOtherModelConditions(
				List<OtherModelCondition> otherModelConditions) {
			this.otherModelConditions = otherModelConditions;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((otherModelConditions == null) ? 0
					: otherModelConditions.hashCode());
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
			if (otherModelConditions == null) {
				if (other.otherModelConditions != null)
					return false;
			} else if (!otherModelConditions.equals(other.otherModelConditions))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "How [otherConditionIds=" + otherModelConditions + "]";
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
		 * 対象がこの中のいずれかの認定を持つ事
		 *
		 * 認定による通知は、認定されたタイミングで生じる。
		 */
		private List<Long> certificationIds = new ArrayList<>();

		/**
		 * 任意のものを手動(manual)指定して追加できる。
		 * ここに指定されたものは他の条件に関係無く常に検索結果に含められる。
		 */
		private List<TenyuReferenceModelI<
				? extends ModelI>> manual = new ArrayList<>();

		/**
		 * この中のいずれかのモデルである事
		 * store名だが実質モデル名
		 */
		private List<StoreName> storeNames = new ArrayList<>();

		/**
		 * 対象がこの中のいずれかのタグを持つ事
		 */
		private List<Long> tagIds = new ArrayList<>();

		private boolean checkCertificationIds(
				TenyupediaObjectI<? extends ModelI> m) {
			if (certificationIds == null)
				return true;

			return Glb.getObje().getCertification(cs -> {
				for (Long certificationId : certificationIds) {
					Certification c = cs.get(certificationId);
					if (c == null) {
						return false;
					}

				}

				return true;
			});
		}

		private boolean checkManual(TenyupediaObjectI<? extends ModelI> m) {
			if (manual == null)
				return true;
			for (TenyuReferenceModelI<?> r : manual) {
				if (m.getStoreName().equals(r.getStoreName())
						&& m.getId().equals(r.getId())) {
					return true;
				}
			}
			return false;
		}

		private boolean checkModel(TenyupediaObjectI<? extends ModelI> m) {
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

		private boolean checkTagIds(TenyupediaObjectI<? extends ModelI> m) {
			if (tagIds == null)
				return true;
			if (!(m instanceof HasTag)) {
				return false;
			}
			HasTag i = (HasTag) m;
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
		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
			if (m == null)
				return false;

			if (!checkModel(m))
				return false;
			if (!checkManual(m))
				return false;
			if (!checkTagIds(m))
				return false;
			if (!checkCertificationIds(m))
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
				r.add(Lang.WHAT, Lang.WHAT_TAG_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (tagIds.size() > tagIdsMax) {
					r.add(Lang.WHAT, Lang.WHAT_TAG_IDS, Lang.ERROR_TOO_MANY,
							"size=" + tagIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(tagIds)) {
						r.add(Lang.WHAT, Lang.WHAT_TAG_IDS, Lang.ERROR_INVALID);
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
				for (TenyuReferenceModelI<? extends ModelI> e : manual) {
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
				for (TenyuReferenceModelI<? extends ModelI> e : manual) {
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
			for (TenyuReferenceModelI<? extends ModelI> e : manual) {
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

		public List<TenyuReferenceModelI<? extends ModelI>> getManual() {
			return manual;
		}

		public void setManual(
				List<TenyuReferenceModelI<? extends ModelI>> manual) {
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
			return "What [certificationIds=" + certificationIds + ", manual="
					+ manual + ", storeNames=" + storeNames + ", tagIds="
					+ tagIds + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((certificationIds == null) ? 0
					: certificationIds.hashCode());
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
			if (certificationIds == null) {
				if (other.certificationIds != null)
					return false;
			} else if (!certificationIds.equals(other.certificationIds))
				return false;
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

		public List<Long> getCertificationIds() {
			return certificationIds;
		}

		public void setCertificationIds(List<Long> certificationIds) {
			this.certificationIds = certificationIds;
		}

		public void setStoreNames(List<StoreName> storeNames) {
			this.storeNames = storeNames;
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

		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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
			if (endDate < 0 && endDate != init) {
				r.add(Lang.WHEN, Lang.END_DATE, Lang.ERROR_INVALID,
						"endDate=" + endDate);
				b = false;
			}
			if (startDate < 0 && startDate != init) {
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
		 * 空なら無条件となりorderMaxやthresholdは使われない
		 */
		private Long startSocialityId;

		@Override
		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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
				r.add(Lang.WHERE, Lang.ORDER_MAX, Lang.ERROR_INVALID,
						"orderMax=" + orderMax);
				b = false;
			}
			if (threshold < 0) {
				r.add(Lang.WHERE, Lang.WHERE_THRESHOLD, Lang.ERROR_INVALID,
						"threshold=" + threshold);
				b = false;
			}
			if (startSocialityId == null) {
				//空でもいい
				//r.add(Lang.WHERE, Lang.START_SOCIALITY_ID, Lang.ERROR_EMPTY);
				//b = false;
			} else {
				if (!Model.validateIdStandard(startSocialityId)) {
					r.add(Lang.WHERE, Lang.START_SOCIALITY_ID,
							Lang.ERROR_INVALID,
							"startSocialityId=" + startSocialityId);
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

		private boolean checkLocales(TenyupediaObjectI<? extends ModelI> m) {
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

		private boolean checkTagIds(TenyupediaObjectI<? extends ModelI> m) {
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

		private boolean checkUserIds(TenyupediaObjectI<? extends ModelI> m) {
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
		public boolean is(TenyupediaObjectI<? extends ModelI> m) {
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
				if (locales.size() > localesMax) {
					r.add(Lang.WHO, Lang.LOCALES, Lang.ERROR_TOO_MANY,
							"locales.size()=" + locales.size());
					b = false;
				}
			}
			if (tagIds == null) {
				r.add(Lang.WHO, Lang.WHO_TAG_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (tagIds.size() > tagIdsMax) {
					r.add(Lang.WHO, Lang.WHO_TAG_IDS, Lang.ERROR_TOO_MANY,
							"tagIds.size()=" + tagIds.size());
					b = false;
				} else {
					if (!Model.validateIdStandard(tagIds)) {
						r.add(Lang.WHO, Lang.WHO_TAG_IDS, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
			if (userIds == null) {
				r.add(Lang.WHO, Lang.USER_IDS, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (userIds.size() > userIdsMax) {
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
	public List<OtherModelCondition> getOtherModelConditions() {
		return how.getOtherModelConditions();
	}

	@Override
	public List<StoreName> getStoreNames() {
		return what.getStoreNames();
	}

	@Override
	public List<TenyuReferenceModelI<? extends ModelI>> getManual() {
		return what.getManual();
	}

	@Override
	public Long getStartSocialityId() {
		return where.getStartSocialityId();
	}

	@Override
	public List<Locale> getLocaleConditions() {
		return who.getLocales();
	}

	@Override
	public List<Long> getUserIds() {
		return who.getUserIds();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((how == null) ? 0 : how.hashCode());
		result = prime * result + ((what == null) ? 0 : what.hashCode());
		result = prime * result + ((when == null) ? 0 : when.hashCode());
		result = prime * result + ((where == null) ? 0 : where.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelCondition other = (ModelCondition) obj;
		if (how == null) {
			if (other.how != null)
				return false;
		} else if (!how.equals(other.how))
			return false;
		if (what == null) {
			if (other.what != null)
				return false;
		} else if (!what.equals(other.what))
			return false;
		if (when == null) {
			if (other.when != null)
				return false;
		} else if (!when.equals(other.when))
			return false;
		if (where == null) {
			if (other.where != null)
				return false;
		} else if (!where.equals(other.where))
			return false;
		if (who == null) {
			if (other.who != null)
				return false;
		} else if (!who.equals(other.who))
			return false;
		return true;
	}

}
