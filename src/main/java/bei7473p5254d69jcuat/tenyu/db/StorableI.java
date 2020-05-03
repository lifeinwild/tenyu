package bei7473p5254d69jcuat.tenyu.db;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 一般に検証処理は何らかのシーケンスに文脈情報が渡されるときに
 * 妥当な情報があるかを検証する。
 * 典型例はメソッドの最初の引数のチェック処理等。
 * しかし、オブジェクトを利用するたびに詳細な検証処理を書くのは
 * 冗長だし、大部分の検証処理はメンバー変数に関する処理だから、
 * オブジェクトのメソッドとして実装するのが妥当と判断した。
 *
 * オブジェクトの検証処理は、作成時、更新時、削除時で異なる。
 * validateAtCreate,validateAtUpdate等。
 *
 * 参照の検証はそのオブジェクト自体の検証というより
 * その参照が現在のDB状態に照らして有効であるか、
 * つまり参照先オブジェクトがDBから見つかるかを検証するので、
 * 通常の検証処理から分離した。validateReference。
 * 具体的には同調処理において参照検証をせずに書き込みたい場合がある。
 * validateReferenceはモデル側検証とするかDB側検証とするか迷ったが
 * モデル側に定義している。
 *
 * 参照検証以外にもDB依存の検証処理がある。
 * DB上で重複してはならないフィールド等がある。
 * それはオブジェクトの内部で決定できる妥当性とは別で、DBとしての整合性検証。
 * そのような検証処理はストア側に書かれる。
 * dvValidateAtUpdate,exist,noExist等。
 *
 * サブクラス（モデル系クラスのメンバー変数を通じて参照されているクラス）について、
 * 一応Storableを実装しているものの、
 * {@link ModelStore}から呼び出されるわけではないので、
 * もし他のクラスのメンバーになる場合があったら、
 * validateAtXXXForClassA
 * というようなメソッドを作る事で任意のホストクラスの事情に応じた検証処理を実装する。
 *
 * サブクラスのStorable系インターフェースは適宜必要性を判断して呼び出したり
 * ホストクラス側で代替実装を用意する。
 *
 * 例えばホストクラスClassBがメンバー変数においてList<ClassA>を持つ場合、
 * ListであるせいでvalidateAtUpdateChangeのoldオブジェクトを特定しえないので、
 * サブクラスClassAのvalidateAtUpdateChangeを参照できない。
 * そのような場合でもClassAは仕様を示すためにそのメソッドを実装しておくべきだろう。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface StorableI {
	/**
	 * ストアから読み出された時に呼ばれる。
	 */
	/**
	default void onReadAtStore() {

	}
	*/

	/**
	 * ストアで作成される時、検証前に呼ばれる。
	 */
	/**
	default void onCreateAtStore() {

	}
	*/

	/**
	 * ストアで更新される時、検証前に呼ばれる。
	 */
	/**
	default void onUpdateAtStore() {

	}
	*/

	/**
	 * ストアで削除される時に呼ばれる。
	 */
	/**
	default void onDeleteAtStore() {

	}
	*/

	/**
	 * @return	{@link ModelStore#create(ModelI)}に入力可能か
	 */
	boolean validateAtCreate(ValidationResult r);

	/**
	 * @param r
	 * @param old
	 * @return	更新後の状態として妥当か
	 */
	boolean validateAtUpdate(ValidationResult r);

	/**
	 * 変更不可なフィールドが変更されていないかなど
	 *
	 * 現在想定される用途ではoldはthisと同じ型になるケースしかないが、
	 * 異なる型とも検証可能なインターフェースにした。
	 * 具象クラスの型を使おうとするとモデルの継承ツリーに総称型の指定が発生し、
	 * その記述の面倒さや連鎖的に引き起こされる問題が大きすぎる。
	 *
	 * @param r
	 * @param old	thisの更新前の状態
	 * @return	oldからの変化は妥当か
	 */
	default boolean validateAtUpdateChange(ValidationResult r, Object old) {
		return true;
	}

	/**
	 * @return	{@link ModelStore#delete(Model)}に入力可能か
	 */
	default boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	/**
	 * {@link Lang#ERROR_DB_NOTFOUND_REFERENCE}を報告する検証処理。
	 *
	 * 他モデルへの参照を検証する。例えばLong userId、Long styleIdなど。
	 * そのようなIDは実際にDBに存在するオブジェクトを参照している必要がある。
	 *
	 * ただし自分のIDは検証しない。
	 * 自分のIDは、DBに登録される前DB上には存在していない。
	 *
	 * なお同調処理では参照の検証をすべきではないので、
	 * この処理を他の検証処理から独立させ、呼ぶ場合と呼ばない場合を
	 * 選択できるようにする必要があった。
	 *
	 * このメソッドはモデル検証を通過した場合に呼ばれるので、
	 * 各フィールドのヌルチェック等validateAtCreateやUpdateで行われる検証は不要。
	 *
	 * ERROR_DB_NOTFOUND_REFERENCEを出す。
	 *
	 * 物理削除において参照が残ってしまうとシャドー生成の問題があるので、
	 * 確実に消す必要がある。外部ツールを活用する必要がある
	 *
	 * @return	そのメソッド呼び出しでエラーメッセージが追加された場合false、されなかった場合true
	 */
	boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception;

}