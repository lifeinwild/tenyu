package bei7473p5254d69jcuat.tenyu.model.promise.objectivity;

import java.io.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * プログラミングで使われる意味での「モデル」という概念の妥当な形式化を
 * これを実装したクラス群で表現する。
 * programmingEnvironment.mdにおいてこのような設計を継承構造と呼んでいる。
 *
 * {@link ModelI}は連番のIDを持ち永続化される事が想定されるオブジェクト
 *
 * モデルとして最も抽象的な概念を表現するこのクラスで扱える概念は極めて限られる。
 * 一般論として、多くの開発者の経験上、
 * 連番IDと作成日時と更新日時は全てのモデルに必要と言われている。
 * そのため継承構造の頂点（この上は{@link Object}というJava標準の抽象クラス）である
 * このレベルでは、そのような普遍的情報のみ想定できる。
 *
 * 他にこのレベルで想定できる事は、モデルの周辺にGUIやストアがある事。
 *
 * 継承構造の強すぎる共通化は以下テクニックによって少し柔軟性を得ている。
 * - transientフィールドを使って対応するストアにおいて処理を変化させるという事をしている。
 * 例えば{@link ModelI#isCatchUp()}など。
 * - 一部メソッドを具象クラスでオーバーライドする事で具象クラスによって
 * 抽象クラスの一部動作を変更できる。
 *
 * @author exceptiontenyu@gmail.com
 */
public interface ModelI extends StorableI, Serializable, UnversionableI,
		ChainVersionup, HasReference, HasGui {
	public static final long defaultDate = -1;
	public static final long defaultHistoryIndex = -1;

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
	 * 連番の最初のID
	 * これを変更すると{@link HashStore}等に深刻な影響が出る。
	 */
	public static Long getFirstId() {
		return 0L;
	}

	/**
	 * @return	getId()で返されるidのバイト数
	 */
	public static int getIdSize() {
		return Long.BYTES;
	}

	/**
	 * Nullが使えない場合に使用する
	 * @return 未設定を意味する値
	 */
	public static Long getNullId() {
		return -1L;
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
	 * @return	作成日時
	 */
	@Unversionable
	long getCreateDate();

	/**
	 * 最初の設定値から更新されない。
	 * @return	作成HI
	 */
	@Unversionable
	long getCreateHistoryIndex();

	ModelGui<?, ?, ?, ?, ?, ?> getGui(String guiName, String cssIdPrefix);

	/**
	 * 一部モデルは{@link HashStore}を使わないが、
	 * それでもHIDをセットすべき。
	 *
	 * @return	{@link HashStore}のhid
	 */
	Long getHid();

	/**
	 * クラス内識別子 連番
	 * 更新で変わる事は無い。
	 * 再利用されない。
	 *
	 * @return	Nullable
	 */
	Long getId();

	/**
	 * @param txn
	 * @return	このモデルを格納するストア
	 */
	ModelStore<? extends ModelI, ? extends ModelI> getStore(
			Transaction txn);

	/**
	 * @return	このモデルを格納するストアの名前
	 */
	StoreName getStoreName();

	/**
	 * @return	更新日時
	 */
	@Unversionable
	long getUpdateDate();

	/**
	 * 作成時は初期値で、ストアでもサブインデックスが作成されない。
	 * @return	更新HI
	 */
	@Unversionable
	long getUpdateHistoryIndex();

	/**
	 * transient
	 * @return	同調処理で得たオブジェクトか
	 */
	boolean isCatchUp();

	default boolean isDefaultCreateDate() {
		return getCreateDate() == defaultDate;
	}

	default boolean isDefaultCreateHistoryIndex() {
		return getCreateHistoryIndex() == defaultHistoryIndex;
	}

	/**
	 * transient
	 * @return	対応するストアの現在の状態に対して最後のキーである保証があるか
	 */
	boolean isLastKey();

	boolean isRecycleHid();

	/**
	 * transient
	 * @return	DBに頼らずIDを指定するオブジェクトか
	 * ただし同調処理でIDが設定済みの場合false
	 * 作者ユーザ作成等の特殊処理でtrueとなる
	 */
	boolean isSpecifiedId();

	void setCatchUp(boolean catchup);

	void setCreateHistoryIndex(long historyIndex);

	void setHid(Long hid);

	void setId(Long id);

	void setLastKey(boolean lastKey);

	void setRecycleHid(boolean recycle);

	void setSpecifiedId(boolean recycle);

	/**
	 * DBに最初に記録される時、検証直前に呼び出される
	 */
	void setupAtCreate();

	/**
	 * DBで削除される直前に呼び出される
	 */
	void setupAtDelete();

	/**
	 * DBで更新される時、検証直前に呼び出される
	 */
	void setupAtUpdate();

	void setUpdateHistoryIndex(long historyIndex);

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
	 * @return	同調処理でDBに入力可能か。{@link ValidationResult}が1件以上あってもtrueの場合がある。
	 */
	default boolean validateAtCatchUp() {
		//作成時検証または更新時検証のいずれかを通過するはず
		return validateAtCreate(new ValidationResult())
				|| validateAtUpdate(new ValidationResult());
	}

}
