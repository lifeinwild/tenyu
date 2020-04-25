package bei7473p5254d69jcuat.tenyu.model.release1;

import java.io.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * 一般的な「モデル」という概念の妥当な形式化を
 * {@link Model}及び{@link ModelGui}及び{@link ModelStore}
 * そしてそれらの子孫クラスを通じて達成する。
 *
 * programmingEnvironment.mdにおいてこのような設計を継承構造と呼んでいる。
 *
 * モデルとして最も抽象的な概念を表現するこのクラスで扱える概念は限られる。
 * 作成日時等の時間概念、モデルが作成や更新や削除の時に検証される必要がある事、
 * GUIやストアがある事など。
 *
 * Model及びその子孫クラスはtransientフィールドを使って
 * 対応するストアにおいて処理を変化させれる。
 * 抽象インターフェースを共通化したままtransientフィールドによって
 * 処理に変化をつけれる。
 * 例えば{@link IdObjectI#isCatchUp()}など。
 *
 * @author exceptiontenyu@gmail.com
 */
public interface ModelI extends Storable, Serializable, UnversionableI,
		ChainVersionup, HasReference, HasGui {
	public static final long defaultDate = -1;
	public static final long defaultHistoryIndex = -1;

	default boolean isDefaultCreateHistoryIndex() {
		return getCreateHistoryIndex() == defaultHistoryIndex;
	}

	default boolean isDefaultCreateDate() {
		return getCreateDate() == defaultDate;
	}

	/**
	 * @return	作成HI
	 */
	@Unversionable
	long getCreateHistoryIndex();

	/**
	* @return	更新HI
	*/
	@Unversionable
	long getUpdateHistoryIndex();

	/**
	 * @return	作成日時
	 */
	@Unversionable
	long getCreateDate();

	/**
	* @return	更新日時
	*/
	@Unversionable
	long getUpdateDate();

	/**
	 * DBに最初に記録される時、検証直前に呼び出される
	 */
	void setupAtCreate();

	/**
	 * DBで更新される時、検証直前に呼び出される
	 */
	void setupAtUpdate();

	/**
	 * DBで削除される直前に呼び出される
	 */
	void setupAtDelete();

	/**
	 * transient
	 * @return	対応するストアの現在の状態に対して最後のキーである保証があるか
	 */
	boolean isLastKey();

	void setLastKey(boolean lastKey);

	/**
	 * @return	このモデルを格納するストアの名前
	 */
	StoreNameEnum getStoreName();

	/**
	 * @param txn
	 * @return	このモデルを格納するストア
	 */
	ModelStore<?, ? extends ModelI, ? extends ModelI> getStore(Transaction txn);

	ModelGui<?, ?, ?, ?, ?, ?> getGui(String guiName, String cssIdPrefix);

}
