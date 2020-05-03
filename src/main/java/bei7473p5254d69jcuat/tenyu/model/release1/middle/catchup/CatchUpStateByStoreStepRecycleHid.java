package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetRecycleHidList.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.AbstractCatchUpState.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.Integrity.*;
import glb.*;
import glb.util.*;

/**
 * 削除済みHIDの同調
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateByStoreStepRecycleHid
		extends AbstractCatchUpByStoreStep {
	/**
	 * リサイクルHIDを取得する通信の管理
	 */
	private AsyncRequestStatesFreeSharding<
			GetRecycleHidList> recycleHidListRequests = new AsyncRequestStatesFreeSharding<>();

	/**
	 * 近傍から取得された全リサイクルHidリスト
	 * 自分が既に持っているものも含まれている
	 *
	 * 初回同調だった場合、このフィールドは空リストのままになる。
	 * しかし多数派のリサイクルHid一覧は空ではない場合がある。
	 * つまりこのフィールドは"同調処理で得た"多数派のリサイクルHid一覧で、
	 * 多数派のリサイクルHid一覧と常には一致しない。
	 */
	private List<IDList> recycleHids = new ArrayList<>();

	@Override
	protected boolean checkCatchUp() {
		//自分のストア別整合性情報
		IntegrityByStore myByStore = getCtx().getMyAtStart().getByStore()
				.get(storeName);
		if (myByStore == null) {
			Glb.getLogger().error("myByStore is null");
			return false;
		}

		//多数派のストア別整合性情報
		IntegrityByStore majorityByStore = getCtx().getMajorityAtStart()
				.getByStore().get(storeName);
		if (majorityByStore == null) {
			Glb.getLogger().error("majorityByStore is null");
			return false;
		}

		//リサイクルHidの数は一致しているか
		if (myByStore.getRecycleHidCount() != majorityByStore
				.getRecycleHidCount()) {
			return false;
		}

		//IDListの数は一致しているか
		if (myByStore.getRecycleHidListHash().size() != majorityByStore
				.getRecycleHidListHash().size()) {
			return false;
		}

		//IDListのハッシュ値は一致しているか
		for (int i = 0; i < myByStore.getRecycleHidListHash().size(); i++) {
			if (!myByStore.getRecycleHidListHash().get(i)
					.equals(majorityByStore.getRecycleHidListHash().get(i)))
				return false;
		}

		return true;
	}

	public List<IDList> getRecycleHids() {
		return recycleHids;
	}

	@Override
	protected boolean isNoRequest() {
		return recycleHidListRequests.size() == 0;
	}

	@Override
	protected boolean isReset() {
		return recycleHidListRequests.size() == 0;
	}

	@Override
	protected void procResponse() {
		recycleHidListRequests.checkAndFireRetry(state -> {
			try {
				Message resM = state.getReq().getRes();
				if (Response.fail(resM)) {
					return false;
				}
				int index = state.getReq().getIndex();
				GetRecycleHidListResponse res = (GetRecycleHidListResponse) resM
						.getContent();
				if (res.getList() != null) {
					Glb.debug("received response");

					//ハッシュ値照合
					byte[] resHash = HashStore.hash(res.getList());
					byte[] majorityHash = getCtx().getMajorityAtStart()
							.getByStore().get(storeName).getRecycleHidListHash()
							.get(index).getByteArray();
					if (!Arrays.equals(resHash, majorityHash)) {
						return false;
					}

					//近傍からのリサイクルHID一覧を採用
					recycleHids.addAll(res.getList());
				}
				return true;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	@Override
	protected void end() {
	}

	@Override
	protected void requestAsync() {
		//リサイクルHIDは、大量に存在する場合分割されるので、複数リクエストに対応する必要がある

		//自分のこのストアのリサイクルHIDの件数
		long count = Glb.getObje().readRet(txn -> {
			ModelStore<?, ?> s = storeName.getStore(txn);
			RecycleHidStore rs = s.getRecycleHidStore();
			return rs.count();
		});

		//リサイクルHIDリスト
		long loop = (count / RecycleHidStore.unitIDList) + 1;
		for (int i = 0; i < loop; i++) {
			GetRecycleHidList req = new GetRecycleHidList();
			req.setStoreName(storeName);
			req.setIndex(i);
			recycleHidListRequests.requestToRandomNeighbor(req);
		}
	}

	@Override
	protected void resetConcrete() {
		recycleHidListRequests.clear();
	}
}