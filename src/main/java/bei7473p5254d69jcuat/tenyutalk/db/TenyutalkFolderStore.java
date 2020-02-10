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
		extends CreativeObjectStore<TenyutalkFolderDBI, TenyutalkFolder> {
	public static final String modelName = TenyutalkFolder.class
			.getSimpleName();

	public TenyutalkFolderStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean createVersionedConcrete(TenyutalkFolderDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateVersionedConcrete(
			TenyutalkFolderDBI updated, TenyutalkFolderDBI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteVersionedConcrete(TenyutalkFolderDBI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existVersionedConcrete(TenyutalkFolderDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public List<StoreInfo> getStoresVersionedConcrete() {
		return new ArrayList<>();
	}

	@Override
	protected boolean noExistVersionedConcrete(TenyutalkFolderDBI o,
			ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	protected boolean updateVersionedConcrete(TenyutalkFolderDBI updated,
			TenyutalkFolderDBI old) throws Exception {
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
