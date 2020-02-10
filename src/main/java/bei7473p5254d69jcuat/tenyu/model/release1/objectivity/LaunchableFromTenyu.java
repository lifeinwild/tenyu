package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;

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
	TenyuFile getExecutableLauncher();

	/**
	 * @return	環境検証プログラムのファイル
	 */
	TenyuFile getValidationLauncher();

}
