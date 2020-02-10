package bei7473p5254d69jcuat.tenyu.db.store.satellite;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * historyIndex : CatchUpUpdatedIDList
 *
 * MiddleのDBではなくObjectivityのDBに記録される。
 * 同調対象ではない。
 * IdObjectStore全般がサブストアとして持つ。
 *
 * トランザクションの必要性から客観DBに記録されるが整合性情報に影響しない。
 * このストアの必要性は同調処理の加速にあり、無くても性能が劣化するだけ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpUpdatedIDListStore extends
		SatelliteStore<Long, CatchUpUpdatedIDList> implements Satellite {
	/**
	 * 1ヒストリーインデックスあたりの最大件数
	 */
	public static final int max = 1000 * 1000;

	public static final String name = CatchUpUpdatedIDList.class
			.getSimpleName();
	private String storeName;

	@Override
	protected ByteIterable cnvKey(Long key) {
		return cnvL(key);
	}

	/**
	 * storeName : updatedId
	 * 各IdObjectStoreは更新されたIDをここに記録する。
	 * トランザクションの終わりにcommitUpdated()を通じてDBに書き込まれる。
	 *
	 * 多数のインスタンスが作成されてそれぞれ更新処理をしても共通の情報を作成できるように
	 * staticを使っている。IDListに圧縮するために1か所にまとめる必要がある。
	 *
	 * 値の実装クラスはHashSetを使うべき。
	 */
	private static final Map<String,
			Set<Long>> updated = new ConcurrentHashMap<>();

	@Override
	protected Long cnvKey(ByteIterable bi) {
		return cnvL(bi);
	}

	@Override
	public List<StoreInfo> getStoresObjectStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	public static void clearUpdated() {
		updated.clear();
	}

	public boolean writeUpdated(Long updatedId) {
		Set<Long> l = updated.get(storeName);
		if (l == null) {
			l = Collections.synchronizedSet(new HashSet<Long>());
			updated.put(storeName, l);
		}

		if (l.size() > max)
			return false;

		l.add(updatedId);
		return true;
	}

	/**
	 * 各ストア別に更新されたIDを書き込む。
	 * 1つのヒストリーインデックスにつき更新されたIDは最大Integer.MAX_VALUE件まで
	 * @return	全ての書き込みに成功したか。
	 */
	public static boolean commitUpdated(long historyIndex, Transaction txn) {
		boolean r = true;
		try {
			for (Entry<String, Set<Long>> e : updated.entrySet()) {
				//0件でも空リストを記録する。空であることを知っているのと、
				//そもそもどういうデータがあったのか知らないのは違うから。
				List<Long> ids = new ArrayList<>(e.getValue());
				int count = ids.size();
				//昇順ソート
				Collections.sort(ids);
				//圧縮
				List<IDList> ll = IDList.compress(ids);
				//記録形式に
				CatchUpUpdatedIDList c = new CatchUpUpdatedIDList(historyIndex,
						ll);
				//記録用ストア
				CatchUpUpdatedIDListStore s = new CatchUpUpdatedIDListStore(
						e.getKey(), txn);
				//記録
				try {
					if (!s.create((Long) historyIndex, c))
						r = false;

					Glb.getLogger().info(count + " ids stored");

				} catch (IOException e1) {
					Glb.getLogger().error("", e1);
					return false;
				}
			}
			clearUpdated();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
		return r;
	}

	/*
	public static void forEachUpdated(BiConsumer<String, Long> f) {
		updated.forEach(f);
	}
	*/

	public CatchUpUpdatedIDListStore(String storeName, Transaction txn) {
		super(txn);
		this.storeName = storeName;
	}

	public CatchUpUpdatedIDListStore(StoreNameObjectivity storeName, Transaction txn) {
		this(storeName.getModelName(), txn);
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		return new StoreInfo(name + "_" + storeName + "_idTo" + name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected CatchUpUpdatedIDList chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.CatchUpUpdatedIDList)
				return (bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.CatchUpUpdatedIDList) o;
			throw new InvalidTargetObjectTypeException(
					"not CatchUpIDList object in CatchUpIDListStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public boolean update(long historyIndex, CatchUpUpdatedIDListDBI o)
			throws IOException {
		return putDirect(cnvL(historyIndex), cnvO(o));
	}

	/**
	 * 約一週間
	 * 厳密に一週間でなくてもいい
	 * 全ノードで一致している必要もない
	 *
	 * ObjectivityCoreに類似した値があるが、あちらは時間を問題にしていて少し事情が異なる。
	 * こちらはヒストリーインデックスが更新されるたびに蓄積されるデータの容量を問題にしている。
	 * そもそもこの値を一週間程度にする必要は必ずしもない。
	 */
	public static final long historyIndexLimit = 5040;

	/**
	 * @return	削除されたヒストリーインデックス数
	 */
	private int clearOldRecord() {
		int count = 0;
		try {

			//古い記録のキーを検索
			long threshold = Glb.getObje().getCore().getHistoryIndex()
					- historyIndexLimit;
			//閾値の直前のキーから処理が始まるのでskipを1にする
			Map<Long,
					CatchUpUpdatedIDList> r = util.getRange(getMainStoreInfo(),
							cnvL(threshold), (keyBi) -> cnvL(keyBi),
							(valBi) -> chainversionup(valBi), false, 1, 1000);

			if (r == null || r.size() == 0)
				return count;

			//順次削除
			for (Long key : r.keySet()) {
				if (util.remove(getMainStoreInfo(), cnvL(key)))
					count++;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return count;
	}

	public static void clearOldRecordAllSimple() {
		Glb.getObje().execute(txn -> clearOldRecordAll(txn));
	}

	/**
	 * 各ストアの古い記録を削除する
	 */
	public static void clearOldRecordAll(Transaction txn) {
		//TODO StoreNamesObjectivity以外のストアへの対応。今のところほかのストアでは使われないが。
		for (StoreNameObjectivity storeName : StoreNameObjectivity.values()) {
			IdObjectStore<?, ?> s = storeName.getStore(txn);
			long count = s.getCatchUpUpdatedIDListStore().clearOldRecord();
			Glb.getLogger().info("clearOldRecord " + storeName
					+ " removedHistoryIndexCount=" + count);
		}
	}

}
