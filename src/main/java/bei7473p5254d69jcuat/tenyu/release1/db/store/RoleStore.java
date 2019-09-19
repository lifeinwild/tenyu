package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.role.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RoleStore extends NaturalityStore<RoleDBI, Role> {
	public static final String modelName = Role.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}
/*
	public static List<Role> getAllValuesSimple() {
		return simple((s) -> s.getAllValues());
	}

	public static Role getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	public static Role getSimple(String roleName) {
		return simple((s) -> s.getByName(roleName));
	}

	private static <R> R simple(Function<RoleStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new RoleStore(txn)));
	}
*/

	public RoleStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Role chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Role)
				return (Role) o;
			throw new InvalidTargetObjectTypeException(
					"not Role object in RoleStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(RoleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(RoleDBI updated,
			RoleDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteNaturalityConcrete(RoleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existNaturalityConcrete(RoleDBI o, ValidationResult vr)
			throws Exception {
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
		return o instanceof RoleDBI;
	}

	@Override
	protected boolean noExistNaturalityConcrete(RoleDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateNaturalityConcrete(RoleDBI updated, RoleDBI old)
			throws Exception {
		return true;
	}
}
