package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetObj.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.HashStore.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.AbstractCatchUpState.*;
import glb.*;
import glb.util.*;

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

	private List<ModelI> tmpObjs = new ArrayList<>();

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
				for (ModelI e : res.getObjs()) {
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
		for (ModelI e : tmpObjs) {
			sb.append(e.getId() + " ");
		}
		return sb.toString();
	}

	@Override
	protected void requestAsync() {
		Glb.debug("requestAsync " + toString());

		if (getCtx().getStoreNameToGetIds() != null) {
			HashSet<Long> getIds = getCtx().getStoreNameToGetIds()
					.get(storeName);
			if (getIds != null && getIds.size() > 0) {
				requestAsync(true, getIds);
			}
		}
		if (getCtx().getStoreNameToGetHids() != null) {
			HashSet<Long> getHids = getCtx().getStoreNameToGetHids()
					.get(storeName);
			if (getHids != null && getHids.size() > 0) {
				requestAsync(false, getHids);
			}
		}

	}

	/**
	 * @param id	hidならfalse
	 * @param ids	idまたはhid一覧
	 */
	private void requestAsync(boolean id, HashSet<Long> ids) {
		//ソート
		List<Long> sortedIds = new ArrayList<>(ids);
		Collections.sort(sortedIds);

		//小分け
		List<IDList> lists = IDList.compress(sortedIds, GetObj.max);

		for (IDList list : lists) {
			try {
				GetObj req = new GetObj();
				if (id) {
					req.setIdList(list);
				} else {
					req.setHidList(list);
				}
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
