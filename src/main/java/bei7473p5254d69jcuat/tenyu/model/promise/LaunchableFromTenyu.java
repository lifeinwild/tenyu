package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyutalk.file.*;

/**
 * Tenyuから起動可能な外部プログラムに関するメタデータが実装すべきインターフェース
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface LaunchableFromTenyu {
	/**
	 * @return	起動ファイル
	 */
	TenyutalkFileMetadataI getExecutableLauncher();

}
