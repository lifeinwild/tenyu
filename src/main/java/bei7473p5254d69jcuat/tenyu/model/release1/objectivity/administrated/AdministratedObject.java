package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated;

import java.util.*;

import org.jetbrains.annotations.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AdministratedObject extends Model
		implements AdministratedObjectI {
	protected Long mainAdministratorUserId = ModelI.getNullId();

	protected Long registererUserId = ModelI.getNullId();

	@Override
	public TenyuReferenceModelI<? extends ModelI> getReference() {
		StoreName storeName = getStoreName();
		if (!(storeName instanceof StoreNameObjectivity)) {
			throw new IllegalArgumentException(
					"storeName.class = " + storeName.getClass());
		}
		return new TenyuReferenceModelSimple<>(id,
				(StoreNameObjectivity) storeName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdministratedObject other = (AdministratedObject) obj;
		if (mainAdministratorUserId == null) {
			if (other.mainAdministratorUserId != null)
				return false;
		} else if (!mainAdministratorUserId
				.equals(other.mainAdministratorUserId))
			return false;
		if (registererUserId == null) {
			if (other.registererUserId != null)
				return false;
		} else if (!registererUserId.equals(other.registererUserId))
			return false;
		return true;
	}

	/**
	 * そのオブジェクトを操作可能なユーザーのIDの一覧を返す。
	 * ただしTenyuManagerによる議決はその一覧に含まれなくてもあらゆる操作が可能。
	 *
	 * Createは多くの場合staticメソッドも用意する。
	 *
	 * @return	そのオブジェクトの管理者のユーザーID
	 * 空リストは全体運営者のみ
	 * nullは全ユーザー
	 */
	public abstract List<Long> getAdministratorUserIdCreate();

	public abstract List<Long> getAdministratorUserIdDelete();

	public abstract List<Long> getAdministratorUserIdUpdate();

	public Long getMainAdministratorUserId() {
		return mainAdministratorUserId;
	}

	@Override
	public Long getRegistererUserId() {
		return registererUserId;
	}

	public User getRegistererUser() {
		return Glb.getObje().getUser(us -> us.get(registererUserId));
	}

	/**
	 * 一部具象クラスはメイン管理者のIDが特殊なIDの場合があるので、
	 * そのような具象クラスではオーバーライドして許容される特殊IDを返す。
	 *
	 */
	public Long getSpecialMainAdministratorId() {
		return null;
	}

	/**
	 * nullIdやそのほか管理者として設定されていても問題無い特殊ID一覧を定義する。
	 * 具象クラスによって特殊な登録者IDが可能な場合、これをオーバーライドする。
	 *
	 * @return
	 */
	public List<Long> getSpecialMainAdministratorIds() {
		List<Long> r = new ArrayList<>();
		Long id = getSpecialMainAdministratorId();
		if (id == null)
			return r;
		r.add(id);
		return r;
	}

	/**
	 * 一部具象クラスはnullIdなど特殊なIdが可能なのでオーバーライドする
	 * 大抵の具象クラスでオーバーライドが必要
	 */
	public Long getSpecialRegistererId() {
		return null;
	}

	/**
	 * nullIdやそのほか登録者として設定されていても問題無い特殊ID一覧を定義する。
	 * 具象クラスによって特殊な登録者IDが可能な場合、これをオーバーライドする。
	 * @return
	 */
	public List<Long> getSpecialRegistererIds() {
		List<Long> r = new ArrayList<>();
		Long specialId = getSpecialRegistererId();
		if (specialId != null) {
			r.add(specialId);
		}
		return r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mainAdministratorUserId == null) ? 0
				: mainAdministratorUserId.hashCode());
		result = prime * result + ((registererUserId == null) ? 0
				: registererUserId.hashCode());
		return result;
	}

	/**
	 * @return	管理者IDは{@link #getSpecialMainAdministratorIds()}に限定されるか
	 */
	public boolean isRestrictedInSpecialIdAdministrator() {
		return false;
	}

	/**
	 * @return	登録者IDは{@link #getSpecialRegistererIds()}に限定されるか
	 */
	public boolean isRestrictedInSpecialIdRegisterer() {
		return false;
	}

	public void setMainAdministratorUserId(Long mainAdministratorUserId) {
		this.mainAdministratorUserId = mainAdministratorUserId;
	}

	public void setRegistererUserId(@NotNull Long registererUserId) {
		this.registererUserId = registererUserId;
	}

	private final boolean validateAtCommonModelConcrete(ValidationResult r) {
		boolean b = true;
		/*
		 * FlowNetworkAbstractNominalが反例になった
		//各モデルクラスにおいてid==0でなければ登録者IDはNullIdではない。
		//複雑な条件なので長期的に見てこの条件が成立し続けるのか分からないので
		//ログだけ出して処理を止めずに続行する。
		if (id != null && id != ModelI.getFirstId()
				&& registererUserId < ModelI.getFirstId()) {
			Glb.getLogger().warn(new Exception("Invalid registererUserId"));
		}
		*/

		if (registererUserId == null) {
			r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (getSpecialRegistererIds().contains(registererUserId)) {
				//正常
			} else {
				if (isRestrictedInSpecialIdRegisterer()) {
					r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER,
							Lang.ERROR_INVALID);
					b = false;
				} else if (!validateIdStandardNotSpecialId(registererUserId)) {
					r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
		}

		if (mainAdministratorUserId == null) {
			r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (getSpecialMainAdministratorIds()
					.contains(mainAdministratorUserId)) {
				//正常
			} else {
				if (isRestrictedInSpecialIdAdministrator()) {
					r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR,
							Lang.ERROR_INVALID);
					b = false;
				} else if (!Model.validateIdStandardNotSpecialId(
						mainAdministratorUserId)) {
					r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonModelConcrete(r))
			b = false;
		if (!validateAtCreateAdministratedObjectConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateChangeModelConcrete(ValidationResult r,
			Object old) {
		if (!(old instanceof AdministratedObject)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		AdministratedObject old2 = (AdministratedObject) old;

		return validateAtUpdateChangeAdministratedObjectConcrete(r, old2);
	}

	abstract protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old);

	@Override
	protected final boolean validateAtUpdateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonModelConcrete(r))
			b = false;
		if (!validateAtUpdateAdministratedObjectConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r);

	/**
	 * 一部具象クラスはメイン管理者がnullIdであってはならない。
	 * @param r
	 */
	protected void validateMainAdministratorNotNullId(ValidationResult r) {
		if (mainAdministratorUserId == null
				|| mainAdministratorUserId.equals(ModelI.getNullId())) {
			r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR, Lang.ERROR_EMPTY);
		}
	}

	@Override
	public boolean validateReferenceModelConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		//登録者IDが許容可能特殊ID一覧に含まれず、かつDB上に存在するIDか
		if (!getSpecialRegistererIds().contains(getRegistererUserId())
				&& new UserStore(txn).get(getRegistererUserId()) == null) {
			r.add(Lang.ADMINISTRATEDOBJECT_REGISTERER,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		//Userは常に自分自身が管理者で、DBに登録される前は登録されていないので、チェックしない
		if (!(this instanceof User)) {
			//管理者IDが許容可能特殊ID一覧に含まれず、かつDB上に存在するIDか
			if (!getSpecialMainAdministratorIds()
					.contains(getMainAdministratorUserId())
					&& new UserStore(txn)
							.get(getMainAdministratorUserId()) == null) {
				r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR,
						Lang.ERROR_DB_NOTFOUND_REFERENCE);
				b = false;
			}
		}

		if (!validateReferenceAdministratedObjectConcrete(r, txn))
			b = false;

		return b;
	}

	abstract protected boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	public abstract AdministratedObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn);

	@Override
	abstract public AdministratedObjectGui<?, ?, ?, ?, ?, ?> getGuiReferenced(
			String guiName, String cssIdPrefix);

	@Override
	public String toString() {
		return "AdministratedObject [mainAdministratorUserId="
				+ mainAdministratorUserId + ", registererUserId="
				+ registererUserId + ", toString()=" + super.toString() + "]";
	}
}
