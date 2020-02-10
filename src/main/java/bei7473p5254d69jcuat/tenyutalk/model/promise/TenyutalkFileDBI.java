package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import glb.*;

public interface TenyutalkFileDBI extends CreativeObjectDBI {

	/**
	 * （ファイルシステム上の）１フォルダ内の最大ファイル数
	 */
	public static final int unit = 1000;

	/**
	 * ファイルパスの規格定義
	 * @param userName
	 * @param id
	 * @return
	 */
	public static String getFilename(String userName, Long id) {
		long idGroup = id / unit;
		String s = Glb.getConst().getFileSeparator();
		return Glb.getFile().getTenyutalkFileDir() + s + userName + s + idGroup
				+ s + id;
	}

}
