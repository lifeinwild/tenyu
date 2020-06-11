package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia;

import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.ModelCondition.How.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;

/**
 * モデルの検索条件
 * 実質的には「情報への需要」の表現
 *
 * 自分用に作るのではなく、全ユーザーで共有する
 *
 * 作成者が管理する
 *
 * {@link ModelI}に依存させず{@link TenyupediaObjectI}に依存させる。
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

	boolean is(TenyupediaObjectI<? extends ModelI> m);

	/**
	 * @param l
	 * @param otherModelConditionId
	 * @return	サブインデックスストアのキー用のbyte[]
	 */
	static byte[] getModelConditionStoreKey(Logic l, Long otherModelConditionId) {
		int logicId = l.getId();
		return ByteBuffer.allocate(Integer.BYTES + Long.BYTES).putInt(logicId)
				.putLong(otherModelConditionId).array();
	}

	List<OtherModelCondition> getOtherModelConditions();

	List<StoreName> getStoreNames();

	List<TenyuReferenceModelI<? extends ModelI>> getManual();

	Long getStartSocialityId();

	List<Locale> getLocaleConditions();

	List<Long> getUserIds();
}
