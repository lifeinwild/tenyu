package glb;

import java.io.*;
import java.nio.file.*;

import org.apache.commons.io.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
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
	private final String dbDir = getSubDBPathSuffix() + "/";

	private final String policyFileName = "tenyu.policy";

	private final String tenyutalkDir = "talk/";

	private final String userHomeFile = "tenyu.txt";

	private final String tenyutalkReleaseDir = "_re/";
	private final String tenyutalkExtractDir = "_ex/";

	/**
	 * @param p	作成されるパス
	 * @return	ファイルが作成されたか
	 */
	public boolean create(Path p, byte[] content, boolean overwrite) {
		if (p == null || !isAppPath(p))
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

	public File get(Path path) {
		if (!isAppPath(path))
			return null;
		return path.toFile();
	}

	/**
	 * @param path	操作したいファイルシステム上のパス
	 * @return	検証済みパス。検証に失敗したらnull
	 */
	public File get(String path) {
		if (!isAppPath(path))
			return null;
		return new File(path);
	}

	/**
	 * @return	匿名ユーザーが作成したjarを置くフォルダ
	 */
	public String getAnonymouseJarDir() {
		return getJarDir() + "anonymouse/";
	}

	public String getCodeBaseOnIde() {
		return "./target/classes/";
	}

	public String getConfFile() {
		return getDataDir() + "tenyu.conf";
	}

	public String getCookieDBDir() {
		return getCookieDir() + dbDir;
	}

	public String getCookieDir() {
		return "cookie/";
	}

	/**
	 * Problem.javaの位置
	 */
	public String getCPUProvementPackage() {
		return CPUProvement.class.getPackage().getName();
	}

	public String getCss() {
		return getDataDir() + Glb.getConst().getAppName() + ".css";
	}

	/**
	 * @return	非アップロード系の、基盤ソフトウェアが作成するほとんどの動的なファイルはここ以下に作成される
	 */
	public String getDataDir() {
		return "data/";
	}

	public String getDefenseDBDir() {
		return getDefenseDir() + getSubDBPathSuffix() + "/";
	}

	public String getDefenseDir() {
		return getModelDir() + "defense/";
	}

	/**
	 * @return	素材やゲーム関連ファイル等動的にアップロードされるファイルのディレクトリ
	 */
	public String getDynamicFileDir() {
		return Glb.getConf().getDynamicFilesDir();
	}

	/**
	 * @return	実行ファイルの名前
	 */
	public String getExecutableJarName() {
		return Glb.getConst().getAppName() + ".jar";
	}

	/**
	 * @return	友人ユーザーが作成したjarを置くフォルダ
	 */
	public String getFriendJarDir() {
		return getJarDir() + "friend/";
	}

	public String getFxmlPath() {
		return getDataDir() + "fxml/" + Glb.getConst().getAppName() + ".fxml";
	}

	public Image getIcon() {
		return new Image(
				Glb.getUtil().getLoader().getResourceAsStream(getIconPath()));
	}

	public String getIconDir() {
		return "images/";
	}

	public String getIconPath() {
		return getIconDir() + Glb.getConst().getAppName() + ".png";
	}

	/**
	 * @return	動的なjarファイルを置くフォルダ
	 */
	public String getJarDir() {
		return getDynamicFileDir() + "jars/";
	}

	public String getKeyDir() {
		return getDataDir() + "key/";
	}

	private String getKeyFilePrefix(String prefix, String name) {
		if (prefix == null || prefix.length() == 0) {
			return getKeyDir() + "/" + name;
		} else {
			return getKeyDir() + "/" + prefix + "_" + name;
		}
	}

	public String getKeyGenerated() {
		return getKeyDir() + "generated/";
	}

	public String getKeySuffix() {
		return ".txt";
	}

	public String getLogDBDir() {
		return getLogDir() + dbDir;
	}

	public String getLogDir() {
		return getModelDir() + "log/";
	}

	public String getLogMessagePropertyFilePath(String language) {
		return getDataDir() + "message_" + language.toLowerCase()
				+ ".properties";
	}

	public String getMiddleDBDir() {
		return getMiddleDir() + dbDir;
	}

	public String getMiddleDir() {
		return getModelDir() + "middle/";
	}

	public String getModelDir() {
		return getDataDir() + "model/";
	}

	public String getObjectivityDBDir() {
		return getObjectivityDir() + dbDir;
	}

	public String getObjectivityDir() {
		return getModelDir() + "objectivity/";
	}

	public Path getPath(String path) {
		if (!isAppPath(path))
			return null;
		return Paths.get(path);
	}

	/**
	 * @return	基盤ソフトウェアの次期バージョンのファイルが置かれるフォルダ
	 */
	public String getPlatformFileDir() {
		return getDynamicFileDir() + getPlatformFileDirName();
	}

	/**
	 * @return	次期バージョンのソフトウェアが作成されるフォルダ
	 */
	public String getPlatformFileDirName() {
		return "platform/";
	}

	public String getPolicyFileName() {
		return policyFileName;
	}

	public String getPrivateKeyFile() {
		return "PrivateKey";
	}

	public String getPrivateKeyPath(KeyType type) {
		return getPrivateKeyPath(type, "");
	}

	public String getPrivateKeyPath(KeyType type, String prefix) {
		return getKeyFilePrefix(prefix, type.name()) + getPrivateKeyFile()
				+ getKeySuffix();
	}

	public String getPublicKeyFile() {
		return "PublicKey";
	}

	public String getPublicKeyPath(KeyType type) {
		return getPublicKeyPath(type, "");
	}

	public String getPublicKeyPath(KeyType type, String prefix) {
		return getKeyFilePrefix(prefix, type.name()) + getPublicKeyFile()
				+ getKeySuffix();
	}

	public String getSignKeyPath(KeyType targetKey, KeyType by) {
		return getSignKeyPath(targetKey, by, "");
	}

	public String getSignKeyPath(KeyType targetKey, KeyType by, String prefix) {
		return getKeyFilePrefix(prefix, targetKey.name() + "By" + by.name())
				+ "_sign.txt";
	}

	public String getSubDBPathSuffix() {
		return "db";
	}

	public String getSubjectivityDBDir() {
		return getSubjectivityDir() + dbDir;
	}

	public String getSubjectivityDir() {
		return getModelDir() + "subjectivity/";
	}

	/**
	 * @return	システム用jarを置くフォルダ
	 */
	public String getSystemJarDir() {
		return getJarDir() + "system/";
	}

	public String getTenyutalkDBDir(String byNode) {
		return getTenyutalkDir() + byNode + dbDir;
	}

	public String getTenyutalkDir() {
		return Glb.getConf().getDynamicFilesDir() + tenyutalkDir;
	}

	public String getTenyutalkDirByUser(User u) {
		return getTenyutalkDir() + idGroup(u.getId()) + "/" + u.getName() + "/";
	}

	/**
	 * （ファイルシステム上の）１フォルダ内の最大ファイル数
	 */
	public static final int unit = 1000;

	public long idGroup(Long id) {
		return id / unit;
	}

	public String getTenyutalkRepositoryDir(TenyuRepositoryI repo) {
		return getTenyutalkDirByUser(repo.getUploaderUser()) + repo.getName()
				+ "/";
	}

	public String getTenyutalkRepositoryReleaseDir(TenyuRepositoryI repo) {
		return getTenyutalkRepositoryDir(repo) + tenyutalkReleaseDir;
	}

	public String getTenyutalkRepositoryReleaseArtifactDir(
			TenyuArtifactI arti) {
		return getTenyutalkRepositoryReleaseDir(arti.getTenyuRepository())
				+ arti.getName() + "/";
	}

	public String getTenyutalkRepositoryReleaseExtractDir(TenyuArtifactI arti) {
		return getTenyutalkRepositoryReleaseArtifactDir(arti)
				+ tenyutalkExtractDir;
	}

	public String getTenyutalkAppletDir(TenyuRepositoryI repo) {
		return getTenyutalkDirByUser(repo.getUploaderUser()) + "applet"
				+ repo.getName() + "/";
	}

	public String getTenyutalkRepositoryWorkingDir(TenyuRepositoryI repo) {
		return getTenyutalkRepositoryDir(repo) + "working/";
	}

	/**
	 * このフォルダに置かれたファイルがp2pネットワーク上からアクセスされる。
	 * @return	talk系ファイルを置くフォルダ
	 */
	public String getTenyutalkFileDir() {
		return getTenyutalkDir() + "file/";
	}

	/**
	 * このフォルダ内でファイル操作を行うとTenyu基盤ソフトウェアが自動的に
	 * ファイルの変更を検出し、コミットを作成する。
	 * @return	talk系ファイルを編集する作業フォルダ
	 */
	public String getTenyutalkFileWorkingDir() {
		return getTenyutalkDir() + "working/";
	}

	/**
	 * @return	信頼されたユーザーが作成したjarを置くフォルダ
	 */
	public String getTrustedJarDir() {
		return getJarDir() + "trusted/";
	}

	public String getTypeSelectorIdLogPath() {
		return getDataDir() + "typeSelectorIdLog.txt";
	}

	/**
	 * ソフトウェアの次期バージョンが置かれるフォルダ
	 */
	public String getUpdateDir() {
		return getObjectivityDir() + "update/";
	}

	public String getUserHomeFile() {
		return userHomeFile;
	}

	public String getUserHomeFileFull() {
		return getUserHomeTenyuDir() + userHomeFile;
	}

	public String getUserHomeTenyuDir() {
		String userhome = System.getProperty("user.home");
		if (userhome == null) {
			Glb.getLogger().error("Failed to get user.home", new Exception());
			return "";
		}
		userhome = Glb.getUtil().addSlashIfNot(userhome);
		return userhome + Glb.getConst().getAppName().toLowerCase() + "/";
	}

	public String getWriteBitsSuffix() {
		return ".writeBits";
	}

	public boolean isAppPathAbsolute(String absolutePath) {
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
	 * ファイル削除や移動などでアプリ外のファイルに影響を与えないようにする防止策
	 * @param p	相対又は絶対パス
	 * @return	パスpがアプリ名を含んでいるか
	 */
	public boolean isAppPath(Path p) {
		return isAppPathAbsolute(p.toAbsolutePath().toString());
	}

	/**
	 * @param path	相対又は絶対パス
	 * @return	アプリケーションのパスか
	 */
	public boolean isAppPath(String path) {
		Path p = null;
		try {
			p = Paths.get(path);
		} catch (Exception e) {
			Glb.getLogger().error("No such file", new IOException());
		}

		if (!isAppPath(p)) {
			return false;
		}
		return true;
	}

	public boolean isAppPathRelative(String relativePath) {
		return isAppPath(Paths.get(relativePath).toAbsolutePath());
	}

	public Path move(Path p1, Path p2) {
		if (p1 == null || p2 == null)
			return null;
		if (!isAppPath(p1) || !isAppPath(p2)) {
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

	public void open() {
		dirSetup();
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
		if (p == null || !isAppPath(p))
			return false;
		return removeInternal(p, false);
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
	 * @param p
	 * @return pがファイルなら削除する
	 */
	public boolean removeIfFile(Path p) {
		if (p == null || !isAppPath(p))
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

}
