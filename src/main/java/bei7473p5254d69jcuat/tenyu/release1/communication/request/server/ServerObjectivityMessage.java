package bei7473p5254d69jcuat.tenyu.release1.communication.request.server;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.Package;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import io.netty.channel.*;

/**
 * サーバーから送信されるユーザーメッセージリストに登録されるメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class ServerObjectivityMessage extends Request
		implements UserMessageListRequestI {

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		//サーバー権限を持つユーザーか
		Package p = m.getInnermostPack();
		if (p == null || !(p instanceof SignedPackage))
			return false;
		SignedPackage sp = (SignedPackage) p;
		Long signer = sp.getSignerUserId();
		if (signer == null)
			return false;
		List<Long> servers = getServers();
		if (servers == null)
			return false;
		if (!servers.contains(signer)) {
			return false;
		}

		return validateServerObjectivityMessageConcrete(m);
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof StandardResponse;
	}

	@Override
	public final boolean received(ChannelHandlerContext ctx,
			Received validated) {
		ResultCode code = ResultCode.DEFAULT;
		if (Glb.getMiddle().getUserMessageListServer()
				.receive(validated.getMessage())) {
			code = ResultCode.SUCCESS;
		} else {
			code = ResultCode.FAIL;
		}

		//返信	結果コードを伝える
		StandardResponse res = new StandardResponse(code);
		Package p = res.createPackage();
		if (p == null || res == null)
			return false;
		Message resM = Message.build(res).packaging(p).finish();
		return Glb.getP2p().response(resM, ctx);
	}

	/**
	 * @return	その種類のメッセージをメッセージ受付サーバに受信させる
	 * 権限を持ったユーザーID一覧
	 */
	protected abstract List<Long> getServers();

	protected abstract boolean validateServerObjectivityMessageConcrete(
			Message m);
}
