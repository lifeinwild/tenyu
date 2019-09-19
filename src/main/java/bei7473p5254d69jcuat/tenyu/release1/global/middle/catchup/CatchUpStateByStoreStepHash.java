package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetHashArray.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.AbstractCatchUpState.*;

public class CatchUpStateByStoreStepHash extends AbstractCatchUpByStoreStep {
	private HashSet<Long> getIds = new HashSet<>();

	/**
	 * 微調整。ハッシュ配列を取得する通信の管理
	 */
	private AsyncRequestStatesFreeSharding<GetHashArray> hashArrayRequests = new AsyncRequestStatesFreeSharding<>();

	/**
	 * 自分の最上位配列は随時変化していくので、
	 * ある時の最上位配列を記憶しておいてそれで同調処理を完遂する
	 * ということを繰り返して全体の同調を完成させる。
	 */
	private HashStoreRecordPositioned myTop = null;

	/**
	 * 基本的にこれは要素0だが、これが存在する場合は、
	 * 多数派近傍が間違ったハッシュ配列を通知した場合等が考えられる
	 */
	private HashSet<Long> shouldRemove = new HashSet<>();

	@Override
	protected boolean checkCatchUp() {
		//大幅に自分の整合性情報が変化している可能性があるので新しくトップ配列を設定
		if (myTop == null)
			myTop = HashStore.getTopHashArraySimple(storeName);

		HashStoreRecordPositioned majoTop = getCtx().getMajorityAtStart()
				.getByStore().get(storeName).getTop();

		if (majoTop == null && myTop == null)
			return true;
		if (majoTop == null || myTop == null)
			return false;

		return myTop.equals(majoTop);
	}

	/**
	 * レベルに応じてハッシュ配列またはオブジェクトを取得する
	 * @param majorityHashArray
	 * @param myHashArray
	 * @return	リトライが不要か
	 */
	private boolean difAndRequest(HashStoreRecord majorityHashArray,
			HashStoreRecord myHashArray) {
		if (majorityHashArray.getKey().getLevel() == HashStore
				.getSecondLevel()) {
			return difAndRequestObj(majorityHashArray, myHashArray);
		} else {
			return difAndRequestHashArray(majorityHashArray, myHashArray);
		}
	}

	/**
	 * ハッシュ配列取得
	 * ハッシュツリーのレベル１以上
	 * 自分のハッシュ配列と異なっている部分について問い合わせを開始
	 * @param majorityHashArray	多数派のハッシュ配列
	 * @param myHashArray		自分のハッシュ配列
	 * @param neighborList		この近傍一覧から問い合わせ先が加重乱択で選択される
	 * @return					このキーについてretryが必要無いならtrue
	 */
	private boolean difAndRequestHashArray(HashStoreRecord majorityHashArray,
			HashStoreRecord myHashArray) {
		//最大同時リクエスト件数を超えていたら差異が何件あっても1件しかリクエストしない
		//全くリクエストしないのではなく1件だけリクエストするのは、ある種のレジリエンスのため
		boolean onlyOne = hashArrayRequests.size() > 1000 * 100;

		//ハッシュ配列の差異に応じて次のリクエストを作成
		for (HashStoreKey key : majorityHashArray.difReturnKey(myHashArray)) {
			requestAsyncCommon(key);
			if (onlyOne)
				break;
		}
		return true;
	}

	/**
	 * オブジェクト取得
	 * ハッシュツリーのレベル０
	 * @param majorityHashArray
	 * @param myHashArray
	 * @param neighborList
	 * @return	このキーについてretryが必要無いならtrue
	 */
	private boolean difAndRequestObj(HashStoreRecord majorityHashArray,
			HashStoreRecord myHashArray) {
		//実データの1つ上のレベルが前提
		if (majorityHashArray.getKey().getLevel() != HashStore.getSecondLevel()
				|| myHashArray.getKey().getLevel() != HashStore
						.getSecondLevel())
			throw new IllegalArgumentException();

		//多数派の方で削除扱いで自分の方で削除されていないものを削除対象とする
		List<Long> majorityExtra = Glb.getUtil().getExtra(
				majorityHashArray.getRemovedList(),
				new HashSet<>(myHashArray.getRemovedList()));
		shouldRemove.addAll(majorityExtra);

		//差異があるオブジェクトのIDを取得
		//自分の方で削除扱いで多数派の方で削除されていないものはここで取得対象になる
		List<Long> ids = majorityHashArray.dif(myHashArray);
		//削除対象に入っているものを除外
		ids = Glb.getUtil().getExtra(ids, shouldRemove);
		getIds.addAll(ids);

		return true;
	}

	@Override
	protected void end() {
	}

	public HashSet<Long> getGetIds() {
		return getIds;
	}

	public HashSet<Long> getShouldRemove() {
		return shouldRemove;
	}

	public String getStoreName() {
		return storeName;
	}

	@Override
	protected boolean isNoRequest() {
		return hashArrayRequests.size() == 0;
	}

	@Override
	protected boolean isReset() {
		return isNoRequest();
	}

	@Override
	protected void procResponse() {
		//レスポンス処理の中で再度リクエストが登録される特殊なタイプ
		hashArrayRequests.checkAndFireRetry((state) -> {
			Message resM = state.getReq().getRes();
			GetHashArrayResponse res = (GetHashArrayResponse) resM.getContent();
			HashStoreRecord majorityHashArray = res.getHashArray();
			HashStoreRecord myHashArray = HashStore
					.getHashArraySimple(state.getReq().getKey(), storeName);

			if (majorityHashArray == null || myHashArray == null
					|| !majorityHashArray.getKey()
							.equals(myHashArray.getKey())) {
				return false;
			}

			Glb.debug("response received");
			return difAndRequest(majorityHashArray, myHashArray);
		});
	}

	@Override
	protected void requestAsync() {
		difAndRequest(
				getCtx().getMajorityAtStart().getByStore().get(storeName).getTop(),
				myTop);
	}

	/**
	 * teachersから信用加重でランダムに近傍を選択して問い合わせる
	 * @param key	取得するHashStoreRecordのキー
	 */
	private void requestAsyncCommon(HashStoreKey key) {
		if (key.getLevel() < HashStore.getFirstLevel())
			return;

		GetHashArray req = new GetHashArray();
		req.setStoreName(storeName);
		req.setKey(key.getKeyBA());
		hashArrayRequests.requestToRandomNeighbor(req);
	}

	@Override
	protected void resetConcrete() {
		hashArrayRequests.clear();
		getIds.clear();
		shouldRemove.clear();
		myTop = null;
	}
}
