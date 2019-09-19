package bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge;

import java.io.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.role.*;
import io.netty.channel.*;

/**
 * 自分の最新のアドレスを相手に通知する。
 * もしUserEdgeが無ければ再作成する。
 * 共通鍵が古ければ再交換する。
 * 起動直後、または定期的に使用する想定。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserEdgeGreeting extends UserRequest {
	/**
	 * リクエスト側アドレス
	 */
	private AddrInfo addr = Glb.getSubje().getMyAddrInfo();

	/**
	 * リクエストしたノードのノードナンバー
	 */
	private int nodeNumber = Glb.getSubje().getMe().getNodeNumber();

	public AddrInfo getAddr() {
		return addr;
	}

	/**
	 * 全アドレス解決サーバに送信する
	 * @return	例外が起きなかった
	 */
	public static boolean send() {
		Role role = Glb.getObje().getRole(
				rs -> rs.getByName(UserAddrServer.getModuleNameStatic()));
		if (role.getAdminUserIds().size() == 0)
			return false;

		for (Long serverUserId : role.getAdminUserIds()) {
			User u = Glb.getObje().getUser(us -> us.get(serverUserId));
			if (u == null)
				continue;
			NodeIdentifierUser identifier = u
					.getNodeIdentifierByRole(role.getRecycleId());
			AddrInfo addr = u.tryToGetAddr(identifier.getNodeNumber());
			if (!send(identifier, addr, true)) {
				Glb.getLogger()
						.warn("Failed to send UserEdgeGreeting userId="
								+ serverUserId + " addr=" + addr,
								new IOException());
			}
		}
		return true;

	}

	/**
	 * @param identifier	相手のユーザー
	 * @param addr		相手のアドレス
	 * @param cke		通信に失敗したら自動的にUserEdgeの確立を行うか
	 * @return	正常にUserEdgeが存在しているか
	 */
	public static boolean send(NodeIdentifierUser identifier, AddrInfo addr,
			boolean cke) {
		if (identifier == null || addr == null)
			return false;
		UserEdgeGreeting req = new UserEdgeGreeting();
		Message reqM = Message.build(req)
				.packaging(req.createPackage(identifier)).finish();
		Message resM = Glb.getP2p().requestSync(reqM, addr.getISAP2PPort());
		if (Response.fail(resM) && cke) {
			//失敗したら共通鍵を再度交換する
			return Glb.getMiddle().getUserEdgeList()
					.commonKeyExchange(identifier, addr.getISAP2PPort());
		}
		return true;
	}

	@Override
	protected boolean validateRequestConcrete(Message m) {
		ValidationResult vr = new ValidationResult();
		if (addr == null || !addr.validateAtCommon(vr)) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof UserEdgeGreetingResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		Long requestorUserId = validated.getMessage().getUserId();

		//アドレスを更新する
		if (!Glb.getMiddle().getUserEdgeList().updateAddr(requestorUserId, addr,
				nodeNumber)) {
			return false;
		}

		//返信
		UserEdgeGreetingResponse res = new UserEdgeGreetingResponse();
		Message resM = Message.build(res).packaging(
				res.createPackage(Glb.getMiddle().getMyNodeIdentifierUser()))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class UserEdgeGreetingResponse extends UserResponse {

		@Override
		protected boolean validateResponseConcrete(Message m) {
			return true;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof UserEdgeGreeting;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}

	}

}
