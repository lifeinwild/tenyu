package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.DBUtil.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link IndividualityObject}系クラスが必ず備えるDB関連機能
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T1>
 * @param <T2>
 */
public abstract class IndividualityObjectStore<T1 extends IndividualityObjectI,
		T2 extends T1> extends AdministratedObjectStore<T1, T2> {

	public static final StoreInfo getNameStoreStatic(String storeName,
			boolean uniqueName) {
		if (uniqueName) {
			return new StoreInfo(storeName + "_nameToId");
		} else {
			return new StoreInfo(storeName + "_nameToId_Dup",
					StoreConfig.WITH_DUPLICATES);
		}
	}

	public static final StoreInfo getTagStoreStatic(String storeName) {
		return new StoreInfo(storeName + "_tagToIds_Dup",
				StoreConfig.WITH_DUPLICATES, true);
	}

	public static final StoreInfo getTagIdStoreStatic(String storeName) {
		return new StoreInfo(storeName + "_tagIdToIds_Dup",
				StoreConfig.WITH_DUPLICATES, true);
	}

	/**
	 * 場合によって具象クラスでオーバーライドする。
	 *
	 * nameの一意制約を切り替えるこのオプションは
	 * {@link CreativeObjectI}を実装する上で必要になった。
	 * バージョンアップに伴い同じ名前のオブジェクトを作成するから。
	 * モデルクラスではこのような抽象性の崩壊は生じておらず
	 * ただDB側の一意制約の違いによってストアクラスにおいてのみ抽象性の崩壊が生じた。
	 * モデルとDBの境界で抽象性の崩壊が避けられなかった。
	 * nameという意味は同じなので、そこに適切な抽象化が施されるべきところで
	 * JavaやXodusにそれを解決する手段が無いという事だと解釈している。
	 * 一意制約の違いは返値がLongかList<Long>かなどインターフェースの違いにも繋がる。
	 *
	 * {@link UnsupportedOperationException}などで使用可能なインターフェースを分ける
	 * 事が考えられる。
	 *
	 * @return	このストアにおいて名前は一意か
	 */
	public boolean isUniqueName() {
		return true;
	}

	/**
	 * {@link Tag#getId()}によるサブインデックス
	 */
	private final StoreInfo tagIdToId;

	private final StoreInfo nameToId;

	private final StoreInfo tagToIds;

	protected IndividualityObjectStore(Transaction txn) {
		super(txn);
		this.nameToId = getNameStoreStatic(getName(), isUniqueName());
		tagToIds = getTagStoreStatic(getName());
		tagIdToId = getTagIdStoreStatic(getName());
	}

	public StoreInfo getTagIdToId() {
		return tagIdToId;
	}

	@Override
	protected final boolean createAdministratedObjectConcrete(T1 o)
			throws Exception {
		if (!util.put(getNameStore(), cnvS(o.getName()), cnvL(o.getId()))) {
			throw new IOException("Failed to put");
		}

		HashSet<String> tags = o.getTags();
		if (tags != null) {
			for (String tag : o.getTags()) {
				if (!util.put(getTagStore(), cnvS(tag), cnvL(o.getId()))) {
					throw new IOException("Failed to put");
				}
			}
		}

		for (Long tagId : o.getTagIds()) {
			if (!util.put(getTagIdToId(), cnvL(tagId), cnvL(o.getId()))) {
				return false;
			}
		}

		if (!createIndividualityObjectConcrete(o)) {
			return false;
		}

		return true;
	}

	abstract protected boolean createIndividualityObjectConcrete(T1 o)
			throws Exception;

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(T1 updated,
			T1 old, ValidationResult r) {
		boolean b = true;

		if (isUniqueName()) {
			if (Glb.getUtil().notEqual(updated.getName(), old.getName())) {
				if (getIdByName(updated.getName()) != null) {
					r.add(Lang.INDIVIDUALITY_OBJECT_NAME, Lang.ERROR_DB_EXIST,
							"name=" + updated.getName());
					b = false;
				}
			}
		}

		Collection<Long> added = Glb.getUtil().getExtra(updated.getTagIds(),
				old.getTagIds());
		//このチェック処理はこのインデックスが動作しなくなる事を防止するというより
		//どこかにバグがあることを検出するような意味
		for (Long tagId : added) {
			if (existByTagId(tagId, updated.getId())) {
				r.add(Lang.INDIVIDUALITY_OBJECT, Lang.TAG_IDS,
						Lang.ERROR_DB_EXIST, "tagId=" + tagId);
				b = false;
				break;
			}
		}

		if (!dbValidateAtUpdateIndividualityObjectConcrete(updated, old, r)) {
			b = false;
		}
		return b;
	}

	public boolean existByTagId(Long tagId, Long id) {
		if (tagId == null || id == null)
			return false;
		return util.getDupSingle(getTagIdToId(), cnvL(tagId), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	abstract protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			T1 updated, T1 old, ValidationResult r);

	@Override
	protected final boolean deleteAdministratedObjectConcrete(T1 o)
			throws Exception {
		if (isUniqueName()) {
			if (!util.remove(getNameStore(), cnvS(o.getName()))) {
				throw new IOException("Failed to remove");
			}
		} else {
			if (!util.deleteDupSingle(getNameStore(), cnvS(o.getName()),
					cnvL(o.getId()))) {
				throw new IOException("Failed to remove");
			}
		}

		HashSet<String> tags = o.getTags();
		if (tags != null) {
			for (String tag : tags) {
				if (!util.deleteDupSingle(getTagStore(), cnvS(tag),
						cnvL(o.getId()))) {
					throw new IOException("Failed to remove");
				}
			}
		}

		for (Long tagId : o.getTagIds()) {
			if (!util.deleteDupSingle(getTagIdToId(), cnvL(tagId),
					cnvL(o.getId())))
				return false;
		}

		if (!deleteIndividualityObjectConcrete(o))
			return false;
		return true;
	}

	abstract protected boolean deleteIndividualityObjectConcrete(T1 o)
			throws Exception;

	@Override
	public final boolean existAdministratedObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByName(o.getName(), o.getId())) {
			vr.add(Lang.INDIVIDUALITY_OBJECT_NAME, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		HashSet<String> tags = o.getTags();
		if (tags != null) {
			for (String tag : tags) {
				if (!existByTag(tag, o.getId())) {
					vr.add(Lang.INDIVIDUALITY_OBJECT_TAGS,
							Lang.ERROR_DB_NOTFOUND, "tag=" + tag);
					b = false;
					break;
				}
			}
		}
		for (Long tagId : o.getTagIds()) {
			if (!existByTagId(tagId, o.getId())) {
				vr.add(Lang.INDIVIDUALITY_OBJECT, Lang.TAG_IDS,
						Lang.ERROR_DB_NOTFOUND,
						"tagId=" + tagId + " id=" + o.getId());
				b = false;
				break;
			}
		}

		if (!existIndividualityObjectConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	/**
	 * @param tagId
	 * @return
	 */
	public List<Long> getIdsByTagId(Long tagId) {
		return util.getDup(getTagIdToId(), cnvL(tagId), bi -> cnvL(bi));
	}

	abstract protected boolean existIndividualityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	public T2 getByName(String name) {
		Long id = getIdByName(name);
		if (id == null)
			return null;
		return get(id);
	}

	public Long getIdByName(String name) {
		if (!isUniqueName())
			throw new UnsupportedOperationException("not unique name.");
		return getId(getNameStore(), cnvS(name));
	}

	public List<Long> getIdsByName(String name) {
		if (isUniqueName())
			throw new UnsupportedOperationException("unique name.");
		return util.getDup(getNameStore(), cnvS(name), bi -> cnvL(bi));
	}

	public List<Long> getIdsByTag(String tag) {
		return util.getDup(getTagStore(), cnvS(tag), bi -> cnvL(bi));
	}

	public boolean existByName(String name, Long id) {
		if (isUniqueName()) {
			return getIdByName(name) != null;
		} else {
			return util.getDupSingle(getNameStore(), cnvS(name), cnvL(id),
					bi -> cnvL(bi)) != null;
		}
	}

	/**
	 * @param tag
	 * @param id
	 * @return	このタグとIDが関連付けられているか
	 */
	public boolean existByTag(String tag, Long id) {
		if (tag == null || id == null)
			return false;
		return util.getDupSingle(getTagStore(), cnvS(tag), cnvL(id),
				bi -> cnvL(bi)) != null;
	}

	public final StoreInfo getNameStore() {
		return nameToId;
	}

	public StoreInfo getTagStore() {
		return tagToIds;
	}

	@Override
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getNameStore());
		r.add(getTagStore());
		r.add(getTagIdToId());
		r.addAll(getStoresIndividualityObjectConcrete());
		return r;
	}

	abstract public List<StoreInfo> getStoresIndividualityObjectConcrete();

	@Override
	protected final boolean noExistAdministratedObjectConcrete(T1 o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByName(o.getName(), o.getId())) {
			vr.add(Lang.INDIVIDUALITY_OBJECT_NAME, Lang.ERROR_DB_EXIST);
			b = false;
		}
		HashSet<String> tags = o.getTags();
		if (tags != null) {
			for (String tag : tags) {
				if (existByTag(tag, o.getId())) {
					vr.add(Lang.INDIVIDUALITY_OBJECT_TAGS, Lang.ERROR_DB_EXIST,
							"tag=" + tag);
					b = false;
					break;
				}
			}
		}
		for (Long tagId : o.getTagIds()) {
			if (existByTagId(tagId, o.getId())) {
				vr.add(Lang.INDIVIDUALITY_OBJECT, Lang.TAG_IDS,
						Lang.ERROR_DB_EXIST, "tagId=" + tagId);
				b = false;
				break;
			}
		}
		if (!noExistIndividualityObjectConcrete(o, vr)) {
			b = false;
		}

		return b;
	}

	abstract protected boolean noExistIndividualityObjectConcrete(T1 o,
			ValidationResult vr) throws Exception;

	/**
	 * サジェスト等ラフな情報で良い用途に使う
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

	public Map<String, Long> prefixSearchByTagRough(String prefix, int max) {
		SearchContext<String, Long> ctx = new SearchContext<>(getTagStore(),
				cnvSRemoveSuffix(prefix), bi -> cnvS(bi), bi -> cnvL(bi), true,
				0, max, new PrefixEqual<>(), null, (runtimeCtx, cursor) -> {
					cursor.getSearchKeyRange(cnvS(prefix));
				});
		return util.search(ctx);
	}

	@Override
	protected final boolean updateAdministratedObjectConcrete(T1 updated,
			T1 old) throws Exception {
		if (Glb.getUtil().notEqual(updated.getName(), old.getName())) {
			if (old.getName() != null) {
				if (isUniqueName()) {
					if (!util.remove(getNameStore(), cnvS(old.getName()))) {
						throw new IOException("Failed to remove");
					}
				} else {
					if (!util.deleteDupSingle(getNameStore(),
							cnvS(old.getName()), cnvL(updated.getId()))) {
						throw new IOException("Failed to deleteDupSingle");
					}
				}
			}
			if (!util.put(getNameStore(), cnvS(updated.getName()),
					cnvL(updated.getId())))
				throw new IOException("Failed to put");
		}

		Collection<String> newTags = Glb.getUtil().getExtra(updated.getTags(),
				old.getTags());
		if (newTags != null) {
			for (String tag : newTags) {
				if (!util.put(getTagStore(), cnvS(tag), cnvL(updated.getId())))
					throw new IOException("Failed to put");
			}
		}

		Collection<String> removedTags = Glb.getUtil().getExtra(old.getTags(),
				updated.getTags());
		if (removedTags != null) {
			for (String tag : removedTags) {
				if (!util.deleteDupSingle(getTagStore(), cnvS(tag),
						cnvL(updated.getId())))
					throw new IOException("Failed to put");
			}
		}

		if (Glb.getUtil().notEqual(updated.getTagIds(), old.getTagIds())) {
			//oldから削除された要素一覧
			Collection<Long> removed = null;
			if (updated.getTagIds() == null) {
				//updatedのがnullならoldのは全て削除されたもの
				removed = old.getTagIds();
			} else {
				//oldのがnullならremovedは空
				if (old.getTagIds() != null) {
					//oldにあってupdatedに無い、つまり削除された
					removed = Glb.getUtil().getExtra(old.getTagIds(),
							new HashSet<>(updated.getTagIds()));
				}
			}

			//updatedで追加された要素一覧
			Collection<Long> added = null;
			if (old.getTagIds() == null) {
				//oldのがnullならupdatedのは全て追加されたもの
				removed = updated.getTagIds();
			} else {
				//updatedのがnullならaddedは空
				if (updated.getTagIds() != null) {
					//updatedにあってoldに無い、つまり追加された
					added = Glb.getUtil().getExtra(updated.getTagIds(),
							new HashSet<>(old.getTagIds()));
				}
			}

			//削除された要素があればサブインデックスを削除
			if (removed != null) {
				for (Long fqdn : removed) {
					if (!util.deleteDupSingle(getTagIdToId(), cnvL(fqdn),
							cnvL(old.getId())))
						return false;
				}
			}

			//追加された要素があればサブインデックスを追加
			if (added != null) {
				for (Long tagId : added) {
					if (!util.put(getTagIdToId(), cnvL(tagId),
							cnvL(old.getId()))) {
						return false;
					}
				}
			}

		}

		if (!updateIndividualityObjectConcrete(updated, old))
			return false;
		return true;
	}

	abstract protected boolean updateIndividualityObjectConcrete(T1 updated,
			T1 old) throws Exception;

}
