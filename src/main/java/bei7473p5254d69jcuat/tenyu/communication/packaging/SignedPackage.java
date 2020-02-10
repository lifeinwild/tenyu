package bei7473p5254d69jcuat.tenyu.communication.packaging;

import java.io.*;
import java.security.*;
import java.security.spec.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;

/**
 * 電子署名つき梱包
 * 暗号化無し
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class SignedPackage extends Package {
	public static SignedPackage getPack(Message m) {
		if (m == null || m.getInnermostPack() == null)
			return null;
		if (!(m.getInnermostPack() instanceof SignedPackage))
			return null;
		return (SignedPackage) m.getInnermostPack();
	}

	public static Long getSigner(Message m) {
		SignedPackage p = getPack(m);
		if (p == null)
			return null;
		return p.getSignerUserId();
	}

	/**
	 * 署名に使われた鍵のタイプ
	 */
	private KeyType keyType = Glb.getConf().getMyStandardKeyType();

	public KeyType getKeyType() {
		return keyType;
	}

	/**
	 * 作成者による内容の署名
	 */
	private byte[] signature = null;

	/**
	 * 署名者
	 *
	 * 現状ユーザーによる署名しか想定しない。
	 * 理屈上、ユーザー登録前でも署名梱包を使う可能性はある。
	 * そうなったら抽象クラスに共通メソッドをくくりだして共通鍵梱包同様の方法で
	 * 実装できるだろう。
	 */
	private Long signerUserId = Glb.getMiddle().getMyUserId();

	@Override
	protected boolean binarizeAndSetContentConcrete(Communicatable content,
			Message m) {
		Communicatable backup = deserialized;
		try {
			deserialized = content;

			//シリアライズ
			contentBinary = Glb.getUtil().toKryoBytesForCommunication(content);
			if (contentBinary == null)
				throw new Exception("failed to serialize by kryo");

			//署名
			if (!sign())
				throw new Exception("failed to sign");
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			deserialized = backup;
			return false;
		}

		return true;
	}

	@Override
	protected Communicatable deserializeConcrete(Message m) {
		try {
			Object o = Glb.getUtil()
					.fromKryoBytesForCommunication(contentBinary);
			if (o != null && o instanceof Communicatable) {
				Communicatable r = (Communicatable) o;
				return r;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	public byte[] getSignature() {
		return signature;
	}

	public Long getSignerUserId() {
		return signerUserId;
	}

	/**
	 * 内容がセットされてから呼ぶ
	 * @return	署名の名目
	 */
	public String getSignNominal() {
		return Glb.getConst().getAppName() + "_"
				+ Glb.getObje().getCore().getHistoryIndex() + "_"
				+ signerUserId;
	}

	/**
	 * このメッセージの署名、公開鍵、署名対象データは整合性があるか
	 */
	private boolean isValidSignature(Message m) {
		try {
			User signer = Glb.getObje().getUser(us->us.get(signerUserId));
			if (signer == null || keyType == null)
				return false;

			//作成者の公開鍵を取得
			byte[] pubB = signer.getPubKey(keyType);
			if (pubB == null)
				return false;

			return Glb.getUtil().verify(getSignNominal(), getSignature(), pubB,
					getContentBinary());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	@Override
	protected boolean isValidType(Object content) {
		return content instanceof SignedPackageContent;
	}

	/**
	 * @return	作成者userIdは存在し、BANされてないか
	 */
	private boolean isValidUserId(Message m) {
		if (signerUserId == null) {
			Glb.debug("signerUserId is null");
			return false;
		}
		User signer = Glb.getObje().getUser(us->us.get(signerUserId));
		if (signer == null || signer.getId() == null)
			return false;

		Sociality s = SocialityStore.getByUserIdSimple(signer.getId());

		if (s == null || s.isBanned())
			return false;

		return true;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	/**
	 * 内容を署名
	 */
	private boolean sign() {
		try {
			signature = Glb.getConf().sign(getSignNominal(), keyType,
					getContentBinary());
			if (signature == null)
				return false;
		} catch (InvalidKeySpecException | NoSuchAlgorithmException
				| IOException e) {
			//signに失敗したらvalidateに失敗するので送受信されない。
			Glb.getLogger().error("", e);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean validatePackageConcrete(Message m) {
		if (!isValidUserId(m))
			return false;

		if (!isValidSignature(m))
			return false;

		return true;
	}

	public static interface SignedPackageContent {
		default Package createPackage() {
			//署名に秘密鍵が必要かつ秘密鍵は秘密にされるので、署名者は常に自分。
			//そして梱包をデシリアライズではなく新規作成を通じて取得するのは署名者だけ。
			//だからsignerを指定する必要は無いように思われる。
			//鍵タイプは現状Standard設定されているものを使うが、指定できるようにする可能性は
			//少しある
			return new SignedPackage();
		}
	}
}
