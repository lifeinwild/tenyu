package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.netty.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Bits;
import jetbrains.exodus.env.*;

/**
 * ファイル。java.io.Fileと区別するためこのクラス名にした。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyuFile implements FileMetadataI, Storable {
	public static final int relativePathMax = 300;

	private TenyuFileCore core = new TenyuFileCore();
	/**
	 * ファイル名またはその前方にフォルダ名がついた文字列
	 * dir/filename.txt など。
	 * フォルダ名は必須ではない。
	 */
	private String dirAndFilename;

	public TenyuFile cloneAndPrefix(String prefix) {
		TenyuFile r = clone();
		r.setDirAndFilename(prefix + r.getDirAndFilename());
		return r;
	}

	@Override
	public TenyuFile clone() {
		try {
			TenyuFile r = new TenyuFile();
			r.setDirAndFilename(dirAndFilename);
			r.setFileHash(getFileHash());
			r.setFileSize(getFileSize());
			return r;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * writeBitsとファイルを削除する。
	 * @return	いずれかが削除されたか
	 */
	public boolean delete() {
		boolean f = Glb.getFile().remove(getRelativePath());
		boolean wri = deleteWriteBits();
		return f || wri;
	}

	public boolean deleteWriteBits() {
		return Glb.getFile().remove(getWriteBitsPath());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TenyuFile other = (TenyuFile) obj;
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

	public boolean exists() {
		return getRelativePath().toFile().exists();
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

	/**
	 * @return	新たに作成されたWriteBits
	 */
	public WriteBits getNewWriteBits() {
		WriteBits r = new WriteBits();
		r.setHash(getFileHash());
		Bits b = new Bits();
		int bitSize = getWriteBitsCount();
		b.set(Bits.sizeToIndex(bitSize));
		r.setBits(b);
		return r;
	}

	private String getPathStrInternal() {
		return Glb.getFile().getFileReceivingPath(getDirAndFilename());
	}

	public Path getRelativePath() {
		String p = getRelativePathStr();
		if (p == null)
			return null;
		return Paths.get(p);
	}

	@Override
	public String getRelativePathStr() {
		String p = getPathStrInternal();
		if (!Glb.getFile().isAppPathRelative(p) || !Glb.getUtil()
				.validatePath(p, ChunkedDataConcatFile.fileNameLenMax)) {
			return null;
		}
		return p;
	}

	public WriteBits getWriteBits() {
		byte[] raw = getWriteBitsRaw();
		return WriteBits.deserializeStatic(raw);
	}

	/**
	 * ファイルシステムからwriteBitsを読み込む。
	 * 未作成か古くなっていたら再作成し、ファイルも上書きする。
	 * @return	このファイルの現在のwriteBits
	 */
	public WriteBits getWriteBitsAndInit() {
		WriteBits r = null;
		boolean b = true;
		try {
			WriteBits loaded = loadWriteBits();
			if (loaded == null || !validateWriteBits(loaded)) {
				b = false;
			}
			r = loaded;
		} catch (Exception e) {
			b = false;
		}
		if (!b) {
			delete();
			//ファイルも再作成
			initFile();
			//writeBits作成
			r = getNewWriteBits();
			writeWriteBits(r);
		}
		return r;
	}

	/**
	 * @return	writeBitsのビット数
	 */
	public int getWriteBitsCount() {
		int bitCount = (int) (getFileSize() / WriteBits.unit);
		if (getFileSize() % WriteBits.unit > 0)
			bitCount++;
		return bitCount;
	}

	/**
	 * writeBitsは分散DLの過程において作成され、DL状況を示す。
	 *
	 * @return	このファイルのwriteBitsのパス
	 */
	public Path getWriteBitsPath() {
		String p = getWriteBitsPathStr();
		if (p == null)
			return null;
		return Paths.get(p);
	}

	public String getWriteBitsPathStr() {
		String p = getPathStrInternal() + Glb.getFile().getWriteBitsSuffix();
		if (!Glb.getFile().isAppPathRelative(p) || !Glb.getUtil()
				.validatePath(p, ChunkedDataConcatFile.fileNameLenMax)) {
			return null;
		}
		return p;
	}

	public byte[] getWriteBitsRaw() {
		try {
			return Files.readAllBytes(getWriteBitsPath());
		} catch (Exception e) {
			return null;
		}
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

	public void initDirs() {
		File f = Glb.getFile().get(getRelativePath());
		f.getParentFile().mkdirs();
	}

	/**
	 * 空のファイルを作成する。サイズはfileSize
	 * @return	作成されたか
	 */
	public boolean initFile() {
		try {
			initDirs();
			File f = Glb.getFile().get(getRelativePath());
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.setLength(getFileSize());
			raf.close();
		} catch (Exception e) {
			Glb.getLogger().error(
					"Failed to init file path=" + getRelativePathStr(), e);
			return false;
		}
		return true;
	}

	/**
	 * @param position
	 * @param size
	 * @return	その部分が書き込み済みか
	 */
	public boolean isWritten(long position, long size) {
		WriteBits wb = getWriteBits();
		return wb.isWritten(position, size);
	}

	/**
	 * @return	ファイルシステムから読み込まれたwriteBits
	 */
	public WriteBits loadWriteBits() {
		WriteBits r = new WriteBits();
		byte[] serialized = getWriteBitsRaw();
		if (serialized == null)
			return null;
		r.deserialize(serialized);
		return r;
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

	@Override
	public String toString() {
		return getRelativePathStr() + " " + core;
	}

	public final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (dirAndFilename == null) {
			r.add(Lang.TENYU_FILE_PATH, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (dirAndFilename.length() > relativePathMax) {
				r.add(Lang.TENYU_FILE_PATH, Lang.ERROR_TOO_LONG,
						"path.size=" + dirAndFilename.length());
				b = false;
			}
		}
		if (!core.validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true && core.validateAtDelete(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	/**
	 * @return	ローカルのファイルシステムにあるファイルがこのファイル情報と正しいか
	 */
	public boolean validateFile() {
		Path p = getRelativePath();
		if (!p.toFile().exists()) {
			return false;
		}
		if (!Arrays.equals(getFileHash(), Glb.getUtil().digestFile(p))) {
			return false;
		}
		if (getFileSize() != p.toFile().length()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true && core.validateReference(r, txn);
	}

	/**
	 * TenyuFileが更新されるとwriteBitsが古くなっている可能性があるので、検証する。
	 * @return	writeBitsは最新か
	 */
	public boolean validateWriteBits(WriteBits wb) {
		if (wb == null || wb.getBits() == null || wb.getHash() == null) {
			return false;
		}
		if (!Arrays.equals(getFileHash(), wb.getHash())) {
			return false;
		}
		if (getWriteBitsCount() != wb.getBits().size()) {
			return false;
		}
		return true;
	}

	/**
	 * writeBitsをファイルに書き込む
	 * @param writeBits
	 * @return
	 */
	public boolean writeWriteBits(WriteBits writeBits) {
		Path p = getWriteBitsPath();
		return Glb.getFile().create(p, writeBits.serialize(), true);
	}

	/**
	 * ファイルパスは用途によって作り方が違ったり、
	 * 別のフィールド値そのものだったりして、
	 * TenyuFileをそのままメンバー変数に置くとDBに値が重複記録されたり
	 * するので、このクラスを作成した。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class TenyuFileCore implements Storable {
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
			TenyuFileCore other = (TenyuFileCore) obj;
			if (!Arrays.equals(fileHash, other.fileHash))
				return false;
			if (fileSize != other.fileSize)
				return false;
			return true;
		}

		public TenyuFile getFile(String dirAndFilename) {
			TenyuFile r = new TenyuFile();
			r.setDirAndFilename(dirAndFilename);
			r.setFileHash(fileHash);
			r.setFileSize(fileSize);
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
			return "size=" + fileSize;
		}

		public boolean validateAtCommon(ValidationResult r) {
			boolean b = true;
			if (fileHash == null) {
				r.add(Lang.TENYU_FILE_HASH, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (fileHash.length != Glb.getConst().getHashSize()) {
					r.add(Lang.TENYU_FILE_HASH, Lang.ERROR_INVALID);
					b = false;
				}
			}
			if (fileSize < 0) {
				r.add(Lang.TENYU_FILE_SIZE, Lang.ERROR_TOO_LITTLE);
				b = false;
			} else {
				if (fileSize > UploadFile.maxSize) {
					r.add(Lang.TENYU_FILE_SIZE, Lang.ERROR_TOO_BIG);
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

	/**
	 * 分散DLの状況を示すデータ。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class WriteBits {
		/**
		 * 250KBが1bitになる。
		 */
		public static final long unit = 1024 * 250;

		public static WriteBits deserializeStatic(byte[] serialized) {
			WriteBits r = new WriteBits();
			r.deserialize(serialized);
			return r;
		}

		private Bits bits;

		private byte[] hash;

		public WriteBits clone() throws CloneNotSupportedException {
			WriteBits r = new WriteBits();
			r.setBits(bits == null ? null : bits.clone());
			r.setHash(hash == null ? null : hash.clone());
			return r;
		}

		public void deserialize(byte[] serialized) {
			ByteBuffer buf = ByteBuffer.wrap(serialized);
			int lastBitIndex = buf.getInt();
			int bitSize = Bits.size(lastBitIndex);
			int byteSize = Bits.bitSizeToByteSize(bitSize);
			int hashSize = Glb.getConst().getHashSize();
			hash = new byte[hashSize];
			buf.get(hash, 0, hashSize);
			byte[] bitsRaw = new byte[byteSize];
			buf.get(bitsRaw, 0, byteSize);
			Bits bits = new Bits();
			bits.set(lastBitIndex, bitsRaw);
			this.bits = bits;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WriteBits other = (WriteBits) obj;
			if (bits == null) {
				if (other.bits != null)
					return false;
			} else if (!bits.equals(other.bits))
				return false;
			if (!Arrays.equals(hash, other.hash))
				return false;
			return true;
		}

		public Bits getBits() {
			return bits;
		}

		public byte[] getHash() {
			return hash;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bits == null) ? 0 : bits.hashCode());
			result = prime * result + Arrays.hashCode(hash);
			return result;
		}

		public boolean isWritten(long position, long size) {
			if (position < 0 || size == 0) {
				Glb.getLogger().warn(
						"Invalid size=" + size + " position=" + position,
						new IllegalArgumentException());
				return false;
			}
			int startIndex = (int) (position / unit);
			int indexCount = (int) (size / unit);
			if (size % unit != 0L) {
				indexCount++;
			}

			for (int currentIndex = startIndex; currentIndex < startIndex
					+ indexCount; currentIndex++) {
				if (!bits.isStand(currentIndex)) {
					return false;
				}
			}
			return true;
		}

		public byte[] serialize() {
			ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES
					+ Glb.getConst().getHashSize() + bits.getBinary().length);
			buf.putInt(bits.getLastBitIndex());
			buf.put(hash);
			buf.put(bits.getBinary());
			return buf.array();
		}

		public void setBits(Bits bits) {
			this.bits = bits;
		}

		public void setHash(byte[] hash) {
			this.hash = hash;
		}
	}

}