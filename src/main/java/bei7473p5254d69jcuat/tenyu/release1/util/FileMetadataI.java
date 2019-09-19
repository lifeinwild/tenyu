package bei7473p5254d69jcuat.tenyu.release1.util;

public interface FileMetadataI {
	/**
	 * @return	アプリケーションフォルダからの相対パス
	 */
	public String getRelativePathStr();

	public byte[] getFileHash();

	public long getFileSize();
}
