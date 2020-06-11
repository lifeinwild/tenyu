package bei7473p5254d69jcuat.tenyu.model.promise;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;

public interface HasTag {

	/**
	 * @param tag
	 * @return	追加されたか
	 */
	default boolean addTagId(Long tagId) {
		return getTagIds().add(tagId);
	}

	default boolean addTag(Tag t) {
		return getTagIds().add(t.getId());
	}

	/**
	 * @return	このオブジェクトに関連するタグ
	 */
	List<Long> getTagIds();

}
