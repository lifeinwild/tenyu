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

public class AvatarStore extends FileConceptStore<AvatarDBI, Avatar> {
	public static final String modelName = Avatar.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}
/*
	public static Avatar getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<AvatarStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new AvatarStore(txn)));
	}
*/
	public AvatarStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Avatar chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Avatar)
				return (Avatar) o;
			throw new InvalidTargetObjectTypeException(
					"not Avatar object in AvatarStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createDynamicFileConcrete(AvatarDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateFileConceptConcrete(AvatarDBI updated,
			AvatarDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteDynamicFileConcrete(AvatarDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existDynamicFileConcrete(AvatarDBI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	protected List<StoreInfo> getStoresFileConceptConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof AvatarDBI;
	}

	@Override
	protected boolean noExistDynamicFileConcrete(AvatarDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateDynamicFileConcrete(AvatarDBI updated,
			AvatarDBI old) throws Exception {
		return true;
	}
}
