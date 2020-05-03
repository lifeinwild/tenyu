package bei7473p5254d69jcuat.tenyutalk.db.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class CommentStore extends AdministratedObjectStore<CommentI, Comment> {
	public static final String modelName = Comment.class.getSimpleName();
	private static final StoreInfo parentIdToId = new StoreInfo(
			modelName + "_parentIdToId_Dup", StoreConfig.WITH_DUPLICATES);

	public CommentStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected boolean createAdministratedObjectConcrete(CommentI o)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			CommentI updated, CommentI old, ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(CommentI o)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean existAdministratedObjectConcrete(CommentI o,
			ValidationResult vr) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected List<StoreInfo> getStoresAdministratedObjectConcrete() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean noExistAdministratedObjectConcrete(CommentI o,
			ValidationResult vr) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(CommentI updated,
			CommentI old) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isSupport(Object o) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected Comment chainversionup(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
