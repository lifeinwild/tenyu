package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;

/**
 * 認定
 * 特定のユーザーが任意の名目を作成し、任意のモデルを認定していける。
 * 認定は{@link ModelConditionI}で検索条件として使用できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface CertificationI extends IndividualityObjectI {
	/**
	 * @return	認定している対象への参照のリスト
	 */
	List<TenyuReferenceModelI<? extends ModelI>> getCertificateds();
}
