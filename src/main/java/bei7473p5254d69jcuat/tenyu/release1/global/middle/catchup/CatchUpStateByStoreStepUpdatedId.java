package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetUpdatedIDList.*;
import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.AbstractCatchUpState.*;

/**
 * 更新されたID一覧の同調
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateByStoreStepUpdatedId
		extends AbstractCatchUpByStoreStep {
	/**
	 * 更新されたオブジェクトのID一覧を取得する通信の管理
	 */
	private AsyncRequestStatesFreeSharding<
			GetUpdatedIDList> objRequestsForUpdate = new AsyncRequestStatesFreeSharding<>();
	/**
	 * 通信で取得された更新されたID一覧
	 * updatedIdsの実装クラスはnull値を許容する必要がある
	 *
	 * 初期同調の場合、実際更新されたID一覧があったとしても、これは空になる。
	 */
	private Map<Long, CatchUpUpdatedIDList> updatedIds = new HashMap<>();

	@Override
	protected boolean checkCatchUp() {
		long majorityHistoryIndex = getCtx().getMajorityAtStart()
				.getHistoryIndex();
		long myHistoryIndex = getCtx().getMyAtStart().getHistoryIndex();
		return majorityHistoryIndex == myHistoryIndex;
	}

	public Map<Long, CatchUpUpdatedIDList> getUpdatedIds() {
		return updatedIds;
	}

	@Override
	protected boolean isNoRequest() {
		return objRequestsForUpdate.size() == 0;
	}

	@Override
	protected boolean isReset() {
		return isNoRequest();
	}

	@Override
	protected void end() {
	}

	@Override
	protected void procResponse() {
		objRequestsForUpdate.checkAndFireRetry((state) -> {
			try {
				Message resM = state.getReq().getRes();
				if (Response.fail(resM)) {
					return false;
				}
				Glb.debug("checkAndFire "
						+ resM.getContent().getClass().getSimpleName());

				long historyIndex = state.getReq().getHistoryIndex();
				GetUpdatedIDListResponse res = (GetUpdatedIDListResponse) resM
						.getContent();
				if (res.getList() == null) {
					//nullも書き込む
					updatedIds.put(historyIndex, null);
				} else {
					//余りに巨大な場合、拒否
					if (res.getList().getUpdated().size() > 65) {
						//falseを返すとretryするが正常データでここに来る場合
						//何度繰り返しても同じ結果になるので、無限ループ回避のためtrueを返す
						//とはいえこのあたりどのような仕様も優劣を決定しづらい
						return true;
					}
					//IDListをメンバー変数に保持
					updatedIds.put(historyIndex, res.getList());
				}
				return true;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	@Override
	protected void requestAsync() {
		long majorityHistoryIndex = getCtx().getMajorityAtStart()
				.getHistoryIndex();
		long myHistoryIndex = getCtx().getMyAtStart().getHistoryIndex();

		Glb.getObje().execute(txn -> {
			long startHistoryIndex = myHistoryIndex;

			//余りに自分のヒストリーインデックスが古い場合、古い更新IDの取得を諦める
			if (majorityHistoryIndex
					- myHistoryIndex > CatchUpUpdatedIDListStore.historyIndexLimit)
				startHistoryIndex = majorityHistoryIndex
						- CatchUpUpdatedIDListStore.historyIndexLimit;

			for (long historyIndex = startHistoryIndex
					+ 1; historyIndex <= majorityHistoryIndex; historyIndex++) {
				try {
					//DBにあるか
					IdObjectStore<? extends IdObjectDBI,
							?> s = Glb.getObje().getStore(storeName, txn);
					CatchUpUpdatedIDList l = s.getCatchUpUpdatedIDListStore()
							.get((Long) historyIndex);
					if (l != null) {
						continue;
					}

					//無ければ近傍から取得
					GetUpdatedIDList req = new GetUpdatedIDList();
					req.setHistoryIndex(historyIndex);
					req.setStoreName(storeName);
					objRequestsForUpdate.requestToRandomNeighbor(req);

					Glb.debug("request GetUpdatedIDList");

				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			}
		});

	}

	@Override
	protected void resetConcrete() {
		objRequestsForUpdate.clear();
	}

}
