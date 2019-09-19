package bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import io.netty.channel.*;

/**
 * リサイクルIDを取得するメッセージ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetRecycleIDList extends AbstractByStoreMessage {
	/**
	 * 100万件ずつしか取得できないので
	 * そのページインデックス
	 */
	private int index = 0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	protected boolean validateAbstractByStoreMessageConcrete(Message m) {
		return index >= 0;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetRecycleIDListResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		GetRecycleIDListResponse res = new GetRecycleIDListResponse();
		res.setStoreName(storeName);
		Glb.getObje().read(txn -> {
			IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);
			res.setList(s.getRecycleIdStore().getIDList(index));
		});

		if (res.getList() == null)
			return false;

		P2PEdge e = validated.getEdgeByInnermostPackage();
		Message resM = Message.build(res).packaging(res.createPackage(e))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class GetRecycleIDListResponse
			extends AbstractByStoreMessageResponse {
		private List<IDList> list = null;

		public List<IDList> getList() {
			return list;
		}

		public void setList(List<IDList> list) {
			this.list = list;
		}

		@Override
		protected boolean validateAbstractByStoreMessageResponseConcrete(
				Message m) {
			return list != null;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetRecycleIDList;
		}

	}
}
