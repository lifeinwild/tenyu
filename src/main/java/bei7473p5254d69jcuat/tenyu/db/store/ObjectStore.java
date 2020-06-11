package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.ModelStore.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 1クラス1ObjectStore
 * 1オブジェクトをシリアライズして1つの値として格納する。
 * その規約によってchainversionupが可能になる。
 * 即ち格納されているオブジェクト毎にバージョンが異なっても良く、
 * 読み出し時にchainversionupによって最新版になる。
 * 読み出し時に確実に最新版にされるということは、
 * 更新時（＝読み出されたオブジェクトの状態が修正され再度格納された時）に
 * KVS上のオブジェクトが徐々に最新版になっていく。
 * このため、基盤ソフトウェアは全モデルクラスについて過去の全バージョンのクラス定義を持つ必要がある。
 * そのためモデルクラスはrelease1, release2などバージョニングされている。
 *
 * モデルクラス：ObjectStoreに格納されるオブジェクトのクラス
 * メインインデックス：ID→シリアライズされたオブジェクトという対応付け
 * メインストア：メインインデックスを管理するKVS上のストア
 * サブインデックス：name等→IDという対応付け
 * サブインデックスストア：サブインデックスを管理するKVS上のストア
 *
 * モデルクラスの一部のメンバー変数は検索に使用される。
 * 例えば{@link IndividualityObjectI#getName()}でKVSからオブジェクトを検索したい場合があるが、
 * そのようなID以外でオブジェクトを取得するための対応付けデータがサブインデックスであり、
 * サブインデックスを通じてname等からIDを得て、
 * さらにメインインデックスを通じてIDからシリアライズされたオブジェクトを得て、オブジェクトを取得する。
 *
 * サブインデックスとメインインデックスは
 * 対応付けられるものが違う（IDとシリアライズされたオブジェクト）事から区別される。
 *
 * ストアクラスは以下の内容を持つ。
 * - どのメンバー変数がサブインデックスを作るかの分析
 * - サブインデックスを利用してオブジェクトを取得するインターフェースの設計と実装
 * - 作成、削除、更新時のインデックス更新処理
 *
 * ここで扱えるインデックスは完全一致や前方一致等の単純なものに限られる。
 * 複雑な検索は外部のグラフDBでやるという設計。
 * 参照：{@link ModelStore}
 *
 * サブインデックス毎にKVS上のストアが１つ作られる。
 * サブインデックスのメンバー変数はNotNull
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
		return util.delete(getMainStoreInfo(), key);
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
			ByteIterable bi = util.get(getMainStoreInfo(), key);
			if (bi == null)
				return null;
			return chainversionup(bi);
		} catch (Exception e) {
			Glb.getLogger().warn("", e);
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
