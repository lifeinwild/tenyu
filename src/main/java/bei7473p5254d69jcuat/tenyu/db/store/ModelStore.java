package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 全ストア系クラスの親クラス
 *
 * サブインデックス無し。
 * 検索という概念は{@link IdObjectI}以下でしか存在しない。IDが必要だから
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <K>
 * @param <T1>
 * @param <T2>
 */
public abstract class ModelStore<K, T1 extends ModelI, T2 extends T1>
		extends ObjectStore<K, T2> {

	public static final StoreInfo getCreateHistoryIndexStoreStatic(
			String modelName) {
		return new StoreInfo(modelName + "_createHistoryIndexToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	public static final StoreInfo getUpdateHistoryIndexStoreStatic(
			String modelName) {
		return new StoreInfo(modelName + "_updateHistoryIndexToIds_Dup",
				StoreConfig.WITH_DUPLICATES);
	}

	/**
	 * 作成HIからそのHIで作成されたID一覧へ
	 */
	//private final StoreInfo createHiToIds;
	/**
	 * 更新HIからそのHIで更新されたID一覧へ
	 */
	//private final StoreInfo updateHiToIds;

	public ModelStore(Transaction txn) {
		super(txn);
		//createHiToIds = getCreateHistoryIndexStoreStatic(getName());
		//updateHiToIds = getUpdateHistoryIndexStoreStatic(getName());
	}

	/**
	 * オブジェクトを作成する
	 * @param created
	 * @return
	 * @throws Exception
	 */
	public final K create(T1 created) throws Exception {
		if (created == null)
			return null;

		created.setupAtCreate();

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

		procBeforeCreate(created);

		//登録されるオブジェクトのメインキー
		K key = getMainKey(created);
		ByteIterable cnvId = cnvKey(key);

		//重複確認
		if (!noExist(created, vr)) {
			Glb.debug("Failed to noExist. " + vr);
			return null;
		}

		//Modelサブインデックス更新
		/*
		if (!util.put(createHiToIds, cnvL(created.getCreateHistoryIndex()),
				cnvKey(key))) {
			throw new IOException("Failed to create createHiToIds");
		}
		if (!util.put(updateHiToIds, cnvL(created.getCreateHistoryIndex()),
				cnvKey(key))) {
			throw new IOException("Failed to update updateHiToIds");
		}
		*/

		ByteIterable createdBi = cnvO(created);
		//メインストア書き込み
		if (!putDirect(cnvId, createdBi, created.isLastKey()))
			throw new Exception(
					"Failed to putDirect in IdObjectStore. created=" + created);

		//子クラス毎のサブインデックス更新
		if (!createModelConcrete(createdBi, created)) {
			Glb.debug("Failed to createSub");
			return null;
		}

		procAfterCreate(createdBi, created);

		return key;
	}

	/**
	 * 作成処理。インデックス部分について子クラスで作成
	 *
	 * @param o
	 * @return
	 * @throws Exception
	 */
	protected abstract boolean createModelConcrete(ByteIterable createdBi, T1 o)
			throws Exception;

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
		return dbValidateAtUpdateModelConcrete(updated, old, r);
	}

	public ValidationResult dbValidateAtUpdate2(T1 updated, T1 old,
			ValidationResult r) {
		dbValidateAtUpdate(updated, old, r);
		return r;
	}

	abstract protected boolean dbValidateAtUpdateModelConcrete(T1 updated,
			T1 old, ValidationResult r);

	/**
	 * 通常こちらの削除メソッドを使う。
	 * 削除されたHIDが記録されリサイクルされる
	 *
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public final boolean delete(K key) throws Exception {
		return delete(get(key));
	}

	public final boolean delete(List<K> keys) throws Exception {
		for (K id : keys) {
			if (!delete(id)) {
				return false;
			}
		}
		return true;
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
		if (deleted == null || !vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!exist(deleted, vr)) {
			Glb.debug(vr.toString());
			return false;
		}

		procBeforeDelete(deleted);

		K key = getMainKey(deleted);
		if (!deleteDirect(cnvKey(key))) {
			throw new IOException(
					"Failed to deleteDirect(). deleted=" + deleted);
		}

		if (!deleteModelConcrete(deleted))
			throw new IOException();

		procAfterDelete(deleted);

		return true;
	}

	abstract protected boolean deleteModelConcrete(T1 o) throws Exception;

	/**
	 * dbValidateAtDelete的なメソッド
	 *
	 * このメソッドを呼ぶ前にモデルクラスの検証メソッドによって
	 * ヌルチェック等が終わっている前提
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
	 * @param o	検証対象
	 * @return	サブインデックス等含めて完全にあるか。
	 * あったりなかったりするサブインデックスは検証されない。
	 * @throws Exception
	 */
	public final boolean exist(T1 o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (o == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!existModelConcrete(o, vr))
				b = false;
		}
		return b && vr.isNoError();
	}

	public final ValidationResult exist2(T1 o, ValidationResult vr)
			throws Exception {
		exist(o, vr);
		return vr;
	}

	abstract protected boolean existModelConcrete(T1 o, ValidationResult vr)
			throws Exception;

	/**
	 * @return	メインストアのキー
	 */
	abstract public K getMainKey(T1 o);

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
	protected abstract boolean noExistModelConcrete(T1 o, ValidationResult r)
			throws Exception;

	/**
	 * DBに登録された直後に呼び出される
	 */
	protected void procAfterCreate(ByteIterable createdBi, T1 created)
			throws Exception {
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
	}

	/**
	 * DBに登録される直前に呼び出される
	 */
	protected void procBeforeCreate(T1 created) throws Exception {
	}

	/**
	 * DBで削除される直前に呼び出される
	 */
	protected void procBeforeDelete(T1 deleted) throws Exception {
	}

	/**
	 * DBで更新される直前に呼び出される
	 */
	protected void procBeforeUpdate(T1 updated, T1 old) throws Exception {
	}

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
		K key = getMainKey(updated);
		T1 old = get(key);
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
		procBeforeUpdate(updated, old);

		ByteIterable updatedBi = cnvO(updated);

		if (!putDirect(cnvKey(key), updatedBi))
			throw new IOException();
		if (!updateModelConcrete(updatedBi, updated, old))
			throw new IOException();

		procAfterUpdate(updatedBi, updated, old);

		return true;
	}

	abstract protected boolean updateModelConcrete(ByteIterable updatedBi,
			T1 updated, T1 old) throws Exception;

	public ValidationResult validateAtUpdate2(T1 updated, ValidationResult r) {
		validateAtUpdateChange(updated, r);
		return r;
	}

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
			return false;
		}

		if (!dbValidateAtUpdate(updated, old, r)) {
			return false;
		}

		return r.isNoError();
	}

}
