package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import io.netty.util.internal.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * T1:サブインデックスのためのインターフェース。変更されない
 * T2:最新版のクラスを子クラスで設定する。変更される
 *
 * オブジェクトをシリアライズし、Long id : byte[] serializedObjectのkvsを必ず持つ。
 * そのkvsをメインストアと呼ぶ。
 * 加えて、サブインデックスフィールド : idのサブインデックスのストアをいくつか持つ。
 * 例えばname : idなど
 *
 * ここにあるDBの思想はやや特殊なので説明する。
 * まずP2Pアプリということでスキーマエボリューション問題が深刻で、
 * エンドユーザーのPCで長時間のスキーマ更新はできない。
 *
 * そこでchainversionupというアイデアによってメインストアは
 * スキーマエボリューションの問題から逃れる。
 *
 * 残る問題はインデックスストアである。
 * インデックスストアのスキーマエボリューションの問題を解決する重要なアイデアは、
 * ”複雑なクエリは人間に凝ったデータを見せるためで内部処理は単純な検索しかない”
 * という信念である。つまりプログラム内部の動作だけを考えれば
 * 複雑なクエリに対応する必要が無く、
 * サブインデックスは限られ、スキーマ更新が将来に渡ってないと
 * 考えられる属性のみに対応するなら、問題は解決される。
 * つまりサブインデックスに用いられているフィールドの構造変更は将来に渡ってない。
 * 内部処理のための最小限のサブインデックスのみを持つからそれが可能と判断した。
 *
 * その将来に渡って変更されないと信じられるサブインデックスに関するインターフェースは
 * DBI系インターフェースに定義される。
 *
 * そして本アプリはKVSによる単純な検索機能を内蔵する。
 * しかし本アプリは人間が使うのだから、複雑なクエリをして凝ったデータを
 * 見たい人も居るかもしれない。
 * その場合、外部アプリで本アプリのDBのデータを吸い出し、より高機能なDBの元で検索する。
 * だから内臓の検索機能は単純で済む。
 *
 * そして、”外部アプリで複雑なクエリをして操作対象のデータを特定し、
 * 基盤ソフトウェア上で操作する”。ということが考えられる。
 *
 * つまりこの3種類の動作が可能。
 * - 内部動作のための単純な検索
 * - 人間が手動で発行する複雑なクエリでデータを見る（外部アプリ）
 * - 人間が手動で発行する複雑なクエリで特定したデータに本アプリ上で操作を加える（外部アプリと本アプリの連携）
 *
 * そしてこれらだけで一般的な検索需要を満たしていると考える。
 * つまり複雑なクエリは必ず人間が手動で発行するとみなして問題無いだろうという信念によって
 * 複雑な検索機能を内蔵する必要が無いと断じている。
 *
 * つまり本アプリは内部動作のために僅かなインデックスを用意し、
 * ユーザーのためのインデックスを用意せず、
 * 高機能な検索は外部アプリのグラフDBに委譲される。
 *
 * 以上のアイデアで、モデルクラスのバージョンアップをしつつ、
 * P2Pソフトウェアでスキーマエボリューションを解決しつつ、
 * 高機能な検索需要にも対応する。
 *
 * 各具象ストアは1つ対象とするモデルクラスを持つ。
 * 例えばUserStoreはUserというモデルクラスを対象とする。
 * 各具象ストアは対象とするモデルクラスのgetClass()#getSimpleName()をnameフィールドの値とする。
 * SimpleNameを使うからモデルクラスの名前はバージョンアップの中で変更不可。
 * nameをpublicにする。
 * nameをgetName()の返値にする。
 *
 * 重複キーのストアは名前に_Dupを末尾につける
 *
 * getId系メソッドやLong idのIDはIdObjectのIdを意味する
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
 * MVCCトランザクションの性質に留意する必要がある。
 * トランザクションAで書き込み操作をした場合、トランザクションAを通じて
 * DB操作している場合書き込んだデータがすぐに見えるが、
 * コミットしない限り他のトランザクションでDB操作をしているとそのデータは見えない。
 *
 * リサイクルHIDは削除されたHIDが記録され次に作成されるオブジェクトに
 * 再度割り振られるという概念。
 * ダミーデータを大量に作られてしまった場合に削除する事になるが、
 * その時ハッシュツリーが無駄に肥大化する事を防ぐための仕様。
 * リサイクルHIDによって空き番となった位置が再利用される。
 * なお再利用されてもオブジェクトのIDとHIDは異なるので
 * エンドユーザーが見る事になるオブジェクトのIDは常に新しいものになる。（非再利用）
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class IdObjectStore<T1 extends IdObjectDBI, T2 extends T1>
		extends ModelStore<Long, T1, T2> {
	public static final StoreInfo getHidStoreStatic(String modelName) {
		return new StoreInfo(modelName + "_hidToId");
	}

	public static StoreInfo getMainStoreInfoStatic(String modelName) {
		return new StoreInfo(modelName + "_idTo" + modelName);
	}

	private final StoreInfo idToModel;
	private final StoreInfo hidToId;

	/**
	 * IdObjectStoreであるなら、HashStoreの管理対象であり、
	 * create,update,deleteに伴いハッシュツリーの更新が行われる。
	 */
	protected final HashStore hStore;

	/**
	 * 更新されたIDを記録し同調処理を補佐するためのストア
	 *
	 * IDではなくHIDを管理すべきか設計判断に迷ったが
	 * オブジェクトを取得する処理は
	 * サブインデックスを通じてHID→IDに変換してID→オブジェクト取得
	 * という手順になるので一手間増える事からID管理とした。
	 * このせいで同調処理のコードはIDとHID両方を扱う事になるのでやや複雑になる。
	 */
	protected final CatchUpUpdatedIDListStore uStore;

	protected IdObjectStore(Transaction txn) {
		super(txn);
		idToModel = getMainStoreInfoStatic(getName());
		hidToId = getHidStoreStatic(getName());
		//IdObject系の抽象クラスであるここでgetName()をHashStoreに通知している事は、
		//HashStore上で扱われるストア名が全てgetName()でえられるものと同じである事を意味する。
		//getName()を必ずストア名と同じ文字列を返すようにすれば、
		//DB上のストア名一覧とHashStore上のストア名一覧は一致する。
		//その前提は達成されなければならない。
		if (needCatchUp()) {
			this.hStore = new HashStore(getName(), txn);
			this.uStore = new CatchUpUpdatedIDListStore(getName(), txn);
		} else {
			this.hStore = null;
			this.uStore = null;
		}
	}

	/**
	 * 同調処理時に呼ぶ。専用の検証処理等が用いられる。
	 * 更新または作成される。
	 *
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
		if (get(o.getId()) == null) {
			if (createCatchUp(o) != null) {
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
	 * 同調処理用のオブジェクト記録インターフェース
	 * 一部検証処理が行われない
	 *
	 * @param created
	 * @return
	 * @throws Exception
	 */
	private final Long createCatchUp(T1 created) throws Exception {
		created.setCatchUp(true);
		created.setSpecifiedId(true);
		return create(created);
	}

	protected abstract boolean createIdObjectConcrete(T1 o) throws Exception;

	@Override
	protected boolean createModelConcrete(ByteIterable createdBi, T1 created)
			throws Exception {
		//子孫クラスサブインデックス作成
		if (!createIdObjectConcrete(created))
			throw new IOException("Failed to create sub indexes");
		if (needCatchUp()) {
			//HashStore更新
			if (!hStore.created(created.getHid(), createdBi))
				throw new Exception("HashStore");

			//IdObjectのサブインデックスの書き込み
			if (!util.put(getHidStore(), cnvL(created.getHid()),
					cnvL(created.getId()))) {
				throw new IOException(
						"Failed to put in HidStore. created=" + created);
			}
		}
		return true;
	}

	/**
	 * IDとHIDを外部で指定するタイプの作成処理
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long createSpecifiedId(T1 created) throws Exception {
		created.setSpecifiedId(true);
		return create(created);
	}

	abstract protected boolean dbValidateAtUpdateIdObjectConcrete(T1 updated,
			T1 old, ValidationResult r);

	protected boolean dbValidateAtUpdateModelConcrete(T1 updated, T1 old,
			ValidationResult r) {
		boolean b = true;
		//メインインデックス検証
		if (updated.getId() == null) {
			r.add(Lang.IDOBJECT_ID, Lang.ERROR_EMPTY);
			b = false;
			return b;
		}

		//サブインデックス検証
		if (needCatchUp()) {
			if (updated.getHid() == null) {
				r.add(Lang.IDOBJECT_HID, Lang.ERROR_EMPTY);
				b = false;
				return b;
			}
		}

		//具象クラスのサブインデックス検証
		if (!dbValidateAtUpdateIdObjectConcrete(updated, old, r)) {
			b = false;
		}
		return b && r.isNoError();
	}

	/**
	 * 同調処理を通じてモデルをDBから削除する場合に使う。
	 * リサイクルHIDの作成を伴わずに削除する
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public final boolean deleteCatchUp(Long id) throws Exception {
		T1 o = get(id);
		if (o == null)
			return false;
		o.setCatchUp(true);
		return delete(o);
	}

	protected abstract boolean deleteIdObjectConcrete(T1 o) throws Exception;

	@Override
	protected boolean deleteModelConcrete(T1 deleted) throws Exception {
		if (needCatchUp()) {
			boolean recycle = !deleted.isCatchUp();
			if (!hStore.removed(deleted.getHid(), recycle)) {
				throw new IOException("Failed to hStore.removed(). recycle="
						+ recycle + " deleted=" + deleted);
			}

			//IdObjectのサブインデックスの削除
			if (!util.remove(getHidStore(), cnvL(deleted.getHid()))) {
				throw new IOException(
						"Failed to remove in HidStore. deleted=" + deleted);
			}
		}
		return deleteIdObjectConcrete(deleted);
	}

	protected abstract boolean existIdObjectConcrete(T1 o, ValidationResult r)
			throws Exception;

	protected final boolean existModelConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getId() == null) {
				vr.add(Lang.IDOBJECT_ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getId()) == null) {
					vr.add(Lang.IDOBJECT_ID, Lang.ERROR_DB_NOTFOUND,
							Lang.IDOBJECT_ID + "=" + o.getId());
					b = false;
				}
			}

			if (needCatchUp()) {
				if (o.getHid() == null) {
					vr.add(Lang.IDOBJECT_HID, Lang.ERROR_EMPTY);
					b = false;
				} else {
					if (getByHid(o.getHid()) == null) {
						vr.add(Lang.IDOBJECT_HID, Lang.ERROR_DB_NOTFOUND,
								Lang.IDOBJECT_HID + "=" + o.getHid());
						b = false;
					}
				}
			}

			if (!existIdObjectConcrete(o, vr)) {
				b = false;
			}
		}
		return b && vr.isNoError();
	}

	/**
	 * hidからidを特定し、idに対応するオブジェクトを返す
	 * @param hid
	 * @return	オブジェクト
	 */
	public T2 getByHid(Long hid) {
		if (!needCatchUp())
			return null;
		return get(getIdByHid(hid));
	}

	public final CatchUpUpdatedIDListStore getCatchUpUpdatedIDListStore() {
		return uStore;
	}

	public final HashStore getHashStore() {
		if (!needCatchUp())
			Glb.getLogger().warn(
					"getHashStore() is called in store of needCatchUp() == false",
					new Exception());
		return hStore;
	}

	public final StoreInfo getHidStore() {
		return hidToId;
	}

	/**
	 * 値がKと同じ型の場合に使用できる。
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

	public Long getIdByHid(Long hid) {
		if (!needCatchUp())
			return null;
		return getId(getHidStore(), cnvL(hid));
	}

	/**
	 * @return	最後のID
	 */
	public final Long getLastId() {
		Store s = getStore();
		try (Cursor c = s.openCursor(util.getTxn())) {
			if (!c.getLast())
				return IdObjectDBI.getFirstId() - 1;
			if (ByteIterable.EMPTY.equals(c.getKey())) {
				return IdObjectDBI.getFirstId() - 1;
			}
			return cnvKey(c.getKey());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return IdObjectDBI.getFirstId() - 1;
		}
	}

	@Override
	public Long getMainKey(T1 o) {
		return o.getId();
	}

	@Override
	public final StoreInfo getMainStoreInfo() {
		return idToModel;
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

		long c = count();
		for (int i = 0; i < max; i++) {
			Long id = ThreadLocalRandom.current().nextLong(c);
			T2 o = get(id);
			if (o == null)
				continue;

			if (needCatchUp()) {
				//もしリサイクルされていたら(その場合削除済みでここに来ないはずだが)
				if (getRecycleHidStore().get(o.getHid()) != null) {
					continue;
				}
			}
			r.add(o);
		}

		return r;
	}

	public T1 getRawObj(Long id) {
		try {
			Object o = cnvO(util.get(getMainStoreInfo(), cnvKey(id)));
			if (o == null || !(o instanceof IdObjectDBI))
				return null;
			return (T1) o;
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * hidからidを特定し、idに対応するオブジェクトをchainversionupせずに返す
	 * @param hid
	 * @return	chainversionupされていないDB上のそのままのオブジェクト
	 */
	public T1 getRawObjByHid(Long hid) {
		if (!needCatchUp())
			return null;
		Object o = getRawObj(getIdByHid(hid));
		if (o == null || !(o instanceof IdObjectDBI))
			return null;
		return (T1) o;
	}

	public final RecycleHidStore getRecycleHidStore() {
		if (!needCatchUp())
			return null;
		return hStore.getRecycleHIDStore();
	}

	abstract protected List<StoreInfo> getStoresIdObjectConcrete();

	@Override
	protected List<StoreInfo> getStoresObjectStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		if (needCatchUp())
			r.add(getHidStore());
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
	 * @return	同調処理が行われるストアか
	 * ただし客観コアストアを除く。同調対象だがH木など必要無いので。
	 */
	protected boolean needCatchUp() {
		return true;
	}

	protected abstract boolean noExistIdObjectConcrete(T1 o, ValidationResult r)
			throws Exception;

	@Override
	protected final boolean noExistModelConcrete(T1 o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getId() == null) {//未登録
				vr.add(Lang.IDOBJECT_ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getId()) != null) {
					vr.add(Lang.IDOBJECT_ID, Lang.ERROR_DB_EXIST);
					b = false;
				}
			}

			if (needCatchUp()) {
				if (o.getHid() == null) {
					vr.add(Lang.IDOBJECT_HID, Lang.ERROR_EMPTY);
					b = false;
				} else {
					Long idByHid = getIdByHid(o.getHid());
					if (idByHid != null) {
						vr.add(Lang.IDOBJECT_HID, Lang.ERROR_DB_EXIST);
						b = false;
					}
				}
			}

			if (!noExistIdObjectConcrete(o, vr)) {
				b = false;
			}
		}

		return b && vr.isNoError();
	}

	@Override
	protected void procAfterCreate(ByteIterable createdBi, T1 created)
			throws IOException {
		if (created.getId() == null || created.getHid() == null)
			throw new IOException("Failed to create");
		if (created.isRecycleHid()) {
			if (!hStore.removeRecycleHid(created.getHid()))
				throw new IOException("Failed to removeRecycleHid()");
		}
	}

	@Override
	protected void procBeforeCreate(T1 created) throws Exception {
		//HID設定
		Long recycleHid = null;
		if (needCatchUp() && created.getHid() == null) {
			if (created.getId() == null) {
				recycleHid = hStore.getRecycleHid();
				if (recycleHid == null) {
					Long hid = hStore.getNextHid();
					if (hid == null)
						throw new Exception(
								"Failed to getNextHid(). created=" + created);
					created.setHid(hid);
				} else {
					created.setHid(recycleHid);
					created.setRecycleHid(true);
				}
			}
		}
		//最後のID+1である保証があるならtrue
		boolean last = false;
		if (created.getId() == null) {
			//ID設定
			Long id = getNextId();
			if (id == null)
				throw new Exception("Failed to create id. created=" + created);
			created.setId(id);
			last = true;
		}

		created.setLastKey(last);
	}

	@Override
	protected void procBeforeUpdate(T1 updated, T1 old) throws Exception {
		super.procBeforeUpdate(updated, old);
		if (needCatchUp()) {
			//これは失敗が致命的ではない
			if (!uStore.writeUpdated(updated.getId()))
				Glb.getLogger().warn("", new Exception());
		}
	}

	/**
	 * データの内容やDBの状況に応じてoを永続化する
	 * @param o
	 * @return	記録されたか
	 */
	public boolean save(T1 o) {
		try {
			if (o.getId() == null) {
				//IDが無い場合、新規作成するしかない
				if (create(o) == null)
					throw new Exception("Failed to create o=" + o);
			} else {
				if (get(o.getId()) == null) {
					//IDがあり、DBにない場合
					if (createSpecifiedId(o) == null)
						throw new Exception(
								"Failed to createSpecifiedId o=" + o);
				} else {
					//IDがあり、DBにある場合
					if (!update(o))
						throw new Exception("Failed to update o=" + o);
				}
			}
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}
	/*
		private final boolean updateAfterValidate(T1 updated) throws Exception {
			if (needCatchUp()) {
				//これは失敗が致命的ではない
				if (!uStore.writeUpdated(updated.getId()))
					Glb.getLogger().warn("", new Exception());
			}
			return updateInternal(updated, null);
		}
	*/

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
		updated.setCatchUp(true);
		updated.setSpecifiedId(true);
		return update(updated);
	}

	/**
	 * インデックスを子クラスで更新する
	 * @param updated		更新された後の新しいオブジェクト
	 * @param old 			更新される前の古いオブジェクト
	 * @return				必要な更新作業ができたか
	 * @throws Exception
	 */
	protected abstract boolean updateIdObjectConcrete(T1 updated, T1 old)
			throws Exception;

	@Override
	protected boolean updateModelConcrete(ByteIterable updatedBi, T1 updated,
			T1 old) throws Exception {
		if (needCatchUp()) {
			if (!hStore.updated(updated.getId(), updatedBi))
				throw new IOException();

			//IdObjectのサブインデックス更新
			if (Glb.getUtil().notEqual(updated.getHid(), old.getHid())) {
				if (old.getHid() != null) {
					if (!util.remove(getHidStore(), cnvL(old.getHid())))
						throw new IOException(
								"Failed to remove in HidStore. updated="
										+ updated);
				}
				if (!util.put(getHidStore(), cnvL(updated.getHid()),
						cnvL(updated.getId())))
					throw new IOException(
							"Failed to put in HidStore. updated=" + updated);
			}
		}
		if (!updateIdObjectConcrete(updated, old))
			throw new IOException();
		return true;
	}

	public static interface StoreFunction<T, R> {
		R apply(T t) throws Exception;
	}

}
