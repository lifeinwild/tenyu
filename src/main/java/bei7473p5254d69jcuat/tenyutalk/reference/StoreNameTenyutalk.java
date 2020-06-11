package bei7473p5254d69jcuat.tenyutalk.reference;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import glb.*;
import jetbrains.exodus.env.*;

public enum StoreNameTenyutalk implements StoreNameEnum {
	TENYUTALK_GITREPOSITORY(
			txn -> new TenyutalkGitRepositoryStore(txn),
			TenyutalkGitRepositoryStore.modelName),
	TENYUTALK_ARTIFACT(
			txn -> new TenyutalkArtifactStore(txn),
			TenyutalkArtifactStore.modelName),
	COMMENT(txn -> new CommentStore(txn), CommentStore.modelName),;

	private final Function<Transaction,
			ObjectStore<?, ? extends ValidatableI>> getStore;

	private final String modelname;

	private StoreNameTenyutalk(
			Function<Transaction,
					ObjectStore<?, ? extends ValidatableI>> getStore,
			String modelname) {
		this.getStore = getStore;
		this.modelname = modelname;
	}

	public StoreNameTenyutalk getRandom() {
		int max = StoreNameTenyutalk.values().length - 1;
		int i = Glb.getRnd().nextInt(max);
		return StoreNameTenyutalk.values()[i];
	}

	public ObjectStore<?, ? extends ValidatableI> getStore(Transaction txn) {
		return getStore.apply(txn);
	}

	@Override
	public String getModelName() {
		return modelname;
	}
}
