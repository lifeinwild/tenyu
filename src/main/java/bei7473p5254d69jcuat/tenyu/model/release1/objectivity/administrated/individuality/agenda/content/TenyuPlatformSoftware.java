package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.content;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyuPlatformSoftware implements ValidatableI {
	public static final int filesMax = 2000;

	/**
	 * ファイル一覧
	 */
	private List<TenyutalkFileMetadataI> files = new ArrayList<>();

	/**
	 * リリース番号
	 * バージョン的な数値
	 */
	private int release;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuPlatformSoftware other = (TenyuPlatformSoftware) obj;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (release != other.release)
			return false;
		return true;
	}

	public List<TenyutalkFileMetadataI> getFiles() {
		List<TenyutalkFileMetadataI> r = new ArrayList<>();
		for (TenyutalkFileMetadataI f : files) {
			r.add(f.cloneAndPrefix(Glb.getFile().getPlatformFileDirName()));
		}
		return r;
	}

	public int getRelease() {
		return release;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + release;
		return result;
	}

	public void setFiles(List<TenyutalkFileMetadataI> files) {
		this.files = files;
	}

	public void setRelease(int release) {
		this.release = release;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (release <= 0) {
			r.add(Lang.TENYU_PLATFORM_FILE_RELEASE, Lang.ERROR_INVALID);
			b = false;
		}
		//release1にハッシュ値一覧を与える事が難しい事に気付いたので
		//ソフトウェアは自身を構成する全てのファイルのハッシュ値一覧を自身に含めれない。
		//release2以降のハッシュ値一覧はAgendaVersionupのオブジェクトとしてDB上に存在し、
		//ハッシュ値一覧自体はソフトウェアに含められない。
		if (files == null || (release > 1 && files.size() == 0)) {
			r.add(Lang.TENYU_PLATFORM_FILES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (files.size() > filesMax) {
				r.add(Lang.TENYU_PLATFORM_FILES, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (TenyutalkFileMetadataI e : getFiles()) {
					if (!e.validateAtCreate(r)) {
						b = false;
						break;
					}
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);//同じ
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (files != null) {
			for (TenyutalkFileMetadataI e : getFiles()) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public String toString() {
		return "TenyuPlatformSoftware [files=" + files + ", release=" + release
				+ "]";
	}

}