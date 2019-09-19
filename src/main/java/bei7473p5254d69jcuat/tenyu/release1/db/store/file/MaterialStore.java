package bei7473p5254d69jcuat.tenyu.release1.db.store.file;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class MaterialStore extends UploadFileStore<MaterialDBI, Material> {
	public static final String modelName = Material.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static Material getByNameSimple(String name) {
		return simple((s) -> {
			Long id = s.getIdByName(name.trim());
			if (id == null)
				return null;
			return s.get(id);
		});
	}

	public static Material getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<MaterialStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new MaterialStore(txn)));
	}
	*/

	public MaterialStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Material chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Material)
				return (Material) o;
			throw new InvalidTargetObjectTypeException(
					"not Material object in MaterialStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createSubUploadFileConcrete(MaterialDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateUploadFileConcrete(MaterialDBI updated,
			MaterialDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteSubUploadFileConcrete(MaterialDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existSubUploadFileConcrete(MaterialDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	protected List<StoreInfo> getStoresUploadFileConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof MaterialDBI;
	}

	@Override
	protected boolean noExistSubUploadFileConcrete(MaterialDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateSubUploadFileConcrete(MaterialDBI updated,
			MaterialDBI old) throws Exception {
		return true;
	}

}
