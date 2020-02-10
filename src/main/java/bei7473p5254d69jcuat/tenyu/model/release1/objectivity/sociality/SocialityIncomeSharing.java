package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 仮想通貨分配時にこのデータによって設定された他のユーザーに
 * 一部の仮想通貨が分配される。
 * 仮想通貨分配で得た仮想通貨がさらに分配される仕組み。
 * 例えばプレイヤー報酬を実現する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class SocialityIncomeSharing extends AdministratedObject
		implements SocialityIncomeSharingDBI {

	public static Map<Long, Double> getSharingRate(Long senderSocialityId) {
		Map<Long, Double> r = new HashMap<Long, Double>();
		return r;
	}

	/**
	 * 分配を受ける側
	 */
	private Long receiverSocialityId;
	/**
	 * 分配する側
	 */
	private Long senderSocialityId;

	/**
	 * 分配数値
	 * senderの全分配設定の中でこの数値が占める割合がreceiverが受け取る割合
	 */
	private long sharingNumber;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//システムが作成
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//システムが削除
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//システムが更新
	}

	public Long getReceiverSocialityId() {
		return receiverSocialityId;
	}

	public Long getSenderSocialityId() {
		return senderSocialityId;
	}

	public long getSharingNumber() {
		return sharingNumber;
	}

	public void setReceiverSocialityId(Long receiverSocialityId) {
		this.receiverSocialityId = receiverSocialityId;
	}

	public void setSenderSocialityId(Long senderSocialityId) {
		this.senderSocialityId = senderSocialityId;
	}

	public void setSharingNumber(long sharingNumber) {
		this.sharingNumber = sharingNumber;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (senderSocialityId == null) {
			r.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(senderSocialityId)) {
				r.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (receiverSocialityId == null) {
			r.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(receiverSocialityId)) {
				r.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (sharingNumber <= 0) {
			r.add(Lang.SOCIALITY_INCOMESHARING_SHARINGNUMBER,
					Lang.ERROR_TOO_LITTLE);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof SocialityIncomeSharing)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		SocialityIncomeSharing old2 = (SocialityIncomeSharing) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getReceiverSocialityId(),
				old2.getReceiverSocialityId())) {
			r.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
					Lang.ERROR_UNALTERABLE,
					"receiverSocialityId=" + getReceiverSocialityId()
							+ " oldReceiverSocialityId="
							+ old2.getReceiverSocialityId());
			b = false;
		}
		if (Glb.getUtil().notEqual(getSenderSocialityId(),
				old2.getSenderSocialityId())) {
			r.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
					Lang.ERROR_UNALTERABLE,
					"senderSocialityId=" + getSenderSocialityId()
							+ " oldSenderSocialityId="
							+ old2.getSenderSocialityId());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		SocialityStore s = new SocialityStore(txn);

		Sociality so1 = s.get(senderSocialityId);
		if (so1 == null) {
			r.add(Lang.SOCIALITY_INCOMESHARING_SENDERSOCIALITYID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		Sociality so2 = s.get(receiverSocialityId);
		if (so2 == null) {
			r.add(Lang.SOCIALITY_INCOMESHARING_RECEIVERSOCIALITYID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		return b;
	}

	@Override
	public AdministratedObjectGui getGui(String guiName, String cssIdPrefix) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public SocialityIncomeSharingStore getStore(Transaction txn) {
		return new SocialityIncomeSharingStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.SOCIALITY_INCOME_SHARING;
	}

}
