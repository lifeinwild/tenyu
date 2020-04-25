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

public class TenyutalkFolderStore
		extends CreativeObjectStore<TenyutalkFolderI, TenyutalkFolder> {
	public static final String modelName = TenyutalkFolder.class
			.getSimpleName();

	public TenyutalkFolderStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean createVersionedConcrete(TenyutalkFolderI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateVersionedConcrete(
			TenyutalkFolderI updated, TenyutalkFolderI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteVersionedConcrete(TenyutalkFolderI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existVersionedConcrete(TenyutalkFolderI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public List<StoreInfo> getStoresVersionedConcrete() {
		return new ArrayList<>();
	}

	@Override
	protected boolean noExistVersionedConcrete(TenyutalkFolderI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateVersionedConcrete(TenyutalkFolderI updated,
			TenyutalkFolderI old) throws Exception {
		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof TenyutalkFolder;
	}

	@Override
	protected TenyutalkFolder chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof TenyutalkFolder)
				return (TenyutalkFolder) o;
			throw new InvalidTargetObjectTypeException(
					"not TenyutalkFolder object in TenyutalkFolderStore");
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
