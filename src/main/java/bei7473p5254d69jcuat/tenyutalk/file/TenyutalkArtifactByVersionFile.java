package bei7473p5254d69jcuat.tenyutalk.file;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link TenyutalkFileMetadataI}の実装
 *
 * Tenyutalk系で扱うファイル。
 * 現状Tenyuの設計では動的に送受信するファイルはこれのみ。
 * {@link TenyuArtifactI}成果物としてのファイルアップロードのみということ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyutalkArtifactByVersionFile
		implements TenyutalkFileMetadataI, ValidatableI {
	public static final int relativePathMax = 300;

	private FileSizeAndHash core = new FileSizeAndHash();
	private String dirAndFilename;
	private Long tenyuArtifactByVersionId;

	public TenyutalkArtifactByVersionFile() {
	}

	public TenyutalkArtifactByVersionFile(Long tenyuArtifactByVersionId,
			String dirAndFilename, byte[] hash, long size) {
		setTenyuArtifactByVersionId(tenyuArtifactByVersionId);
		setDirAndFilename(dirAndFilename);
		setFileHash(hash);
		setFileSize(size);
	}

	@Override
	public TenyutalkArtifactByVersionFile clone() {
		try {
			TenyutalkArtifactByVersionFile r = new TenyutalkArtifactByVersionFile();
			r.setDirAndFilename(dirAndFilename);
			r.setFileHash(getFileHash());
			r.setFileSize(getFileSize());
			r.setTenyuArtifactByVersionId(getTenyuArtifactByVersionId());
			return r;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyutalkArtifactByVersionFile other = (TenyutalkArtifactByVersionFile) obj;
		if (core == null) {
			if (other.core != null)
				return false;
		} else if (!core.equals(other.core))
			return false;
		if (dirAndFilename == null) {
			if (other.dirAndFilename != null)
				return false;
		} else if (!dirAndFilename.equals(other.dirAndFilename))
			return false;
		return true;
	}

	@Override
	public String getBaseDir() {
		return Glb.getFile().getTenyutalkRepositoryReleaseArtifactDir(
				getTenyuArtifact()) + getDirAndFilename();
	}

	public String getDirAndFilename() {
		return dirAndFilename;
	}

	public byte[] getFileHash() {
		return core.getFileHash();
	}

	public long getFileSize() {
		return core.getFileSize();
	}

	public TenyuArtifactI getTenyuArtifact() {
		return Glb.getObje()
				.getTenyuArtifact(tas -> tas.get(tenyuArtifactByVersionId));
	}

	public Long getTenyuArtifactByVersionId() {
		return tenyuArtifactByVersionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((core == null) ? 0 : core.hashCode());
		result = prime * result
				+ ((dirAndFilename == null) ? 0 : dirAndFilename.hashCode());
		return result;
	}

	public void setDirAndFilename(String dirAndFilename) {
		this.dirAndFilename = dirAndFilename;
	}

	public void setFileHash(byte[] fileHash) {
		core.setFileHash(fileHash);
	}

	public void setFileSize(long fileSize) {
		core.setFileSize(fileSize);
	}

	public void setTenyuArtifactByVersionId(Long tenyuArtifactByVersionId) {
		this.tenyuArtifactByVersionId = tenyuArtifactByVersionId;
	}

	@Override
	public String toString() {
		return "TenyuFile [core=" + core + ", dirAndFilename=" + dirAndFilename
				+ "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true && core.validateAtDelete(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	private final boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (dirAndFilename == null) {
			r.add(Lang.TENYUTALK_ARTIFACT_FILE_PATH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (dirAndFilename.length() > relativePathMax) {
				r.add(Lang.TENYUTALK_ARTIFACT_FILE_PATH, Lang.ERROR_TOO_LONG,
						"path.size=" + dirAndFilename.length());
				b = false;
			}
		}
		if (!core.validateAtCommon(r)) {
			b = false;
		}

		if (tenyuArtifactByVersionId == null) {
			r.add(Lang.TENYUTALK_ARTIFACT_FILE,
					Lang.TENYU_ARTIFACT_BY_VERSION_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(tenyuArtifactByVersionId)) {
				r.add(Lang.TENYUTALK_ARTIFACT_FILE,
						Lang.TENYU_ARTIFACT_BY_VERSION_ID, Lang.ERROR_INVALID,
						"artifactByVersionId=" + tenyuArtifactByVersionId);
				b = false;
			}
		}

		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;

		TenyuArtifactStore tas = new TenyuArtifactStore(txn);
		if (tas.get(tenyuArtifactByVersionId) == null) {
			r.add(Lang.TENYUTALK_ARTIFACT_FILE, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"artifactId=" + tenyuArtifactByVersionId);
			b = false;
		}

		if (!core.validateReference(r, txn))
			b = false;
		return b;
	}

	/**
	 * ファイルを移動した時、サイズとハッシュ値は変わらないのに対して
	 * ファイルパスは変化するので、別クラスとした。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class FileSizeAndHash implements ValidatableI {
		/**
		 * 1ファイルの最大サイズ
		 */
		public static final long maxSize = 1000L * 1000 * 1000 * 2;
		private byte[] fileHash;

		private long fileSize;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileSizeAndHash other = (FileSizeAndHash) obj;
			if (!Arrays.equals(fileHash, other.fileHash))
				return false;
			if (fileSize != other.fileSize)
				return false;
			return true;
		}

		public TenyutalkArtifactByVersionFile getFile(String dirAndFilename,
				Long artifactId) {
			TenyutalkArtifactByVersionFile r = new TenyutalkArtifactByVersionFile();
			r.setDirAndFilename(dirAndFilename);
			r.setFileHash(fileHash);
			r.setFileSize(fileSize);
			r.setTenyuArtifactByVersionId(artifactId);
			return r;
		}

		public byte[] getFileHash() {
			return fileHash;
		}

		public long getFileSize() {
			return fileSize;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(fileHash);
			result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
			return result;
		}

		public void setFileHash(byte[] fileHash) {
			this.fileHash = fileHash;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		@Override
		public String toString() {
			return "TenyuFileMetadata [fileHash=" + Arrays.toString(fileHash)
					+ ", fileSize=" + fileSize + "]";
		}

		private boolean validateAtCommon(ValidationResult r) {
			boolean b = true;
			if (fileHash == null) {
				r.add(Lang.TENYUTALK_ARTIFACT_FILE_HASH, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (fileHash.length != Glb.getConst().getHashSize()) {
					r.add(Lang.TENYUTALK_ARTIFACT_FILE_HASH,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
			if (fileSize < 0) {
				r.add(Lang.TENYUTALK_ARTIFACT_FILE_SIZE, Lang.ERROR_TOO_LITTLE);
				b = false;
			} else {
				if (fileSize > maxSize) {
					r.add(Lang.TENYUTALK_ARTIFACT_FILE_SIZE,
							Lang.ERROR_TOO_BIG);
					b = false;
				}
			}
			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			return validateAtCommon(r);
		}

		@Override
		public boolean validateAtDelete(ValidationResult r) {
			return true;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			return validateAtCommon(r);
		}

		@Override
		public boolean validateReference(ValidationResult r, Transaction txn)
				throws Exception {
			return true;
		}
	}

}