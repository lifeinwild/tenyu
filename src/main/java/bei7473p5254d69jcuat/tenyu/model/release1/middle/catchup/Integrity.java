package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.HashStore.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import glb.util.Util.*;

/**
 * 整合性情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class Integrity {
	/**
	 * ストア名：ストア毎の整合性情報
	 * 順序を保証するためLinked
	 */
	private Map<StoreNameObjectivity, IntegrityByStore> byStore = new LinkedHashMap<>();

	/**
	 * {@link ObjectivityCore}のハッシュ値
	 */
	private byte[] coreHash;

	/**
	 * 最新のヒストリーインデックス
	 */
	private long historyIndex = -1;

	public Map<StoreNameObjectivity, IntegrityByStore> getByStore() {
		return byStore;
	}

	public byte[] getCoreHash() {
		return coreHash;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public ByteArrayWrapper getSerialized() {
		return new ByteArrayWrapper(getSerializedByteArray());
	}

	private byte[] getSerializedByteArray() {
		try {
			return Glb.getUtil().toKryoBytesForCommunication(this);
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public void setByStore(Map<StoreNameObjectivity, IntegrityByStore> byStore) {
		this.byStore = byStore;
	}

	public void setCoreHash(byte[] coreHash) {
		this.coreHash = coreHash;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public boolean validate() {
		if (byStore == null)
			return false;
		for (IntegrityByStore s : byStore.values()) {
			if (!s.validate())
				return false;
		}

		return coreHash != null && coreHash.length > 0 && historyIndex != -1;
	}

	/**
	 * ストア毎の整合性情報
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class IntegrityByStore {
		/**
		 * ハッシュツリー上の最後のHid
		 * 実データとしては削除されている可能性がある
		 */
		private Long lastHidOfHashStore = null;
		/**
		 * リサイクルHidの件数
		 */
		private long recycleHidCount = 0;

		/**
		 * リサイクルHidのリストのハッシュ値
		 */
		private List<ByteArrayWrapper> recycleHidListHash = new ArrayList<>();

		/**
		 * 最上位ハッシュ配列
		 */
		private HashStoreRecordPositioned top = null;

		public Long getLastHidOfHashStore() {
			return lastHidOfHashStore;
		}

		public long getRecycleHidCount() {
			return recycleHidCount;
		}

		public List<ByteArrayWrapper> getRecycleHidListHash() {
			return recycleHidListHash;
		}

		public HashStoreRecordPositioned getTop() {
			return top;
		}

		public void setLastHidOfHashStore(Long lastHidOfHashStore) {
			this.lastHidOfHashStore = lastHidOfHashStore;
		}

		public void setRecycleHidCount(long recycleHidCount) {
			this.recycleHidCount = recycleHidCount;
		}

		public void setRecycleHidListHash(List<ByteArrayWrapper> recycleHidListHash) {
			this.recycleHidListHash = recycleHidListHash;
		}

		public void setTop(HashStoreRecordPositioned top) {
			this.top = top;
		}

		public boolean validate() {
			if (top != null && top.isTop() == false)
				return false;
			if (recycleHidListHash == null)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "IntegrityByStore [lastHidOfHashStore=" + lastHidOfHashStore
					+ ", recycleHidCount=" + recycleHidCount
					+ ", recycleHidListHash=" + recycleHidListHash + ", top="
					+ top + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lastHidOfHashStore == null) ? 0
					: lastHidOfHashStore.hashCode());
			result = prime * result
					+ (int) (recycleHidCount ^ (recycleHidCount >>> 32));
			result = prime * result + ((recycleHidListHash == null) ? 0
					: recycleHidListHash.hashCode());
			result = prime * result + ((top == null) ? 0 : top.hashCode());
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
			IntegrityByStore other = (IntegrityByStore) obj;
			if (lastHidOfHashStore == null) {
				if (other.lastHidOfHashStore != null)
					return false;
			} else if (!lastHidOfHashStore.equals(other.lastHidOfHashStore))
				return false;
			if (recycleHidCount != other.recycleHidCount)
				return false;
			if (recycleHidListHash == null) {
				if (other.recycleHidListHash != null)
					return false;
			} else if (!recycleHidListHash.equals(other.recycleHidListHash))
				return false;
			if (top == null) {
				if (other.top != null)
					return false;
			} else if (!top.equals(other.top))
				return false;
			return true;
		}

	}

	@Override
	public String toString() {
		return "Integrity [byStore=" + byStore + ", coreHash="
				+ Arrays.toString(coreHash) + ", historyIndex=" + historyIndex
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((byStore == null) ? 0 : byStore.hashCode());
		result = prime * result + Arrays.hashCode(coreHash);
		result = prime * result + (int) (historyIndex ^ (historyIndex >>> 32));
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
		Integrity other = (Integrity) obj;
		if (byStore == null) {
			if (other.byStore != null)
				return false;
		} else if (!byStore.equals(other.byStore))
			return false;
		if (!Arrays.equals(coreHash, other.coreHash))
			return false;
		if (historyIndex != other.historyIndex)
			return false;
		return true;
	}

}