package bei7473p5254d69jcuat.tenyu.model.release1.reference;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import jetbrains.exodus.env.*;

/**
 * SingleのStoreName
 * これらはHashStore等のサブストアを持たない
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum StoreNameSingle implements StoreNameEnum {
	OBJECTIVITY_CORE(
			txn -> new ObjectivityCoreStore(txn),
			ObjectivityCoreStore.modelName),
	SUBJECTIVITY(
			txn -> new SubjectivityStore(txn),
			SubjectivityStore.modelName),
	MIDDLE(txn -> new MiddleStore(txn), MiddleStore.modelName),
	P2P_DEFENSE(txn -> new P2PDefenseStore(txn), P2PDefenseStore.modelName),;
	private final Function<Transaction,
			ModelStore<? extends ModelI, ?>> getStore;
	private final String modelname;

	private StoreNameSingle(
			Function<Transaction, ModelStore<? extends ModelI, ?>> getStore,
			String modelname) {
		this.getStore = getStore;
		this.modelname = modelname;
	}

	public ModelStore<? extends ModelI, ?> getStore(Transaction txn) {
		return getStore.apply(txn);
	}

	@Override
	public String getModelName() {
		return modelname;
	}
}
