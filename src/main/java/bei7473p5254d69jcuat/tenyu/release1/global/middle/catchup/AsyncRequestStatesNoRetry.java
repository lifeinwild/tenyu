package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetIntegrity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

/**
 * 多数の近傍にリクエストを送って、リトライしない。
 * その結果から信用ベースの加重多数決を行う機能があるが、
 * それを使用しないなら単にリトライ無しの一斉リクエスト機能。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <R>
 */
public class AsyncRequestStatesNoRetry<R extends P2PEdgeCommonKeyRequest>
		extends AsyncRequestStateHolder<R> {
	public AsyncRequestStatesNoRetry() {
		super();
	}

	public AsyncRequestStatesNoRetry(
			Supplier<ReadonlyNeighborList> getNeighborList) {
		super(getNeighborList);
	}

	/**
	 * 全リクエストが完了している前提
	 * 各リクエストについてfを実行する
	 * 処理されたリクエストはrequestsから削除される
	 *
	 * @param f
	 * @return	全リクエストが処理されたらtrue、そうでなければfalse
	 */
	public boolean checkAndFireNoRetry(Consumer<AsyncRequestState<R>> f) {
		if (isAllDone()) {
			for (AsyncRequestState<R> state : requests) {
				try {
					f.accept(state);
				} catch (Exception e) {
					Glb.debug("", e);
				}
				requests.remove(state);
			}
			return true;
		} else {
			Glb.getLogger().warn("not all done", new Exception());
			return false;
		}
	}

	/**
	 * @param getValue		メッセージクラス毎の投票値取得処理
	 * @return				多数決の結果
	 */
	public <GenericsResult,
			GenericsResponse extends Communicatable> GenericsResult majority(
					GenericsResult myValue,
					Function<Communicatable, GenericsResult> getValue) {
		if (requests.size() == 0)
			return null;
		Map<GenericsResult, Integer> majorityFormat = new HashMap<>();

		//今回応答した近傍全体の合計信用
		int totalCredit = 0;
		for (AsyncRequestState<?> state : getRequests()) {
			try {
				totalCredit += state.getTo().creditForCatchUp();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				continue;
			}
		}

		//自信の最大値。近傍全体の半分を超えないようにする
		int creditSelfMax = (int) (totalCredit * 0.49);
		if (creditSelfMax < 5) {
			creditSelfMax = 5;
		}

		//自分の主張値を加える
		int creditSelf = 10;

		//自分の客観が最新だと思っているなら少し自分の意見の影響力を強める
		if (Glb.getMiddle().getObjeCatchUp().imVeteran()) {
			creditSelf += 10;
		}
		creditSelf *= Glb.getMiddle().getObjeCatchUp().getCatchUpImpression();

		if (creditSelf > creditSelfMax)
			creditSelf = creditSelfMax;
		if (creditSelf <= 0)
			creditSelf = 1;

		majorityFormat.put(myValue, creditSelf);

		for (AsyncRequestState<?> state : getRequests()) {
			try {
				//近傍の信用
				int credit = state.getTo().creditForCatchUp();
				if (credit == 0)
					continue;

				//各近傍から届いた値
				if (Response.fail(state.getReq().getRes())) {
					continue;
				}
				Content res = state.getReq().getRes().getContent();
				GenericsResult value = getValue.apply(res);
				if (value == null)
					continue;

				GetIntegrityResponse resC = (GetIntegrityResponse) res;
				Glb.debug("sociality lastid = "
						+ resC.getIntegrity().getByStore().get("Sociality")
								.getLastIdOfHashStore()
						+ " port=" + state.getTo().getNode().getP2pPort());

				//必要な情報が揃っていれば投票値として登録
				if (value != null && credit > 0) {
					Integer exist = majorityFormat.get(value);
					if (exist == null)
						exist = 0;
					exist += credit;
					majorityFormat.put(value, exist);
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				continue;
			}
		}
		return (GenericsResult) Glb.getUtil().majority(majorityFormat);
	}

}