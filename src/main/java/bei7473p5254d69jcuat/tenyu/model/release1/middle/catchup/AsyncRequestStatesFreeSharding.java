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
			if (e.getState().isSuccess()) {
				try {
					if (!f.apply(e)) {
						retry(e, i);
						continue;
					}
					requests.remove(i);
				} catch (Exception excep) {
					retry(e, i);
				}
			} else {
				retry(e, i);
			}
		}
	}
}