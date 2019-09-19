package bei7473p5254d69jcuat.tenyu.release1.global;

import java.io.*;
import java.nio.file.*;

import org.apache.commons.io.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement.*;
import javafx.scene.image.*;

/**
 * このクラスを継承してオーバーライドしてGlbに設定すれば
 * ファイルシステムへの依存部分が変更可能になる。
 * ファイルの読み込み、書き込み処理も出来るだけここで扱う。
 *
 * このクラスを置いた意図は、システムはメイン領域と外界の間に
 * 通信とファイルシステムがあるというような理解からで、
 * ファイル系は全てここで扱うのが望ましい。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FileManagement {
	public void open() {
		dirSetup();
	}

	/**
	 * ファイル削除
	 */
	private boolean removeFile(Path file) {
		try {
			Files.delete(file);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * ファイル削除や移動などでアプリ外のファイルに影響を与えないようにする防止策
	 * @param p	相対又は絶対パス
	 * @return	パスpがアプリ名を含んでいるか
	 */
	public boolean isAppPathBoth(Path p) {
		return isAppPath(p.toAbsolutePath().toString());
	}

	public boolean isAppPathRelative(String relativePath) {
		return isAppPathBoth(Paths.get(relativePath).toAbsolutePath());
	}

	/**
	 * @return	基盤ソフトウェアの次期バージョンのファイルが置かれるフォルダ
	 */
	public String getPlatformFileDir() {
		return getDynamicFileDir() + getPlatformFileDirSingle();
	}

	public String getPlatformFileDirSingle() {
		return "platform/";
	}

	/**
	 * @param path	相対又は絶対パス
	 * @return	アプリケーションのパスか
	 */
	public boolean isAppPathBoth(String path) {
		Path p = null;
		try {
			p = Paths.get(path);
		} catch (Exception e) {
			Glb.getLogger().error("No such file", new IOException());
		}

		if (!isAppPathBoth(p)) {
			return false;
		}
		return true;
	}

	public Path move(Path p1, Path p2) {
		if (p1 == null || p2 == null)
			return null;
		if (!isAppPathBoth(p1) || !isAppPathBoth(p2)) {
			return null;
		}
		try {
			return Files.move(p1, p2);
		} catch (IOException e) {
			Glb.getLogger().warn("Failed to move file. p1=" + p1 + " p2=" + p2,
					e);
			return null;
		}
	}

	/**
	 * @param path	操作したいファイルシステム上のパス
	 * @return	検証済みパス。検証に失敗したらnull
	 */
	public File get(String path) {
		if (!isAppPathBoth(path))
			return null;
		return new File(path);
	}

	public Path getPath(String path) {
		if (!isAppPathBoth(path))
			return null;
		return Paths.get(path);
	}

	public File get(Path path) {
		if (!isAppPathBoth(path))
			return null;
		return path.toFile();
	}

	public boolean isAppPath(String absolutePath) {
		int index = absolutePath.toUpperCase()
				.indexOf(Glb.getConst().getAppName().toUpperCase());
		if (index != -1) {
			return true;
		} else {
			Glb.getLogger().error("",
					new Exception("not app dir. path should contains \""
							+ Glb.getConst().getAppName() + "\" absolutePath="
							+ absolutePath));
			return false;
		}
	}

	/**
	 * ディレクトリを丸ごと削除する
	 * @param dir	削除対象ディレクトリ
	 * @return		例外が発生し無かったらtrue
	 */
	private boolean removeDir(Path dir) {
		try {
			FileUtils.deleteDirectory(dir.toFile());
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * @param p	作成されるパス
	 * @return	ファイルが作成されたか
	 */
	public boolean create(Path p, byte[] content, boolean overwrite) {
		if (p == null || !isAppPathBoth(p))
			return false;

		File f = p.toFile();
		if (f.exists()) {
			if (overwrite) {
				if (!removeInternal(p)) {
					return false;
				}
			} else {
				return false;
			}
		}

		File pare = f.getParentFile();
		if (pare != null)
			pare.mkdirs();

		if (content == null) {
			try {
				return f.createNewFile();
			} catch (IOException e) {
				Glb.getLogger().error("", e);
				return false;
			}
		}

		try (FileOutputStream fos = new FileOutputStream(f)) {
			fos.write(content);
			fos.flush();
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("Failed to write path=" + p, e);
			return false;
		}

	}

	/**
	 * ファイルまたはディレクトリを削除する
	 * 無関係のファイルを削除しないチェック処理を入れてあるので
	 * 基本的にこのメソッドでファイルを削除すべき
	 *
	 * @param p
	 * @return
	 */
	public boolean remove(Path p) {
		if (p == null || !isAppPathBoth(p))
			return false;

		return removeInternal(p);
	}

	private boolean removeInternal(Path p) {
		File f = p.toFile();
		if (f.isDirectory()) {
			return removeDir(p);
		} else if (f.isFile()) {
			return removeFile(p);
		}
		return false;
	}

	private void dirSetup() {
		File updateDirF = get(getUpdateDir());
		updateDirF.mkdir();
		File modelDirF = get(getModelDir());
		modelDirF.mkdir();
		File objeDirF = get(getObjectivityDir());
		objeDirF.mkdir();
		File subjeDirF = get(getSubjectivityDir());
		subjeDirF.mkdir();
		File keyDirF = get(getKeyDir());
		keyDirF.mkdir();
	}

	public String getPublicKeyPath(KeyType type, String prefix) {
		return getKeyFilePrefix(prefix, type.name()) + getPublicKeyFile()
				+ getKeySuffix();
	}

	private String getKeyFilePrefix(String prefix, String name) {
		if (prefix == null || prefix.length() == 0) {
			return getKeyDir() + "/" + name;
		} else {
			return getKeyDir() + "/" + prefix + "_" + name;
		}
	}

	public String getPrivateKeyPath(KeyType type, String prefix) {
		return getKeyFilePrefix(prefix, type.name()) + getPrivateKeyFile()
				+ getKeySuffix();
	}

	public String getPublicKeyPath(KeyType type) {
		return getPublicKeyPath(type, "");
	}

	public String getPrivateKeyPath(KeyType type) {
		return getPrivateKeyPath(type, "");
	}

	public String getSignKeyPath(KeyType targetKey, KeyType by, String prefix) {
		return getKeyFilePrefix(prefix, targetKey.name() + "By" + by.name())
				+ "_sign.txt";
	}

	public String getSignKeyPath(KeyType targetKey, KeyType by) {
		return getSignKeyPath(targetKey, by, "");
	}

	public String getIconDir() {
		return "images/";
	}

	public String getIconPath() {
		return getIconDir() + Glb.getConst().getAppName() + ".png";
	}

	public Image getIcon() {
		return new Image(
				Glb.getUtil().getLoader().getResourceAsStream(getIconPath()));
	}

	public String getConfFile() {
		return "tenyu.conf";
	}

	public String getKeyDir() {
		return "key";
	}

	public String getUploadFileDir() {
		return getDynamicFileDir() + getUploadFileDirSingle();
	}

	public String getUploadFileDirSingle() {
		return "upload/";
	}

	public String getKeyGenerated() {
		return getKeyDir() + "/generated";
	}

	public String getFxmlPath() {
		return "fxml/" + Glb.getConst().getAppName() + ".fxml";
	}

	public String getPropertyFileName() {
		return Glb.getConst().getAppName();
	}

	public String getPrivateKeyFile() {
		return "PrivateKey";
	}

	public String getPublicKeyFile() {
		return "PublicKey";
	}

	public String getKeySuffix() {
		return ".txt";
	}

	private final String dbPath = "/db";

	public String getObjectivityDBPath() {
		return getObjectivityDir() + dbPath;
	}

	public String getSubjectivityDBPath() {
		return getSubjectivityDir() + dbPath;
	}

	public String getLogDBPath() {
		return getLogDir() + dbPath;
	}

	public String getLogDir() {
		return getModelDir() + "/log";
	}

	public String getMiddleDBPath() {
		return getMiddleDir() + dbPath;
	}

	public String getObjectivityDir() {
		return getModelDir() + "/objectivity";
	}

	public String getSubjectivityDir() {
		return getModelDir() + "/subjectivity";
	}

	public String getMiddleDir() {
		return getModelDir() + "/middle";
	}

	public String getSubObjectivityDir() {
		return getModelDir() + "/subObjectivity";
	}

	public String getDefenseDir() {
		return getModelDir() + "/defense";
	}

	public String getSubDBPathPrefix() {
		return getSubObjectivityDir() + "/";
	}

	public String getSubDBPathSuffix() {
		return "db";
	}

	public String getDefenseDBPath() {
		return getDefenseDir() + "/" + "db";
	}

	/**
	 * ソフトウェアの次期バージョンが置かれるフォルダ
	 */
	public String getUpdateDir() {
		return getObjectivityDir() + "/update";
	}

	public String getModelDir() {
		return "model";
	}

	/**
	 * Problem.javaの位置
	 */
	public String getCPUProvementPackage() {
		return CPUProvement.class.getPackage().getName();
	}

	public String getLogMessagePropertyFilePath(String language) {
		return "message_" + language.toLowerCase() + ".properties";
	}

	public String getTypeSelectorIdLogPath() {
		return "typeSelectorIdLog.txt";
	}

	public String getMaterialDir() {
		return getUploadFileDir() + getMaterialDirSingle();
	}

	public String getMaterialDirSingle() {
		return "material/";
	}

	public String getStaticGameDir() {
		return getDynamicFileDir() + getStaticGameDirSingle();
	}

	public String getStaticGameDirSingle() {
		return "staticGame/";
	}

	public String getRatingGameDir() {
		return getDynamicFileDir() + getRatingGameDirSingle();
	}

	public String getRatingGameDirSingle() {
		return "ratingGame/";
	}

	/**
	 * @param categoryName		avatar, natural, building等。
	 * @param graphicStyleName	リアル、トゥーン、SD等。
	 * @param materialName		アバター名等。強く関連した1素材群の名前
	 * @return
	 */
	public String getAvatarDir(String categoryName, String graphicStyleName,
			String materialName) {
		return getMaterialDir() + "/" + graphicStyleName + "/avatar/"
				+ materialName;
	}

	public String getOtherMaterialDir(String graphicStyleName,
			String materialName) {
		return getMaterialDir() + "/" + graphicStyleName + "/other/"
				+ materialName;
	}

	/**
	 * @return	素材やゲーム関連ファイル等動的にアップロードされるファイルのディレクトリ
	 */
	public String getDynamicFileDir() {
		return Glb.getConf().getDynamicFilesDir();
	}

	public String getFileReceivingPath(String fileName) {
		return Glb.getFile().getDynamicFileDir() + fileName;
	}

	public String getWriteBitsSuffix() {
		return ".writeBits";
	}

}
