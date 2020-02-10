package glb.util;

public interface FileMetadataI {
	/**
	 * @return	アプリケーションフォルダからの相対パス
	 */
	public String getRelativePathStr();

	public byte[] getFileHash();

	public long getFileSize();
}
