package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia;

import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.reference.*;

/**
 * モデルの検索条件
 * 実質的には「情報への需要」の表現
 *
 * 自分用に作るのではなく、全ユーザーで共有する
 *
 * 作成者が管理する
 *
 * @author exceptiontenyu@gmail.com
 */
public interface ModelConditionI
		extends IndividualityObjectI, ModelConditionElementI {
	/**
	 * ID0はAll
	 */
	static final Long all = 0L;

	/**
	 * @return	何に対しても常に該当する条件か
	 */
	default boolean isAll() {
		return getId().equals(all);
	}

	boolean is(ModelI m);

	/**
	 * @param l
	 * @param otherConditionId
	 * @return	サブインデックスストアのキー用のbyte[]
	 */
	static byte[] getModelConditionStoreKey(Logic l, Long otherConditionId) {
		int logicId = l.getId();
		return ByteBuffer.allocate(Integer.BYTES + Long.BYTES).putInt(logicId)
				.putLong(otherConditionId).array();
	}

	Map<Logic, Long> getOtherModelConditionIds();

	List<StoreName> getStoreNames();

	List<TenyuReference<?>> getManual();

	Long getStartSocialityId();

	List<Locale> getLocales();

	List<Long> getTagIds();

	List<Long> getUserIds();
}
