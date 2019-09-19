package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetRecycleIDList.*;
import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.AbstractCatchUpState.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.Integrity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;

/**
 * 削除済みIDの同調
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateByStoreStepRecycleId
		extends AbstractCatchUpByStoreStep {
	/**
	 * リサイクルIDを取得する通信の管理
	 */
	private AsyncRequestStatesFreeSharding<
			GetRecycleIDList> recycleIDListRequests = new AsyncRequestStatesFreeSharding<>();;

	/**
	 * 近傍から取得された全リサイクルIDリスト
	 * 自分が既に持っているものも含まれている
	 *
	 * 初回同調だった場合、このフィールドは空リストのままになる。
	 * しかし多数派のリサイクルID一覧は空ではない場合がある。
	 * つまりこのフィールドは"同調処理で得た"多数派のリサイクルID一覧で、
	 * 多数派のリサイクルID一覧と常には一致しない。
	 */
	private List<IDList> recycleIds = new ArrayList<>();

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

		//リサイクルIDの数は一致しているか
		if (myByStore.getRecycleIdCount() != majorityByStore
				.getRecycleIdCount()) {
			return false;
		}

		//IDListの数は一致しているか
		if (myByStore.getRecycleIdListHash().size() != majorityByStore
				.getRecycleIdListHash().size()) {
			return false;
		}

		//IDListのハッシュ値は一致しているか
		for (int i = 0; i < myByStore.getRecycleIdListHash().size(); i++) {
			if (!Arrays.equals(myByStore.getRecycleIdListHash().get(i),
					majorityByStore.getRecycleIdListHash().get(i)))
				return false;
		}

		return true;
	}

	public List<IDList> getRecycleIds() {
		return recycleIds;
	}

	@Override
	protected boolean isNoRequest() {
		return recycleIDListRequests.size() == 0;
	}

	@Override
	protected boolean isReset() {
		return recycleIDListRequests.size() == 0;
	}

	@Override
	protected void procResponse() {
		recycleIDListRequests.checkAndFireRetry((state) -> {
			try {
				Message resM = state.getReq().getRes();
				if (Response.fail(resM)) {
					return false;
				}
				int index = state.getReq().getIndex();
				GetRecycleIDListResponse res = (GetRecycleIDListResponse) resM
						.getContent();
				if (res.getList() != null) {
					Glb.debug("received response");

					//ハッシュ値照合
					byte[] resHash = Glb.getUtil().hash(res.getList());
					byte[] majorityHash = getCtx().getMajorityAtStart()
							.getByStore().get(storeName).getRecycleIdListHash()
							.get(index);
					if (!Arrays.equals(resHash, majorityHash)) {
						return false;
					}

					//IDListをメンバー変数に保持
					recycleIds.addAll(res.getList());
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
		//リサイクルIDは、大量に存在する場合分割されるので、複数リクエストに対応する必要がある

		//リサイクルIDの件数
		long count = Glb.getObje().readRet(txn -> {
			IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);
			RecycleIdStore rs = s.getRecycleIdStore();
			return rs.count();
		});

		//リサイクルIDリスト
		long loop = (count / RecycleIdStore.unitIDList) + 1;
		for (int i = 0; i < loop; i++) {
			GetRecycleIDList req = new GetRecycleIDList();
			req.setStoreName(storeName);
			req.setIndex(i);
			recycleIDListRequests.requestToRandomNeighbor(req);
		}
	}

	@Override
	protected void resetConcrete() {
		recycleIDListRequests.clear();
	}
}