package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.io.*;
import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import bei7473p5254d69jcuat.tenyutalk.ui.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyutalkFolder extends CreativeObject implements TenyutalkFolderI {
	/**
	 * このフォルダが参照するファイル一覧
	 * キーはローカル変数名のようなもの
	 * より上にある要素が優先度が高い
	 */
	private List<TenyutalkReferenceBase<
			? extends TenyutalkFile>> files = new ArrayList<>();
	/**
	 * このフォルダが参照するフォルダ一覧
	 * キーはローカル変数名のようなもの
	 * より上にある要素が優先度が高い
	 */
	private List<TenyutalkReferenceBase<
			? extends TenyutalkFolder>> folders = new ArrayList<>();
	/**
	 * 客観系モデルの参照一覧
	 */
	private List<TenyuReferenceSimple<
			? extends Model>> models = new ArrayList<>();

	/**
	 * １フォルダ内の最大要素数。要素は{@link #getSize()}。
	 * これによって十分に要素数が制限されるのでfilesやfoldersはMapでなくListでいい。
	 * アップロード者は１０００以上の要素を扱う場合、適切にフォルダを区切る必要がある。
	 */
	public static final int max = 1000;

	/**
	 * このフォルダを始点として再帰的に参照先を辿り
	 * ローカルのファイルシステムに書き込む。
	 *
	 * @param symbolicLink 同じファイルやフォルダが複数回出現した場合にシンボリックリンクにするか
	 * @param depthMax	探索の深さの最大
	 * @return	DLに成功したか
	 */
	public boolean download(boolean symbolicLink, int depthMax) {
		return false;//TODO
	}

	@Override
	public byte[] getSignTarget() {
		byte[] parent = super.getSignTarget();
		try {
			byte[] filesSeri = Glb.getUtil().toKryoBytes(files,
					Glb.getKryoForTenyutalk());
			byte[] foldersSeri = Glb.getUtil().toKryoBytes(folders,
					Glb.getKryoForTenyutalk());
			return ByteBuffer
					.allocate(parent.length + filesSeri.length
							+ foldersSeri.length)
					.put(parent).put(filesSeri).put(foldersSeri).array();
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * @return	要素数
	 */
	public int getSize() {
		return files.size() + folders.size();
	}

	public TenyutalkReferenceBase<? extends TenyutalkFile> getFile(
			String name) {
		for (TenyutalkReferenceBase<? extends TenyutalkFile> e : files) {
			if (name.equals(e.getName()))
				return e;
		}
		return null;
	}

	public TenyutalkReferenceBase<? extends TenyutalkFolder> getFolder(
			String name) {
		for (TenyutalkReferenceBase<? extends TenyutalkFolder> e : folders) {
			if (name.equals(e.getName()))
				return e;
		}
		return null;
	}

	public TenyutalkReferenceBase<?> get(String name) {
		TenyutalkReferenceBase<? extends TenyutalkFile> r = getFile(name);
		if (r != null)
			return r;
		return getFolder(name);
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		int total = 0;
		if (files == null) {
			r.add(Lang.TENYUTALK_FOLDER_FILES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			total += files.size();
		}

		if (folders == null) {
			r.add(Lang.TENYUTALK_FOLDER_FOLDERS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			total += folders.size();
		}

		if (models == null) {
			r.add(Lang.TENYUTALK_FOLDER_MODELS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			total += models.size();
		}

		if (total > max) {
			r.add(Lang.TENYUTALK_FOLDER, Lang.ERROR_TOO_MANY,
					"elements size=" + total);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateCreativeObjectConcrete(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			if (!Glb.getUtil().validateAtCreate(files, r))
				b = false;
			if (!Glb.getUtil().validateAtCreate(folders, r))
				b = false;
			if (!Glb.getUtil().validateAtCreate(models, r))
				b = false;
		} else {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeCreativeObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!Glb.getUtil().validateAtUpdateChange(files, r,
				e -> getRegistererUserId().equals(e.getUploaderUserId()))) {
			b = false;
		}
		if (!Glb.getUtil().validateAtUpdateChange(folders, r,
				e -> getRegistererUserId().equals(e.getUploaderUserId()))) {
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateCreativeObjectConcrete(ValidationResult r) {
		boolean b = true;
		if (validateCommon(r)) {
			if (!Glb.getUtil().validateAtUpdate(files, r))
				b = false;
			if (!Glb.getUtil().validateAtUpdate(folders, r))
				b = false;
			if (!Glb.getUtil().validateAtUpdate(models, r))
				b = false;
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReferenceCreativeObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (!Glb.getUtil().validateReference(files, r, txn))
			b = false;
		if (!Glb.getUtil().validateReference(folders, r, txn))
			b = false;
		if (!Glb.getUtil().validateReference(models, r, txn))
			b = false;
		return b;
	}

	public TenyutalkFolderStore getStore(Transaction txn) {
		return new TenyutalkFolderStore(txn);
	}

	public TenyutalkFolderGui getGui(String guiName, String cssIdPrefix) {
		return new TenyutalkFolderGui(guiName, cssIdPrefix);
	}

	public static int getMax() {
		return max;
	}

	public List<TenyutalkReferenceBase<? extends TenyutalkFile>> getFiles() {
		return files;
	}

	public void setFiles(
			List<TenyutalkReferenceBase<? extends TenyutalkFile>> files) {
		this.files = files;
	}

	public List<
			TenyutalkReferenceBase<? extends TenyutalkFolder>> getFolders() {
		return folders;
	}

	public void setFolders(
			List<TenyutalkReferenceBase<? extends TenyutalkFolder>> folders) {
		this.folders = folders;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + ((folders == null) ? 0 : folders.hashCode());
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyutalkFolder other = (TenyutalkFolder) obj;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (folders == null) {
			if (other.folders != null)
				return false;
		} else if (!folders.equals(other.folders))
			return false;
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!models.equals(other.models))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TenyutalkFolder [files=" + files + ", folders=" + folders
				+ ", models=" + models + "]";
	}

	public List<TenyuReferenceSimple<? extends Model>> getModels() {
		return models;
	}

	public void setModels(
			List<TenyuReferenceSimple<? extends Model>> models) {
		this.models = models;
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameTenyutalk.TENYUTALK_FOLDER;
	}

}
