package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;

/**
 * 同じタスクのための多数のリクエスト
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <R>
 */
public class AsyncRequestStatesFreeSharding<R extends P2PEdgeCommonKeyRequest>
		extends AsyncRequestStateHolder<R> {
	public AsyncRequestStatesFreeSharding() {
		super();
	}

	public AsyncRequestStatesFreeSharding(
			Supplier<ReadonlyNeighborList> getNeighborList) {
		super(getNeighborList);
	}

	/**
	 * @param f	falseを返した場合、リトライされる。
	 */
	public void checkAndFireRetry(Function<AsyncRequestState<R>, Boolean> f) {
		for (int i = 0; i < requests.size(); i++) {
			AsyncRequestState<R> e = requests.get(i);
			//通信中ならまだ判定しない
			if(!e.getState().isDone())
				continue;
			if (e.getState().isSuccess()) {//通信が成功したか
				try {
					//通信に成功したら得られたデータに応じた処理をする
					if (!f.apply(e)) {
						//相手が不正なデータを返してきた場合などで処理段階で失敗した場合
						//別の相手からデータを取得しなおす
						retry(e, i);
						continue;
					}
					requests.remove(i);
				} catch (Exception excep) {
					//これも処理段階の失敗。またはバグ
					retry(e, i);
				}
			} else if(e.getState().isFailed()){	//通信が失敗したか
				//なお成功じゃないなら失敗かというとそうではない。
				//例えばまだ通信中の場合がある。
				//だからisFailedで確認する必要がある
				retry(e, i);
			}
		}
	}
}