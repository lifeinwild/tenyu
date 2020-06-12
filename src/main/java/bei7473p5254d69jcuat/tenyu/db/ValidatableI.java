package bei7473p5254d69jcuat.tenyu.db;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * このインターフェースはモデル検証を扱う。
 *
 * 一般に検証処理がどうあるべきかを考えると、
 * モデル検証と周辺検証（DB検証や通信検証等）があれば良い事が分かる。
 * 参照：rpc.md
 *
 * 周辺検証はDBや通信などモデルの周辺にある要素における検証。
 * システムの中心にはモデルがあるという観点で、その周辺を意味する。
 *
 * 例えばIDがDBで一意でなければならないというような検証処理は、
 * １個のオブジェクトがあるだけでは分からず、
 * DBという文脈が与えられて、あるオブジェクトがそのDBにおいて一意制約を破壊するか
 * が判定可能になる。だからその検証処理はモデル検証ではなく周辺検証になる。
 *
 * 通信検証は、例えば通信においてオブジェクトがlocalhost以外から届いてはいけない
 * という検証は周辺検証になる。
 *
 * モデル検証は様々なタイミング毎にある。
 * 例えば作成時、更新時、削除時など、様々なタイミングがある。
 * validateAtCreate,validateAtUpdate等。
 *
 * 例えば作成時に更新日時は作成日時と同じであるはずだが、
 * 更新時では作成日時より大きいはずで、作成日時以下だったら不正とみなせる。
 * この例から作成時と更新時で更新日時の検証処理が違うことが分かる。
 * 同様にタイミング毎に必要な検証処理は異なる。
 *
 * 参照検証validateReference()はそのオブジェクト自体の検証というより
 * その参照が現在のDB状態に照らして有効であるか、
 * つまり参照先オブジェクトがDBから見つかるかを検証するので、
 * 通常の検証処理（validateAtCreate等）から分離した。
 * 特に、具体的事情として、同調処理において通常の検証はしつつも
 * 参照検証をせずにDBに書き込みたい場合がある。
 * だから通常の検証と参照の検証は分けられている必要がある。
 * validateReferenceはモデル側検証とするかDB側検証とするか迷ったが
 * モデル側に定義している。
 * そこに不正状態が生じたとして壊れるのはDBの検索インターフェースではなく
 * モデル側のインターフェースだからだ。
 *
 * DB検証はモデル毎のストアクラスに書かれる。
 * dvValidateAtUpdate,exist,noExist等。
 * そこではDB内で一意であるべきフィールド等DBにおける制約が検証される。
 *
 * サブクラス（モデル系クラスのメンバー変数からネストされているクラス）について、
 * 一応{@link ValidatableI}を実装しているものの、
 * {@link ModelStore}から呼び出されるわけではないので、
 * もし他のクラスのメンバーになる場合があったら、
 * validateAtXXXForClassA
 * というようなメソッドを作る事で任意のホストクラスの事情に応じた検証処理を実装する。
 *
 * サブクラスの{@link ValidatableI}系インターフェースは適宜必要性を判断して呼び出したり
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
public interface ValidatableI {

	/**
	 * RPCリクエストオブジェクトのメンバー変数にこのオブジェクトがあった場合に
	 * このオブジェクトの状態を検証する。
	 *
	 * @return	妥当か
	 */
	default boolean validateAtRpc(ValidationResult r) {
		//RPCはセキュリティリスクが大きいので必ず個別に実装する。
		throw new UnsupportedOperationException();
	}

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
	 * モデルクラスがリストを持ち、
	 * そのリストの要素が{@link ValidatableI}を実装したサブクラスの場合について。
	 * そのような要素が更新された時、
	 * リスト上で元の要素と新しい要素を対応づける方法が無いので、
	 * 更新に関する問題を検出する事はできない、
	 * 即ちこのメソッドが実装されていても単に仕様を示すだけで実際に呼び出されない。
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