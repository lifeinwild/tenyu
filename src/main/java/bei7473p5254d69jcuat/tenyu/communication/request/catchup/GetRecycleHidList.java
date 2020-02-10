package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * リサイクルHidを取得するメッセージ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetRecycleHidList extends AbstractByStoreMessage {
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
		return res instanceof GetRecycleHidListResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		GetRecycleHidListResponse res = new GetRecycleHidListResponse();
		res.setStoreName(storeName);
		Glb.getObje().read(txn -> {
			IdObjectStore<?, ?> s = storeName.getStore(txn);
			res.setList(s.getRecycleHidStore().getIDList(index));
		});

		if (res.getList() == null)
			return false;

		P2PEdge e = validated.getEdgeByInnermostPackage();
		Message resM = Message.build(res).packaging(res.createPackage(e))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class GetRecycleHidListResponse
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
			return req instanceof GetRecycleHidList;
		}

	}
}
