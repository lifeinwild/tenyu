package bei7473p5254d69jcuat.tenyu.communication.request;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import io.netty.channel.*;

/**
 * 最新のメッセージリストを持っているか問い合わせる
 * @author exceptiontenyu@gmail.com
 *
 */
public class HasUserMessageList extends P2PEdgeCommonKeyRequest {
	/**
	 * このヒストリーインデックスのメッセージリストを取得する。
	 */
	private long historyIndex = -1L;

	@SuppressWarnings("unused")
	private HasUserMessageList() {
	}

	public HasUserMessageList(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof HasUserMessageListResponse;
	}

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		if (historyIndex < ObjectivityCore.firstHistoryIndex) {
			return false;
		}
		return true;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		//最新のメッセージリストを取得してみる
		ObjectivityUpdateSequence seq = Glb.getMiddle()
				.getObjectivityUpdateSequence();

		if (seq == null || seq.getUserMessageList() == null || seq
				.getUserMessageList().getNextHistoryIndex() != historyIndex) {
			Glb.debug("invalid request");
			return false;
		}

		//返信作成
		HasUserMessageListResponse res = new HasUserMessageListResponse();
		if (seq != null) {
			UserMessageList list = seq.getUserMessageList();
			//空じゃなければtrueを設定
			if (list != null) {
				res.setHas(true);
			}
		}
		//送信
		Message resM = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class HasUserMessageListResponse
			extends P2PEdgeCommonKeyResponse {
		/**
		 * メッセージリストを持っているか
		 */
		private boolean has = false;

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return true;
		}

		public boolean isHas() {
			return has;
		}

		public void setHas(boolean has) {
			this.has = has;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof HasUserMessageList;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			return true;//リクエストした箇所で処理
		}

	}
}
