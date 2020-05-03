package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement;

import java.nio.charset.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;

/**
 * Webページから取得したURL証明に関する情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class URLProvementInfoOnWeb {
	/**
	 * このURLの管理者の公開鍵
	 */
	private byte[] pubKey;

	/**
	 * pubKeyによるこのURLへの署名
	 */
	private byte[] sign;

	/**
	 * 対象URL
	 */
	private String url;

	/**
	 * 署名した日時。ミリ秒
	 */
	private long signDate;

	/**
	 * @return	一通りの情報があり、署名が正しいか
	 */
	public boolean validate() {
		if (pubKey == null || sign == null || url == null)
			return false;
		Charset c = Glb.getConst().getCharsetNio();
		if (!Glb.getUtil().verify(getNominal(), sign, pubKey, url.getBytes(c)))
			return false;
		return true;
	}

	/**
	 * @return	このURLの管理者ユーザー
	 */
	public User getAdminUserId() {
		return Glb.getObje().compute(txn -> {
			try {
				UserStore us = new UserStore(txn);
				Long userId = us.getIdByAny(pubKey);
				if (userId == null) {
					Glb.getLogger().warn("User not found by pubKey.",
							new Exception());
					return null;
				}
				return us.get(userId);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	public static final String delimiter = ".";

	/**
	 * @return	URL証明コード。このオブジェクトの文字列表現
	 */
	public String toCode() {
		String pubBase64 = Base64.getEncoder().encodeToString(pubKey);
		String signBase64 = Base64.getEncoder().encodeToString(sign);
		return prefix + pubBase64 + delimiter + signBase64 + delimiter + url
				+ delimiter + signDate + suffix;
	}

	public String toURLProvementCode(String code) {
		return prefix + code + suffix;
	}

	/**
	 * URL証明コードの接頭辞
	 */
	public static final String prefix = "URLProvementCodeStart ";

	/**
	 * URL証明コードの接尾辞
	 */
	public static final String suffix = " URLProvementCodeEnd";

	/**
	 * @param adminAreaText	管理者しか書き込めない領域から抽出されたURL証明コードを含む文字列
	 * @return	URL証明に関する文字列
	 */
	public static String parseCode(String adminAreaText) {
		if (adminAreaText == null)
			return null;
		int start = adminAreaText.indexOf(prefix) + prefix.length();
		if (start == -1)
			return null;
		int end = adminAreaText.indexOf(suffix);
		if (end == -1)
			return null;
		return adminAreaText.substring(start, end);
	}

	/**
	 * @return 名目。null
	 * Webへ埋め込む事を考慮し、名目という本アプリ固有のアイデアは使わない
	 */
	public static String getNominal() {
		return null;
	}

	/**
	 * Webページから取得した情報で{@link URLProvementInfoOnWeb}を作成する
	 *
	 * @param code	{@link URLProvementInfoOnWeb#toCode()}の返値
	 * @return
	 */
	public static URLProvementInfoOnWeb create(String code) {
		if (code == null || code.length() == 0)
			return null;
		try {
			//parse
			int nextStart = prefix.length();
			int nextEnd = code.indexOf(delimiter);

			String pubBase64 = code.substring(nextStart, nextEnd);
			nextStart = nextEnd + 1;
			nextEnd = code.indexOf(delimiter, nextStart);

			String signBase64 = code.substring(nextStart, nextEnd);
			nextStart = nextEnd + 1;
			nextEnd = code.indexOf(delimiter, nextStart);

			String url = code.substring(nextStart, nextEnd);
			nextStart = nextEnd + 1;
			nextEnd = code.indexOf(delimiter, nextStart);

			String signDateStr = code.substring(nextStart, nextEnd);
			nextStart = nextEnd + 1;
			nextEnd = code.indexOf(delimiter, nextStart);

			URLProvementInfoOnWeb r = new URLProvementInfoOnWeb();
			r.setPubKey(Base64.getDecoder().decode(pubBase64));
			r.setSign(Base64.getDecoder().decode(signBase64));
			r.setUrl(url);
			r.setSignDate(Long.valueOf(signDateStr));
			return r;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * Webページに設置する{@link URLProvementInfoOnWeb}を作成する
	 * @param type
	 * @param url
	 * @return
	 */
	public static URLProvementInfoOnWeb create(KeyType type, String url) {
		URLProvementInfoOnWeb r = new URLProvementInfoOnWeb();
		byte[] pubB = Glb.getConf().getKeys().getMyPublicKey(type).getEncoded();
		r.setPubKey(pubB);
		r.setUrl(url);

		try {
			Charset c = Glb.getConst().getCharsetNio();
			long date = System.currentTimeMillis();
			byte[] sign = Glb.getConf().getKeys().sign(getNominal(), type,
					url.getBytes(c));
			if (sign == null)
				throw new Exception("Failed to sign");
			r.setSign(sign);
			r.setSignDate(date);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}

		return r;
	}

	public byte[] getPubKey() {
		return pubKey;
	}

	public void setPubKey(byte[] pubKey) {
		this.pubKey = pubKey;
	}

	public byte[] getSign() {
		return sign;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSignDate() {
		return signDate;
	}

	public void setSignDate(long signDate) {
		this.signDate = signDate;
	}

}