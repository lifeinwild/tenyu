package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.*;
import glb.util.*;

/**
 * インスタンスがバージョニングされるオブジェクト。
 * セマンテックバージョニングされる。
 *
 * tenyutalkシステムによってセルフホスティングされる
 *
 * サブインデックス
 * firstId.major.minor.patch : id
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@SelfHosting
public interface CreativeObjectDBI extends IndividualityObjectDBI {
	GeneralVersioning getVersion();

	/**
	 * @return	そのオブジェクトの初期バージョンのオブジェクトのID
	 */
	Long getFirstId();

	/**
	 * @return	アップロードしたユーザーのID
	 */
	Long getUploaderUserId();

	/**
	 * @return	公開日時証明をするか
	 */
	boolean isPublicationTimestamp();

	/**
	 * @return	初代オブジェクトか
	 */
	default boolean isFirst() {
		Long id = getId();
		if (id == null)
			throw new IllegalStateException("id is null.");
		Long firstId = getFirstId();
		if (firstId == null)
			throw new IllegalStateException("firstId is null");
		return id.equals(firstId);
	}
}
