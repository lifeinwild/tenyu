package glb;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.util.*;
import veddan.physicalcores.*;

/**
 * エンドユーザーが設定する設定ファイルからの情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class Conf {
	/**
	 * アップデート直後の起動か
	 */
	private boolean afterUpdateLaunch = false;

	/**
	 * 起動日時
	 */
	private long launchDate = System.currentTimeMillis();

	public long getLaunchDate() {
		return launchDate;
	}

	public Locale getLocale() {
		return Locale.getDefault();
	}

	public AddrInfo getAddrInfo() {
		AddrInfo addr = new AddrInfo();
		addr.setFqdn(getFqdn());
		addr.setP2pPort(getP2pPort());
		addr.setGamePort(getGamePort());
		addr.setTenyutalkPort(getTenyutalkPort());
		return addr;
	}

	private RunLevel runLevel = RunLevel.DEV;

	/**
	 * ゲームのクライアントソフトウェアで使われるポート
	 */
	private int gamePort;
	/**
	 * Tenyu基盤ソフトウェアのP2P用ポート
	 */
	private int p2pPort;
	/**
	 * tenyutalk用ポート
	 */
	private int tenyutalkPort;

	private int userMessageServerPort;
	private boolean neighborWhitelist = false;

	/**
	 * プロセッサ証明において回答者にならない。検証者にはなる。
	 */
	private boolean serverMode = false;

	/**
	 * Tenyu基盤ソフトウェアのユーザーの鍵
	 * アプリフォルダ以下に鍵が記録される
	 *
	 * JDKの{@link KeyStore}を使わなかった。１フォルダを移動させるだけで
	 * 別環境に移せるという要件のため。
	 * KeyStoreはJDKフォルダ以下に記録されるようだった。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	protected Keys keys = new Keys(this);

	/**
	 * タイムゾーンが同じなら地理的に近かったり経済水準が近かったりする。
	 * 地理的に近いとはレイテンシが短いということ。
	 * 国コードよりタイムゾーンがその用途で適している。
	 */
	private ZoneId timeZone = ZoneId.systemDefault();

	private String fqdn;

	/**
	 * 通常のPCよりもはるかに大きなストレージか
	 */
	private boolean bigStorage;

	/**
	 * 自分の国や言語
	 */
	private Locale myLocale = Locale.getDefault();

	/**
	 * 利用可能な物理コア数。ユーザーによって実際の物理コア数より
	 * 低く設定されている場合もある。
	 * もし環境の問題で取得できなかった場合、論理コア数の半分の数値が設定される。
	 * 実際それは物理コア数の半分になっている場合がある。
	 */
	private int physicalCoreNumber;

	/**
	 * このノードのP2PNodeのnodeNumber
	 */
	private int nodeNumber;

	/**
	 * 動的にアップロードされるファイルが置かれるフォルダ
	 * HDDを想定
	 * https://pc.watch.impress.co.jp/docs/column/semicon/1168315.html
	 *
	 * 本来OSがHDDやSSDを使い分けてくれるのが妥当な気がするが現状アプリで対応するしかない。
	 * アップロードされた素材やゲームなどが保存される。
	 * tenyuアプリ本体と異なるドライブを指定できる。
	 *
	 * Tenyu基盤ソフトウェアが書き込み操作するフォルダは
	 * 必ずアプリ名をパスに含む必要がある。
	 *
	 * もしユーザーがdynamicFilesDirを設定する場合、
	 * Tenyuを終了してから既存のdynamicFilesDirを新しい場所に移して、
	 * tenyu.confのdynamicFilesDirに新しいdynamicFilesDirの絶対パスを設定し、
	 * Tenyuを起動すると新しいフォルダが使用される。
	 */
	private String dynamicFilesDir = Glb.getConst().getAppName().toLowerCase()
			+ "DynamicFiles/";

	public String getDynamicFilesDir() {
		return dynamicFilesDir;
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	public String getFqdn() {
		return fqdn;
	}

	public Keys getKeys() {
		return keys;
	}

	public boolean isAfterUpdateLaunch() {
		return afterUpdateLaunch;
	}

	public void setAfterUpdateLaunch(boolean afterUpdateLaunch) {
		this.afterUpdateLaunch = afterUpdateLaunch;
	}

	/**
	 * TODO:本番動作では必要ないので削除すべきか
	 * @param l
	 */
	public void setRunLevel(RunLevel l) {
		this.runLevel = l;
	}

	public boolean getNeighborWhitelist() {
		return neighborWhitelist;
	}

	public int getP2pPort() {
		return p2pPort;
	}

	public AddrInfo getAddrInfo(byte[] addr) {
		return new AddrInfo(addr, getFqdn(), getP2pPort(), getGamePort(),
				getTenyutalkPort());
	}

	public int getPhysicalCoreNumber() {
		return physicalCoreNumber;
	}

	public RunLevel getRunlevel() {
		return runLevel;
	}

	public void init() {
		//		loc = loadLoc();
		loadTenyuConf();
	}

	/**
	 * @param password	検証されるパスワード
	 * @return	検証を通過したらnull。エラーがあったらそのエラーメッセージ
	 */
	public ValidationResult validateSecretKeyPassword(String password) {
		return validateSecretKeyPassword(password, new ValidationResult());
	}

	public ValidationResult validateSecretKeyPassword(String password,
			ValidationResult r) {
		if (password == null || password.length() == 0) {
			r.add(Lang.SECRETKEYPASSWORD, Lang.ERROR_EMPTY);
		} else {
			int min = Glb.getConst().getPasswordSizeMin();
			int max = Glb.getConst().getPasswordSizeMax();
			if (password.length() < min) {
				r.add(Lang.SECRETKEYPASSWORD, Lang.ERROR_TOO_SHORT);
			}
			if (password.length() > max) {
				r.add(Lang.SECRETKEYPASSWORD, Lang.ERROR_TOO_LONG);
			}
		}
		return r;
	}

	public boolean isBigStorage() {
		return bigStorage;
	}

	/**
	 * @return	開発またはテストモードか
	 */
	public boolean isDevOrTest() {
		return runLevel.equals(RunLevel.DEV) || runLevel.equals(RunLevel.TEST);
	}

	private void loadTenyuConf() {
		Properties tenyuconf = Glb.getUtil().loadProperties(f.getConfFile(),
				Glb.getConst().getCharset(), Glb.getLogger());
		//設定が必須なタイプ
		String runlevelStr = tenyuconf.getProperty("runLevel");
		try {
			runLevel = RunLevel.valueOf(runlevelStr);
			//runlevel = RunLevel.RELEASE;//DEV_CODE
		} catch (Exception e) {
			Glb.getLogger().error(Lang.RUNLEVEL + Lang.ERROR_INVALID.toString(),
					e);
			System.exit(1);
		}
		Glb.getLogger().info(Lang.RUNLEVEL + " " + getRunlevel());
		if (!runLevel.equals(RunLevel.RELEASE)) {
			Glb.getLogger().warn(Lang.RUNLEVEL_WARN);
		}

		String p2pPortStr = tenyuconf.getProperty("p2pPort");
		try {
			p2pPort = Integer.parseInt(p2pPortStr);
			Glb.getLogger().info(Lang.P2PPORT + " " + getP2pPort());
		} catch (Exception e) {
			Glb.getLogger().error(
					Lang.P2PPORT.toString() + Lang.ERROR_INVALID.toString(), e);
			System.exit(1);
		}

		//以下、設定が無くても良いタイプ
		String bigStorageStr = tenyuconf.getProperty("bigStorage");
		if (bigStorageStr != null) {
			try {
				bigStorage = Boolean.valueOf(bigStorageStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.BIG_STORAGE + Lang.ERROR_INVALID.toString(), e);
			}
		}
		Glb.getLogger().info(Lang.BIG_STORAGE + " " + isBigStorage());

		String neighborWhitelistStr = tenyuconf
				.getProperty("neighborWhitelist");
		if (neighborWhitelistStr != null) {
			try {
				neighborWhitelist = Boolean.valueOf(neighborWhitelistStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.NEIGHBOR_WHITELIST + Lang.ERROR_INVALID.toString(),
						e);
			}
		}
		Glb.getLogger()
				.info(Lang.NEIGHBOR_WHITELIST + " " + getNeighborWhitelist());

		String fqdnTmp = tenyuconf.getProperty("fqdn");
		if (fqdnTmp != null) {
			try {
				InetAddress.getByName(fqdnTmp);
				fqdn = fqdnTmp;
			} catch (Exception e) {
				Glb.getLogger().error(Lang.FQDN + Lang.ERROR_INVALID.toString(),
						e);
			}
		}
		Glb.getLogger().info(Lang.FQDN_CONF + " " + getFqdn());

		String processorProvementCoreNumberStr = tenyuconf
				.getProperty("processorProvementCoreNumber");
		if (processorProvementCoreNumberStr == null) {
			//設定値が無ければ自動的に取得
			setupPhysicalCoreNumber();
		} else {
			try {
				//設定値があればそれを使用する
				int physicalCoreNumber = Integer
						.parseInt(processorProvementCoreNumberStr);
				if (physicalCoreNumber < 1) {
					physicalCoreNumber = 1;
				}
				this.physicalCoreNumber = physicalCoreNumber;
			} catch (Exception e) {
				//例外が出たら自動的に取得
				Glb.getLogger().error(Lang.PHYSICAL_CORE_NUMBER
						+ Lang.ERROR_INVALID.toString(), e);
				setupPhysicalCoreNumber();
			}
		}
		Glb.getLogger().info(
				Lang.PHYSICAL_CORE_NUMBER + " " + getPhysicalCoreNumber());

		String nodeNumberStr = tenyuconf.getProperty("nodeNumber");
		if (nodeNumberStr != null) {
			try {
				nodeNumber = Integer.valueOf(nodeNumberStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.NODE_NUMBER + Lang.ERROR_INVALID.toString(), e);
			}
		}
		Glb.getLogger().info(Lang.NODE_NUMBER + " " + nodeNumber);

		String dynamicFilesDirStr = tenyuconf.getProperty("dynamicFilesDir");
		if (dynamicFilesDirStr != null && dynamicFilesDirStr.length() > 0) {
			try {
				File dynamicFilesDirF = f.get(dynamicFilesDirStr);
				if (dynamicFilesDirF.exists()
						&& dynamicFilesDirF.isDirectory()) {
					dynamicFilesDir = Glb.getUtil()
							.addSlashIfNot(dynamicFilesDirStr);
				} else {
					throw new Exception();
				}
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.DYNAMICFILES_DIR + Lang.ERROR_INVALID.toString(),
						e);
			}
		}
		Glb.getLogger().info(Lang.DYNAMICFILES_DIR + " " + dynamicFilesDir);

		String gamePortStr = tenyuconf.getProperty("gamePort");
		if (gamePortStr != null) {
			gamePortStr = gamePortStr.trim();
			try {
				gamePort = Integer.valueOf(gamePortStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.GAMEPORT.toString() + Lang.ERROR_INVALID, e);
			}
		}
		Glb.getLogger().info(Lang.GAMEPORT + " " + gamePort);

		String tenyutalkPortStr = tenyuconf.getProperty("tenyutalkPort");
		if (tenyutalkPortStr != null) {
			tenyutalkPortStr = tenyutalkPortStr.trim();
			try {
				tenyutalkPort = Integer.valueOf(tenyutalkPortStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.TENYUTALKPORT.toString() + Lang.ERROR_INVALID, e);
			}
		}
		Glb.getLogger().info(Lang.TENYUTALKPORT + " " + gamePort);

		String serverModeStr = tenyuconf.getProperty("serverMode");
		if (serverModeStr != null) {
			serverModeStr = serverModeStr.trim();
			try {
				serverMode = Boolean.valueOf(serverModeStr);
			} catch (Exception e) {
				Glb.getLogger().error(
						Lang.SERVERMODE.toString() + Lang.ERROR_INVALID, e);
			}
		}
		Glb.getLogger().info(Lang.SERVERMODE + " " + serverMode);
	}

	private void setupPhysicalCoreNumber() {
		//https://github.com/veddan/java-physical-coresを使用
		//jarになっていないが、ライセンスはapache 2.0でコピペして使用できるようだ。
		Integer physicalCoreNumber = PhysicalCores.physicalCoreCount();

		if (physicalCoreNumber == null) {
			int logicalCoreNumber = Runtime.getRuntime().availableProcessors();
			//HT搭載の場合に論理コア数を使用するとプロセッサ証明で重くなりすぎるので
			//HT非搭載の場合に物理コアの半分しか使えないとしても、半分にする
			physicalCoreNumber = logicalCoreNumber / 2;
		}

		if (physicalCoreNumber < 1)
			physicalCoreNumber = 1;

		this.physicalCoreNumber = physicalCoreNumber;

		if (Glb.getConf().isDevOrTest()) {
			Glb.debug("physicalCoreNumber=" + physicalCoreNumber);
		}
	}

	public static enum RunLevel {
		/**
		 * 本番動作。デバッグログ無し
		 */
		RELEASE,
		/**
		* 本番動作の試験。デバッグログ無し
		* テスターによるテストを想定
		*/
		TEST,
		/**
		 * 開発動作。CPU証明など時間がかかる処理が軽減される。
		 * デバッグログあり
		 */
		DEV,
		/**
		 * βテスト
		 * エンドユーザーが試運転に参加する場合を想定
		 */
		BETA_TEST,
	}

	/**
	 * 意味的に設置されたというより
	 * テストコードとの兼ね合いでコーディング上の都合で設置された。
	 */
	protected FileManagement f = Glb.getFile();

	public ZoneId getTimeZone() {
		return timeZone;
	}

	/**
	 * @return	後進地域か
	 */
	public boolean isDiscriminated() {
		return !Glb.getConst().isDevelopedRegions(myLocale.getCountry());
	}

	public int getUserMessageServerPort() {
		return userMessageServerPort;
	}

	public int getGamePort() {
		return gamePort;
	}

	public boolean isServerMode() {
		return serverMode;
	}

	public int getTenyutalkPort() {
		return tenyutalkPort;
	}

	FileManagement getFile() {
		return f;
	}

	/**
	 * user.homeにこのアプリの情報、例えばローカルIPCポートを設置し、
	 * 他のアプリがこのアプリと連携できるようにする。
	 *
	 * 現在の呼び出し位置だと、起動直後秘密鍵パスワードを入力した後に
	 * これが呼ばれる。外部アプリとの連携もそれ以降出ないとできない。
	 * しかしそれは仕方がないだろう。
	 * パスワードが無ければ動作しない機能がたくさんある。
	 */
	public void createUserHomeFile() {
		try {
			String dir = Glb.getFile().getUserHomeTenyuDir();
			Path p = Paths.get(dir, Glb.getFile().getUserHomeFile());

			StringBuilder content = new StringBuilder();
			if (Glb.getLocalIpc() != null) {
				content.append(localIpcPort + "=" + Glb.getLocalIpc().getPort()
						+ System.lineSeparator());
			}
			content.append(tenyuDir + "=" + Glb.getUtil().getExecutionFilePath()
					+ System.lineSeparator());

			boolean r = Glb.getFile().create(p,
					content.toString().getBytes(Glb.getConst().getCharsetNio()),
					true);
			if (!r) {
				throw new IOException();
			}
		} catch (Exception e) {
			Glb.getLogger().error("Failed to create UserHomeFile", e);
		}
	}

	private static final String localIpcPort = "localIpcPort";
	private static final String tenyuDir = "tenyuDir";

	/**
	 * @return	user.homeのファイルから取得されたLocalIPC用ポート
	 */
	public int getLocalIpcPortFromUserHomeFile() {
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(f.getUserHomeFileFull()));
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return -1;
		}
		for (String line : lines) {
			if (line.startsWith(localIpcPort)) {
				return Integer.valueOf(line.substring(line.indexOf("=") + 1));
			}
		}
		return -1;
	}
}
