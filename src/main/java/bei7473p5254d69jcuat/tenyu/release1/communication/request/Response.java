package bei7473p5254d69jcuat.tenyu.release1.communication.request;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.AbstractStandardResponse.*;

/**
 * received()は、レスポンスを受け取った側、つまり
 * リクエストした側で実行される。
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class Response extends Content {
	/**
	 * このリクエストに応じて返されたレスポンス
	 */
	protected transient Message req;

	/**
	 * 返信全般このメソッドで成否をチェックする
	 * @param resM
	 * @return	返信に成功したか
	 */
	public static boolean success(Message resM) {
		if (resM == null || resM.getContent() == null)
			return false;

		if (resM.getContent() instanceof StandardResponse) {
			return success((StandardResponse) resM.getContent());
		} else {
			return true;
		}
	}

	/**
	 * 通信系APIはリクエストにおいて接続に失敗した場合返値をnullにする必要があるという仕様を規定するメソッド
	 *
	 * @param resM	これがnullの場合のみtrueを返す
	 * @return	接続に失敗した事で返信を受け取れなかったか
	 */
	public static boolean failConnection(Message resM) {
		return resM == null;
	}

	public static boolean fail(Message resM) {
		return !success(resM);
	}

	public static boolean success(StandardResponse res) {
		if (res == null || res.getCode() == null)
			return false;
		return res.getCode() == ResultCode.SUCCESS;
	}

	protected abstract boolean validateResponseConcrete(Message m);

	@Override
	protected final boolean validateConcreteContent(Message m) {
		return validateResponseConcrete(m);
	}

	public void setReq(Message req) {
		this.req = req;
	}

	public abstract boolean isValid(Request req);

	public Message getReq() {
		return req;
	}
}
