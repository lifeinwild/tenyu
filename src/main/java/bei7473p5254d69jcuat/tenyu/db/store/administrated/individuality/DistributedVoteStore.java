package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class DistributedVoteStore
		extends IndividualityObjectStore<DistributedVoteI, DistributedVote> {
	public static final String modelName = DistributedVote.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public DistributedVoteStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected DistributedVote chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof DistributedVote)
				return (DistributedVote) o;
			throw new InvalidTargetObjectTypeException(
					"not DistributedVote object in DistributedVoteStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createIndividualityObjectConcrete(DistributedVoteI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			DistributedVoteI updated, DistributedVoteI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(DistributedVoteI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(DistributedVoteI o,
			ValidationResult vr) throws Exception {
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
		return o instanceof DistributedVoteI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(DistributedVoteI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	public boolean put(DistributedVote o) throws Exception {
		if (o == null || o.getId() == null)
			return false;
		return putDirect(cnvL(o.getId()), cnvO(o));
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(DistributedVoteI updated,
			DistributedVoteI old) throws Exception {
		return true;
	}
}
