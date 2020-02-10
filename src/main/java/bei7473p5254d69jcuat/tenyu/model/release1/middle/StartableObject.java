package bei7473p5254d69jcuat.tenyu.model.release1.middle;

public abstract class StartableObject {

	/**
	 * 開始したか
	 */
	protected boolean started = false;

	public boolean isStarted() {
		return started;
	}

	public synchronized boolean start() {
		boolean tmp = started;
		started = true;
		return tmp != started;
	}

	public synchronized boolean stop() {
		boolean tmp = started;
		started = false;
		return tmp != started;
	}

}
