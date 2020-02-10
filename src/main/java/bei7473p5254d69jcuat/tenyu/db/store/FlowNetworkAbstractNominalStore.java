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

public class FlowNetworkAbstractNominalStore
		extends IndividualityObjectStore<FlowNetworkAbstractNominalDBI,
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

	public FlowNetworkAbstractNominalStore(Transaction txn) {
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
	protected boolean createIndividualityObjectConcrete(FlowNetworkAbstractNominalDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			FlowNetworkAbstractNominalDBI updated,
			FlowNetworkAbstractNominalDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(FlowNetworkAbstractNominalDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(FlowNetworkAbstractNominalDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.FlowNetworkAbstractNominalDBI)
			return true;
		return false;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(FlowNetworkAbstractNominalDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(
			FlowNetworkAbstractNominalDBI updated,
			FlowNetworkAbstractNominalDBI old) throws Exception {
		return true;
	}

}
