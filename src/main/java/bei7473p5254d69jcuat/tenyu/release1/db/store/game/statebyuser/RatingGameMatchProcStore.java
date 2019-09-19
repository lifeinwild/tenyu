package bei7473p5254d69jcuat.tenyu.release1.db.store.game.statebyuser;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class RatingGameMatchProcStore
		extends DelayRunStore<RatingGameMatchProcDBI, RatingGameMatchProc> {
	public static final String modelName = RatingGameMatchProc.class
			.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public RatingGameMatchProcStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected RatingGameMatchProc chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof RatingGameMatchProc)
				return (RatingGameMatchProc) o;
			throw new InvalidTargetObjectTypeException(
					"not RatingApply object in RatingApplyStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createDelayRunConcrete(DelayRunDBI o) {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateDelayRunConcrete(DelayRunDBI updated,
			DelayRunDBI old, ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteDelayRunConcrete(DelayRunDBI o) {
		return true;
	}

	@Override
	protected ValidationResult existDelayRunConcrete(DelayRunDBI o,
			ValidationResult vr) {
		return vr;
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresDelayRunConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof RatingGameMatchProcDBI;
	}

	@Override
	protected boolean noExistDelayRunConcrete(DelayRunDBI o,
			ValidationResult vr) {
		return true;
	}

	@Override
	protected boolean updateDelayRunConcrete(DelayRunDBI updated,
			DelayRunDBI old) {
		return true;
	}

}
