package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RoleStore extends IndividualityObjectStore<RoleI, Role> {
	public static final String modelName = Role.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

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
	protected boolean createIndividualityObjectConcrete(RoleI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(RoleI updated,
			RoleI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(RoleI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(RoleI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RoleI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(RoleI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(RoleI updated, RoleI old)
			throws Exception {
		return true;
	}

}
