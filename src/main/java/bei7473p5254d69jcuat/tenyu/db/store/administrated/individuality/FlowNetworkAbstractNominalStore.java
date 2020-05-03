package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class FlowNetworkAbstractNominalStore
		extends IndividualityObjectStore<FlowNetworkAbstractNominalI,
				FlowNetworkAbstractNominal> {
	public static final String modelName = FlowNetworkAbstractNominal.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

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
	protected boolean createIndividualityObjectConcrete(FlowNetworkAbstractNominalI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			FlowNetworkAbstractNominalI updated,
			FlowNetworkAbstractNominalI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(FlowNetworkAbstractNominalI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(FlowNetworkAbstractNominalI o,
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
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.FlowNetworkAbstractNominalI)
			return true;
		return false;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(FlowNetworkAbstractNominalI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(
			FlowNetworkAbstractNominalI updated,
			FlowNetworkAbstractNominalI old) throws Exception {
		return true;
	}

}
