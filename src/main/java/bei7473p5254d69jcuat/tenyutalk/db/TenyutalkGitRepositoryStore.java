package bei7473p5254d69jcuat.tenyutalk.db;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class TenyutalkGitRepositoryStore
		extends TenyutalkRepositoryStore<TenyutalkGitRepository> {
	public static final String modelName = TenyutalkGitRepository.class
			.getSimpleName();

	public TenyutalkGitRepositoryStore(Transaction txn) {
		super(txn);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected TenyutalkGitRepository chainversionup(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected Long cnvKey(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected ByteIterable cnvKey(Long key) {
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
