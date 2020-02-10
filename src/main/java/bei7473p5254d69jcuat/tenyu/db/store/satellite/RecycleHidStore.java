package bei7473p5254d69jcuat.tenyu.db.store.satellite;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.DBUtil.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 削除された{@link HashStore}のHIDを記録し、
 * 新しくHIDを割り振る時にリサイクルする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RecycleHidStore extends ObjectStore<Long, Byte>
		implements Satellite {
	private static final String name = RecycleHidStore.class.getSimpleName();
	private final String storeName;
	/**
	 * Valueは使っていないので無意味な値を設定している
	 */
	private final byte meaningless = 0;

	public RecycleHidStore(String storeName, Transaction txn) {
		super(txn);
		this.storeName = storeName;
	}

	public RecycleHidStore(StoreNameObjectivity storeName, Transaction txn) {
		this(storeName.getModelName(), txn);
	}

	@Override
	protected ByteIterable cnvKey(Long key) {
		return cnvL(key);
	}

	@Override
	protected Long cnvKey(ByteIterable bi) {
		return cnvL(bi);
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		return new StoreInfo(name + "_" + storeName + "_keyToMeaningless");
	}

	public boolean create(Long id) throws IOException {
		if (storeName == null)
			return false;
		if (!HashStore.validateHid(id))
			return false;

		return putDirect(cnvL(id), cnvB(meaningless));
	}

	public List<Long> dbExtra(HashSet<Long> filter) {
		return util.dif(getMainStoreInfo(), new HashSet<Long>(filter),
				(bi) -> cnvL(bi));
	}

	/**
	 * @return	全リサイクルID一覧。DBのキー順序のまま返される
	 * LongキーなのでDBのキー順序はLongの大小順になる
	 */
	public List<Long> getAllIds() {
		Map<Long,
				Byte> r = util.getRangePreserveOrder(getMainStoreInfo(),
						cnvL(IdObjectDBI.getFirstId()),
						(arg) -> cnvL(arg), (arg) -> null, true, 0, -1);
		if (r == null || r.size() == 0)
			return new ArrayList<>();
		return new ArrayList<>(r.keySet());

		/*
		List<Long> allIds = new ArrayList<>();
		for (int i = 0;; i++) {
			List<IDList> ll = getIDList(i);
			if (ll == null)
				break;
			for (IDList l : ll) {
				for (long id : l.uncompress()) {
					allIds.add(id);
				}
			}
		}
		return allIds;
		*/
	}

	/**
	 * @return　リサイクル可能IDから選択された最も小さいID
	 */
	public Long getFirst() {
		KVSRecord<Long,
				Byte> r = util.getRangeSingle(getMainStoreInfo(),
						cnvL(IdObjectDBI.getFirstId()),
						(arg) -> cnvL(arg), (arg) -> null);
		if (r == null)
			return null;

		return r.getKey();
	}

	/**
	 * 失敗しても処理を継続する
	 * @param recycleHidList
	 * @return	1件でも失敗したか
	 * @throws IOException
	 */
	public boolean delete(List<Long> recycleHidList) throws IOException {
		boolean r = true;
		for (Long id : recycleHidList) {
			if (!delete(id)) {
				r = false;
			}
		}
		return r;
	}

	public boolean delete(Long recycleHid) throws IOException {
		return deleteDirect(cnvL(recycleHid));
	}

	/*
	public Byte get(Long key) throws IOException {
		return getMain(cnvL(key));
	}
	*/

	public boolean update(Long key, Byte val) throws IOException {
		if (!IdObject.validateIdStandardNotSpecialId(key))
			return false;
		return putDirect(cnvL(key), cnvB(val));
	}

	@Override
	public List<StoreInfo> getStoresObjectStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected Byte chainversionup(ByteIterable bi) {
		return cnvB(bi);
	}

	/**
	 * 1個のIDListの件数
	 */
	public static final int unitIDList = 1000 * 1000;

	/**
	 * @param index
	 * @return			圧縮されたリサイクルIDの一覧、または0件の場合null
	 * 1つのindexにつきunitIDList件に限られ、
	 * 全ID一覧の一部である場合がある。
	 */
	public List<IDList> getIDList(int index) {
		Map<Long, Byte> r = util.getRangePreserveOrder(getMainStoreInfo(),
				cnvL(IdObjectDBI.getFirstId()), (arg) -> cnvL(arg),
				(arg) -> null, true, index * unitIDList, unitIDList);

		if (r == null || r.size() == 0)
			return null;

		List<Long> ids = new ArrayList<>();
		ids.addAll(r.keySet());
		Collections.sort(ids);
		return IDList.compress(ids);
	}

}
