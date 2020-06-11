package bei7473p5254d69jcuat.tenyutalk.db;

import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import jetbrains.exodus.env.*;

/**
 * repositoryId : {@link TenyutalkGitRepository}
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class TenyutalkRepositoryStore<V extends TenyutalkRepository>
		extends ByNodeStore<Long, V> {

	public TenyutalkRepositoryStore(Transaction txn) {
		super(txn);
		// TODO 自動生成されたコンストラクター・スタブ
	}

}
