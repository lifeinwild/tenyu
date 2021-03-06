package bei7473p5254d69jcuat.tenyu.db.store.satellite;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 永続化されるが客観ではなくノード別管理となるオブジェクト。
 *
 * Tenyuのモデル設計は{@link ModelStore}系列がメインで、
 * その系列は客観となるが、
 * この系列はノード別管理となり補助的な役割を持つ。
 *
 * {@link ModelStore}系と異なる検証処理の構造を持つ。
 * これは{@link Object}を改修できないことによって生じている可能性がある。
 * つまり{@link ModelI}のいくつかのインターフェースを{@link Object}が持てれば
 * 共通化が可能かもしれない。
 *
 * このクラスに定義された検証系インターフェースは
 * 必要ならオーバーライドする事を想定している。
 *
 * 旧名:SatteliteStore
 * もしこのようなモデル設計をP2Pソフトウェア以外でやるなら、
 * 旧名の方が妥当かもしれない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class ByNodeStore<K, V extends ValidatableI>
		extends ObjectStore<K, V> {

	public ByNodeStore(Transaction txn) {
		super(txn);
	}

	public final boolean create(K key, V val) throws Exception {
		if (key == null || val == null)
			return false;

		ValidationResult vr = new ValidationResult();
		val.validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		val.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		//重複確認
		if (!dbValidateAtCreate(key, val, vr)) {
			Glb.debug("Failed to dbValidateAtCreate. " + vr);
			return false;
		}

		//書き込み
		if (!putDirect(cnvKey(key), cnvVal(val)))
			throw new Exception(key.getClass().getSimpleName());
		return true;
	}

	public final boolean update(K key, V updated) throws Exception {
		if (updated == null) {
			Glb.debug("updated is null ");
			return false;
		}
		if (key == null) {
			return false;
		}
		ValidationResult vr = new ValidationResult();
		V old = get(key);
		if (old == null)
			return false;
		updated.validateAtUpdate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!validateAtUpdateChange(key, updated, old, vr)) {
			Glb.debug(vr.toString());
			return false;
		}

		updated.validateReference(vr, util.getTxn());
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!putDirect(cnvKey(key), cnvVal(updated)))
			throw new IOException();

		return true;
	}

	public boolean validateAtUpdateChange(K key, V updated, V old,
			ValidationResult r) {
		if (old == null) {
			old = get(key);
		}
		if (old == null) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_DB_NOTFOUND);
			return false;
		}

		if (!updated.validateAtUpdateChange(r, old)) {
			return false;
		}

		if (!dbValidateAtUpdate(key, updated, old, r)) {
			return false;
		}

		return r.isNoError();
	}

	public final boolean delete(K key) throws Exception {
		if (key == null)
			return false;
		V deleted = get(key);
		if (deleted == null)
			return false;
		ValidationResult vr = new ValidationResult();
		deleted.validateAtDelete(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		if (!dbValidateAtDelete(key, deleted, vr)) {
			Glb.debug(vr.toString());
			return false;
		}

		return deleteDirect(cnvKey(key));
	}

	public boolean delete(List<K> keys) throws Exception {
		boolean r = true;
		for (K key : keys) {
			if (!delete(key)) {
				r = false;
			}
		}
		return r;
	}

	/**
	 * DB系更新時検証。
	 * もしサブインデックスがあり複数のフィールドで一意になるなど
	 * ストア固有の複雑な制約があれば
	 * このインターフェースで検証処理を実装する。
	 * 必要ならオーバーライド
	 * @param key
	 * @param updated
	 * @param old
	 * @param r
	 * @return
	 */
	protected boolean dbValidateAtUpdateSatelliteConcrete(K key, V updated,
			V old, ValidationResult r) {
		return true;
	}

	public final boolean dbValidateAtCreate(K key, V val, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (key == null || val == null || vr == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (get(key) != null) {
				vr.add(Lang.OBJECT, Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	public final boolean dbValidateAtDelete(K key, V val, ValidationResult vr)
			throws Exception {
		boolean b = true;
		if (key == null || val == null || vr == null) {
			vr.add(Lang.OBJECT, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (get(key) == null) {
				vr.add(Lang.OBJECT, Lang.ERROR_DB_NOTFOUND);
				b = false;
			}
		}
		return b;
	}

	/**
	 * 実際のところDBに関する更新系の検証は生じていないので
	 * 子クラスに実装を要求しない。
	 *
	 * @param key
	 * @param updated
	 * @param old
	 * @param r
	 * @return
	 */
	public boolean dbValidateAtUpdate(K key, V updated, V old,
			ValidationResult r) {
		boolean b = true;
		if (!dbValidateAtUpdateSatelliteConcrete(key, updated, old, r)) {
			b = false;
		}
		return b && r.isNoError();
	}

}
