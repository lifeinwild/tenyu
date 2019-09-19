package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

public class UserProfileUpdate extends UserRightRequest {
	private User latest;

	@Override
	protected boolean validateUserRightConcrete(Message m) {
		if (latest == null || latest.getRecycleId() == null)
			return false;
		if (!(m.getInnermostPack() instanceof SignedPackage))
			return false;
		SignedPackage signed = (SignedPackage) m.getInnermostPack();
		if (!latest.getRecycleId().equals(signed.getSignerUserId()))
			return false;
		return true;
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		if (latest == null || latest.getRecycleId() == null)
			return false;
		UserStore s = new UserStore(txn);
		if (!s.update(latest))
			throw new IllegalStateException();
		return true;
	}

	@Override
	public String getName() {
		return Lang.USER_UPDATE.toString();
	}

	public User getLatest() {
		return latest;
	}

	public void setLatest(User latest) {
		this.latest = latest;
	}

}
