package bei7473p5254d69jcuat.tenyu.communication;

import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.Package;
import glb.*;
import io.netty.channel.*;

/**
 * メッセージは梱包と内容に分かれる。これは内容。
 * 現状、梱包と内容の関係は制限され、自由に組み合わせれないようにしている。
 * その制限は、各種梱包がPackageAContentという空のインターフェースを持っているので
 * 内容がそれを実装することで作られる。受信者は梱包から内容を取り出した時
 * その内容がその梱包のインターフェースを実装しているかをチェックする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class MessageContent extends Communicatable {

	/**
	 * 作成された時点のヒストリーインデックス
	 * この情報はHI更新の前後でノード毎にばらつく可能性がある。
	 * しかし用途がある程度トレランスのある判定に使われるので問題ない。
	 */
	protected long createHistoryIndex = Glb.getObje().getCore()
			.getHistoryIndex();

	/**
	 * メッセージ作成日時
	 * この情報はメッセージ作成者によってのみ設定されるのでばらける事が無い
	 */
	protected long createDate = Glb.getUtil().now();

	public long getCreateDate() {
		return createDate;
	}

	/**
	 * @return	このメッセージを客観に反映する際の負荷の大きさ。
	 * 通常のメッセージは1。内容が可変長で巨大になりうる場合、このメソッドを
	 * オーバーライドし、メッセージ毎の負荷を考慮した数値を返す。
	 */
	public int getApplySize() {
		return 1;
	}

	/**
	 * 各メッセージのID
	 * メッセージの重複を防止するために導入を考えたが、現状没案
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class MessageId {
		/**
		 * メッセージを作成したプロセスのノードID
		 */
		private int nodeNumber = -1;
		private long subjeId = -1;

		private MessageId() {
		}

		private MessageId(int nodeNumber, long subjeId) {
			this.nodeNumber = nodeNumber;
			this.subjeId = subjeId;
		}

		public static MessageId build() {
			return new MessageId(Glb.getConf().getNodeNumber(),
					Glb.getSubje().getNextSubjeMessageId());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + nodeNumber;
			result = prime * result + (int) (subjeId ^ (subjeId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageId other = (MessageId) obj;
			if (nodeNumber != other.nodeNumber)
				return false;
			if (subjeId != other.subjeId)
				return false;
			return true;
		}

		public boolean validate() {
			return nodeNumber != -1 && subjeId != -1;
		}

		public int getNodeNumber() {
			return nodeNumber;
		}

		public long getSubjeId() {
			return subjeId;
		}
	}

	/**
	 * 自分のメッセージで一意なID
	 */
	private MessageId subjeMessageId = MessageId.build();

	public MessageId getSubjeMessageId() {
		return subjeMessageId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageContent other = (MessageContent) obj;
		if (createHistoryIndex != other.createHistoryIndex)
			return false;
		if (subjeMessageId == null) {
			if (other.subjeMessageId != null)
				return false;
		} else if (!subjeMessageId.equals(other.subjeMessageId))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ (int) (createHistoryIndex ^ (createHistoryIndex >>> 32));
		result = prime * result
				+ ((subjeMessageId == null) ? 0 : subjeMessageId.hashCode());
		return result;
	}

	public long getCreateHistoryIndex() {
		return createHistoryIndex;
	}

	/**
	 * メッセージの有効期間
	 */
	protected static final long expirationHistoryIndex = 2;

	public static long getExpiration() {
		return expirationHistoryIndex;
	}

	private boolean isValidCreateHistory() {
		return Glb.getObje().getCore().getHistoryIndex()
				- createHistoryIndex < expirationHistoryIndex;
	}

	/**
	 * このメッセージを受信した時の処理。
	 * 特にセキュリティを気にして書く。
	 * @param ctx	通信の文脈。返信用インターフェースに渡す等
	 * @param validated		検証済みメッセージ
	 * @return		処理に成功したか
	 */
	public abstract boolean received(ChannelHandlerContext ctx,
			Received validated);

	@Override
	protected final boolean validateConcrete(Message m) {
		return isValidCreateHistory() && validateConcreteContent(m);
	}

	protected abstract boolean validateConcreteContent(Message m);

	//各具象クラスはここからパッケージングとP2Pエッジ特定方法を選択する
	protected Package packagingPlainEdgeIdUnsecure(Message m) {
		PlainPackage pack = new PlainPackage();
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

	protected Package packagingPlainNoEdge(Message m) {
		PlainPackage pack = new PlainPackage();
		/*
		NoEdgeDetector detector = new NoEdgeDetector();
		pack.setDetector(detector);
		*/
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

	protected Package packagingUserNoEdge(Message m) {
		SignedPackage pack = new SignedPackage();
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

	//TODO メソッド名からディテクター名を除外する
	protected Package packagingCommonKeyEdgeId(Message m) {
		P2PEdgeCommonKeyPackage pack = new P2PEdgeCommonKeyPackage();
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

	/*
		protected Package packagingRsaEdgeId(Detector d) {
			RSAPackage pack = new RSAPackage();
			pack.binarizeAndSetContent(this, d);
			return pack;
		}

		protected Package packagingRsaNoEdge(Detector d) {
			RSAPackage pack = new RSAPackage();
	//		NoEdgeDetector detector = new NoEdgeDetector();
	//		pack.setDetector(detector);
			pack.binarizeAndSetContent(this, d);
			return pack;
		}

	protected Package packagingRsaEdgeIdUnsecure(Detector d) {
		RSAPackage pack = new RSAPackage();

	//	EdgeIdDetectorUnsecure detector = new EdgeIdDetectorUnsecure();
	//	detector.setP2pEdgeIdCreator(to.getEdgeId());
	//	detector.setP2pEdgeIdReceiver(to.getFromOther().getEdgeId());
	//	pack.setDetector(detector);

		pack.binarizeAndSetContent(this, d);
		return pack;
	}

	*/
	//仮の近傍リストを使用するタイプ TODO unsecureという区別は不要では？
	protected Package packagingPlainNoEdgeUnsecure(Message m) {
		PlainPackage pack = new PlainPackage();
		/*
		NoEdgeDetectorUnsecure detector = new NoEdgeDetectorUnsecure();
		pack.setDetector(detector);
		*/
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

	protected Package packagingCommonKeyEdgeIdUnsecure(Message m) {
		P2PEdgeCommonKeyPackage pack = new P2PEdgeCommonKeyPackage();
		/*
		EdgeIdDetectorUnsecure detector = new EdgeIdDetectorUnsecure();
		detector.setP2pEdgeIdCreator(to.getEdgeId());
		detector.setP2pEdgeIdReceiver(to.getFromOther().getEdgeId());
		pack.setDetector(d);
		*/
		pack.binarizeAndSetContent(this, m);
		return pack;
	}

}
