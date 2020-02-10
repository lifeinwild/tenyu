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
 * HistoryIndexがサブインデックスになることで
 * オブジェクトの同期生が分かるようになる。
 *
 * Model及びその子孫クラスはtransientフィールドを使って
 * 対応するストアにおいて処理を変化させれる。
 * 抽象インターフェースを共通化したままtransientフィールドによって
 * 処理に変化をつけれる。
 * 例えば{@link IdObjectDBI#isCatchUp()}など。
 *
 * @author exceptiontenyu@gmail.com
 */
public interface ModelDBI extends Storable, Serializable, UnversionableI,
		ChainVersionup, HasReference, HasGui {
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
	ModelStore<?, ? extends ModelDBI, ? extends ModelDBI> getStore(
			Transaction txn);

}
