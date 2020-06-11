package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.PlainPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * P2Pネットワーク上のアドレス解決サーバに問い合わせてアドレスを取得する。
 * ユーザーIDとノード番号で絞り込む。
 *
 * 取得されたアドレスが最新か、現在相手のノードがオンラインか等は保証されない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetUserAddr extends Request implements PlainPackageContent {
	private static final long period = 1000L * 60 * 1;
	private static final long max = 1;
	/**
	 * アドレス解決サーバへの問い合わせペース制限
	 * 同じユーザーについて短時間の間に何度も問い合わせない
	 */
	private static final ThroughputLimit<
			NodeIdentifierUser> requestLimit = new ThroughputLimit<>(period,
					max);

	/**
	 * 接続可能かつuserIdで指定された相手かを確認する
	 * @param userId
	 * @param addrP2p
	 * @return	userIdとaddrの対応関係が正しいか
	 */
	/*
	public static boolean check(NodeIdentifierUser userId, AddrInfo addr) {
		return UserEdgeGreeting.send(userId, addr);
	}
	*/

	public static AddrInfo get(Long userId, int nodeNumber) {
		return get(new NodeIdentifierUser(userId, nodeNumber));
	}

	public static AddrInfo get(NodeIdentifierUser identifier) {
		/*	単純に書くべき。
		 * ここからtryToGetAddrを呼び出すと、
		 * その中でこれがまた呼ばれるのでループする可能性がある。
		 *
		//既存のデータから返す
		UserEdge ue = Glb.getMiddle().getUserEdgeList().getEdge(identifier);
		if (ue != null) {
			AddrInfo r = ue.getAddr();
			if (r != null) {
				if (check(identifier, r))
					return r;
			}
		}

		//サーバー系ならユーザー情報にFQDNがあるので、UserStoreから探す
		User u = UserStore.getSimple(identifier.getUserId());
		if (u != null) {
			AddrInfo addr = u.tryToGetAddr(identifier.getNodeNumber());
			if(addr != null)
				return addr;
		}
		*/

		//アドレス解決サーバーに問い合わせる
		Role addrServers = Glb.getObje().getRole(
				rs -> rs.getByName(UserAddrServer.getModuleNameStatic()));
		if (addrServers.getAdminUserIds().size() == 0)
			return null;

		if (requestLimit.isOverCount(identifier)) {
			return null;
		}

		Middle mid = Glb.getMiddle();
		GetUserAddr req = new GetUserAddr();
		req.setIdentifier(identifier);
		Message reqM = Message.build(req).packaging(req.createPackage())
				.finish();
		String rName = UserAddrServer.getModuleNameStatic();
		Message resM = Glb.getP2p().requestToServer(id -> reqM,
				addrServers.getAdminNodes(), mid.getCachedServer(rName),
				addrServers.getId(),
				id -> mid.removeCachedServer(rName, id),
				id -> mid.putCachedServer(rName, id));
		if (Response.fail(resM))
			return null;
		GetUserAddrResponse res = (GetUserAddrResponse) resM.getContent();
		return res.getAddr();
	}

	private NodeIdentifierUser identifier;

	public NodeIdentifierUser getIdentifier() {
		return identifier;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetUserAddrResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		UserEdge ue = Glb.getMiddle().getUserEdgeList().getEdge(identifier);

		if (ue == null)
			return false;
		AddrInfo addr = ue.getAddr();
		if (addr == null)
			return false;

		GetUserAddrResponse res = new GetUserAddrResponse();
		res.setAddr(addr);
		Message m = Message.build(res).packaging(res.createPackage()).finish();

		return Glb.getP2p().response(m, ctx);
	}

	public void setIdentifier(NodeIdentifierUser identifier) {
		this.identifier = identifier;
	}

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return identifier != null && identifier.validate();
	}

	public static class GetUserAddrResponse extends Response
			implements PlainPackageContent {
		private AddrInfo addr;

		public AddrInfo getAddr() {
			return addr;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetUserAddr;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}

		public void setAddr(AddrInfo addr) {
			this.addr = addr;
		}

		@Override
		protected boolean validateResponseConcrete(Message m) {
			return addr != null;
		}
	}

}
