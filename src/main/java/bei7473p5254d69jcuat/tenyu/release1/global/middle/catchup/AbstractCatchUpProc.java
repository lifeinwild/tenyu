package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 一部の同調処理の処理構造を定義したクラス
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AbstractCatchUpProc extends AbstractCatchUpState {

	/**
	 * @param majorityAtStart	多数派の整合性情報
	 * @param myAtStart		自分の整合性情報
	 * @return			catchUpと同値
	 */
	public boolean catchUp() throws Exception {
		try {
			if (getCtx() == null || getCtx().getMajorityAtStart() == null
					|| getCtx().getMajorityAtStart().getCoreHash() == null) {
				Glb.getLogger().error("",
						new IllegalArgumentException("invalid CatchUpContext"));
				return false;
			}

			//activatedフラグによって特定の同調機能を無効化できる
			if(!activated) {
				finish = true;
				return finish;
			}

			if (isReset()) {
				//初回
				if (checkCatchUp()) {
					//最初から一致していた場合
					initiallyCatchUp = true;
					finish = true;
				} else {
					//差異がある場合、同調開始
					finish = false;
					requestAsync();

					//1件もリクエストが作成されなかったら終わり
					finish = isNoRequest();
				}
			} else {
				//二回目以降

				//完了した通信の返信を処理する
				procResponse();

				//全通信が完了したか
				finish = isNoRequest();

				if(finish) {
					end();
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			finish = true;
		}
		return finish;
	}

	/**
	 * その部分の同調処理が終わった時に呼ばれる
	 */
	protected abstract void end();

	/**
	 * @return	その同調処理が担当する要素について一致しているか、既に同調済みか
	 */
	protected abstract boolean checkCatchUp();

	/**
	 * catchUp()から、初回呼び出しでは呼ばれない事が保証される。
	 *
	 * @return	非同期通信が空か
	 */
	protected abstract boolean isNoRequest();

	/**
	 * @return	初回呼び出しが完了する前か
	 */
	protected abstract boolean isReset();

	/**
	 * 非同期通信の返信を処理
	 */
	protected abstract void procResponse();

	/**
	 * 非同期通信を作成
	 */
	protected abstract void requestAsync();

}