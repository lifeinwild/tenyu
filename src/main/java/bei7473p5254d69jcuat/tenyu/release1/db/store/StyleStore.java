package bei7473p5254d69jcuat.tenyu.release1.db.store;

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

public class StyleStore extends NaturalityStore<StyleDBI, Style> {
	public static final String modelName = Style.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static Style getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<StyleStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new StyleStore(txn)));
	}
	*/

	public StyleStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Style chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Style)
				return (Style) o;
			throw new InvalidTargetObjectTypeException(
					"not Style object in StyleStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(StyleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(StyleDBI updated,
			StyleDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteNaturalityConcrete(StyleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existNaturalityConcrete(StyleDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof StyleDBI;
	}

	@Override
	protected boolean noExistNaturalityConcrete(StyleDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateNaturalityConcrete(StyleDBI updated,
			StyleDBI old) throws Exception {
		return true;
	}
}
