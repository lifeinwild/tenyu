package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.*;
import glb.util.*;

/**
 * インスタンスがバージョニングされるオブジェクト。
 * １バージョンにつき１オブジェクトが作成され、
 * 初代オブジェクトのIDで同じ創作物のバージョン別オブジェクト一覧を特定できる。
 *
 * tenyutalkシステムによってホスティングされる。
 *
 * 画像、動画、MDファイル、jarファイル、ゲームのリプレイファイルなど
 * ファイル及びフォルダ全般を扱える。
 * TenyutalkはWWWの置き換えを主張しているが、
 * これらと{@link MultiplayerObjectI}でそれを達成する。
 *
 * サブインデックス
 * firstId.major.minor.patch : id
 * 初代オブジェクトIDとセマンテックバージョニングを指定すると一意にIDが対応づく。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@LocalHosting
public interface CreativeObjectI extends IndividualityObjectI {
	GeneralVersioning getVersion();

	/**
	 * @return	そのオブジェクトの初期バージョンのオブジェクトのID
	 */
	Long getFirstId();

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
