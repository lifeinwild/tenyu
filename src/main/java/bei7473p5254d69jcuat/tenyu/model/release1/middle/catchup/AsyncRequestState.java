package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;

/**
 * １近傍へのリクエスト
 * @author exceptiontenyu@gmail.com
 *
 * @param <R>
 */
public class AsyncRequestState<R extends P2PEdgeCommonKeyRequest> {
	private R req;
	private RequestFutureP2PEdge state;
	private P2PEdge to;

	public AsyncRequestState(RequestFutureP2PEdge state, R req, P2PEdge to) {
		if(state == null)
			throw new IllegalArgumentException("state is null");
		this.state = state;
		this.req = req;
		this.to = to;
	}

	public R getReq() {
		return req;
	}

	public RequestFutureP2PEdge getState() {
		return state;
	}

	public P2PEdge getTo() {
		return to;
	}
}