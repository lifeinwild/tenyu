package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.Middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import glb.*;

/**
 * ユーザーメッセージ受付サーバ
 * サーバと言っているが実際は常駐型ハンドラ
 * Glbから参照されるグローバル状態の一つ
 *
 * サーバ役は複数いて、客観コアに設定されている。
 * サーバ役のうちオンラインのノードがサブサーバを起動し、
 * そのうちサーバ役リストで最前列のノードがメインサーバを起動する。
 *
 * 全てのサーバ役のノードが落ちた場合、客観の更新が停止する。
 * しかし、既存の客観を改ざんされてしまうことは無い。
 * あるいはサーバ役が腐敗した場合も客観の更新が停止し、客観の改ざんは不可能である。
 * このようなサーバを導入する事でP2Pネットワークの性能は飛躍的に向上する。
 *
 * P2Pネットワークは全体運営者を自由に選べるし、全体運営者はサーバ役を指名できるし、
 * P2Pソフトウェアがサーバプログラムを含みどのノードもサーバとして動作できるので、
 * サーバを用いる形態でもP2Pネットワーク主権である。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserMessageListServer
		extends TakeOverServer<TakeOverMessageUserMessageListServer> {
	/**
	 * 受信中のメッセージリスト
	 */
	private UserMessageList next;

	@Override
	public boolean sendInheritingMessage(NodeIdentifierUser nextServerUserId) {
		return TakeOverMessageUserMessageListServer.send(nextServerUserId,
				next);
	}

	/**
	 * 特別受付。
	 * 通常受付が超過した時、全体運営者のメッセージだけはここに記録され、
	 * 次のメッセージリストの初期値になる。
	 * こうする事で機械的に大量のメッセージが来た場合でも
	 * 全体運営者はメッセージを通す事ができる。
	 * 全体運営者が実施できる機能の中に不正者をまとめてBANする機能があるので、
	 * それで不正行為に対抗できる。
	 */
	private UserMessageList special;

	@Override
	public String getModuleName() {
		return UserMessageListServer.class.getSimpleName();
	}

	@Override
	public List<NodeIdentifierUser> getServerCandidates() {
		return Glb.getObje()
				.getRole(rs -> rs.getByName(getModuleName()).getAdminNodes());
	}

	@Override
	public void registerToOnlineChecker() {
		Glb.getMiddle().getOnlineChecker().register(getModuleName(),
				new OnlineCheckerFuncs(() -> checkAndStartOrStop(),
						() -> getServerCandidates()));
	}

	public synchronized boolean start() {
		boolean r = super.start();
		next = new UserMessageList();
		return r;
	}

	/**
	 * メッセージリストを取得し、内部のメッセージリストを新しいものに変える
	 * 検証と不正なメッセージの除去が行われる
	 * @return 同調状況が混沌の場合、null。それ以外の場合、次に拡散すべきメッセージリスト
	 */
	public synchronized UserMessageList getAndNewMessageList() {
		if (Glb.getMiddle().getObjeCatchUp()
				.getCurrentCircumstance() == ObjectivityCircumstance.CHAOS) {
			return null;
		}

		UserMessageList r = next;
		if (special != null) {
			//特別受付が存在する場合
			next = special;
			special = null;
		} else {
			//標準ケース
			next = new UserMessageList();
		}
		r.setHistoryIndex(Glb.getObje().getCore().getHistoryIndex());

		if (!r.validateAndRemove())
			return null;

		return r;
	}

	/**
	 * メッセージリストにメッセージを登録する
	 * @param m		UserPackageのメッセージ
	 * @return		次のメッセージリストに入ったか。最大件数を超えていた場合、入らない。
	 */
	public synchronized boolean receive(Message validatedUserMessage) {
		Glb.debug(validatedUserMessage.getContent().getClass().getSimpleName());
		if (!started) {
			Glb.getLogger().warn("not started. server=" + toString());
			return false;
		}
		if (!next.add(validatedUserMessage)) {
			//受付に失敗した時
			//全体運営者のメッセージか
			Long userId = validatedUserMessage.getUserId();
			if (userId != null && Glb.getObje().getCore().getManagerList()
					.getManagerPower(userId) > 0) {
				//特別受付
				if (special == null)
					special = new UserMessageList();
				return special.add(validatedUserMessage);
			}
			return false;
		}
		return true;
	}

	@Override
	public void takeover(TakeOverMessageUserMessageListServer message) {
		//getMessages()の返値のUserMessageListは
		//検証されていなくても問題無い。
		if (message.getMessages() == null)
			return;
		next.addAll(message.getMessages());
	}

}
