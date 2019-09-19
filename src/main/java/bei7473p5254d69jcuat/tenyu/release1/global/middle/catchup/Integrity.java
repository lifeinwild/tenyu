package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;

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
	private Map<String, IntegrityByStore> byStore = new LinkedHashMap<>();

	/**
	 * ObjectivityCoreハッシュ値
	 */
	private byte[] coreHash;

	/**
	 * 最新のヒストリーインデックス
	 */
	private long historyIndex = -1;

	@Override
	public String toString() {
		return "historyIndex=" + historyIndex + " coreHash="
				+ Arrays.toString(coreHash) + " byStore=" + byStore;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Integrity))
			return false;
		Integrity other = (Integrity) o;

		return getSerialized().equals(other.getSerialized());
	}

	public Map<String, IntegrityByStore> getByStore() {
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

	@Override
	public int hashCode() {
		return getSerialized().hashCode();
	}

	public void setByStore(Map<String, IntegrityByStore> byStore) {
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
		 * ハッシュツリー上の最後のID
		 * 実データとしては削除されている可能性がある
		 */
		private Long lastIdOfHashStore = null;
		/**
		 * リサイクルIDの件数
		 */
		private long recycleIdCount = 0;

		/**
		 * リサイクルIDのリストのハッシュ値
		 */
		private List<byte[]> recycleIdListHash = new ArrayList<>();

		/**
		 * 最上位ハッシュ配列
		 */
		private HashStoreRecordPositioned top = null;

		public Long getLastIdOfHashStore() {
			return lastIdOfHashStore;
		}

		public long getRecycleIdCount() {
			return recycleIdCount;
		}

		public List<byte[]> getRecycleIdListHash() {
			return recycleIdListHash;
		}

		public HashStoreRecordPositioned getTop() {
			return top;
		}

		public void setLastIdOfHashStore(Long lastIdOfHashStore) {
			this.lastIdOfHashStore = lastIdOfHashStore;
		}

		public void setRecycleIdCount(long recycleIdCount) {
			this.recycleIdCount = recycleIdCount;
		}

		public void setRecycleIdListHash(List<byte[]> recycleIdListHash) {
			this.recycleIdListHash = recycleIdListHash;
		}

		public void setTop(HashStoreRecordPositioned top) {
			this.top = top;
		}

		public boolean validate() {
			if (top != null && top.isTop() == false)
				return false;
			if (recycleIdListHash == null)
				return false;
			return true;
		}

	}

}