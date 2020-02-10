package bei7473p5254d69jcuat.tenyu.db.store;

import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import glb.util.*;

/**
 * idは0から連番
 * 無い場合null
 * Long idをつけるのは膨大な行を持つDBを想定しているから。
 * IdObjectは本質的にDBに記録されるオブジェクト。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface IdObjectDBI extends ModelDBI{
	/**
	 * 削除されたID
	 *
	 * あるオブジェクトのメンバー変数に他のオブジェクトのIDがあり
	 * その参照先オブジェクトが削除された場合に、
	 * 使用不可能になった参照であることを示すために設定する等。
	 */
	public static Long getDeletedId() {
		return -5L;
	}

	/**
	 * GUIで例外的な値を表示するため
	 * @return	例外的な場合を意味する値
	 */
	public static Long getExceptionalId() {
		return -2L;
	}

	/**
	 * 連番のIDの最小値
	 * これを変更するとHashStore等に深刻な影響が出る。
	 */
	public static Long getFirstId() {
		return 0L;
	}

	/**
	 * Nullが使えない場合に使用する
	 * @return 未設定を意味する値
	 */
	public static Long getNullId() {
		return -1L;
	}

	/**
	 * @return	getId()で返されるidのバイト数
	 */
	public static int getIdSize() {
		return Long.BYTES;
	}

	/**
	 * @return	システムによって作成された場合のID
	 */
	public static Long getSystemId() {
		return -4L;
	}

	/**
	 * @return	議決で作成された場合のID
	 */
	public static Long getVoteId() {
		return -3L;
	}

	public static boolean isSpecialId(Long id) {
		if (id == null)
			return false;
		return id.equals(getNullId()) || id.equals(getExceptionalId())
				|| id.equals(getVoteId()) || id.equals(getSystemId());
	}

	/**
	 * クラス内識別子 連番
	 * 更新で変わる事は無い。
	 * 再利用されない。
	 *
	 * @return
	 */
	Long getId();

	void setId(Long id);

	/**
	 * @return	{@link HashStore}のhid
	 */
	Long getHid();

	void setHid(Long hid);

	/**
	 * 同調処理では通常の作成、更新といった概念を適用できない。
	 * 同調処理で得たオブジェクトは数回の更新を経たオブジェクトかもしれないので
	 * 作成段階では認められない状態になっているかもしれず、
	 * 作成時検証に入力できない。
	 * 一方で同調処理で得たオブジェクトの直前の状態を持っているわけではない。
	 * 自分が持っているのはずっと昔のオブジェクトである可能性がある。
	 * だから更新時検証もできない。
	 * とはいえ無検証でDBに入れるわけにはいかない。
	 * そこで同調時検証が必要になる。
	 *
	 * @param r	作成時検証と更新時検証両方の結果が返る
	 * @return	同調処理でDBに入力可能か。ValidationResultが1件以上あっても
	 * trueの場合がある。
	 */
	default boolean validateAtCatchUp() {
		//作成時検証または更新時検証のいずれかを通過するはず
		return validateAtCreate(new ValidationResult())
				|| validateAtUpdate(new ValidationResult());
	}

	/**
	 * transient
	 * @return	同調処理で得たオブジェクトか
	 */
	boolean isCatchUp();
	void setCatchUp(boolean catchup);

	/**
	 * transient
	 * @return	DBに頼らずIDを指定するオブジェクトか
	 * ただし同調処理でIDが設定済みの場合false
	 * 作者ユーザ作成等の特殊処理でtrueとなる
	 */
	boolean isSpecifiedId();
	void setSpecifiedId(boolean recycle);

	boolean isRecycleHid();
	void setRecycleHid(boolean recycle);

}
