package bei7473p5254d69jcuat.tenyu.release1.communication;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.subjectivity.PeriodicNotification.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * 全通信クラスはこれを継承する。
 * Kryoの仕様のためno-arg constructorを実装するか独自のシリアライザを
 * 作成する必要がある。デフォルトコンストラクタ＋セッターが楽だろう。
 * P2P#kryoSetup()にクラスを追記する必要がある。
 * @author exceptiontenyu@gmail.com
 */
public abstract class Communicatable {
	protected int release = Glb.getConst().getRelease();

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

	private boolean isValidRelease() {
		return release == Glb.getConst().getRelease()
				|| this instanceof PeriodicNotification
				|| this instanceof PeriodicNotificationResponse
				|| this instanceof FileDownload;
	}

	public final boolean validate(Message m) {
		return isValidRelease() && validateConcrete(m);
	}

	protected abstract boolean validateConcrete(Message m);

}
