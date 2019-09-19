package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * 任意のURLを客観に登録し同時に社会性を作成する。
 * 誰でも登録できる。
 * この処理を通じて設定されたWebオブジェクトの登録者はURL証明を通じて上書きされる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class WebRegistration extends UserRightRequest {
	/**
	 * 登録されるURL
	 */
	private Web requestedUrl;

	@Override
	protected boolean validateUserRightConcrete(Message m) {
		if (requestedUrl == null)
			return false;

		if (!requestedUrl.validateAtCreate(new ValidationResult()))
			return false;

		if (requestedUrl.getRegistererUserId() == null)
			return false;

		//登録されるURLの登録者はこのメッセージの署名者か
		if (!requestedUrl.getRegistererUserId().equals(m.getUserId()))
			return false;

		return true;
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		return Web.createSequence(txn, requestedUrl, false, historyIndex);
	}

	@Override
	public String getName() {
		return Lang.WEB_URL_REGISTRATION.toString();
	}

}
