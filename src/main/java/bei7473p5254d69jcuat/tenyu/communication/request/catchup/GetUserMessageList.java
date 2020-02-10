package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import io.netty.channel.*;

/**
 * ユーザーメッセージリストを取得するメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetUserMessageList extends P2PEdgeCommonKeyRequest {
	/**
	 * 取得するリストのヒストリーインデックス
	 * ただし初期値のままなら最新のリストが返される
	 */
	private long historyIndex = -1;

	@Override
	protected boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return historyIndex >= -1;
		//		GetUserMessageList c = (GetUserMessageList) m.getContent();
		//		return c.getHistoryIndex() != -1;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetUserMessageListResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		UserMessageList l;
		if (historyIndex == -1) {
			l = Glb.getMiddle().getObjectivityUpdateSequence().getUserMessageList();
		} else {
			l = Glb.getMiddle().getObjeCatchUp().getLog().get(historyIndex);
		}

		if (l == null)
			return false;
		GetUserMessageListResponse res = new GetUserMessageListResponse();
		res.setList(l);
		P2PEdge e = validated.getEdgeByInnermostPackage();
		Message resM = Message.build(res).packaging(res.createPackage(e))
				.finish();
		return Glb.getP2p().response(resM, ctx);

	}

	public static class GetUserMessageListResponse
			extends P2PEdgeCommonKeyResponse {
		private UserMessageList list;

		public UserMessageList getList() {
			return list;
		}

		public void setList(UserMessageList list) {
			this.list = list;
		}

		@Override
		protected boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			return list != null;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetUserMessageList;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;//データ取得系なのでやる事無し
		}

	}

}
