package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;

/**
 * {@link ModelConditionI#is(TenyupediaObject)}に対応するためのインターフェース。
 *
 * そのモデル条件の判定において実オブジェクトで判定する場合と
 * 参照で判定する場合があるので、
 * その共通インターフェースとして作成した。
 *
 * @param <V>	対象とするオブジェクト。{@link TenyuReferenceModelI}だと
 * 参照そのものではなく参照先オブジェクトがV。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyupediaObjectI<V extends ObjectI> {
	/**
	 * @return	モデル
	 */
	V getObj();

	long getUpdateDate();

	long getCreateDate();

	StoreName getStoreName();

	Long getId();

}
