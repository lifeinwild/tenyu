package bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import io.netty.channel.*;

/**
 * 整合性情報を取得するメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetIntegrity extends P2PEdgeCommonKeyRequest {
	public static AsyncRequestStatesNoRetry<GetIntegrity> sendBatch() {
		//最近接続した近傍一覧
		List<P2PEdge> neighbors = Glb.getSubje().getNeighborList()
				.getNeighborsRecentlyConnected();
		ReadonlyNeighborList neighborList = new ReadonlyNeighborList(neighbors);

		//整合性情報取得の通信の状態管理オブジェクト
		AsyncRequestStatesNoRetry<
				GetIntegrity> integrityState = new AsyncRequestStatesNoRetry<>(
						() -> neighborList);

		//整合性情報取得メッセージ
		//可能な限り多くの近傍に問い合わせる
		integrityState.requestToAll(to -> new GetIntegrity());

		return integrityState;
	}

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return true;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetIntegrityResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		//返信
		GetIntegrityResponse res = new GetIntegrityResponse();

		res.setIntegrity(Glb.getObje().getIntegrity());
		res.setVeteran(Glb.getMiddle().getObjeCatchUp().imVeteran());

		//送信
		Message m = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();
		return Glb.getP2p().response(m, ctx);
	}

	public static class GetIntegrityResponse extends P2PEdgeCommonKeyResponse {
		private Integrity integrity;

		private boolean veteran = false;

		public void setVeteran(boolean veteran) {
			this.veteran = veteran;
		}

		public boolean isVeteran() {
			return veteran;
		}

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return integrity != null && integrity.validate();
		}

		public Integrity getIntegrity() {
			return integrity;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetIntegrity;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			validated.getEdgeByInnermostPackage().getNode().setVeteran(veteran);
			return true;//リクエスト元で処理
		}

		public void setIntegrity(Integrity integrity) {
			this.integrity = integrity;
		}

	}

}
