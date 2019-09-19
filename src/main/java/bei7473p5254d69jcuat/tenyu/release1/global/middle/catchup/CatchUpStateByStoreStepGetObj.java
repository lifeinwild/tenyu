package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetObj.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.AbstractCatchUpState.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;

/**
 * 最も高速な、客観系オブジェクト全般の同調
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateByStoreStepGetObj extends AbstractCatchUpByStoreStep {
	/**
	 * 新規。不足オブジェクトを取得する通信の管理。
	 */
	private AsyncRequestStatesSerialSharding<
			GetObj> objRequests = new AsyncRequestStatesSerialSharding<>();

	private List<IdObjectDBI> tmpObjs = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		HashSet<Long> getIds = getCtx().getStoreNameToGetIds().get(storeName);
		if (getIds != null) {
			for (Long id : getIds) {
				sb.append(id + " ");
			}
		}
		return this.getClass().getSimpleName() + " " + storeName + " "
				+ sb.toString();
	}

	@Override
	protected boolean checkCatchUp() {
		//このクラス固有の担当部分というものは無く、
		//そのストア全体の一致をもって同調成功と判定している。
		//クラス固有の担当部分を持つ同調処理クラスもあるので意味的な違いを留意する必要がある

		HashStoreRecordPositioned majorityTop = getCtx().getMajorityAtStart()
				.getByStore().get(storeName).getTop();
		HashStoreRecordPositioned myTop = getCtx().getMyAtStart().getByStore()
				.get(storeName).getTop();

		if (majorityTop == null && myTop == null)
			return true;
		if (majorityTop == null || myTop == null)
			return false;

		return myTop.equals(majorityTop);
	}

	@Override
	protected void end() {
		if (tmpObjs.size() > 0) {
			Glb.getObje().applySparseObjectList(storeName, tmpObjs);
			tmpObjs.clear();
		}
	}

	@Override
	protected boolean isNoRequest() {
		return objRequests.size() == 0;
	}

	@Override
	protected boolean isReset() {
		return isNoRequest();
	}

	@Override
	protected void procResponse() {
		try {
			objRequests.checkAndFireRetrySerial(get -> {
				//取得されたデータ
				Message resM = get.getReq().getRes();
				GetObjResponse res = (GetObjResponse) resM.getContent();
				for (IdObjectDBI e : res.getObjs()) {
					//一時リストに書き込み
					tmpObjs.add(e);
				}
			});
		} catch (Exception e) {
			objRequests.clear();
			return;
		}

		Glb.debug("applySparseObjectList " + storeName + " tmpObjs="
				+ toStringTmpObjs());
		Glb.getObje().applySparseObjectList(storeName, tmpObjs);
		tmpObjs.clear();
	}

	private String toStringTmpObjs() {
		StringBuilder sb = new StringBuilder();
		for (IdObjectDBI e : tmpObjs) {
			sb.append(e.getRecycleId() + " ");
		}
		return sb.toString();
	}

	@Override
	protected void requestAsync() {
		Glb.debug("requestAsync " + toString());

		if (getCtx().getStoreNameToGetIds() == null) {
			return;
		}

		HashSet<Long> getIds = getCtx().getStoreNameToGetIds().get(storeName);
		if (getIds == null || getIds.size() == 0) {
			return;
		}

		//ソート
		List<Long> sortedIds = new ArrayList<>(getIds);
		Collections.sort(sortedIds);

		//小分け
		List<IDList> lists = IDList.compress(sortedIds, GetObj.max);

		for (IDList list : lists) {
			try {
				GetObj req = new GetObj();
				req.setIdList(list);
				req.setStoreName(storeName);
				objRequests.requestToRandomNeighbor(req);
				Glb.debug("Added to objRequests");
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
	}

	@Override
	protected void resetConcrete() {
		objRequests.clear();
	}

}
