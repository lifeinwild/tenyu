package bei7473p5254d69jcuat.tenyutalk.reference;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import jetbrains.exodus.env.*;

public enum StoreNameTenyutalk implements StoreNameEnum {
	TENYUTALK_FILE(
			txn -> new TenyutalkFileStore(txn),
			TenyutalkFileStore.modelName),
	TENYUTALK_FOLDER(
			txn -> new TenyutalkFolderStore(txn),
			TenyutalkFolderStore.modelName),
	COMMENT(txn -> new CommentStore(txn), CommentStore.modelName),;

	private final Function<Transaction,
			IdObjectStore<? extends IdObjectI, ?>> getStore;

	private final String modelname;

	private StoreNameTenyutalk(
			Function<Transaction,
					IdObjectStore<? extends IdObjectI, ?>> getStore,
			String modelname) {
		this.getStore = getStore;
		this.modelname = modelname;
	}

	public IdObjectStore<? extends IdObjectI, ?> getStore(Transaction txn) {
		return getStore.apply(txn);
	}

	@Override
	public String getModelName() {
		return modelname;
	}
}
