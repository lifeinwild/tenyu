package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;

/**
 * 言葉
 *
 * タグに用いられる
 * Tenyupediaにおける説明対象または事例対象となる概念
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TagI extends IndividualityObjectI {

	List<Long> getTagIds();

	void setTagIds(List<Long> tagIds);
}
