package bei7473p5254d69jcuat.tenyu.communication.packaging;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import glb.*;
import glb.util.*;

/**
 * ノードA、Bがあり通信するとして、共通鍵が2つあり、
 * A→BとB→Aで異なる共通鍵を使用する。
 * A→Bは、ノードAで前者の共通鍵で暗号化、ノードBで前者の共通鍵で複合化する
 * B→Aは、ノードBで後者の共通鍵で暗号化、ノードAで後者の共通鍵で復号化する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class CommonKeyPackage extends Package {
	protected abstract CommonKeyInfo getCki(Message m);

	/**
	 * 今回のIV。毎回違う。知られても問題無い。
	 */
	private byte[] iv = generateIV();

	private static byte[] generateIV() {
		byte[] iv = new byte[Glb.getConst().getCommonKeyIvSize()];
		Glb.getRnd().nextBytes(iv);
		return iv;
	}

	@Override
	protected final boolean validatePackageConcrete(Message m) {
		return iv != null && iv.length > 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CommonKeyPackage))
			return false;
		CommonKeyPackage p = (CommonKeyPackage) obj;
		if (!Arrays.equals(iv, p.getIv()))
			return false;

		return super.equals(obj);
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public CommonKeyPackage() {
		Glb.getRnd().nextBytes(iv);
	}

	@Override
	protected Communicatable deserializeConcrete(Message m) {
		try {
			byte[] decrypted = null;
			decrypted = getCki(m).decrypt(contentBinary, iv);
			if (decrypted == null)
				return null;

			Object o = Glb.getUtil().fromKryoBytesForCommunication(decrypted);
			if (o != null && o instanceof Communicatable)
				return (Communicatable) o;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	@Override
	protected boolean binarizeAndSetContentConcrete(Communicatable content,
			Message m) {
		try {
			byte[] s = Glb.getUtil().toKryoBytesForCommunication(content);
			if (s == null || s.length == 0)
				return false;
			contentBinary = getCki(m).encrypt(s, iv);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error(content.getClass().getSimpleName(), e);
		}
		return false;
	}

}
