package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.io.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

/**
 * 連番のオブジェクトを分散DLする
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AsyncRequestStatesSerialSharding<R extends P2PEdgeCommonKeyRequest>
		extends AsyncRequestStateHolder<R> {

	public AsyncRequestStatesSerialSharding() {
		super();
	}

	public AsyncRequestStatesSerialSharding(
			Supplier<ReadonlyNeighborList> getNeighborList) {
		super(getNeighborList);
	}

	/**
	 * requestsの最初から順にチェックしていって終わっていない処理があればそこで終了する。
	 * 失敗した処理があればリトライして終了する。
	 * requestsにidの連番に沿った順序でリクエストを登録すれば、
	 * idの連番に沿ってfiredが呼ばれる。
	 * @param fired
	 * @return 全ての通信が使用されたか
	 * @throws IOException	通信が失敗したらある程度の回数まで送信先を変えて再試行するが、
	 * それでも失敗し続けたら例外を投げる。
	 */
	public boolean checkAndFireRetrySerial(Consumer<AsyncRequestState<R>> fired)
			throws IOException {
		//失敗した通信を再試行
		for (int i = 0; i < requests.size(); i++) {
			AsyncRequestState<R> e = requests.get(i);
			//通信が失敗したか
			if (e.getState().isFailed()) {
				//再試行
				if (!retry(e, i)) {
					//再試行しても無駄ならrequestsを放棄する
					throw new IOException(
							"Failed to retry. requests were not completed. ");
				}
			}
		}

		//最初から連続して成功しているところまで反映
		for (int i = 0; i < requests.size(); i++) {
			AsyncRequestState<R> e = requests.get(i);
			//通信が成功したか
			if (e.getState().isSuccess()) {
				try {
					//取得したオブジェクトを使用する
					fired.accept(e);
					//使用済みの情報を削除
					requests.remove(i);
				} catch (Exception excep) {
					if (!retry(e, i)) {
						//再試行しても無駄ならrequestsを放棄する
						throw new IOException(
								"Failed to retry. requests were not completed. ");
					}
				}
			} else {
				//1つでも成功していない通信があればそこで終了
				return false;
			}
		}
		return requests.size() == 0;
	}

	/**
	 * requestsの最大件数
	 * このクラスを使用する場合、maxを超える件数を処理するなら、
	 * 処理を分けるような仕組みが必要。
	 */
	private static final int max = 1000 * 1000;

	/**
	 * 現在何番までDBに反映したか
	 */
	private long cursor = 0;

	public long getCursor() {
		return cursor;
	}

	public void incrementCursor() {
		cursor++;
	}
}
