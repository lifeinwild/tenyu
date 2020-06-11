package bei7473p5254d69jcuat.tenyutalk;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import glb.*;
import glb.Glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * node毎に異なるインスタンスが必要になるので
 * 少し特殊な設計がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Tenyutalk implements DBObj, GlbMemberDynamicState {

	/**
	 * このユーザーが個人的にブロックしている{@link User}の情報
	 * この情報がどのように用いられるかはアプレット任せ。
	 * ミラーノードの存在を考えるとブロックしたからといって情報を見られないという事はない。
	 * 一部アプレットでは適宜この情報を使うべき。
	 */
	private UserGroup blockedUsers = new UserGroup();

	/**
	 * このユーザーがDL済みのリポジトリのIDリスト
	 */
	private List<Long> downloadedRepositoryIds = new ArrayList<>();

	/**
	 * 起動中のサービス型（常駐型）アプレット
	 */
	private List<Long> launchedServiceAppletIds = new ArrayList<>();

	/**
	 * 起動中のタスク型（非常駐型）アプレット
	 */
	private List<Long> launchedTaskAppletIds = new ArrayList<>();

	/**
	 * 全てのTenyutalk系メッセージは受信時このメソッドに渡される。
	 * @param mes	Tenyutalk系メッセージ
	 * @return	処理に成功したか
	 */
	public boolean receive(Received mes) {
		return false;//TODO
	}

	public Environment getEnv(NodeIdentifierUser node) {
		return Glb.getDb(Glb.getFile()
				.getTenyutalkDBDir(node.getUser().getName()
						+ Glb.getConst().getFileSeparator()
						+ node.getNodeNumber()));
	}

	public <T> T getComment(Function<CommentStore, T> f) {
		return readTryW(txn -> f.apply(new CommentStore(txn)));
	}

	@Override
	public Environment getEnv() {
		return getEnv(new NodeIdentifierUser(Glb.getMiddle().getMyUserId()));
	}

}
