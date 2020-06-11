package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * WEBページノードをDBに読み書きする
 * @author exceptiontenyu@gmail.com
 *
 */
public class WebStore extends IndividualityObjectStore<WebI, Web> {
	public static final String modelName = Web.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return ModelStore.getMainStoreInfoStatic(modelName);
	}

	public static List<StoreInfo> getWebStoresStatic() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	public WebStore(Transaction txn) {
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
	protected boolean createIndividualityObjectConcrete(WebI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(WebI updated,
			WebI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(WebI u) throws Exception {
		return true;
	}

	@Override
	public boolean existIndividualityObjectConcrete(WebI url, ValidationResult vr) {
		return true;
	}

	public Long getIdByURL(String url) {
		return getIdByName(url);
	}

	@Override
	public String getName() {
		return modelName;
	}

	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		return getWebStoresStatic();
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.WebI)
			return true;
		return false;
	}

	@Override
	public boolean noExistIndividualityObjectConcrete(WebI url, ValidationResult vr) {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(WebI updated, WebI old)
			throws Exception {
		if (updated.getUrl() == null) {
			return false;
		}

		return true;
	}

}
