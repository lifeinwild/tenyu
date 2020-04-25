package bei7473p5254d69jcuat.tenyu.communication.request.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * 近傍から全体運営者の立候補者リストを取得する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetCandidacyList extends P2PEdgeCommonKeyRequest {
	/**
	 * @param neighbors　問い合わせ先候補
	 * @return	取得された立候補者リストまたはnull
	 */
	public static CandidacyList send(UpdatableNeighborList neighbors) {
		Glb.getLogger().info("send GetCandidacyList");
		//limit回まで近傍から取得を試みる
		int limit = 5;
		for (int i = 0; i < limit; i++) {
			//問い合わせ先。同じ問い合わせ先が複数回選ばれる可能性があるが、大きな問題はない
			List<P2PEdge> tmp = neighbors.getNeighborRandom(1, true);
			if (tmp == null || tmp.size() == 0)
				continue;
			P2PEdge to = tmp.get(0);

			//FQDNがあればIPアドレスを最新化する
			to.getNode().updateAddrByFqdn();

			//送信してレスポンスを受け取る
			GetCandidacyList req = new GetCandidacyList();
			Message m = Message.build(req).packaging(req.createPackage(to))
					.finish();
			Message resMessage = Glb.getP2p().requestSync(m, to);
			if (Response.fail(resMessage))
				continue;

			GetCandidacyListResponse c = (GetCandidacyListResponse) resMessage
					.getContent();

			return c.getList();
		}

		//limit回までやっても取得できなかった場合
		return null;
	}

	@Override
	protected boolean validateP2PEdgeCommonKeyConcrete(Message m) {
		return true;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetCandidacyListResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		P2PEdge to = validated.getEdgeByInnermostPackage();
		GetCandidacyListResponse res = new GetCandidacyListResponse();
		res.setList(Glb.getSubje().getCandidacyList());
		Message resM = Message.build(res).packaging(res.createPackage(to))
				.finish();
		return Glb.getP2p().response(resM, ctx);
	}

	public static class GetCandidacyListResponse
			extends P2PEdgeCommonKeyResponse {
		private CandidacyList list;

		@Override
		protected boolean validateP2PEdgeCommonKeyResponseConcrete(Message m) {
			if (list == null)
				return false;
			if (!list.validateAtUpdate(new ValidationResult()))
				return false;
			return true;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetCandidacyList;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			//Glb.getSubje().getCandidacyList().add(list);
			return true;
		}

		public CandidacyList getList() {
			return list;
		}

		public void setList(CandidacyList list) {
			this.list = list;
		}

		@Override
		public String toString() {
			return "GetCandidacyListResponse [list=" + list + "]";
		}

	}
}
