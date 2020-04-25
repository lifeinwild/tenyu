package bei7473p5254d69jcuat.tenyu.communication.request.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.Subjectivity.*;
import glb.*;
import io.netty.channel.*;

/**
 * 挨拶。共通鍵交換の前の公開鍵交換、グローバルアドレスの通知、最新の自分の情報の通知など、
 * 様々な目的で行われる。共通点は他の通信のための準備であるという点である。
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class PeriodicNotification extends P2PEdgeCommonKeyRequest {
	public static void send(UpdatableNeighborList neighbors) {
		List<RequestFutureP2PEdge> results = new ArrayList<>();
		for (P2PEdge to : neighbors.getNeighborsUnsafe()) {
			if (!to.getCommonKeyExchangeState().isSucceed())
				continue;

			try {
				//DNSがあればIPアドレスを更新
				to.getNode().updateAddrByFqdn();

				//通知処理
				PeriodicNotification req = new PeriodicNotification();
				req.setRequestor(Glb.getSubje().getMe());
				req.setResponderGlobalAddr(to.getNode().getAddr());
				Message m = Message.build(req).packaging(req.createPackage(to))
						.finish();
				RequestFutureP2PEdge state = Glb.getP2p().requestAsync(m, to);
				if (state == null) {
					Glb.debug("");
				} else {
					Glb.debug("periodic notification requested");
				}
				results.add(state);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				to.incrementConnectionFailedCount();
			}
		}

		if (results.size() == 0)
			return;

		int i = 0;
		while (results.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			i = Glb.getRnd().nextInt(results.size());
			RequestFutureP2PEdge state = results.get(i);
			if (state.getState().isDone()) {
				state.getState().channel().close();
				results.remove(i);
				if (state.isSuccess() && state.getHandler()
						.getRes() instanceof PeriodicNotificationResponse) {
					//PeriodicNotificationResponse#received()で処理される
				} else {
					Glb.debug(() -> "notification fail:"
							+ state.getTo().getNode().getP2pPort());
					state.getTo().incrementConnectionFailedCount();
				}
			}
		}
	}

	/**
	 * 送信先アドレス。自分のグローバルIPアドレスを知るため、互いに送り合う。
	 */
	private byte[] responderGlobalAddr;

	private P2PNode requestor;

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return responderGlobalAddr != null && responderGlobalAddr.length > 0
				&& requestor != null && requestor.validateAtCommon();
	}

	@Override
	public boolean isValid(Response res) {
		if (!(res instanceof PeriodicNotificationResponse))
			return false;
		return true;
	}

	@Override
	public boolean received(ChannelHandlerContext con, Received validated) {
		P2PEdge n = validated.getEdgeByInnermostPackage();
		if (n == null) {
			Glb.getLogger().error("", new Exception());
			return false;
		}

		n.updateLastCommunication();
		n.incrementConnectionCount();

		//相手が主張する自分のグローバルアドレスを設定する
		n.setMyGlobalAddr(responderGlobalAddr);
		n.getNode().update(requestor);

		//返事
		PeriodicNotificationResponse res = new PeriodicNotificationResponse();
		res.setRequestorGlobalAddr(P2P.isa(con).getAddress().getAddress());
		P2PNode me = Glb.getSubje().getMe();
		setupP2PNetworkObservation(me);
		res.setResponder(me);

		//送信
		Message m = Message.build(res).packaging(res.createPackage(n)).finish();
		Glb.getP2p().response(m, con);
		return true;
	}

	/**
	 * nに必要なら{@link P2PNetworkObservationByNode}をセットする。
	 * @param n
	 */
	private void setupP2PNetworkObservation(P2PNode n) {
		//ネットワーク状況のデータ量が大きいからあえてnullをセットする
		//こうしないとメッセージ受付サーバから近傍へノード情報が伝わった時(GetAddresses)
		//ネットワーク状況つきのノード情報が拡散してしまう
		n.setObservation(null);
		try {
			Long requestorUserId = Glb.getObje()
					.getUser(us -> us.getId(requestor.getType(),
							requestor.getPubKey().getByteArray()));
			if (requestorUserId == null)
				return;//ユーザー登録前のノードは例外ではない

			//ネットワーク状況を伝えるか
			boolean setup = false;
			//requestorが全体運営者だったら
			if (Glb.getObje().getCore().getManagerList()
					.isManager(requestorUserId)) {
				setup = true;
			} else {
				//メッセージ受付サーバだったら
				Role role = Glb.getObje().getRole(rs -> rs.getByName(
						UserMessageListServer.class.getSimpleName()));
				if (role != null) {
					if (role.isAdmin(requestorUserId)) {
						setup = true;
					}
				}
			}

			if (setup) {
				n.setObservation(Glb.getSubje().getObservation());
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

	}

	public byte[] getResponderGlobalAddr() {
		return responderGlobalAddr;
	}

	public void setResponderGlobalAddr(byte[] responderGlobalAddr) {
		this.responderGlobalAddr = responderGlobalAddr;
	}

	public static class PeriodicNotificationResponse
			extends P2PEdgeCommonKeyResponse {
		/**
		 * 送信先アドレス
		 */
		private byte[] requestorGlobalAddr;

		private P2PNode responder;

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return requestorGlobalAddr != null && requestorGlobalAddr.length > 0
					&& responder != null && responder.validateAtCommon();
		}

		public byte[] getRequestorGlobalAddr() {
			return requestorGlobalAddr;
		}

		public void setRequestorGlobalAddr(byte[] requestorGlobalAddr) {
			this.requestorGlobalAddr = requestorGlobalAddr;
		}

		@Override
		public boolean received(ChannelHandlerContext r, Received validated) {
			P2PEdge n = validated.getEdgeByInnermostPackage();
			if (n == null) {
				Glb.getLogger().error("", new Exception());
				return false;
			}
			Glb.debug(() -> "notification success:" + n.getNode().getP2pPort());
			n.setMyGlobalAddr(requestorGlobalAddr);
			n.getNode().update(responder);

			return true;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof PeriodicNotification;
		}

		public P2PNode getResponder() {
			return responder;
		}

		public void setResponder(P2PNode responder) {
			this.responder = responder;
		}
	}

	public P2PNode getRequestor() {
		return requestor;
	}

	public void setRequestor(P2PNode requestor) {
		this.requestor = requestor;
	}

}
