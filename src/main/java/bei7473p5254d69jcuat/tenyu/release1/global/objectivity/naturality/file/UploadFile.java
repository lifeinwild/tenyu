package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.TenyuFile.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import jetbrains.exodus.env.*;

/**
 * アップロードされるファイル
 * getFile()でTenyuFileが取得できるが、TenyuFileをメンバー変数に持っていない。
 * そうするとnameを重複記録してしまう事になるのでそうしなかった。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class UploadFile extends Naturality
		implements UploadFileDBI, FileMetadataI {
	/**
	 * 1ファイルの最大サイズ
	 */
	public static final long maxSize = 1000L * 1000 * 1000 * 2;

	public static final int nameMax = 150;

	public static boolean validateRelativeFilePathStatic(String name,
			ValidationResult r) {
		return Naturality.validateName(name, r, nameMax);
	}

	protected TenyuFileCore fileCore = new TenyuFileCore();

	public String getDir() {
		return "material";
	}

	public byte[] getFileHash() {
		return fileCore.getFileHash();
	}

	public long getFileSize() {
		return fileCore.getFileSize();
	}

	@Override
	public int getNameMax() {
		return nameMax;
	}

	/**
	 * ファイルパス
	 * アプリインストールフォルダからの相対位置
	 */
	public String getPath() {
		//アップロードしたユーザーの名前をパスに含める
		return getGeneratedDir() + getName();
	}

	/**
	 * @return	DBに記録されずコードで生成される部分パス文字列
	 */
	public String getGeneratedDir() {
		//アップロードしたユーザー
		User uploader = Glb.getObje().getUser(us->us.get(registererUserId));
		if (uploader == null) {
			Glb.getLogger().error("uploader is null", new Exception());
			return null;
		}
		return getBaseDir() + uploader.getName() + "/";
	}

	public void setFile(TenyuFile f) {
		setFileHash(f.getFileHash());
		setFileSize(f.getFileSize());
	}

	public TenyuFile getFile() {
		return fileCore.getFile(getPath());
	}

	abstract public String getBaseDir();

	@Override
	public String getRelativePathStr() {
		return getPath();
	}

	public void setFileHash(byte[] fileHash) {
		fileCore.setFileHash(fileHash);
	}

	public void setFileSize(long fileSize) {
		fileCore.setFileSize(fileSize);
	}

	private final boolean validateAtCommonNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (fileCore == null) {
			r.add(Lang.UPLOADFILE_FILECORE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!fileCore.validateAtCommon(r)) {
				b = false;
			}

			if (getFileSize() == 0) {
				r.add(Lang.UPLOADFILE_FILESIZE, Lang.ERROR_EMPTY);
				b = false;
			} else if (getFileSize() > maxSize) {
				r.add(Lang.UPLOADFILE_FILESIZE, Lang.ERROR_TOO_BIG,
						getFileSize() + " / " + maxSize);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		if (!validateAtCreateUploadFileConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtCreateUploadFileConcrete(
			ValidationResult r);

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof UploadFile)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//UploadFile old2 = (UploadFile) old;

		boolean b = true;
		if (!validateAtUpdateChangeUploadFileConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean validateAtUpdateChangeUploadFileConcrete(
			ValidationResult r, Object old);

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		if (!validateAtUpdateUploadFileConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtUpdateUploadFileConcrete(
			ValidationResult r);

	@Override
	public boolean validateNameSub(ValidationResult r) {
		//全nameがパス扱いされる可能性を想定してパスの検証処理を受ける事になったので
		//ここで特殊な処理を追加する必要が無くなった
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (!validateReferenceUploadFileConcrete(r, txn))
			b = false;
		return b;
	}

	abstract public boolean validateReferenceUploadFileConcrete(
			ValidationResult r, Transaction txn) throws Exception;
}
