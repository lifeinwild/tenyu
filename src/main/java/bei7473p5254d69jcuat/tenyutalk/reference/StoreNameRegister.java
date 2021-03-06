package bei7473p5254d69jcuat.tenyutalk.reference;

import com.esotericsoftware.kryo.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * {@link Kryo#register(Class)}
 * でストアクラスが登録済みのストアを参照する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StoreNameRegister implements StoreName {
	/**
	 * {@link Registration#getId()}
	 */
	private int registerId;

	@Override
	public ObjectStore<?, ?> getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getModelName() {
		Registration reg = Glb.getKryoForTenyutalk()
				.getRegistration(getRegisterId());
		if (reg == null)
			return null;
		return reg.getClass().getName();
	}

	public int getRegisterId() {
		return registerId;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return validateCommon(r);
	}
}
