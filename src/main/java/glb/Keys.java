package glb;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.text.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.util.*;

/**
 * Tenyu基盤ソフトウェアの公開鍵等を管理するクラス
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Keys {
	/**
	 * Confとの相互参照
	 */
	private Conf cf;

	public Keys(Conf cf) {
		this.cf = cf;
	}

	/**
	 * セキュアユーザーか
	 * {@link User}に詳細
	 */
	private boolean secureUser = false;

	private PublicKey myOfflinePublicKey;
	private PublicKey myPcPublicKey;
	private PublicKey myMobilePublicKey;

	/**
	 * この設定を変えると通信メッセージにおいて使用される鍵が変わる。
	 *
	 */
	private KeyType myStandardKeyType = KeyType.PC;

	//秘密鍵はgetter禁止。ここで署名機能を提供するだけ。
	//秘密鍵情報はKeys及びConfから出ない。
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

	public byte[] sign(String nominal, byte[] b) throws IOException,
			InvalidKeySpecException, NoSuchAlgorithmException {
		return sign(nominal, getMyStandardKeyType(), b);
	}

	/**
	 * @return	各種鍵の作成済みフラグがあるか
	 */
	public boolean isKeyGenerated() {
		File generated = new File(cf.getFile().getKeyGenerated());
		return generated.exists();
	}

	private String getDate() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	}

	public KeyType getMyStandardKeyType() {
		return myStandardKeyType;
	}

	public PublicKey getMyStandardPublicKey() {
		return getMyPublicKey(myStandardKeyType);
	}

	public void init2(String password) {
		init2(toBytes(password));
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
		boolean imAuthor = imAuthor();
		Glb.getLogger().info("imAuthor=" + imAuthor);
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
				File generated = Glb.getFile()
						.get((cf.getFile().getKeyGenerated()));
				if (!cf.getFile().create(
						Paths.get(cf.getFile().getKeyGenerated()), null,
						true)) {
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
		reset();

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
		File signBase64File = Glb.getFile()
				.get(cf.getFile().getSignKeyPath(targetKey, by));
		if (signBase64File.exists()) {
			String path = cf.getFile().getSignKeyPath(targetKey, by,
					backupPrefix);
			cf.getFile().move(signBase64File.toPath(), Paths.get(path));
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

	/**
	 * 電子署名の名目
	 */
	/*
	private static final String signKeyNominal = Conf.class.getTypeName()
			+ "#signKey()";
	*/

	/**
	 * 署名の名目にメソッド名を使用する。
	 * 署名の名目というアイデアは、
	 * 様々な場面で署名が行われた場合にどのような意味で署名されたのか混同する可能性が生じるので、
	 * 名目文字列を加えて署名している。
	 *
	 * @return	signKey()で使用する署名の名目
	 */
	public static String getSignKeyNominal() {
		//リファクタリングでConfからKeysクラスが派生してインターフェースが移動したが
		//署名名目はConfがそのまま使われている。
		//変更した場合の影響が面倒なのと、今でもKeysはConf系という位置づけ（Conf以外でメンバー変数にならない）
		//なので問題ない。Keysはほぼ常にConf#getKeys()を通じて利用される。

		//完全修飾名だとパッケージ名を変えれないのでsimpleで
		return Conf.class.getSimpleName() + "#signKey";
	}

	private byte[] loadPri(KeyType type, byte[] password) throws Exception {
		String base64 = Glb.getUtil()
				.readAll(cf.getFile().getPrivateKeyPath(type));
		byte[] encrypted = Base64.getDecoder().decode(base64);
		return Glb.getUtil().crypt(false, password, encrypted);
	}

	private byte[] loadPub(KeyType type) throws IOException {
		String base64 = Glb.getUtil()
				.readAll(cf.getFile().getPublicKeyPath(type));
		return Base64.getDecoder().decode(base64);
	}

	private byte[] loadSign(KeyType targetKey, KeyType by) throws IOException {
		return Base64.getDecoder().decode(getSignBase64(targetKey, by));
	}

	/**
	 * @param prefix			鍵タイプ
	 * @return				各鍵への署名データ
	 * @throws IOException
	 */
	public String getSignBase64(KeyType targetKey, KeyType by)
			throws IOException {
		return Glb.getUtil()
				.readAll(cf.getFile().getSignKeyPath(targetKey, by));
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

	private byte[] toBytes(String s) {
		try {
			return s.getBytes(Glb.getConst().getCharsetPassword());
		} catch (UnsupportedEncodingException e) {
			Glb.getLogger().error("", e);
			return null;
		}
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
			if (!cf.getFile().isAppPathRelative(path)
					|| !cf.getFile().isAppPathRelative(backupPath)) {
				Glb.getLogger().warn("", new Exception("invalid path"));
				return false;
			}

			File file = cf.getFile().get(path);
			if (file.exists()) {
				cf.getFile().move(file.toPath(), Paths.get(backupPath));
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

			writeToFile(cf.getFile().getPublicKeyPath(type),
					cf.getFile().getPublicKeyPath(type, backupPrefix),
					pair.getPublic().getEncoded());
			writeToFile(cf.getFile().getPrivateKeyPath(type),
					cf.getFile().getPrivateKeyPath(type, backupPrefix),
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

	public byte[] decryptByMyStandardPrivateKey(byte[] encrypted) {
		return Glb.getUtil().decrypt(getMyStandardPrivateKey(), encrypted);
	}

	public byte[] decryptByPrivateKey(KeyType type, byte[] encrypted) {
		return Glb.getUtil().decrypt(getMyPrivateKey(type), encrypted);
	}

	private PrivateKey getMyStandardPrivateKey() {
		return getMyPrivateKey(myStandardKeyType);
	}

	public boolean changeSecretKeyPassword(String newPassword) {
		return changeSecretKeyPassword(toBytes(newPassword));
	}

	public boolean changeSecretKeyPassword(byte[] newPassword) {
		if (!isLoadedKeys()) {
			Glb.getLogger().error("Not loaded keys", new Exception());
			return false;
		}
		String date = getDate();

		writeToFile(cf.getFile().getPrivateKeyPath(KeyType.PC),
				cf.getFile().getPrivateKeyPath(KeyType.PC, date),
				myPcPrivateKey.getEncoded(), newPassword);
		writeToFile(cf.getFile().getPrivateKeyPath(KeyType.MOBILE),
				cf.getFile().getPrivateKeyPath(KeyType.MOBILE, date),
				myMobilePrivateKey.getEncoded(), newPassword);
		writeToFile(cf.getFile().getPrivateKeyPath(KeyType.OFFLINE),
				cf.getFile().getPrivateKeyPath(KeyType.OFFLINE, date),
				myOfflinePrivateKey.getEncoded(), newPassword);

		return true;
	}

	public boolean isLoadedKeys() {
		return myPcPrivateKey != null && myMobilePrivateKey != null
				&& myOfflinePrivateKey != null;
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

	/**
	 * 鍵がロードされた後に呼ぶ。
	 * 鍵のロードに成功する事は正しい秘密鍵が伴っている事を意味し、
	 * 作者の公開鍵が定数として記録されているので、
	 * この鍵オブジェクトが作者のものかを判定できる。
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

	public void reset() {
		myOfflinePublicKey = null;
		myPcPublicKey = null;
		myMobilePublicKey = null;
	}

	public PublicKey getMyOfflinePublicKey() {
		return myOfflinePublicKey;
	}

	public void setMyOfflinePublicKey(PublicKey myOfflinePublicKey) {
		this.myOfflinePublicKey = myOfflinePublicKey;
	}

	public PublicKey getMyPcPublicKey() {
		return myPcPublicKey;
	}

	public void setMyPcPublicKey(PublicKey myPcPublicKey) {
		this.myPcPublicKey = myPcPublicKey;
	}

	public PublicKey getMyMobilePublicKey() {
		return myMobilePublicKey;
	}

	public void setMyMobilePublicKey(PublicKey myMobilePublicKey) {
		this.myMobilePublicKey = myMobilePublicKey;
	}

	public byte[] getMyPcKeySignByOffB() {
		return myPcKeySignByOffB;
	}

	public void setMyPcKeySignByOffB(byte[] myPcKeySignByOffB) {
		this.myPcKeySignByOffB = myPcKeySignByOffB;
	}

	public byte[] getMyMobileKeySignByOffB() {
		return myMobileKeySignByOffB;
	}

	public void setMyMobileKeySignByOffB(byte[] myMobileKeySignByOffB) {
		this.myMobileKeySignByOffB = myMobileKeySignByOffB;
	}

	public byte[] getMyOffKeySignByPcB() {
		return myOffKeySignByPcB;
	}

	public void setMyOffKeySignByPcB(byte[] myOffKeySignByPcB) {
		this.myOffKeySignByPcB = myOffKeySignByPcB;
	}

	public byte[] getMyOffKeySignByMobB() {
		return myOffKeySignByMobB;
	}

	public void setMyOffKeySignByMobB(byte[] myOffKeySignByMobB) {
		this.myOffKeySignByMobB = myOffKeySignByMobB;
	}

	public void setMyStandardKeyType(KeyType myStandardKeyType) {
		this.myStandardKeyType = myStandardKeyType;
	}

	private void setMyPcPrivateKey(PrivateKey myPcPrivateKey) {
		this.myPcPrivateKey = myPcPrivateKey;
	}

	private void setMyMobilePrivateKey(PrivateKey myMobilePrivateKey) {
		this.myMobilePrivateKey = myMobilePrivateKey;
	}

	private void setMyOfflinePrivateKey(PrivateKey myOfflinePrivateKey) {
		this.myOfflinePrivateKey = myOfflinePrivateKey;
	}

	public boolean isSecureUser() {
		return secureUser;
	}

	public void setSecureUser(boolean secureUser) {
		this.secureUser = secureUser;
	}
}
