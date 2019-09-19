package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * ファイルをアップロードする場合に使用されるファイルの情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class UploadFileGui {
	/**
	 * 指定されたフォルダからの相対パス
	 */
	private String dirAndFilenameFromSpecifiedFolder;
	/**
	 * アプリ外の絶対パス
	 */
	private Path originalPath;
	/**
	 * アプリ内の絶対パス
	 */
	private Path inAppPath;

	public byte[] digestOriginalPath() {
		try {
			return Glb.getUtil().digestFile(getOriginalPath());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public String getDirAndFilenameFromSpecifiedFolder() {
		return dirAndFilenameFromSpecifiedFolder;
	}

	public void setDirAndFilenameFromSpecifiedFolder(
			String dirAndFilenameFromSpecifiedFolder) {
		this.dirAndFilenameFromSpecifiedFolder = dirAndFilenameFromSpecifiedFolder;
	}

	public long getSize() {
		return originalPath.toFile().length();
	}

	public Path getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath(Path originalPath) {
		this.originalPath = originalPath;
	}

	public Path getInAppPath() {
		return inAppPath;
	}

	public void setInAppPath(Path inAppPath) {
		this.inAppPath = inAppPath;
	}
}
