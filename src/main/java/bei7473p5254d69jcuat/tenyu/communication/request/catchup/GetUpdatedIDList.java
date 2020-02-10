package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import glb.*;
import io.netty.channel.*;

/**
 * 客観系ストアの更新されたID一覧を取得するメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetUpdatedIDList extends AbstractByStoreMessage {
	private long historyIndex;

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	@Override
	protected boolean validateAbstractByStoreMessageConcrete(Message m) {
		return historyIndex >= 0;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetUpdatedIDListResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		GetUpdatedIDListResponse res = new GetUpdatedIDListResponse();
		res.setStoreName(storeName);
		Glb.getObje().read(txn -> {
			IdObjectStore<?, ?> s = storeName.getStore(txn);
			res.setList(
					s.getCatchUpUpdatedIDListStore().get((Long) historyIndex));
		});

		//nullでも返す
		//		if (res.getList() == null || res.getList().getUpdated() == null)
		//			return false;

		Message resM = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class GetUpdatedIDListResponse
			extends AbstractByStoreMessageResponse {
		private CatchUpUpdatedIDList list;

		public CatchUpUpdatedIDList getList() {
			return list;
		}

		public void setList(CatchUpUpdatedIDList list) {
			this.list = list;
		}

		@Override
		protected boolean validateAbstractByStoreMessageResponseConcrete(
				Message m) {
			return true;//通信においては、CatchUpUpdatedIDListはnullもある
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetUpdatedIDList;
		}

	}

}
