package bei7473p5254d69jcuat.tenyutalk.file;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.netty.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import glb.util.*;
import glb.util.Bits;

/**
 * ファイルメタデータ及びtenyuフォルダからファイルへの相対パス
 * {@link Downloader}に対応するためのインターフェースを備える
 *
 * {@link FileMetadataI}は一般的なファイルメタデータだが、そこにwriteBits等のインターフェスを加えて
 * Tenyu基盤ソフトウェア的なファイルメタデータにしたもの。
 *
 * 前提とする状態がファイルシステム上にあるせいでdefaultメソッドで実装できるものが多い。
 *
 * どのようにファイルパスを作るかが実装毎に異なる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkFileMetadataI extends FileMetadataI, ValidatableI {
	/**
	 * @return	署名対象。ファイルのハッシュ値
	 */
	default byte[] getSignTargetHash() {
		return getFileHash();
	}

	default byte[] getSignTargetHashFromFile() {
		return getFileHashFromFile();
	}

	/**
	 * 呼び出し毎にファイルを読み込んでハッシュ値を作成する
	 * @return
	 */
	default byte[] getFileHashFromFile() {
		return Glb.getUtil().hashFile(getRelativePath());
	}

	/**
	 * writeBitsとファイルを削除する。
	 * @return	いずれかが削除されたか
	 */
	default boolean delete() {
		boolean f = Glb.getFile().remove(getRelativePath());
		boolean wri = deleteWriteBits();
		return f || wri;
	}

	TenyutalkFileMetadataI clone();

	default TenyutalkFileMetadataI cloneAndPrefix(String prefix) {
		TenyutalkFileMetadataI r = clone();
		r.setDirAndFilename(prefix + r.getDirAndFilename());
		return r;
	}

	void setDirAndFilename(String dirAndFilename);

	/**
	 * これに{@link #getDirAndFilename()}を足せばカレントからの相対パスになる
	 *
	 * これの実装方法が具象クラス毎に分かれることを想定していて、
	 * このインターフェースを作成した事はほぼそのためといっていい。
	 *
	 * @return	基準フォルダ
	 */
	String getBaseDir();

	/**
	 * 任意の基準フォルダ{@link #getBaseDir()}からの相対パス。
	 * どのフォルダを基準とするかはこのクラスの利用側が決める。
	 * <基準フォルダ>dir/filename.txt など。
	 * フォルダ名は必須ではない。<基準フォルダ>filename.txtが可能。
	 * /から始められない。<基準フォルダ>/filename.txtは不可能。
	 *
	 * {@link #getRelativePathStr()}と区別するためにこの名前にした。
	 */
	String getDirAndFilename();

	@Override
	default String getRelativePathStr() {
		String p = getBaseDir();
		if (!Glb.getFile().isAppPathRelative(p) || !Glb.getUtil()
				.validatePath(p, ChunkedDataConcatFile.fileNameLenMax)) {
			return null;
		}
		return p;
	}

	default boolean deleteWriteBits() {
		return Glb.getFile().remove(getWriteBitsPath());
	}

	default boolean exists() {
		return getRelativePath().toFile().exists();
	}

	/**
	 * @return	新たに作成されたWriteBits
	 */
	default WriteBits getNewWriteBits() {
		WriteBits r = new WriteBits();
		r.setHash(getFileHash());
		Bits b = new Bits();
		int bitSize = getWriteBitsCount();
		b.set(Bits.sizeToIndex(bitSize));
		r.setBits(b);
		return r;
	}

	default WriteBits getWriteBits() {
		byte[] raw = getWriteBitsRaw();
		return WriteBits.deserializeStatic(raw);
	}

	/**
	 * ファイルシステムからwriteBitsを読み込む。
	 * 未作成か古くなっていたら再作成し、ファイルも上書きする。
	 * @return	このファイルの現在のwriteBits
	 */
	default WriteBits getWriteBitsAndInit() {
		WriteBits r = null;
		boolean valid = true;
		try {
			WriteBits loaded = loadWriteBits();
			if (loaded == null || !validateWriteBits(loaded)) {
				valid = false;
			}
			r = loaded;
		} catch (Exception e) {
			valid = false;
		}
		if (!valid) {
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
	default int getWriteBitsCount() {
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
	default Path getWriteBitsPath() {
		String p = getWriteBitsPathStr();
		if (p == null)
			return null;
		return Paths.get(p);
	}

	default String getWriteBitsPathStr() {
		String p = getBaseDir() + Glb.getFile().getWriteBitsSuffix();
		if (!Glb.getFile().isAppPathRelative(p) || !Glb.getUtil()
				.validatePath(p, ChunkedDataConcatFile.fileNameLenMax)) {
			return null;
		}
		return p;
	}

	default byte[] getWriteBitsRaw() {
		try {
			return Files.readAllBytes(getWriteBitsPath());
		} catch (Exception e) {
			return null;
		}
	}

	default void initDirs() {
		File f = Glb.getFile().get(getRelativePath());
		f.getParentFile().mkdirs();
	}

	/**
	 * 空のファイルを作成する。サイズはfileSize
	 * @return	作成されたか
	 */
	default boolean initFile() {
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
	default boolean isWritten(long position, long size) {
		WriteBits wb = getWriteBits();
		return wb.isWritten(position, size);
	}

	/**
	 * @return	ファイルシステムから読み込まれたwriteBits
	 */
	default WriteBits loadWriteBits() {
		WriteBits r = new WriteBits();
		byte[] serialized = getWriteBitsRaw();
		if (serialized == null)
			return null;
		r.deserialize(serialized);
		return r;
	}

	/**
	 * @return	ローカルのファイルシステムにあるファイルがこのファイル情報と正しいか
	 */
	default boolean validateFile() {
		Path p = getRelativePath();
		if (!p.toFile().exists()) {
			return false;
		}
		if (!Arrays.equals(getFileHash(), Glb.getUtil().hashFile(p))) {
			return false;
		}
		if (getFileSize() != p.toFile().length()) {
			return false;
		}

		return true;
	}

	/**
	 * {@link TenyutalkFileMetadataI}が更新されるとwriteBitsが古くなっている可能性があるので、検証する。
	 * @return	writeBitsは最新か
	 */
	default boolean validateWriteBits(WriteBits wb) {
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
	default boolean writeWriteBits(WriteBits writeBits) {
		Path p = getWriteBitsPath();
		return Glb.getFile().create(p, writeBits.serialize(), true);
	}
}
