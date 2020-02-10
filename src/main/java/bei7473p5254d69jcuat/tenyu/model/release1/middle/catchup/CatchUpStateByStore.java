package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import glb.util.*;

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
	 * リサイクルHIDや更新されたIDから作成された取得すべきID一覧のオブジェクトを取得する通信の管理
	 */
	private CatchUpStateByStoreStepGetObj objProc = new CatchUpStateByStoreStepGetObj();

	/**
	 * リサイクルHIDを取得する通信の管理
	 */
	private CatchUpStateByStoreStepRecycleHid recycleHidProc = new CatchUpStateByStoreStepRecycleHid();

	/**
	 * 処理段階
	 */
	private int step = 0;

	/**
	 * 対象とするストア
	 */
	private StoreNameObjectivity storeName;

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
				if (!recycleHidProc.isFinish()) {
					Glb.debug("recycleHidProc.catchUp();");
					recycleHidProc.catchUp();
				}

				if (!updatedIdProc.isFinish()) {
					Glb.debug("updatedHidProc.catchUp();");
					updatedIdProc.catchUp();
				}

				if (recycleHidProc.isFinish() && updatedIdProc.isFinish()) {
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

	public CatchUpStateByStoreStepRecycleHid getRecycleHidProc() {
		return recycleHidProc;
	}

	public void resetConcrete() {
		recycleHidProc.reset();
		recycleHidProc.setStoreName(storeName);

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

	public void setStoreName(StoreNameObjectivity storeName) {
		this.storeName = storeName;
	}

	protected HashSet<Long> getMyRecycleHids() {
		return Glb.getObje().readRet(txn -> {
			try {
				return new HashSet<>(
						new RecycleHidStore(storeName, txn).getAllIds());
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
		step0FinishProcId();
		step0FinishProcHid();
	}

	/**
	 * 最後のHIDやリサイクルHID一覧の差から取得すべきHID一覧を作成する
	 */
	private void step0FinishProcHid() {
		//自分の全リサイクルHID一覧
		HashSet<Long> myRecycleHids = getMyRecycleHids();
		Glb.debug("storeName=" + storeName + " myRecycleHids=" + myRecycleHids);

		//多数派の全リサイクルHID一覧
		HashSet<Long> majorityRecycleHids;
		if (recycleHidProc.isInitiallyCatchUp()) {
			majorityRecycleHids = myRecycleHids;
		} else {
			//近傍から得たリサイクルHID一覧
			List<IDList> recycleHidSrc = recycleHidProc.getRecycleHids();

			majorityRecycleHids = new HashSet<>();
			for (IDList l : recycleHidSrc) {
				l.uncompressToHashSet(majorityRecycleHids);
			}
		}
		Glb.debug("initiallyCatchUp=" + recycleHidProc.isInitiallyCatchUp()
				+ "storeName=" + storeName + " majorityRecycleHids="
				+ majorityRecycleHids);

		//取得すべきHID一覧
		HashSet<Long> getHids = new HashSet<>();

		//多数派の最後のHID
		Long majorityLastHid = getCtx().getMajorityAtStart().getByStore()
				.get(storeName).getLastHidOfHashStore();
		if (majorityLastHid == null)
			majorityLastHid = IdObjectDBI.getFirstId() - 1;

		//自分の最後のHID
		Long myLastHid = getCtx().getMyAtStart().getByStore().get(storeName)
				.getLastHidOfHashStore();
		if (myLastHid == null)
			myLastHid = IdObjectDBI.getFirstId() - 1;

		Glb.debug("majorityLastHid=" + majorityLastHid + " myLastHid="
				+ myLastHid);

		//最後のHIDを一致させるため不足HIDを登録
		for (long hid = myLastHid + 1; hid <= majorityLastHid; hid++) {
			//削除済みHIDなら別途登録されるので無視
			if (!majorityRecycleHids.contains(hid))
				getHids.add(hid);
		}

		final long myLastHidTmp = myLastHid;
		final long majorityLastHidTmp = majorityLastHid;

		//DB系処理
		Glb.getObje().execute(txn -> {
			try {
				//このオブジェクトが対象としているストア
				IdObjectStore<?, ?> s = storeName.getStore(txn);

				//最後のHIDについて自分の方が多数派より進んでいるか
				if (myLastHidTmp > majorityLastHidTmp) {
					//なぜか自分のほうが多数派よりもIDが進んでいた場合
					//リサイクルHIDが生じない削除処理で過剰分を削除
					for (long hid = myLastHidTmp; hid > majorityLastHidTmp; hid--) {
						s.deleteCatchUp(hid);
					}
					//ハッシュ木を縮小して多数派と同じ状態にする
					s.getHashStore().cut(majorityLastHidTmp);
				}

				if (recycleHidProc.isInitiallyCatchUp()) {
					//最初から一致していた場合、何もしない
					Glb.getLogger().info(
							"recycleHidProc initiallyCatchUp=true storeName=" + storeName);
				} else {
					//リサイクルHID一覧の差異から取得すべきHID一覧へ追加

					//多数派リサイクルHID一覧に対する自分側の過剰分=多数派の不足分
					Collection<Long> myExtraRecycleHids = Glb.getUtil()
							.getExtra(myRecycleHids, majorityRecycleHids);

					//自分のリサイクルHID一覧に対する多数派の過剰分=自分の不足分
					Collection<Long> majorityExtraRecycleHids = Glb.getUtil()
							.getExtra(majorityRecycleHids, myRecycleHids);

					//自分側過剰分を取得HIDとして追加
					Glb.debug("storeName=" + storeName + " myExtraRecycleHids="
							+ myExtraRecycleHids);
					for (Long hid : myExtraRecycleHids) {
						getHids.add(hid);
					}

					//多数派側過剰分についてDBから削除
					Glb.debug("storeName=" + storeName
							+ " majorityExtraRecycleHids="
							+ majorityExtraRecycleHids);
					for (Long hid : majorityExtraRecycleHids) {
						//未削除のを削除
						Long id = s.getIdByHid(hid);
						if (id != null) {
							s.delete(id);
						}
					}
				}

				Glb.debug("getHids=" + getHids);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});

		//同調文脈に設定
		getCtx().getStoreNameToGetHids().put(storeName, getHids);
	}

	/**
	 * 更新されたID一覧の差異から取得すべきID一覧を作成する
	 */
	private void step0FinishProcId() {
		//多数派の更新されたID一覧
		//初期同調だった場合このリストは空。
		//しかしこの処理においては問題が生じない。
		Map<Long, CatchUpUpdatedIDList> updatedIdsMajority = updatedIdProc
				.getUpdatedIds();

		//取得すべきID一覧
		HashSet<Long> getIds = new HashSet<>();
		if (updatedIdsMajority != null) {
			//ヒストリーインデックス毎の更新ID一覧について自分と多数派の差異を取得ID一覧に追加
			max : for (Entry<Long, CatchUpUpdatedIDList> e : updatedIdsMajority
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
				for (long id : majorityUpdatedIds) {
					//自分の更新済みID一覧に含まれていたら取得しない。
					//自分がそのオブジェクトの最新状態を持っている可能性が高いからだが、
					//この条件では絶対の保証はない。
					//しかし細かい問題はハッシュ木で解決される。
					if (myUpdatedIds != null && myUpdatedIds.contains(id)) {
						continue;
					}

					//取得すべきID一覧に追加
					getIds.add(id);

					//更新されたIDの上限数
					if (getIds.size() > GetObj.max * 100) {
						break max;
					}
				}
			}
		}

		//同調文脈に設定
		getCtx().getStoreNameToGetIds().put(storeName, getIds);
	}

	private void step1FinishProc() {
		//更新されたIDをDBに書き込む
		Glb.getObje().execute(txn -> {
			IdObjectStore<? extends IdObjectDBI,
					?> s = storeName.getStore(txn);
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
		//取得すべきHID一覧の登録
		getCtx().getStoreNameToGetHids().put(storeName, hashProc.getGetHids());

		//削除すべきIDを削除
		Glb.getObje().deleteByHids(hashProc.getShouldRemoveHids(), storeName);
	}
}
