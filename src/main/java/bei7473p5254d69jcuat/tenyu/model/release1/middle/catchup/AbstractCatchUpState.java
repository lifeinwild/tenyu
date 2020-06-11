package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;

public abstract class AbstractCatchUpState {
	/**
	 * 停止スイッチ
	 * テストで特定の機能を無効化して同調処理するために用意した。
	 * 基本的にプロセス起動時に設定され、それ以降変化しない。
	 */
	protected boolean activated = true;

	/**
	 * 最後の同調処理で同調を確認できたか
	 */
	protected boolean finish = false;

	/**
	 * 同調処理するまでもなく最初から一致していたか
	 */
	protected boolean initiallyCatchUp = false;

	public boolean isInitiallyCatchUp() {
		return initiallyCatchUp;
	}

	public void setInitiallyCatchUp(boolean initiallyCatchUp) {
		this.initiallyCatchUp = initiallyCatchUp;
	}

	protected CatchUpContext getCtx() {
		return Glb.getMiddle().getObjeCatchUp().getCtx();
	}

	public boolean isFinish() {
		return finish;
	}

	/**
	 * 処理を開始できる状態にする。
	 * 使用される前に必ず一度呼び出される。
	 */
	public void reset() {
		this.finish = false;
		initiallyCatchUp = false;
		resetConcrete();
	}

	protected abstract void resetConcrete();

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public static abstract class AbstractCatchUpByStoreStep
			extends AbstractCatchUpProc {
		protected StoreNameObjectivity storeName;

		public void setStoreName(StoreNameObjectivity storeName) {
			this.storeName = storeName;
		}
	}

}