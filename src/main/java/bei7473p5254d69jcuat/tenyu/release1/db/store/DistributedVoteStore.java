package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.vote.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class DistributedVoteStore
		extends NaturalityStore<DistributedVoteDBI, DistributedVote> {
	public static final String modelName = DistributedVote.class
			.getSimpleName();

	/*
	public static List<DistributedVote> getAllSimple() {
		return simple((s) -> s.getAllValues());
	}
	*/

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static DistributedVote getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<DistributedVoteStore, R> f) {
		return IdObjectStore.simpleReadAccess(
				(txn) -> f.apply(new DistributedVoteStore(txn)));
	}
	*/

	public DistributedVoteStore(Transaction txn)
			throws NoSuchAlgorithmException {
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
	protected boolean createNaturalityConcrete(DistributedVoteDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(
			DistributedVoteDBI updated, DistributedVoteDBI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteNaturalityConcrete(DistributedVoteDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existNaturalityConcrete(DistributedVoteDBI o,
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
		return o instanceof DistributedVoteDBI;
	}

	@Override
	protected boolean noExistNaturalityConcrete(DistributedVoteDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	public boolean put(DistributedVote o) throws Exception {
		if (o == null || o.getRecycleId() == null)
			return false;
		return putDirect(cnvL(o.getRecycleId()), cnvO(o));
	}

	@Override
	protected boolean updateNaturalityConcrete(DistributedVoteDBI updated,
			DistributedVoteDBI old) throws Exception {
		return true;
	}
}
