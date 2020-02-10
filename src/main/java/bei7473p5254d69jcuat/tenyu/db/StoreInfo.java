package bei7473p5254d69jcuat.tenyu.db;

import jetbrains.exodus.env.*;

public class StoreInfo {
	private final String storeName;
	private final StoreConfig type;
	/**
	 * ストア内に登録されたオブジェクト数、つまりメインストアのペア数と
	 * そのサブインデックスのペア数が一致しない場合true。
	 */
	private boolean freeSizeFromMainStore = false;

	public StoreInfo(String storeName) {
		this.storeName = storeName;
		//TODO:WITH_PREFIXINGのほうがランダムアクセス性能が高いらしいが
		//xodus1.1.0はバグがあって使えない
		//https://youtrack.jetbrains.com/issue/XD-673
		this.type = StoreConfig.WITHOUT_DUPLICATES;
	}

	public StoreInfo(String storeName, StoreConfig type) {
		this.storeName = storeName;
		this.type = type;
	}

	public StoreInfo(String storeName, StoreConfig type, boolean freeSizeFromMainStore) {
		this(storeName, type);
		this.freeSizeFromMainStore = freeSizeFromMainStore;
	}

	public StoreConfig getType() {
		return type;
	}

	public String getStoreName() {
		return storeName;
	}

	@Override
	public String toString() {
		return storeName;
	}

	public boolean isFreeSizeFromMainStore() {
		return freeSizeFromMainStore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (freeSizeFromMainStore ? 1231 : 1237);
		result = prime * result
				+ ((storeName == null) ? 0 : storeName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		StoreInfo other = (StoreInfo) obj;
		if (freeSizeFromMainStore != other.freeSizeFromMainStore)
			return false;
		if (storeName == null) {
			if (other.storeName != null)
				return false;
		} else if (!storeName.equals(other.storeName))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
