package bei7473p5254d69jcuat.tenyu.communication.request.gui.right.user;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * PC鍵、モバイル鍵を更新する
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserKeyUpdate extends UserRightRequest {
	/**
	 * 更新対象ユーザーのID
	 */
	private Long userId;
	/**
	 * 新しいPC鍵
	 */
	private byte[] newPcKey;
	/**
	 * 新しいモバイル鍵
	 */
	private byte[] newMobileKey;

	@Override
	protected boolean validateUserRightConcrete(Message m) {
		if (!(m.getInnermostPack() instanceof SignedPackage)) {
			return false;
		}
		SignedPackage p = SignedPackage.getPack(m);
		//鍵更新はオフライン鍵による署名が必要
		if (p == null || p.getKeyType() != KeyType.OFFLINE)
			return false;
		//署名検証は開梱時に行われているのでやる必要無し
		return newPcKey != null || newMobileKey != null && userId != null
				&& userId.equals(p.getSignerUserId());
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		UserStore store = new UserStore(txn);
		User exist = store.get(userId);
		if (exist == null)
			return false;
		if (newPcKey != null)
			exist.setPcPublicKey(newPcKey);
		if (newMobileKey != null)
			exist.setMobilePublicKey(newMobileKey);
		if (!store.update(exist))
			throw new Exception("Failed to update");
		return true;
	}

	@Override
	public String getName() {
		return Lang.USER_KEY_UPDATE.toString();
	}

}
