package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.HashStore.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import io.netty.channel.*;

/**
 * ハッシュツリー上の特定のハッシュ配列を取得するメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetHashArray extends AbstractByStoreMessage {
	private byte[] key;

	@Override
	protected final boolean validateAbstractByStoreMessageConcrete(Message m) {
		return key != null && key.length > 0;
	}

	public byte[] getKey() {
		return key;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetHashArrayResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		Glb.debug("storeName=" + storeName + " key=" + HashStoreKey.parse(key));

		HashStoreRecordPositioned r = HashStore.getHashArraySimple(key,
				storeName);
		GetHashArrayResponse res = new GetHashArrayResponse();
		res.setStoreName(storeName);
		res.setHashArray(r);
		P2PEdge e = validated.getEdgeByInnermostPackage();
		Message m = Message.build(res).packaging(res.createPackage(e)).finish();
		return Glb.getP2p().response(m, ctx);
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public static class GetHashArrayResponse
			extends AbstractByStoreMessageResponse {
		private HashStoreRecordPositioned hashArray;

		public HashStoreRecordPositioned getHashArray() {
			return hashArray;
		}

		@Override
		protected final boolean validateAbstractByStoreMessageResponseConcrete(
				Message m) {
			return hashArray != null && hashArray.validate();
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetHashArray;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			return true;
		}

		public void setHashArray(HashStoreRecordPositioned hashArray) {
			this.hashArray = hashArray;
		}
	}

}
