package bei7473p5254d69jcuat.tenyu.communication.request.gui.right;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;
import glb.*;

/**
 * UserRightMessageは各ユーザーが自分の権利を行使する意味のメッセージである。
 * 例えば仮想通貨の送金、プロフィールの設定など。
 * 必ずそのユーザーの電子署名を必要とする。
 *
 * 一部のメッセージは運営者の署名も必要とする。
 *
 * 注意：compareToを具象クラスで実装する事
 *
 * ここにLong creatorUserIdを定義しようかと思ったが
 * コードを間違えるリスクが高まる。
 * 必ず梱包の検証されたUserIdを使うほうが良い。
 * もし梱包とここでuserIdが違っていたら、そのチェックを逃したらと考えると
 * 記録しない方が良いかと思った。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class UserRightRequest extends GuiCausedRequest
		implements UserMessageListRequestI, ObjectivityUpdateDataElement {

	@Override
	protected ResultCode receivedConcrete(Received validated) {
		//ここを通ると後に全ノードのストレージ消費に繋がるので、
		//サイズ制限する
		ResultCode code = null;
		Message message = validated.getMessage();
		if (Glb.getP2pDefense().isOverSize(message.getUserId(), message)) {
			//サイズ超過
			code = ResultCode.OVER_SIZE;
		} else {
			if (Glb.getMiddle().getUserMessageListServer().receive(message)) {
				code = ResultCode.SUCCESS;
			} else {
				code = ResultCode.FAIL;
			}
		}
		return code;
	}

	protected abstract boolean validateUserRightConcrete(Message m);

	/**
	 * @return	ネットワークに拡散しきるまでに必要な十分な時間。ミリ秒
	 */
	public long getDiffuseTime() {
		return 1000 * 60 * 2;
	}

	@Override
	protected final boolean validateGuiCausedConcrete(Message m) {
		return validateUserRightConcrete(m);
	}

	@Override
	public boolean isDiffused() {
		long elapsed = Glb.getUtil().elapsed(getCreateDate());
		return elapsed > getDiffuseTime();
	}

	@Override
	public boolean isOld() {
		long elapsed = Glb.getUtil().elapsed(getCreateDate());
		long threshold = getDiffuseTime() * 3;
		return elapsed > threshold;
	}
}
