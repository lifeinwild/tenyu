package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import java.util.*;

import org.jetbrains.annotations.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 管理者や登録者が設定されるオブジェクト。
 * 管理者はこのオブジェクトの更新権限等を持つ。
 *
 * 「様々な人の手に晒されるのに特定の人しか管理権限が認められない」
 * という排他的管理を本質としている。
 * そのため客観に記録されるオブジェクトは必ずこの抽象クラスを継承している。
 * さらにTenyutalk上で扱われるコンテンツも同様。
 *
 * TODO:メンバー変数にHashMapやそのほかコンテナ系クラスがあった場合、
 * 同じ値が同じ順序で入力されるなら、
 * どの環境でもシリアライズ時のbyte[]は同値になるか？
 * これが保証されないと設計を大幅に見直さなければならない。
 * 保証されないなら、それを保証する独自のListやMap実装で置き換える事になる。
 * これは最大の問題だが調査するのに時間がかかりそうで保留している。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AdministratedObject extends IdObject
		implements AdministratedObjectDBI {
	/**
	 * 管理者のユーザーID
	 *
	 * 登録者と異なり交代する可能性がある。
	 *
	 * この管理者以外にもこの客観オブジェクトを管理可能なユーザーが存在する
	 * 場合もある。この管理者はメイン管理者と考えられる。
	 *
	 * 管理者は当然この客観オブジェクトの編集権限を持つ。
	 * しかしこの管理者と社会性の管理者は、多くの場合同じだが、データとしては分けられている。
	 */
	protected Long mainAdministratorUserId = IdObjectDBI.getNullId();

	/**
	 * 基本的に、この情報を登録したユーザーのID。
	 * ただし実際には登録者として設定されたユーザーのBANまたは削除に応じて
	 * このオブジェクトもBANまたは削除されるという連鎖的削除において参照される情報である。
	 * Webなどで最初の登録者から変更される場合がある。
	 *
	 * 客観コア、抽象ノード名目等一部のオブジェクトは特殊な登録者が設定される。
	 */
	protected Long registererUserId = IdObjectDBI.getNullId();

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

	/**
	 * 一部具象クラスはメイン管理者のIDが特殊なIDの場合があるので、
	 * そのような具象クラスではオーバーライドして許容される特殊IDを返す。
	 *
	 */
	public Long getSpecialMainAdministratorId() {
		return null;
	}

	/**
	 * nullIdやそのほか設定されていても問題無い特殊ID一覧を定義する。
	 * 必要ならオーバーライドする。
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
	 * 大抵の具象クラスは初期値不可
	 */
	public Long getSpecialRegistererId() {
		return null;
	}

	/**
	 * 特殊な登録者IDを使用し、かつそれが複数ある場合、これをオーバーライドする。
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

	private final boolean validateAtCommonIdObjectConcrete(ValidationResult r) {
		boolean b = true;
		/*
		 * FlowNetworkAbstractNominalが反例になった
		//各モデルクラスにおいてid==0でなければ登録者IDはNullIdではない。
		//複雑な条件なので長期的に見てこの条件が成立し続けるのか分からないので
		//ログだけ出して処理を止めずに続行する。
		if (id != null && id != IdObjectDBI.getFirstId()
				&& registererUserId < IdObjectDBI.getFirstId()) {
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
				} else if (!IdObject.validateIdStandardNotSpecialId(
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
	protected final boolean validateAtCreateIdObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIdObjectConcrete(r))
			b = false;
		if (!validateAtCreateAdministratedObjectConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateChangeIdObjectConcrete(ValidationResult r,
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
	protected final boolean validateAtUpdateIdObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIdObjectConcrete(r))
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
				|| mainAdministratorUserId.equals(IdObjectDBI.getNullId())) {
			r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR, Lang.ERROR_EMPTY);
		}
	}

	@Override
	public boolean validateReferenceIdObjectConcrete(ValidationResult r,
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

	public abstract AdministratedObjectStore<? extends AdministratedObjectDBI,
			? extends AdministratedObjectDBI> getStore(Transaction txn);

	@Override
	abstract public AdministratedObjectGui<?, ?, ?, ?, ?, ?> getGui(
			String guiName, String cssIdPrefix);
}
