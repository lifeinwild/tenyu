package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;

/**
 * User等の多数のオブジェクトを格納する客観系ストアの同調処理
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpStateDB {
	/**
	 * 同調処理を実行するスレッド
	 */
	protected Thread catchUpThread = null;

	/**
	 * ストア毎の同調処理関係の状態とそれら状態に関係した処理。
	 */
	private Map<StoreNameObjectivity,
			CatchUpStateByStore> stateByStores = new HashMap<>();

	public CatchUpStateDB() {
		init();
	}

	private void init() {
		for (StoreNameObjectivity storeName : StoreNameObjectivity.values()) {
			CatchUpStateByStore byStore = new CatchUpStateByStore();
			byStore.setStoreName(storeName);
			stateByStores.put(storeName, byStore);
		}
	}

	/**
	 * DB同調
	 * @param majorityAtStart
	 * @param myAtStart
	 * @return ストア別同調状態で1つでもまだ呼び出す必要があるものがあればfalse
	 */
	public boolean catchUp() {
		boolean r = true;
		//各ストアの同調処理を呼び出す
		for (CatchUpStateByStore e : getStateByStores().values()) {
			if (!e.isFinish()) {
				//１つでもfalseがあればfalse
				if (!e.catchUp()) {
					r = false;
				}
			}
		}
		return r;
	}

	public Thread getCatchUpThread() {
		return catchUpThread;
	}

	public Map<StoreNameObjectivity, CatchUpStateByStore> getStateByStores() {
		return stateByStores;
	}

	public boolean isFinish() {
		for (CatchUpStateByStore e : stateByStores.values()) {
			if (!e.isFinish()) {
				return false;
			}
		}
		return true;
	}

	public void reset() {
		for (CatchUpStateByStore e : getStateByStores().values()) {
			e.reset();
		}

		if (catchUpThread != null && !catchUpThread.isInterrupted()
				&& catchUpThread.isAlive()) {
			catchUpThread.interrupt();
		}
		catchUpThread = null;
	}

	public void setCatchUpThread(Thread catchUpThread) {
		this.catchUpThread = catchUpThread;
	}

	public void setStateByStores(
			Map<StoreNameObjectivity, CatchUpStateByStore> stateByStores) {
		this.stateByStores = stateByStores;
	}

}