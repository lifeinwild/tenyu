package bei7473p5254d69jcuat.tenyu.communication.request.gui.right.user;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class UserRegistration extends UserRightRequest {
	private UserRegistrationInfo info;

	public void setInfo(UserRegistrationInfo info) {
		this.info = info;
	}

	@Override
	public String getName() {
		return Lang.USER_REGISTRATION.toString();
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		return User.createSequence(txn, info.getMe(), false, historyIndex);
	}

	@Override
	protected final boolean validateUserRightConcrete(Message m) {
		Long signerUserId = SignedPackage.getSigner(m);
		if (signerUserId == null)
			return false;
		if (info == null || info.getMe() == null)
			return false;
		User u = info.getMe();
		Long inviterUserId = u.getInviter();
		if (inviterUserId == null
				|| IdObjectI.getNullId().equals(inviterUserId))
			return false;
		boolean dbCheck = Glb.getObje().compute(txn -> {
			try {
				UserStore store = new UserStore(txn);
				return store.noExistIdObjectConcrete(u, new ValidationResult());
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
		if (!dbCheck)
			return false;

		ValidationResult vr = new ValidationResult();
		u.validateAtCreate(vr);
		if (!vr.isNoError())
			return false;

		//署名者は紹介が認められたユーザーか
		User signer = Glb.getObje().getUser(us -> us.get(signerUserId));
		if (!signer.canIntroduce()) {
			return false;
		}

		//署名者と紹介者が同じか
		if (!signerUserId.equals(u.getRegistererUserId()))
			return false;

		//セキュア設定はシーケンスが指定する状態か
		if (u.isSecure() != false)
			return false;

		// このような検証処理は特にこのシーケンス固有ではないので書く場所が違う
		//そしてDB登録時に検証処理が行われているのでここでやる必要は無い。
		//FQDNはnullまたは妥当か
		//if (u.getAddr() == null)
		//return false;
		//String fqdn = u.getAddr().getFqdn();
		//if (fqdn != null && fqdn.length() > 50)
		//	return false;

		return info.validate() && signer.getId().equals(inviterUserId);
	}
}
