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

public class FlowNetworkAbstractNominalStore
		extends NaturalityStore<FlowNetworkAbstractNominalDBI,
				FlowNetworkAbstractNominal> {
	public static final String modelName = FlowNetworkAbstractNominal.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static FlowNetworkAbstractNominal getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <
			R> R simple(Function<FlowNetworkAbstractNominalStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new FlowNetworkAbstractNominalStore(txn)));
	}
	*/

	public FlowNetworkAbstractNominalStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected FlowNetworkAbstractNominal chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof FlowNetworkAbstractNominal)
				return (FlowNetworkAbstractNominal) o;
			throw new InvalidTargetObjectTypeException(
					"not Web object in WebStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(
			FlowNetworkAbstractNominalDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(
			FlowNetworkAbstractNominalDBI updated,
			FlowNetworkAbstractNominalDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteNaturalityConcrete(
			FlowNetworkAbstractNominalDBI o) throws Exception {
		return true;
	}

	@Override
	protected boolean existNaturalityConcrete(
			FlowNetworkAbstractNominalDBI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.FlowNetworkAbstractNominalDBI)
			return true;
		return false;
	}

	@Override
	protected boolean noExistNaturalityConcrete(
			FlowNetworkAbstractNominalDBI o, ValidationResult vr)
			throws Exception {
		return true;
	}

	@Override
	protected boolean updateNaturalityConcrete(
			FlowNetworkAbstractNominalDBI updated,
			FlowNetworkAbstractNominalDBI old) throws Exception {
		return true;
	}

}
