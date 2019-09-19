package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

public abstract class AbstractGame extends Naturality
		implements AbstractGameDBI, LaunchableFromTenyu {

	/**
	 * クライアントファイルの最大数
	 */
	public static final int clientFilesMax = 1000 * 100;

	/**
	 * クライアントソフトウェアのファイルのメタデータ
	 * ファイル名のみが記述される。
	 * 実際のファイルパスはゲームタイトル等がパスの中間に挿入される。
	 */
	protected List<TenyuFile> clientFiles = new ArrayList<>();

	/**
	 * 起動ファイルを指定する。
	 * clientFilesのインデックス。
	 * 初期値は検証を通らない。
	 */
	protected int executableLauncherIndex = -1;

	/**
	 * ファイルが更新されるたびにインクリメントされる
	 */
	protected int fileUpdateCount;

	/**
	 * 環境を検証する実行可能プログラムを指定する。
	 * clientFilesのインデックス。
	 * 初期値は検証を通る。
	 *
	 * 例えばこのゲームが必要とするプログラムのプロセスが発見できるかなどを調べる。
	 * つまり、もしゲームを実行する前に起動しておくべきプログラムが存在する場合、
	 * 検証用プログラムがメインプログラムの前に起動されるので、
	 * ユーザーに警告を出したりインストール手順を指示できる。
	 */
	protected int validationLauncherIndex = -1;

	/**
	 * ゲームタイトルの画像のインデックス
	 */
	protected int titleImageIndex = -1;

	/**
	 * @return	パスの中間文字列
	 */
	public abstract String getDir();

	public boolean addGameClientFile(TenyuFile f) {
		if (f != null) {
			String dir = getDir();
			if (f.getRelativePathStr().contains(dir)) {
				Glb.getLogger().warn("dont contains intermidiate dir=" + dir
						+ " invalid path=" + f, new Exception());
			}
		}

		return clientFiles.add(f);
	}

	/**
	 * @return	ゲームタイトル等によるパス補正を加えて返す。読み取り専用
	 */
	public List<TenyuFile> getClientFiles() {
		List<TenyuFile> r = new ArrayList<>();
		String dir = getDir();
		for (TenyuFile f : clientFiles) {
			r.add(f.cloneAndPrefix(dir));
		}
		return Collections.unmodifiableList(r);
	}

	@Override
	public TenyuFile getExecutableLauncher() {
		TenyuFile r = clientFiles.get(executableLauncherIndex);
		if (r == null)
			return null;
		return r.cloneAndPrefix(getDir());
	}

	public int getExecutableLauncherIndex() {
		return executableLauncherIndex;
	}

	public int getFileUpdateCount() {
		return fileUpdateCount;
	}

	@Override
	public TenyuFile getValidationLauncher() {
		TenyuFile r = clientFiles.get(validationLauncherIndex);
		if (r == null)
			return null;
		return r.cloneAndPrefix(getDir());
	}

	public int getValidationLauncherIndex() {
		return validationLauncherIndex;
	}

	public void incrementFileUpdateCount() {
		fileUpdateCount++;
	}

	public void setClientFiles(List<TenyuFile> clientFiles) {
		if (clientFiles != null) {
			String dir = getDir();
			for (TenyuFile e : clientFiles) {
				if (e.getRelativePathStr().contains(dir)) {
					Glb.getLogger().warn("dont contains intermidiate dir=" + dir
							+ " invalid path=" + e, new Exception());
				}
			}
		}
		this.clientFiles = clientFiles;
	}

	public void setExecutableLauncherIndex(int executableLauncherIndex) {
		this.executableLauncherIndex = executableLauncherIndex;
	}

	public void setFileUpdateCount(int fileUpdateCount) {
		this.fileUpdateCount = fileUpdateCount;
	}

	public void setValidationLauncherIndex(int validationLauncherIndex) {
		this.validationLauncherIndex = validationLauncherIndex;
	}

	protected final boolean validateAtCommonNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (clientFiles == null || clientFiles.size() == 0) {
			r.add(Lang.GAME_CLIENTFILES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (clientFiles.size() > clientFilesMax) {
				r.add(Lang.GAME_CLIENTFILES, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (TenyuFile e : getClientFiles()) {
					if (!e.validateAtCreate(r)) {
						b = false;
						break;
					}
				}
			}
		}

		if (executableLauncherIndex < 0
				|| executableLauncherIndex >= clientFiles.size()) {
			r.add(Lang.GAME_EXECUTABLELAUNCHERINDEX, Lang.ERROR_INVALID,
					"executableLauncherIndex=" + executableLauncherIndex);
			b = false;
		}

		if (validationLauncherIndex != -1
				&& validationLauncherIndex >= clientFiles.size()) {
			r.add(Lang.GAME_VALIDATIONEXECUTABLEINDEX, Lang.ERROR_INVALID,
					"validationLauncherIndex=" + validationLauncherIndex);
			b = false;
		}

		if (titleImageIndex == -1 || titleImageIndex >= clientFiles.size()) {
			r.add(Lang.GAME_TITLEIMAGEINDEX, Lang.ERROR_INVALID,
					"titleImageIndex=" + titleImageIndex);
			b = false;
		}
		return b;
	}

	protected abstract boolean validateAtCreateAbstractGameConcrete(
			ValidationResult r);

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r)) {
			b = false;
		}
		if (fileUpdateCount != 0) {
			r.add(Lang.GAME_FILE_UPDATE_COUNT, Lang.ERROR_INVALID);
			b = false;
		}
		if (!validateAtCreateAbstractGameConcrete(r))
			b = false;
		return b;
	}

	protected abstract boolean validateAtUpdateAbstractGameConcrete(
			ValidationResult r);

	abstract protected boolean validateAtUpdateChangeAbstractGameConcrete(
			ValidationResult r, Object old);

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof AbstractGame)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//AbstractGame old2 = (AbstractGame) old;

		boolean b = true;
		if (!validateAtUpdateChangeAbstractGameConcrete(r, old)) {
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r)) {
			b = false;
		}
		if (fileUpdateCount < 0) {
			r.add(Lang.GAME_FILE_UPDATE_COUNT, Lang.ERROR_TOO_LITTLE);
			b = false;
		}
		if (!validateAtUpdateAbstractGameConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	abstract public boolean validateReferenceAbstractGameConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (!validateReferenceAbstractGameConcrete(r, txn)) {
			b = false;
		} else {
			for (TenyuFile e : getClientFiles()) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((clientFiles == null) ? 0 : clientFiles.hashCode());
		result = prime * result + executableLauncherIndex;
		result = prime * result + fileUpdateCount;
		result = prime * result + titleImageIndex;
		result = prime * result + validationLauncherIndex;
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
		AbstractGame other = (AbstractGame) obj;
		if (clientFiles == null) {
			if (other.clientFiles != null)
				return false;
		} else if (!clientFiles.equals(other.clientFiles))
			return false;
		if (executableLauncherIndex != other.executableLauncherIndex)
			return false;
		if (fileUpdateCount != other.fileUpdateCount)
			return false;
		if (titleImageIndex != other.titleImageIndex)
			return false;
		if (validationLauncherIndex != other.validationLauncherIndex)
			return false;
		return true;
	}

	public int getTitleImageIndex() {
		return titleImageIndex;
	}

	public void setTitleImageIndex(int titleImageIndex) {
		this.titleImageIndex = titleImageIndex;
	}

}
