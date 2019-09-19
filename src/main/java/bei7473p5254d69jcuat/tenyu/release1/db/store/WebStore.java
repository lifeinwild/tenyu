package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * WEBページノードをDBに読み書きする
 * @author exceptiontenyu@gmail.com
 *
 */
public class WebStore extends NaturalityStore<WebDBI, Web> {
	public static final String modelName = Web.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return IdObjectStore.getMainStoreInfoStatic(modelName);
	}

	/*
		public static Web getSimple(Long id) {
			return simple((s) -> s.get(id));
		}

		private static <R> R simple(Function<WebStore, R> f) {
			return IdObjectStore
					.simpleReadAccess((txn) -> f.apply(new WebStore(txn)));
		}
		*/
	public static List<StoreInfo> getWebStoresStatic() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	public WebStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Web chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Web)
				return (Web) o;
			throw new InvalidTargetObjectTypeException(
					"not Web object in WebStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(WebDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(WebDBI updated,
			WebDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteNaturalityConcrete(WebDBI u) throws Exception {
		return true;
	}

	@Override
	public boolean existNaturalityConcrete(WebDBI url, ValidationResult vr) {
		return true;
	}

	public Long getIdByURL(String url) {
		return getIdByName(url);
	}

	@Override
	public String getName() {
		return modelName;
	}

	public List<StoreInfo> getStoresNaturalityConcrete() {
		return getWebStoresStatic();
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.WebDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistNaturalityConcrete(WebDBI url, ValidationResult vr) {
		return true;
	}

	@Override
	protected boolean updateNaturalityConcrete(WebDBI updated, WebDBI old)
			throws Exception {
		if (updated.getUrl() == null) {
			return false;
		}

		return true;
	}

}
