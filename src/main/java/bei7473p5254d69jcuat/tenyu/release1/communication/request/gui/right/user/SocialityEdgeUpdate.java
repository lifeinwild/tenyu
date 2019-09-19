package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 社会性のエッジの作成または更新
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class SocialityEdgeUpdate extends UserRightRequest {
	/**
	 * 新規作成されるエッジ、または既存のエッジの更新内容
	 */
	private Edge e;
	/**
	 * このユーザーからのエッジが更新される
	 */
	private Long fromUserId;

	@Override
	protected boolean validateUserRightConcrete(Message m) {
		Long signer = SignedPackage.getSigner(m);
		if (signer == null)
			return false;

		ValidationResult vr = new ValidationResult();
		if (e == null || !e.validateAtCommon(vr)) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return false;
		}

		//メッセージクラスを通したエッジ作成ではゲーム系ノードにエッジを作成できない。
		//エンジニアリングメモに詳細な考察がある。
		//ゲームは他のゲームに創作的な影響を与えた等の意味でエッジを獲得する場合
		//そのWEBノードを通じて獲得すべきである。
		//Tenyuに登録されたゲームがTenyuからゲームを削除した場合でも
		//相互評価フローネットワークの利用は可能である。
		//一方、ゲーム系ノードを通じて獲得するエッジはTenyuに組み込まれている事によって得られるもので、
		//Tenyuにゲームが登録されている間のみ機能する。
		//ゲーム系ノードが獲得するエッジはシステムを通じて自動的に作成される。
		Sociality to = SocialityStore.getSimple(e.getDestSocialityId());
		if (to.getType() == NodeType.RATINGGAME
				|| to.getType() == NodeType.STATICGAME) {
			return false;
		}

		return fromUserId != null && fromUserId.equals(signer);
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		SocialityStore store = new SocialityStore(txn);
		Sociality s = store.getByNaturality(NodeType.USER, fromUserId);
		if (s == null)
			return false;
		//キーが存在しなければ新規に作成されるのでDB処理がcreateになる
		boolean create = s.getEdgeManager().getEdges()
				.containsKey(e.getDestSocialityId());
		if (!s.getEdgeManager().add(e, historyIndex))
			return false;
		if (create) {
			if (store.create(s) == null)
				throw new Exception("Failed to create");
		} else {
			if (!store.update(s))
				throw new Exception("Failed to update");
		}

		return true;
	}

	@Override
	public String getName() {
		return Lang.USER_EDGE_UPDATE.toString();
	}

}
