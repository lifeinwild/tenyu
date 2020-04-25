package glb.util;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.validator.routines.*;
import org.apache.logging.log4j.*;
import org.bouncycastle.jce.provider.*;

import com.carrotsearch.sizeof.*;
import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.ibm.icu.text.*;

import bei7473p5254d69jcuat.tenyu.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * @author exceptiontenyu@gmail.com
 *
 */
public class Util {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public boolean validateReference(Collection<? extends Storable> storables,
			ValidationResult r, Transaction txn) {
		return validateCollection(storables, r, e -> {
			try {
				return e.validateReference(r, txn);
			} catch (Exception e1) {
				Glb.getLogger().error("", e1);
				return false;
			}
		});
	}

	public boolean validateAtDelete(Collection<? extends Storable> storables,
			ValidationResult r) {
		return validateCollection(storables, r, e -> e.validateAtDelete(r));
	}

	public boolean validateAtUpdate(Collection<? extends Storable> storables,
			ValidationResult r) {
		return validateCollection(storables, r, e -> e.validateAtUpdate(r));
	}

	/**
	 * @param d
	 * @return	dの規模。Double.MIN_VALUEを1とし、量が１０倍になるごとに+1され、
	 * Double.MAX_VALUEで632となる。
	 * ある程度コストがかかり、MAX_VALUEを入力して１００万回実行に８秒程度。
	 */
	public int getScaleForNumber(double d) {
		int r = 0;
		for (; d > 0; r++) {
			d /= 10;
		}
		return r;
	}

	/**
	 * シグモイド関数
	 * @param x
	 * @return
	 */
	public double sigmoid(double x) {
		return (1 / (1 + Math.pow(Math.E, (-1 * x))));
	}

	/**
	 * 多くの場合{@link Storable#validateAtUpdateChange(ValidationResult, Object)}は
	 * 単にサブクラスの実装を呼び出すだけでは済まない。
	 * ホストクラス側にサブクラスのメンバー変数を意識した検証ロジックを書く必要がある。
	 * それがvalidate
	 *
	 * @param l
	 * @param r
	 * @param validate
	 * @return
	 */
	public <V> boolean validateAtUpdateChange(Collection<V> l,
			ValidationResult r, Function<V, Boolean> validate) {
		for (V e : l) {
			if (!validate.apply(e)) {
				return false;
			}
		}
		return true;
	}

	public boolean validateAtCreate(Collection<? extends Storable> storables,
			ValidationResult r) {
		return validateCollection(storables, r, e -> e.validateAtCreate(r));
	}

