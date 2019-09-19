package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;

/**
 * アドレス解決サーバ
 * 一般ノードから問い合わせを受けて任意のユーザーのアドレスとポートを返すサーバー。
 * 一般ノードは起動直後または定期的にこのサーバーに自分のアドレスとポートを通知する。
 *
 * アドレス等の情報はUserEdgeに記録される。
 * UserEdgeを確立する事で確かに相手がそのユーザーであると確認する。
 *
 * UserEdgeは引き継げないので、
 * ユーザーは普段からアドレス解決サーバの全候補ノードにUserEdgeを作成しておく。
 * だから引継ぎ処理は無い。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserAddrServer
		extends TakeOverServer<TakeOverMessageUserAddrServer> {

	@Override
	public List<NodeIdentifierUser> getCurrentServers(
			List<NodeIdentifierUser> onlineServerCandidates) {
		if (onlineServerCandidates == null
				|| onlineServerCandidates.size() == 0)
			return null;
		//このRoleでは全候補が同時稼働する
		return onlineServerCandidates;
	}

	@Override
	public void takeover(TakeOverMessageUserAddrServer message) {
		//引継ぎ無し
	}

	@Override
	public List<NodeIdentifierUser> getServerCandidates() {
		return Glb.getObje()
				.getRole(rs -> rs.getByName(getModuleName()).getAdminNodes());
	}

	@Override
	public void registerToOnlineChecker() {
		Glb.getMiddle().getOnlineChecker().register(getModuleName(),
				new OnlineCheckerFuncs(() -> checkAndStartOrStop(),
						() -> getServerCandidates()));
	}

	@Override
	public String getModuleName() {
		return getModuleNameStatic();
	}

	public static String getModuleNameStatic() {
		return UserAddrServer.class.getSimpleName();
	}

	@Override
	public boolean sendInheritingMessage(NodeIdentifierUser nextServer) {
		return true;//引継ぎの必要無し
	}

}
