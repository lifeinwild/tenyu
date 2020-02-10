package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
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
	 * @return	初代ID。初代オブジェクトならnull
	 */
	Long getFirstId();

	Long getUploaderUserId();

	boolean isPublicationTimestamp();
}
