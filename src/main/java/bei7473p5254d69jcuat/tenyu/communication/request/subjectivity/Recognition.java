package bei7473p5254d69jcuat.tenyu.communication.request.subjectivity;

import java.net.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.PlainPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * ノードが他のノードを認識するためのメッセージ
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class Recognition extends Request implements PlainPackageContent {

	/**
	 * 認識プロセスが同時に複数開始する事を防止するロックオブジェクト
	 */
	private static volatile Object recognitionLock = new Object();

	/**
	 * 共通鍵交換
	 *
	 * @param n
	 * @return
	 */
	public static boolean commonKeyExchange(P2PEdge n) {
		//checkAndSetで更新開始日時が更新される
		if (n == null || !n.getCommonKeyExchangeState().checkAndSet())
			return false;
		//共通鍵交換開始
		CommonKeyExchangeState info = n.getCommonKeyExchangeState();
		CommonKeyExchangeState infoOther = n.getFromOther()
				.getCommonKeyExchangeState();

		infoOther.setUpdateStart();

		//失敗した時に元に戻せるように
		CommonKeyExchangeState backup = info.clone();

		//交換する情報。これらはRSA暗号化で送信される。
		byte[] co = CommonKeyInfo.createConfirmation();
		CommonKeyInfo cki = CommonKeyInfo.build();

		//P2PEdgeに設定	TODO:リクエスト等完了後に設定すればいいのでは？
		info.commonKeyReset();
		info.setCommonKeyInfo(cki);
		info.setConfirmation(co);

		//リクエスト
		byte[] serialized = CommonKeyInfo.serialize(cki, co);
		byte[] encrypted = Glb.getUtil().encryptByPublicKey(
				n.getNode().getPubKey().getByteArray(), serialized);
		CommonKeyExchange exchange = new CommonKeyExchange(encrypted, n);
		Message m = Message.build(exchange).packaging(exchange.createPackage())
				.finish();
		Message exchangeResMessage = Glb.getP2p().requestSync(m, n);
		if (Response.fail(exchangeResMessage))
			return false;

		//共通鍵暗号化で通信を確認。相手が送信してきたcoを共通鍵で返せるか
		CommonKeyExchangeConfirmation confirm = new CommonKeyExchangeConfirmation(
				infoOther.getConfirmation());
		Message confirmM = Message.build(confirm)
				.packaging(confirm.createPackage(n)).finish();
		Message confirmResMessage = Glb.getP2p().requestSync(confirmM, n);
		if (confirmResMessage == null)
			return false;
		Response confirmRes = (Response) confirmResMessage.getContent();
		if (confirmRes == null)
			return false;

		//共通鍵交換及び確認の通信に成功したか
		//成功した場合、メッセージクラスのreceived()でsucceed=trueに設定される
		if (!info.isSucceed()) {
			//元に戻す
			n.setCommonKeyInfo(backup);
			return false;
		}
		//更新完了日時を設定する
		info.setUpdateEnd();
		infoOther.setUpdateEnd();

		//共通鍵交換成功
		return true;
	}

	/**
	 * 仮の近傍リストから本命の近傍リストに入れ替え
	 * @param unsecureEdge
	 * @return
	 */
	public static boolean createOrUpdateAndRemoveFromUnsecureList(
			P2PEdge unsecureEdge) {
		//仮のリストから削除
		if (!Glb.getSubje().getUnsecureNeighborList()
				.removeNeighbor(unsecureEdge.getEdgeId())) {
			Glb.debug(() -> "no edge in unsecure list." + unsecureEdge);
			return false;
		}
		//本命のリストに存在するか
		//ここでは公開鍵で検索すべき
		//本命のリストに入っているノードでIPアドレスが分からなくなり
		//新たに偶然認識プロセスが始まった場合があるから
		P2PEdge exist = Glb.getSubje().getNeighborList()
				.getNeighbor(unsecureEdge.getNode().getP2PNodeId());
		if (exist == null) {
			//新規登録
			return Glb.getSubje().getNeighborList()
					.addNeighbor(unsecureEdge) != null;
		} else {
			//更新
			exist.getNode().setAddr(unsecureEdge.getNode().getAddr());
			exist.getNode().setAddrInfo(unsecureEdge.getNode());
			exist.setCommonKeyInfo(unsecureEdge.getCommonKeyExchangeState());

			//この更新処理は既知のノードであっても共通鍵更新のために定期的に発生する。
			//あるいは、既知のノードでアドレスが変わり不通になり、偶然アドレスを知った場合等も。
			//ここでMapのキーであるエッジIDが変わってしまう。
			//TODO existのedgeIdを残すべきか、更新すべきか？edgeIdを継続した場合のセキュリティリスクの評価？
			Glb.getSubje().getNeighborList().removeNeighbor(exist.getEdgeId());
			exist.setEdgeId(unsecureEdge.getEdgeId());
			Glb.getSubje().getNeighborList().addNeighbor(exist);

			exist.getFromOther().setCommonKeyInfo(
					unsecureEdge.getFromOther().getCommonKeyExchangeState());
			exist.getFromOther()
					.setEdgeId(unsecureEdge.getFromOther().getEdgeId());
		}
		return true;
	}

	/**
	 * @param addr
	 * @param p2pPort
	 * @param edgeIdFromOther
	 * @return			認識プロセスを続行すべきか
	 */
	public static boolean sameTimeBlock(byte[] addr, int p2pPort,
			long edgeIdFromOther) {

		//既に認識を開始していたらexistが存在する
		//なおメインリストの方に入っていても認識を続行する
		//なおアドレスは日々変化するので他のノードが該当する可能性もあるが
		//仮のリストは定期的にクリアされるので混同はほぼない
		P2PEdge exist = Glb.getSubje().getUnsecureNeighborList()
				.getNeighbor(addr, p2pPort);
		if (exist != null) {
			//ここに来るのはAとBが互いにほぼ同時に開始した場合。
			//Subjectivity#recognition()で片方が開始していたら開始しないようにしているから。
			//ほぼ同時に開始した場合、世界のどこかのコンピューターで自分に対する
			//認識プロセスが既に開始されているかを知る方法は無いので、ここに来る可能性がある。
			if (exist.getEdgeId() > edgeIdFromOther)
				return false;//相手のエッジIDが小さい場合、相手の認識プロセスが停止
		}
		return true;
	}

	/**
	 * 最初に他ノードのアドレスを知った時の処理
	 * @param addr
	 * @param p2pPort
	 * @return
	 */
	public static boolean send(byte[] addr, int p2pPort) {
		return send(addr, p2pPort, null);
	}

	/**
	 * 指定したアドレスのノードを認識する。
	 *
	 * @param addr
	 * @param p2pPort
	 * @param introducer
	 * @return	認識できたか
	 */
	public static boolean send(byte[] addr, int p2pPort, P2PEdge introducer) {
		try {
			P2PEdge n = null;
			synchronized (recognitionLock) {
				//既に認識プロセスが開始していないか確認する
				P2PEdge exist = Glb.getSubje().getUnsecureNeighborList()
						.getNeighbor(addr, p2pPort);
				if (exist != null) {
					Glb.debug(() -> "既に認識プロセスが開始していたので開始しない。");
					return false;//後から開始した方が止める
				}

				//エッジを作成する
				n = new P2PEdge();
				n.getNode().setAddr(addr);
				n.getNode().setP2pPort(p2pPort);
				if (Glb.getSubje().getUnsecureNeighborList()
						.addNeighbor(n) == null)
					throw new Exception("仮近傍リストへの追加に失敗");
			}

			//認識シーケンスの最初のメッセージを作成
			Recognition reco = new Recognition();
			reco.setEdgeId(n.getEdgeId());
			reco.setRequester(Glb.getSubje().getMe());

			if (introducer == null) {
				//紹介が居ないなら近傍手動追加なので自動削除不可に
				reco.setGuiCaused(true);
			} else {
				//紹介者が居れば設定
				n.setIntroducer(introducer);
			}

			//送信
			InetSocketAddress isa = new InetSocketAddress(
					InetAddress.getByAddress(addr), p2pPort);
			Message recoM = Message.build(reco).packaging(reco.createPackage())
					.finish();
			Message resMessage = Glb.getP2p().requestSync(recoM, isa);
			if (Response.fail(resMessage) || !(resMessage
					.getContent() instanceof RecognitionResponse)) {
				Glb.debug("返信が無い。相手がオフラインか同時に相互に認識プロセスが開始された場合発生する。");
				return false;
			}

			//なおRecognitionResponseのreceived()によってnに情報が追加されている

			//tmpがnullなら相手が自分のエッジIDを正しく認識しなかった事を意味する
			//あるいは、received()で自分で削除すべきエッジと判断して削除した事を意味する
			//Recognition系はメッセージ内部にエッジIDを持つのでそれでエッジを特定する
			if (!(resMessage.getContent() instanceof RecognitionResponse)) {
				return false;
			}
			RecognitionResponse recoRes = (RecognitionResponse) resMessage
					.getContent();
			P2PEdge tmp2 = Glb.getSubje().getUnsecureNeighborList()
					.getNeighbor(n.getEdgeId());
			if (n.getFromOther().getEdgeId() != recoRes.getEdgeId())
				throw new Exception("invalid edge id");
			if (tmp2 == null)
				throw new Exception("no P2PEdge in unsecure list");

			//続けて共通鍵交換をして、失敗したら削除
			//共通鍵交換の成功は主張された公開鍵に対応する秘密鍵を持つ事を証明する

			//共通鍵交換
			if (commonKeyExchange(n)) {
				//メインの近傍リストに入れる
				//この段階で対応する秘密鍵を持つ事を証明できているので、
				//メインの近傍リストに既にその公開鍵のノードがある場合、
				//今回得た情報で更新して良い
				n.getNode().updateDiscriminated();
				return createOrUpdateAndRemoveFromUnsecureList(n);
			} else {
				throw new Exception("共通鍵交換に失敗");
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			P2PEdge exist = Glb.getSubje().getUnsecureNeighborList()
					.getNeighbor(addr, p2pPort);
			//ここでエッジを削除するのでfalseを返すか例外を投げるかは慎重に判断する必要がある
			if (exist != null)
				Glb.getSubje().getUnsecureNeighborList()
						.removeNeighbor(exist.getEdgeId());
			return false;
		}
	}

	/**
	 * メッセージ作成者から相手へのP2PEdgeのID
	 */
	private long edgeId;

	/**
	 * GUIの近傍手動追加か
	 */
	private boolean guiCaused = false;

	/**
	 * メッセージ作成者
	 */
	private P2PNode requester;

	public long getEdgeId() {
		return edgeId;
	}

	public P2PNode getRequester() {
		return requester;
	}

	public boolean isGuiCaused() {
		return guiCaused;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof RecognitionResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		Glb.debug(() -> edgeId + " : " + requester.getP2pPort());
		Subjectivity s = Glb.getSubje();
		byte[] addr = P2P.addr(P2P.isa(ctx));

		//認識プロセスの開始時点でエッジは存在していないのでもともとnull
		P2PEdge n = null;
		synchronized (recognitionLock) {
			if (!sameTimeBlock(addr, requester.getP2pPort(), edgeId)) {
				Glb.debug(() -> "既に認識プロセスが開始したので停止。");
				return false;
			}

			//もし既存のエッジがあればそれを更新する
			//ここで見つかる場合は両者が同時に認識プロセスを開始した場合だけ
			n = s.getUnsecureNeighborList().getNeighbor(addr,
					requester.getP2pPort());
			if (n == null) {
				//エッジを作成する
				n = new P2PEdge(requester);
				n.getNode().setAddr(addr);
				//仮のリストに追加
				P2PEdge add = s.getUnsecureNeighborList().addNeighbor(n);
				if (add == null)
					return false;
			} else {
				n.getNode().setPubKey(requester.getPubKey());
				n.getNode().setNodeNumber(requester.getNodeNumber());
				n.getNode().update(requester);
			}
		}
		//一部情報を更新
		n.updateLastCommunication();
		n.getFromOther().setEdgeId(edgeId);

		//近傍手動追加なら自動削除不可フラグを立てる
		n.setDontRemoveAutomatically(true);

		//レスポンスを作成する
		RecognitionResponse res = new RecognitionResponse();
		res.setResponder(Glb.getSubje().getMe());
		res.setEdgeId(n.getEdgeId());

		Glb.debug(n + "");

		Message m = Message.build(res).packaging(res.createPackage()).finish();
		return Glb.getP2p().response(m, ctx);
	}

	public void setEdgeId(long edgeId) {
		this.edgeId = edgeId;
	}

	public void setGuiCaused(boolean guiCaused) {
		this.guiCaused = guiCaused;
	}

	public void setRequester(P2PNode requester) {
		this.requester = requester;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return edgeId != 0 && edgeId != P2PEdge.getSpecialEdgeId()
				&& requester.getP2pPort() > 0 && requester.getPubKey() != null
				&& requester.getPubKey().getByteArray().length > 0
				&& requester.getRange() != null && requester.getType() != null;
	}

	public static class RecognitionResponse extends Response
			implements PlainPackageContent {
		/**
		 * 返信者から問い合わせ側へのエッジID
		 */
		private long edgeId;
		/**
		 * 返信者
		 */
		private P2PNode responder;

		public long getEdgeId() {
			return edgeId;
		}

		public P2PNode getResponder() {
			return responder;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof Recognition;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			Glb.debug(() -> edgeId + ":" + responder.getP2pPort());
			Recognition request = (Recognition) req.getContent();
			UpdatableNeighborList unsecure = Glb.getSubje()
					.getUnsecureNeighborList();

			//リクエスターは既にエッジを作成済みのはず
			P2PEdge exist = unsecure.getNeighbor(request.getEdgeId());
			if (exist == null)
				return false;

			//相手が知らせてきた公開鍵が既に仮リストに入っているか
			//普通この段階で相手の公開鍵が分かっていないのでtmpはnull
			//もし見つかるなら、重複して認識プロセスを始めたか、中途半端に終わった認識プロセスが過去にあった
			P2PEdge tmp = unsecure.getNeighbor(responder.getP2PNodeId());
			//tmpがnullじゃなく、しかも今回の認識プロセスで作ったP2PEdgeとIDが異なるならtmpを削除
			if (tmp != null && tmp.getEdgeId() != exist.getEdgeId()) {
				unsecure.removeNeighbor(tmp.getEdgeId());
			}

			//もし相手がIPアドレスを通知してこなかったらこちらで設定する。
			if (responder.getAddr() == null) {
				responder.setAddr(validated.getAddr());
			}

			//入手した情報をセット
			exist.getNode().setPubKey(responder.getPubKey());
			exist.getNode().setNodeNumber(responder.getNodeNumber());
			exist.getNode().update(responder);
			exist.getNode().setAddrInfo(responder);
			exist.getFromOther().setEdgeId(edgeId);
			exist.updateLastCommunication();
			return true;
		}

		public void setEdgeId(long edgeId) {
			this.edgeId = edgeId;
		}

		public void setResponder(P2PNode responder) {
			this.responder = responder;
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			return edgeId != 0 && edgeId != P2PEdge.getSpecialEdgeId()
					&& responder.getP2pPort() > 0
					&& responder.getPubKey() != null
					&& responder.getPubKey().getByteArray().length > 0
					&& responder.getRange() != null
					&& responder.getType() != null;
		}

	}
}
