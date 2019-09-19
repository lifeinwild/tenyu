package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class AgendaProcStore extends DelayRunStore<AgendaProcDBI, AgendaProc> {
	public static final String modelName = AgendaProc.class.getSimpleName();

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public AgendaProcStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected AgendaProc chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof AgendaProc)
				return (AgendaProc) o;
			throw new InvalidTargetObjectTypeException(
					"not AgendaProc object in AgendaProcStore");
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
		return o instanceof AgendaProcDBI;
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
