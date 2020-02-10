package glb;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.text.*;
import java.time.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
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

	/**
	 * セキュアユーザーか
	 * Userクラスに詳細
	 */
	private boolean secureUser = false;

	private RunLevel runLevel;

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
	/**
	 * localhostの他のプログラムにwebapiを提供するためのポート
	 */
	private int localIpcPort;
	private int userMessageServerPort;
	private boolean neighborWhitelist = false;

	/**
	 * プロセッサ証明において回答者にならない。検証者にはなる。
	 */
	private boolean serverMode = false;

	protected PublicKey myOfflinePublicKey;
	protected PublicKey myPcPublicKey;
	protected PublicKey myMobilePublicKey;

	/**
	 * この設定を変えると通信メッセージにおいて使用される鍵が変わる。
	 *
	 */
	private KeyType myStandardKeyType = KeyType.PC;

	//getter禁止。ここで署名機能を提供するだけ。秘密鍵情報はConfから出ない。
	//署名機能は本来Confではないかもしれないが、セキュリティのためここに書く。
	//同じ理由で、getter等のインターフェースはKeyPairではなく
	//PublicKeyやPrivateKeyといったレベルで作成される。
	protected PrivateKey myPcPrivateKey;
	protected PrivateKey myMobilePrivateKey;
	protected PrivateKey myOfflinePrivateKey;//nullable

	protected byte[] myPcKeySignByOffB;
	protected byte[] myMobileKeySignByOffB;
	protected byte[] myOffKeySignByPcB;
	protected byte[] myOffKeySignByMobB;

	protected FileManagement f = Glb.getFile();
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

	public KeyPair generateKey(boolean secureUser) {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator
					.getInstance(Glb.getConst().getKeyPairGeneratorAlgorithm());

			keyPairGen.initialize(User.getRsaKeySizeBitBySecure(secureUser));
			return keyPairGen.genKeyPair();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			System.exit(1);
		}
		return null;
	}

	/**
	 * パスワード無し版。公開鍵は暗号化されない
	 * @param path
	 * @param backupPath
	 * @param data
	 * @return
	 */
	private boolean writeToFile(String path, String backupPath, byte[] data) {
		return writeToFile(path, backupPath, data, null);
	}

	private boolean writeToFile(String path, String backupPath, byte[] data,
			byte[] password) {
		try {
			if (!Glb.getFile().isAppPathRelative(path)
					|| !Glb.getFile().isAppPathRelative(backupPath)) {
				Glb.getLogger().warn("", new Exception("invalid path"));
				return false;
			}

			File file = f.get(path);
			if (file.exists()) {
				f.move(file.toPath(), Paths.get(backupPath));
			}

			if (password != null)
				data = Glb.getUtil().crypt(true, password, data);
			String base64 = Base64.getEncoder().encodeToString(data);

			writeToFile(base64, file);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * RSA鍵をファイルシステムから読み込む。
	 * 無ければ作成し、ファイルに書き込む。
	 * その時、中途半端に一部の鍵や署名ファイルがあればそれをバックアップする。
	 *	generatedが無いなら鍵があってもリネームして再作成。
	 * このメソッドが呼ばれた時点でgeneratedは判定されているべき。
	 * @param type
	 * @param backupPrefix
	 * @return
	 */
	private KeyPair generateKeyAndWriteToFile(KeyType type, String backupPrefix,
			byte[] password) {
		KeyPair pair = null;
		try {
			Glb.getLogger().info(Lang.START_GENERATE_KEY + ":" + type);
			pair = generateKey(secureUser);

			writeToFile(f.getPublicKeyPath(type),
					f.getPublicKeyPath(type, backupPrefix),
					pair.getPublic().getEncoded());
			writeToFile(f.getPrivateKeyPath(type),
					f.getPrivateKeyPath(type, backupPrefix),
					pair.getPrivate().getEncoded(), password);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			System.exit(1);
		}
		return pair;
	}

	private boolean writeToFile(String str, File f) throws Exception {
		try (FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);) {
			pw.print(str);
			pw.flush();
		}
		return true;
	}

	public String getFqdn() {
		return fqdn;
	}

	public int getLocalIpcPort() {
		return localIpcPort;
	}

	public PublicKey getMyMobilePublicKey() {
		return myMobilePublicKey;
	}

	public PublicKey getMyOfflinePublicKey() {
		return myOfflinePublicKey;
	}

	public PublicKey getMyPcPublicKey() {
		return myPcPublicKey;
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

	private PrivateKey getMyPrivateKey(KeyType type) {
		switch (type) {
		case MOBILE:
			return myMobilePrivateKey;
		case PC:
			return myPcPrivateKey;
		case OFFLINE:
			return myOfflinePrivateKey;
		default:
			return null;
		}
	}

	public PublicKey getMyPublicKey(KeyType type) {
		switch (type) {
		case MOBILE:
			return myMobilePublicKey;
		case PC:
			return myPcPublicKey;
		case OFFLINE:
			return myOfflinePublicKey;
		default:
		}
		return null;
	}

	public KeyType getMyStandardKeyType() {
		return myStandardKeyType;
	}

	public byte[] decryptByMyStandardPrivateKey(byte[] encrypted) {
		return Glb.getUtil().decrypt(getMyStandardPrivateKey(), encrypted);
	}

	public byte[] decryptByPrivateKey(KeyType type, byte[] encrypted) {
		return Glb.getUtil().decrypt(getMyPrivateKey(type), encrypted);
	}

	private PrivateKey getMyStandardPrivateKey() {
		return getMyPrivateKey(myStandardKeyType);
	}

	public PublicKey getMyStandardPublicKey() {
		return getMyPublicKey(myStandardKeyType);
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

	/**
	 * @param prefix			鍵タイプ
	 * @return				各鍵への署名データ
	 * @throws IOException
	 */
	public String getSignBase64(KeyType targetKey, KeyType by)
			throws IOException {
		return Glb.getUtil().readAll(f.getSignKeyPath(targetKey, by));
	}

	public void init() {
		//		loc = loadLoc();
		loadTenyuConf();
	}

	public void init2(String password) {
		init2(toBytes(password));
	}

	private byte[] toBytes(String s) {
		try {
			return s.getBytes(Glb.getConst().getCharsetPassword());
		} catch (UnsupportedEncodingException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 鍵をセットアップする。鍵の作成や読み込みはパスワードを必要とする。
	 * パスワードはアプリ起動時にダイアログを通じて入力されるが、
	 * init()はその時点で実行済みであるのに対してinit2()はその後に実行される。
	 * パスワードの必要性がメソッドを分けている。
	 * @param password	秘密鍵暗号化に使われるパスワード
	 */
	public void init2(byte[] password) {
		//パスワードをハッシュ値にする
		//そうしてもハッシュ値さえわかれば復号化できてしまうが
		//ユーザーがいろいろなサイトでパスワードを使いまわしていた場合流出時の被害を抑えれる
		password = Glb.getUtil().hashSecure(password);
		setupKeys(password);
		loadKeys(password);

		//自分は作者か
		boolean imAuthor = Glb.getConf().imAuthor();
		Glb.getLogger().info("imAuthor=" + imAuthor);
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

	public boolean changeSecretKeyPassword(String newPassword) {
		return changeSecretKeyPassword(toBytes(newPassword));
	}

	public boolean changeSecretKeyPassword(byte[] newPassword) {
		if (!isLoadedKeys()) {
			Glb.getLogger().error("Not loaded keys", new Exception());
			return false;
		}

		writeToFile(f.getPrivateKeyPath(KeyType.PC),
				f.getPrivateKeyPath(KeyType.PC, getDate()),
				myPcPrivateKey.getEncoded(), newPassword);
		writeToFile(f.getPrivateKeyPath(KeyType.MOBILE),
				f.getPrivateKeyPath(KeyType.MOBILE, getDate()),
				myMobilePrivateKey.getEncoded(), newPassword);
		writeToFile(f.getPrivateKeyPath(KeyType.OFFLINE),
				f.getPrivateKeyPath(KeyType.OFFLINE, getDate()),
				myOfflinePrivateKey.getEncoded(), newPassword);

		return true;
	}

	public boolean isLoadedKeys() {
		return myPcPrivateKey != null && myMobilePrivateKey != null
				&& myOfflinePrivateKey != null;
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

	/**
	 * @return	各種鍵の作成済みフラグがあるか
	 */
	public boolean isKeyGenerated() {
		File generated = new File(f.getKeyGenerated());
		return generated.exists();
	}

	private String getDate() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	}

	/**
	 * PC、MOBILE、OFFの全鍵ペアが存在する状態にする。
	 * 無ければ作成し、あれば作成しない。
	 * @param password
	 */
	public void setupKeys(byte[] password) {
		try {
			//鍵が未作成なら
			if (!isKeyGenerated()) {
				String date = getDate();

				//作成済みの鍵があればそれを使う
				//PC鍵やモバイル鍵が流出した場合、オフライン鍵を残して
				//他を削除して再実行することで再作成される。

				//存在しない場合だけ作成するという条件分岐があることで
				//generateKeyAndWriteToFileのリネーム機能は無意味になっている。

				KeyPair pc = load(KeyType.PC, password);
				if (pc == null)
					pc = generateKeyAndWriteToFile(KeyType.PC, date, password);
				KeyPair mobile = load(KeyType.MOBILE, password);
				if (mobile == null)
					mobile = generateKeyAndWriteToFile(KeyType.MOBILE, date,
							password);
				KeyPair off = load(KeyType.OFFLINE, password);
				if (off == null)
					off = generateKeyAndWriteToFile(KeyType.OFFLINE, date,
							password);

				Glb.getLogger().info(Lang.START_SIGN_KEY);
				signKey(KeyType.PC, KeyType.OFFLINE,
						pc.getPublic().getEncoded(), off.getPrivate(), date);
				signKey(KeyType.MOBILE, KeyType.OFFLINE,
						mobile.getPublic().getEncoded(), off.getPrivate(),
						date);
				signKey(KeyType.OFFLINE, KeyType.PC,
						off.getPublic().getEncoded(), pc.getPrivate(), date);
				signKey(KeyType.OFFLINE, KeyType.MOBILE,
						off.getPublic().getEncoded(), mobile.getPrivate(),
						date);

				//作成済みフラグを作成
				File generated = f.get((f.getKeyGenerated()));
				if (!f.create(Paths.get(f.getKeyGenerated()), null, true)) {
					Glb.getLogger().error(
							"Failed to create generated flag file",
							new Exception());
				}
				//リリース番号を書き込む
				//もしkeyフォルダの仕様が変わってもこの情報を頼りに修正できる可能性がある
				try (FileWriter writer = new FileWriter(generated)) {
					writer.write(Glb.getConst().getRelease() + "");
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			Glb.getApp().stop();
		}

	}

	/**
	 * @param type		KeyType
	 * @param password	秘密鍵暗号化のパスワード
	 * @return			鍵ペア
	 */
	private KeyPair load(KeyType type, byte[] password) {
		try {
			byte[] pub = loadPub(type);
			byte[] pri = loadPri(type, password);
			Util u = Glb.getUtil();
			KeyFactory kf = u.getKf();
			return new KeyPair(u.getPub(kf, pub), u.getPri(kf, pri));
		} catch (NoSuchFileException e) {
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * ファイルシステムから鍵を読み込み、検証し、オブジェクトに設定する。
	 */
	public void loadKeys(byte[] password) {
		//設定前にリセット
		myOfflinePublicKey = null;
		myPcPublicKey = null;
		myMobilePublicKey = null;

		myPcPrivateKey = null;
		myMobilePrivateKey = null;
		myOfflinePrivateKey = null;

		myPcKeySignByOffB = null;
		myMobileKeySignByOffB = null;
		myOffKeySignByMobB = null;
		myOffKeySignByPcB = null;

		try {
			//各鍵ペアをファイルから読み取る
			KeyPair pc = load(KeyType.PC, password);
			KeyPair mobile = load(KeyType.MOBILE, password);
			//KeyPair off = load(KeyType.OFFLINE, password);

			//オフライン秘密鍵は無くても動作すべき
			//オフライン公開鍵だけ設定する
			byte[] offPubB = loadPub(KeyType.OFFLINE);
			PublicKey offPub = Glb.getUtil().getPub(offPubB);

			//nullだったら例外
			if (pc == null || mobile == null || offPub == null)
				throw new Exception();

			//オフライン鍵によるPC鍵とモバイル鍵への署名をファイルから読み取る
			byte[] signPcB = loadSign(KeyType.PC, KeyType.OFFLINE);
			byte[] signMobileB = loadSign(KeyType.MOBILE, KeyType.OFFLINE);
			byte[] signOffByPcB = loadSign(KeyType.OFFLINE, KeyType.PC);
			byte[] signOffByMobileB = loadSign(KeyType.OFFLINE, KeyType.MOBILE);

			//オフライン秘密鍵の署名をオフライン公開鍵で検証。
			//署名対象はpc鍵とmobile鍵
			if (!verifyKeys(pc.getPublic(), mobile.getPublic(), offPub, signPcB,
					signMobileB, signOffByPcB, signOffByMobileB))
				throw new IllegalStateException();

			//公開鍵と秘密鍵のペアは正しいか。署名と検証で確認する。
			boolean validPc = validatePubPri(pc);
			if (!validPc) {
				Glb.getLogger().error(
						Lang.CONF_PC_KEY_PAIR + " " + Lang.ERROR_INVALID);
			}

			boolean validMobile = validatePubPri(mobile);
			if (!validMobile) {
				Glb.getLogger().error(
						Lang.CONF_MOBILE_KEY_PAIR + " " + Lang.ERROR_INVALID);
			}

			if (!validPc || !validMobile) {
				throw new IllegalStateException();
			}

			// オブジェクトに設定
			myOfflinePublicKey = offPub;
			myPcPublicKey = pc.getPublic();
			myMobilePublicKey = mobile.getPublic();

			myPcPrivateKey = pc.getPrivate();
			myMobilePrivateKey = mobile.getPrivate();

			myPcKeySignByOffB = signPcB;
			myMobileKeySignByOffB = signMobileB;
			myOffKeySignByMobB = signOffByMobileB;
			myOffKeySignByPcB = signOffByPcB;

			//オフライン秘密鍵もあれば読み込む
			try {
				byte[] offPriB = loadPri(KeyType.OFFLINE, password);
				if (offPriB != null && offPriB.length > 0) {
					PrivateKey offPri = Glb.getUtil().getPri(offPriB);

					KeyPair off = new KeyPair(offPub, offPri);
					boolean validOff = validatePubPri(off);
					if (validOff) {
						myOfflinePrivateKey = off.getPrivate();
					} else {
						Glb.getLogger().error(Lang.CONF_OFF_KEY_PAIR + " "
								+ Lang.ERROR_INVALID);
					}
				}
			} catch (Exception e) {
				Glb.debug(e);
				//オフライン秘密鍵は無くても問題無し
			}

		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	/*
		private Locale loadLoc() {
			return Locale.forLanguageTag(System.getProperty("user.language"));
		}
		*/

	public static boolean verifyKeys(byte[] pcPub, byte[] mobPub, byte[] offPub,
			byte[] signPcB, byte[] signMobileB, byte[] signOffByPcB,
			byte[] signOffByMobileB) {
		Util u = Glb.getUtil();
		try {
			return verifyKeys(u.getPub(pcPub), u.getPub(mobPub),
					u.getPub(offPub), signPcB, signMobileB, signOffByPcB,
					signOffByMobileB);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public static boolean verifyKeys(PublicKey pcPub, PublicKey mobPub,
			PublicKey offPub, byte[] signPcB, byte[] signMobileB,
			byte[] signOffByPcB, byte[] signOffByMobileB) {
		Util u = Glb.getUtil();
		boolean pubPcByOff = u.verify(getSignKeyNominal(), signPcB, offPub,
				pcPub.getEncoded());
		if (!pubPcByOff) {
			Glb.getLogger().error(Lang.CONF_PC_SIGN + " " + Lang.ERROR_INVALID);
		}

		boolean pubMobileByOff = u.verify(getSignKeyNominal(), signMobileB,
				offPub, mobPub.getEncoded());
		if (!pubMobileByOff) {
			Glb.getLogger()
					.error(Lang.CONF_MOBILE_SIGN + " " + Lang.ERROR_INVALID);
		}

		//オフ公開鍵をpc秘密鍵で署名
		boolean offByPc = u.verify(getSignKeyNominal(), signOffByPcB, pcPub,
				offPub.getEncoded());
		if (!offByPc) {
			Glb.getLogger()
					.error(Lang.CONF_OFF_SIGN_BY_PC + " " + Lang.ERROR_INVALID);
		}

		//オフ公開鍵をmobile秘密鍵で署名
		boolean offByMobile = u.verify(getSignKeyNominal(), signOffByMobileB,
				mobPub, offPub.getEncoded());
		if (!offByMobile) {
			Glb.getLogger().error(
					Lang.CONF_OFF_SIGN_BY_MOB + " " + Lang.ERROR_INVALID);
		}

		//全ての検証を通過したか
		return pubPcByOff && pubMobileByOff;
	}

	private byte[] loadPri(KeyType type, byte[] password) throws Exception {
		String base64 = Glb.getUtil().readAll(f.getPrivateKeyPath(type));
		byte[] encrypted = Base64.getDecoder().decode(base64);
		return Glb.getUtil().crypt(false, password, encrypted);
	}

	private byte[] loadPub(KeyType type) throws IOException {
		String base64 = Glb.getUtil().readAll(f.getPublicKeyPath(type));
		return Base64.getDecoder().decode(base64);
	}

	private byte[] loadSign(KeyType targetKey, KeyType by) throws IOException {
		return Base64.getDecoder().decode(getSignBase64(targetKey, by));
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

		String localIpcPortStr = tenyuconf.getProperty("localIpcPort");
		try {
			localIpcPort = Integer.parseInt(localIpcPortStr);
		} catch (Exception e) {
			Glb.getLogger().error(
					Lang.LOCALIPCPORT + Lang.ERROR_INVALID.toString(), e);
			System.exit(1);
		}
		Glb.getLogger().info(Lang.LOCALIPCPORT + " " + getLocalIpcPort());

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

	public byte[] sign(String nominal, byte[] b) throws IOException,
			InvalidKeySpecException, NoSuchAlgorithmException {
		return sign(nominal, getMyStandardKeyType(), b);
	}

	/**
	 * @param nominal	署名対象データのアプリ固有の型、または名目
	 * @param keyType	署名に使う鍵の種類
	 * @param b			署名対象データ
	 * @return			署名
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] sign(String nominal, KeyType keyType, byte[] b)
			throws IOException, InvalidKeySpecException,
			NoSuchAlgorithmException {
		if (b == null)
			return null;
		Util u = Glb.getUtil();
		switch (keyType) {
		case MOBILE:
			return u.sign(nominal, b, myMobilePrivateKey);
		case PC:
			return u.sign(nominal, b, myPcPrivateKey);
		case OFFLINE:
			return u.sign(nominal, b, myOfflinePrivateKey);
		default:
			return null;
		}
	}

	/**
	 * 電子署名の名目
	 */
	/*
	private static final String signKeyNominal = Conf.class.getTypeName()
			+ "#signKey()";
	*/

	/**
	 * 署名の名目にメソッド名を使用する
	 * @return	signKey()で使用する署名の名目
	 */
	public static String getSignKeyNominal() {
		//完全修飾名だとパッケージ名を変えれないのでsimpleで
		return Conf.class.getSimpleName() + "#signKey";
	}

	/**
	 * @param targetName	署名対象の名前
	 * @param target		署名対象
	 * @param pri			署名に使う秘密鍵
	 * @param backupPrefix	既に署名ファイルが存在した場合これを接頭辞として
	 * バックアップする。
	 */
	private void signKey(KeyType targetKey, KeyType by, byte[] target,
			PrivateKey pri, String backupPrefix) {
		String signBase64 = Base64.getEncoder().encodeToString(
				Glb.getUtil().sign(getSignKeyNominal(), target, pri));
		File signBase64File = f.get(f.getSignKeyPath(targetKey, by));
		if (signBase64File.exists()) {
			String path = f.getSignKeyPath(targetKey, by, backupPrefix);
			f.move(signBase64File.toPath(), Paths.get(path));
		}
		try (FileWriter fw = new FileWriter(signBase64File);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);) {
			pw.print(signBase64);
			pw.flush();
		} catch (IOException e1) {
			Glb.getLogger().error("", e1);
			System.exit(1);
		}
	}

	private boolean validatePubPri(KeyPair pair) {
		byte[] test = new byte[64];
		new Random().nextBytes(test);
		try {
			Signature s = Signature
					.getInstance(Glb.getConst().getSignatureAlgorithm());

			s.initSign(pair.getPrivate());
			s.update(test);
			byte[] testSign = s.sign();
			s.initVerify(pair.getPublic());
			s.update(test);
			return s.verify(testSign);
		} catch (NoSuchAlgorithmException | InvalidKeyException
				| SignatureException e) {
			Glb.getLogger().error("", e);
			return false;
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

	public boolean isSecureUser() {
		return secureUser;
	}

	/**
	 * Confに自分の鍵がロードされた後に呼ぶ。
	 * 鍵のロードに成功する事は正しい秘密鍵が伴っている事を意味する、という前提。
	 *
	 * @return　自分は作者か
	 */
	public boolean imAuthor() {
		/*
		User author = Glb.getConst().getAuthor();
		//オフライン秘密鍵はオフラインにしておくので、PC鍵とモバイル鍵だけで作者認定
		boolean r1 = Arrays.equals(myPcPublicKey.getEncoded(),
				author.getPcPublicKey())
				&& Arrays.equals(myMobilePublicKey.getEncoded(),
						author.getMobilePublicKey());
								//PC鍵とモバイル鍵は更新する場合があるので、オフライン鍵だけが正しい場合も作者
		boolean r2 = Arrays.equals(myOfflinePublicKey.getEncoded(),
				author.getOfflinePublicKey());
		//いずれかを満たせばtrue
		return r1 || r2;

						*/
		for (String authorPubBase64 : Glb.getConst().getAuthorPublicKeys()) {
			byte[] authorPub = Base64.getDecoder().decode(authorPubBase64);
			if (Arrays.equals(authorPub, myOfflinePublicKey.getEncoded())) {
				return true;
			}
		}
		return false;
	}

	public byte[] getMyPcKeySignByOffB() {
		return myPcKeySignByOffB;
	}

	public byte[] getMyMobileKeySignByOffB() {
		return myMobileKeySignByOffB;
	}

	public byte[] getMyOffKeySignByMobB() {
		return myOffKeySignByMobB;
	}

	public byte[] getMyOffKeySignByPcB() {
		return myOffKeySignByPcB;
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

}
