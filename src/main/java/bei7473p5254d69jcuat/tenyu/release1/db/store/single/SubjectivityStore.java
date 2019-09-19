package bei7473p5254d69jcuat.tenyu.release1.db.store.single;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class SubjectivityStore extends SingleObjectStore<Subjectivity> {
	private static final String modelName = Subjectivity.class.getSimpleName();

	public SubjectivityStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected boolean createIdObjectConcrete(Subjectivity created) throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return modelName;
	}

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	@Override
	protected Subjectivity chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Subjectivity)
				return (Subjectivity) o;
			throw new InvalidTargetObjectTypeException(
					"not Subjectivity object in SubjectivityStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.Subjectivity)
			return true;
		return false;
	}
}
