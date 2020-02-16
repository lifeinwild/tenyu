package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.IdObjectStore.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 1クラス1ObjectStore
 * 1オブジェクトをシリアライズして1つの値として格納する。
 * その規約によってchainversionupが可能になる。
 *
 * クラスの一部のメンバーはサブインデックスになる。
 * サブインデックス用ストアも作られるので、
 * 1ObjectStoreが多数のストアを管理する場合がある。
 * その場合でもメインストアという概念があり、対象クラスは1つに限定される。
 * 1メインストア多サブインデックスストア。1クラスとインデックス用のいくつかのフィールド。
 *
 * 1ObjectStoreにつき1個以上のKVSのストアがある。
 * サブインデックスとは、例えばUserはnameでも検索できるが、nameはサブインデックスである。
 * 一方でメインのインデックスはlongの連番である。
 *
 * サブインデックスはNotNull
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <K>	キー		取得系の引数
 * @param <V>	バリュー	取得系の返値の型
 */
public abstract class ObjectStore<K, V> {
	protected static <R> R simpleAccess(String dbPath,
			StoreFunction<Transaction, R> f) {
		return Glb.getDb(dbPath).computeInTransaction((txn) -> {
			try {
				return f.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	protected static <R> R simpleReadAccess(String dbPath,
			StoreFunction<Transaction, R> f) {
		return Glb.getDb(dbPath).computeInReadonlyTransaction((txn) -> {
			try {
				return f.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	protected DBUtil util;

	public ObjectStore(Transaction txn) {
		this.util = new DBUtil(txn);
	}

	public Object getObj(StoreInfo s, ByteIterable key) {
		try {
			ByteIterable bi = util.get(s, key);
			if (bi == null)
				return null;
			return cnvO(bi);
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 読み取られたシリアライズされたデータをデシリアライズしてオブジェクトにして、
	 * 次々とバージョンアップして最新化して返す。
	 *
	 * 古いバージョンのオブジェクトが読み込まれた時、永続化されるクラスは必ず
	 * 前のバージョンからのバージョンアップメソッドを実装するので、
	 * 次のバージョンがある限りバージョンアップする。
	 * 古いバージョンのオブジェクトは、読み取られただけなら、古いまま残る。
	 * 更新された場合、最新版になる。
	 * この挙動は全ノードで同じである。
	 *
	 * このメソッドは実質的にcnvVal()と名付けても良いようなもの。
	 *
	 * @param bi	読み出されたデータ
	 * @return	バージョンアップされたオブジェクト。その時の最新バージョンのクラス。
	 */
	protected abstract V chainversionup(ByteIterable bi);

	abstract protected K cnvKey(ByteIterable bi);

	protected final V cnvVal(ByteIterable bi) {
		return chainversionup(bi);
	}

	protected ByteIterable cnvVal(V val) throws Exception {
		return cnvO(val);
	}

	abstract protected ByteIterable cnvKey(K key);

	/**
	 * DB上のbyte[]を返す。
	 *
	 * @param key
	 * @return
	 */
	public final byte[] getRawVal(K key) {
		return cnvBA(util.get(getMainStoreInfo(), cnvKey(key)));
	}

	/**
	 * DB上のbyte[]をデシリアライズして返す。
	 *
	 * 通常DBからのgetはchainversionupしなければならないが
	 * このメソッドはしてはいけない。同調処理で使われる。
	 * 同調処理はDBの実状態を同調させるのであってversionupさせると
	 * ハッシュ値が一致しないので。
	 *
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public Object getRawObj(K key) {
		try {
			return cnvO(util.get(getMainStoreInfo(), cnvKey(key)));
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * @return メインストアのレコード数
	 */
	public long count() {
		return util.count(getMainStoreInfo());
	}

	/**
	 * 検証処理等をせず単にKVSから指定されたキーを削除する。
	 * 重複キー型のストアでは同じキーの全ての値が削除される。
	 * ObjectStoreのK,Vに当たるストア、メインストアでは今のところ重複キー型は無い。
	 *
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public boolean deleteDirect(ByteIterable key) throws IOException {
		return util.remove(getMainStoreInfo(), key);
	}

	public Map<K, V> getAll() {
		return util.getAll(getMainStoreInfo(), bi -> cnvKey(bi),
				bi -> cnvVal(bi));
	}

	public List<V> getAllValues() {
		return util.getAllValues(getMainStoreInfo(), bi -> cnvVal(bi));
	}

	public DBUtil getDbUtil() {
		return util;
	}

	/**
	 * 単品取得
	 * @param key
	 * @return
	 */
	public final V get(K key) {
		if (key == null)
			return null;
		return getMain(cnvKey(key));
	}

	/**
	 * まとめて取得する
	 * @param keys
	 * @return
	 */
	public final List<V> get(List<K> keys) {
		if (keys == null)
			return null;
		List<V> r = new ArrayList<>();
		for (K key : keys) {
			V v = get(key);
			if (v == null)
				continue;
			r.add(v);
		}
		return r;
	}

	private final V getMain(ByteIterable key) {
		//他のDB系インターフェースはthrows Exceptionにしているがここはtry catchを内部でやる
		//特に優劣を決定できず、こうした方がコードが減るので
		try {
			return chainversionup(util.get(getMainStoreInfo(), key));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return	K:Vのストア
	 */
	public abstract StoreInfo getMainStoreInfo();

	/**
	 * @return	ストア名
	 */
	public abstract String getName();

	/**
	 * @return	メインストア
	 */
	protected final Store getStore() {
		return util.getStore(getMainStoreInfo());
	}

	/**
	 * @return	担当する全ストア一覧
	 */
	public final List<StoreInfo> getStores() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		List<StoreInfo> tmp = getStoresObjectStoreConcrete();
		if (tmp != null)
			r.addAll(tmp);
		return r;
	}

	abstract protected List<StoreInfo> getStoresObjectStoreConcrete();

	/**
	 * 全てのストアを初期化する
	 * 書き込みトランザクションでストアを取得した後呼び出す
	 */
	public final void initStores() {
		for (StoreInfo s : getStores())
			util.getStore(s);
	}

	/**
	 * 検証処理等をせず単にメインストアに書き込む。
	 *
	 * @param key
	 * @param val
	 * @return
	 * @throws IOException
	 */
	protected boolean putDirect(ByteIterable key, ByteIterable val)
			throws IOException {
		return putDirect(key, val, false);
	}

	/**
	 * @param key
	 * @param val
	 * @param last	lastだと確定している場合true。falseでもlastの場合がある。
	 * @return
	 * @throws IOException
	 */
	protected boolean putDirect(ByteIterable key, ByteIterable val,
			boolean last) throws IOException {
		if (key == null || val == null)
			return false;

		if (last) {
			return util.putRight(getMainStoreInfo(), key, val);
		} else {
			return util.put(getMainStoreInfo(), key, val);
		}
	}

}