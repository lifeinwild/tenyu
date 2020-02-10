package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import io.netty.channel.*;

/**
 * オブジェクトをIDの範囲で取得する。
 * IDリストを持つ必要が無く、下限と件数を指定する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetObjRange extends P2PEdgeCommonKeyRequest {
	/**
	 * 一度に取得できる最大件数
	 */
	private static final int max = 1000 * 30 * 3;

	public static int getMax() {
		return max;
	}

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return count > 0 && count < max && startId >= 0 && storeName != null;
	}

	/**
	 * 件数。startId+countのIDは含まれない。
	 * この仕様はfor(int i=0;i<max;i++)に合わせている
	 */
	private long count;
	/**
	 * このIDから。このIDを含む
	 */
	private long startId;
	private StoreNameObjectivity storeName;

	public long getCount() {
		return count;
	}

	public long getStartId() {
		return startId;
	}

	public StoreNameObjectivity getStoreName() {
		return storeName;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetObjRangeResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		//返信されるメッセージ
		GetObjRangeResponse res = new GetObjRangeResponse();
		boolean r = Glb.getObje().readRet(txn -> {
			//対象ストア
			IdObjectStore<?, ?> s = storeName.getStore(txn);
			//要求されたIDを順次設定
			for (long i = startId; i < count; i++) {
				IdObjectDBI o = s.getRawObj(i);
				//抜けがあるデータを返すと同調処理を壊す可能性がある
				if (o == null)
					return false;
				res.getObjs().add(o);
			}
			return true;
		});
		if (!r)
			return false;
		//返信
		Message m = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();
		Glb.getP2p().response(m, ctx);
		return true;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setStartId(long startId) {
		this.startId = startId;
	}

	public void setStoreName(StoreNameObjectivity storeName) {
		this.storeName = storeName;
	}

	public static class GetObjRangeResponse extends P2PEdgeCommonKeyResponse {
		/**
		 * 問い合わされたオブジェクトの一覧
		 */
		private List<IdObjectDBI> objs = new ArrayList<>();

		public List<IdObjectDBI> getObjs() {
			return objs;
		}

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return objs != null && objs.size() > 0;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetObjRange;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			return true;
		}

		public void setObjs(List<IdObjectDBI> objs) {
			this.objs = objs;
		}

	}
}