	private boolean validateCollection(Collection<? extends Storable> storables,
			ValidationResult r, Function<Storable, Boolean> f) {
		for (Storable e : storables) {
			if (!f.apply(e)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * NominalSignatureベースの汎用署名メソッド。
	 *
	 * @param set	署名データを書き込む
	 * @param getNominal	署名名目を取得する
	 * @param getSignTarget	署名対象データを取得する
	 * @return	署名及び署名データの書き込みに成功したか
	 */
	public boolean sign(Function<NominalSignature, Boolean> set,
			Supplier<String> getNominal, Supplier<byte[]> getSignTarget) {
		try {
			//作成される電子署名データ
			NominalSignature sign = new NominalSignature();

			//情報取得
			byte[] signTarget = getSignTarget.get();
			if (signTarget == null)
				return false;
			String nominal = getNominal.get();
			if (nominal == null)
				return false;

			//署名
			if (!sign.sign(nominal, signTarget))
				return false;

			//署名に成功した場合
			if (!set.apply(sign))
				return false;
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * 文字列化して返す
	 * @param l	対象リスト
	 * @return	lの文字列
	 */
	public String toStringUtil(List<byte[]> l) {
		StringBuilder r = new StringBuilder();
		for (byte[] b : l) {
			r.append(Arrays.toString(b));
		}
		return r.toString();
	}

	public LocalDate getLocalDate(long date, ZoneOffset offset) {
		return LocalDateTime.ofEpochSecond(date, 1000, offset).toLocalDate();
	}

	/**
	 * @param date		エポックミリ秒
	 * @param offset	タイムゾーン
	 * @return	日時 "yyyy/MM/dd HH:mm:ss"
	 */
	public String getLocalDateStr(long d, ZoneId z) {
		Instant in = Instant.ofEpochMilli(d);
		DateTimeFormatter f = DateTimeFormatter
				.ofPattern("yyyy/MM/dd HH:mm:ss");
		return LocalDateTime.ofInstant(in, z).format(f);
	}

	public String getLocalDateStr(long d) {
		return getLocalDateStr(d, ZoneId.systemDefault());
	}

	public long getEpochMilli() {
		return System.currentTimeMillis();
	}

	/**
	 * 年と月が同じ限り同じ値を返す。UTC
	 * @return	日以下が無視されたミリ秒
	 */
	public long getEpochMilliIgnoreDays() {
		LocalDateTime now = java.time.LocalDateTime.now(Clock.systemUTC());
		long milli = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 1, 1)
				.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
		return milli;
	}

	/**
	 * @return	時以下が無視されたミリ秒
	 */
	public long getEpochMilliIgnoreHours() {
		LocalDateTime now = java.time.LocalDateTime.now(Clock.systemUTC());
		long milli = LocalDateTime
				.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 1, 1)
				.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
		return milli;
	}

	/**
	 * このメソッドを使用すると多数のノードで全く同じ日時値をそれぞれ自力で（通信無しで）取得できる。
	 * @param expectedSec	この秒の前後で必ず全ノードで一致する（ノード毎に値がずれるタイミングをこの秒から離れた秒にする）。
	 * @return	秒以下が無視された全ノードで値が一致する現在日時のミリ秒表現
	 * ノード間の時計のずれが３０秒以内であれば全ノードで一致する。
	 */
	public long getEpochMilliIgnoreSeconds(int expectedSec) {
		return getEpochMilliIgnoreSeconds(expectedSec, 30);
	}

	/**
	 * @param expectedSec
	 * @param tolerance	これを大きくすると全ノードで返値が一致する確率が上がる。
	 * 言い換えればノード間の時計のずれに強くなる。しかし、
	 * 返値が変化する周期が長くなるので、例えば１分に１回返値が変化してほしいなら
	 * 30以下を指定する必要がある。
	 *
	 * @return
	 */
	public long getEpochMilliIgnoreSeconds(int expectedSec, int tolerance) {
		LocalDateTime now = java.time.LocalDateTime.now(Clock.systemUTC());
		int min = now.getMinute();
		int sec = now.getSecond();
		int toleranceMin = expectedSec - tolerance;
		if (toleranceMin < 0) {
			int threshold = 60 + toleranceMin;
			if (sec > threshold) {
				min++;
			}
		}
		int toleranceMax = expectedSec + tolerance;
		if (toleranceMax > 60) {
			int threshold = toleranceMax - 60;
			if (sec < threshold) {
				min--;
			}
		}
		long milli = LocalDateTime
				.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
						now.getHour(), min, 0)
				.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
		return milli;
	}

	public LocalDateTime nowDate() {
		return LocalDateTime.now(Clock.systemUTC());
	}

	/**
	 * @param start	ここから
	 * @return	経過時間
	 */
	public long elapsed(long start) {
		long now = Glb.getUtil().now();
		long elapsed = now - start;
		return elapsed;
	}

	/**
	 * TODO ソースコード上のSystem.currentTimeMillis()をこれに置き換えるべきか？
	 * やるなら置換で出来そう。
	 *
	 * @return	現在日時
	 */
	public long now() {
		return LocalDateTime.now(Clock.systemUTC()).atZone(ZoneId.of("UTC"))
				.toInstant().toEpochMilli();
	}

	public long getEpochMilliSpecifiedSecond(LocalDateTime date, int second) {
		return LocalDateTime
				.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
						date.getHour(), date.getMinute(), second, 0)
				.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
	}

	/**
	 * @return	秒を指定したものにして現在日時をミリ秒で返す。UTC
	 */
	public long getEpochMilliSpecifiedSecond(int second) {
		LocalDateTime now = java.time.LocalDateTime.now(Clock.systemUTC());
		return getEpochMilliSpecifiedSecond(now, second);
	}

	/**
	 * javaのオブジェクトサイズの取得は簡単ではない。
	 * jolはoracleJDKかcorrettoじゃないと動作しない。
	 * twitterのobjectsizeはlibericaで動作しなかった。
	 * ソースコード全体でサイズ取得メソッドを一元化して、
	 * 簡単にここで変更できるようにする必要がある。
	 *
	 * @param o
	 * @return
	 */
	public long sizeOf(Object o) {
		return RamUsageEstimator.sizeOfAll(o);
	}

	/**
	 * 渡されたコードを実行してその実行時間をログに出力する。
	 * 主にテストで使う事になるが便利過ぎるのでここに入れた。
	 *
	 * @param s
	 * @param r
	 * @throws Exception
	 */
	public void benchmark(String s, Runnable r) throws Exception {
		long start = System.currentTimeMillis();
		r.run();
		long end = System.currentTimeMillis();
		System.out.println(s + " " + (end - start) + "ms");//50ms
	}

	/**
	 * 末尾が/じゃなければ/をつける
	 * @param s
	 * @return
	 */
	public String addSlashIfNot(String s) {
		if (s == null)
			return null;
		if (!s.substring(s.length() - 1).equals("/")) {
			s = s + "/";
		}
		return s;
	}

	/**
	 * {@link IndividualityObject}に似たメソッドがあるがこれは
	 * ValidationResultを使わないバージョン。pathのみを想定
	 *
	 * @param path
	 * @param pathLenMax
	 * @return
	 */
	public boolean validatePath(String path, int pathLenMax) {
		if (validatePathInternal(path, pathLenMax)) {
			return true;
		} else {
			String log = "too long path";
			if (path.length() < pathLenMax)
				log = path;
			Glb.getLogger().warn("invalid path=" + log, new Exception());
			return false;
		}
	}

	private boolean validatePathInternal(String path, int pathLenMax) {
		if (path == null || path.length() == 0) {
			return false;
		}
		if (path.length() < 1) {
			return false;
		}
		if (path.length() > pathLenMax) {
			return false;
		}
		if (path.contains("..") || path.startsWith("/")) {
			return false;
		}

		if (!validateTextAllCtrlChar(path))
			return false;

		return true;
	}

	/**
	 * トリムされているか、ユニコード最適化されているか
	 * CR,LF,HT等も禁止
	 * @param n
	 * @param text
	 * @param vr
	 * @return
	 */
	public boolean validateTextAllCtrlChar(String text) {
		if (!validateTextCommon(text))
			return false;
		//制御文字禁止
		if (hasControlChar(text)) {
			return false;
		}
		return true;
	}

	/**
	 * トリムされているか、ユニコード最適化されているか
	 * @param n
	 * @param text
	 * @param vr
	 * @return
	 */
	private boolean validateTextCommon(String text) {
		if (text == null)
			return false;
		if (!text.equals(text.trim())) {
			return false;
		}

		//ユニコード最適化
		String normalized = Normalizer2.getNFKCInstance().normalize(text);
		if (!text.equals(normalized)) {
			return false;
		}

		return true;
	}

	public byte[] toByteArray(Long l1) {
		if (l1 == null)
			return null;
		return toByteArray((long) l1);
	}

	public byte[] toByteArray(long l1) {
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
		buf.putLong(l1);
		return buf.array();
	}

	public byte[] concat(Long l1, Long l2) {
		if (l1 == null || l2 == null)
			return null;
		return concat((long) l1, (long) l2);
	}

	public byte[] concat(Long l1, long l2) {
		if (l1 == null)
			return null;
		return concat((long) l1, l2);
	}

	public byte[] concat(long l1, long l2) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		buffer.putLong(l1);
		buffer.putLong(l2);
		return buffer.array();
	}

