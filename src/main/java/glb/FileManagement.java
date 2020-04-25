package glb;

import java.io.*;
import java.nio.file.*;

import org.apache.commons.io.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.*;
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
	 * @return	非アップロード系の、基盤ソフトウェアが作成するほとんどの動的なファイルはここ以下に作成される
	 */
	public String getDataDir() {
		return "data/";
	}

	/**
	 * @return	動的なjarファイルを置くフォルダ
	 */
	public String getJarDir() {
		return getDynamicFileDir() + "jars/";
	}

	/**
	 * @return	システム用jarを置くフォルダ
	 */
	public String getSystemJarDir() {
		return getJarDir() + "system/";
	}

	/**
	 * @return	信頼されたユーザーが作成したjarを置くフォルダ
	 */
	public String getTrustedJarDir() {
		return getJarDir() + "trusted/";
	}

	/**
	 * @return	友人ユーザーが作成したjarを置くフォルダ
	 */
	public String getFriendJarDir() {
		return getJarDir() + "friend/";
	}

	/**
	 * @return	匿名ユーザーが作成したjarを置くフォルダ
	 */
	public String getAnonymouseJarDir() {
		return getJarDir() + "anonymouse/";
	}

	private final String policyFileName = "tenyu.policy";

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
		return getDynamicFileDir() + "/" + getPlatformFileDirName();
	}

	/**
	 * @return	次期バージョンのソフトウェアが作成されるフォルダ
	 */
	public String getPlatformFileDirName() {
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
			//シンボリックリンクの場合リンク自体が削除されるが、仕様化されていない
			//https://issues.apache.org/jira/browse/IO-576
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
		return removeInternal(p, false);
	}

	/**
	 * @return	実行ファイルの名前
	 */
	public String getExecutableJarName() {
		return Glb.getConst().getAppName() + ".jar";
	}

	public String getCodeBaseOnIde() {
		return "./target/classes/";
	}

	public String getPolicyFileName() {
		return policyFileName;
	}

	/**
	 * @param p
	 * @return pがファイルなら削除する
	 */
	public boolean removeIfFile(Path p) {
		if (p == null || !isAppPathBoth(p))
			return false;
		return removeInternal(p, true);
	}

	private boolean removeInternal(Path p) {
		return removeInternal(p, false);
	}

	private boolean removeInternal(Path p, boolean onlyFile) {
		File f = p.toFile();
		if (f.isDirectory()) {
			if (onlyFile)
				return false;
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

	/**
	 * Glb.getConf()が設定されてから呼ぶ
	 */
	public void dirSetupAfterConf() {
		File dynamicDirF = get(getDynamicFileDir());
		dynamicDirF.mkdir();
		File jarDirF = get(getJarDir());
		jarDirF.mkdir();
		File systemJarDirF = get(getSystemJarDir());
		systemJarDirF.mkdir();
		File trustedJarDirF = get(getTrustedJarDir());
		trustedJarDirF.mkdir();
		File friendJarDirF = get(getFriendJarDir());
		friendJarDirF.mkdir();
		File anonymouseJarDirF = get(getAnonymouseJarDir());
		anonymouseJarDirF.mkdir();
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
		return getDataDir() + "tenyu.conf";
	}

	public String getKeyDir() {
		return getDataDir() + "key";
	}

	public String getKeyGenerated() {
		return getKeyDir() + "/generated";
	}

	public String getFxmlPath() {
		return getDataDir() + "fxml/" + Glb.getConst().getAppName() + ".fxml";
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

	private final String dbPath = "/" + getSubDBPathSuffix();

	private final String tenyutalkDir = "/tenyutalk";

	public String getTenyutalkDBPath(String byNode) {
		return getTenyutalkDir() + "/" + byNode + dbPath;
	}

	public String getCookieDBPath() {
		return getCookieDir() + dbPath;
	}

	public String getTenyutalkFileDir() {
		return getTenyutalkDir() + "/file";
	}

	public String getTenyutalkDir() {
		return Glb.getConf().getDynamicFilesDir() + tenyutalkDir;
	}

	public String getCookieDir() {
		return "cookie";
	}

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
		return getDefenseDir() + "/" + getSubDBPathSuffix();
	}

	/**
	 * ソフトウェアの次期バージョンが置かれるフォルダ
	 */
	public String getUpdateDir() {
		return getObjectivityDir() + "/update";
	}

	public String getModelDir() {
		return getDataDir() + "model";
	}

	/**
	 * Problem.javaの位置
	 */
	public String getCPUProvementPackage() {
		return CPUProvement.class.getPackage().getName();
	}

	public String getLogMessagePropertyFilePath(String language) {
		return getDataDir() + "message_" + language.toLowerCase()
				+ ".properties";
	}

	public String getTypeSelectorIdLogPath() {
		return getDataDir() + "typeSelectorIdLog.txt";
	}

	/**
	 * @return	素材やゲーム関連ファイル等動的にアップロードされるファイルのディレクトリ
	 */
	public String getDynamicFileDir() {
		return Glb.getConf().getDynamicFilesDir();
	}

	public String getFileReceivingPath(String fileName) {
		return Glb.getFile().getDynamicFileDir() + "/" + fileName;
	}

	public String getWriteBitsSuffix() {
		return ".writeBits";
	}

	public String getCss() {
		return getDataDir() + Glb.getConst().getAppName() + ".css";
	}

}
