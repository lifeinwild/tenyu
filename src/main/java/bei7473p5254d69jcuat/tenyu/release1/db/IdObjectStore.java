package bei7473p5254d69jcuat.tenyu.release1.db;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import io.netty.util.internal.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * T1:サブインデックスのためのインターフェース。変更されない
 * T2:最新版のクラスを子クラスで設定する。変更される
 *
 * オブジェクトをシリアライズし、Long id:byte[] objectのkvsを必ず持つ。
 * そのkvsをメインストアと呼ぶ。
 * 加えて、property:idのサブインデックスのストアをいくつか持つ。
 *
 * ここにあるDBの思想は、やや特殊である。
 * まずP2Pアプリということでスキーマエボリューション問題が深刻である。
 * エンドユーザーのPCで長時間のスキーマ更新はできない。
 *
 * しかし、chainversionupのアイデアによってメインストアは
 * スキーマエボリューションの問題から逃れる。
 *
 * 残る問題はインデックスストアである。
 * インデックスストアのスキーマエボリューションの問題を解決する重要なアイデアは、
 * ”複雑なクエリは人間に凝ったデータを見せるためで内部処理は単純な検索しかない”
 * という信念である。つまりプログラム内部の動作だけを考えれば
 * 複雑なクエリに対応する必要が無く、
 * インデックスストアは極めて限られ、スキーマ更新が将来に渡ってないと
 * 考えられる属性のみに対応するなら、問題は解決される。
 * その将来に渡って変更されないと信じられるインターフェースは<ModelClass>DBIという
 * インターフェースに定義される。
 * ところで、本アプリは人間が使うのだから、複雑なクエリをして凝ったデータを
 * 見たい人も居るはずである。
 * その場合、WebAPIを通じてDBのデータを吸い出し、より高機能なDBの元で検索する。
 * 本アプリのDB更新は数分に1回といった固定間隔であり、
 * 前回更新日時と更新間隔をWebAPIで取得すれば、更新直後に同期可能である。
 * さらに、人によって、その高機能なDBをWWWで提供する。
 * 低スペックな人も複雑なクエリができる。
 * ”高機能DBでの複雑なクエリを通じて操作対象のIDを特定して、
 * 基盤ソフトウェア上で操作する”。
 * つまり本アプリは内部動作のために僅かなインデックスを用意し、
 * ユーザーのためのインデックスを用意せず、
 * 高機能な検索は外部ツールのグラフDBに委譲される。
 *
 * 以上のアイデアで、モデルクラスのバージョンアップを想定しつつ、
 * P2Pソフトウェアでスキーマエボリューションを回避していける。
 *
 * 各具象ストアは1つ対象とするモデルクラスを持つ。
 * 例えばUserStoreはUserというモデルクラスを対象とする。
 * 各具象ストアは対象とするモデルクラスのgetClass()#getSimpleName()をnameの値とする。
 * SimpleNameを使うから、モデルクラスの名前はかえられないが、完全修飾名はかえられる。
 * nameをpublicにすること。
 * nameをgetName()の返値にすること。
 *
 * 重複キーのストアは_Dupを末尾につける
 *
 * getId系メソッドやLong idのIDはIdObjectのrecycleIdを意味する
 *
 * ・全文検索を実装すべきか？
 * 実装するならIdObjectStoreのレベルになり、IdObjectIはString keywords()を定義し、
 * keywords()において各具象クラスは自身の状態について連結した1つの文字列を出力し、
 * 何らかの形態素解析でキー一覧を得る事になるだろう。
 * 全文検索は内部動作で使う事は無く、GUIでユーザーが使うだけだから、
 * このプログラムの設計方針からすれば外部ツールに委譲すべき機能である。
 * KVS上で全文検索をマネージメントしていくのは考慮する事が増えそうだし、却下したい。
 * ただ簡単に性能負荷を考えてみると、削除・更新処理以外は問題無いと思う。
 * 削除・更新処理はかなり重くなることが予想され、
 * メッセージリストの反映処理において遅れが生じる可能性がある。
 * 1キー上の大量のIDリストから1つのIDを見つけて削除する処理はシーケンシャルである。
 * つまり件数が増えて1キーに対して大量のIDが登録されたケースが出ると、
 * そのキーのIDの削除は非常に遅い。
 * ＞性能上の考慮に影響を与えすぎるので却下
 * ＞巨大キーの出現は設計から予測できないので、
 * ＞Content#getApplySize()等で負荷量を決定する事もできない。
 *
 * ・メインストアをIdObjectStoreのレベルにあげるべき？
 * ＞staticにメインストアを取得する事ができなくなる。一方でstaticに決定するものである
 * ＞継承等の機構がstaticメソッドでは効かないので
 *
 * 一般的な留意事項として、トランザクション中に書き込んだデータが見えるのは
 * Transactionを引数に受け取るメソッドのみ。
 * 例えばGameItemInstance#getItemClassなどは既にコミット済みのデータから取得する。
 * それを留意してコードを書く必要がある。
 *
 * リサイクルIDは削除されたIDが記録され次に作成されるオブジェクトに
 * 再度割り振られるという概念。
 * これはハッシュツリーの事情から生じた仕様で、
 * IDは本来リサイクルされるべきではないが、
 * このアプリでは止むを得なかった。
 * 非リサイクルIDにする仕様も相当検討したが、同調処理があまりにも難しくなり、
 * 同調が素早く収束するという保証ができなかった。
 * IDのリサイクルがあると、モデルの各所でID参照があるが、
 * どこかから参照されているオブジェクトを削除してそのIDがリサイクルされた時、
 * ID参照がシャドー生成される恐れがある。
 * そのため、本アプリはエンドユーザーによる削除処理を認められない。
 * 外部ツールによる検討によって削除ID一覧を作成し、議決によって削除する機能は可能。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class IdObjectStore<T1 extends IdObjectDBI, T2 extends T1>
		extends ModelStore<Long, T2> {
	public static StoreInfo getMainStoreInfoStatic(String modelName) {
		return new StoreInfo(modelName + "_idTo" + modelName);
	}

	/*
	public static <S extends IdObjectStore<T1, T2>,
			T1 extends IdObjectDBI,
			T2 extends T1> T2 getSimple(Function<Transaction, S> getStoreFunc,
					Long id) {
		return simple(getStoreFunc, (s) -> s.get(id));
	}
	*/

	/**
	 * simple系インターフェースについて。
	 * 抽象、具象問わずストアはsimple系インターフェースを備える場合がある。
	 * simple系インターフェースはstaticで、一行でDBからオブジェクトを取得できる。
	 * 抽象ストアが扱っているインデックスで取得する場合、
	 * 抽象ストアのsimple系インターフェースが使えるが、
	 * 呼び出し方が面倒なので、UserStoreなどよく使うストアでは類似したインターフェースが
	 * 実装してあり、さらに簡易に呼び出せる。
	 *
	 * @param getStoreFunc
	 * @param f
	 * @return
	 */
	/*
	protected static <S extends IdObjectStore<T1, T2>,
			T1 extends IdObjectDBI,
			T2 extends T1,
			R> R simple(Function<Transaction, S> getStoreFunc,
					Function<S, R> f) {
		return IdObjectStore.<
				R> simpleReadAccess((txn) -> f.apply(getStoreFunc.apply(txn)));
	}
	*/

	/**
	 * TODO IdObjectは客観だけではないが客観DBパスを指定してしまっている。
	 * 実際、staticアクセスするのは客観系だけだから問題は起きていないはず。
	 *
	 * @param f
	 * @return
	 */
	/*
	protected static <R> R simpleAccess(StoreFunction<Transaction, R> f) {
		return simpleAccess(Glb.getFile().getObjectivityDBPath(), f);
	}

	protected static <R> R simpleReadAccess(StoreFunction<Transaction, R> f) {
		//ここでgetObjectivityDBPath()に依存しているせいで
		//IdObject系は客観である事が前提になっている。
		return simpleReadAccess(Glb.getFile().getObjectivityDBPath(), f);
	}
	*/

	/**
	 * IdObjectStoreであるなら、HashStoreの管理対象であり、
	 * create,update,deleteに伴いハッシュツリーの更新が行われる。
	 */
	protected final HashStore hStore;

	/**
	 * 削除されたIDを記録しリサイクルするためのストア
	 */
	protected final RecycleIdStore rStore;

	/**
	 * 更新されたIDを記録し同調処理を補佐するためのストア
	 */
	protected final CatchUpUpdatedIDListStore uStore;

	protected IdObjectStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
		//IdObject系の抽象クラスであるここでgetName()をHashStoreに通知している事は、
		//HashStore上で扱われるストア名が全てgetName()でえられるものと同じである事を意味する。
		//getName()を必ずストア名と同じ文字列を返すようにすれば、
		//DB上のストア名一覧とHashStore上のストア名一覧は一致する。
		//その前提は達成されなければならない。
		this.rStore = new RecycleIdStore(getName(), txn);
		this.hStore = new HashStore(getName(), txn);
		this.uStore = new CatchUpUpdatedIDListStore(getName(), txn);
	}

	/**
	 * 同調処理時に呼ぶ。専用の検証処理等が用いられる。
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public final boolean catchUp(T1 o) throws Exception {
		if (o == null) {
			Glb.debug("o is null ");
			return false;
		}

		ValidationResult r = new ValidationResult();
		if (!o.validateAtCatchUp()) {
			Glb.getLogger().error("", r.toString());
			return false;
		}
		if (get(o.getRecycleId()) == null) {
			if (createSpecifiedIdCatchUp(o) != null) {
				Glb.debug("catchup createSpecifiedIdCatchUp");
				return true;
			} else {
				Glb.getLogger().error("", new Exception(""));
				return false;
			}
		} else {
			if (updateAtCatchUp(o)) {
				Glb.debug("catchup updateAtCatchUp");
				return true;
			} else {
				Glb.getLogger().error("", new Exception(""));
				return false;
			}
		}
	}

	@Override
	protected Long cnvKey(ByteIterable bi) {
		return cnvL(bi);
	}

	@Override
	protected ByteIterable cnvKey(Long key) {
		return cnvL(key);
	}

	/**
	 * Longの連番IDを持つオブジェクトを作成する
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long create(T1 created) throws Exception {
		if (created == null)
			return null;

		ValidationResult vr = new ValidationResult();
		created.validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return null;
		}

		created.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return null;
		}

		return createAfterValidate(created);
	}

	private final Long createAfterValidate(T1 created) throws Exception {
		Long recycleId = null;
		if (created.getRecycleId() == null) {
			//id設定
			Long id = null;

			//リサイクルIDの取得を試みる
			recycleId = rStore.getFirst();
			if (recycleId == null) {
				id = getNextId();
			} else {
				id = recycleId;
			}

			if (id == null)
				return null;

			created.setRecycleId(id);
		}

		Long r = createInternal(created);

		if (r == null)
			return null;

		//リサイクルIDを使用したか
		if (recycleId != null) {
			//使われたIDを削除
			if (!rStore.delete(recycleId))
				throw new Exception();
			//リサイクルIDが使われた場合、作成処理でも更新されたID一覧に加える
			if (!uStore.writeUpdated(recycleId))
				Glb.getLogger().warn("", new Exception());
		}

		return r;
	}

	//子クラスの実装をフレームワーク的にする抽象メソッド一覧
	/**
	 * 作成処理。インデックス部分について子クラスで作成
	 *
	 * @param o
	 * @return
	 * @throws Exception
	 */
	protected abstract boolean createIdObjectConcrete(T1 o) throws Exception;

	private final Long createInternal(T1 created) throws Exception {
		ByteIterable cnvId = cnvKey(created.getRecycleId());

		//重複確認
		ValidationResult vr = new ValidationResult();
		if (!noExist(created, vr)) {
			Glb.debug("Failed to noExist. " + vr);
			return null;
		}

		//子クラス毎の整合性チェック及びインデックス等の書き込み
		if (!createIdObjectConcrete(created)) {
			Glb.debug("Failed to createSub");
			return null;
		}

		//書き込み
		ByteIterable createdBi = cnvO(created);
		if (!putDirect(cnvId, createdBi))
			throw new Exception(
					"IdObjectStore:" + created.getClass().getSimpleName());

		//HashStore更新
		if (!hStore.created(created.getRecycleId(), createdBi))
			throw new Exception("HashStore");
		//作成されたレコードのidを返す
		return cnvKey(cnvId);

	}

	/**
	 * IDを外部で指定するタイプの作成処理
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long createSpecifiedId(T1 created) throws Exception {
		return createSpecifiedIdCommon(created, true);
	}

	private final Long createSpecifiedIdCatchUp(T1 created) throws Exception {
		return createSpecifiedIdCommon(created, false);
	}

	private final Long createSpecifiedIdCommon(T1 created, boolean validateRef)
			throws Exception {
		ValidationResult vr = new ValidationResult();
		created.validateAtCreateSpecifiedId(vr);
		if (created == null || !vr.isNoError()) {
			Glb.debug(vr.toString());
			return null;
		}

		if (validateRef) {
			created.validateReference(vr, util.getTxn());
			if (!vr.isNoError()) {
				Glb.debug(vr.toString());
				return null;
			}
		}

		//IDが飛んだところは削除扱いでリサイクルIDを書き込む
		Long lastId = getLastId();
		for (long middleId = lastId + 1; middleId < created
				.getRecycleId(); middleId++) {
			if (rStore.get(middleId) == null) {
				rStore.create(middleId);
			}
		}

		return createInternal(created);
	}

	/**
	 * 参照の検証処理を省いて作成処理をする
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long createWithoutValidateReference(T1 created)
			throws Exception {
		if (created == null)
			return null;

		ValidationResult vr = new ValidationResult();
		created.validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return null;
		}

		return createAfterValidate(created);
	}

	/**
	 * unrecycleIdは典型的には、Userのユーザー名、WebのURLなど、一意な文字列
	 * @return	unrecycleId:objectのストア
	 */
	//	protected abstract StoreInfo getUnrecycleIdStoreInfo();

	/**
	 * オブジェクト更新時に
	 * 更新されたサブインデックスのフィールドについて制約違反が無いか調べる
	 * 変更された部分限定のnoExist()
	 * 重複キーが変更される場合、重複キーとRecycleIDの組み合わせで一意になるなら、
	 * チェックする必要が無い。
	 *
	 * @param updated	更新されたオブジェクト
	 * @param r		検証結果
	 * @return	更新可能か
	 */
	public boolean dbValidateAtUpdate(T1 updated, T1 old, ValidationResult r) {
		boolean b = true;
		//書く必要ない。責任外のコード
		if (updated.getRecycleId() == null) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_EMPTY);
			b = false;
			return b;
		}

		if (!dbValidateAtUpdateIdObjectConcrete(updated, old, r)) {
			b = false;
		}
		return b && r.isNoError();
	}

	public ValidationResult dbValidateAtUpdate2(T1 updated, T1 old,
			ValidationResult r) {
		dbValidateAtUpdate(updated, old, r);
		return r;
	}

	abstract protected boolean dbValidateAtUpdateIdObjectConcrete(T1 updated,
			T1 old, ValidationResult r);

	public final boolean delete(List<Long> ids) throws Exception {
		for (Long id : ids) {
			if (!delete(id)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 通常こちらの削除メソッドを使う。
	 * 削除されたIDが記録されリサイクルされる
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public final boolean delete(Long id) throws Exception {
		return delete(get(id));
	}

	public final boolean delete(T1 o) throws Exception {
		if (deleteWithoutRecycle(o)) {
			//削除されたリサイクルIDの記録
			Long deletedId = o.getRecycleId();

			if (!rStore.create(deletedId))
				throw new Exception("Failed to create deletedId");

			return true;
		} else {
			return false;
		}
	}

	protected abstract boolean deleteIdObjectConcrete(T1 o) throws Exception;

	/**
	 * リサイクルIDの作成を伴わずに削除する
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public final boolean deleteWithoutRecycle(Long id) throws Exception {
		return deleteWithoutRecycle(get(id));
	}

	/**
	 * オブジェクトを削除する。
	 * このメソッドを直接呼び出した場合、削除されるIDが記録されず、リサイクルされない。
	 * @param deleted
	 * @return
	 * @throws Exception
	 */
	public final boolean deleteWithoutRecycle(T1 deleted) throws Exception {
		ValidationResult vr = new ValidationResult();
		deleted.validateAtDelete(vr);
		if (deleted == null || !vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!exist(deleted, vr)) {
			Glb.debug(vr.toString());
			return false;
		}

		boolean r1 = deleteDirect(cnvKey(deleted.getRecycleId()));
		boolean r2 = hStore.removed(deleted.getRecycleId());
		if (!r1 || !r2)
			throw new IOException();

		return deleteIdObjectConcrete(deleted);
	}

	/**
	 * dbValidateAtDelete的なメソッド
	 *
	 * インデックス等含めて完全にある。
	 * 重複キーのインデックスはチェックされない
	 * このメソッドを呼ぶ前にモデルクラスの検証メソッドによって
	 * ヌルチェック等が終わっている前提
	 */
	public final boolean exist(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getRecycleId() == null) {
				vr.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getRecycleId()) == null) {
					vr.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_DB_NOTFOUND,
							Lang.IDOBJECT_RECYCLE_ID + "=" + o.getRecycleId());
					b = false;
				}
				if (!existIdObjectConcrete(o, vr)) {
					b = false;
				}
			}
		}
		return b && vr.isNoError();
	}

	public final ValidationResult exist2(T1 o, ValidationResult vr)
			throws Exception {
		exist(o, vr);
		return vr;
	}

	/**
	 * oの全てのサブインデックスについて、
	 * 一意であるなどのDB上の制約を満たし、
	 * DBに正常に存在しているか。
	 *
	 * existとnoExistは名前からすると互いに相手の結果を反転すれば
	 * 良いように思えるが、そうではない。
	 * オブジェクトがユニーク属性を複数持っていた場合、
	 * noExistは各ユニーク属性についてまだ存在しない事を確認する。
	 * existは各ユニーク属性について既に存在する事を確認する。
	 * もしDBに例えばユニーク属性のうち１つだけ既に存在する場合、
	 * noExistはfalseであり、existもfalseである。
	 *
	 * GUIにおいてDBとの整合性を検証するとき、
	 * まだIDが割り振られていないのでexist()を呼べないので、
	 * existSub()を使う。
	 *
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public abstract boolean existIdObjectConcrete(T1 o, ValidationResult r) throws Exception;

	public final CatchUpUpdatedIDListStore getCatchUpUpdatedIDListStore() {
		return uStore;
	}

	public final HashStore getHashStore() {
		return hStore;
	}

	/**
	 * @param s
	 * @param key
	 * @return		見つからなければnull
	 */
	public final Long getId(StoreInfo s, ByteIterable key) {
		if (s == null || key == null)
			return null;
		ByteIterable bi = util.get(s, key);
		if (bi == null)
			return null;
		return cnvKey(bi);
	}

	/**
	 * @return	最後のID
	 */
	public final Long getLastId() {
		//過去存在したIDでも最後のIDとして返す可能性があるので留意する必要がある
		Store s = getStore();
		try (Cursor c = s.openCursor(util.getTxn())) {
			if (!c.getLast())
				return IdObjectDBI.getFirstRecycleId() - 1;
			if (ByteIterable.EMPTY.equals(c.getKey())) {
				return IdObjectDBI.getFirstRecycleId() - 1;
			}
			return cnvKey(c.getKey());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return IdObjectDBI.getFirstRecycleId() - 1;
		}
	}

	@Override
	public final StoreInfo getMainStoreInfo() {
		return getMainStoreInfoStatic(getName());
	}

	/**
	 * シングルスレッドでしか書き込まれない想定
	 * @return	最後のID+1
	 */
	protected final Long getNextId() {
		return getLastId() + 1;
	}

	/**
	 * ランダムにいくつかオブジェクトを取得する
	 * @param max	最大取得件数
	 * @return		max件以下のオブジェクト
	 * @throws IOException
	 */
	public List<T2> getRandom(int max) throws Exception {
		List<T2> r = new ArrayList<>();

		for (int i = 0; i < max; i++) {
			Long id = ThreadLocalRandom.current().nextLong(count());
			if (rStore.get(id) != null) {
				continue;
			}
			r.add(get(id));
		}

		return r;
	}

	/**
	 * 通常DBからのgetはchainversionupしなければならないが
	 * このメソッドはしてはいけない。hashによる同調処理で使われる。
	 * 同調処理はDBの実状態を同調させるのであってversionupさせると
	 * ハッシュが一致しない。
	 * @param id
	 * @return
	 */
	public final byte[] getRawVal(Long id) {
		return cnvBA(util.get(getMainStoreInfo(), cnvKey(id)));
	}

	public final RecycleIdStore getRecycleIdStore() {
		return rStore;
	}

	abstract public List<StoreInfo> getStoresIdObjectConcrete();

	@Override
	public List<StoreInfo> getStoresModelStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.addAll(getStoresIdObjectConcrete());
		return r;
	}

	/**
	 * @param id
	 * @return		idは最後のIDか
	 */
	public final boolean isLast(Long id) {
		Long lastId = getLastId();
		if (lastId == null)
			return false;
		return lastId.equals(id);
	}

	/**
	 * idでオブジェクトを取得
	 * @param id
	 * @return
	 */
	/*
	public final T2 get(Long id) {
		return getMain(cnvKey(id));
	}
*/
	/**
	 * ストア毎のクラスでinstanceofを行う。
	 * instanceofはDBI系インターフェースかクラスの完全修飾名で行う。
	 * クラス名を指定する場合、
	 * クラスがアップデートされ新しいバージョンのクラス定義が追加されたら
	 * instanceofの行を追記していく必要がある。
	 *
	 * ストアには同調処理を通じて古いバージョンのオブジェクトが追加される可能性があるので、
	 * 最新版だけを許可するだけでは足りない。
	 * このストア内に存在していていいクラスなら全て許可する必要がある。
	 *
	 * @param o		判定対象
	 * @return		oはこのストアに格納していいオブジェクトか
	 */
	public abstract boolean isSupport(Object o);

	/**
	 * dbValidateAtCreate的なメソッド
	 *
	 * インデックス等含めて完全に無い
	 * このメソッドを呼ぶ前にモデルクラスの検証メソッドによって
	 * ヌルチェック等が終わっている前提
	 * @throws Exception
	 */
	public final boolean noExist(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getRecycleId() == null) {//未登録
				vr.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getRecycleId()) != null) {
					vr.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_DB_NOTFOUND);
					b = false;
				}
				if (!noExistIdObjectConcrete(o, vr)) {
					b = false;
				}
			}
		}

		return b && vr.isNoError();
	}

	public final ValidationResult noExist2(T1 o, ValidationResult vr)
			throws Exception {
		noExist(o, vr);
		return vr;
	}

	/**
	 * oの全てのユニーク属性について、DBから一つも見つからないか。
	 * id以外は子クラスで対応。
	 * その他、oは既存DBに追加するとして整合性違反が生じないか。
	 *
	 * GUIにおいてDBとの整合性を検証するとき、
	 * まだIDが割り振られていないのでnoExist()を呼べないので、
	 * noExistSub()を使う。
	 *
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public abstract boolean noExistIdObjectConcrete(T1 o, ValidationResult r)
			throws Exception;

	/**
	 * オブジェクトを更新する
	 * @param updated
	 * @return
	 * @throws Exception
	 */
	public final boolean update(T1 updated) throws Exception {
		if (updated == null) {
			Glb.debug("updated is null ");
			return false;
		}
		ValidationResult vr = new ValidationResult();
		T1 old = get(updated.getRecycleId());
		if (old == null)
			return false;
		updated.validateAtUpdate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!validateAtUpdateChange(updated, vr)) {
			Glb.debug(vr.toString());
			return false;
		}

		updated.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		return updateAfterValidate(updated);
	}

	private final boolean updateAfterValidate(T1 updated) throws Exception {
		//これは失敗が致命的ではない
		if (!uStore.writeUpdated(updated.getRecycleId()))
			Glb.getLogger().warn("", new Exception());

		return updateInternal(updated, null);
	}

	/**
	 * 参照検証等を省いて更新処理をする。
	 *
	 * dbValidateAtUpdateは省けない。
	 * {@link Objectivity#applySparseObjectList(String, List)}に書いた通り
	 * その検証のせいでfalseになったとしても繰り返し同調処理をすることで解決される。
	 *
	 * @param updated
	 * @return
	 * @throws Exception
	 */
	private final boolean updateAtCatchUp(T1 updated) throws Exception {
		ValidationResult vr = new ValidationResult();

		T1 old = get(updated.getRecycleId());
		if (old == null) {
			vr.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_DB_NOTFOUND);
			return false;
		}

		//この検証処理は省けない。
		if (!dbValidateAtUpdate(updated, old, vr)) {
			Glb.debug(vr.toString());
			return false;
		}
		return updateInternal(updated, old);
	}

	/**
	 * インデックスを子クラスで更新する
	 * @param updated		更新された後の新しいオブジェクト
	 * @param old 			更新される前の古いオブジェクト
	 * @return				必要な更新作業ができたか
	 * @throws Exception
	 */
	protected abstract boolean updateIdObjectConcrete(T1 updated, T1 old) throws Exception;

	private final boolean updateInternal(T1 updated, T1 old) throws Exception {
		if (old == null) {
			old = get(updated.getRecycleId());
		}
		if (old == null) {
			throw new IOException();
		}

		ByteIterable updatedBi = cnvO(updated);
		if (!putDirect(cnvKey(updated.getRecycleId()), updatedBi))
			throw new IOException();

		if (!hStore.updated(updated.getRecycleId(), updatedBi))
			throw new IOException();

		return updateIdObjectConcrete(updated, old);
	}

	public ValidationResult validateAtUpdate2(T1 updated, ValidationResult r) {
		validateAtUpdateChange(updated, r);
		return r;
	}

	/**
	 * 同じIDのオブジェクトの現在の状態から
	 * 更新された状態への遷移について検証する。
	 * この検証処理はオブジェクト内部の状態についての検証と
	 * DBにおける整合性の検証の2種類の検証処理が呼び出される。
	 *
	 * @param updated
	 * @param r
	 * @return
	 */
	public boolean validateAtUpdateChange(T1 updated, ValidationResult r) {
		T2 old = get(updated.getRecycleId());
		if (old == null) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_DB_NOTFOUND);
			return false;
		}

		if (!updated.validateAtUpdateChange(r, old)) {
			return false;
		}

		if (!dbValidateAtUpdate(updated, old, r)) {
			return false;
		}

		return r.isNoError();
	}

	public static interface StoreFunction<T, R> {
		R apply(T t) throws Exception;
	}

}
