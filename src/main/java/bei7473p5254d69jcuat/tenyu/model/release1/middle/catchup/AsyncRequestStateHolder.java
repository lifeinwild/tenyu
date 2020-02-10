package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

/**
 * 非同期リクエストの一覧を管理するオブジェクト
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <R>
 */
public abstract class AsyncRequestStateHolder<
		R extends P2PEdgeCommonKeyRequest> {
	/**
	 * オブジェクトをリセットしてから無効化されるまでの時間
	 */
	private static final long expired = 1000L * 60 * 2;

	public static long getExpired() {
		return expired;
	}

	/**
	 * オブジェクトがリセットされた日時
	 */
	protected long createDate = System.currentTimeMillis();
	protected final Supplier<ReadonlyNeighborList> getNeighborList;
	/**
	 * 管理している非同期リクエストの一覧
	 *
	 * 具象クラスはrequestsの要素について
	 * 使用したら削除する必要がある。
	 * started==trueかつrequestsが空である事が処理の完了を意味する。
	 */
	protected List<AsyncRequestState<R>> requests = new ArrayList<>();

	/**
	 * リトライされた回数
	 */
	protected long retryCount = 0;

	/**
	 * リトライの最大回数
	 *
	 * リトライは送信先を変えて行われるので、ある程度の回数試行する意味がある
	 */
	protected long retryMax = 25;

	/**
	 * 同調処理で使う場合
	 */
	public AsyncRequestStateHolder() {
		//ここでteachersを指定しているので正しい整合性情報を持っていたノードにのみ問い合わせることになる
		this.getNeighborList = () -> Glb.getMiddle().getObjeCatchUp().getCtx()
				.getTeachers();
	}

	/**
	 * 同調処理以外で使う場合
	 * @param getNeighborList	問い合わせ先の近傍一覧
	 */
	public AsyncRequestStateHolder(
			Supplier<ReadonlyNeighborList> getNeighborList) {
		this.getNeighborList = getNeighborList;
	}

	public void add(AsyncRequestState<R> a) {
		requests.add(a);
	}

	/**
	 * fを実行し、実行された場合のみrequestsをクリアする。
	 */
	public void checkAndFire(Runnable f) {
		if (isAllDone()) {
			try {
				f.run();
				requests.clear();
			} catch (Exception e) {
				Glb.debug("", e);
			}
		}
	}

	/**
	 * 全通信をキャンセルし、通信の状態管理をクリアする
	 */
	public void clear() {
		for (AsyncRequestState<R> e : requests) {
			if (e.getState().getState().isCancellable()) {
				e.getState().getState().cancel(true);
			}
		}
		requests.clear();
		createDate = System.currentTimeMillis();
		retryCount = 0;
	}

	public AsyncRequestState<R> get(int i) {
		return requests.get(i);
	}

	public long getCreateDate() {
		return createDate;
	}

	public List<AsyncRequestState<R>> getRequests() {
		return requests;
	}

	public long getRetryCount() {
		return retryCount;
	}

	public long getRetryMax() {
		return retryMax;
	}

	/**
	 * @return	全通信が終わったか
	 */
	public boolean isAllDone() {
		boolean allDone = true;
		for (AsyncRequestState<R> e : requests) {
			if (e.getState().isProc()) {
				allDone = false;
				break;
			}
		}
		return allDone;
	}

	/**
	 * @return	全ての通信が成功したか
	 */
	public boolean isAllSuccess() {
		boolean r = true;
		for (AsyncRequestState<R> e : requests) {
			if (!e.getState().isSuccess()) {
				r = false;
				break;
			}
		}
		return r;
	}

	/**
	 * @return	期限切れか
	 */
	public boolean isExpired() {
		long dif = System.currentTimeMillis() - createDate;
		return dif > expired;
	}

	public boolean isOverCount() {
		return retryCount > retryMax;
	}

	/**
	 * このオブジェクトに設定された近傍取得処理を用いて近傍一覧を取得し、
	 * それら近傍に同じリクエストを送信する。
	 * 近傍毎に異なるリクエストをする場合、自前でリクエスト処理を書いて
	 * requestsに加える必要がある。
	 * @param req
	 */
	public void request(R req) {

	}

	/**
	 * このオブジェクトに登録された全近傍に同じリクエストを送信する
	 * @param getReq	リクエストオブジェクトを使いまわす事はできないので
	 * リクエストのたびに作成して返す必要がある。
	 */
	public void requestToAll(Function<P2PEdge, R> getReq) {
		for (P2PEdge to : getNeighborList.get().getNeighborsCopy()) {
			try {
				R req = getReq.apply(to);
				Message m = Message.build(req).packaging(req.createPackage(to))
						.finish();
				RequestFutureP2PEdge state = Glb.getP2p().requestAsync(m, to);
				AsyncRequestState<
						R> e = new AsyncRequestState<R>(state, req, to);
				add(e);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
	}

	/**
	 * 1つのランダムに選択された近傍に1つのメッセージを送る
	 * @param req
	 */
	public void requestToRandomNeighbor(R req) {
		P2PEdge to = getNeighborList.get().getNeighborRandomWeight();
		Message m = Message.build(req).packaging(req.createPackage(to))
				.finish();
		RequestFutureP2PEdge state = Glb.getP2p().requestAsync(m, to);
		AsyncRequestState<R> e = new AsyncRequestState<R>(state, req, to);
		add(e);
	}

	/**
	 * 失敗した通信について近傍を変えて再試行する
	 *
	 * @return 再試行が行われたか
	 */
	protected boolean retry(AsyncRequestState<R> e, int i) {
		//何らかのプログラムのミスで無限に試行される可能性を無くす
		if (isExpired() || isOverCount())
			return false;

		retryCount++;

		//再試行
		//加重乱択で近傍を選択
		//P2PEdge to = neighbors.getNeighborRandomWeight();
		P2PEdge to = getNeighborList.get().getNeighborRandomWeight();//Glb.getSubje().getNeighborList().getNeighborRandomWeight();
		//リクエスト
		Message m = Message.build(e.getReq())
				.packaging(e.getReq().createPackage(to)).finish();
		RequestFutureP2PEdge state = Glb.getP2p().requestAsync(m, to);
		//新しい通信状態
		AsyncRequestState<
				R> retry = new AsyncRequestState<>(state, e.getReq(), to);
		//置き換える
		requests.set(i, retry);
		return true;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setRequests(List<AsyncRequestState<R>> requests) {
		this.requests = requests;
	}

	public void setRetryCount(long retryCount) {
		this.retryCount = retryCount;
	}

	public void setRetryMax(long retryMax) {
		this.retryMax = retryMax;
	}

	public int size() {
		return requests.size();
	}

	/**
	 * 全リクエストが終了するのを待つ
	 * デフォルトで最長1分待つ
	 */
	public void waitAllDone() {
		waitAllDone(1000L * 60);
	}

	/**
	 * 全リクエストが完了した後に呼ぶ想定
	 * @return	成功したリクエストの数
	 */
	public List<AsyncRequestState<R>> getOnlySuccess(
			Function<AsyncRequestState<R>, Boolean> additionalCondition) {
		List<AsyncRequestState<R>> r = new ArrayList<>();
		for (AsyncRequestState<R> state : requests) {
			if (!state.getState().isSuccess()) {
				continue;
			}

			if (additionalCondition != null
					&& !additionalCondition.apply(state)) {
				continue;
			}

			r.add(state);
		}
		return r;
	}

	public void waitAllDone(long timeout) {
		long elapsed = 0;
		long start = System.currentTimeMillis();
		while (elapsed < timeout) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (isAllDone()) {
				break;
			}
			elapsed = System.currentTimeMillis() - start;
		}
	}

}