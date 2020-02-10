package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.role.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RoleStore extends IndividualityObjectStore<RoleDBI, Role> {
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

	public RoleStore(Transaction txn) {
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
	protected boolean createIndividualityObjectConcrete(RoleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(RoleDBI updated,
			RoleDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(RoleDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(RoleDBI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RoleDBI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(RoleDBI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(RoleDBI updated, RoleDBI old)
			throws Exception {
		return true;
	}

}
