package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;

/**
 * 他のモジュールがOnlineCheckerを利用する場合、
 * OnlineCheckerFuncsオブジェクトを登録する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class OnlineCheckerFuncs {
	/**
	 * 自分がサーバーをやるべき状況になった場合呼ばれる
	 */
	private Runnable funcWhenOnlineStatesChanged;
	/**
	 * オンライン状態をチェックされるユーザーID一覧を返す処理
	 * つまりその一覧は固定ではなく随時変化しうる
	 */
	private Supplier<List<NodeIdentifierUser>> getCheckUserIds;

	/**
	 * @param funcWhenOnlineStatesChanged	オンライン状態が変化した時に呼び出される処理
	 * @param getCheckUserNodes	オンライン状態を監視するノード一覧
	 */
	public OnlineCheckerFuncs(Runnable funcWhenOnlineStatesChanged,
			Supplier<List<NodeIdentifierUser>> getCheckUserNodes) {
		this.funcWhenOnlineStatesChanged = funcWhenOnlineStatesChanged;
		this.getCheckUserIds = getCheckUserNodes;
	}

	@SuppressWarnings("unused")
	private OnlineCheckerFuncs() {
	}

	public Runnable getFuncWhenOnlineStatesChanged() {
		return funcWhenOnlineStatesChanged;
	}

	public Supplier<List<NodeIdentifierUser>> getGetCheckUserIds() {
		return getCheckUserIds;
	}

	public void setGetCheckUserIds(
			Supplier<List<NodeIdentifierUser>> getCheckUserIds) {
		this.getCheckUserIds = getCheckUserIds;
	}

	public boolean validate() {
		if (getCheckUserIds == null)
			return false;
		if (funcWhenOnlineStatesChanged == null)
			return false;
		return true;
	}

}