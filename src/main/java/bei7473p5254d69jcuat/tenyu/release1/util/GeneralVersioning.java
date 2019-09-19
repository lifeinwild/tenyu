package bei7473p5254d69jcuat.tenyu.release1.util;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * modified semantec versioning
 * プログラム以外にも拡張された解釈を持つセマンテックバージョニング
 * さらに、major,minor,patchは1度のバージョンアップで全て上がる可能性がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GeneralVersioning {
	/**
	 * 直前のバージョンの利用者が利用できなくなる変更が加わったら+1
	 * 機能削除、機能修正、意味の変更など。
	 */
	private int major;
	/**
	 * 新たな利用者が加わるような変更が加わったら+1
	 * 機能追加など。
	 */
	private int minor;
	/**
	 * 僅かな変更なら+1
	 * バグ修正、品質向上など。
	 */
	private int patch;

	/**
	 * 同じソースコードから作られたビルドや編集の違いによるバリエーション
	 */
	private String edition = "Standard";

	public static final int editionMax = 50;

	/**
	 * 制作側の自信を示す
	 */
	private ReleaseLevel level;

	/**
	 * win/mac/linuxなど。
	 * nullは全プラットフォーム。jpg,png,bmp,txt等はnullで良い。
	 * Listにしているのは、プログラムは良く複数のプラットフォームに対応しているから。
	 * 全プラットフォーム対応と多数のプラットフォームに対応している事は区別される。
	 * pure javaなら全プラットフォーム対応、
	 * 各環境向けにネイティブdllを同梱しているなら多数のプラットフォームに対応。
	 */
	private List<String> platforms;

	public static final class Platform{
		private List<String> necessary;

	}

	/**
	 * 対応プラットフォームの最大数
	 */
	public static final int platformsMax = 1000;
	/**
	 * プラットフォーム名の最大長
	 */
	public static final int platformNameMax = 100;

	/**
	 * @param r	error messages
	 * @return	is valid
	 */
	public boolean validate(ValidationResult r) {
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
		if (edition == null) {
			r.add(Lang.VERSION_EDITION, Lang.ERROR_EMPTY, "edition=" + edition);
			b = false;
		} else {
			if (edition.length() > editionMax) {
				r.add(Lang.VERSION_EDITION, Lang.ERROR_TOO_LONG,
						"edition.length()=" + edition.length());
				b = false;
			}
		}
		if (level == null) {
			r.add(Lang.VERSION_LEVEL, Lang.ERROR_EMPTY);
			b = false;
		}

		if (platforms == null) {
		} else {
			if (platforms.size() > platformsMax) {
				r.add(Lang.VERSION_PLATFORMS, Lang.ERROR_TOO_MANY,
						"platforms.size()=" + platforms.size());
				b = false;
			} else {
				for (String platform : platforms) {
					if (platform.length() > platformNameMax) {
						r.add(Lang.VERSION_PLATFORM, Lang.ERROR_TOO_LONG,
								"platform.length()=" + platform.length());
						b = false;
					}
				}
			}
		}
		return b;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public void setPatch(int patch) {
		this.patch = patch;
	}

	public static enum ReleaseLevel {
		/**
		 * これに依存して何かを作っても良い
		 */
		STABLE,
		/**
		 * 修正が入る前提でこれに依存して何かを少し作って良い
		 */
		BETA,
		/**
		 * 実験的に使用する
		 */
		ALPHA,
		/**
		 * テスターが使用する
		 */
		TEST,
		/**
		 * 開発者が使用する
		 */
		DEV;
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
		result = prime * result + ((edition == null) ? 0 : edition.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + patch;
		result = prime * result
				+ ((platforms == null) ? 0 : platforms.hashCode());
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
		if (edition == null) {
			if (other.edition != null)
				return false;
		} else if (!edition.equals(other.edition))
			return false;
		if (level != other.level)
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (patch != other.patch)
			return false;
		if (platforms == null) {
			if (other.platforms != null)
				return false;
		} else if (!platforms.equals(other.platforms))
			return false;
		return true;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public List<String> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<String> platforms) {
		this.platforms = platforms;
	}

}
