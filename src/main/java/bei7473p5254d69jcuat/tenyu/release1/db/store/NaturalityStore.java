package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * User,Web等Naturalityの子クラスが必ず備えるDB関連機能
 *
 * UserStore,WebStoreなど子クラス専用ストアは、getStore()において
 * Naturality用ストアも含めなければならない。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T1>
 * @param <T2>
 */
public abstract class NaturalityStore<T1 extends NaturalityDBI, T2 extends T1>
		extends ObjectivityObjectStore<T1, T2> {
	/*
	public static <S extends NaturalityStore<T1, T2>,
			T1 extends NaturalityDBI,
			T2 extends T1> T2 getByNameSimple(
					Function<Transaction, S> getStoreFunc, String name) {
		return simple(getStoreFunc, (s) -> {
			Long id = s.getIdByName(name.trim());
			if (id == null)
				return null;
			return s.get(id);
		});
	}
	*/

	/*
	public static <S extends NaturalityStore<T1, T2>,
			T1 extends NaturalityDBI,
			T2 extends T1> Long getIdByNameSimple(
					Function<Transaction, S> getStoreFunc, String name) {
		return simple(getStoreFunc, (s) -> s.getIdByName(name.trim()));
	}
	*/

	public static final StoreInfo getNameStoreStatic(String storeName) {
		return new StoreInfo(storeName + "_nameToId");
	}

	/*
	public static <S extends NaturalityStore<T1, T2>,
			T1 extends NaturalityDBI,
			T2 extends T1> Map<String, Long> prefixSearchByNameSimple(
					Function<Transaction, S> getStoreFunc, String prefix,
					int max) {
		return simple(getStoreFunc,
				(s) -> s.prefixSearchByNameRough(prefix.trim(), max));
	}
	*/

	protected NaturalityStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	abstract protected boolean createNaturalityConcrete(T1 o) throws Exception;

	@Override
	protected final boolean createObjectivityObjectConcrete(T1 o)
			throws Exception {
		if (!util.put(getNameStore(), cnvS(o.getName()),
				cnvL(o.getRecycleId()))) {
			throw new IOException("Failed to put");
		}

		if (!createNaturalityConcrete(o)) {
			return false;
		}

		return true;
	}

	abstract protected boolean dbValidateAtUpdateNaturalityConcrete(T1 updated,
			T1 old, ValidationResult r);

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;

		if (Glb.getUtil().notEqual(updated.getName(), old.getName())) {
			if (getIdByName(updated.getName()) != null) {
				r.add(Lang.NATURALITY_NAME, Lang.ERROR_DB_EXIST,
						"name=" + updated.getName());
				b = false;
			}
		}

		if (!dbValidateAtUpdateNaturalityConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	abstract protected boolean deleteNaturalityConcrete(T1 o) throws Exception;

	@Override
	protected final boolean deleteObjectivityObjectConcrete(T1 o)
			throws Exception {
		if (!util.remove(getNameStore(), cnvS(o.getName()))) {
			throw new IOException("Failed to remove");
		}
		if (!deleteNaturalityConcrete(o))
			return false;
		return true;
	}

	abstract protected boolean existNaturalityConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	public final boolean existObjectivityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByName(o.getName()) == null) {
			vr.add(Lang.NATURALITY_NAME, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		if (!existNaturalityConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	public T2 getByName(String name) {
		Long id = getIdByName(name);
		if (id == null)
			return null;
		return get(id);
	}

	public Long getIdByName(String name) {
		return getId(getNameStore(), cnvS(name));
	}

	public final StoreInfo getNameStore() {
		return getNameStoreStatic(getName());
	}

	abstract public List<StoreInfo> getStoresNaturalityConcrete();

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getNameStore());
		r.addAll(getStoresNaturalityConcrete());
		return r;
	}

	abstract protected boolean noExistNaturalityConcrete(T1 o,
			ValidationResult vr) throws Exception;

	@Override
	public final boolean noExistObjectivityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (getIdByName(o.getName()) != null) {
			vr.add(Lang.NATURALITY_NAME, Lang.ERROR_DB_EXIST);
			b = false;
		}
		if (!noExistNaturalityConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	/**
	 * サジェスション等ラフな情報で良い用途に使う
	 * @param prefix	名前の接頭辞
	 * @param max		取得する最大件数
	 * @return			該当したレコード一覧	名前：ID
	 */
	public Map<String, Long> prefixSearchByNameRough(String prefix, int max) {
		SearchContext<String,
				Long> ctx = new SearchContext<>(getNameStore(),
						cnvSRemoveSuffix(prefix), (bi) -> cnvS(bi),
						(bi) -> cnvL(bi), true, 0, max, new PrefixEqual<>(),
						null, (runtimeCtx, cursor) -> {
							cursor.getSearchKeyRange(cnvS(prefix));
						});
		return util.search(ctx);
	}

	abstract protected boolean updateNaturalityConcrete(T1 updated, T1 old)
			throws Exception;

	@Override
	protected final boolean updateObjectivityObjectConcrete(T1 updated, T1 old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getName(), old.getName())) {
			if (old.getName() != null) {
				if (!util.remove(getNameStore(), cnvS(old.getName())))
					throw new IOException("Failed to remove");
			}
			if (!util.put(getNameStore(), cnvS(updated.getName()),
					cnvL(updated.getRecycleId())))
				throw new IOException("Failed to put");
		}
		if (!updateNaturalityConcrete(updated, old))
			return false;
		return true;
	}

}
