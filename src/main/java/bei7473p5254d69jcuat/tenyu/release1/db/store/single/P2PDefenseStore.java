package bei7473p5254d69jcuat.tenyu.release1.db.store.single;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class P2PDefenseStore extends SingleObjectStore<P2PDefense> {
	private static final String modelName = P2PDefense.class.getSimpleName();

	public P2PDefenseStore(Transaction txn) throws NoSuchAlgorithmException {
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
	protected P2PDefense chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof P2PDefense)
				return (P2PDefense) o;
			throw new InvalidTargetObjectTypeException(
					"not P2PDefense object in P2PDefenseStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.communication.P2PDefense)
			return true;
		return false;
	}
}
