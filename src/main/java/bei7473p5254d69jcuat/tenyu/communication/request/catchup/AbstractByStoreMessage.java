package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import io.netty.channel.*;

/**
 * 客観DB問い合わせ。
 * ストア毎にメッセージを作成する。
 * 複数ストアを一括して問い合わせ無い。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AbstractByStoreMessage extends P2PEdgeCommonKeyRequest {
	protected StoreNameObjectivity storeName;

	public StoreNameObjectivity getStoreName() {
		return storeName;
	}

	public void setStoreName(StoreNameObjectivity storeName) {
		this.storeName = storeName;
	}

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return storeName != null && validateAbstractByStoreMessageConcrete(m);
	}

	protected abstract boolean validateAbstractByStoreMessageConcrete(
			Message m);

	public static abstract class AbstractByStoreMessageResponse
			extends P2PEdgeCommonKeyResponse {
		protected StoreNameObjectivity storeName;

		public StoreNameObjectivity getStoreName() {
			return storeName;
		}

		public void setStoreName(StoreNameObjectivity storeName) {
			this.storeName = storeName;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			return true;//リクエストを送信した箇所で処理するのでここでは何もしない
		}

		protected abstract boolean validateAbstractByStoreMessageResponseConcrete(
				Message m);

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return storeName != null
					&& validateAbstractByStoreMessageResponseConcrete(m);
		}
	}
}
