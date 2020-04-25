package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * URL証明のサイト固有管理者領域を特定するための正規表現を格納するDB
 * @author exceptiontenyu@gmail.com
 *
 */
public class URLProvementRegexStore
		extends IndividualityObjectStore<URLProvementRegexI, URLProvementRegex> {
	public static final String modelName = URLProvementRegex.class
			.getSimpleName();

	/**
	 * List型かつ重複キーという珍しいタイプのサブインデックス
	 *
	 * List型と言っているのはURLProvementRegexのfqdnsがListであるということ。
	 * 重複キーは1個のfqdnが多数のURLProvementRegexに対応づく場合があるという事。
	 *
	 * WEBサイトは複数のドメインが同じサイトを指す場合があり、
	 * さらに1つのサイトが複数のページを持ちいずれのページでも
	 * URL証明が可能にするので、
	 * FQDN : URLProvementRegexは n:mである。
	 * FQDNはURLProvementRegexをある程度絞り込み、
	 * 最終的にURLProvementRegexがURLを調べて対応するURLか決定する。
	 */
	private static final StoreInfo fqdnToId = new StoreInfo(
			modelName + "_fqdnToId_Dup", StoreConfig.WITH_DUPLICATES, true);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public URLProvementRegexStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected URLProvementRegex chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof URLProvementRegex)
				return (URLProvementRegex) o;
			throw new InvalidTargetObjectTypeException(
					"not URLProvementRegex object in URLProvementRegexStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(URLProvementRegexI o)
			throws Exception {
		for (String fqdn : o.getFqdns()) {
			if (!util.put(fqdnToId, cnvS(fqdn), cnvL(o.getId()))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			URLProvementRegexI updated, URLProvementRegexI old,
			ValidationResult r) {
		boolean b = true;
		Collection<String> added = Glb.getUtil().getExtra(updated.getFqdns(),
				old.getFqdns());
		for (String fqdn : added) {
			if (existByFqdn(fqdn, updated.getId())) {
				r.add(Lang.URLPROVEMENT_REGEX_FQDN, Lang.ERROR_DB_EXIST,
						"fqdn=" + fqdn);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(URLProvementRegexI o)
			throws Exception {
		for (String fqdn : o.getFqdns()) {
			if (!util.deleteDupSingle(fqdnToId, cnvS(fqdn),
					cnvL(o.getId())))
				return false;
		}
		return true;
	}

	public boolean existByFqdn(String fqdn, Long rId) {
		if (fqdn == null || rId == null)
			return false;
		return util.getDupSingle(fqdnToId, cnvS(fqdn), cnvL(rId),
				(bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(URLProvementRegexI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (String fqdn : o.getFqdns()) {
			if (!existByFqdn(fqdn, o.getId())) {
				vr.add(Lang.URLPROVEMENT_REGEX_FQDN, Lang.ERROR_DB_NOTFOUND,
						"fqdn=" + fqdn + " id=" + o.getId());
				b = false;
				break;
			}
		}
		return b;
	}

	/**
	 * @param fqdn
	 * @return	該当したオブジェクト一覧。非null
	 */
	public List<URLProvementRegex> getByFqdn(String fqdn) {
		List<URLProvementRegex> r = new ArrayList<>();
		List<Long> ids = getIdsByFqdn(fqdn);
		if (ids == null || ids.size() == 0)
			return r;

		for (Long id : ids) {
			r.add(get(id));
		}

		return r;
	}

	/**
	 * FQDNで正規表現を検索
	 * @param fqdn
	 * @return
	 */
	public List<Long> getIdsByFqdn(String fqdn) {
		return util.getDup(fqdnToId, cnvS(fqdn), bi -> cnvL(bi));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(fqdnToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof URLProvementRegexI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(URLProvementRegexI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (String fqdn : o.getFqdns()) {
			if (existByFqdn(fqdn, o.getId())) {
				vr.add(Lang.URLPROVEMENT_REGEX_FQDN, Lang.ERROR_DB_EXIST);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(URLProvementRegexI updated,
			URLProvementRegexI old) throws Exception {
		//fqdnsについて変化があるか。順序が違うだけで変化があったとみなされる
		if (Glb.getUtil().notEqual(updated.getFqdns(), old.getFqdns())) {
			//以下、差分を見出して処理するのでfqdnsの順序の違いは影響しない

			//oldから削除された要素一覧
			Collection<String> removed = null;
			if (updated.getFqdns() == null) {
				//updatedのがnullならoldのは全て削除されたもの
				removed = old.getFqdns();
			} else {
				//oldのがnullならremovedは空
				if (old.getFqdns() != null) {
					//oldにあってupdatedに無い、つまり削除された
					removed = Glb.getUtil().getExtra(old.getFqdns(),
							new HashSet<>(updated.getFqdns()));
				}
			}

			//updatedで追加された要素一覧
			Collection<String> added = null;
			if (old.getFqdns() == null) {
				//oldのがnullならupdatedのは全て追加されたもの
				removed = updated.getFqdns();
			} else {
				//updatedのがnullならaddedは空
				if (updated.getFqdns() != null) {
					//updatedにあってoldに無い、つまり追加された
					added = Glb.getUtil().getExtra(updated.getFqdns(),
							new HashSet<>(old.getFqdns()));
				}
			}

			//削除された要素があればサブインデックスを削除
			if (removed != null) {
				for (String fqdn : removed) {
					if (!util.deleteDupSingle(fqdnToId, cnvS(fqdn),
							cnvL(old.getId())))
						return false;
				}
			}

			//追加された要素があればサブインデックスを追加
			if (added != null) {
				for (String fqdn : added) {
					if (!util.put(fqdnToId, cnvS(fqdn),
							cnvL(old.getId()))) {
						return false;
					}
				}
			}

		}

		return true;
	}

}
