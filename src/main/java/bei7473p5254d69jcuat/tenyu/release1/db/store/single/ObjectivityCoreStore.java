package bei7473p5254d69jcuat.tenyu.release1.db.store.single;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class ObjectivityCoreStore extends SingleObjectStore<ObjectivityCore> {
	private static final String modelName = ObjectivityCore.class
			.getSimpleName();

	public ObjectivityCoreStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	public String getName() {
		return modelName;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	protected ObjectivityCore chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof ObjectivityCore)
				return (ObjectivityCore) o;
			throw new InvalidTargetObjectTypeException(
					"not PlatformObjectivity object in PlatformObjectivityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.ObjectivityCore)
			return true;
		return false;
	}
}
