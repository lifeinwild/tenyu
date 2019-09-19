package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.nio.*;
import java.security.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 *
 * シングルスレッドかつIdObjectでなければ扱えない。
 *
 * HashStoreはUserStoreやEdgeStore等の内部状態になり、
 * IdObjectStoreの具象クラス毎にストアが作られる。
 *
 * 最上位ハッシュは常にハッシュツリー全体に依存するハッシュ値になり、
 * ここが一致するならそのクラスの全データが一致している。
 *
 * HashStoreValueのバージョンアップは困難。
 * 例えば配列の要素数が変わった場合、単品で終える事はできない。
 * 膨大なオブジェクト全体を一斉に更新するしかない。
 * 全レコードのバージョンが一致しているという前提で
 * いくつかのレコードのバージョンを調べて、
 * そのバージョンから最新バージョンまで繰り返しバージョンアップする。
 * それは非常に遅いので、DBが大規模化したら最後、
 * 基本的にHashStoreのバージョンアップは出来ないというつもりで居るべき。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class HashStore extends ModelStore<byte[], HashStoreValue>
		implements StoreByStore {

	/**
	 * ハッシュ値のサイズ
	 */
	private static final int hashSize = Glb.getConst().getHashSize();
	/**
	 * 上位から下位への削除の伝播
	 *
	 * @param	parent	全ての子要素がDBに存在している配列で、削除される
	 * @return	削除が発生したか
	 */
	/*
	private boolean removePropagation(HashStoreRecordPositioned parent) {
		if (!util.remove(generateMainStoreInfo(), parent.getKey().getBi()))
			return false;

		if (parent.getKey().getLevel() == 1)
			return true;

		for (Long removed : parent.getRemovedList()) {

			HashStoreKey childKey = new HashStoreKey(
					parent.getKey().getChildLevel(), removed);
			byte[] hashArray = getHashArrayNonNull(childKey);

			//子要素を取得している時点で非top
			boolean top = false;

			HashStoreRecordPositioned child = new HashStoreRecordPositioned(top,
					childKey, new HashStoreValue(hashArray));

			//削除済みなら
			if (child.getValue().isZeroFill()) {
				removePropagation(child);
			}
		}
		return true;
	}
	*/
	/**
	 * 各レベルの末尾の要素が削除された場合に呼ばれる。
	 * この処理はハッシュツリーの縮小を発生させる場合がある。
	 * この処理によってのみ配列の削除が発生する。
	 *
	 * @param child	呼ばれるきっかけとなった削除済みの下位レベルの要素
	 * @throws Exception
	 */
	/*
	private boolean removeLast(Child child) throws Exception {
		//DBから読み出す
		HashStoreRecordPositioned parent = getParentRecord(child.getKey());

		//配列処理
		int index = parent.getIndex(child.getKey().getId());
		parent.remove(index);

		//レベル1でtop
		if (parent.isTop() && parent.getKey().getLevel() == 1) {
			if (parent.getValue().isZeroFill()) {
				//level==1という保証があるので伝播必要無し
				return util.remove(generateMainStoreInfo(),
						parent.getKey().getBi())
						&& putTopLevel(parent.getKey().getLevel() - 1);
			} else {
				return util.put(generateMainStoreInfo(),
						parent.getKey().getBi(), parent.getValue().getBi());
			}
		} else if (parent.isTop() && parent.getKey().getLevel() != 1) {
			//レベル1以外でtop

			//残り1件ならレコードごと削除してトップを下げる。
			//この文脈で残り1件は、最上位配列になるべき配列の上に残ってしまってる状態。
			if (parent.getValue().isZeroExceptFirst()) {

				//level!=1なので下位レベルへ削除伝播	没案
				//				return removePropagation(parent)
				//						&& putTopLevel(parent.getKey().getLevel() - 1);

				return util.remove(generateMainStoreInfo(),
						parent.getKey().getBi())
						&& putTopLevel(parent.getKey().getLevel() - 1);
			} else {
				//そうでなければ配列を更新する
				return util.put(generateMainStoreInfo(),
						parent.getKey().getBi(), parent.getValue().getBi());
			}
		}

		//上にレベル1及びレベル1以外のtopの場合にreturnするコードがあるので以下非top
		//zeroFillならレコードごと削除
		if (parent.getValue().isZeroFill()) {
			Glb.debug("removed:" + parent.getKey());
			if (!util.remove(generateMainStoreInfo(), parent.getKey().getBi()))
				return false;

			//トップまでremove。
			return removeLast(parent);
		} else {
			//書き込み
			if (!util.put(generateMainStoreInfo(), parent.getKey().getBi(),
					parent.getValue().getBi())) {
				return false;
			}

			return update(parent);
		}

	}
	*/
	private static ByteIterable meaningLessKey = cnvL(
			IdObjectDBI.getFirstRecycleId());
	/**
	 * key : data
	 * <level><id> : <HashArrayData>
	 * level nはlevel n-1のオブジェクトをunit個数担当する。unit=100なら
	 * 1.0-99 in 2.0
	 * 1.100-199 in 2.1
	 * 2.0-99 in 3.0
	 * HashArrayDataは下位レベルのオブジェクトのハッシュをunit個数持つ。
	 */
	private static final String name = HashStoreValue.class.getSimpleName();

	private static final int unit = 20;//MTUを考慮して1400バイト以内に30;//>=2 簡単な計測によると30が最速

	/**
	 * ハッシュツリーを下に進む場合等に使う子IDを特定するための処理
	 * @param parentId		親ID
	 * @param arrayIndex	ハッシュ配列上の要素番号
	 * @return				子ID
	 */
	public static Long calculateChildId(Long parentId, int arrayIndex) {
		return parentId * unit + arrayIndex;
	}

	public static long calculateHashStoreCount(long levelZeroCount) {
		if (levelZeroCount <= 0) {
			return 0;
		}
		//unitと書き込み件数に応じたHashStore件数
		//hashstoreのレベル別レコード数
		long thisLevelCount = levelZeroCount;
		//hashstore全体のレコード数
		long count = 0;
		while (true) {
			//剰余>0なら1そうでなければ0
			long option = thisLevelCount % HashStore.getUnit() > 0 ? 1 : 0;
			//商
			long fill = thisLevelCount / HashStore.getUnit();
			//このレベルのレコード件数
			thisLevelCount = fill + option;
			//全体のレコード数に加算
			count += thisLevelCount;
			//			System.out.println("thisLevelCount=" + thisLevelCount);
			//			Glb.debug("thisLevelCount=" + thisLevelCount);
			if (fill == 0 || (fill == 1 && option == 0))
				break;
		}
		return count;
	}

	/**
	 * @return	配列内の最後のインデックス
	 */
	public static int calculateLastIndex() {
		return unit - 1;
	}

	/**
	 * @param levelZeroLastId	レベル0の最後のID
	 * @return					レベル0の最後のIDがlevelZeroLastIdの時最上位配列のレベルはいくつか
	 */
	public static int calculateTopLevel(Long levelZeroLastId) {
		if (levelZeroLastId == null || levelZeroLastId < 0)
			return 0;
		if (levelZeroLastId == 0)
			return 1;
		int level = 0;
		Long hereLevelLastId = levelZeroLastId;
		while (hereLevelLastId != 0) {
			level++;
			hereLevelLastId = hereLevelLastId / unit;
		}
		return level;
	}

	public static IdRangeDiff difCreateSimple(HashStore model,
			String storeName) {
		return simple((s) -> s.difCreate(model), storeName);
	}

	public static List<Long> difUpdateSimple(HashStore model,
			String storeName) {
		return simple((s) -> s.difUpdate(model), storeName);
	}

	/**
	 * @return	各レベルの最初のID
	 */
	private static final long getFirstId() {
		return 0L;
	}

	@Override
	protected byte[] cnvKey(ByteIterable bi) {
		return cnvBA(bi);
	}

	/**
	 * 値がオブジェクトのレベル
	 * @return	LevelZeroRecordのレベル
	 * HashStoreに記録されない仮想的なレベル
	 */
	public static final int getFirstLevel() {
		return 0;
	}

	public static HashStoreRecordPositioned getHashArraySimple(byte[] key,
			String storeName) {
		return simple((s) -> s.getHashArray(cnvBA(key)), storeName);
	}

	public static Long getLastIdSimple(String storeName) {
		return simple((s) -> s.getLastIdOfHashStore(), storeName);
	}

	/**
	 * @return	ハッシュ配列における2つ目の要素のID
	 */
	private static final long getSecondId() {
		return getFirstId() + 1;
	}

	/**
	 * 値が配列となる最初のレベル
	 * @return	通常のハッシュ配列のうち最下位のレベル
	 */
	public static final int getSecondLevel() {
		return 1;
	}

	public static HashStoreRecordPositioned getTopHashArraySimple(
			String storeName) {
		return simple((s) -> s.getTopHash(), storeName);
	}

	public static byte[] getTopKeySimple(String storeName) {
		return simple((s) -> s.getTopKey().getKeyBA(), storeName);
	}

	public static int getUnit() {
		return unit;
	}

	private static <T, R> R simple(Function<HashStore, R> f, String storeName) {
		return Glb.getObje().readRet(txn -> {
			try {
				HashStore s = new HashStore(storeName, txn);
				return f.apply(s);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	private final MessageDigest md;

	/**
	 * 各ストア毎にこのオブジェクトを持つので、
	 * ストア名をメンバー変数に設定できる。
	 */
	private final String storeName;

	@Override
	protected ByteIterable cnvKey(byte[] key) {
		return cnvBA(key);
	}

	public HashStore(String storeName, Transaction txn)
			throws NoSuchAlgorithmException {
		super(null);//txnは後から設定
		this.storeName = storeName;
		util.setTxn(txn);

		try {
			md = MessageDigest.getInstance(Glb.getConst().getDigestAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			throw e;
		}
	}

	@Override
	protected HashStoreValue chainversionup(ByteIterable loaded) {
		try {
			Object o = cnvO(loaded);
			if (o instanceof bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.HashStoreValue)
				return (bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.HashStoreValue) o;
		} catch (IOException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * 新しいハッシュ配列を作成する。
	 * @param child 作成のきっかけとなった下位レベルの要素
	 * @throws Exception
	 */
	private boolean create(Child child) throws Exception {
		//ハッシュ配列を作成
		HashStoreRecordPositioned parent = createParentRecord(child.getKey());

		//ハッシュを書き込み
		if (!parent.write(child.getHash(md), child.getKey().getId()))
			return false;

		//ハッシュ配列を書き込む
		if (!util.put(getMainStoreInfo(), parent.getKey().getBi(),
				parent.getValue().getBi()))
			return false;

		//上位伝播
		if (parent.isTop()) {
			//topが作成された場合 level==1のトップしか来ない
			return true;//putTopLevel(parent.getKey().getLevel());
		}

		if (parent.getKey().getId() == getSecondId()) {
			//どのレベルでも2件目id==secondIdで１つレベルを上げてトップを作る
			//レベル2以上は1件目id==firstIdでcreateが停止するから
			return createTop(parent);
		}

		//上にtopの場合returnするコードがあるので非top
		if (parent.getKey().isFirstInParent()) {
			//上位配列の最初の要素
			return create(parent);
		} else {
			//上位配列の2番目以降の要素。既に上位配列があるのでupdate
			return update(parent);
		}
	}

	/**
	 * 実データ作成時に呼ぶ。
	 * @return	書き込みが行われたか
	 * @throws Exception	DB操作に失敗、その他状態の不整合
	 */
	public boolean created(Long id, ByteIterable o) throws Exception {
		if (id == null || o == null) {
			Glb.getLogger().error("", new IllegalArgumentException(
					"HashStore created " + storeName));
			return false;
		}
		//作成されたオブジェクトoをHashStore上の概念にする
		Child d = new LevelZeroRecord(new HashStoreKey(getFirstLevel(), id),
				cnvBA(o));

		Long lastId = getLastIdOfHashStore();

		//とびとびのIDが作成された場合に対応する
		//idが飛ばされたか
		long dif = id - lastId;
		if (dif >= 2) {
			long lastIdParentId = HashStoreKey.calculateParentId(lastId);
			//とびとびのIDが作成された場合に対応するため、
			//間のID分の処理をする。
			long middleId;
			for (middleId = lastId + 1; middleId < id; middleId++) {
				long middleIdParentId = HashStoreKey
						.calculateParentId(middleId);
				if (lastIdParentId == middleIdParentId) {
					//現状getLastIdOfHashStore()の呼び出しが内部処理において無いから、
					//新規配列を作らない場合、何も処理する必要が無い。既に書き込むべきところが
					//0埋めされているはずだから。
					continue;
				} else {
					//削除済みとして作成
					NullRecord nullObj = new NullRecord(
							new HashStoreKey(getFirstLevel(), middleId));
					if (create(nullObj)) {
					} else {
						throw new Exception("中途半端に書き込んだ上で失敗");
					}
				}
			}

			//最後のIDを更新
			updateLastId(middleId);
		}

		boolean r = false;
		if (d.getKey().isFirstInParent() && id > lastId) {
			r = create(d);
		} else {
			//レベル0で親配列2個目以降の要素ならば非top
			r = update(d);
		}

		if (!r)
			return false;

		updateLastId(id);
		return true;
	}

	/**
	 * 親配列を作成する。
	 */
	private HashStoreRecordPositioned createParentRecord(
			HashStoreKey childKey) {
		HashStoreKey parentKey = new HashStoreKey(childKey.getParentLevel(),
				childKey.getParentId());
		//createの場合新規作成する
		byte[] hashArray = new byte[hashSize * unit];
		//createでそのレベルの最初の配列を作ったらそこがトップ
		boolean top = childKey.getParentId() == getFirstId();
		return new HashStoreRecordPositioned(top, parentKey,
				new HashStoreValue(hashArray));
	}

	/**
	 * 与えられた情報と同じ配列を作成する
	 */
	private HashStoreRecordPositioned createRecord(HashStoreKey key,
			byte[] hashArray, boolean top) {
		return createRecord(key, new HashStoreValue(hashArray), top);
	}

	private HashStoreRecordPositioned createRecord(HashStoreKey key,
			HashStoreValue val, boolean top) {
		return new HashStoreRecordPositioned(top, key, val);
	}

	/**
	 * 与えられた情報から配列を作成する
	 */
	private HashStoreRecordPositioned createRecord(int level, Long id) {
		HashStoreKey key = new HashStoreKey(level, id);
		return new HashStoreRecordPositioned(false, key,
				new HashStoreValue(getHashArrayNonNull(key)));
	}

	/**
	 * トップのハッシュ配列作成と1，2件目の記録
	 * @throws IOException
	 */
	private boolean createTop(HashStoreRecord second) throws IOException {
		//トップ作成時下位要素の1件目を取得して書き込む必要がある
		Long firstId = second.getKey().getId() - 1;
		byte[] firstO = cnvBA(util.get(getMainStoreInfo(),
				new HashStoreKey(second.getKey().getLevel(), firstId).getBi()));

		HashStoreRecordPositioned secondParent = createParentRecord(
				second.getKey());

		md.reset();
		byte[] firstHash = md.digest(firstO);
		if (!secondParent.write(firstHash, firstId))
			return false;

		md.reset();
		byte[] secondHash = md.digest(second.getValue().getHashArray());
		if (!secondParent.write(secondHash, second.getKey().getId()))
			return false;

		return util.put(getMainStoreInfo(), secondParent.getKey().getBi(),
				secondParent.getValue().getBi());

		//トップだから上位伝播がない
	}

	/**
	 * ハッシュツリーを縮小する
	 *
	 * 同調処理のために用意されたAPI
	 * もし自分のハッシュツリーが多数派より大きい場合、縮小しなければ
	 * 最上位配列が一致しない。
	 *
	 * @param lastIdAfter		ここまで縮小される。このIDは残る
	 * @return		lastIdBefore、
	 * または指定されたIDが最後のIDより後で処理する必要が無かった場合null
	 */
	public Long cut(Long lastIdAfter) throws Exception {
		if (lastIdAfter == null)
			throw new IllegalArgumentException();
		Long lastIdBefore = getLastIdOfHashStore();
		if (lastIdBefore == null)
			throw new IllegalStateException();

		if (lastIdAfter >= lastIdBefore)
			return null;

		//全件削除なら簡易な処理でクリアして終了。
		if (lastIdAfter.equals(-1L)) {
			util.resetStores(getStores());
			return lastIdBefore;
		}

		//以下処理は全件削除ではないという前提を置ける。もし全件削除の場合に以下を実行すると例外発生

		//mdはメンバー変数にもあるが、
		//同調処理と客観更新は並列に実行されてしまうリスクがあるのでインスタンスを分ける
		MessageDigest md = Glb.getUtil().getMD();

		int topLevelBefore = calculateTopLevel(lastIdBefore);
		//各レベルについて削除
		Long preLevelLastIdBefore = null;
		Long preLevelLastIdAfter = null;
		int topLevelAfter = calculateTopLevel(lastIdAfter);
		HashStoreRecord preTail = null;
		Long hereLevelLastIdBefore = lastIdBefore;
		Long hereLevelLastIdAfter = lastIdAfter;
		for (int level = getSecondLevel(); level <= topLevelBefore; level++) {
			preLevelLastIdBefore = hereLevelLastIdBefore;
			preLevelLastIdAfter = hereLevelLastIdAfter;
			hereLevelLastIdBefore = HashStoreKey
					.calculateParentId(preLevelLastIdBefore);
			hereLevelLastIdAfter = HashStoreKey
					.calculateParentId(preLevelLastIdAfter);
			//削除
			for (long id = hereLevelLastIdBefore; id > hereLevelLastIdAfter; id--) {
				//配列ごと削除
				util.remove(getMainStoreInfo(),
						new HashStoreKey(level, id).getBi());
			}

			//削除範囲と残存範囲の境界であるレコード
			HashStoreKey tailKey = new HashStoreKey(level,
					hereLevelLastIdAfter);
			HashStoreRecord tail = new HashStoreRecord(tailKey,
					getValue(tailKey));

			//境界がそのレベル唯一かつ最上位になるべき配列じゃなければ削除
			if (level > topLevelAfter) {
				//トップなら削除
				if (isTop(level)) {
					util.remove(getMainStoreInfo(),
							new HashStoreKey(level, getFirstId()).getBi());
					//一度ここに来たらそれ以降境界配列は全て削除される
					continue;
				}
			}

			//境界について一部要素を0埋め
			for (long id = preLevelLastIdAfter + 1; id <= calculateChildId(
					tail.getKey().getId(), calculateLastIndex()); id++) {
				tail.getValue().remove(HashStoreKey.calculateIndex(id));
			}

			if (tail.getKey().getLevel() > getSecondLevel()
					&& preTail != null) {
				//境界の子要素のうちの境界だった要素について更新
				tail.write(preTail.getHash(md), preTail.getKey().getId());
			}

			//書き込み
			if (!util.put(getMainStoreInfo(), tail.getKey().getBi(),
					tail.getValue().getBi()))
				throw new Exception("Failed to update tail");

			preTail = tail;
		}

		if (!putLastId(lastIdAfter))
			throw new Exception("Failed to putLastId");

		return lastIdBefore;
	}

	/**
	 * 作成処理の違い。作成処理がIDの範囲の違いを作る
	 * @param model
	 * @return			IDの範囲の違い
	 */
	public IdRangeDiff difCreate(HashStore model) {
		Long myLastId = getLastIdOfHashStore();
		Long modelLastId = model.getLastIdOfHashStore();
		return new IdRangeDiff(myLastId, modelLastId);
	}

	/**
	 * 更新処理の違い。主に更新処理が各IDの内容の違いを作る
	 *
	 * IDの範囲が一致している２つのDBについて、
	 * 内容が違っているレコードのIDを報告する。
	 *
	 * ネットワークごしのHashStoreと比較する想定。
	 * HashStoreを近傍との通信をベースとして実装する。
	 * @return nullならこのHashStore全体が空
	 */
	public List<Long> difUpdate(HashStore model) {
		List<Long> dif = new ArrayList<>();
		HashStoreRecordPositioned top = getTopHash();//topのキーが一致している前提
		difUpdateInternal(top, dif, model);
		return dif;
	}

	private boolean difUpdateInternal(HashStoreRecordPositioned my,
			List<Long> dif, HashStore model) {
		ByteIterable key = my.getKey().getBi();
		HashStoreRecordPositioned other = model.getHashArray(key);
		List<Long> difIds = my.dif(other);

		if (my.getKey().getLevel() == 1) {
			return dif.addAll(difIds);
		}
		for (Long difId : difIds) {
			if (!difUpdateInternal(
					createRecord(my.getKey().getLevel() - 1, difId), dif,
					model)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * id : ハッシュ値がunit個数敷き詰められたバイト配列
	 */
	@Override
	public StoreInfo getMainStoreInfo() {
		return new StoreInfo(name + "_" + storeName + "_pathToHash");
	}

	public HashStoreRecordPositioned getHashArray(ByteIterable key) {
		ByteIterable val = util.get(getMainStoreInfo(), key);
		if (val == null)
			return null;
		return createRecord(HashStoreKey.parse(key), cnvBA(val), false);
	}

	public HashStoreRecordPositioned getHashArray(HashStoreKey key) {
		return createRecord(key, getValue(key), false);
	}

	private byte[] getHashArrayNonNull(HashStoreKey key) {
		byte[] hashArray;
		ByteIterable bi = util.get(getMainStoreInfo(), key.getBi());
		if (bi == null || ByteIterable.EMPTY.equals(bi)) {
			return null;
			//耐障害性を高めるため、nullなら新規作成
			//hashArray = new byte[hashSize * unit];
		} else {
			hashArray = cnvBA(bi);
		}
		return hashArray;
	}

	/**
	 * このメソッドで最上位ハッシュ配列を得てもisTopは正しい値を返さない。
	 * 常にisTopはfalseである。
	 * topを得た後に呼び出す予定だから実用途上falseで合っている。
	 */
	public HashStoreRecordPositioned getHashArrayNotTop(int level, Long id) {
		return getHashArray(new HashStoreKey(level, id).getBi());
	}

	/**
	 * @return	HashStoreのレベル0の最後のID。
	 * そのIDに対応づく実データは削除済みである可能性がある。
	 */
	public Long getLastIdOfHashStore() {
		ByteIterable bi = util.get(getLastIdStoreInfo(), meaningLessKey);
		//最初lastIdはnullでその場合最初のID-1を設定して計算を合わせる
		if (bi == null)
			return IdObjectDBI.getFirstRecycleId() - 1;
		return cnvL(bi);
	}

	/**
	 * 最後のIDを取得できるストア
	 */
	private StoreInfo getLastIdStoreInfo() {
		return new StoreInfo(name + "_" + storeName + "_lastId");
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * 親配列を取得する。
	 */
	private HashStoreRecordPositioned getParentRecord(HashStoreKey childKey) {
		HashStoreKey parentKey = new HashStoreKey(childKey.getParentLevel(),
				childKey.getParentId());
		byte[] hashArray = getHashArrayNonNull(parentKey);
		return new HashStoreRecordPositioned(isTop(childKey.getParentLevel()),
				parentKey, new HashStoreValue(hashArray));
	}

	/**
	 * @return	最上位ハッシュ
	 */
	public HashStoreRecordPositioned getTopHash() {
		HashStoreKey hashStoreKey = getTopKey();
		if (hashStoreKey == null)
			return null;
		ByteIterable key = hashStoreKey.getBi();
		if (key == null)
			return null;
		ByteIterable val = util.get(getMainStoreInfo(), key);
		if (val == null)
			return null;
		return createRecord(HashStoreKey.parse(key), cnvBA(val), true);
	}

	/**
	 * @return	最上位ハッシュのキー
	 */
	public HashStoreKey getTopKey() {
		int topLevel = calculateTopLevel(getLastIdOfHashStore());

		return new HashStoreKey(topLevel, getFirstId());
	}

	public HashStoreValue getValue(ByteIterable key) {
		ByteIterable bi = util.get(getMainStoreInfo(), key);
		if (bi == null)
			return null;
		return new HashStoreValue(cnvBA(bi));
	}

	public HashStoreValue getValue(HashStoreKey key) {
		return getValue(key.getBi());
	}

	@Override
	public List<StoreInfo> getStoresModelStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(getLastIdStoreInfo());
		return r;
	}

	/**
	 * @param level
	 * @return	このレベルは1件のみ配列を持つか
	 * ハッシュツリー全体が整備済みの場合、それはこのレベルがトップであることを意味する
	 */
	private boolean isTop(int level) {
		if (level < getSecondLevel())
			throw new IllegalArgumentException("illegal level");
		//2件目、つまりid==1が無ければトップ
		return util.get(getMainStoreInfo(),
				new HashStoreKey(level, getSecondId()).getBi()) == null;
	}

	//TODO:デバッグ用に作ったけど消すべきか
	public void printAll() {
		for (Entry<ByteIterable, ByteIterable> e : util
				.getAll(getMainStoreInfo()).entrySet()) {
			if (ByteIterable.EMPTY.equals(e.getKey()))
				continue;
			System.out.println("keyToHash key:" + cnvBA(e.getKey()));
			System.out.println(Arrays.toString(cnvBA(e.getValue())));
			if (util.get(getMainStoreInfo(), e.getKey()) == null) {
				System.out.println("exception");
			}
		}
		for (Entry<ByteIterable, ByteIterable> e : util
				.getAll(getLastIdStoreInfo()).entrySet()) {
			if (ByteIterable.EMPTY.equals(e.getKey()))
				continue;
			System.out.println("nameToLastId key:" + cnvBA(e.getKey()));
			System.out.println("nameToLastId val:" + cnvL(e.getValue()));
		}
	}

	private boolean putLastId(Long id) throws Exception {
		if (id == null) {
			if (!util.remove(getLastIdStoreInfo(), meaningLessKey))
				throw new Exception("Failed to remove");
		} else {
			if (!util.put(getLastIdStoreInfo(), meaningLessKey, cnvL(id)))
				throw new Exception("Failed to write");
		}
		return true;
	}

	/**
	 * @param child
	 * @return
	 * @throws Exception
	 */
	private boolean remove(Child child) throws Exception {
		//DBから読み出す
		HashStoreRecordPositioned parent = getParentRecord(child.getKey());

		//配列処理
		int index = HashStoreKey.calculateIndex(child.getKey().getId());
		//レベル1の配列だけ下位レベルの削除に応じて0埋めされる
		//これによって、0埋めを目印としてハッシュツリーの情報だけで
		//レベル1の最後のIDが分かる。
		parent.getValue().remove(index);
		if (!util.put(getMainStoreInfo(), parent.getKey().getBi(),
				parent.getValue().getBi()))
			throw new Exception("Failed to update array");

		//childは最後のIDではないことが前提で、
		//途中のIDが削除されたことによる配列の削除やTOP配列の変更は起こりえないので
		//そのような可能性に対応するコードは必要ない

		//親配列があれば更新
		if (parent.isTop()) {
			return true;
		} else {
			return update(parent);
		}
	}

	/**
	 * 実データ削除時に呼ぶ
	 * @param id	削除されたIdObjectのid
	 * @return		削除できたか
	 * @throws Exception
	 */
	public boolean removed(Long id) throws Exception {
		if (id == null || id < IdObjectDBI.getFirstRecycleId()) {
			Glb.getLogger().error("", new IllegalArgumentException(
					"HashStore removed " + storeName));
			return false;
		}

		Long lastId = getLastIdOfHashStore();
		//削除は1件以上作成された後なので必ずlastIdは存在する
		if (lastId == null) {
			throw new IllegalArgumentException(
					"No lastId" + storeName + " removedId=" + id);
		}

		if (id > lastId || id < getFirstId()) {
			throw new IllegalArgumentException("HashStore removed " + storeName
					+ " id=" + id + " lastId=" + lastId);
		}

		Child d = new LevelZeroRecord(new HashStoreKey(getFirstLevel(), id),
				null);
		//レベル0の削除は必ず非top
		if (!remove(d))
			throw new Exception("Failed to remove");

		return true;
	}

	/**
	 * 既存のハッシュ配列を更新する。
	 * 親配列がある場合のみ呼べるので、呼ぶ直前にisTop()等で最上位配列であるかのチェックが必要
	 * @param child		下位レベルの更新ないし作成された要素
	 * @return	更新に成功したか
	 * @throws Exception
	 */
	private boolean update(Child child) throws Exception {
		HashStoreRecordPositioned parent = getParentRecord(child.getKey());

		if (!parent.write(child.getHash(md), child.getKey().getId()))
			return false;

		if (!util.put(getMainStoreInfo(), parent.getKey().getBi(),
				parent.getValue().getBi())) {
			throw new Exception("Failed to update array");
		}

		//上位伝播
		if (parent.isTop())
			return true;

		//トップまでupdate
		return update(parent);
	}

	/**
	 * 実データ更新時に呼ぶ
	 * @throws Exception
	 */
	public boolean updated(Long id, ByteIterable o) throws Exception {
		if (id == null || o == null) {
			Glb.getLogger().error("", new IllegalArgumentException(
					"HashStore updated " + storeName));
			return false;
		}
		Child d = new LevelZeroRecord(new HashStoreKey(getFirstLevel(), id),
				cnvBA(o));
		//レベル0で更新時は非top
		return update(d);
	}

	/**
	 * createdIdが最後のIDより大きければ更新する
	 * @param createdId		最後のID候補
	 * @return	最後のIDが更新されたか
	 * @throws	DB操作に失敗
	 */
	private boolean updateLastId(Long createdId) throws Exception {
		Long lastId = getLastIdOfHashStore();
		if (lastId != null && lastId >= createdId)
			return false;

		return putLastId(createdId);
	}

	/**
	 * 親配列から子要素として利用可能なオブジェクトが備えるインターフェース
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private static interface Child {
		/**
		 * @param md	内部でreset()される
		 * @return		このレコードの値のハッシュ値
		 */
		byte[] getHash(MessageDigest md);

		HashStoreKey getKey();

	}

	/**
	 * IDの範囲を一致させた後に各IDの内容を一致させるが、
	 * その時に利用する違いを表すクラス
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class ContentDif {
		List<Long> contentDif = new ArrayList<>();

		public void addContentDif(Long id) {
			contentDif.add(id);
		}

		public List<Long> getContentDif() {
			return contentDif;
		}
	}

	public static class HashStoreKey {
		/**
		 * @param childId		1つ下のレベルのid
		 * @return			childIdはこのハッシュ配列の何番目か
		 */
		public static int calculateIndex(Long childId) {
			return (int) (childId % unit);
		}

		/**
		 * @param id
		 * @return	この配列を要素とする1つ上のレベルの配列のID
		 */
		public static Long calculateParentId(long id) {
			if (id < 0)
				return -1L;
			return id / unit;
		}

		public static HashStoreKey parse(byte[] key) {
			ByteBuffer buf = ByteBuffer.wrap(key);
			int level = buf.getInt();
			long id = buf.getLong();
			return new HashStoreKey(level, id);
		}

		public static HashStoreKey parse(ByteIterable key) {
			return parse(cnvBA(key));
		}

		/**
		 * そのレベルにおけるid
		 */
		private final Long id;

		/**
		 * レベル
		 */
		private final int level;

		/**
		 * kryo
		 */
		@SuppressWarnings("unused")
		private HashStoreKey() {
			level = -1;
			id = null;
		}

		public HashStoreKey(int level, Long id) {
			this.level = level;
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HashStoreKey))
				return false;
			HashStoreKey otherKey = (HashStoreKey) o;

			if (level != otherKey.getLevel())
				return false;

			if (!id.equals(otherKey.getId()))
				return false;

			return true;
		}

		/**
		 * このハッシュ配列のキーをxodus形式で返す
		 * @return
		 */
		public ByteIterable getBi() {
			return cnvBA(getKeyBA());
		}

		public int getChildLevel() {
			return level - 1;
		}

		public Long getId() {
			return id;
		}

		public byte[] getKeyBA() {
			ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES + Long.BYTES);
			buf.putInt(level);
			buf.putLong(id);
			return buf.array();
		}

		public int getLevel() {
			return level;
		}

		public Long getParentId() {
			return calculateParentId(id);
		}

		public int getParentLevel() {
			return level + 1;
		}

		/**
		 * @param id
		 * @return		idはその親のハッシュ配列内で最初の要素か
		 */
		public boolean isFirstInParent() {
			return getId() % unit == getFirstId();
		}

		/**
		 * @return	親のハッシュ配列内で2つ目の要素か
		 */
		public boolean isSecondInParent() {
			return id % unit == getSecondId();
		}

		@Override
		public String toString() {
			return "level=" + level + " id=" + id;
		}
	}

	/**
	 * HashStoreのレコード
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class HashStoreRecord implements Child {
		/**
		 * KVSのキー
		 */
		protected HashStoreKey key;

		/**
		 * ハッシュ配列の内容
		 * ハッシュ配列は下位レベルのオブジェクトのハッシュをunit個数持つ。
		 * ハッシュ配列は上位レベルで1個のオブジェクトとみなされる。
		 * ここでオブジェクトはハッシュ配列またはDBの実データのbyte[]表現。
		 */
		protected HashStoreValue value;

		/**
		 * kryo
		 */
		private HashStoreRecord() {
		}

		public HashStoreRecord(HashStoreKey key, HashStoreValue value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * ２つのハッシュツリーを比較する際に
		 * ハッシュツリー全体で同じ位置にあるハッシュ配列を比較し、
		 * 異なっている部分についてchildIdにして返す。
		 * @param o
		 * @return
		 */
		public List<Long> dif(HashStoreRecord o) {
			List<Long> r = new ArrayList<>();
			for (int i = (int) getFirstId(); i < unit; i++) {
				if (!Arrays.equals(getValue().read(i), o.getValue().read(i))) {
					Long childId = calculateChildId(key.getId(), i);
					r.add(childId);
				}
			}
			return r;
		}

		/**
		 * @param o		比較対象
		 * @return		HashStoreKeyのbyte[]表現の一覧
		 */
		public List<HashStoreKey> difReturnKey(HashStoreRecord o) {
			List<Long> difChildIds = dif(o);
			List<HashStoreKey> r = new ArrayList<>();
			for (Long childId : difChildIds)
				r.add(new HashStoreKey(key.getLevel() - 1, childId));
			return r;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof HashStoreRecord)) {
				return false;
			}
			HashStoreRecord other = (HashStoreRecord) obj;

			if (!key.equals(other.getKey()))
				return false;

			if (!value.equals(other.getValue()))
				return false;

			return true;
		}

		/**
		 * @return	存在する最初の子要素のID
		 */
		public long getChildLastId() {
			long base = key.getId() * unit;
			return base + value.getLastIndex();
		}

		@Override
		public byte[] getHash(MessageDigest md) {
			md.reset();
			return md.digest(value.getHashArray());
		}

		@Override
		public HashStoreKey getKey() {
			return key;
		}

		/**
		 * @return	この配列の子要素のうち削除された要素の一覧
		 */
		public List<Long> getRemovedList() {
			List<Long> r = new ArrayList<>();
			for (int i = (int) getFirstId(); i < unit; i++) {
				if (getValue().isZeroFill(i)) {
					r.add(calculateChildId(key.getId(), i));
				}
			}
			return r;
		}

		public HashStoreValue getValue() {
			return value;
		}

		public final boolean validate() {
			return key != null && value != null;
		}

		/**
		 * 配列にハッシュを書き込む。childIdから位置が計算される
		 * @param srcHash
		 * @param childId このHashArrayより１つ下のレベルのid
		 * @return	書き込みに成功したか
		 */
		public boolean write(byte[] srcHash, Long childId) {
			//0埋めの配列を拒否
			//			if (Glb.getUtil().isZeroFill(srcHash))
			//				return false;

			//この配列に属する子IDじゃなければ例外
			if ((childId / unit) != key.getId())
				throw new IllegalArgumentException();
			//この配列内のインデックス
			int index = HashStoreKey.calculateIndex(childId);
			System.arraycopy(srcHash, 0, value.getHashArray(), index * hashSize,
					srcHash.length);
			return true;
		}
	}

	/**
	 * これも1レコードの表現だが、top値の設定があり、
	 * HashStore上に配置済みのレコードでありいくつかの前提が成立している。
	 * createRecord系メソッドからのみ作成される事で
	 * HashStoreとの状態の整合性について前提が成立する。
	 *
	 * 新しいレコードが作成された時、ハッシュ配列が作成される場合がある。
	 * ハッシュ配列の作成は新しいレコードの作成で、連鎖的にハッシュ配列が
	 * 作成される場合がある。ハッシュ配列は作成された時点で最上位か否か分かる。
	 *
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class HashStoreRecordPositioned extends HashStoreRecord {
		/**
		 * 最上位ハッシュ配列か
		 * memo:topはtransientにしたい。DBにtopかを記録するつもりはないから。
		 * しかし通信で他のノードにハッシュ配列がtopかを伝える事はある。
		 * 今のところHashStoreRecordPositionedそのものをDBに記録する事が無いから、
		 * transientを付けないことで対応している。
		 * 付けるなら、整合性情報を受信するところでtop値を設定する必要がある。
		 */
		private boolean top;

		/**
		 * kryo
		 */
		@SuppressWarnings("unused")
		private HashStoreRecordPositioned() {
		}

		public HashStoreRecordPositioned(boolean top, HashStoreKey key,
				HashStoreValue value) {
			super(key, value);
			this.top = top;
		}

		@Override
		public boolean equals(Object obj) {
			/*
			if (obj == null || !(obj instanceof HashStoreRecordPositioned)) {
				return false;
			}
			HashStoreRecordPositioned other = (HashStoreRecordPositioned) obj;

			//
			//			if (top != other.isTop())
			//				return false;
			*/
			return super.equals(obj);
		}

		/**
		 * 最上位ハッシュ配列は常にそのレベルで唯一のハッシュ配列で、
		 * 全ハッシュ配列中最もレベルが高く、
		 * ハッシュツリー全体のハッシュを算出できる。
		 * @return	このハッシュ配列は最上位ハッシュ配列か
		 */
		public boolean isTop() {
			return top;
		}

	}

	public static class HashStoreValue {

		private byte[] hashArray;

		/**
		 * kryo
		 */
		@SuppressWarnings("unused")
		private HashStoreValue() {
		}

		public HashStoreValue(byte[] hashArray) {
			this.hashArray = hashArray;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof HashStoreValue))
				return false;

			HashStoreValue other = (HashStoreValue) obj;

			if (!Arrays.equals(hashArray, other.getHashArray()))
				return false;
			return true;
		}

		/**
		 * @return	ハッシュ配列をxodus形式にしたもの
		 */
		public ByteIterable getBi() {
			return cnvBA(getHashArray());
		}

		public byte[] getHashArray() {
			return hashArray;
		}

		/**
		 * @return	存在する子要素の最後の位置。全子要素が0埋めなら-1
		 */
		public int getLastIndex() {
			for (int i = 0; i < unit; i++) {
				if (isZeroFill(i))
					return i - 1;
			}
			return calculateLastIndex();
		}

		/**
		 * @return	ハッシュ配列が埋まっているか
		 */
		public boolean isFillup() {
			for (int i = calculateLastIndex() * hashSize; i < unit
					* hashSize; i++) {
				if (hashArray[i] != 0) {
					return true;
				}
			}
			return false;
		}

		/**
		 * zeroFill or zeroExceptFirst
		 */
		public boolean isZeroExceptFirst() {
			for (int i = hashSize; i < hashArray.length; i++) {
				if (hashArray[i] != 0)
					return false;
			}
			return true;
		}

		/**
		 * @return	0埋めされているか、または全要素が削除済みか
		 */
		public boolean isZeroFill() {
			for (byte b : hashArray) {
				if (b != 0)
					return false;
			}
			return true;
		}

		/**
		 * @param i	この位置のハッシュのみが対象になる
		 * @return	0埋めされているか
		 */
		public boolean isZeroFill(int i) {
			return Glb.getUtil().isZeroFill(read(i));
		}

		/**
		 * @param index		ハッシュ値のハッシュ配列内番号
		 * @return			指定された位置に格納されたハッシュ値
		 */
		public byte[] read(int index) {
			byte[] r = new byte[hashSize];
			System.arraycopy(getHashArray(), index * hashSize, r, 0, r.length);
			return r;
		}

		/**
		 * ハッシュ配列の指定された部分を0埋め。削除を意味する
		 * @param index
		 */
		public void remove(int index) {
			final byte[] empty = new byte[hashSize];
			System.arraycopy(empty, 0, getHashArray(), index * hashSize,
					empty.length);
		}

		public void setHashArray(byte[] hashArray) {
			this.hashArray = hashArray;
		}
	}

	/**
	 * ２つのDBのIDの範囲の違い。
	 * ２つのDBを比較するに当たり、各レコードの内容を無視して
	 * IDの範囲の違いをまず修正するので。
	 * そこにはIDの範囲が単にどちらかが先行しているという想定がある。
	 * まばらに欠落しているという想定が無い。
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class IdRangeDiff {
		Long modelLastId;
		Long myLastId;

		public IdRangeDiff(Long myLastId, Long modelLastId) {
			this.modelLastId = modelLastId;
			this.myLastId = myLastId;
		}

		public long difCount() {
			return modelLastId - myLastId;
		}

		public Long getModelLastId() {
			return modelLastId;
		}

		public Long getMyLastId() {
			return myLastId;
		}

		public boolean isLeadingMe() {
			return modelLastId < myLastId;
		}

		public boolean isLeadingModel() {
			return modelLastId > myLastId;
		}

		public boolean noCreateDif() {
			return myLastId.equals(modelLastId);
		}
	}

	/**
	 * レベル０はハッシュ配列ではなくシリアライズされたオブジェクトを値とし、
	 * HashStoreに記録されない。レベル１のハッシュ値を作るためにある。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private static class LevelZeroRecord implements Child {
		private HashStoreKey key;
		private byte[] serialized;

		public LevelZeroRecord(HashStoreKey key, byte[] serialized) {
			//レベル1の要素=レベル0レコードのgetHashの返値
			//レベル1の要素の0埋めは削除を意味するので、
			//ここでレベル1レコードのgetHashの返値になるserializedは0埋めであってはならない
			if (serialized != null && Glb.getUtil().isZeroFill(serialized)) {
				throw new IllegalArgumentException("serialized is zero fill");
			}

			this.key = key;
			this.serialized = serialized;
		}

		@Override
		public byte[] getHash(MessageDigest md) {
			md.reset();
			return md.digest(serialized);
		}

		@Override
		public HashStoreKey getKey() {
			return key;
		}
	}

	/**
	 * 空作成のため
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private static class NullRecord implements Child {
		private HashStoreKey key;

		public NullRecord(HashStoreKey key) {
			this.key = key;
		}

		@Override
		public byte[] getHash(MessageDigest md) {
			int size = Glb.getConst().getHashSize();
			return new byte[size];
		}

		@Override
		public HashStoreKey getKey() {
			return key;
		}

	}

}
