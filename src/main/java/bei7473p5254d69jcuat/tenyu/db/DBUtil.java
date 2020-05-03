package bei7473p5254d69jcuat.tenyu.db;

import java.io.*;
import java.util.*;
import java.util.function.*;

import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.bindings.*;
import jetbrains.exodus.env.*;

/**
 * TransactionやByteIterableなどxodus系クラスを扱うクラス
 * cnv系メソッドはByteIterableといくつかの基本的なクラスの相互変換をする。
 *
 * Dup系メソッドはキー重複型に対応するためのコードだが、
 * キーとバリューが指定されればペアが一意に定まる事を前提にしている。
 * キー重複型は値がリスト化しているようなイメージ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DBUtil {
	private static String singleObject = "singleObject";
	private Transaction txn;

	public DBUtil(Transaction txn) {
		this.txn = txn;
	}

	public void setTxn(Transaction txn) {
		this.txn = txn;
	}

	public Transaction getTxn() {
		return txn;
	}

	public static ByteIterable cnvL(Long id) {
		return LongBinding.longToEntry(id);
	}

	public static Long cnvL(ByteIterable bi) {
		return LongBinding.entryToLong(bi);
	}

	public static ByteIterable cnvI(Integer i) {
		return IntegerBinding.intToEntry(i);
	}

	public static Integer cnvI(ByteIterable bi) {
		return IntegerBinding.entryToInt(bi);
	}

	/**
	 * 検索等のキーのために作成
	 * ByteIterableの中身を取り出すだけでなくさらに末尾1バイトを削る必要があった
	 *
	 * @param key
	 * @return
	 */
	public static ByteIterable cnvSRemoveSuffix(String key) {
		ByteIterable bi = cnvS(key);
		int rlen = bi.getLength() - 1;//末尾1バイト削除される
		byte[] r = new byte[rlen];
		System.arraycopy(bi.getBytesUnsafe(), 0, r, 0, rlen);
		return new ArrayByteIterable(r);
	}

	public static ByteIterable cnvS(String key) {
		return StringBinding.stringToEntry(key);
	}

	public static String cnvS(ByteIterable bi) {
		return StringBinding.entryToString(bi);
	}

	public static ByteIterable cnvD(Double i) {
		return DoubleBinding.doubleToEntry(i);
	}

	public static Double cnvD(ByteIterable bi) {
		return DoubleBinding.entryToDouble(bi);
	}

	public static ByteIterable cnvB(Byte b) {
		return ByteBinding.byteToEntry(b);
	}

	public static Byte cnvB(ByteIterable bi) {
		return ByteBinding.entryToByte(bi);
	}

	public static ByteIterable cnvBo(Boolean b) {
		return BooleanBinding.booleanToEntry(b);
	}

	public static Boolean cnvBo(ByteIterable bi) {
		return BooleanBinding.entryToBoolean(bi);
	}

	/**
	 * convert ByteIterable
	 * 非プリミティブ用
	 * @param o
	 * @return
	 * @throws IOException
	 * @
	 */
	public static ByteIterable cnvO(Object o) throws IOException {
		byte[] b = Glb.getUtil().toKryoBytesForPersistence(o);
		return new ArrayByteIterable(b);
	}

	public static Object cnvO(ByteIterable bi) throws IOException {
		return Glb.getUtil().fromKryoBytesForPersistence(cnvBA(bi));
	}

	/**
	 * {@link ByteIterable#getBytesUnsafe()}はサイズが本来の内容と異なってしまう可能性があるが、
	 * {@link ArrayByteIterable#getBytesUnsafe()}はその問題が無い。
	 * このcnvBAはArrayByteIterableを使用している。
	 *
	 * 他のcnvS等ではgetBytesUnsafe()は使用不可ないしgetLength()によるサイズの調整を必要とする。
	 *
	 * convert ByteArray to ByteIterable
	 * @param b
	 * @return
	 */
	public static ByteIterable cnvBA(byte[] b) {
		return new ArrayByteIterable(b);
	}

	/**
	 * cnvBAで作成されたデータに対しては使う必要が無さそう
	 * Long,String,その他オブジェクトは使う必要がある
	 *
	 * @param bi
	 * @return		余剰部分が取り除かれたByteIterableの内容
	 */
	public static byte[] cnvBA(ByteIterable bi) {
		int len = bi.getLength();
		byte[] src = bi.getBytesUnsafe();
		if (src == null)
			return null;

		byte[] r = new byte[len];
		System.arraycopy(src, 0, r, 0, len);
		return r;
	}

	public ByteIterable getObject(StoreInfo sInfo) {
		return getStore(sInfo).get(txn, cnvS(singleObject));
	}

	/**
	 * インデックス＋シーケンシャルの検索の条件設定等
	 * @author exceptiontenyu@gmail.com
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static class SearchContext<K, V> {
		private StoreInfo sInfo;
		private ByteIterable key;
		private Function<ByteIterable, K> cnvKey;
		private Function<ByteIterable, V> cnvValue;
		private boolean next;
		private long skip;
		private long max;
		private Function<SearchContext<K, V>, Boolean> breakFunc;
		private Function<SearchContext<K, V>, Boolean> deleteFunc;
		private BiConsumer<SearchContext<K, V>, Cursor> init;
		/**
		 * 検索メソッド側で設定される
		 */
		private Cursor cursor;

		/**
		 * @param sInfo		対象ストア
		 * @param key		接頭辞
		 * @param cnvKey	キーをxodus形式から変換する
		 * @param cnvValue	バリューをxodus形式から変換する
		 * @param next	prevかnextか、どっち向きにカーソルを進めるか
		 * @param skip	何件飛ばすか。0なら飛ばさない
		 * @param max	最大取得件数。-1なら無条件
		 * @param breakFunc	falseを返したら検索終了。nullならmaxを小さめにするかストアのサイズが小さい事が保証されないと
		 * 巨大な負荷がかかる可能性がある。
		 * @param init		最初のカーソルの位置を決定する
		 */
		public SearchContext(StoreInfo sInfo, ByteIterable key,
				Function<ByteIterable, K> cnvKey,
				Function<ByteIterable, V> cnvValue, boolean next, long skip,
				long max, Function<SearchContext<K, V>, Boolean> breakFunc,
				Function<SearchContext<K, V>, Boolean> deleteFunc,
				BiConsumer<SearchContext<K, V>, Cursor> init) {
			this.sInfo = sInfo;
			this.key = key;
			this.cnvKey = cnvKey;
			this.cnvValue = cnvValue;
			this.next = next;
			this.skip = skip;
			this.max = max;
			this.breakFunc = breakFunc;
			this.deleteFunc = deleteFunc;
			this.init = init;
		}

		public Function<SearchContext<K, V>, Boolean> getDeleteFunc() {
			return deleteFunc;
		}

		public BiConsumer<SearchContext<K, V>, Cursor> getInit() {
			return init;
		}

		/**
		 * キャッシュ
		 */
		private transient byte[] keyB = null;

		public byte[] getKeyB() {
			if (keyB == null) {
				keyB = cnvBA(getKey());
			}
			return keyB;
		}

		public byte[] getHereValue() {
			if (getCursor().getValue() == null)
				return null;
			return cnvBA(getCursor().getValue());
		}

		public byte[] getHereKey() {
			return cnvBA(getCursor().getKey());
		}

		public Function<SearchContext<K, V>, Boolean> getBreakFunc() {
			return breakFunc;
		}

		public Cursor getCursor() {
			return cursor;
		}

		public ByteIterable getKey() {
			return key;
		}

		public StoreInfo getStoreInfo() {
			return sInfo;
		}

		public Function<ByteIterable, K> getCnvKey() {
			return cnvKey;
		}

		public Function<ByteIterable, V> getCnvValue() {
			return cnvValue;
		}

		public boolean isNext() {
			return next;
		}

		public long getSkip() {
			return skip;
		}

		public long getMax() {
			return max;
		}

		public void setCursor(Cursor cursor) {
			this.cursor = cursor;
		}
	}

	/**
	 * byte[]の前方一致検索にする処理
	 * @author exceptiontenyu@gmail.com
	 */
	public static class PrefixEqual<K, V>
			implements Function<SearchContext<K, V>, Boolean> {
		@Override
		public Boolean apply(SearchContext<K, V> ctx) {
			return prefixEqual(ctx.getKeyB(),
					ctx.getCursor().getKey().getBytesUnsafe());
		}
	}

	public static boolean prefixEqual(byte[] prefix, byte[] data) {
		if (data == null || data.length < prefix.length)
			return false;

		byte[] herePrefix = new byte[prefix.length];
		System.arraycopy(data, 0, herePrefix, 0, prefix.length);

		if (!Arrays.equals(prefix, herePrefix)) {
			return false;
		}
		return true;
	}

	public static class KeyPerfectEqualBreak<K, V>
			implements Function<SearchContext<K, V>, Boolean> {
		@Override
		public Boolean apply(SearchContext<K, V> a) {
			//TODO cnvKeyを通した後に比較すべきか？
			//もしcnvに入力されるbyte[]と出力値が1:1対応じゃないと結果が狂う
			return Arrays.equals(a.getKeyB(), a.getHereKey());
		}
	}

	public static class KeyValuePerfectEqualInit<K, V>
			implements BiConsumer<SearchContext<K, V>, Cursor> {
		private ByteIterable value;

		public KeyValuePerfectEqualInit(ByteIterable value) {
			this.value = value;
		}

		@Override
		public void accept(SearchContext<K, V> ctx, Cursor c) {
			c.getSearchBoth(ctx.getKey(), value);
		}
	}

	/**
	 * ある程度複雑な条件に対応可能な検索
	 * XodusのB木の検索でカーソルの初期位置を決定した後、
	 * 与えられた条件でシーケンシャルにカーソルが移動していく。
	 * インターフェースが複雑なので無理にこれを使う必要は無いだろう。
	 *
	 * @param ctx			検索条件等
	 * @return
	 */
	public <K, V> Map<K, V> search(SearchContext<K, V> ctx) {
		Map<K, V> r = new HashMap<>();
		return search(ctx, r);
	}

	/**
	 * DBから読み出された順番でイテレートできる複数件取得メソッド
	 *
	 * @param ctx
	 * @return
	 */
	public <K, V> Map<K, V> searchPreserveOrder(SearchContext<K, V> ctx) {
		Map<K, V> r = new LinkedHashMap<>();
		return search(ctx, r);
	}

	public <K, V> Map<K, V> search(SearchContext<K, V> ctx, Map<K, V> r) {
		Store s = getStore(ctx.getStoreInfo());

		if (ctx.getMax() == 0)
			return r;

		int skipCount = 0;
		int putCount = 0;
		try (Cursor c = s.openCursor(txn)) {
			ctx.setCursor(c);
			ctx.getInit().accept(ctx, c);

			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return null;

			do {
				skipCount++;
				if (skipCount <= ctx.getSkip())
					continue;

				if (ctx.getBreakFunc() != null
						&& !ctx.getBreakFunc().apply(ctx))
					break;

				r.put(ctx.getCnvKey().apply(c.getKey()),
						ctx.getCnvValue().apply(c.getValue()));
				putCount++;

				//検索インターフェースに削除機能がある。使わない事もできるが
				//検索というよりpairへのシーケンシャルアクセスインターフェースという事になる
				if (ctx.getDeleteFunc() != null
						&& ctx.getDeleteFunc().apply(ctx)) {
					c.deleteCurrent();
				}

				if (ctx.getMax() != -1 && putCount >= ctx.getMax())
					break;
			} while (ctx.isNext() ? c.getNext() : c.getPrev());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
		return r;
	}

	/**
	 * そのペアが無い状態にする、というメソッド。
	 * @return	もとから無ければtrue、あったら最初のペアを削除を試みて成功すればtrueを返す。
	 * 複数の該当するペアがある事を想定しない。
	 * @throws IOException
	 */
	public boolean deleteDupSingleNoExist(StoreInfo sInfo, ByteIterable key,
			ByteIterable val) throws IOException {
		if (sInfo == null || key == null || val == null)
			return false;

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			if (!c.getSearchBoth(key, val))
				return true;
			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return false;

			if (!c.deleteCurrent())
				throw new IOException();
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			throw e;
		}
	}

	/**
	 * @param sInfo		検索対象のストア
	 * @param key		検索条件
	 * @return			keyと対応付けられた全ての値。0件の場合、空リスト。例外が発生した場合null
	 */
	public <V> List<V> getDup(StoreInfo sInfo, ByteIterable key,
			Function<ByteIterable, V> cnvValue) {
		return getDup(sInfo, key, cnvValue, -1);
	}

	public <V> List<V> getDup(StoreInfo sInfo, ByteIterable key,
			Function<ByteIterable, V> cnvValue, int max) {
		Store s = getStore(sInfo);
		List<V> r = new ArrayList<>();

		//余計なデータが末尾に付加されるので除去したもの
		byte[] keyB = cnvBA(key);

		try (Cursor c = s.openCursor(txn)) {
			if (c.getSearchKey(key) == null)
				return r;
			do {
				ByteIterable hereKey = c.getKey();
				if (hereKey == null || hereKey == ByteIterable.EMPTY)
					break;

				byte[] hereKeyB = cnvBA(hereKey);
				if (!Arrays.equals(hereKeyB, keyB))
					break;

				r.add(cnvValue.apply(c.getValue()));
				if (max != -1 && r.size() >= max)
					break;
			} while (c.getNext());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
		return r;
	}

	public <V> V getDupSingle(StoreInfo sInfo, ByteIterable key,
			ByteIterable value, Function<ByteIterable, V> cnvValue) {
		Store s = getStore(sInfo);

		try (Cursor c = s.openCursor(txn)) {
			if (!c.getSearchBoth(key, value))
				return null;
			ByteIterable hereKey = c.getKey();
			if (hereKey == null || hereKey == ByteIterable.EMPTY
					|| !Arrays.equals(cnvBA(hereKey), cnvBA(key)))
				return null;

			return cnvValue.apply(c.getValue());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 指定されたキーとバリューに完全一致する最初のペアを削除する。
	 * キー重複型ストアでキー＋バリューで一意になる場合を想定。
	 *
	 * @param sInfo	対象ストア
	 * @return	削除されたか
	 * @throws IOException
	 */
	public boolean deleteDupSingle(StoreInfo sInfo, ByteIterable key,
			ByteIterable val) throws IOException {
		if (sInfo == null || key == null || val == null)
			return false;

		Store s = getStore(sInfo);

		try (Cursor c = s.openCursor(txn)) {
			if (!c.getSearchBoth(key, val))
				return false;

			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return false;

			if (!c.deleteCurrent())
				throw new IOException();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			throw e;
		}
		return true;
	}

	public static class KVSRecord<K, V> {
		private K key;
		private V value;

		@SuppressWarnings("unused")
		private KVSRecord() {
		}

		public KVSRecord(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
	}

	/**
	 * @param sInfo
	 * @param keyPrefix	TODO cnvS()の返値は末尾に１バイト加わるせいで誤作動するかも
	 * @param cnvValue	値を任意のオブジェクトにするコード
	 * @return
	 */
	public <K, V> V prefixSingle(StoreInfo sInfo, ByteIterable keyPrefix,
			Function<ByteIterable, V> cnvValue) {
		Store s = getStore(sInfo);
		byte[] keyPrefixB = cnvBA(keyPrefix);
		try (Cursor c = s.openCursor(txn)) {
			c.getSearchKeyRange(keyPrefix);

			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return null;

			if (!prefixEqual(keyPrefixB, cnvBA(c.getKey())))
				return null;

			return cnvValue.apply(c.getValue());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * Long等数値型キーにおいてレコードを検索する事を想定して作成。1件取得
	 * @param sInfo
	 * @param key
	 * @param cnvKey
	 * @param cnvValue
	 * @return
	 */
	public <K, V> KVSRecord<K, V> getRangeSingle(StoreInfo sInfo,
			ByteIterable key, Function<ByteIterable, K> cnvKey,
			Function<ByteIterable, V> cnvValue) {
		Store s = getStore(sInfo);

		try (Cursor c = s.openCursor(txn)) {
			ByteIterable success = c.getSearchKeyRange(key);
			if (success == null)
				return null;
			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return null;
			return new KVSRecord<K, V>(cnvKey.apply(c.getKey()),
					cnvValue.apply(c.getValue()));
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 数値的キーにおいて最後のレコードを取得することを想定
	 * @param sInfo
	 * @param cnvKey
	 * @param cnvValue
	 * @return	ストアが空ならnull、そうでなければ最後のレコード
	 */
	public <K, V> KVSRecord<K, V> getLast(StoreInfo sInfo,
			Function<ByteIterable, K> cnvKey,
			Function<ByteIterable, V> cnvValue) {
		Store s = getStore(sInfo);

		try (Cursor c = s.openCursor(txn)) {
			if (!c.getLast())
				return null;
			if (c.getKey() == null || c.getKey() == ByteIterable.EMPTY)
				return null;
			return new KVSRecord<K, V>(cnvKey.apply(c.getKey()),
					cnvValue.apply(c.getValue()));
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}

	}

	/**
	 * DBから読み出された順番でイテレートできる複数件取得メソッド
	 *
	 * @param sInfo
	 * @param key
	 * @param cnvKey
	 * @param cnvValue
	 * @param next
	 * @param skip
	 * @param max
	 * @return
	 */
	public <K, V> Map<K, V> getRangePreserveOrder(StoreInfo sInfo,
			ByteIterable key, Function<ByteIterable, K> cnvKey,
			Function<ByteIterable, V> cnvValue, boolean next, long skip,
			long max) {
		SearchContext<K, V> ctx = new SearchContext<>(sInfo, key, cnvKey,
				cnvValue, next, skip, -1, null, null,
				(runtimeCtx, c) -> c.getSearchKeyRange(runtimeCtx.getKey()));
		return searchPreserveOrder(ctx);
	}

	/**
	 * Long等数値型キーにおいてレコードを検索する事を想定して作成。
	 * 順序保証されない。
	 *
	 * @param sInfo
	 * @param key
	 * @param cnvKey
	 * @param cnvValue
	 * @param next		カーソルの進む向き
	 * @param skip		何件飛ばすか
	 * @param max		最大何件取得するか
	 * @return
	 */
	public <K, V> Map<K, V> getRange(StoreInfo sInfo, ByteIterable key,
			Function<ByteIterable, K> cnvKey,
			Function<ByteIterable, V> cnvValue, boolean next, long skip,
			long max) {
		SearchContext<K, V> ctx = new SearchContext<>(sInfo, key, cnvKey,
				cnvValue, next, skip, -1, null, null,
				(runtimeCtx, c) -> c.getSearchKeyRange(runtimeCtx.getKey()));
		return search(ctx);
		/*
				Store s = getStore(sInfo);

				Map<K, V> r = new HashMap<>();

				if (max == 0)
					return r;

				int startCount = 0;
				int putCount = 0;
				try (Cursor c = s.openCursor(txn)) {
					c.getSearchKeyRange(key);
					do {
						try {
							startCount++;
							if (startCount <= skip)
								continue;

							if (c.getKey() == null)
								break;

							r.put(cnvKey.apply(c.getKey()),
									cnvValue.apply(c.getValue()));
							putCount++;
							if (putCount >= max)
								break;
						} catch (Exception e) {
							//cnvKey,Value等で例外が発生する可能性があるが
							//できるだけ処理を継続する
						}
					} while (next ? c.getNext() : c.getPrev());
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					return null;
				}
				return r;
		*/
	}

	/**
	 * 処理時間はレコード件数や該当件数に比例。
	 * @param filter
	 * @return			DBにありfilterに無いキー
	 */
	public <K> List<K> dif(StoreInfo sInfo, HashSet<K> filter,
			Function<ByteIterable, K> cnvKey) {
		List<K> r = new ArrayList<>();

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			while (c.getNext()) {
				K key = cnvKey.apply(c.getKey());
				if (filter.contains(key))
					continue;
				r.add(key);
			}
		}

		return r;
	}

	/**
	 * DBに格納されている順番でキーを返す
	 * @param sInfo
	 * @param keyPrefix
	 * @param cnvKey
	 * @param max
	 * @return
	 */
	public <K> List<K> getKeys(StoreInfo sInfo, ByteIterable keyPrefix,
			Function<ByteIterable, K> cnvKey, int max) {
		List<K> r = new ArrayList<>();
		byte[] keyPrefixBA = cnvBA(keyPrefix);

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			if (c.getSearchKeyRange(keyPrefix) == null)
				return r;
			do {
				if (max != -1 && r.size() >= max)
					break;
				if (!prefixEqual(keyPrefixBA, cnvBA(c.getKey())))
					break;

				K key = cnvKey.apply(c.getKey());
				r.add(key);
			} while (c.getNext());
		}

		return r;
	}

	public <V> List<V> getValuesByKeyPrefix(StoreInfo sInfo,
			ByteIterable keyPrefix, Function<ByteIterable, V> cnvVal, int max) {
		List<V> r = new ArrayList<>();
		byte[] keyPrefixBA = cnvBA(keyPrefix);

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			if (c.getSearchKeyRange(keyPrefix) == null)
				return r;
			do {
				if (max != -1 && r.size() >= max)
					break;
				if (!prefixEqual(keyPrefixBA, cnvBA(c.getKey())))
					break;

				V val = cnvVal.apply(c.getValue());
				r.add(val);
			} while (c.getNext());
		}

		return r;
	}

	/**
	 * 全件取得。DBが巨大だとアプリが落ちる可能性がある。
	 * @param sInfo
	 * @return
	 */
	public Map<ByteIterable, ByteIterable> getAll(StoreInfo sInfo) {
		Map<ByteIterable, ByteIterable> r = new HashMap<>();

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			while (c.getNext()) {
				r.put(c.getKey(), c.getValue());
			}
		}
		return r;
	}

	public <K, V> Map<K, V> getAll(StoreInfo sInfo,
			Function<ByteIterable, K> cnvKey,
			Function<ByteIterable, V> cnvVal) {
		Map<K, V> r = new HashMap<>();

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			while (c.getNext()) {
				r.put(cnvKey.apply(c.getKey()), cnvVal.apply(c.getValue()));
			}
		}
		return r;
	}

	public <V> List<V> getAllValues(StoreInfo sInfo,
			Function<ByteIterable, V> cnvVal) {
		List<V> r = new ArrayList<>();

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			while (c.getNext()) {
				r.add(cnvVal.apply(c.getValue()));
			}
		}
		return r;
	}

	/**
	 * 全キーを取得する。重複しているキーは1件にまとめられる
	 * @param sInfo
	 * @param cnvKey
	 * @return
	 */
	public <K> List<K> getAllKeysNoDup(StoreInfo sInfo,
			Function<ByteIterable, K> cnvKey) {
		List<K> r = new ArrayList<>();

		Store s = getStore(sInfo);
		try (Cursor c = s.openCursor(txn)) {
			while (c.getNextNoDup()) {
				r.add(cnvKey.apply(c.getKey()));
			}
		}
		return r;
	}

	public long count(StoreInfo sInfo) {
		return getStore(sInfo).count(txn);
	}

	public boolean putObject(StoreInfo sInfo, ByteIterable object)
			throws IOException {
		if (!getStore(sInfo).put(txn, cnvS(singleObject), object))
			throw new IOException();
		return true;
	}

	public ByteIterable get(StoreInfo sInfo, ByteIterable key) {
		return getStore(sInfo).get(txn, key);
	}

	/**
	 * openStoreはストアファイルの作成が伴うようで、
	 * read-onlyトランザクションでは例外が出る。
	 * つまり、アプリ起動時に一度全ストアについて書き込み可能トランザクションで
	 * 一度開いて閉じるということをしておかなければ、
	 * read-onlyトランザクションは使えない。
	 *
	 * getStoreは既にストアが作成されている場合高速で（恐らく内部でキャッシュされている）
	 * 100万回が50ms等で性能上の問題は無い。
	 * @param sInfo
	 * @return
	 */
	public Store getStore(StoreInfo sInfo) {
		return txn.getEnvironment().openStore(sInfo.getStoreName(),
				sInfo.getType(), txn);
	}

	public boolean put(StoreInfo sInfo, ByteIterable key, ByteIterable val)
			throws IOException {
		if (!getStore(sInfo).put(txn, key, val))
			throw new IOException("Failed to put");
		return true;
	}

	/**
	 * auto-incrementなkeyでのみ利用可能。keyが必ず最大値なら利用できる。
	 * DBライブラリの説明によると数倍高速とあるがこのアプリでは１０％程度の高速化。
	 *
	 * @param sInfo
	 * @param key
	 * @param val
	 */
	public boolean putRight(StoreInfo sInfo, ByteIterable key,
			ByteIterable val) {
		getStore(sInfo).putRight(txn, key, val);
		return true;
	}

	public boolean remove(StoreInfo sInfo, ByteIterable key)
			throws IOException {
		if (!getStore(sInfo).delete(txn, key))
			throw new IOException();
		return true;
	}

	/**
	 * 指定されたストアの件数を返す
	 * @param s		対象ストア
	 * @return		sのペア数
	 */
	public static long countStatic(StoreInfo s) {
		return Glb.getObje().compute(txn -> {
			try {
				DBUtil util = new DBUtil(txn);
				return util.count(s);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	/**
	 * clearStoresによって指定された全ストアの全レコードを削除する
	 * @param s
	 * @param clearStores
	 * @throws Exception
	 */
	public void resetStores(List<StoreInfo> clearStores) throws Exception {
		for (StoreInfo e : clearStores) {
			try (Cursor c = getStore(e).openCursor(txn)) {
				while (c.getNext()) {
					if (!c.deleteCurrent())
						throw new Exception("Failed to delete");
				}
			}
		}
	}

}
