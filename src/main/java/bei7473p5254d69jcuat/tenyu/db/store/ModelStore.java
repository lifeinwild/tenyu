package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.DBUtil.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyutalk.*;
import glb.*;
import glb.util.*;
import io.netty.util.internal.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 参照：{@link ObjectStore}
 *
 * このクラスを継承した子クラスはそれぞれT1とT2を指定する必要がある。
 * T1:サブインデックス用インターフェースを定義したinterface。変更されない{@link UnversionableI}
 * T2:モデルクラス（T1実装）。変更される
 *
 * シリアライズされたオブジェクトをこのストアに登録できる。
 * ストアは必ずこのようなメインストアを持つ。
 * Long id : byte[] serializedObject
 *
 * 加えて、０個以上のサブインデックスのストアを持つ。
 * サブインデックスキー : id
 * サブインデックスキーは任意。
 * 例えば{@link IndividualityObjectI#getName()} : idなど。文字列も数値もそれ以外もありうる。
 * サブインデックスは、1:nのインデックス等いくつかのパターンがある。
 *
 * ここにあるDBの思想はやや特殊なので説明する。
 * まずP2Pアプリということでスキーマエボリューション問題が深刻で、
 * エンドユーザーのPCで長時間のスキーマ更新はできない。
 *
 * そこで{@link ChainVersionup}というアイデアによってメインストアは
 * スキーマエボリューションの問題から逃れる。
 *
 * 残る問題はインデックスストアである。
 * サブインデックスの仕様が変更された時、各ユーザーのPC上で
 * 変更されたサブインデックスのストアに格納された全データを一斉に書き換える等
 * スキーマエボリューションが生じるが、その処理時間はデータ量に比例して増大する。
 *
 * 以下の信念がサブインデックスストアのスキーマエボリューションの問題を解決する。
 *
 * ”一般に検索において、
 * 複雑なクエリは人間に凝ったデータを見せるために行われるので検索条件の入力のために必ず手動操作があり、
 * ソフトウェア内部の自動的な検索（設計時に洗い出せる）は単純な検索（完全一致、前方一致等）だけで提供できる”
 *
 * つまりプログラム内部の動作だけを考えれば複雑なクエリに対応する必要が無く、
 * サブインデックスの数やインデックス構造の複雑さは限定的になり、
 * スキーマ更新が将来に渡ってないと考えられるサブインデックスの仕様に到達できるので問題は解決される。
 * （＝モデル系インターフェースによる約束）
 * 内部処理のための最小限のサブインデックスのみを持つからそれが可能と判断した。
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
 * 他に、MVCCを利用してDB全体を一斉に書き換える事が考えられる。
 * この場合長時間のスキーマエボリューションをバックグラウンドで進めれるので、
 * この方向性によって解決する場合もありそうだが、
 * 変更されるインデックスが重要な内部処理に関わっていない場合に限定されるだろう。
 * もしプログラムが新しいインデックス構造を期待し、
 * しかしDBが古いインデックス構造のままだった場合、
 * 例外が発生するか検索結果が０件になるなどして不正な値を作成する事になる。
 * TenyuのP2Pネットワークにおいて不正値を主張するノードが大量発生する事になる。
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
 * getId系メソッドやLong idのIDは{@link Model}のIdを意味する
 *
 * ・全文検索を実装すべきか？
 * 実装するなら{@link ModelStore}のレベルになり、
 * {@link ModelI}はString keywords()を定義し、
 * keywords()において各具象クラスは自身の状態について連結した1つの文字列を出力し、
 * 何らかの形態素解析でキー一覧を得る事になるだろう。
 * 全文検索は内部動作で使う事は無く、GUIでユーザーが使うだけだから、
 * このプログラムの設計方針からすれば外部ツールに委譲すべき機能に思える。
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
 * コミットしない限り他のトランザクションからそのデータは見えない。
 *
 * リサイクルHIDは削除されたHIDが記録され次に作成されるオブジェクトに
 * 再度割り振られるという概念。
 * ダミーデータを大量に作られてしまった場合に削除する事になるが、
 * その時ハッシュツリーが無駄に肥大化する事を防ぐための仕様。
 * リサイクルHIDによって空き番となった位置が再利用される。
 * なお再利用されてもオブジェクトのIDとHIDは異なるので
 * エンドユーザーが見る事になるオブジェクトのIDは常に新しいものになる。（非再利用）
 *
 * historyIndex系はサブインデックスを作らない。
 * date系は作る。dateは客観系モデルでは{@link Objectivity#getGlobalCurrentTime()}
 * が全ノードで一致するので、ある程度同じ日時にいくつかのオブジェクトがインデックスされる。
 * ただし{@link Tenyutalk}系モデルは独自の日時を設定するのでばらばらになる。
 * ばらばらでも日時１～日時２の間の全IDを検索する事ができるので意味がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class ModelStore<T1 extends ModelI, T2 extends T1>
		extends ObjectStore<Long, T2> {
	public static final StoreInfo getCreateDateStoreStatic(String modelName) {
		return new StoreInfo(modelName + "_createDateToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	public static final StoreInfo getHidStoreStatic(String modelName) {
		return new StoreInfo(modelName + "_hidToId");
	}

	public static StoreInfo getMainStoreInfoStatic(String modelName) {
		return new StoreInfo(modelName + "_idTo" + modelName);
	}

	public static final StoreInfo getUpdateDateStoreStatic(String modelName) {
		//第三引数をtrueにする設計(updateDateが初期値ならインデックスしない)も考慮したが論理的一貫性が崩れ
		//バグが出やすいと判断した
		return new StoreInfo(modelName + "_updateDateToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	/**
	 * 作成日時からその日時に作成されたID一覧へ
	 */
	private final StoreInfo createDateToIds;

	/**
	 * 更新日時からその日時に更新されたID一覧へ
	 */
	private final StoreInfo updateDateToIds;

	private final StoreInfo idToModel;
	private final StoreInfo hidToId;

	/**
	 * {@link ModelStore}であるなら、HashStoreの管理対象であり、
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

	protected ModelStore(Transaction txn) {
		super(txn);
		idToModel = getMainStoreInfoStatic(getName());
		hidToId = getHidStoreStatic(getName());
		//Model系の抽象クラスであるここでgetName()をHashStoreに通知している事は、
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
		createDateToIds = getCreateDateStoreStatic(getName());
		updateDateToIds = getUpdateDateStoreStatic(getName());
	}

	/**
	 * @return	対象とするモデルが同じクラスを参照するか。
	 * {@link Tag#getTagIds()}など。
	 */
	public boolean isSelfReference() {
		return false;
	}

	/**
	 * 同調処理時に呼ぶ。同調処理専用の検証処理等が用いられる。
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
			Glb.getLogger().warn(o.toString() + r.toString(),
					new IllegalStateException());
			if (!o.isWarningValidation())
				return false;
		}
		T1 old = get(o.getId());
		if (old == null) {
			if (createCatchUp(o) != null) {
				Glb.debug("catchup createSpecifiedIdCatchUp");
				return true;
			} else {
				Glb.getLogger().error("", new Exception(""));
				return false;
			}
		} else {
			if (updateAtCatchUp(o, old)) {
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
	 * オブジェクトを作成する
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long create(T1 created) throws Exception {
		created.setupAtCreate(util.getTxn());
		return createInternal(created);
	}

	private final Long createInternal(T1 created) throws Exception {
		if (created == null)
			return null;

		//モデル側ロジックとしての検証前設定
		created.setupAtCreate(util.getTxn());

		ValidationResult vr = new ValidationResult();
		created.validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.getLogger().warn(created.toString() + vr.toString(),
					new IllegalStateException());
			if (!created.isWarningValidation())
				return null;
		}

		created.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.getLogger().warn(created.toString() + vr.toString(),
					new IllegalStateException());
			if (!created.isWarningValidation())
				return null;
		}

		procBeforeCreate(created);

		//登録されるオブジェクトのメインキー
		Long key = getMainKey(created);
		ByteIterable cnvId = cnvKey(key);

		//重複確認
		if (!noExist(created, vr)) {
			Glb.getLogger().warn(created.toString() + vr.toString(),
					new IllegalStateException());
			if (!created.isWarningValidation())
				return null;
		}

		ByteIterable createdBi = cnvO(created);
		//メインストア書き込み
		if (!putDirect(cnvId, createdBi, created.isLastKey()))
			throw new Exception(
					"Failed to putDirect in ModelStore. created=" + created);

		//時間系サブインデックス更新
		if (!util.put(getCreateDateToIds(), cnvL(created.getCreateDate()),
				cnvKey(key))) {
			throw new IOException("Failed to create createDateToIds");
		}
		if (!util.put(getUpdateDateToIds(), cnvL(created.getUpdateDate()),
				cnvKey(key))) {
			throw new IOException("Failed to create updateDateToIds");
		}

		if (needCatchUp()) {
			//HashStore更新
			if (!hStore.created(created.getHid(), createdBi))
				throw new Exception("HashStore");

			//Modelのサブインデックスの書き込み
			if (!util.put(getHidStore(), cnvL(created.getHid()),
					cnvL(created.getId()))) {
				throw new IOException(
						"Failed to put in HidStore. created=" + created);
			}
		}

		//子クラスサブインデックス作成
		if (!createModelConcrete(created))
			throw new IOException("Failed to create sub indexes");

		procAfterCreate(createdBi, created);

		return key;
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
		return createInternal(created);
	}

	protected abstract boolean createModelConcrete(T1 o) throws Exception;

	/**
	 * IDとHIDを外部で指定するタイプの作成処理
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final Long createSpecifiedId(T1 created) throws Exception {
		created.setSpecifiedId(true);
		created.setupAtCreate(util.getTxn());
		return createInternal(created);
	}

	/**
	 * オブジェクト更新時に
	 * 更新されたサブインデックスのフィールドについて制約違反が無いか調べる。
	 * 変更された部分限定のnoExist()をして一意であるかをチェックする等。
	 *
	 * 重複可能（非一意）インデックスフィールドが変更される場合は
	 * 基本的にチェックする必要が無い。
	 *
	 * @param updated	更新されたオブジェクト
	 * @param r		検証結果
	 * @return	更新可能か
	 */
	public final boolean dbValidateAtUpdate(T1 updated, T1 old,
			ValidationResult r) {
		boolean b = true;
		//メインインデックス検証
		if (updated.getId() == null) {
			r.add(Lang.MODEL, Lang.ID, Lang.ERROR_EMPTY);
			b = false;
			return b;
		}

		//サブインデックス検証
		if (needCatchUp()) {
			if (updated.getHid() == null) {
				r.add(Lang.MODEL, Lang.HID, Lang.ERROR_EMPTY);
				b = false;
				return b;
			}
		}

		//具象クラスのサブインデックス検証
		if (!dbValidateAtUpdateModelConcrete(updated, old, r)) {
			b = false;
		}
		return b && r.isNoError();
	}

	public ValidationResult dbValidateAtUpdate2(T1 updated, T1 old,
			ValidationResult r) {
		dbValidateAtUpdate(updated, old, r);
		return r;
	}

	abstract protected boolean dbValidateAtUpdateModelConcrete(T1 updated,
			T1 old, ValidationResult r);

	public final boolean delete(List<Long> keys) throws Exception {
		for (Long id : keys) {
			if (!delete(id)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 通常こちらの削除メソッドを使う。
	 * 削除されたHIDが記録されリサイクルされる
	 *
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public final boolean delete(Long key) throws Exception {
		return delete(get(key));
	}

	/**
	 * オブジェクトを削除する
	 * @param deleted	削除対象
	 * @return	削除に成功したか
	 * @throws Exception
	 */
	public final boolean delete(T1 deleted) throws Exception {
		if (deleted == null)
			return false;
		ValidationResult vr = new ValidationResult();
		deleted.validateAtDelete(vr);
		if (!vr.isNoError()) {
			Glb.getLogger().warn(deleted.toString() + vr.toString(),
					new IllegalStateException());
			if (!deleted.isWarningValidation())
				return false;
		}

		if (!exist(deleted, vr)) {
			Glb.getLogger().warn(deleted.toString() + vr.toString(),
					new IllegalStateException());
			if (!deleted.isWarningValidation())
				return false;
		}

		procBeforeDelete(deleted);

		Long key = getMainKey(deleted);
		if (!deleteDirect(cnvKey(key))) {
			throw new IOException(
					"Failed to deleteDirect(). deleted=" + deleted);
		}

		if (!util.deleteDupSingle(getCreateDateToIds(),
				cnvL(deleted.getCreateDate()), cnvL(deleted.getId()))) {
			throw new IOException(
					"Failed to util.deleteDupSingleNoExist(getCreateDateToIds(). deleted="
							+ deleted);
		}

		if (!util.deleteDupSingle(getUpdateDateToIds(),
				cnvL(deleted.getUpdateDate()), cnvL(deleted.getId()))) {
			throw new IOException(
					"Failed to util.deleteDupSingleNoExist(getUpdateDateToIds(). deleted="
							+ deleted);
		}

		if (needCatchUp()) {
			boolean recycle = !deleted.isCatchUp();
			if (!hStore.removed(deleted.getHid(), recycle)) {
				throw new IOException("Failed to hStore.removed(). recycle="
						+ recycle + " deleted=" + deleted);
			}

			//Modelのサブインデックスの削除
			if (!util.delete(getHidStore(), cnvL(deleted.getHid()))) {
				throw new IOException(
						"Failed to remove in HidStore. deleted=" + deleted);
			}
		}

		if (!deleteModelConcrete(deleted))
			throw new IOException("Failed to remove. deleted=" + deleted);

		procAfterDelete(deleted);

		return true;
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

	protected abstract boolean deleteModelConcrete(T1 o) throws Exception;

	/**
	 * 削除時DB側検証dbValidateAtDelete的なメソッド
	 *
	 * existとnoExistは名前からすると互いに相手の結果を反転すれば
	 * 良いように思えるが、そうではない。
	 * オブジェクトがユニーク属性を複数持っていた場合、
	 * noExistは各ユニーク属性についてまだ存在しない事を確認する。
	 * existは各ユニーク属性について既に存在する事を確認する。
	 * もしユニーク属性AはあるがBはないという中途半端な状態になった場合、
	 * noExistはfalseであり、existもfalseである。
	 * 完全にない、完全にあるを判定するから。
	 *
	 * @param o	検証対象。モデル側検証通過済みであること。
	 * @return	サブインデックス等含めて完全にあるか。
	 * あったりなかったりするサブインデックスは検証されない。
	 * @throws Exception
	 */
	public final boolean exist(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.MODEL, Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getId() == null) {
				vr.add(Lang.MODEL, Lang.ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getId()) == null) {
					vr.add(Lang.MODEL, Lang.ID, Lang.ERROR_DB_NOTFOUND,
							Lang.ID + "=" + o.getId());
					b = false;
				}
			}

			if (needCatchUp()) {
				if (o.getHid() == null) {
					vr.add(Lang.MODEL, Lang.HID, Lang.ERROR_EMPTY);
					b = false;
				} else {
					if (getByHid(o.getHid()) == null) {
						vr.add(Lang.MODEL, Lang.HID, Lang.ERROR_DB_NOTFOUND,
								Lang.HID + "=" + o.getHid());
						b = false;
					}
				}
			}

			if (!existByCreateDate(o.getId(), o.getCreateDate())) {
				vr.add(Lang.MODEL, Lang.CREATE_DATE, Lang.ERROR_DB_NOTFOUND,
						"o=" + o);
				b = false;
			}
			if (!existByUpdateDate(o.getId(), o.getUpdateDate())) {
				vr.add(Lang.MODEL, Lang.UPDATE_DATE, Lang.ERROR_DB_NOTFOUND,
						"o=" + o);
				b = false;
			}

			if (!existModelConcrete(o, vr)) {
				b = false;
			}
		}
		return b && vr.isNoError();
	}

	public final ValidationResult exist2(T1 o, ValidationResult vr)
			throws Exception {
		exist(o, vr);
		return vr;
	}

	public boolean existByCreateDate(Long id, long createDate) {
		return util.getDupSingle(getCreateDateToIds(), cnvL(createDate),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	public boolean existByUpdateDate(Long id, long updateDate) {
		return util.getDupSingle(getUpdateDateToIds(), cnvL(updateDate),
				cnvL(id), bi -> cnvL(bi)) != null;
	}

	protected abstract boolean existModelConcrete(T1 o, ValidationResult r)
			throws Exception;

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

	public StoreInfo getCreateDateToIds() {
		return createDateToIds;
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

	public Map<Long, Long> getIdsByCreateDate(Long startCreateDate,
			Long endCreateDate, long max) {
		return getIdsByDate(getCreateDateToIds(), startCreateDate,
				endCreateDate, max);
	}

	private Map<Long, Long> getIdsByDate(StoreInfo s, Long startDate,
			Long endDate, long max) {
		if (startDate > endDate)
			throw new IllegalArgumentException("startDate > endDate");

		Function<SearchContext<Long, Long>,
				Boolean> breakF = ctx -> cnvL(ctx.getKey()) >= endDate;

		SearchContext<Long, Long> ctx = new SearchContext<>(s, cnvL(startDate),
				k -> cnvL(k), v -> cnvL(v), true, 0, max, breakF, null,
				(runtimeCtx, c) -> c.getSearchKeyRange(runtimeCtx.getKey()));

		Map<Long, Long> r = new HashMap<>();
		util.search(ctx, r);
		return r;
	}

	public Map<Long, Long> getIdsByUpdateDate(Long startUpdateDate,
			Long endUpdateDate, long max) {
		return getIdsByDate(getCreateDateToIds(), startUpdateDate,
				endUpdateDate, max);
	}

	/**
	 * @return	最後のID
	 */
	public final Long getLastId() {
		Store s = getStore();
		try (Cursor c = s.openCursor(util.getTxn())) {
			if (!c.getLast())
				return ModelI.getFirstId() - 1;
			if (ByteIterable.EMPTY.equals(c.getKey())) {
				return ModelI.getFirstId() - 1;
			}
			return cnvKey(c.getKey());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return ModelI.getFirstId() - 1;
		}
	}

	/**
	 * @return	メインストアのキー。Long連番のID
	 */
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
		HashSet<T2> tmp = new HashSet<>();

		long c = count();
		for (int i = 0; i < max && i < c; i++) {
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
			tmp.add(o);
		}

		return new ArrayList<>(tmp);
	}

	@SuppressWarnings("unchecked")
	public T1 getRawObj(Long id) {
		try {
			Object o = cnvO(util.get(getMainStoreInfo(), cnvKey(id)));
			if (o == null || !(o instanceof ModelI))
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
	@SuppressWarnings("unchecked")
	public T1 getRawObjByHid(Long hid) {
		if (!needCatchUp())
			return null;
		Object o = getRawObj(getIdByHid(hid));
		if (o == null || !(o instanceof ModelI))
			return null;
		return (T1) o;
	}

	public final RecycleHidStore getRecycleHidStore() {
		if (!needCatchUp())
			return null;
		return hStore.getRecycleHIDStore();
	}

	abstract protected List<StoreInfo> getStoresModelConcrete();

	@Override
	protected List<StoreInfo> getStoresObjectStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		if (needCatchUp())
			r.add(getHidStore());
		if (hStore != null)
			r.addAll(hStore.getStores());
		if (uStore != null)
			r.addAll(uStore.getStores());
		r.add(getCreateDateToIds());
		r.add(getUpdateDateToIds());
		r.addAll(getStoresModelConcrete());
		return r;
	}

	public StoreInfo getUpdateDateToIds() {
		return updateDateToIds;
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
	 * ストア毎のクラスでinstanceofを行う。
	 * instanceofはモデル系インターフェースかモデルクラスの完全修飾名で行う。
	 * モデルクラス名を指定する場合、
	 * モデルクラスがアップデートされ新しいバージョンのクラス定義が追加されたら
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
	 * @return	同調処理が行われるストアか
	 * ただし客観コアストアを除く。同調対象だがH木など必要無いので。
	 * つまりH木とかHIDといったものを使用するストアかという意味。
	 */
	protected boolean needCatchUp() {
		return true;
	}

	/**
	 * 作成時DB側検証dbValidateAtCreate的なメソッド
	 *
	 * インデックス等含めて完全に無い
	 * このメソッドを呼ぶ前にモデルクラスの検証メソッドによって
	 * ヌルチェック等が終わっている前提
	 * @throws Exception
	 */
	public final boolean noExist(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.MODEL, Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (o.getId() == null) {//未登録
				vr.add(Lang.MODEL, Lang.ID, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (get(o.getId()) != null) {
					vr.add(Lang.MODEL, Lang.ID, Lang.ERROR_DB_EXIST);
					b = false;
				}
			}

			if (needCatchUp()) {
				if (o.getHid() == null) {
					vr.add(Lang.MODEL, Lang.HID, Lang.ERROR_EMPTY);
					b = false;
				} else {
					Long idByHid = getIdByHid(o.getHid());
					if (idByHid != null) {
						vr.add(Lang.MODEL, Lang.HID, Lang.ERROR_DB_EXIST);
						b = false;
					}
				}
			}

			//時間系
			if (existByCreateDate(o.getId(), o.getCreateDate())) {
				vr.add(Lang.MODEL, Lang.CREATE_DATE, Lang.ERROR_DB_EXIST,
						"o=" + o);
				b = false;
			}
			if (existByUpdateDate(o.getId(), o.getUpdateDate())) {
				vr.add(Lang.MODEL, Lang.UPDATE_DATE, Lang.ERROR_DB_EXIST,
						"o=" + o);
				b = false;
			}

			if (!noExistModelConcrete(o, vr)) {
				b = false;
			}
		}

		return b && vr.isNoError();
	}

	public final ValidationResult noExist2(T1 o, ValidationResult vr)
			throws Exception {
		noExist(o, vr);
		return vr;
	}

	protected abstract boolean noExistModelConcrete(T1 o, ValidationResult r)
			throws Exception;

	protected void procAfterCreate(ByteIterable createdBi, T1 created)
			throws IOException {
		if (created.getId() == null || created.getHid() == null)
			throw new IOException("Failed to create");
		if (created.isRecycleHid()) {
			if (!hStore.removeRecycleHid(created.getHid()))
				throw new IOException("Failed to removeRecycleHid()");
		}
		notification(created);
	}

	/**
	 * 通知システムに登録
	 * @param obj
	 */
	private void notification(T1 obj) {
		try {
			Glb.getMiddle().getTenyupedia().addCandidate(obj);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	/**
	 * DBで削除された直後に呼び出される
	 */
	protected void procAfterDelete(T1 deleted) throws Exception {
	}

	/**
	 * DBで更新された直後に呼び出される
	 */
	protected void procAfterUpdate(ByteIterable updatedBi, T1 updated, T1 old)
			throws Exception {
		notification(updated);
	}

	/**
	 * 検証後、作成直前に呼び出される
	 *
	 * これはストア側のロジックであり
	 * {@link Model#setupAtCreate()}はモデル側のロジックで、異なる。
	 *
	 * ここに書かれているようなIDやHIDの設定処理は
	 * ストア上で一意であるべきという制約のためストア側ロジックとしてしか実装できない。
	 *
	 * @param created	検証通過済みの作成対象オブジェクト
	 * @throws Exception
	 */
	protected void procBeforeCreate(T1 created) throws Exception {
		//HID設定
		Long recycleHid = null;
		if (needCatchUp() && created.getHid() == null) {
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

	/**
	 * DBで削除される直前に呼び出される
	 */
	protected void procBeforeDelete(T1 deleted) throws Exception {
	}

	protected void procBeforeUpdate(T1 updated, T1 old) throws Exception {
		if (needCatchUp()) {
			//これは失敗が致命的ではない
			if (!uStore.writeUpdated(updated.getId()))
				Glb.getLogger().warn("", new IOException());
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

	/**
	 * オブジェクトを更新する
	 * @param updated
	 * @return
	 * @throws Exception
	 */
	public final boolean update(T1 updated) throws Exception {
		updated.setupAtUpdate(util.getTxn());
		return updateInternal(updated);
	}

	private final boolean updateInternal(T1 updated) throws Exception {
		if (updated == null) {
			Glb.debug("updated is null ");
			return false;
		}
		ValidationResult vr = new ValidationResult();
		Long key = getMainKey(updated);
		T1 old = get(key);
		if (old == null)
			return false;
		updated.validateAtUpdate(vr);
		if (!vr.isNoError()) {
			Glb.getLogger().warn(updated.toString() + vr.toString(),
					new IllegalStateException());
			if (!updated.isWarningValidation())
				return false;
		}

		if (!validateAtUpdateChange(updated, vr)) {
			Glb.getLogger().warn(updated.toString() + vr.toString(),
					new IllegalStateException());
			if (!updated.isWarningValidation())
				return false;
		}

		updated.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.getLogger().warn(updated.toString() + vr.toString(),
					new IllegalStateException());
			if (!updated.isWarningValidation())
				return false;
		}

		return updateAfterValidate(key, updated, old);
	}

	/**
	 * 検証通過後の更新処理
	 *
	 * @param key
	 * @param updated
	 * @param old
	 * @return
	 * @throws Exception
	 */
	private boolean updateAfterValidate(Long key, T1 updated, T1 old)
			throws Exception {
		procBeforeUpdate(updated, old);

		ByteIterable updatedBi = cnvO(updated);

		if (!putDirect(cnvKey(key), updatedBi))
			throw new IOException();

		//時間系
		if (Glb.getUtil().notEqual(updated.getUpdateDate(),
				old.getUpdateDate())) {
			if (!util.deleteDupSingle(getUpdateDateToIds(),
					cnvL(old.getUpdateDate()), cnvL(old.getId())))
				throw new IOException("Failed to updateSubIndex");
			if (!util.put(getUpdateDateToIds(), cnvL(updated.getUpdateDate()),
					cnvL(updated.getId())))
				throw new IOException("Failed to updateSubIndex");
		}

		if (needCatchUp()) {
			if (!hStore.updated(updated.getId(), updatedBi))
				throw new IOException();

			//Modelのサブインデックス更新
			if (Glb.getUtil().notEqual(updated.getHid(), old.getHid())) {
				if (old.getHid() != null) {
					if (!util.delete(getHidStore(), cnvL(old.getHid())))
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

		//子クラス系更新処理
		if (!updateModelConcrete(updated, old))
			throw new IOException();

		procAfterUpdate(updatedBi, updated, old);
		return true;
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
	private final boolean updateAtCatchUp(T1 updated, T1 old) throws Exception {
		updated.setCatchUp(true);
		updated.setSpecifiedId(true);
		return updateAfterValidate(updated.getId(), updated, old);
	}

	/**
	 * インデックスを子クラスで更新する
	 * @param updated		更新された後の新しいオブジェクト
	 * @param old 			更新される前の古いオブジェクト
	 * @return				必要な更新作業ができたか
	 * @throws Exception
	 */
	protected abstract boolean updateModelConcrete(T1 updated, T1 old)
			throws Exception;

	/**
	 * updatedと同じIDのDB上のオブジェクトの比較から
	 * その状態遷移について検証する。
	 *
	 * オブジェクト内部の状態についての検証と
	 * DBにおける整合性の検証の2種類の検証処理が呼び出される。
	 *
	 * @param updated
	 * @param r
	 * @return
	 */
	public boolean validateAtUpdateChange(T1 updated, ValidationResult r) {
		T2 old = get(getMainKey(updated));
		if (old == null) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_DB_NOTFOUND);
			return false;
		}

		if (!updated.validateAtUpdateChange(r, old)) {
			Glb.getLogger().warn(updated.toString() + r.toString(),
					new IllegalStateException());
			if (!updated.isWarningValidation())
				return false;
		}

		if (!dbValidateAtUpdate(updated, old, r)) {
			Glb.getLogger().warn(updated.toString() + r.toString(),
					new IllegalStateException());
			if (!updated.isWarningValidation())
				return false;
		}

		return r.isNoError();
	}

	public ValidationResult validateAtUpdateChange2(T1 updated,
			ValidationResult r) {
		validateAtUpdateChange(updated, r);
		return r;
	}

	public static interface StoreFunction<T, R> {
		R apply(T t) throws Exception;
	}

	/**
	 * @param s
	 * @param oldId
	 * @param updatedId
	 * @param updatedIdsF
	 * @param oldIdsF
	 * @param cnvK
	 * @return
	 * @throws IOException
	 */
	protected <T1, T2, K> boolean updateCollectionSubIndex(StoreInfo s,
			Long oldId, Long updatedId, Supplier<Collection<K>> updatedIdsF,
			Supplier<Collection<K>> oldIdsF, Function<K, ByteIterable> cnvK)
			throws IOException {
		//oldから削除された要素一覧
		Collection<K> removed = null;
		Collection<K> updatedIds = updatedIdsF.get();
		Collection<K> oldIds = oldIdsF.get();
		//変更が無ければ何もしない
		if (!Glb.getUtil().notEqual(updatedIds, oldIds)) {
			return true;
		}
		if (updatedIds == null) {
			//updatedのがnullならoldのは全て削除されたもの
			removed = oldIds;
		} else {
			//oldのがnullならremovedは空
			if (oldIds != null) {
				//oldにあってupdatedに無い、つまり削除された
				removed = Glb.getUtil().getExtra(oldIds,
						new HashSet<>(updatedIds));
			}
		}

		//updatedで追加された要素一覧
		Collection<K> added = null;
		if (oldIds == null) {
			//oldのがnullならupdatedのは全て追加されたもの
			removed = updatedIds;
		} else {
			//updatedのがnullならaddedは空
			if (updatedIds != null) {
				//updatedにあってoldに無い、つまり追加された
				added = Glb.getUtil().getExtra(updatedIds,
						new HashSet<>(oldIds));
			}
		}

		//削除された要素があればサブインデックスを削除
		if (removed != null) {
			for (K key : removed) {
				if (!util.deleteDupSingle(s, cnvK.apply(key), cnvL(oldId)))
					return false;
			}
		}

		//追加された要素があればサブインデックスを追加
		if (added != null) {
			for (K key : added) {
				if (!util.put(s, cnvK.apply(key), cnvL(oldId))) {
					return false;
				}
			}
		}
		return true;
	}

}
