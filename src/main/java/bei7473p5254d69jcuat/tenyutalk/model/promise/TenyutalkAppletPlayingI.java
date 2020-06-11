package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import glb.*;

/**
 * アプレットの実行
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkAppletPlayingI extends ValidatableI{
	/**
	 * @return	このアプレットを公開しているリポジトリのID
	 */
	Long getTenyuRepositoryId();

	/**
	 * @return	このアプレットのjar
	 */
	TenyuArtifactI getTenyuArtifact();

	void stop();

	default TenyuRepositoryI getTenyuRepository() {
		return Glb.getObje()
				.getTenyuRepository(trs -> trs.get(getTenyuRepositoryId()));
	}

	default String getTenyutalkAppletDir() {
		return Glb.getFile().getTenyutalkAppletDir(getTenyuRepository());
	}

	/**
	 * @return	起動日時
	 */
	long getLaunchDate();

	/**
	 * 常駐型か
	 */
	boolean isService();

	/**
	 * Tenyu基盤ソフトウェア起動時に自動的に起動されるか
	 */
	boolean isStartup();

	void start();
}