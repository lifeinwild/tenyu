package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.Package;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.SignedPackage.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.env.*;

/**
 * 送金
 * @author exceptiontenyu@gmail.com
 *
 */
public class SendMoney extends UserRightRequest
		implements SignedPackageContent {
	private Long sender;
	private Long receiver;

	@Override
	public String getName() {
		return Lang.SEND_MONEY.toString();
	}

	@Override
	protected final boolean validateUserRightConcrete(Message m) {
		if (m == null || sender == null || receiver == null)
			return false;

		//署名者
		Long signer = m.getUserId();

		if (signer == null) {
			return false;
		}

		//直前梱包のUserPackageを取得
		Package p = m.getInnermostPack();
		//直前が署名梱包か
		if (!(p instanceof SignedPackage))
			return false;
		//署名者と送金者が一致しているか
		return sender.equals(signer);
	}

	public Long getSender() {
		return sender;
	}

	public void setSender(Long sender) {
		this.sender = sender;
	}

	public Long getReceiver() {
		return receiver;
	}

	public void setReceiver(Long receiver) {
		this.receiver = receiver;
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
