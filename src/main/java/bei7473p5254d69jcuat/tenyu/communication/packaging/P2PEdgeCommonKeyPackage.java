package bei7473p5254d69jcuat.tenyu.communication.packaging;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;

public class P2PEdgeCommonKeyPackage extends CommonKeyPackage {
	protected transient P2PEdge edgeCache = null;

	/**
	* 受信側→送信側のP2PエッジID
	*/
	protected long edgeIdReceiverToSender;

	/**
	 * 送信側→受信側のP2PエッジID
	 */
	protected transient long edgeIdSenderToReceiver;

	/**
	 * Kryo用
	 */
	public P2PEdgeCommonKeyPackage() {
	}

	/**
	 * 基本的にこれを使う
	 */
	public P2PEdgeCommonKeyPackage(long edgeId, long edgeIdFromOther) {
		edgeIdSenderToReceiver = edgeId;
		edgeIdReceiverToSender = edgeIdFromOther;
	}

	/**
	 * 共通鍵交換中に共通鍵梱包が使用される事を防止する。
	 *
	 * @param m
	 * @param n
	 */
	private void commonKeyExchangeSynchronization(Message m, P2PEdge n) {
		MessageContent c = m.getContent();
		//共通鍵交換で使用されるメッセージクラスか
		//共通鍵交換自体で待機が発生すると処理が進まない
		boolean commonKeyExchangeMessage = c instanceof CommonKeyExchange
				|| c instanceof CommonKeyExchangeConfirmation
				|| c instanceof Recognition;
		//受信したメッセージか
		//送信側が最新の共通鍵で送信するなら受信時は待機する必要無し
		boolean receive = c == null || !m.isMyMessage();
		if (!commonKeyExchangeMessage && !receive) {
			//共通鍵情報を交換中だったらしばらく待機する
			int exchangeWait = 0;
			while (n.getCommonKeyExchangeState().isDuringExchange()) {
				try {
					Thread.sleep(CommonKeyExchangeState.exchangeWaitMax / 20);
				} catch (InterruptedException e) {
					Glb.getLogger().error("", e);
				}
				exchangeWait++;
				if (exchangeWait > 20) {
					Glb.getLogger().error("共通鍵の交換の終了の待機処理が規定時間を超過",
							new Exception());
					break;
				}
			}
		}
	}

	@Override
	protected CommonKeyInfo getCki(Message m) {
		//かなりややこしいので解説
		//getEdge(m)はP2Pネットワークを想定した時のエッジを特定する
		//エッジは有向で、基本的に双方向なので、2ノード間で2本ある
		//それぞれのエッジのIDがメンバー変数に記録される。片方はtransientで相手側に通知されない
		//myMessageフラグによって２つあるエッジIDのどちらを使うかを決定する。
		//こうすることでメッセージ作成者の手元でも受信側でもエッジが特定される

		//さらにエッジはfromOtherというメンバーに相手側からのエッジの情報を持っている。つまり
		//A→BのエッジはそのfromOtherにB→Aとほとんど同値の情報を持っている。逆も然り
		//しかし"AはB→Aそのものを持っていないし、BはA→Bそのものを持っていない"。
		//互いにfromOtherを通して相手からのエッジの情報について知る
		//さらに、2ノード間で暗号通信する場合、共通鍵はA→BとそのfromOther、B→AとそのfromOtherに記録されている。
		//AとBはそれぞれ標準情報かfromOtherかで2種類の共通鍵のどちらを見るかをmyMessageフラグに応じて決定する。
		//BからAに送信するならB→Aの情報かA→BのfromOtherに記録されている。
		//getEdge(m)内部でmyMessageフラグを見て取得方法を変えているが、それは
		//自分が作成したメッセージならA→Bの標準情報を見て、
		//相手が作成したメッセージならB→AのfromOtherを見るからである。
		//そしてここのコードは特定されたエッジの標準情報とfromOtherのどちらを使うかを決定している
		//myMessageフラグを見たこの分岐処理が無ければ、メッセージを作成した直後自ら開梱する事と
		//相手側で開梱する事を両立させれない。
		P2PEdge n = getEdge(m);
		if (n == null) {
			Glb.debug("no P2PEdge");
			return null;
		}

		commonKeyExchangeSynchronization(m, n);

		CommonKeyInfo cki = null;
		if (m.isMyMessage()) {
			cki = n.getCommonKeyExchangeState().getCommonKeyInfo();
		} else {
			cki = n.getFromOther().getCommonKeyExchangeState()
					.getCommonKeyInfo();
		}

		Glb.debug("CommonKeyPackage check:"
				+ Arrays.toString(cki.getCommonKey()));
		return cki;
	}

	public P2PEdge getEdge(Message m) {
		P2PEdge n = null;
		if (edgeCache != null) {
			n = edgeCache;
		} else if (m.isMyMessage()) {
			n = getNeighborList().getNeighbor(edgeIdSenderToReceiver);
		} else {
			//少しややこしい
			//edgeIdReceiverToSenderは送信側の手元でのfromOtherのエッジIDである
			//それは受信側の標準の、非fromOtherのエッジIDである
			//だからこれで取得できる
			n = getNeighborList().getNeighbor(edgeIdReceiverToSender);
		}

		//近傍は定期的に削除処理が入るので、相手側で知られていても
		//こちらはもう知らないということは普通にある。その場合、P2PEdgeは発見されない。
		if (n == null && Glb.getConf().isDevOrTest()) {
			List<Long> ids = new ArrayList<>();
			for (P2PEdge e : Glb.getSubje().getNeighborList()
					.getNeighborsUnsafe()) {
				ids.add(e.getEdgeId());
			}
			List<Long> ids2 = new ArrayList<>();
			UpdatableNeighborList unsecureList = Glb.getSubje()
					.getUnsecureNeighborList();
			Collection<P2PEdge> unsecureEdges;
			synchronized (unsecureList) {
				unsecureEdges = unsecureList.getNeighborsUnsafe();
			}

			for (P2PEdge e : unsecureEdges) {
				ids2.add(e.getEdgeId());
			}
			StringBuilder sb = new StringBuilder();
			sb.append("edge id : ");
			if (m.isMyMessage()) {
				sb.append("myMessage=true creatorEdgeId="
						+ edgeIdSenderToReceiver);
			} else {
				sb.append("myMessage=false receiverEdgeId="
						+ edgeIdReceiverToSender);
			}
			sb.append(" : mainList=" + ids + " unsecureList=" + ids2
					+ "neighbors=" + Glb.getSubje().getNeighborList()
					+ "unsecureNeighbors="
					+ Glb.getSubje().getUnsecureNeighborList());
			Glb.debug(sb.toString(), new Exception(
					"P2PEdgeが見つからない。定期削除等で削除された場合これは正常。そうでなければ例外。"));
		}

		edgeCache = n;
		return n;
	}

	/**
	 * 子クラスでオーバーライドされる場合がある
	 * @return	近傍リスト
	 */
	protected UpdatableNeighborList getNeighborList() {
		return Glb.getSubje().getNeighborList();
	}

	@Override
	protected boolean isValidType(Object content) {
		return content instanceof P2PEdgeCommonKeyPackageContent;
	}

	public static interface P2PEdgeCommonKeyPackageContent {
		default Package createPackage(P2PEdge e) {
			return new P2PEdgeCommonKeyPackage(e.getEdgeId(),
					e.getFromOther().getEdgeId());
		}
	}
}
