package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 相互評価フローネットワークの抽象ノード
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FlowNetworkAbstractNominal extends Naturality
		implements FlowNetworkAbstractNominalDBI {

	public static boolean createSequence(Transaction txn,
			FlowNetworkAbstractNominal u, boolean specifiedId,
			long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, new FlowNetworkAbstractNominalStore(txn), null,
				u.getRegistererUserId(), NodeType.FLOWNETWORK_ABSTRACTNOMINAL);
	}
	public static boolean deleteSequence(Transaction txn,
			FlowNetworkAbstractNominal u) throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				new FlowNetworkAbstractNominalStore(txn),
				NodeType.FLOWNETWORK_ABSTRACTNOMINAL);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return new ArrayList<>();
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
		return IdObjectDBI.getNullId();//作成当初null、その後ユーザーが設定される
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectDBI.getVoteId();
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
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
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		return true;
	}

}
