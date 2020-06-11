package glb.util;

import java.nio.file.*;

public interface FileMetadataI {
	/**
	 * @return	アプリケーションフォルダ（プログラム実行時のカレントディレクトリ）からの相対パス
	 */
	String getRelativePathStr();

	default Path getRelativePath() {
		String p = getRelativePathStr();
		if (p == null)
			return null;
		return Paths.get(p);
	}

	byte[] getFileHash();

	long getFileSize();

}