	public long getLong1(byte[] twoLong) {
		ByteBuffer buffer = ByteBuffer.wrap(twoLong, 0, Long.BYTES);
		return buffer.getLong();
	}

	public long getLong2(byte[] twoLong) {
		ByteBuffer buffer = ByteBuffer.wrap(twoLong, Long.BYTES, Long.BYTES);
		return buffer.getLong();
	}

	/**
	 * @param when	input経過時間	output待機を続行するならtrue
	 */
	public void waitFor(Function<Long, Boolean> when, long interval,
			long forciblyTimeout) {
		long start = System.currentTimeMillis();
		long elapsed = 0;
		while (when.apply(elapsed)) {
			elapsed = System.currentTimeMillis() - start;
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
			}
			//強制タイムアウト
			if (elapsed > forciblyTimeout) {
				return;
			}
		}
	}

	/**
	 * @param regex	正規表現
	 * @return	正規表現の構文が正しいか
	 */
	public boolean validateRegex(String regex) {
		try {
			Pattern.compile(regex);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 制御文字があるかチェックする。
	 * LF,CR,HT等テキストエリアで使われる文字を除く。
	 * @param s	対象文字列
	 * @return	制御文字が含まれているか
	 */
	public boolean hasControlCharExceptLF_CR_HT(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == 9 || ch == 10 || ch == 13)
				continue;
			if (Character.isISOControl(ch))
				return true;
		}
		return false;
	}

	public boolean hasControlChar(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isISOControl(ch))
				return true;
		}
		return false;
	}

	/**
	 * ファイルのハッシュ値を計算する
	 * @param p	対象ファイル
	 * @return	ハッシュ値
	 * @throws Exception
	 */
	/*
	public byte[] digestFile(Path p) throws Exception {
		MessageDigest md = Glb.getUtil().getMD();
		byte[] b = Files.readAllBytes(p);
		return md.digest(b);
	}
	*/

	/**
	 * 巨大なファイルでもメモリ消費量が限定的
	 * ファイルのハッシュ値を計算する
	 * @param p	対象ファイル
	 * @return	ハッシュ値
	 */
	public byte[] digestFile(Path path) {
		MessageDigest md = getMDSecure();
		byte[] h = null;
		try (InputStream is = new BufferedInputStream(
				Files.newInputStream(path));
				DigestInputStream dis = new DigestInputStream(is, md)) {
			byte[] buf = new byte[1024 * 100];
			while (dis.read(buf) != -1) {

			}
			h = md.digest();
		} catch (IOException e) {
			Glb.getLogger().error("", e);
		}
		return h;
	}

	/**
	 * TODO:スレッドセーフか確認できなかった。恐らくそうだろう
	 */
	private UrlValidator urlValidator = new UrlValidator(
			UrlValidator.ALLOW_LOCAL_URLS);

	public boolean isValidURL(String url) {
		return url != null && url.length() > 0 && urlValidator.isValid(url);
	}

	public MessageDigest getMDSecure() {
		try {
			return MessageDigest.getInstance(
					Glb.getConst().getDigestAlgorithmSecure(),
					Glb.getConst().getSecurityProvider());
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	public MessageDigest getMDFast() {
		try {
			return MessageDigest.getInstance(
					Glb.getConst().getDigestAlgorithmFast(),
					Glb.getConst().getSecurityProvider());
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	public <T> Collection<T> getExtra(Collection<T> src, List<T> filter) {
		if (src == null)
			return new ArrayList<>();
		if (filter == null)
			return src;
		return getExtra(src, new HashSet<>(filter));
	}

	/**
	 * @param src		ここからfilterが引かれる
	 * @param filter	targetから除外される値の一覧
	 * @return			targetからfilterが除外された一覧
	 */
	public <T> Collection<T> getExtra(Collection<T> src, HashSet<T> filter) {
		if (src == null)
			return new ArrayList<>();
		if (filter == null)
			return src;
		List<T> extra = new ArrayList<>();
		for (T o : src) {
			if (!filter.contains(o)) {
				extra.add(o);
			}
		}
		return extra;
	}

	public <T extends Object> boolean foundIn(List<T> l, T target) {
		boolean found = false;
		for (Object o : l) {
			if (o.equals(target))
				found = true;
		}
		return found;
	}

	/**
	 * @param o1	配列はbyte[]しか対応していない
	 * @param o2
	 * @return	o1とo2が異なるか。両方nullの場合falseなので注意
	 */
	public boolean notEqual(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return false;
		} else if (o1 == null || o2 == null) {
			return true;
		} else if (o1.getClass() != o2.getClass()) {
			return false;
		} else {
			//ここisArray()とObject[]で汎用的に書く事は無理だった。正しく機能しない
			if (o1 instanceof byte[]) {
				return !Arrays.equals((byte[]) o1, (byte[]) o2);
			} else {
				return !o1.equals(o2);
			}
		}
	}

	/**
	 * 衝突率が低い
	 * @param o
	 * @return		oのハッシュ値
	 */
	public byte[] hashSecure(Object o) {
		try {
			MessageDigest md = MessageDigest
					.getInstance(Glb.getConst().getDigestAlgorithmSecure());
			return hash(o, md);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 高速
	 * @param o
	 * @return
	 */
	public byte[] hashFast(Object o) {
		try {
			MessageDigest md = MessageDigest
					.getInstance(Glb.getConst().getDigestAlgorithmFast());
			return hash(o, md);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	private byte[] hash(Object o, MessageDigest md) {
		try {
			byte[] seri = Glb.getUtil().toKryoBytesForPersistence(o);
			return md.digest(seri);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 高速
	 * @param o
	 * @return	oのハッシュ値
	 */
	public byte[] hashFast(byte[] o) {
		try {
			MessageDigest md = MessageDigest
					.getInstance(Glb.getConst().getDigestAlgorithmFast());
			return hash(o, md);
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 衝突率が低い
	 * @param o
	 * @return	oのハッシュ値
	 */
	public byte[] hashSecure(byte[] o) {
		try {
			MessageDigest md = MessageDigest
					.getInstance(Glb.getConst().getDigestAlgorithmSecure());
			return hash(o, md);
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public byte[] hashSecure(byte[]... o) {
		try {
			MessageDigest md = MessageDigest
					.getInstance(Glb.getConst().getDigestAlgorithmSecure());
			byte[] r = null;
			for (byte[] e : o)
				r = hash(e, md);
			return r;
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	private byte[] hash(byte[] o, MessageDigest md) {
		return md.digest(o);
	}

	public boolean isZeroFill(byte[] hashArray) {
		for (byte b : hashArray) {
			if (b != 0)
				return false;
		}
		return true;
	}

	public Signature getSignature() {
		try {
			return Signature.getInstance(Glb.getConst().getSignatureAlgorithm(),
					Glb.getConst().getSecurityProvider());
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	public byte[] crypt(boolean encrypt, String password, byte[] data) {
		try {
			byte[] passwordB = password
					.getBytes(Glb.getConst().getCharsetPassword());
			return crypt(encrypt, passwordB, data);
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	/**
	 * https://blogs.osdn.jp/2017/09/24/runnable-jar.html
	 * @return	自身の絶対パス
	 */
	public String getExecutionFilePath() {
		try {
			ProtectionDomain pd = Tenyu.class.getProtectionDomain();
			CodeSource cs = pd.getCodeSource();
			URL location = cs.getLocation();
			URI uri = location.toURI();
			Path path = Paths.get(uri);
			File f = path.toFile();
			//取得できるパスがファイルの場合もフォルダの場合もあるので、出力が一致するよう調整する
			if (f.isFile()) {
				//***.jarまで含まれている場合、そのフォルダのパスを返す
				return path.getParent().toString();
			} else {
				//pathがフォルダを参照しているならそれをそのまま返す
				return path.toString();
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * パスワードからiv等を作成して共通鍵暗号化または復号化を行う
	 * @param encrypt
	 * @param password
	 * @param data
	 * @return
	 */
	public byte[] crypt(boolean encrypt, byte[] passwordB, byte[] data) {
		try {
			Security.setProperty("crypto.policy", "unlimited");

			int keySize = Glb.getConst().getPasswordKeySize();
			int ivSize = Glb.getConst().getCommonKeyIvSize();
			MessageDigest md = getMDSecure();
			byte[] key = new byte[keySize];
			System.arraycopy(md.digest(passwordB), 0, key, 0, keySize);
			byte[] iv = new byte[ivSize];
			System.arraycopy(md.digest(key), 0, iv, 0, ivSize);

			int mode;
			if (encrypt) {
				mode = Cipher.ENCRYPT_MODE;
			} else {
				mode = Cipher.DECRYPT_MODE;
			}
			return commonKey(data, mode, key, iv);
		} catch (Exception e) {
			Glb.debug(e);
			return null;
		}
	}

	/**
	 * 共通鍵暗号化/復号化
	 * @param data
	 * @param mode
	 * @param key
	 * @param iv
	 * @return
	 */
	public byte[] commonKey(byte[] data, int mode, byte[] key, byte[] iv) {
		byte[] r = null;
		SecretKeySpec keyObj = setupCommonKey(key);
		IvParameterSpec ivObj = new IvParameterSpec(iv);
		try {
			Cipher cipher = Cipher.getInstance(
					Glb.getConst().getCommonKeyCipherAlgorithm(),
					Glb.getConst().getSecurityProvider());
			cipher.init(mode, keyObj, ivObj);
			r = cipher.doFinal(data);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return r;
	}

	public SecretKeySpec setupCommonKey(byte[] keySrc) {
		return new SecretKeySpec(keySrc,
				Glb.getConst().getCommonKeyAlgorithm());
	}

	/**
	 * Kryo等シリアライザは型情報を１つの数値にする事がある。
	 * アプリAで型１のオブジェクトをシリアライズしたデータに電子署名すると、
	 * もしその署名を他のアプリに持ち込んだら、
	 * その型番号は別の型を意味するだろう。
	 * もし偶然メンバー変数の型の並びが同じだったら、
	 * 電子署名の名目の捏造が可能になってしまう。
	 * だから電子署名はアプリ固有の情報を含めて署名すべきだろう。
	 * 署名対象オブジェクトのクラスの完全修飾型名が良いだろう。
	 *
	 * @param nominal		署名の経緯や名目を示す情報
	 * @param target	署名対象
	 * @param pri		秘密鍵
	 * @return			署名
	 */
	public byte[] sign(String nominal, byte[] target, PrivateKey pri) {
		Signature s;
		try {
			s = getSignature();
			s.initSign(pri, new SecureRandom());
			Charset c = Glb.getConst().getCharsetNio();
			s.update(Glb.getConst().getAppName().getBytes(c));
			if (nominal != null)
				s.update(nominal.getBytes(c));
			s.update(target);
			return s.sign();
		} catch (InvalidKeyException | SignatureException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * 署名を検証する。
	 *
	 * @param nominal		署名時に指定された名目
	 * @param signature		署名
	 * @param pub			署名に使われた秘密鍵に対応する公開鍵
	 * @param target		署名対象
	 * @return				検証結果
	 */
	public boolean verify(String nominal, byte[] signature, PublicKey pub,
			byte[] target) {
		try {
			Signature s = getSignature();
			s.initVerify(pub);
			Charset c = Glb.getConst().getCharsetNio();
			s.update(Glb.getConst().getAppName().getBytes(c));
			if (nominal != null)
				s.update(nominal.getBytes(c));
			s.update(target);
			return s.verify(signature);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public boolean verify(String nominal, byte[] signature, byte[] pub,
			byte[] target) {
		PublicKey pubObj;
		try {
			pubObj = getPub(pub);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return false;
		}
		return verify(nominal, signature, pubObj, target);
	}

	/*
		public boolean verify(byte[] signature, PublicKey pub, byte[] data) {
			try {
				Signature s = getSignature();
				s.initVerify(pub);
				s.update(data);
				return s.verify(signature);
			} catch (InvalidKeyException | SignatureException e) {
				Glb.getLogger().error("", e);
				return false;
			}
		}
	*/
	/**
	 * @param pubSrc	公開鍵
	 * @param data		暗号化対象
	 * @return			暗号化されたデータ
	 */
	public byte[] encryptByPublicKey(byte[] pubSrc, byte[] data) {
		try {
			PublicKey pub = getPub(pubSrc);
			return encrypt(pub, data);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	public byte[] encrypt(PublicKey pub, byte[] data) {
		return rsa(pub, data, Cipher.ENCRYPT_MODE);
	}

	public PrivateKey getPri(byte[] pri)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		return getPri(getKf(), pri);
	}

	public PrivateKey getPri(KeyFactory kf, byte[] pri)
			throws InvalidKeySpecException {
		return kf.generatePrivate(new PKCS8EncodedKeySpec(pri));
	}

	/**
	 * 公開鍵の生データからPublicKeyオブジェクトを得る。
	 * @param pub
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public PublicKey getPub(byte[] pub)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		return getPub(getKf(), pub);
	}

	public PublicKey getPub(KeyFactory kf, byte[] pub)
			throws InvalidKeySpecException {
		if (pub == null)
			return null;
		return kf.generatePublic(new X509EncodedKeySpec(pub));
	}

	public KeyFactory getKf() throws NoSuchAlgorithmException {
		try {
			return KeyFactory.getInstance(
					Glb.getConst().getKeyFactoryAlgorithm(),
					Glb.getConst().getSecurityProvider());
		} catch (NoSuchProviderException e) {
			Glb.debug(e);
			return null;
		}
	}

	/**
	 * @param priSrc	秘密鍵
	 * @param data		複合化対象
	 * @return			複合化されたデータ
	 */
	public byte[] decryptByPrivateKey(byte[] priSrc, byte[] data) {
		try {
			return decrypt(getPri(priSrc), data);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	public byte[] decryptByPublicKey(byte[] pubSrc, byte[] data) {
		try {
			return decrypt(getPub(pubSrc), data);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	public byte[] decrypt(PrivateKey pri, byte[] data) {
		return rsa(pri, data, Cipher.DECRYPT_MODE);
	}

	public byte[] decrypt(PublicKey pub, byte[] data) {
		return rsa(pub, data, Cipher.DECRYPT_MODE);
	}

	private byte[] rsa(Key key, byte[] data, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(
					Glb.getConst().getRsaCipherAlgorithm(),
					Glb.getConst().getSecurityProvider());
			cipher.init(mode, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 加重乱択
	 * @param candidates	候補：信用
	 * @return				選択された候補
	 */
	public <R> R rndSelect(final Map<R, Integer> candidates) {
		long total = 0;
		for (Integer i : candidates.values()) {
			total += i;
		}
		long rnd = ThreadLocalRandom.current().nextLong(total);
		for (Entry<R, Integer> e : candidates.entrySet()) {
			rnd -= e.getValue();
			if (rnd <= 0)
				return e.getKey();
		}
		return null;
	}

	/**
	 * 加重多数決
	 *
	 * votesを作る過程は共通化するメリットがあまりないと判断
	 *
	 * @param votes	投票項目:重み
	 * @return
	 */
	public <R> R majority(final Map<R, Integer> votes) {
		/*
		//投票値 : 票数
		Map<R, Integer> counts = new HashMap<>();
		for (Entry<R, Integer> vote : votes.entrySet()) {
			//この投票の値
			R value = vote.getKey();
			//この投票の重み,あるいは票数
			Integer weight = vote.getValue();
			//投票値で検索して既存の票数を取得
			Integer totalWeight = counts.get(value);
			//まだ票数設定が無いなら票数０で作成
			if (totalWeight == null)
				totalWeight = 0;
			//票数を増加
			totalWeight += weight;
			//投票値voteの現在の票数はtotalWeightである
			counts.put(value, totalWeight);
		}
		*/
		//最大の票数
		int max = 0;
		//最大の票数を得た投票値
		R key = null;
		//全投票値と件数について網羅的に調べる
		for (Entry<R, Integer> e : votes.entrySet()) {
			//もし現在の最大の票数を越えていたら
			if (e.getValue() > max) {
				key = e.getKey();//最大票数の投票値を更新
				max = e.getValue();
			}
		}

		if (key == null)
			return null;

		return key;
	}

	/**
	 * @param votes	投票一覧
	 * @return		最大票数の投票値
	 */
	public <R> R majority(final Collection<R> votes) {
		//投票値 : 票数
		Map<R, Integer> counts = new HashMap<>();
		for (R vote : votes) {
			//投票値で検索して既存の票数を取得
			Integer exist = counts.get(vote);
			//まだ票数設定が無いなら票数０で作成
			if (exist == null)
				exist = 0;
			//票数を1増加
			exist++;
			//投票値voteの現在の票数はexistである
			counts.put(vote, exist);
		}

		//最大の票数
		int max = 0;
		//最大の票数を得た投票値
		R key = null;
		//全投票値と件数について網羅的に調べる
		for (Entry<R, Integer> e : counts.entrySet()) {
			//もし現在の最大の票数を越えていたら
			if (e.getValue() > max)
				key = e.getKey();//最大票数の投票値を更新
		}

		if (key == null)
			return null;

		return key;
	}

	public double averageLong(final Collection<Long> values) {
		long total = 0;
		for (long v : values) {
			total += v;
		}
		return total / values.size();
	}

	public double average(final Collection<Double> scores) {
		double total = 0;
		for (Double i : scores) {
			total += i;
		}
		return total / scores.size();
	}

	public double averageInt(final Collection<Integer> scores) {
		double total = 0;
		for (Integer i : scores) {
			total += i;
		}
		return total / scores.size();
	}

	/**
	 * 加重平均
	 * @param scores	値：重み
	 * @return			平均
	 */
	public double average(final Map<Double, Integer> scores) {
		long totalWeight = 0;
		double totalValue = 0;
		for (Entry<Double, Integer> e : scores.entrySet()) {
			totalWeight += e.getValue();
			totalValue += e.getKey() * e.getValue();
		}
		return totalValue / totalWeight;
	}

	/**
	 * @param scores
	 * @return		分散
	 */
	public double getUnbiasedVariance(final Collection<Double> scores) {
		double variance = 0;
		double ave = average(scores);
		for (Double i : scores) {
			variance += Math.pow(i - ave, 2);
		}
		return variance / scores.size();
	}

	/**
	 * 加重分散なる独自に考えた無次元量。既にあるのかもしれないが
	 * @param scores
	 * @param ave		加重平均
	 * @return			加重分散
	 */
	public double getWeightVariance(final Map<Double, Integer> scores,
			double ave) {
		double variance = 0;
		double weightAve = averageInt(scores.values());
		if (weightAve == 0)
			return Double.NaN;
		for (Entry<Double, Integer> e : scores.entrySet()) {
			double weight = e.getValue() / weightAve;
			variance += Math.pow(weight * (e.getKey() - ave), 2);
		}
		return variance / scores.size();
	}

	/**
	 * @param scores
	 * @return			標準偏差
	 */
	public double deviation(final Collection<Double> scores) {
		return Math.abs(Math.sqrt(getUnbiasedVariance(scores)));
	}

	/**
	 * @param scores
	 * @param ave
	 * @return			加重標準偏差
	 */
	public double deviation(final Map<Double, Integer> scores, double ave) {
		return Math.abs(Math.sqrt(getWeightVariance(scores, ave)));
	}

	/**
	 * @param ave
	 * @param devi
	 * @param score
	 * @return			偏差値
	 */
	public double standardScore(final double ave, final double devi,
			final Double score) {
		return (50 + 10 * (score - ave) / devi);
	}

	public byte[] concat(byte[] a, byte[] b) {
		byte[] buf = new byte[a.length + b.length];
		System.arraycopy(a, 0, buf, 0, a.length);
		System.arraycopy(b, 0, buf, a.length, b.length);
		return buf;
	}

	public <E> String toString(List<E> l) {
		StringBuilder sb = new StringBuilder();
		sb.append("size:" + l.size() + System.lineSeparator());
		for (int i = 0; i < l.size(); i++) {
			sb.append(i + ":" + l.get(i) + System.lineSeparator());
		}

		return sb.toString();
	}

	public <K, V> String toString(Map<K, V> m) {
		StringBuilder sb = new StringBuilder();
		sb.append("size:" + m.size() + System.lineSeparator());

		for (Entry<K, V> e : m.entrySet()) {
			sb.append("key:" + e.getKey() + System.lineSeparator());
			sb.append("val:" + e.getValue() + System.lineSeparator());
		}

		return sb.toString();
	}

	/**
	 * 全要素の合計が1.0に近くなるような全要素への倍率を求める
	 * @param l
	 * @return
	 */
	public double leveling(Collection<Double> l, double total) {
		double currentTotal = 0;
		for (Double d : l)
			currentTotal += d;
		if (currentTotal == total)
			return 1.0;

		double r = total / currentTotal;
		return r;
	}

	/**
	 * 値の合計をtotalにする。全ての値が同じ倍率で変化する
	 * @param powers
	 */
	public void leveling(Map<Long, Double> powers, double total) {
		double multiplier = Glb.getUtil().leveling(powers.values(), total);
		for (Entry<Long, Double> power : powers.entrySet()) {
			powers.put(power.getKey(), power.getValue() * multiplier);
		}
	}

	public static interface MultiThreadTask {
		/**
		 * @param taskId
		 * @throws InterruptedException	ループ処理を止めたい場合に投げる
		 */
		public abstract void call(int taskId) throws Exception;
	}

	/**
	 * タスクを並列処理する。
	 * この方法はtという１インスタンスがマルチスレッドで実行されるので
	 * 内部状態など注意が必要。
	 * @param endTime		この時間まで出来るだけたくさん処理する
	 * 						20分以内であること
	 * @param ThreadSolveMax	1スレッドあたりの最大t.call()回数
	 * @param t				外部から渡される処理
	 */
	public void parallelTask(long endTime, int ThreadSolveMax,
			MultiThreadTask t) {
		Glb.debug("start:" + System.currentTimeMillis());
		long time = endTime - System.currentTimeMillis();
		if (time > 1000L * 60 * 20)
			return;//異常に長い時間ならやらない

		//物理コア数-3個のスレッドで計算する
		int threadNum = Glb.getConf().getPhysicalCoreNumber() - 3;
		if (threadNum < 1)
			threadNum = 1;
		AtomicInteger ai = new AtomicInteger(0);
		final ExecutorService threadpool = Executors
				.newFixedThreadPool(threadNum);

		try {
			//処理内容の設定
			Collection<Callable<Void>> jobs = new ArrayList<Callable<Void>>();
			for (int i = 0; i < threadNum; i++) {
				jobs.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						//時間内に出来るだけたくさん解く
						try {
							int solvedThisThread = 0;
							long solveStart = System.currentTimeMillis();
							long expect = 1;
							//expectに応じて次の処理をするかを決定する事は、
							//このようなタスク内ループを使わなければできない。
							while (expect > 0) {
								int taskId = ai.getAndIncrement();
								try {
									t.call(taskId);
								} catch (InterruptedException e) {
									break;
								}
								solvedThisThread++;

								if (solvedThisThread > ThreadSolveMax)
									break;

								//経過時間
								long elapsed = System.currentTimeMillis()
										- solveStart;
								Glb.debug("elapsed:" + elapsed);
								//1回あたりの時間
								long perSolve = elapsed / solvedThisThread;
								//残り時間
								long solveRemaining = endTime
										- System.currentTimeMillis();

								//残り時間で期待される回数
								if (perSolve > 0)
									expect = solveRemaining / perSolve;
							}
						} catch (Exception e) {
							Glb.getLogger().error("", e);
						}
						return null;
					}
				});
			}

			//定期的に終了時間をチェックして越えていたら強制終了する
			Glb.getExecutor().execute(() -> {
				while (true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						return;
					}
					if (endTime < System.currentTimeMillis()) {
						threadpool.shutdownNow();
						return;
					}
				}
			});

			//並列処理開始
			threadpool.invokeAll(jobs);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		} finally {
			if (threadpool != null && !threadpool.isShutdown())
				threadpool.shutdownNow();
		}
	}

	/**
	 * endtimeまで停止
	 */
	public void sleepUntil(long endTime) {
		sleepUntil(endTime, "");
	}

	/**
	 * @param endTime	この時間まで停止
	 * @param comment	debugメッセージ
	 */
	public void sleepUntil(long endTime, String comment) {
		long t = endTime - System.currentTimeMillis();
		Glb.debug(() -> "wait=" + t + "ms " + comment);
		if (t < 0)
			return;
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			Glb.getLogger().error("", e);
		}
	}

	public <K, V extends Comparable<V>> List<Entry<K, V>> rankingByValue(
			Map<K, V> m) {
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(m.entrySet());

		Collections.sort(entries, new Comparator<Entry<K, V>>() {
			public int compare(Entry<K, V> obj1, Entry<K, V> obj2) {
				return obj2.getValue().compareTo(obj1.getValue());
			}
		});

		return entries;
	}

	public byte[] toKryoBytesForPersistence(final Object o) throws IOException {
		return toKryoBytes(o, Glb.getKryoForPersistence());
	}

	public byte[] toKryoBytesForCommunication(final Object o)
			throws IOException {
		return toKryoBytes(o, Glb.getKryoForCommunication());
	}

	public byte[] toKryoBytes(final Object o, Kryo kryo) throws IOException {
		final byte[] b;
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				final Output output = new Output(bos)) {
			kryo.writeClassAndObject(output, o);
			output.flush();
			b = bos.toByteArray();
			//			if (b != null)
			//				Glb.debug(() -> "toKryoBytes object size:" + b.length);
		}
		return b;
	}

	public Object fromKryoBytesForCommunication(final byte[] b) {
		return fromKryoBytes(b, Glb.getKryoForCommunication());
	}

	public Object fromKryoBytesForPersistence(final byte[] b) {
		return fromKryoBytes(b, Glb.getKryoForPersistence());
	}

	public Object fromKryoBytes(final byte[] b, final Kryo kryo) {
		if (b == null)
			return null;
		//Glb.debug(() -> "fromKryoBytes object size:" + b.length);
		Input input = new Input(new ByteArrayInputStream(b));
		return kryo.readObject(input, kryo.readClass(input).getType());
	}

	public String readAll(final String path) throws IOException {
		return Files
				.lines(Paths.get(path),
						Charset.forName(Glb.getConst().getCharset()))
				.collect(Collectors
						.joining(System.getProperty("line.separator")));
	}

	public Properties loadProperties(String resourceName, String charset,
			Logger logger) {
		Properties result = new Properties();
		try {
			InputStream is = getLoader().getResourceAsStream(resourceName);
			InputStreamReader isr = new InputStreamReader(is, charset);
			result.load(isr);
		} catch (IOException e) {
			logger.error("", e);
		}
		return result;
	}

	public URLClassLoader getLoader() {
		URLClassLoader loader = null;
		try {
			loader = new URLClassLoader(
					new URL[] { new File("./").toURI().toURL() });
		} catch (MalformedURLException e1) {
			Glb.getLogger().error("", e1);
		}
		return loader;
	}

	/**
	 * big endian
	 *
	 * @param data
	 * @return
	 */
	public final byte[] toBytes(int data) {
		byte[] b = new byte[4];

		for (int i = 0; i < 4; i++) {
			b[3 - i] = (byte) (data >>> i * 8);
		}

		return b;
	}

	/**
	 * @param text
	 * @return		テキストの頭の部分
	 */
	public String option(String text) {
		return option(text, 20);
	}

	public String option(String text, int max) {
		String option = "";
		if (text != null) {
			if (text.length() > max) {
				option = text.substring(0, max);
			} else {
				option = text;
			}
		}
		return option;
	}

	public static class ByteArrayWrapper implements Comparable {
		private byte[] byteArray;

		public ByteArrayWrapper() {
		}

		public ByteArrayWrapper(byte[] b) {
			this.byteArray = b;
		}

		public byte[] getByteArray() {
			return byteArray;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ByteArrayWrapper other = (ByteArrayWrapper) obj;
			if (!Arrays.equals(byteArray, other.byteArray))
				return false;
			return true;
		}

		/**
		 * byte[]用
		 * @param bytes
		 * @return
		 */
		public boolean equals(byte[] bytes) {
			return Arrays.equals(byteArray, bytes);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(byteArray);
			return result;
		}

		@Override
		public String toString() {
			return Arrays.toString(byteArray);
		}

		@Override
		public int compareTo(Object arg0) {
			if (!(arg0 instanceof ByteArrayWrapper))
				return -1;
			ByteArrayWrapper o = (ByteArrayWrapper) arg0;
			byte[] ba = o.getByteArray();

			if (byteArray == null && ba == null)
				return 0;
			if (byteArray != null && ba == null)
				return 1;
			if (byteArray == null && ba != null)
				return -1;

			for (int i = 0; i < byteArray.length && i < ba.length; i++) {
				int c = Byte.compare(byteArray[i], ba[i]);
				if (c != 0)
					return c;
			}

			if (byteArray.length < ba.length)
				return -1;
			if (byteArray.length > ba.length)
				return 1;
			return 0;
		}
	}

}
