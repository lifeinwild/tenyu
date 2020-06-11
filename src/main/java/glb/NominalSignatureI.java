package glb;

import java.nio.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.user.*;
import jetbrains.exodus.env.*;

/**
 * 任意の名目で使用される署名クラス。
 * 名目と日時情報は文脈によらず必須とみなした。
 *
 * 署名情報はメンバー変数に各情報を持つかgetter呼び出し時に作成するか
 * 場合によって異なるので、具象クラスによってその多様性を表現する。
 *
 * set系インターフェースは具象クラスが必要としない場合、空実装で問題ない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface NominalSignatureI extends ValidatableI {
	default void copy(NominalSignatureI from) {
		setKeyType(from.getKeyType());
		setNominal(from.getNominal());
		setSign(from.getSign());
		setSignDate(from.getSignDate());
		setSignerUserId(from.getSignerUserId());
	}

	default void copy(NominalSignatureI from, Transaction txn) {
		setKeyType(from.getKeyType(txn), txn);
		setNominal(from.getNominal(txn), txn);
		setSign(from.getSign(txn), txn);
		setSignDate(from.getSignDate(txn), txn);
		setSignerUserId(from.getSignerUserId(txn), txn);
	}

	/**
	 * @param date	署名日時
	 * @param signTargetOrig	利用側が入力した署名対象。ここに日時データが付加される
	 * @return		署名対象
	 */
	default byte[] createSignTarget(long date, byte[] signTargetOrig) {
		ByteBuffer buffer = ByteBuffer
				.allocate(Long.BYTES + signTargetOrig.length);
		buffer.put(signTargetOrig);
		buffer.putLong(date);
		return Glb.getUtil().hashSecure(buffer.array());
	}

	default byte[] createSignTarget(long date, byte[] signTargetOrig,
			Transaction txn) {
		return createSignTarget(date, signTargetOrig);
	}

	/**
	 * @return	署名者が署名に用いた{@link KeyType}の現在用いている公開鍵。
	 * ただしこれが署名に用いた鍵かは分からない。
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	default PublicKey getCurrentSignerPublicKey()
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		return Glb.getUtil()
				.getPub(Glb.getObje().getUser(us -> us.get(getSignerUserId()))
						.getPubKey(getKeyType()));
	}

	default PublicKey getCurrentSignerPublicKey(Transaction txn)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		return Glb.getUtil().getPub(new UserStore(txn).get(getSignerUserId(txn))
				.getPubKey(getKeyType(txn)));
	}

	KeyType getKeyType();

	default KeyType getKeyType(Transaction txn) {
		return getKeyType();
	}

	String getNominal();

	default String getNominal(Transaction txn) {
		return getNominal();
	}

	/**
	 * @return	署名者の過去の公開鍵
	 */
	default KeysLog getPubOldKeys() {
		try {
			return Glb.getObje().getUser(us -> us.get(getSignerUserId()))
					.getKeysLog();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	default KeysLog getPubOldKeys(Transaction txn) {
		try {
			return new UserStore(txn).get(getSignerUserId(txn)).getKeysLog();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	byte[] getSign();

	default byte[] getSign(Transaction txn) {
		return getSign();
	}

	long getSignDate();

	default long getSignDate(Transaction txn) {
		return getSignDate();
	}

	Long getSignerUserId();

	default Long getSignerUserId(Transaction txn) {
		return getSignerUserId();
	}

	/**
	 * @return	名目に依存して居ない署名対象バイナリ
	 */
	byte[] getSignTargetOrig();

	default byte[] getSignTargetOrig(Transaction txn) {
		return getSignTargetOrig();
	}

	/**
	 * 署名を検証する場合の標準的メソッド
	 *
	 * @return	署名者の署名に用いた種類の全公開鍵（過去の公開鍵を含む）の中に
	 * 署名を正しく検証できるものが１つでもあればtrue
	 */
	default boolean searchAndVerify() {
		return searchAndVerifyPub() != null;
	}

	default boolean searchAndVerify(Transaction txn) {
		return searchAndVerifyPub(txn) != null;
	}

	/**
	 * @return	署名者が署名に用いた公開鍵。
	 * 検証に成功する鍵が見つからないならnull
	 */
	default PublicKey searchAndVerifyPub() {
		try {
			PublicKey first = searchAndVerifyWithCurrentKey();
			if (first != null)
				return first;

			return searchAndVerifyWithOldKeys();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	default PublicKey searchAndVerifyPub(Transaction txn) {
		try {
			PublicKey first = searchAndVerifyWithCurrentKey(txn);
			if (first != null)
				return first;

			return searchAndVerifyWithOldKeys(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * @return	署名者が署名に用いた種類の現在用いている公開鍵。
	 * ただし検証に失敗する場合null
	 */
	default PublicKey searchAndVerifyWithCurrentKey() {
		try {
			PublicKey first = getCurrentSignerPublicKey();
			//検証を通過すればこれを返す
			if (simpleVerify(first)) {
				return first;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	default PublicKey searchAndVerifyWithCurrentKey(Transaction txn) {
		try {
			PublicKey first = getCurrentSignerPublicKey(txn);
			//検証を通過すればこれを返す
			if (simpleVerify(first, txn)) {
				return first;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * @return	署名者の署名に用いた種類の全公開鍵（過去の公開鍵を含む）の中に
	 * 署名を正しく検証できるものがあればそれを返す
	 */
	default PublicKey searchAndVerifyWithOldKeys() {
		try {
			//鍵の更新を想定し、古い鍵からも探す。
			KeysLog log = getPubOldKeys();
			List<PublicKey> candidates = log.getOldKeysPublicKey(getKeyType());
			for (PublicKey e : candidates) {
				if (simpleVerify(e)) {
					return e;
				}
			}
			return null;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	default PublicKey searchAndVerifyWithOldKeys(Transaction txn) {
		try {
			//鍵の更新を想定し、古い鍵からも探す。
			KeysLog log = getPubOldKeys(txn);
			List<PublicKey> candidates = log
					.getOldKeysPublicKey(getKeyType(txn));
			for (PublicKey e : candidates) {
				if (simpleVerify(e, txn)) {
					return e;
				}
			}
			return null;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	void setKeyType(KeyType type);

	default void setKeyType(KeyType type, Transaction txn) {
		setKeyType(type);
	}

	void setNominal(String nominal);

	default void setNominal(String nominal, Transaction txn) {
		setNominal(nominal);
	}

	void setSign(byte[] sign);

	default void setSign(byte[] sign, Transaction txn) {
		setSign(sign);
	}

	void setSignDate(long date);

	default void setSignDate(long date, Transaction txn) {
		setSignDate(date);
	}

	void setSignerUserId(Long signerUserId);

	default void setSignerUserId(Long signerUserId, Transaction txn) {
		setSignerUserId(signerUserId);
	}

	default boolean sign() {
		return sign(getNominal(), getSignTargetOrig());
	}

	default boolean sign(byte[] signTargetOrig) {
		return sign(getNominal(), signTargetOrig);
	}

	default boolean sign(byte[] signTargetOrig, Transaction txn) {
		return sign(getNominal(txn), signTargetOrig, txn);
	}

	/**
	 * @param nominal	署名名目
	 * @param signTargetOrig	署名対象データの元となるデータ。これに日時が加えられる。
	 * @return	署名されたか
	 */
	default boolean sign(String nominal, byte[] signTargetOrig) {
		try {
			//日時情報を必ずつける事にした
			long date = System.currentTimeMillis();
			byte[] signTarget = createSignTarget(date, signTargetOrig);

			//署名
			//署名は署名者本人しかしないのでConfに依存できる。
			byte[] sign = Glb.getConf().getKeys().sign(nominal, getKeyType(),
					signTarget);
			if (sign == null)
				return false;

			//署名に成功した場合その署名に関連した情報をこのオブジェクトにセットする。
			setSign(sign);
			setSignerUserId(Glb.getMiddle().getMyUserId());
			setNominal(nominal);
			setSignDate(date);

			/*
			Glb.debug("sign signerUserId=" + getSignerUserId() + " Nominal="
					+ getNominal() + " TargetOrig="
					+ Arrays.toString(signTargetOrig) + " Target="
					+ Arrays.toString(signTarget) + " Sign="
					+ Arrays.toString(sign) + " pub="
					+ Arrays.toString(Glb.getConf().getKeys()
							.getMyPublicKey(getKeyType()).getEncoded()));
							*/

			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	default boolean sign(String nominal, byte[] signTargetOrig,
			Transaction txn) {
		try {
			//日時情報を必ずつける事にした
			long date = System.currentTimeMillis();
			byte[] signTarget = createSignTarget(date, signTargetOrig, txn);

			//署名
			//署名は署名者本人しかしないのでConfに依存できる。
			byte[] sign = Glb.getConf().getKeys().sign(nominal, getKeyType(txn),
					signTarget);
			if (sign == null)
				return false;

			//署名に成功した場合その署名に関連した情報をこのオブジェクトにセットする。
			setSign(sign, txn);
			setSignerUserId(Glb.getMiddle().getMyUserId(txn), txn);
			setNominal(nominal, txn);
			setSignDate(date, txn);

			/*
			Glb.debug("sign signerUserId=" + getSignerUserId() + " Nominal="
					+ getNominal() + " TargetOrig="
					+ Arrays.toString(signTargetOrig) + " Target="
					+ Arrays.toString(signTarget) + " Sign="
					+ Arrays.toString(sign) + " pub="
					+ Arrays.toString(Glb.getConf().getKeys()
							.getMyPublicKey(getKeyType(txn)).getEncoded()));
							*/

			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	default boolean sign(Transaction txn) {
		return sign(getNominal(txn), getSignTargetOrig(txn), txn);
	}

	/**
	 * 指定した公開鍵で検証する
	 *
	 * @param pub
	 * @return	この公開鍵で検証に成功するか
	 */
	default boolean simpleVerify(PublicKey pub) {
		return simpleVerify(pub, getSignTargetOrig());
	}

	default boolean simpleVerify(PublicKey pub, byte[] signTargetOrig) {
		byte[] signTarget = createSignTarget(getSignDate(), signTargetOrig);
		/*
		Glb.debug("verify signerUserId=" + getSignerUserId() + " Nominal="
				+ getNominal() + " signTargetOrig="
				+ Arrays.toString(signTargetOrig) + System.lineSeparator()
				+ " signTarget=" + Arrays.toString(signTarget) + " Sign="
				+ Arrays.toString(getSign()) + " pub="
				+ Arrays.toString(pub.getEncoded()));
				*/

		return Glb.getUtil().verify(getNominal(), getSign(), pub, signTarget);
	}

	default boolean simpleVerify(PublicKey pub, byte[] signTargetOrig,
			Transaction txn) {
		byte[] signTarget = createSignTarget(getSignDate(txn), signTargetOrig,
				txn);
		/*
		Glb.debug("verify signerUserId=" + getSignerUserId() + " Nominal="
				+ getNominal() + " signTargetOrig="
				+ Arrays.toString(signTargetOrig) + System.lineSeparator()
				+ " signTarget=" + Arrays.toString(signTarget) + " Sign="
				+ Arrays.toString(getSign()) + " pub="
				+ Arrays.toString(pub.getEncoded()));
		*/
		return Glb.getUtil().verify(getNominal(txn), getSign(txn), pub,
				signTarget);
	}

	default boolean simpleVerify(PublicKey pub, Transaction txn) {
		return simpleVerify(pub, getSignTargetOrig(txn), txn);
	}

	/**
	 * @return	現在の鍵で更新に成功するか
	 */
	default boolean verifyWithCurrentKeys() {
		return searchAndVerifyWithCurrentKey() != null;
	}

	default boolean verifyWithCurrentKeys(Transaction txn) {
		return searchAndVerifyWithCurrentKey(txn) != null;
	}

	/**
	 * @return	古い鍵で検証に成功するか
	 */
	default boolean verifyWithOldKeys() {
		return searchAndVerifyWithOldKeys() != null;
	}

	default boolean verifyWithOldKeys(Transaction txn) {
		return searchAndVerifyWithOldKeys(txn) != null;
	}

}
