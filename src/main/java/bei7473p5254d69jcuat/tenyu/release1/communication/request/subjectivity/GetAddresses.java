package bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import io.netty.channel.*;

/**
 * 近傍の近傍を取得する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class GetAddresses extends P2PEdgeCommonKeyRequest {
	private static final int maxConditionCount = 100;

	/**
	 * 近傍の近傍を取得
	 * @param conditions 取得するノードの条件。null可
	 */
	public static boolean send(List<P2PNode> conditions,
			UpdatableNeighborList neighbors) {
		GetAddresses req = new GetAddresses();
		if (conditions != null && conditions.size() > 0)
			req.setConditions(conditions);

		//問い合わせ先、つまり紹介者
		P2PEdge introducer = null;
		if (Glb.getRnd().nextInt(100) < 20) {
			//20%の確率でランダムに問い合わせ先を決定する事で
			//近傍リストがある程度多様化する
			List<P2PEdge> l = neighbors.getNeighborRandom(1, true);
			if (l == null || l.size() == 0) {
				//もしランダム取得に失敗したら低レイテンシノードを取得
				introducer = neighbors.getNeighborLowLatency();
			} else {
				//ランダム取得したノードを使用
				introducer = l.get(0);
			}
		} else {
			//８０％の確率で低レイテンシノードに問い合わせることで
			//近傍リストが概ね近所のノードになり基幹回線への負荷が低下する
			introducer = neighbors.getNeighborLowLatency();
		}
		//問い合わせ先ノードを決定できていなかったら何もせず終わる
		if (introducer == null)
			return false;

		//FQDNがあればIPアドレスを最新化する
		introducer.getNode().updateAddrByFqdn();
		//送信してレスポンスを受け取る
		Message m = Message.build(req).packaging(req.createPackage(introducer))
				.finish();
		Message resMessage = Glb.getP2p().requestSync(m, introducer);
		if (Response.fail(resMessage))
			return false;
		neighbors.batchRecognition(
				(GetAddressesResponse) resMessage.getContent(), introducer);
		return true;
	}

	public static int getMaxSize() {
		return 1000 * 100;//P2PEdge.getMaxSize() * maxConditionCount;
	}

	private List<P2PNode> conditions;

	public List<P2PNode> getConditions() {
		return conditions;
	}

	@Override
	public int getResponseTotalSize() {
		return 1000 * 100;//super.getMaxSize()
		//				+ P2PEdge.getMaxSize() * GetAddressesResponse.nodeCount;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetAddressesResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext con, Received validated) {
		P2PEdge n = validated.getEdgeByInnermostPackage();
		//プロセッサ証明を少しでもしている事を必要とする
		//プロセッサ証明は８時間に１回しかないので、これによって
		//ネットワークを走査するのに必要な時間が大幅に伸びる
		//P2Pネットワークへの攻撃は、ノードのIPアドレスの調査が大抵の場合
		//必要である
		if (n.getImpression() <= 0)
			return false;

		GetAddressesResponse res = new GetAddressesResponse();

		List<P2PNode> nodes = Glb.getSubje().getNeighborList()
				.getNeighborRandomForCommunication(
						GetAddressesResponse.getNodecount(), conditions);
		//リクエスト者をリクエスト者に返さない
		for (int i = 0; i < nodes.size(); i++)
			if (nodes.get(i).getP2PNodeId().equals(n.getNode().getP2PNodeId()))
				nodes.remove(i);

		if (nodes.size() == 0)
			return false;

		res.setNodes(nodes);
		Message m = Message.build(res).packaging(res.createPackage(n)).finish();
		Glb.getP2p().response(m, con);
		return true;
	}

	public void setConditions(List<P2PNode> conditions) {
		int over = conditions.size() - maxConditionCount;
		if (over > 0) {
			for (int i = conditions.size() - 1; i < over; i--) {
				conditions.remove(i);
			}
		}
		this.conditions = conditions;
	}

	@Override
	protected final boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return true;
	}

	public static class GetAddressesResponse extends P2PEdgeCommonKeyResponse {
		/**
		 * １回のGetAddressesで返すノードの最大件数
		 */
		private static final int nodeCount = Glb.getSubje().getNeighborList()
				.getNeighborThreshold() >= 10
						? Glb.getSubje().getNeighborList()
								.getNeighborThreshold() / 10
						: 1;

		public static int getNodecount() {
			return nodeCount;
		}

		@Override
		protected final boolean validateP2PEdgeCommonKeyResponseConcrete(
				Message m) {
			return nodes != null && nodes.size() > 0
					&& nodes.size() < nodeCount;
		}

		/**
		 * リクエストされた条件に一致するノードのリスト。
		 */
		private List<P2PNode> nodes;

		public List<P2PNode> getNodes() {
			return nodes;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetAddresses;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			//この処理は長いのですぐにリターンし、リクエストした箇所で処理する
			return true;
		}

		public void setNodes(List<P2PNode> nodes) {
			this.nodes = nodes;
		}
	}

}
