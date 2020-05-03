package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;

/**
 * 認定
 *
 * ただし組織を表現するためにも用いる。
 * 基本的にTenyuプラットフォームの構想上組織概念は必要ないが
 * 任意の承認型の集合を表現できるので、僅かに用途があるかと思う。
 * 例えば{@link ModelConditionI}で特定の組織が作成したモデルを検索できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface CertificationI extends IndividualityObjectI {
	/**
	 * @return	認定している対象への参照のリスト
	 */
	List<TenyuReference<? extends ModelI>> getRefs();
}
