package bei7473p5254d69jcuat.tenyutalk.db;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * repositoryId : artifact
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TenyutalkArtifactStore
		extends ByNodeStore<String, TenyutalkArtifact> {
	public static final String modelName = TenyutalkArtifact.class
			.getSimpleName();

	public TenyutalkArtifactStore(Transaction txn) {
		super(txn);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected TenyutalkArtifact chainversionup(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected String cnvKey(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected ByteIterable cnvKey(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected List<StoreInfo> getStoresObjectStoreConcrete() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
