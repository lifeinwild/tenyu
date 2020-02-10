package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.Package;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import glb.*;
import io.netty.channel.*;

/**
 * エンドユーザーがGUIから操作をしたことに応じて発生するメッセージ。
 * 成功か失敗かをGUIで通知する。
 *
 * GUIから操作されたという事は、通信の結果についてGUIで通知する必要がある。
 * GuiCausedRequestはレスポンスの型をGui表示を伴うものに限定する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GuiCausedRequest extends Request {
	protected abstract boolean validateGuiCausedConcrete(Message m);

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return getName().length() < 200 && validateGuiCausedConcrete(m);
	}

	@Override
	public final boolean received(ChannelHandlerContext ctx,
			Received validated) {
		ResultCode code = receivedConcrete(validated);

		//返信	結果コードを伝える
		StandardResponseGui res = new StandardResponseGui(code);
		Package p = res.createPackage();
		if (p == null || res == null)
			return false;
		Message resM = Message.build(res).packaging(p).finish();
		return Glb.getP2p().response(resM, ctx);
	}

	/**
	 * GuiCausedRequest系は抽象クラスでレスポンスを送信するコードや
	 * レスポンスの型が限定されているので、
	 * 子クラスはこのメソッドでそれに合わせてレスポンスの内容を返す。
	 *
	 * @param validated
	 * @return		レスポンスの内容
	 */
	protected abstract ResultCode receivedConcrete(Received validated);

	/**
	 * @return	エンドユーザーが読める処理名
	 */
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof StandardResponseGui;
	}
}
