package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 相互評価フローネットワークの抽象ノード
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FlowNetworkAbstractNominal extends IndividualityObject
		implements FlowNetworkAbstractNominalI {

	public static boolean createSequence(Transaction txn,
			FlowNetworkAbstractNominal u, boolean specifiedId,
			long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, null, u.getRegistererUserId(),
				StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL);
	}

	public static boolean deleteSequence(Transaction txn,
			FlowNetworkAbstractNominal u) throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return new ArrayList<>();
	}

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return ModelI.getNullId();//作成当初null、その後ユーザーが設定される
	}

	@Override
	public Long getSpecialRegistererId() {
		return ModelI.getVoteId();
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	@Override
	protected final boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof FlowNetworkAbstractNominal)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//FlowNetworkAbstractNominal old2 = (FlowNetworkAbstractNominal) old;

		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		return true;
	}

	@Override
	public FlowNetworkAbstractNominalGui getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new FlowNetworkAbstractNominalGui(guiName, cssIdPrefix);
	}

	@Override
	public FlowNetworkAbstractNominalStore getStore(Transaction txn) {
		return new FlowNetworkAbstractNominalStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL;
	}

}
