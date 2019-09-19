package bei7473p5254d69jcuat.tenyu.release1.db.store.single;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class MiddleStore extends SingleObjectStore<Middle> {
	private static final String modelName = Middle.class.getSimpleName();

	public MiddleStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.middle.Middle)
			return true;
		return false;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	protected Middle chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Middle)
				return (Middle) o;
			throw new InvalidTargetObjectTypeException(
					"not Middle object in SubjectivityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

}
