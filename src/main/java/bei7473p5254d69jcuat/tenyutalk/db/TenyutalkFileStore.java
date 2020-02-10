package bei7473p5254d69jcuat.tenyutalk.db;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class TenyutalkFileStore
		extends CreativeObjectStore<TenyutalkFileDBI, TenyutalkFile> {
	public static final String modelName = TenyutalkFile.class.getSimpleName();

	public TenyutalkFileStore(Transaction txn) {
		super(txn);
	}

	@Override
	public boolean isUniqueName() {
		return false;
	}

	@Override
	protected boolean createVersionedConcrete(TenyutalkFileDBI o) throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean dbValidateAtUpdateVersionedConcrete(TenyutalkFileDBI updated, TenyutalkFileDBI old,
			ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean deleteVersionedConcrete(TenyutalkFileDBI o) throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean existVersionedConcrete(TenyutalkFileDBI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	public List<StoreInfo> getStoresVersionedConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	protected boolean noExistVersionedConcrete(TenyutalkFileDBI o, ValidationResult vr)
			throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean updateVersionedConcrete(TenyutalkFileDBI updated, TenyutalkFileDBI old)
			throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof TenyutalkFileStore;
	}

	@Override
	protected TenyutalkFile chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof TenyutalkFile)
				return (TenyutalkFile) o;
			throw new InvalidTargetObjectTypeException(
					"not TenyutalkFile object in TenyutalkFileStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

}
