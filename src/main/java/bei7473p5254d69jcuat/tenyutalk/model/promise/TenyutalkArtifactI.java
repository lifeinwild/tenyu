package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import glb.*;

/**
 * 成果物
 *
 * 成果物は１ファイルまたは１フォルダ。
 * フォルダの場合それ以下の全ファイルフォルダが成果物に含められる。
 * ただしシンボリックリンクと隠しファイルフォルダは無視される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkArtifactI extends ObjectI {

	Long getTenyuArtifactId();

	default TenyuArtifactI getTenyuArtifact() {
		return Glb.getObje()
				.getTenyuArtifact(tas -> tas.get(getTenyuArtifactId()));
	}

	/**
	 * @return	ファイルフォルダから取得されたバージョン
	 */
	/*
	default Version getVersionFromFile() {
		TenyutalkFileMetadataI meta = getTenyutalkFileMetadataI();
		String s = meta.getRelativePathStr();
		Version v = new Version();
		Version.valueOf(version);
	}
	*/

}
