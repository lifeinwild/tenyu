package bei7473p5254d69jcuat.tenyu.release1.global;

import java.io.*;
import java.nio.charset.*;
import java.security.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.Base64.*;

import javax.crypto.*;

import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;

public class Const {
	/**
	 * アドレスの最大長
	 */
	private final int addrMax = 20;
	/**
	 * 鍵ファイルの署名の名目に使われているので変更すると鍵ファイルの修正まで必要になる
	 */
	private final String appName = "Tenyu";
	/**
	 * 作者信用
	 */
	private final int authorCredit = 200;
	/**
	 * 一度オンラインにしてしまったので旧に
	 */
	final String authorOffPubBase64Old = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiUDRqf9GiC0rX6k2BxknD1qMBJdH8PfeCr5nnprn+PYqUwb1lAUA2OOTB9gLsfDsFpdfwF6B2Sa8r+f17tfNPSzUwMwi9Gj3tiYoaKdo+Y8zx1iHDvcCzgMw6I8gz8SYXZmgPCNEnkX0KbWN/gNBDP9DfYZkznSM3Mq6cIcWcRvn/9GUIB0E5WKqKm4axUJbyzuh653W0H2g3BnnQkknwGx2Dl4znDDLZ8pLwjB7Irt+Kn5r/pR3kUXfoxbkeIe663a6DeSV2GFnghSNgTtzVmyj5CDTVwMdVXYdenBnmtIgIgobqzA4a+890B3eecJ5NOIOn9qh6cY7p8il0aG0M7CT9XsA9Ld76FYqAfMS2c86WkI8d4kSswU5WdaXJ6FND2duUXJBeOJbw8XhqWO5I/tW9Y33iXTXpiF3cuJbCn7XF8F4cHFAS7nMEI+rlc8ADjRx+OCk1qJPNpZyzvcJpfZP2gTmTWbEKt8Znc2+Wt7nIY/Hk4aRNfMbF5K7WPz1knR4GR9sUHL5t+OPZjJOqvQGrgMJ3v/f5j54cpecEJbpHrx83cxS3LQIWROZa+5lgjOk1T0hD+dUYyTeoJj3zfZHIdgKpslO7xraZiUcEG8gETYW9FRGQ+jVBvAZpuqxlchMAYOYFxW4LV0wYEZME1OpFzHWTjln6G1VwnXADg0CAwEAAQ==";
	/**
	 * 作者公開鍵のBase64
	 */
	private final ArrayList<String> authorPublicKeys = new ArrayList<>();

