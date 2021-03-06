package bei7473p5254d69jcuat.tenyu.communication;

import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.PeriodicNotification.*;
import glb.*;
import glb.Conf.*;

/**
 * 全通信クラスはこれを継承する。
 * Kryoの仕様のためno-arg constructorを実装するか独自のシリアライザを
 * 作成する必要がある。デフォルトコンストラクタ＋セッターが楽だろう。
 * P2P#kryoSetup()にクラスを追記する必要がある。
 * @author exceptiontenyu@gmail.com
 */
public abstract class Communicatable {
	protected int release = Glb.getConst().getRelease();
	protected RunLevel runLevel = Glb.getConf().getRunlevel();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + release;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Communicatable other = (Communicatable) obj;
		if (release != other.release)
			return false;
		return true;
	}

	public int getRelease() {
		return release;
	}

	private boolean isValidRunLevel() {
		return runLevel == Glb.getConf().getRunlevel();
	}

	private boolean isValidRelease() {
		return release == Glb.getConst().getRelease()
				|| this instanceof PeriodicNotification
				|| this instanceof PeriodicNotificationResponse
				|| this instanceof GetFile;
	}

	/**
	 * 送受信時に検証処理が行われる。
	 * @param m	メッセージの梱包における電子署名等に内容の検証処理が依存している場合があるので
	 * メッセージ全体が検証時の文脈になる。
	 * @return	thisとmで実行可能なすべての検証処理において正しい内容か
	 */
	public final boolean validate(Message m) {
		return isValidRelease() && isValidRunLevel() && validateConcrete(m);
	}

	protected abstract boolean validateConcrete(Message m);

}
