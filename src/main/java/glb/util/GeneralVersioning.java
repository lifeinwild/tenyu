package glb.util;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * プログラム以外にも拡張された解釈を持つセマンテックバージョニング
 *
 * ほぼこれ
 * https://semver.org/lang/ja/
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GeneralVersioning
		implements Storable, Comparable<GeneralVersioning> {
	/**
	 * 直前のバージョンの参照元に変化が想定されるなら+1
	 * 既存のAPIの削除や修正など。
	 */
	private long major;

	/**
	 * 新たな機能や意味が追加されたが直前のバージョンの参照元に変化が想定されないなら+1
	 * API追加など。
	 */
	private long minor;

	/**
	 * 直前のバージョンの参照元に変化が想定されないなら+1
	 * バグ修正、品質向上など。
	 */
	private long patch;

	/**
	 * 現在の成熟度
	 */
	private ReleaseLevel level;

	/**
	 * @param r	error messages
	 * @return	is valid
	 */
	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (major < 0) {
			r.add(Lang.VERSION_MAJOR, Lang.ERROR_TOO_LITTLE, "major=" + major);
			b = false;
		}
		if (minor < 0) {
			r.add(Lang.VERSION_MINOR, Lang.ERROR_TOO_LITTLE, "minor=" + minor);
			b = false;
		}
		if (patch < 0) {
			r.add(Lang.VERSION_PATCH, Lang.ERROR_TOO_LITTLE, "patch=" + patch);
			b = false;
		}
		if (level == null) {
			r.add(Lang.VERSION_LEVEL, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	public long incrementMajor() {
		return major++;
	}

	public long incrementMinor() {
		return minor++;
	}

	public long incrementPatch() {
		return patch++;
	}

	public long getMajor() {
		return major;
	}

	public long getMinor() {
		return minor;
	}

	public long getPatch() {
		return patch;
	}

	/**
	 * ソフトウェアの成熟度
	 */
	public static enum ReleaseLevel {
		/**
		 * これに依存して何かを作っても良い
		 */
		STABLE,
		/**
		 * 内容的にSTABLE相当と思われるが動作実績が少なすぎる段階
		 */
		BETA,
		/**
		 * 一般ユーザーがテストないし先行体験として使用する
		 * 品質向上
		 */
		ALPHA,
		/**
		 * テスターが使用する
		 * デバッグ
		 */
		TEST,
		/**
		 * 開発者が使用する
		 * 一通りの基本設計、実装
		 */
		DEV;
		public static ReleaseLevel getRandom() {
			return ReleaseLevel.values()[Glb.getRnd()
					.nextInt(ReleaseLevel.values().length - 1)];
		}
	}


	public ReleaseLevel getLevel() {
		return level;
	}

	public void setLevel(ReleaseLevel level) {
		this.level = level;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + (int) (major ^ (major >>> 32));
		result = prime * result + (int) (minor ^ (minor >>> 32));
		result = prime * result + (int) (patch ^ (patch >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneralVersioning other = (GeneralVersioning) obj;
		if (level != other.level)
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (patch != other.patch)
			return false;
		return true;
	}

	@Override
	public String toString() {
		//この文字列フォーマットはファイルパスに使用されるので変更してはならない
		return major + delimiter + minor + delimiter + patch + delimiter
				+ level;
	}

	public static final String delimiter = "_";

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		return b;
	}

	public void setMajor(long major) {
		this.major = major;
	}

	public void setMinor(long minor) {
		this.minor = minor;
	}

	public void setPatch(long patch) {
		this.patch = patch;
	}

	@Override
	public int compareTo(GeneralVersioning o) {
		if (major < o.getMajor()) {
			return -1;
		} else if (major > o.getMajor()) {
			return 1;
		}
		//major等しい場合
		if (minor < o.getMinor()) {
			return -1;
		} else if (minor > o.getMinor()) {
			return 1;
		}
		//minor等しい場合
		if (patch < o.getPatch()) {
			return -1;
		} else if (patch > o.getPatch()) {
			return 1;
		}
		//patchまですべて等しい場合
		return 0;
	}

}
