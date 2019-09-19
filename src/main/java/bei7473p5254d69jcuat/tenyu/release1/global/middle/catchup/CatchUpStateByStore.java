package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;

/**
 * 各ストア毎の同調処理に関する状態が記録される。
 * ただし整合性情報はIntegrityInfoの中に記録される。
 *
 * トランザクションの管理について。
 * 1トランザクションで大量のデータを書き込む方が効率が良い。
 * 一方で、微調整処理はそれに期待される少しでも前進していくという性質から、
 * トランザクションを分けるのは妥当かもしれない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateByStore extends AbstractCatchUpState {
	/**
	 * ハッシュ照合の通信の管理
	 */
	private CatchUpStateByStoreStepHash hashProc = new CatchUpStateByStoreStepHash();
	/**
	 * ハッシュ照合を通じて特定した値が異なるオブジェクトを取得する通信の管理
	 */
	private CatchUpStateByStoreStepGetObj obj2Proc = new CatchUpStateByStoreStepGetObj();
	/**
	 * リサイクルIDや更新されたIDから作成されたID一覧のオブジェクトを取得する通信の管理
	 */
	private CatchUpStateByStoreStepGetObj objProc = new CatchUpStateByStoreStepGetObj();

	/**
	 * リサイクルIDを取得する通信の管理
	 */
	private CatchUpStateByStoreStepRecycleId recycleIdProc = new CatchUpStateByStoreStepRecycleId();

	/**
	 * 処理段階
	 */
	private int step = 0;

	/**
	 * 対象とするストア
	 */
	private String storeName;

	/**
	 * 更新されたIDを取得する通信の管理
	 */
	private CatchUpStateByStoreStepUpdatedId updatedIdProc = new CatchUpStateByStoreStepUpdatedId();

	/**
	 * 同調処理
	 * 繰り返し呼ぶことで少しずつ進行していく
	 * @return 完走したか
	 */
	public boolean catchUp() {
		try {
			switch (step) {
			case 0:
				if (!recycleIdProc.isFinish()) {
					Glb.debug("recycleIdProc.catchUp(ctx);");

					recycleIdProc.catchUp();
				}

				if (!updatedIdProc.isFinish()) {
					Glb.debug("updatedIdProc.catchUp(ctx);");
					updatedIdProc.catchUp();
				}

				if (recycleIdProc.isFinish() && updatedIdProc.isFinish()) {
					Glb.debug("step0 finish");
					step0FinishProc();
					step = 1;
				}

				break;
			case 1:
				//オブジェクト取得
				if (objProc.isFinish()) {
					Glb.debug("step1 finish");
					step = 2;

					step1FinishProc();
				} else {
					objProc.catchUp();
				}
				break;
			case 2:
				//ハッシュ照合
				if (hashProc.isFinish()) {
					Glb.debug("step2 finish");
					step = 3;
					step2FinishProc();
				} else {
					Glb.debug("hashProc.catchUp(ctx);");
					hashProc.catchUp();
				}
				break;
			case 3:
				//オブジェクト取得2
				if (obj2Proc.isFinish()) {
					Glb.debug("step3 finish");
					step = 0;
					finish = true;
					return true;
				} else {
					Glb.debug("obj2Proc.catchUp(ctx);");
					obj2Proc.catchUp();
				}
				break;
			default:
				throw new Exception();
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			reset();
		}
		return false;
	}

	public CatchUpStateByStoreStepGetObj getObj2Proc() {
		return obj2Proc;
	}

	public CatchUpStateByStoreStepGetObj getObjProc() {
		return objProc;
	}

	public CatchUpStateByStoreStepRecycleId getRecycleIdProc() {
		return recycleIdProc;
	}

	public void resetConcrete() {
		recycleIdProc.reset();
		recycleIdProc.setStoreName(storeName);

		objProc.reset();
		objProc.setStoreName(storeName);

		updatedIdProc.reset();
		updatedIdProc.setStoreName(storeName);

		hashProc.reset();
		hashProc.setStoreName(storeName);

		obj2Proc.reset();
		obj2Proc.setStoreName(storeName);
	}

	public void setStep(int step) {
		this.step = step;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	protected HashSet<Long> getMyRecycleIds() {
		return Glb.getObje().readRet(txn -> {
			try {
				return new HashSet<>(
						new RecycleIdStore(storeName, txn).getAllIds());
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	protected CatchUpUpdatedIDList getMyUpdatedIds(long historyIndex) {
		return Glb.getObje().readRet(txn -> {
			try {
				return new CatchUpUpdatedIDListStore(storeName, txn)
						.get(historyIndex);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	/**
	 * ネストが深いので分けた。step0の通信処理で得た情報を使って
	 * DBを修正したり取得すべきオブジェクトのIDを決定したりする。
	 * @param ctx
	 */
	private void step0FinishProc() {
		//自分側の全リサイクルID一覧
		HashSet<Long> myRecycleIds = getMyRecycleIds();
		Glb.debug("storeName=" + storeName + " myRecycleIds=" + myRecycleIds);

		//多数派の全リサイクルID一覧
		HashSet<Long> majorityRecycleIds;
		if (recycleIdProc.isInitiallyCatchUp()) {
			majorityRecycleIds = myRecycleIds;
		} else {
			//近傍から得た削除済みID一覧
			List<IDList> recycleIdSrc = recycleIdProc.getRecycleIds();

			//削除済みID一覧
			majorityRecycleIds = new HashSet<>();
			for (IDList l : recycleIdSrc) {
				l.uncompressToHashSet(majorityRecycleIds);
			}
		}
		Glb.debug("initiallyCatchUp=" + recycleIdProc.isInitiallyCatchUp()
				+ "storeName=" + storeName + " majorityRecycleIds="
				+ majorityRecycleIds);

		//更新されたID一覧
		//初期同調だった場合、このリストは空で、実際の更新されたID一覧と一致しない。
		//しかしこの処理においては問題が生じない。
		Map<Long, CatchUpUpdatedIDList> updatedIdsMajority = updatedIdProc
				.getUpdatedIds();

		//取得すべきID一覧
		HashSet<Long> getIds = new HashSet<>();

		int updatedIdsCount = 0;
		//ヒストリーインデックス毎の更新ID一覧から自分の更新ID一覧と削除済みID一覧を引いて取得ID一覧に追加
		for (Entry<Long, CatchUpUpdatedIDList> e : updatedIdsMajority
				.entrySet()) {
			if (e.getValue() == null)
				continue;

			long historyIndex = e.getKey();
			//自分の更新されたID一覧
			HashSet<Long> myUpdatedIds = null;
			CatchUpUpdatedIDList myUpdatedIdList = getMyUpdatedIds(
					historyIndex);
			if (myUpdatedIdList != null) {
				myUpdatedIds = myUpdatedIdList.getIds();
			}

			//多数派の更新されたID一覧
			HashSet<Long> majorityUpdatedIds = e.getValue().getIds();
			if (majorityRecycleIds == null)
				continue;

			for (long id : majorityUpdatedIds) {
				//自分の更新済みID一覧に含まれていたら取得しない。
				//整合性がずれている場合を考えると、
				//自分が取得したオブジェクトが間違っていた可能性があるが、
				//それはHツリーで解決される。
				//大量に更新された時にどこかが僅かに異なっていただけで
				//全ての更新されたIDを再取得するのはやりすぎなので差分だけ取得する
				if (myUpdatedIds != null && myUpdatedIds.contains(id)) {
					continue;
				}

				//削除済みIDなら取得しない
				if (!majorityRecycleIds.contains(id)) {
					getIds.add(id);
					updatedIdsCount++;
				}

				//更新されたIDの上限数
				if (updatedIdsCount > 1000 * 100) {
					break;
				}
			}
		}

		//最後のIDを一致させるため不足IDを登録
		Long majorityLast = getCtx().getMajorityAtStart().getByStore()
				.get(storeName).getLastIdOfHashStore();
		if (majorityLast == null)
			majorityLast = IdObjectDBI.getFirstRecycleId() - 1;
		Long myLast = getCtx().getMyAtStart().getByStore().get(storeName)
				.getLastIdOfHashStore();
		if (myLast == null)
			myLast = IdObjectDBI.getFirstRecycleId() - 1;

		Glb.debug("majorityLast=" + majorityLast + " myLast=" + myLast);

		for (long i = myLast + 1; i <= majorityLast; i++) {
			//削除済みIDなら取得しない
			if (!majorityRecycleIds.contains(i))
				getIds.add(i);
		}

		//同調文脈に設定
		getCtx().getStoreNameToGetIds().put(storeName, getIds);

		final long myLastTmp = myLast;
		final long majorityLastTmp = majorityLast;

		//DB処理
		Glb.getObje().execute(txn -> {
			try {
				IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);

				//最後のIDについて自分の方が多数派より進んでいるか
				if (myLastTmp > majorityLastTmp) {
					//リサイクルIDが生じないように過剰分を削除
					for (long id = myLastTmp; id > majorityLastTmp; id--) {
						s.deleteWithoutRecycle(id);
					}
					s.getHashStore().cut(majorityLastTmp);
				}

				if (recycleIdProc.isInitiallyCatchUp()) {
					//最初から一致していた場合、何もしない
					Glb.getLogger().info(
							"initiallyCatchUp=true storeName=" + storeName);
				} else {
					//多数派と自分のリサイクルID一覧に差異があった場合
					//取得IDを追加したりDBから削除したり

					//多数派リサイクルID一覧に対する自分側の過剰分
					List<Long> myExtraRecycleIds = Glb.getUtil()
							.getExtra(myRecycleIds, majorityRecycleIds);

					//自分のリサイクルID一覧に対する多数派の過剰分
					List<Long> majorityExtraRecycleIds = Glb.getUtil()
							.getExtra(majorityRecycleIds, myRecycleIds);

					//自分側過剰分を取得IDとして追加
					Glb.debug("storeName=" + storeName + " myExtraRecycleIds="
							+ myExtraRecycleIds);
					for (Long id : myExtraRecycleIds) {
						getIds.add(id);
					}

					//多数派側過剰分についてDBから削除
					Glb.debug("storeName=" + storeName
							+ " majorityExtraRecycleIds="
							+ majorityExtraRecycleIds);
					for (Long id : majorityExtraRecycleIds) {
						//未削除のを削除
						if (s.get(id) != null) {
							s.delete(id);
						}
					}
				}

				Glb.debug("getIds=" + getIds);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});

	}

	private void step1FinishProc() {
		//更新されたIDをDBに書き込む
		Glb.getObje().execute(txn -> {
			IdObjectStore<? extends IdObjectDBI,
					?> s = Glb.getObje().getStore(storeName, txn);
			CatchUpUpdatedIDListStore ups = s.getCatchUpUpdatedIDListStore();
			for (Entry<Long, CatchUpUpdatedIDList> e : updatedIdProc
					.getUpdatedIds().entrySet()) {
				try {
					//既にDBにあるか
					boolean existInMyDb = ups.get(e.getKey()) != null;
					//更新されたID一覧が存在するか
					boolean existInMajority = e.getValue() != null
							&& e.getValue().getUpdated() != null;
					String method = null;
					boolean result = false;
					if (existInMajority) {
						if (existInMyDb) {
							//更新
							method = "更新";
							result = ups.update(e.getKey(), e.getValue());
						} else {
							//作成
							method = "作成";
							result = ups.create(e.getKey(), e.getValue());
						}
					} else {
						if (existInMyDb) {
							//削除
							method = "削除";
							result = ups.delete(e.getKey());
						} else {
							//処理する必要無し
							method = null;
							result = true;
						}
					}

					if (method != null) {
						Glb.debug("更新されたID一覧の" + method + "に"
								+ (result ? "成功" : "失敗") + " storeName="
								+ storeName + " historyIndex=" + e.getKey()
								+ " " + e.getValue());
					}
				} catch (Exception e1) {
					Glb.getLogger().error("", e1);
				}
			}
		});
	}

	private void step2FinishProc() {
		//取得すべきID一覧の登録
		getCtx().getStoreNameToGetIds().put(storeName, hashProc.getGetIds());

		//削除すべきIDを削除
		Glb.getObje().delete(hashProc.getShouldRemove(), storeName);
	}
}