	private final ResourceBundle.Control bundleUtf8 = new ResourceBundle.Control() {
		@Override
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException,
				IOException {
			String bundle = toBundleName(baseName, locale);
			String resource = toResourceName(bundle, "properties");

			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				is = loader.getResourceAsStream(resource);
				isr = new InputStreamReader(is, "UTF-8");
				br = new BufferedReader(isr);
				return new PropertyResourceBundle(br);
			} finally {
				if (is != null)
					is.close();
				if (isr != null)
					isr.close();
				if (br != null)
					br.close();
			}
		}
	};

	private final String charset = "UTF-8";

	private final Charset charsetNio = Charset.forName(charset);

	private final String charsetPassword = "UTF-8";

	private final String commonKeyAlgorithm = "AES";

	private final String commonKeyCipherAlgorithm = "AES/CBC/PKCS7Padding";

	/**
	 * 共通鍵のサイズ
	 */
	private final int commonKeySizeForCommunication = 128 / 8;

	private final int commonKeyConfirmationSize = commonKeySizeForCommunication;

	private final int commonKeyIvSize = commonKeySizeForCommunication;

	private final List<String> defaultDomains = new ArrayList<String>();

	/**
	 * PCやネット環境の水準が高いと思われる地域一覧
	 * 理想的には国よりさらに的確な単位で判定したいが、現状国で判定している。
	 */
	private List<Locale> developeds = new ArrayList<>();

	private final String digestAlgorithm = "SHA-512";

	private final String distributedVoteManagerVoteName = "ManagerVote";
	/*	クラス名を使うようにしたので不要になった
	//p2pシーケンスのチャンネル一覧
	private final int processorProvementChannel = 1;
	private final int powerDecisionChannel = 2;
	private final int userMessageListChannel = 3;
	*/
	/**
	 * 分散合意のシーケンスは動的に追加されるのでチャンネルの範囲を割り当てる
	 */
	private final int distributedVoteRangeStart = 1000 * 1000;
	/**
	 * ファイル区切り文字	/path/to/file
	 * DBに区切り文字が記録される場合、File.separator等で環境毎に変える事は解決策にならない。
	 */
	private final String fileSeparator = "/";
	/**
	 * FQDNの最大長
	 */
	private final int fqdnMax = 200;
	private final int hashSize = 512 / 8;
	/**
	 * 発明者が１つパスワードを作成し、
	 * 発明者のオフライン公開鍵でそのパスワードを暗号化した文字列。
	 *
	 * オフライン公開鍵は旧オフライン公開鍵でリストの3番目の公開鍵。
	 *
	 * 暗号化処理のプログラムを記述する。
	 * pubは公開鍵リスト3番の公開鍵
	 * 暗号化と復号化をして元の文字列になる事を確認しつつ、
	 * 暗号化後byte[]をbase64した文字列を出力している。
	 * この文字列がinvestorProvementCryptedString
	 * password1は実際には作者証明パスワードが設定されていた。
	 *
	 * 		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	 * 			Cipher c = Cipher.getInstance("RSA/None/NoPadding", "BC");
	 * 			SecureRandom rnd = new SecureRandom();
	 * 			c.init(Cipher.ENCRYPT_MODE, pub, rnd);
	 * 			String password1 = "password";
	 * 			byte[] crypted = c.doFinal(password1.getBytes());
	 * 			c.init(Cipher.DECRYPT_MODE, pri, rnd);
	 * 			byte[] decrypted = c.doFinal(crypted);
	 * 			String password2 = new String(decrypted);
	 * 			if(password1.equals(password2)){
	 * 				System.out.println("true");
	 * 				System.out.println(Base64.getEncoder().encodeToString(crypted));
	 * 			}
	 */
	private final String inventorProvementCryptedString = "Cg6rqtLZNClRQWPyt6eWy4Bo7iYg4vBxH5pwP3TXK30wYiWE0Lk+cxmKAum0IvLunwb5H/TNg9oFW7RVYYcm3KGiH3R48gyYen0gRjTKh9TECO5GzDlQgebNDx3SdnOEopdKpFqZ/AIQLbRAqXuK29vaHWIab23YGsSyTkqr4TfhMglxMFs/kwz2s+7v/yerwT9ebb3UUilytdLsMDkuCrNY6x+3eyni+Gigvgv/SO1G/u4YYS4LBl3xwoH8nJyuzePjmn9tVTI0scueUiJzMFW/aix9GvW118AXUxX6M6hl6d04fOn9PBBkllZby/shGj+YJnW2DLhAQN9Epg84jUbqhmumpE2EQ9xrR5a+MJhnH9mdLTlbWkRPUNmr98psbWnIQju3K6LxL2v8YGEAsxcKTWhanYqBE24YPNSeJnS1FmFYem8w229pHr1G1hWMN9qu5auVfMNDH8hmXD5YRf9e68RQLvdImqE4OmhPrnf3IFqxd7t2zmbG3g2hR+I07E1YyVqs+KUYu68T0b9j9cJy5+UbhSsZkSCbyKQc9F2vwD043N7kmj4UzFvILj3iOyzUIfhufH3ASq4nuxp8mzvkxIwvsOrLsiilzu86WcqmJ8Zt0vPPSmDLH8l58QH5E2KS4g7/Ful1Qeron86SH//g/WL2UGY6MwTzT88VC8A=";
	private final List<String> ipRegistrationURLAdd = new ArrayList<String>();

	private final List<String> ipRegistrationURLRead = new ArrayList<String>();
	/**
	 * ipv6アドレスサイズ
	 */
	private final int ipv6size = 16;
	private final String keyFactoryAlgorithm = "RSA";
	private final String keyPairGeneratorAlgorithm = keyFactoryAlgorithm;

	/**
	 * 全体運営者の最大数
	 */
	private final int managerMax = 2000;
	private final String objectivity = "Objectivity";
	private final String passwordCipherAlgorithm = "AES/CBC/PKCS7Padding";
	/**
	 * 内部的に用いられる暗号化の鍵長
	 * パスワードが何文字であれハッシュ関数を通してこのサイズに補正される
	 */
	private final int passwordKeySize = 32;
	private final int passwordSizeMax = 100;

	/**
	 * パスワードは事実上どこかに記録しておくしかない。
	 * 人によって記録場所が違う事で一網打尽にするようなウイルスを作れない。
	 * だから20文字という長いパスワードを要求する事は問題ないと思う。
	 * どこかの記録を参照しながら、あるいはコピペによってパスワードを入力する。
	 */
	private final int passwordSizeMin = 20;

	/**
	 * 保護期間の作者影響割合
	 */
	private final double protectionAuthorPower = 0.6;
	/**
	 * 正規表現の最大長
	 */
	public final int regexMax = 2000;
	/**
	 * リリースされるたびに＋１
	 */
	private final int release = 1;

	/**
	 * OAEPの方がPKCS1より多くの状況でセキュア。復号オラクルが存在する状況で差があるらしい。
	 * 復号オラクルは、攻撃者が送信した任意の暗号文、主にでたらめに作成した暗号文を
	 * 復号化してくれる存在。パディングアルゴリズムを指定する事ででたらめな暗号文を扱わなくなる。
	 * SHA512にしているがそこはSHA1などでもセキュリティにほとんど影響しないそうだ
	 */
	private final String rsaCipherAlgorithm = "RSA/NONE/OAEPWithSHA512AndMGF1Padding";//"RSA/ECB/PKCS1Padding";

	private final int rsaKeySizeBit = 2048;//4096;

	private final int rsaKeySizeBitSecure = 4096;
	private final int rsaKeySizeByte = rsaKeySizeBit / 8;
	private final int rsaKeySizeByteSecure = rsaKeySizeBitSecure / 8;
	private final String securityProvider = "BC";
	/**
	 * RSA署名はメッセージをハッシュ関数に入れてその出力に署名しなければ
	 * 脆弱であるらしく、このSHAxxxwithというのは使用するハッシュ関数を指定している。
	 * このアルゴリズムを指定している時点でその脆弱性は防止されている。
	 */
	private final String signatureAlgorithm = "SHA512withRSA";
	/**
	 * 署名の最大長
	 * 可変長フィールドに異常なサイズを設定されないためで、
	 * 大雑把に大きな値が設定されている。
	 */
	private final int signMaxRough = 1000 * 5;
	private final String subjectivity = "Subjectivity";
	private final String virtualCurrencyUnit = "YU";

	private final String virtualCurrencyUnitKanji = "祐";
	{
		defaultDomains.add("");
	}

	{
		ipRegistrationURLAdd.add("");
	}
	{
		ipRegistrationURLRead.add("");
	}

	public Const() {
		developeds.add(Locale.KOREA);
		developeds.add(new Locale("NO"));
		developeds.add(new Locale("SE"));
		developeds.add(new Locale("HK"));
		developeds.add(new Locale("CH"));
		developeds.add(new Locale("FI"));
		developeds.add(new Locale("SG"));
		developeds.add(Locale.JAPAN);
		developeds.add(new Locale("DK"));
		developeds.add(Locale.US);
		developeds.add(new Locale("NL"));
		developeds.add(new Locale("RO"));
		developeds.add(new Locale("CZ"));
		developeds.add(Locale.UK);
		developeds.add(new Locale("TW"));
		developeds.add(new Locale("LV"));
		developeds.add(new Locale("BE"));
		developeds.add(Locale.CANADA);
		developeds.add(new Locale("TH"));
		developeds.add(new Locale("IE"));
		developeds.add(new Locale("BG"));
		developeds.add(new Locale("ES"));
		developeds.add(Locale.GERMANY);
		developeds.add(new Locale("HU"));
		developeds.add(new Locale("NZ"));
		developeds.add(new Locale("LT"));
		developeds.add(new Locale("AU"));
		developeds.add(new Locale("QA"));

		initAuthorPublicKeys();
	}

	public int getAddrMax() {
		return addrMax;
	}

	public String getAppName() {
		return appName;
	}

	/**
	 * 開発者。返値を修正されてしまう可能性があるので、
	 * 必ず新しいオブジェクトを作成して返す。
	 */
	public User getAuthor() {
		User author = new User();
		String authorPcPubBase64;
		String authorMobilePubBase64;
		String authorOffPubBase64;
		/*
		 * テスト時に作者公開鍵で署名したメッセージを
		 * 本番環境に持っていかれると困る。
		 * つまり、作者機能のテストができなくなってしまう。
		 * そこで、テスト用の作者公開鍵を決める。
		 */
		if (Glb.getConf().isDevOrTest()) {
			//テストまたは開発時の作者公開鍵
			authorPcPubBase64 = authorPublicKeys.get(4);
			authorMobilePubBase64 = authorPublicKeys.get(5);
			authorOffPubBase64 = authorPublicKeys.get(6);
		} else {
			//本番時の作者公開鍵
			authorPcPubBase64 = authorPublicKeys.get(0);
			authorMobilePubBase64 = authorPublicKeys.get(1);
			authorOffPubBase64 = authorPublicKeys.get(3);//2は旧オフライン公開鍵
		}
		//OtherPublicKeys:PublicKeys.txt
		author.setRecycleId(IdObjectDBI.getFirstRecycleId());
		author.setMainAdministratorUserId(author.getRecycleId());

		AddrInfo addr = new AddrInfo();
		addr.setFqdn("lifeinwild.f5.si");
		addr.setP2pPort(32847);
		if (Glb.getConf().isDevOrTest()) {//TODO テストコードが存在している
			addr.setP2pPort(21344);
		}
		addr.setGamePort(32947);
		addr.setTenyutalkPort(33047);
		author.addNodeNumberToAddr(0, addr);
		author.setSecure(true);
		author.setName("lifeInWild");
		author.setExplanation("the author of " + appName + "\r\n"
				+ "exceptiontenyu@gmail.com\r\n" + "satoji@protonmail.com\r\n"
				+ "lifeinwild1@gmail.com");

		author.setMobilePublicKey(
				Base64.getDecoder().decode(authorMobilePubBase64));
		author.setPcPublicKey(Base64.getDecoder().decode(authorPcPubBase64));
		author.setOfflinePublicKey(
				Base64.getDecoder().decode(authorOffPubBase64));
		author.setTimezone(ZoneId.of("Japan"));

		author.setRegistererUserId(IdObjectDBI.getSystemId());

		return author;
	}

	public int getAuthorCredit() {
		return authorCredit;
	}

	public String getAuthorOffPubBase64Old() {
		return authorOffPubBase64Old;
	}

	public ArrayList<String> getAuthorPublicKeys() {
		return authorPublicKeys;
	}

	public ResourceBundle.Control getBundleUtf8() {
		return bundleUtf8;
	}

	public String getCharset() {
		return charset;
	}

	public Charset getCharsetNio() {
		return charsetNio;
	}

	public String getCharsetPassword() {
		return charsetPassword;
	}

	public String getCommonKeyAlgorithm() {
		return commonKeyAlgorithm;
	}

	public String getCommonKeyCipherAlgorithm() {
		return commonKeyCipherAlgorithm;
	}

	public int getCommonKeyConfirmationSize() {
		return commonKeyConfirmationSize;
	}

	public int getCommonKeyIvSize() {
		return commonKeyIvSize;
	}

	public int getCommonKeySizeForCommunication() {
		return commonKeySizeForCommunication;
	}

	public List<String> getDefaultDomains() {
		return defaultDomains;
	}

	public String getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public String getDistributedVoteManagerVoteName() {
		return distributedVoteManagerVoteName;
	}

	public int getDistributedVoteRangeStart() {
		return distributedVoteRangeStart;
	}

	public String getFileSeparator() {
		return fileSeparator;
	}

	public int getFqdnMax() {
		return fqdnMax;
	}

	public int getHashSize() {
		return hashSize;
	}

	public String getInventorProvementCryptedString() {
		return inventorProvementCryptedString;
	}

	public List<String> getIpRegistrationURLAdd() {
		return ipRegistrationURLAdd;
	}

	public List<String> getIpRegistrationURLRead() {
		return ipRegistrationURLRead;
	}

	public int getIpv6size() {
		return ipv6size;
	}

	public String getKeyFactoryAlgorithm() {
		return keyFactoryAlgorithm;
	}

	public String getKeyPairGeneratorAlgorithm() {
		return keyPairGeneratorAlgorithm;
	}

	public int getManagerMax() {
		return managerMax;
	}

	public int getMaxManagerCount() {
		return managerMax;
	}

	public String getObjectivity() {
		return objectivity;
	}

	public String getPasswordCipherAlgorithm() {
		return passwordCipherAlgorithm;
	}

	public int getPasswordKeySize() {
		return passwordKeySize;
	}

	public int getPasswordSizeMax() {
		return passwordSizeMax;
	}

	public int getPasswordSizeMin() {
		return passwordSizeMin;
	}

	public double getProtectionAuthorPower() {
		return protectionAuthorPower;
	}

	public int getRegexMax() {
		return regexMax;
	}

	public int getRelease() {
		return release;
	}

	public String getRsaCipherAlgorithm() {
		return rsaCipherAlgorithm;
	}

	public int getRsaKeySizeBit() {
		return rsaKeySizeBit;
	}

	public int getRsaKeySizeBitSecure() {
		return rsaKeySizeBitSecure;
	}

	public int getRsaKeySizeByte() {
		return rsaKeySizeByte;
	}

	public int getRsaKeySizeByteSecure() {
		return rsaKeySizeByteSecure;
	}

	public String getSecurityProvider() {
		return securityProvider;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public int getSignMaxRough() {
		return signMaxRough;
	}

	public String getSubjectivity() {
		return subjectivity;
	}

	public String getVirtualCurrencyUnit() {
		return virtualCurrencyUnit;
	}

	public String getVirtualCurrencyUnitKanji() {
		return virtualCurrencyUnitKanji;
	}

	private ArrayList<String> initAuthorPublicKeys() {
		//作者公開鍵のBase64
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsr3dxpS/V6zzCl16xVKwTUHpGlGE8IpFWcFdMkB90YyQyfoRwQm1dlSD8Rn6u3+OvfmBPoz9ppa+H4Zu9G9E9m0xYmnsIYakIMIboUt1qd95JNpqxzImTkWv0b2gInnwFeBp0LN/keRHicrBuUfVq98qwitcQq1OYQUQuJyeMXmmDxkvv8ug3fXizw/zDVKJ8M7nrsfjSnFwfi5egitd9eX3/KqQf5UGn7NqWbXFUPQwQ7MLQ9K8dfOF9xweWrrMEC57WZECxI82CTOprmvNruNh7FOLbqA0RpIdC1SRj6WgsOqveSXv/c+kU0R6zs7FVDg2X75n+fFOCLybEg0v2auBsiteQzdPoQvdLoJWvXKZ3ofCF0L3Gv8fhiR/3w6MLWHYYqsYkzdwmSjUS1SiPjCSrxonyId988o6Y82KwxiuEp50hvroJ0raEV9LhkfimkGTbSVegyag/avMksRvFM2aRlkncksTcCywNyfzMB8g73WoewTFO3tZ8V8JZB4csrot1jNfmQCIsYA6a0GgdQk9z7dhB+0zVS7kuXO+Djh6lpP5BM2uxByA0GBh1HYchuuEHasITRwqqzGozDFx2ScobegMQx4hObWv81O3rQdDhbEvG9h8jFZQB4yMQYjLQQaRhuTiq2Rr/critB/TmjhMIaDsemSkgkm3tWbpHAUCAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEApIEDa624qLZcsZKRbAND9fgRylL2uvvm/OFafB420BXp270Doj1avizKheVJgeGz3dbV74ML3sTsLa6U46rtO8CTTeWec5o9OXNhRACdiqYMAx9uzifCvFe/VOSwwjp48bcCn4DrpvwVbD1ipbPo3Mt6ym7mX6491uonKkad6b9nlRAueesjP7C+yha8qOXzWQu3ibgRvEHj7pWXYrPUoP6k/A2QANkcDanzPA81gNCQLeU9JgyBIOjVVuW1++8xNTD+HVzww+5pF3lyFfAiCNjjMDAfuiHVExogF014oX59s9pvcE/8HWiAv1xyYaEeJ/8Li8189kHL9A2y7V91SErpHWvXr31OtymTrBH97eczLh8152MV2STDZRm73Uu9WtpGB6JyCA/0Tsl6C25em3B8Pb020ULEa7H+86PXjuifICqP4hzDLYPbbCXh3/lmAtibyRnjaA3I32wfKfCiN57i9X8SBjG1en9WqWxDSLo0BsWLMJjf3dP1PalBL17/a5EF5FfARL5/wIRkl58geLgvtGz0oKaoz/RvMmyuTD7cqZ1J03GZGVswlPfTMuDOSu1na3cKRYPpAyQcHYd/y7y1jP4KWt+fNE22gJUfYzr/aF4Eua+UDdln1Tq1B2BZycSwsy4NjMIJMxaGTI1EJUwykCiVYi+uEBdOWT74zEkCAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiUDRqf9GiC0rX6k2BxknD1qMBJdH8PfeCr5nnprn+PYqUwb1lAUA2OOTB9gLsfDsFpdfwF6B2Sa8r+f17tfNPSzUwMwi9Gj3tiYoaKdo+Y8zx1iHDvcCzgMw6I8gz8SYXZmgPCNEnkX0KbWN/gNBDP9DfYZkznSM3Mq6cIcWcRvn/9GUIB0E5WKqKm4axUJbyzuh653W0H2g3BnnQkknwGx2Dl4znDDLZ8pLwjB7Irt+Kn5r/pR3kUXfoxbkeIe663a6DeSV2GFnghSNgTtzVmyj5CDTVwMdVXYdenBnmtIgIgobqzA4a+890B3eecJ5NOIOn9qh6cY7p8il0aG0M7CT9XsA9Ld76FYqAfMS2c86WkI8d4kSswU5WdaXJ6FND2duUXJBeOJbw8XhqWO5I/tW9Y33iXTXpiF3cuJbCn7XF8F4cHFAS7nMEI+rlc8ADjRx+OCk1qJPNpZyzvcJpfZP2gTmTWbEKt8Znc2+Wt7nIY/Hk4aRNfMbF5K7WPz1knR4GR9sUHL5t+OPZjJOqvQGrgMJ3v/f5j54cpecEJbpHrx83cxS3LQIWROZa+5lgjOk1T0hD+dUYyTeoJj3zfZHIdgKpslO7xraZiUcEG8gETYW9FRGQ+jVBvAZpuqxlchMAYOYFxW4LV0wYEZME1OpFzHWTjln6G1VwnXADg0CAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAnsNTcDuJIlO4dnyMx4ILU5dLZwyGsqqoqbsEL+jAIQgVUxcr7wmo+VtqbZ7r+P/xncHTFz0WvA7g9xgNyxwk6y+HjIm38IS6fNzNQRRlkFEXL4+iabMHJTjK5ugPKqVVgPo9aGcrwW2m7wVnC9E7thQ5mHf6MqIGh6J/jRx9Ke6bjlO4a8x1IrXP/PLAW/UnJuv5T2wdbPJ5Jq7PGDeMtWtNUSOAio770Ou3KmemLTTnrXRkZAXdNO+t7iBUUOcgr1wxDKscczYu07cdKrVy1UnYq8W+mJ8EHpknEXc2Fm1alOuee3zSpS5gUxTGyj5uIEBFvDn2uEgu45/vi2A1/zhWxnaXpMw0m5cPCD4wpfq7HMqpX1GZzY9zjSCBsOc93SzU0Oz6/k2LOuxVz18bym3T3ri6fcERcftfipGnaLn7wpsXjCmr+7hZFejMWaIovEDq/pO6FoLuXH0l8/4PtvIGso7Ay/LnarJl7HbZc7aZucQEjyrYg6z6yTFrxUorXxFBdNC4L/m/akjStN6P/yvYs7/DAWGfAQPNOpCA0MxqwuncBcU3FzHAASeBFndPOYXH2fNTXot7svj2Nm5GolPO1M2ZUUqlmXD2wYPCC+Efp3uMFH1Wuo04wGJfIchyKv4S65+9EfW3u4b5hH2UPcWzFQf5HluGB/W9k3VbRP8CAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA7Y3y4jXGb3YhPSeDUobcKEkMLqwTtf9qUTEEWtjrLPgUtMtMCjLFZgyw0LL9BuM8NtO8JVFg6jI7GOXPfOlHPEmbind5JoBwVuwNNaZtAjm+NehSxjcNmnPjt8LrhqTti1kFLp0KUmbqNCbgPfU9lPaSxzz3UDHxBAFU3ua8TiUH7ILwlvUe2xn94F6fIHcFxY2VIzAnFgOhLwdxdL5vBfIXDUBX4C8VtIfGTbQAnU6xm9SqXSdwQqmEWzigHVzJk7a40Lp6RRl8ELgt7vq6P82GohQVW9ZkGCOVB4vl6fPdW62AiyGlP3C5BftmnQEvHYJOGszjoPS0zXJM1CMBdZNGlVEuSzypKYrIRiQ80/EEfhU+QyVIrCaPJbeES3vifIYHo54ecWbJCQ8aYCvPaKjkqetSI5HDHUhtKLwsrhu9JxHB4hOlXOC1GP1tufYB8SJP7VUBRoKbz20lyKp3pIs7Esx1nTSLeBZ/SRI+5ikFjaewDI+u0b7bP+8qRYlMpuv9I7YWJwLnBRfDYDELItKz5GL4bv4RRddL8Y0+B9Qte6hk9VP+8Qs/n5TRAS07vEyzfMi3uZLUN0O7NfZtWNFyvplqgQ09TyYxzMQgKpdHpRnghCi5FWkpCPPNNHOKheUcyvYwIDpYW1b/i4DnE1FXag9gjNXn2LQB9NM3UqMCAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAt3/kZ5s47vtS65GBALio/YlX9UF0rco+R6kg7Cp/IEOi6bA5yyUjWYbU+58ITooAw3PYK8d6DBF/b8bOJjoWWdPABFIk2qzkOVbO1xyVvk5cHLRh7K0T1ACgpzZUSIb5UzsP8gBz71CwbqYwEhinbsTk1QXPWaiiFEnumGAAaP/O8n6+APu4/GirTzQaQMsBHakB61gcoGCST7ScnJcRDz65H30KZy+Qjju5LUnn9xUTyVlRDE274dS6iiFPA6uUi6P61bQS6JZWoShKNuXpaDLDR+jfXhAuGPYrYWe8kwWXrAnOFY1IhIhSD8cfO8i1zd9v+KxeULzMcmQOYxFC+4jp0tmN9gy+q0smi9d/P9XnnL5mRxN1L7aG1ORwVlxjMOnN4vtd9J86R3ynJ/yxWJLVyJ8RcJ8xAElQWofzAbkzI2s97E1VDyeQQF7lZXmhX+qDU5qIDhze2Xxcq+raI94MhytQCmdbho2I5Uqlg3DpIU3CqSFDCERehwNkHLhoB9VP0OB8KahLaK7ddGW8womu/96JQEDMa1uHXqPb6YE1NSWkkTJbXfv3uFATY6nX9oExuft2Yu88lnzwUZAFT97L6/SrEPkjiL6FrpzA2wxVIelj8PG7EvxVzl7Akd9DHHYP75A8eFiHnl8HCD+A7ctDFrPZFaKCuQiLJs4mLdcCAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjRIPfR083XiIPkEIYGPUeW/sXVaUh5GRKoM9B9kzWwuOqKVbWRvhgyUKL7hkyIGL6ZtPwhuXQOmeq1qM4DC5uv/UCWEZzvydJO8PjGvDyP/W272IX9HTILOZs7pN67/HMikmRicuN/lws5sRzenFLvTdyNHwz5V4u85boVNraUWPk7pY60fS/lkF+2tTI/jAnjsVdamAICklo9smV4O76m1ZjkT9Bbap9s6ZPfPK0PD51k1BoD+a+cg254GHYBUcw/NqUFFoV2EOF/WRfkq/Bugd4051qoF8WcDDheVxxKFGYhkKPoZus0NuHTwoVvJi+Pg3QvzhgIDnmc+ocORSHUtRMT2Og0OFPVZlseK7lwH+HIVk2HMmRvgHn2H3VkRfQFFF9JMlkZuzKQ23cnduSsdz7KOPnFXAGZ2PBrXmA7QXTVl233iJf/9LODHdPiKj6bdh2s53o/9A+aQDqPgneQ9MGgtMPd0i+lhqJ/X4k0W9C2BjzGIQx2a3ZKO27U3Vwp8bJjlD2zhuKYBbPtVBLou6Moy11ljDkC7QT9cbh3/Sc7GyEvhX+NZ8dgjeyN5BcWu0Nq33ZAfHkhug8wOdM3h+eI94hh0iJ47JJ1GT3/fsHscd+CYMrCxccmT5sEdk02wMHRnduOm8PKCBCyIgmt34fw4hpEx2F3U9KU0xfJ8CAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAittu+yqCJ5iKYbp56iHFtj4QQi1QPlJoVyUPK2pCTK/fxhXSW6Z9VRUirhzDxo7e2iQXlzOFiH+6HwUwJuXjw9b7+e84DXBhnS2IkMEYR5aZvYxlikgm9EjsmyaUR+MgxIi3dyiTxgyGvvuUrnNoVOoxaQTtQ1QVXgWko5R9wPRoYMymBtI5T6wb1hRBDJAn/nib+UJ0+ZVAK3UCGJxaCrnTwynmxPfbqDcMxlMs1YDNucbwzxF/LGPkVTDUtYc4U4sASfsVJarUQ5+AC0WFKzsj0SwacbXJMyi1kt8cagmz2MLjivSqqfC6SrGNM9L5yywJ21ca4GzAqww+VMGH/fiS8V+HWnr3tou9T6ot45/EJ1iSv5fmAXcNIXn/PdkktcJJuU0tyZbdk9VfrpeV1p242h4QnUzXG0fxb5YOcGO+91KiNnH8X0T5/BzSAeJNEpBVV6XjJbjts+cF3yT+goXy0GfKaoPR/DTayx2PZX3/Wmib+TcPU/eSxzGVDFzHtxmIN9FgGGvQD9TQIbM+/VSxZeuNg2O74eFLQJYmcSrEECUV7/x25SXI3hAiZKv981GrljSbNrKm2R7g1M6T8WJO0Ky9/0hsShcLjup76vdrbACn+RaoIOVDSauoAvzFLOQ9mhGDOZPKnlSugPe7jDVjuboV4WpkwplFoSnDVpECAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiBbiaGnGFwlRcuxiutxvcdJT+GI5J8q12vlcuakO8iWOWzhq6dW8i5AF5gWfTIm1/1sDnpHl4rdF/tnJV95KNtiZBShsflw69yRoOcCKKa2s6Iju4+z4aPycB2puOvBHEP/2b8tvNRezREMJ3vp2QA+sRsmnWi9F2kO0aFiV0RZhg+JRSV0+KGKpXALoShGWKSFvy0o32CTvvryxiw4bY4ed8VIxrlDmVgVTadsgEhUT6jJoYK6u/Eohno9OCxs0b8R1drZRkknV9Ufma8BgY4PuS2hJ7w0PmrxRidOMxvh+kwO667AHVvNQHUDFq165/3vkID7jNFgFDqt6Xa/cSil1DMydG4yECMRhOtozjmkokg1vInLUIwNQBw1XS0QhE6QTGcnibeiNFUkMMtxIy4wxq2CFVNPZ6lgNbOXgL4rGPvvwDPFDw6IRdcIUWIQUrzFAIoByVT8lPdn7RKoFeUd/M3nddzW+gXGjnIuZ0ENL/EG3SdZuIE7e1apRnhAbTk/Rf3+fPnxUi5dkT1aYVq8U2wYmr5wWuSUH1RH1WU++TAqh0hG1+ElTNb33WBroDT4FSrFlIjWAkPOIByeMLShU0JpMbc7bhX1POZvXuWBzE+KtZgMxyOB/uFMdax/UGJ3guxcIHAvJ8Obg1zXL3FCR8A9AYV9uScUCRk5oVt8CAwEAAQ==");
		authorPublicKeys.add(
				"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAgAtKZNI4ab9gnJejSrb9UgohtCFFuv1ETx07ruLDFff4HL4/FUSlobUQnR79fOzW4NLAzTfAfu0LJn1hY1aAiUUl2lZUToRRH7azivp2OhgFtfDebRRiC9PnYwH2LpXU6tEvIaQKuKHMjMrVwQXxlWQJeXLu5Aogq+s2UJn4F4xK8vgId5cdaqboCgWMJk6DApQhlmu7jqSUkh01RMIgCd8FGmv15my4g3i3n3Wl0Ey47fCye5pKPsIfmYtm2GcE6HCWZbD+7hOYUF5ipWo3d1im70Sl+FwjqvqNcSzVZ/wuutzbqIOKoU0GbnvxQ9Z4vBLhI5AwVTESMv2IEzsCTFgSm9bZQUnfyetDSm5IOMYSdTZ9IvgwCEtYm53Gga1+4QCIFcCiBYkWssj2Z/ZxDwP8+Ioop+VmK+JDEJhI/K2JX4g8dolsBmHXTtO39xg2Acggj/vPJPljhkOYL/5gPqy4tMkMp/mxi+Lr7B+YAzCKx//Z892KrtAOD251q4/aXdD5yOGFrqjDOI4FWqN5CiGrrMz9oO+7S4Ktub6Hcyxq+m34sbZXWRQYCNz7lJp4G7mEY/z5kTwcP087fc+2yUttF04M00w/++fUvfmCR2JoPNQIhzck5LDJu3s6Hhd0xMFVAEWddJeAT5xRqArgWibBGtjAltYpANHZWbyRWo8CAwEAAQ==");
		return authorPublicKeys;
	}

	public boolean isAuthorPublicKey(ByteArrayWrapper pub) {
		return getAuthorPublicKeys().contains(
				Base64.getEncoder().encodeToString(pub.getByteArray()));
	}

	/**
	 * 後進地域のPC性能やネットワーク環境の水準の低さが分散合意に悪影響を及ぼす可能性が
	 * あるので、後進地域でこのプログラムが実行された場合、従属的挙動をする必要がある。
	 * まとまった数の低性能ノードがクラスタをなし、そのクラスタ全体で低性能なせいで
	 * 異常な値に収束してしまうという問題を懸念している。
	 * 先進地域の低性能ノードは周囲が高性能だからその問題が無い。
	 *
	 * 参考：
	 * https://en.wikipedia.org/wiki/List_of_countries_by_Internet_connection_speeds
	 *
	 * @return	先進地域か
	 */
	public boolean isDevelopedRegions(String country) {
		for (Locale developed : developeds) {
			if (developed.getCountry().equals(country)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO 動作確認していない
	 * @param candidate	発明者証明パスワードの候補
	 * @return	正しい発明者証明パスワードか
	 */
	public boolean isInventorPassword(String candidate) {
		try {
			String pubBase64 = Glb.getConst().getAuthorOffPubBase64Old();
			byte[] offPub = Base64.getDecoder().decode(pubBase64);
			PublicKey pub = Glb.getUtil().getPub(offPub);

			Cipher c = Cipher.getInstance("RSA/None/NoPadding", "BC");
			SecureRandom rnd = new SecureRandom();
			c.init(Cipher.ENCRYPT_MODE, pub, rnd);
			byte[] crypted = c.doFinal(candidate.getBytes());
			String cryptedBase64 = Base64.getEncoder().encodeToString(crypted);
			Glb.debug(cryptedBase64);
			return cryptedBase64.equals(inventorProvementCryptedString);
		} catch (Exception e) {
			Glb.debug(e);
			return false;
		}
	}

	/**
	 * リリースからしばらくの間、全体運営者等は完全に自由化されない等の仕様があるので
	 * 保護期間の判定機能がある。
	 * 保護期間はP2Pネットワークのβテスト期間のようなもの。
	 * @return	保護期間か
	 */
	public boolean isProtectionPeriod() {
		try {
			DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date d = format.parse("2021/2/1 15:00:00");
			long millis = d.getTime();
			return System.currentTimeMillis() < millis;
		} catch (ParseException e) {
			Glb.getLogger().error("", e);
		}
		return true;
	}

	public final boolean validateAuthorPublicKeys() {
		//新旧の作者オフライン秘密鍵による10種の作者公開鍵の署名
		//その10種に新旧の作者オフライン公開鍵が含まれる

		ArrayList<String> signsByOld = new ArrayList<>();
		ArrayList<String> signsByNew = new ArrayList<>();

		if (Glb.getConf().isDevOrTest()) {
			signsByNew.add(
					"MQjgnTqD1kCkRD/DVAf5Vav5LPXiJxVRLKcMK8UDoxBaboRXk3RGDiaerGHFwXnTe1E4pUcWIMGUvE6KUC//HA9mylk2UyCyb4iD2dGja6L5d3yynggZAlwWrgF5ydM0yIriRJie3UlLgjP/Z4158Z7aXoY6stnjFWH5EKAZJN+CRjrZEQ9e4mAu/ApTgfX54mvETFWiKS6dYgQo0cu5n0rGJKapyekidhBJXc7ixdN3LYLk9AasPtsaQFaztX31ZpVouYlE4jckih+9ZknqgcRJeRiIgZh2aJPQGSO30WrDoBBFvThrfU15n0t+5lTkbrENGZZMWNEDT4PcaETm5MeljSNGW69SySLBI6Ut1Nf5tnPgnUyfsLI9RN3g0Jsn6vVMfAM1uujUma0PTcjn6o2rdUErrySNSPmipljOiG+48vORQqhzr41e3FTTr/P/ShBqL6bcL4vU1WNz5RBxvahTakT3ox+KEswYXhC2IF805Emj6KnRVRHGRBjmsWse39i5w5lr/PrRwQDd0lFeUP5i8078nvrYVtnp3h/e6SlByx4GJ7Wvv8L3fBoKUxJCMKJvhKboir85kWQBDbPQ/CJ5pgsbYWKwGyBZShbiOq7VBoekopwj/4ClzR3i/bRzrOGdpqQEO/CumDevQwGlRx0rIr5oXw20E06mY79uKlI=");
			signsByNew.add(
					"I+U6jsVCPvOhnZlEpsZ6nqBZc2/ZhR/bePGa2+s+gjgfO9W3/qTT8rwt5hpsGiMju5IQMAiwsJZzmRh1+KE0AuoJ/mai6aDn+2eu/RzHNwQWA1GrmoDAAoA1oiXV/iX6QEDuwpl3NIpberDoVVSBTKixGjMiopbWcsDm9xUeGa6spxxdxcUqpg5DuJUlU0Tn3LXggLKEj0JyDtClfd1a3SeK2+eBx+uV7w02QAKMMPRj4dendj+EkCfpb2qH6y9RlA3EZwU1yqN35k/bVzc1b3+mzfKZZmCqA8gt3pEyJimnZBvNSS6eNMuHSm7g6Rr2i7kJPmKxdo9V3iffUX5QHc99YtlXLzAQatSg8KYWiSlHtifSeMWg4DP0BnLKFbbxdO0NipEuNUZDb9hKS+dvCRqHvz5XWXY5oqs6tdyiBzzMHFKs7A4R1U9CkX++ia7cWt6vKxKqU8JS/nF/Lt2xqUivmEsGp55Yi7/mfbOZrrfnK5k28jfTKrJiC2fLMAI1tRbMOBt0+DLrvPgq+uWe2nJSz0g1sU3qTpNtWotyettv83AmZojiz4r4L9Z+MKqFNdQpwFNJcMfBX++p2uZG/uGLQfmx6d+3W184N8EC/6ue1wNQ5aSXVoc0t+U9XaZy0fyEdhqxUehMUDQdg2CCUDlost8tpgPXha0p0IXIzXw=");
			signsByNew.add(
					"MwdCxGRzTObzzcCsxCloNtAMdWnR0zAROt/SDau6beU1dEokLHHmMyxYF3TviA6WlsQPOIf5jTVuo2n0BatoD8YcgrgFv085zNlvfFzKpMRLfFU79pF9hnATP2nb1xJPbUhCJLJ05BdcUwONIhcRqRAWdkb11ms8uJj0pXU3HvirwEfrSo9ndVAK9oNEFvHgQUaAEKatGjnIH9pUe/aMJNGS4Ay6wGaftBkVjoDggkvhdLepzgJs9SaxQhV23e0/3kA0Otc0r1Xw2eTxWbgeMy9CSggE2C2BgCOf3r52Vic+KtO/g03NPExpRmkEm0O9a8rWPq6iy+9EiPKQT1zLp6nseAzq59sgaIqt8OUkvYNalqzCB27VHPaFVVs3X7XGI4Bbe68/832FNi/0iSaVDGxyw/oreZOtBMw08neJtaf+rLs+Z0LBOwt5AgjAT2okjmIaTs6043Kx16/bfqXio9SnfkyLjoM1hnYvmxKdTKa7ZspI0mpwlkupUUWmEBVJUTYpdmSqRh65InJ2Tp6tMM22h/VR7WeSoo8Q8Xrr2P6EnrfxdDv3J+l1rmMTvNL7bnmpSQHoiG/xAAnIXpI3RTb8zo3alKIT4tDt7BN6IOsoL+zQ7HAOFbUsvtZdUxe80t+OhgCtDqhsylEeupyaf3OExtrcNLwY0xJ6M89A0Mw=");
			signsByNew.add(
					"ZxfZN7JSjTVWKtrB4y9YNSMwPVc8sj2Z0rg/PgA078FhuZ2Yy96cTur9kZqUYMK0suV0/aQKPrEVlfqvVLUrBU9mUJhD6j9qDxhzg9XI3saMvii34dQbIGx4Eft16YVm7z0H9gup4aCmqAaGTU1xWqYMU1YgZfnpLeAgTpnEAJNWbw4cZoGZyf4rEe1G6eCtdsbMyNURakEts1VRnGMiLpND9fvrkvRD2bYBnKR6j8TXiAoYQ+/i6zAYdNH3fqVhkPlkMYShJhogD3oQXZNMULeJDnBsbu3KgsQPSKSKnV+JV1+PjU4ilmbXa4E9Xl5hCyKi4jJeqYEFJ2cmJkmkXAAwfsDiGRlrpgns/7286w/xgGRi+bax5t4F5hk593BNKs+az7bvquYnipFu4/UT/5/8BxfkZ1zH/kkEo+LbgEVQx+/yjSwbIXMmtYA/jZMl1LuD3/8s7juS9qTtpx4REnDnWhXQuqqpZA77VBD1QM7/+OWU54TrtRxZRBgD86+AkN1WynX0DLUDKpYHFgfR5A0NG0SvhAQpfn5eAtiRsoCL+mDRgHkLBi4dlvfU0tQAMUsrQaOCJ/V8AFTtRM4paT+xXXKbC4pzLgNNiiykJdGA2gfM8EQQ4L4DTNOlipVzG6hm55B6XsWcNqrKnniYqRNecisbVruhLUQ0kWewyUM=");
			signsByNew.add(
					"cCp5CKjqS98HJhvDunrFgn/BVAkAlKP5dlSHaqsIgF37qfeL5bU4LNCArlI1ZnZZ57BMK3R3J3OttmPDsweDArfXGpoKBu3DmgPTeAYYzgERXTTCYmRaoOnTYidoAytZC8rzSWqY8nktfB+ib2weFL2Rpl/9Lk7YiRyXume/VXEYyWpnqOP98vsxXqbZ4QfGlbdiiNodIm0YQJOzZY5+fNnIMQFw0jSK/7Lz9oIZDMh0mj41ysjBUr2cLhWVP+JaJAy/RW/c0rwExEqh2SyPCjKv96BBhVCerI0mEvrUSRpS/M5oEFlgReq0z/xxP0aCa23DOqI6dZ74DuZpme77QUxQFcDzJGy7D99FUynMeCTKirtOU3/dOHAXOtuL2mFyQkTd9K8P6spj1mTb/aOfI0f4UUbsVIPBTleizjPSyD1vbdl/Rszdb7ejncJaxx+Lns4NJfskdJkUlASCFjAPDoTDVDEFISMZcHD2/pmwHjiJOTBcDEu5d0AUNjEaLchSYYVFhCFwcoDCKAFnbf96KgVX80pGUeBopUp7PPnGAVRdWsqzwmaYyT5JBql25IyWbmZLE5OL5oLwhcRLJhwkUTSJbSH4Gn4cjk7skedRRYGDexNk2PIJ+gXbr86iB5iN/dXHhBzRaUHeUhp36y1WSNKprLrr5WCVnPbd1kCliJ0=");
			signsByNew.add(
					"h4pi0vFWWMktv5ORXoqB/pyHCO2dPV39h4YnQcIOJdaV0xzEYS9FIkCrZAevIxVYt5Q7L7NzINJb/r7pHGkwE/0gZyou5IRaHCeg64r3lmlyLqhi/B5pZitHnR3kTfJ/KL1CIczSZpGDWmnzWjT0nCLYgf6Y53d9awl7HhvHdJ8ALIEWRgCyelXgCvJdCMXEAna55MXQu19Lt0V6K+66a0IF7XhZEiBJ2dbHbk9+V+b1y1Tdn/z00Gel1mY0F14aC9+Jhu3JXfIrNkvUqHfuPg553Dv/ZSYoi3S8kAukpVNIFaihPi+0yIE9zfvBMbdhBK2HFsRhIoVEhn0rBoajAj4c3AFcya4TQIAy9df0c9OtNCYmsOatDjnUNJLcF6AP3aqLzX0xETvo7OMua+AdcwrhQWioP101neMgeyFv3jrjpp26o5B9+flmVsJxL0cnzjc9rB400pu0pCxFAvyftP9Hly2IkIllhnQluTdGGnxW+GSiebQdkj6t5tgYnc1MGRXKM8jBuxlxFrlwpoSGB/E2xCa2ulFLKgucVwJQUR6yD1V42IX/73sdCMqlxOgDYqOZr3w23GeUPAeA60lvm3HR+YOS5RLfLoTsr8UovCFXvor3PNqssYQOBkV/TKPgfrx19qhYQbrsSoYEmgigFgihhWj2xevOkuwDwj14yhg=");
			signsByNew.add(
					"EkUvJmj2D1ixn2ky3BEysoCgmGNMrLOYPzX6A0h6v7TBp2U6UpQrNKRXRLSrG9VGjo31OvbLjSshbOivKhN7oN85/BuhWn0YQyYZJ035Ij1+qXeuk1us4WR0psYK7YoaC6FEV35mlsV5wTBIrOT/oY01CzV6xrV/y3x+Siyoiswj4StUqmP0MllbnJi0Mu+69oO62YP62/RJpdBgVtaQXJRidjSsAHiPZos55ln6ZDfUwI3b6WaS2IRztcDq5Vgxt9ZCtMQZB+VcyxkkXUyrV0kEcsnkmS6JQ0TEFtib/IL5MEXRJralM2Sjlv6VTWTW7+BWW0L5L/R31qO/PDbthzmOjL3IsCWAmkCHkLehTqQrKwyZxGTK/uG+JUFYX4XEYhEIVSs0kLgJ85NHltXnSoStfgZFqQ661HZYn7+w+J5w/rCLG3iaDQKZPVToK2lZIYO1xGphZrdtBDvp1dGmp2f052eIQ/+COoVe2O1dTs8o85holt4+nuxe+0Kh2FGSD/HiVuHqDMJXuH4LTxeJ8yOn96EQJJQ1/JEyIUIpP2uWoH31QUdFsXTKbeHqQsMUvNF5rQQ1Pts5nP6HOLlauUIJrCRtQNysxtwCExn/W02/ZIsHBBZThRVwmtIZaL/d5xKT/0gBF+iBvt4wwRANHOpQ2EgIiWEx2qRBe5PB5Cg=");
			signsByNew.add(
					"DLkNBNUcAo/8xA4skCeB1IPmST9B7bEqVVxS+MJ1oS2O0x0ej96j5/riLgz98tSyxrEKKVJe3SZc5ODJbWkMzFyXGGVJonvuskIxXdzSYhSwcGSPFvT4KGB93y7pl8yhu76a4Vjnf8OwuyBXhXgKaR21x2uoRfzV5STWDfl8myk2SdAoTBRfnYqeGejK1uoCSy0IeJZaYgzOBAjaXe+vJAPiaaYvvejZQ/lt9vhXueKpLhpQ73rCczmze7RR39c6bxZpXbnDBYCkrDsf1TCSKRdkIJZRxQ4+eR5ZvImTQnFfOfTYXPkZZj5Gi2ZIHRtT+3BwP+Px56Noie2fqy+JYJQGpsVSiJmnJokul/WMSjDJuP1piZbXp92jGdWD49VQfFao7d1snWkzvRx7GFAm82AqS1pbc6Y3XEJYhh2SpHxTKhpEU/Oez6rUjmNjWxTgLEpSNs9vQ2ZdXM1dguMx7rigfFBCdAl/BONDqsX3KddIHxoLR5ssEnwbuwD2+G9woURMr1H4Kj0n64YBuzC5hlhKyC0l41OGG/82lgoqm1x8qkpRH3MbmT9PfF962Viz5lQM2YKMxG8Plcs/Gw9pm4uZUnosADzFkTdInaVvgy1cH1VySPGlZoXWgFVBf5KUU9OtgNIVJ0wG/05f1DV7tBzoVPuP+P01AmilGE4PqzQ=");
			signsByNew.add(
					"TKenrJfURZsNAx3Vd//DFIYnoRUqLA5Trv/0+RkfeU41Yy6es8emvTXeRh5Ew1UvnNzOOOthpC+u+Xa9ZCXG/W1dzSPUrnk0yoa0EZN84ohK+E8OHnTm+gRRVrNayJPk/FKG5h5i2EAXOn3BILTTjlZlKjDRDnVHeWP74PQyLdvwUDCibtKOVD+HN8ke8r7nRvjYN4fgIMSINrUd+RxxOa2Orn7c3CtHrZnS7QIaddHsTNyRLhfQ6yba4xRXISFHEnCXjMFLd6M77xugwPAKtqHZGIHIk4zunJOUmw65QAq9ki2G5JJv+Un6bF/hbp3mZVNZzIo4VEjx2QU3LrWYpodFb152V8grR+a4GRfFuDpd89BdM6hVStT9MTTOtwtze4gq3GL7YPxOhqCnxRgCWyj4yTJAe2qfUN0jV0rJHS3llc+HR4z/uq8H07ROUd8lszsvYxP6l3KAj1fMv615fDjpTJOg/jutKkonY/SiGmOx3OWQhWbakeUdTyRP5Z87omaOx9I/P3OZuOAyoT9CuvelmjEECJxlm7L1VOpsJETx5JzrNkrnLpCrGzIDjb2ZL40nS9HPaE57Tx+IREgGFe19NtgSgCbmf4mXxDRLrKqDSJdNvBzecaNc/BxKaQmobamo7nFRXV+P2+YNBzPUpxjxz/dF14iX1dw5+vnnges=");
			signsByNew.add(
					"eE6uu5e/HsqT679STDVp5m68aJPcvFeYu0uZtj9egocR13P67u/tptNoNT5UHKfmSxwfGvMAq1IfMxDSum1FoR6+qHC8Ah7sJeqc6hXraRFG7tiM/GcZ4awBuUc1s+U7jEEnJskEQqyQJSB+d/FwgSNCsTrsnwURQKpYukXSg45qdxBxL7iUm8HfJ1KqY/d3lxWzWKgZlE2U72jr5YfSPE2aBQutn64G5noxSCpFxbF3rRcl16ULQ5prusIUpwJmKPlhLdb1+PTgyuKCvZ4LKLfWc4mIPV+7nIRgQLgWGOdXoYtNIxlsJY3sXjCgTkr4MagqS73lnPZy6dPdsmvOUXy4rGhRiC1CYBF2NlDolHHznoCX7eyntIWhqRyDrDysdzrLb8VqhSfsDIUPQA2C4MUS892nh2cKXNuQzYzY1I0eVjSxZZQh5Sg2JGGGp7NJVvRApve0x1zxFY5lINp+lu6U8KxKTJ8lPnLUqxAeCAC6AUf4xCau8jkbgaTNMBzT9LvNvja1NhXSSHMRYwMfCHhBmzueSM9mgK2vdvD5QlLBr8voTpxVnrXRLAW+ssM3NIYnz8csQKyEbLv8/ZUmVOesj4P6Drs8OwqnmZ9tmanIUCQPJyEXREON3qttoGCS2k5izPAI26/82RQP2uggH5eGnC15PsJc/MKY4fMkFXI=");
		} else {
			signsByNew.add(
					"WajH9o96ysDRgz3ZWyhpJYhWIL0Q7mSf5DaPReNSkEHTVCllEqRWf3rhBi+gT1UoUctuIXLFMCQKnyQkjpVyoqnhT7qQdGtYZYqS/54qRYLdgcojqH/QaZzdQaji7QbEElAhvrU+hTNlHKqz3XIJ7R/d781Iocxe91iFtPqjTzj2ZuD1JOVibt0Xb8hhc4vv9prIGD6pMP3aA5+W32ctjY+aGll+Ki7owwUFI7XJCJyG//B/eBmRd6e6v48pB0wX53i43NBp6czQhwd/OZTnA14bzVHPzhKyN64XBbyqY/p3860+C7UYZa2cXVDdvXKsE3kep+qxL4iahc0wRTp2GUPWK+Nozc9/5G3/mFQxu1cAwwov1CQEaroC5v2VaoEC/ezJr+MgcJLMMj6a0e5HBlV/lOvaKR6o6ueYQ2IADa1TkfkKtDMpiAB/lLLoh0b5k55jE+TW4nc9JJmfyFJNjHkipgrlQxwriZ3zph/PCm7DeqSNrC3Umb99RDtxFaqCjd/9OJZp/uJ/J7mJEs3A7zLm73HIyzqs5rt0YPPYOI69CMEEeBVPgKUyHDybvJMiVGxKGqAFIUkWmDVLHcpY+c7g8JTK5up4rETYFydP4b+NAtS2hMPlR70YftvtmQ/a8GuyEdpoTWZn5lVVz2Guv6xMhdtQ4hccLxpTQjPoP2c=");
			signsByNew.add(
					"N2L7VwEWLIwhI/Kv6OjAwGhqnyQcB35cOxmGv5BD4i/uUdFfLTV1yI5sKa0Hr6Ddhy8RxJ3paRWYij83CqOSgDq6u9ePERB5SOOFhEFBUJpi4iM//0fqvXM1GEx0giRS3eth8rtWBrYCQUD8Mqce7go8G8cAVtRZQY6S/5CRz3LSvX9mXquK3jmsBcrCgxFRO6snelDEsa/gKfsFORbgxVk5wZ+O8roC570tTO/OcJ1qjw7yFrrrpyieYBpuLbhf82H4JgbJ5CiAadxZK0dAkamHeq0vXqCiHXMuM4PV6se7gEqtu+BqpCBJrOsouLpjTj0g8Prn8tk6fvYs1qmuTQyqk7OYXthgkW5DR467mrAdFwUjweZ08wVM+riBSaNzaIHmgXTWzfe+QBqGlL6GhEIqdr2GR2QU/N+4/NGOrpo5YuJummm76MitgOe1FfbNZ4B1oEBgoUTCP4+PgUZaFkSct7TsCCJq/Aet6xxuuPfZdsqgA2FtC25LY2k32pTx02wmlZBpYZYGW5pVSNFvr553vxm4u3tgWnBSPVFysz95CGjfTAAy2TexIDnRuVSanUWST+Z/Hv1TDItf/1jUvyywsWYDVMNgskTmpQfb0SIXnojyGm2Qe4yFpa5HTyAya9P/vLFfC6WbbO6GZsO2C9GU+cd3Y4GlsP5MRv8Wn+0=");
			signsByNew.add(
					"ANsDYHQ+qZpSuhtyYxKj0/1icLcsex9gd6pWZEfB1e/p0yzqpnoOUeccqTjAnKC2hpQgjU7eo2iE7B6ifCSHWeZzqAPfCHbX8Yt8dCDu0vWS2XSJrnr+jOYkCgxKD4fWOeG32NDhRCiDUib+KPcbb9l1wTLMA32bYwKd1cxUcupIfGtm9kq6Blw0XqV40D1AfPMc4Th2q/WcdyzXkhDSm6WNr4PpvaG4oRi/FuTrs7PAiEgIzp2S3HLTW2mcOFYFziLnZj4Yo5IYZK/2vHhqvxztDRxnw4WXC7IckNbcUVMxG/jlpOdOcgs+EtqfJlbsi1o1bsA5KaDEC7oWl/9P3hbh+qV6OQ9PpEiRdSRJVYfdrT/LTMFATIET0b5iXdyZHIVp3G7p9MKeRcsCZBrVDkj8V2erygupqCKv25Ou6XihPSY1pEI0+G4w41ahOCGWLcMDIf7yI0afXrrlov3T6UW/hWM76obYDBvhzS56c0psbDzr/ZDaQPWr7n/3QCpv/Xnhdx6HRoyxyN47Px5JvT1baHKA7oTntgroNFjwlWBmD7rGFKQsABC0RlGGhUzif/usUo/92zzkU/OeJoHAQsqmpMOH1/vOnfo4rRoCzxyOPgdi5A2kJu9XVeFmrUUxP1Re7j281vvSdlURt8MA2j14AMYEtXJWHPR0/R5ZmKM=");
			signsByNew.add(
					"BkuOO8D0u8F1etP1QMG4WGUXqNW1eh5SMX6xcYuXGW/fSWWY5lT3A+/Ie9VUZM+ftovY6W9Pc2IsMuWqGkxnAgwnlxdvtdnLclvRix+OJveXdeefjTouzxBwSndqK+8iUsm0UsWjwjLYWhx3DyLFPVNhmmA9Dba1d/gG+Eh4s8bKMahf3XiVawEj5QQhhbJQWCz9RWmVJ1rRls9hGASXmw22I9Qg0vXXZ4J5n5dlniG8xtQlTYaa5Iv8VoAqnJsonGCl9v2L+OqxVdQ9PuplBUUu9VSH7z9eynOTSppe2DYjAA1zULTucD7YCmBfN/t2t/fmQyDtk1u+aHJ93msbj8oy5Bcl66UJB6iddHZ5iNC5vbqwTm50ublwBvfSeSkH25qRtvhICSLQNdq9/zzRx+Pl1/YV9LoMIyyfrK+QY6OSd0M+hKw+VH7b/wO8G7vfbTnK0mRzKx39vBwiwqElHXczG0EZf6tlVmqota0+4rKD/ffeOxw27fF/NGszpueR3AgUfLX/WqXMxPws3bM6WUouZ//061ioTAgFO2Ij+84iAqcGxbizxNi6yJxFyYFG9Ux+h4xT/MKLFfsEEcs5iE+po2Ql8IGxNwm+8ar32SWAS10y28fbONVvWuBoXftZsbozOpAxjTqo14kK8Wfft00QZSEmFu7EPCQ/K7cQ4ig=");
			signsByNew.add(
					"jiN4MsCkiBxbFLznqqb1Gxa3rCfS+lEN61B15iAg+SAMZmq+Qu66kk9efcJYg6wKyAEoiVK93+73BOKDBPeGQMuj19kIV7xcfZ/pHyKQVMcITJcfL72GS+zDaLHLFRCs08XDBnnu1Jfw2ACtK/EmyZcgGO+sV/5l2Z3hUzF6oholZQKnT3Ce4+4PiaASRG9KBr/R14q+u2OwBMLX54L41sEmppkOEqndHH75wEz1k0EDrNs4uwNPMlGRlblIV6uF6uevlFd4YNLQ7a3SQn6cRhsTxzh/FBBCn/M024NmHkyG8TCziRblBrANixiNqXZobgjicvnapn8Svwem3RfUrNrjULv5hKTzm1Z1S7XchbXfpkuK9Y2GMG9JmTVfu3/1F6rV15+5rzw3wi8osQ5lsSff8q+ZAI5h9Zlvhs7ytzXeJLL4bnNoqryEC38WoaJ8udBKCb99nk5dkWZ67Bt2j5zdKM+NFfI1X3lmjHQrss/hLorFwvdCyoxvidrExQSHBdqki6EyHqfao6l4zgPmgsHp4b9DMtxQpQ3ukwrlm4RqbZfFu6SVcTptbUQEP/WEK1hDNGdAqwqmtnpwtEOJx3bMOnjzg5KJ7vxok3mZtEd+oyk1l4cUvGh/55psMMFz6Wakji8+Rf6DD5A/tD8ROHnxpzmyue1+br1uwo2eU+E=");
			signsByNew.add(
					"NP2KWbaSYe0UFpRbDY3DX/Znca+BB8HDX4CzXsc+KZbMJly290ygLDhTYsWsEdk3avVIu3dUX5wPIGtjJDlfQ4wZKpQDPnvGjv8bsRUlVTrB9Ypya1zny9HxZHXdCtDv18UUwO4wv5cTtxx3sI8/P7pmjMqDGF0jVgAN8xMGtKrcmYy3apHq3ShUMFHNYWzZY6U0nyqLM3ryouJYMpaKDDNNARRTIjcCuUGxlO4YYM2zFpTownGCo9p3/Y81GR1VZzcSoVNIEby/Ra0yrBC9KlHJgj9ekROdEMgnL675EAsxtOb8q1CQDAu/gGTB/m+hoRXp79LekZIxs6Pevi7ZDbcX/Kcd2vSm/MyN6rnjFhS7SMCRWgZIxoz/3tHruKg9WMm6RSeqyliXGMN/90AbujbyZ18qdtWbKTgJb3XCjFxC6g3XfTJE/AlxGvAkU8uuszmpq5VPxk8Og4iRQ5ZhROc1xIDRK5s9H6YydDEIy9BciAsk0td7tEvT8tsX61iC0pnMjuX/1eTyJmaTNNwvS+Ofr+h1oFk8c0tzWl4E02PygqLJxKfZSoJI87qEuHhIfBM3RufubBIEr+StCLBtRx2cvGbCaR+39XVgr4NZeBh2kbnuaVo+lTC/mcIgY+uQOPELCWCwjQSVs1fLBbnmoxZyQ4i9mAIJyMRMbmPKiHQ=");
			signsByNew.add(
					"RvywpERtWNfEcgC20w67hvIx7nEmz8NA19PPsSHNIHtCICySW5Y3m35JIaprj/N/PfYYZwyBKT+oHYBw8sXD/FcFi2PtWu53tc9F2rrpdmj+9L+cjFfVGE6aw8SKIkV8rVt7i0XVwIuw2UqRgCpMPgAKNYZhM367/5+RYLyMUSTbhfdtPzrjIqytJDHlnV6ytBq1TfjsK4M7yGcwJRumAKuPAMwL2T99+lMpeZCB1JDXnAVYQw+/BsPPa0KFF19HEFFvvRZtCJuP7L28Z6U64h0QNHb8W9i7ui7DVNNBk+lPBjJcWnJOnoWMnj42jfi6jAIdhCGGiWKRn0YTUM5yOSlmCJ+4op2gEFwitbKCeRbyVvklBtgY9A848une0q8vX8Q2eSwnEhmU6s6hHdoQxXEWkpGtT73/4uNO8xcids57S9vp7e1/vCpu0esx5bBfvCMc3hWyPmWFkY0NEbxwYBSOoMHYysSqC/A8Vips747uKFoeBy+WTUeZL7AfqjP584YsFHxmbJS7sv15rKm1uTotpod8q81/Dw2CjFYZtqGcFAihmXKdGtpTyr+WAc0Jl8JalZRFmX98W+K50VwYNQRbzEGSC/VyYwP2UpQ8PAWChSzsFtWrSdoNg3A5VV8iiLEibB6b3JqA9IAa26ffTs195wQRjE3vGsJYiQqa2tI=");
			signsByNew.add(
					"LfWfc7zL74VtjjcDidaE1JAUma1lRt80/+0SHB83VKu8Hj/2vtj90qjRgEus41Bo2FOpCksxIEiSFWRTE1dHXO+ZKzZL3BF4AS+pFlzpZI/VIQHsdlAKZkljC83wrg2M7Ka0yUDSInNfqI4zjL1l0B5PMUnjfweM1SztPFKD+bjlPcF9do4znaYLAELWc1W2xrl44Iv6L5lGyHsWUeUu16mg55TcjTvleE/iXzr7pv69X/oi1Not2A3YtDSUZv2qXgr76EnaSitkrk85kJBY4ivjHPQETdgXYs7pkayJyNBUmUspd/krVEQnpXu0gN0vaYPsO5LyMlmfIjzcj0pgjMLvTm++M0EI8UO0hAc15Mc6dWzDsYyy3i8uQAjfS8qYJVYze5ASr5m+0jP/KGZsQ8uA8Uz3iSIHCDNi6uUNMKlBr30VUjePhh9B18q+WlZZigwzuNRg0yPe2XJltZEhQFkVZbjfn9B06brgWLSbATsvEOfjpJ98o71CxBicoCdTNXglTtckazCczRrwOYOgFecvvHXGOayzfO5nDrfNQFKdeRVSq5TRtJrH5VitAbFGBOKUbgtzvrH004+t+kgmfoAekWcvT057VjFySUIVD/PbMcVxBUWkosP+i3HQLZpo6yghElcEh+pdfhv7qKJ+TWgGja39FSDwTGyxpHlZN3I=");
			signsByNew.add(
					"EJFb3JK6PLFri5ms7jf+OvJ8urp+/cNMfwKR2Hw3mGoujKcrCSCIqRa3+wGeoYVYxpcfKO4CEAjaxlXR36ycnjd2m53cDhhvRInqi7ABUO65x9/tvfTWNeeR8ssLO3amErkTClNCKrwuSe02b47Hj0nhgGqMvB2x1sqLNk776h98wnyaBTPmIo8SMAJ0PGHQmhoxxIv1q4k2mBoJYaYYhrawMqiRO1APdyem3/s7/Kga1FagW3uu2rGaMtaOcg47ue9t0LUl7l4AWer01o/xubvBlnsJc+SOos/+U5B1LAN8J5RZygmKyAKOL4j0Lky+j7paMR1B3YnSrBfOzSVymTNrc3+uJY9Wdr8DHcltQjrlNpnv+yTB1pOuH3u0rxCruV0o/3LlwvzmZWfrFRjTactzcikU6mr2lIhfK9ggU/U6ovMIavBR7x34x3XXaJw8nBn4eWcn1RNeVyj2fBwbV57gFO9H9+oHETlHqGssxBFpBTi1kTGaPQlYh+92URbI3F9GHGaRfUD0dVZx7NPtAHc/4HV7Iy376Uu3NhZCVb9mnRtMxiJpBImi3/St8Hb+axnB3hNnmGBZQRNgM5dFxyTXFB9ommGLaqc6HmZ8pEmbFDJkmRpshYN8h77VVYrv+68b0bi9HSzAPYNSGbpEBT9cCKnSpqdbRVoIPRcClgY=");
			signsByNew.add(
					"LOemk27uXmVE+jf4Xd6mqGHcvDJg5b2Kvpd3RqpnKvNKTJBOsuwbbwEzgfQoSvlUABQRGp0ltuTzYpPF1p7/X2OAFOCOfY0bHEGT5YbbYXVcVkam0VEBSTmgtV63RLLWWoi61bS+dEIZRZWZwiTwlNzSjsWt8sxIEiPN3H+odJNvMSqKQjFzW8L4yW2Af/WtAuMe42fC6HRBsvcSLRnA+GlDtkC5jwImYI308tcsLe8mJr62ZAItwACl7xDVwfExyb0VMDAAiiWArQmSKJb7b7ntFzqqUIosK+pESXibUEce1tyj/Vzzr7a3ZreEX7xMGj/ZC1SsO7wkl1m4AB5k50SSwOe08gkXHCXeFjon1aY/6PNSDDjC7TM9bPQcW07oLpQzXGtq3tpfomo2u226eDH/v+Hb3WXY4+JXiOQXc48EffNeu/AA9zugeaBpy2FlisbeBcblqOvODzx/ssz5y2xJKY/idmsxDQGvVWbr+s/qfw+3al6G6Q1jtSoJDh5aeEdTbSDe52zrALEilXtL3h2OYNtMBhPbXGk9zvIMLcRqz8tmGDc+TufkDrfhQoqChYMGK2SbfIjY1VKq235FqbwCtImhwJsxrI80RBmye3gVawQ3Wk5MfICXD4bkeJWANsfC9KkHtVAti0YEw2JOMAbWLYzNjVtVZ7/W5QWU8bs=");
		}
		signsByOld.add(
				"ce4Tf9tuLczGvz/7TgrscoH5K23sjaOQ/QCilNfznc7e5JKhiENEXOyMAKDZOgz4QkH7NMVW9ckarPBH7CZtzvyBZknuOmfa+JMuWhqHpbxdyZ2T+B3c3na9F6LTQSn2rtLiS6z6+23MQ6SsTOSR4TALn/k66+ld3s5CZBpgjuW4hy6JsFKnOWBQhj+1ZlUJyo8mECJ7DwD0rhyWfoN5vSAVMvqYRwvDqJ2+SWUaP9J+abgykQcztsKUFTzjefZT7/9hvFTqHEKPNdw4nw0RvbcSDB5iaLqOX4jfdkex3Z4MbmZU3aFzErnFKVNO+BCR4WHFXhcBuAtAkp9nt7W8qA2RCDKJS5wsDkILqxt2MwkBkP5ILTZu4gTxbCoPdJ0Td9I1QeFUykNxzuQdHJZeXjYhfrrPnxBbr6YYjDKWadPRToemOKfFjoMt3fYeNKnSAPem87vis2xRPpOZ9R9C6ShFy2des5JFP2ogoNdY6CoA9x70Qharx756EBYnrmzwTk6ijzSQk4VAS2a2lrtU3NzAPAGhMV9eIKnH/VAlcekcZGrvTPEguTLDy2gvJM3djDWnzyBbAVfnGBsd2URFDykndblsmiFrS41pQe02YrVjFSCFl5EZee8aBviwdyUWpdSF//YIzGCYN3aTj4WPRKsk/rM+tsYr4GCQBFmFlCM=");
		signsByOld.add(
				"XthC/KzbcZi2/sRRtfKB0HreYSBvOLubAmYDVp9DjVgRaDLPCxvyy9AGiaqlzED4G0Es3XarVvpVziaSqtOGKObcsGVHh+Kr+N2LFuhBq4gKsFMG9X6vZJ9Mcu+8pzITE0m4w5Y0WR7boI43ygPQcsJWh5YWuMjCLT+WLno+xgh67ugY/TEOfmlY++68+Oem51BvR9Wdu1Grrn6jrPFeNMT6TG3hzC6ujWfKEV9NtNGS6oBZf1/PsMg2kVf5mSw6VnPVbGTxUtNnRl2DL9C1+W9v3YZ9Pn/9UQy+Yx9NFQBKD2oRQyXNKULwn10ikOUI+qwIqFV1uIreM2qTJxxJozpopPjh1NIO2C6MAlxV85AirNCs8WsO9ZTSwOQS02DWyEXK2KvmOp+K5qBE35imYte69yXnBLrUzgxXNXtXO4z22t3vbNI8EjqxIPyPJOzcWmboIUBMPEdWKrBf/ubBtxYRd9CDdsQ5EUqexnushIPjy1mwy2wg9v/i3XfuJR4FvYburpTaWdtYuOs+pljCVzKcIcz2zm1MmB0udbLAwBYf/nvwNBbHmxe3Q9IK/EblZwOsn93VV77+/GzIe1RyNADm1sS0TJWRdNIgMDcx6xA2qUsfKGzALVzmFvMjPmNs8fMnr6izUKFeJT9LVzy+xKufRo4Xvk72Hz6eE9EZVSM=");
		signsByOld.add(
				"IXSpPKjUPmqrTEDceiqlkQsZsBPR0eIU8RRzT0zJo16CxMl6khQvgsQzoerIg0KJi3sTSgPF6I4SbHmP4b7lJ344jHR9FPSJtOZ4Pyl/AjMWARNtP60exlIf5dpS55XU95CeYFfBqzrOvhsAwa1bPSfUxameHpf2Mm4eiSWYuQfdUFig+fTsBYxxyRyxSmq7VX3nI5WKve87e//f8vAIZv9AbFn72DvV8uSwXIKmuVihJsqCXbLxAOFpHTqowwlZnZmukqc2cU8Fb18/SP1pyqp9cQZBaWTZCqwkMSdhSM3Tc5IFDxegMkdU3FhzHrlx65WNfSlkK6Qly4TMTU2zEHqoJ08nC3qawQmfTLN9YbBqnw7uawkeT/4CNHwc6JeEwCTLBNnFpsAPz8INo08NFO74+XZ1pUWtz1SyfjNGG9zAeOLv6rngj9Bi4ktev/cLf0XQPSKNgvshqq+muaU3tjvmfK6yge9tteV/UG3SII54ltNSA4fPth2aNsqtG9mg6L1gUuR8Rpc3eGPrjCwgNmWZJw8zXa61wg80Ki7+XWUOT00ta3hUwhO4Rf1PaPdod1XL7gw22cVbQAnJ8vwDOJyHPcQoTWbOZMMo0/s/w86oLcbtBnNw5ddDE/xQlXN28QaQ+Q8W27/VIf3ygO6TeT8x/eMkjfhUgehCfoII/xo=");

		signsByOld.add(
				"TOEMxAZzZuQRFVquJgw4aq5/8mhXH3IvHNF5uzon7Vp2FnsBu+IXjx1eT9npwTzmru9oDv3iPIVd0y6iMUPFdXw7Q7KN40lfvLWvPmeJPFz8Wxg76ywnEFOxhrgoiZAgz8W32KEV4VjiKOcskmnWCge0IMNGmC+D7eZ4y8nZUC7YyU+B+lD2l3p8I7Jq/+vRoZHEyU6/NJ0N0bIap8ktWB2k2vqf6gHjMF+TjJK7LXbnXv/QrOx/dfxD3Ss1fmlj4A+LHppvXRU46ePzJBxeZfKY7uiP6o8FXqsx9DeF/KL/3EdsY8+16aw/p2cMvsFyZO5u0hflBcMtY99nw/sYYOYIg2uYUdYjbkTgWMEJccq7aDJOVJYPU3MeO/4ZEDmgVLKj6a+5bg2P0pSe8nm9DL0/SIVL/CBOjIPZuUI5YMi3htPsGU7VU56OG7q6u0q+0MehYP/8M3avf9NXxowig5Sr+zRlnJnGYTM6QBFe3rdqfkeCvsIE345WLZtybkOmxyMALfqHYn/x4I51WQYljZT2UBxq+iGf3g8tvUNXWmZ5ysSI+n+SwnZS8A70+B1levyx8zbI1FTXL4nZjpwsYGC9l+r2Cb8kPo/V5tHGJIyyGP6MADVQQZNQLZnw5xhJhsO3I+A4N6r9VtrlNdvB5Zxe3iMQ2DzSax2efhVjLhs=");

		signsByOld.add(
				"g8dezqJsRpDWFqYS+uWdq0JVmBjRh3D2Al5imy4PnCCDjTfYXWuaVokXdePOTCNKuzbwfSLnXiRmsCN0tb3gkC7fIVfORvyog2U8nAC0WETkjSWvMtZn2Y2enAZ3bcG6kJ65zoR7VEhlYrbD01vYfVcUM2mthONMBAbyldJeSG7hrYSESH/WKeQP9nbbvDju+zKB4N35+HWy8znqBHOVIejOg64KQ1J6O2yNJJR5PYE2owPGmouP+qpZEJTz9WOBVY/r0Wl2UL/ZilzGkxzGGuwSArhmlLFd6+JIwuwBg4J7IKn+UmEw+DywD4YdZ+nstJC0PQo8Td0sBoHah5WWfrIm7txN7bN0eQ3CtXm0eeRHc9fyIP06bhh+1wGzxbaDbnveXpR5whNCBmNTQsjIYW9Sj+myzAFj5t9EwuDGLrPsQIjzbj0PuZaODoTVFNBxBxeWrrjxZwCavPDn436X6U4asF20/oagYgKlvM+B02b9bnoo2V+WhuvBkSUNpTUVrsq8C5c1FAmOA5YMcUp2iJFF3s6PWhEgQ9U3qJaMlwYk6InYVcyq2TxLuEUCVJF8Xmeus/mqFQYWID6H8mrGzqNxO261qH5UhBb1sYeO8JnRT49NH7MbU6D2+r2d3+LM7cvhnVjhnKmOwRI2qGFTJ+x7sPosmbEuPDwlv0AuC8M=");

		signsByOld.add(
				"MylvEmbRDkfNTjd2iIOieBcc1cIryBfO+wzn+6NsMxPaGumu2L9cpn0rhWSKCQ+9O5w1ufO+WL8pX86qXyG4YYxjJUIv7+lPDUqCRWqmwQs2YGPucyicf3EnXdyY4Crawvm/R6JrJ1MT3aQhKY9D1X+atcjL8T0FibczBY+da/QEC+VvNZ5raBebRXDjb/FDJ5elgVxsYsKxEiNMMaZQVymGjNmbPY2Zg222PlZo/DvZVnkuXUapWa4X/2zUzNFePsmmRgB57Ale53Yjtbk6HYgBKdDLouSBOqS0mN/RHDpn4pYGC4bbyDRLXg1bX473Vnfqye2zJXP2M6eS/sdLwrjBZol4psH7a/cTiznaiB0g0qcEFsGBfOMLehT8QUbyToEJYcPbeEOeG84UGpZF/nhj53Y8sWPWKbJIYz823tIIVt2patzUR2ur2d9ljQ1Zpxnfj6EklLItbrhpNv/PDwtPtiIK4qxxEJ6tRxre5vs9GwqFknJVeHhob8CXeIfAz8GiYP+llgkeKK77WcTsS9OrTmuMX5B/Wbuyw98Px+Q4zaOugPiuWx7o3ypfcjwwHsK6HZ85HLIyzoFOGaU0hPjkiGUFNMOzeGm1IdgrEL/HTSVn76v7sxDMDi9+6EcmCepsj3mqF0JZT0M+NdVy6s/bGae75WKRdX8AY/2CrSg=");

		signsByOld.add(
				"cMR278LeC8S3RcB7MiRER2QYZwlfS2smMHIjK60BuBI22Rv+4/1rdS9niq3cGKG8BAuMOC2yQyx7x1awEn3qT8agJWJrEp5rMTa7CuVcBa2Gx6/fL3WF6WR5SsuUlLR4xhc2rtQLg6DgEli/V/QOgiD8GWLq0tvLBvLIQzaCMJQjqJpzFRpDj3TDrhTRBP0V1PxKZZy0xPNoFoqfMLovDHh7I8/9VVuxe1Nh5I1Zs0XMFIEpt99LXBygPkpzviGgyfb/kE/ZeBCkH4V8X2xaLfVRSwkLzWWgmD/0AK8l1PxnJUEJOLmO1uxZtNLaP0m85BywTUd4guYN8TDQQzSFLVMyVQKaZ4VYUlo+7MgSQfKpKiksuGsPGpbM68By5YFtKMBmwYnAyK1WKNi1N/SyBDPPAU2hlsK/MUC3Xw+usiWcfiPhGzWTtGJHPrWCSvbya3E4FSUZrTr4enrRScyX5lXnJ/GFQgpmiiPJeUg0Kl8NzRjz/hyG1af1JtH5KuTl6YI4LBgkhyeoVtbs3Kh2vFGPMJsaw/Fb0WiO0l1QqMGvMVTzsuH0Nayd++TGKa88j4BP2OxJTZxeN32bWx/wJfQNDtg6FP9dZdNtNGjpNZ8KSw4NCHxdYaEms4y4RSejmwLff4MSjunLdISMgeUIBk1Gn63sCPPjqS5SSsvbT8U=");

		signsByOld.add(
				"B5hZtDpFAz4jnob78qaHXXsGGaVgZJmQWPxQuv0TE8FbnnaDlNIy2RrYQ/EiHO9gZgUdPQGw+y3k1UDjk+vE02JSJJoGPznSmYUlmWIcFwdQIUg97bNebwoVgiLI2pD0q4kcK0lXCI0Pt+IStXLM8mhZVjtJV1BuLaCfppIz3vZ0F2Q4x4zVqItnYbQ80Ncs5OWhEAKdzTS/kjuY8lYM1RhoxyhI9ko9yy/jKMjyfGa2uVofbmFkKOS6FjBqT4zSY2vyD2kfTVm98t4jDKp9zPOwixDebtPjaguXbkSWBwyjXhsNrODKfUkoE9a1VsRKcGEj40duWnjOFF9afeeLukCy/HexNpVZvWVA7DjW3pVoVs7ZlL0kXn9Mqa2BWhQWQFUZXdMZ/nzW0IlmGA826fnFCaVpzISLEeCpUS1Vfd7/HVa7rtyAu2hDzjD7RokkfL1RtYKGhgT3kMar2UGM8JlFpv9Fhfi4Hi+RK0t2mWy3vUFxkx6NYkl8AcYYj01vgU2rC1FoqTqJ62739TFtI+lJKGKZrjuYv0o6EF8EfYMGwRr7+XLVgbX8McD0ednn8mgPNDuWl34ofRH+AOEiceenVmzJesEwJjR+SZIez2VEEVQRpzRXw0UyK4zGiWxq29c+VpbqeH/tVGa7LqsKynAZbWb+1DV1Szoq5bHhMHw=");

		signsByOld.add(
				"fw1XZCFNLww0hITwKgitsOMp/ycU764NuwP/eeH1jEeJ/N2nQ+7zNQ1Sth23uCtQDdqd55MN5x2Q+MsmsYh+dH186gO6+1ZU7zOMqTq59XIHcK8+bnTv2AnFjsO3dNoFTJJXQuHq1UgH3g5pSu6/RwZOgXiqupEnW7bdaRwylynEJzVsFDQ4w4KLPt3ExRYsVdmT7A74aiWXLfHySnJK0mT5LKyLNNb5SZDndHIkjLIsSoYiAYnp+ek7KkQAD4GFwrvOk1szIg03tuSAnOBglnA2qV97tne60PmqpGsUbUp+wOU1IiP6mXedl0gKhxx7Uhkly60k1j3oOtUi8/gH3rQY0kkN8U7IL8/rJGxWYHwDLOASzOy/dThUWY1bmXqVqkYVAev7Z6MbbRuDX+OLjHPMxcKXT/i/eTvZBExgLI7PfpKWN31RC1LfYqLanZMrZjHfkqPoxos+HoP9ChzI8iG3HJvQxAS7ypKsbu10Dl5b9aEDPkJ3UtAQhQ/sSiv7UBPIKEl1UFD2KSLjNjN9N0nnM0/LvKNkTeVl0nLFrpNOSzObhuJ+epOTxTFziEvkNlAyJhrBCYt1OIU5F9YIy26O/OO6lJpZoCufhNMTfFJM8XNYLMCxYM+wiCrL25FT2AcJrg9XBYOA7y1UgFOcHjFlJ14N0codRY2Vqy5rv6c=");

		signsByOld.add(
				"VCHwsFGCE0v6XtLOAi2xuLlw4aExuFb9H8HMKwiwqsYXT78fT3kHchRn1grH+c8eilDyKeBjU96eDoBjYk8WsZrU3ldq+fIMf/YbA4o2St4+dsRpO6CbPwpTef/VjZMTulEyIrOOGfiPR9g+I5gALdOuCyMHnwvriTSMHijfMU/ji99VRJdxxRX7dC1Kd+OaebeimSiCEYpQg4LG+bwXiluh3RK6V1pCsG4BkUOv+u0paekKHm4kih0T5Ij6Z7zLY9PksEFZrfpczcYSW1EVkBC/3pUCFANVyw2AO+1FrGTagZ6COtI5grx7KjVWGWaBSkNh8kRH9GBvHQUGSPfuort06Nw/+sMhDTFzAObqAaIdVPR7ShtzTALMBQ3EycS/kQU4udei1y7veOOhkuVSYfPvXA9Bf0OR1p+U+0HvC11Ci1ZanFaNgHM8b7gI8+Fi2zP5eYTRoxM6G6suro7ryrrIV21wDXNBYRM16eM3RWy3M6d/s/oLs2qhluMiS+Giwgr0GqLlg9JYLnQybv9AMQ03t5dW1eIMH400QA71yf6/45NKKn2fd8nlEMpFHGcJzISqudkQJj9uQTw84WGi6Yb68UDSxJCuAkHAhqe0Q83w2Fh9S85NDtls9Hj6yxHItz/n2ADkZXYJ7Zu5VBz2LrYXro9g2wAVdfLR2GGEWEo=");

		try {
			Decoder d = Base64.getDecoder();
			Util u = Glb.getUtil();
			User author = getAuthor();
			PublicKey authorOffPubOld = u
					.getPub(d.decode(authorPublicKeys.get(2)));
			PublicKey authorOffPubNew = u.getPub(author.getOfflinePublicKey());

			for (int i = 0; i < authorPublicKeys.size(); i++) {
				byte[] pub = d.decode(authorPublicKeys.get(i));

				String signByOldStr = signsByOld.get(i);
				if (signByOldStr.length() == 0)
					continue;
				byte[] signByOld = d.decode(signByOldStr);

				boolean b1 = u.verify(Conf.getSignKeyNominal(), signByOld,
						authorOffPubOld, pub);

				String signByNewStr = signsByNew.get(i);
				if (signByNewStr.length() == 0)
					continue;
				byte[] signByNew = d.decode(signByNewStr);
				boolean b2 = u.verify(Conf.getSignKeyNominal(), signByNew,
						authorOffPubNew, pub);

				if (!b1 || !b2) {
					Glb.debug("検証失敗");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			Glb.debug(e);
		}
		return false;
	}

}
