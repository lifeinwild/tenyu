package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * ここに蓄積されるハッシュ値に対応するメッセージリストと
 * 分散合意の結果一覧があれば客観をネットワーク勃興時から再現できる
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserMessageListHashStore extends
		ObjectivityObjectStore<UserMessageListHashDBI, UserMessageListHash> {

	public static final String modelName = UserMessageListHash.class
			.getSimpleName();
	private static final StoreInfo historyIndexToId = new StoreInfo(
			modelName + "_historyIndexToId");

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public UserMessageListHashStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected UserMessageListHash chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof UserMessageListHash)
				return (UserMessageListHash) o;
			throw new InvalidTargetObjectTypeException(
					"not UserMessageListHash object in UserMessageListHashStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createObjectivityObjectConcrete(
			UserMessageListHashDBI o) throws Exception {
		return util.put(historyIndexToId, cnvL(o.getHistoryIndex()),
				cnvL(o.getRecycleId()));
	}

	@Override
	protected boolean dbValidateAtUpdateObjectivityObjectConcrete(
			UserMessageListHashDBI updated, UserMessageListHashDBI old,
			ValidationResult r) {
		boolean b = true;
		if (Glb.getUtil().notEqual(updated.getHistoryIndex(),
				old.getHistoryIndex())) {
			if (getIdByHistoryIndex(updated.getHistoryIndex()) != null) {
				r.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX,
						Lang.ERROR_DB_EXIST);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteObjectivityObjectConcrete(
			UserMessageListHashDBI h) throws Exception {
		return util.remove(historyIndexToId, cnvL(h.getHistoryIndex()));
	}

	@Override
	public boolean existObjectivityObjectConcrete(UserMessageListHashDBI o,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByHistoryIndex(o.getHistoryIndex()) == null) {
			vr.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX,
					Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	public Long getIdByHistoryIndex(Long historyIndex) {
		return getId(historyIndexToId, cnvL(historyIndex));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresObjectivityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(historyIndexToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.UserMessageListHashDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistObjectivityObjectConcrete(UserMessageListHashDBI o,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByHistoryIndex(o.getHistoryIndex()) != null) {
			vr.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateObjectivityObjectConcrete(
			UserMessageListHashDBI updated, UserMessageListHashDBI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getHistoryIndex(),
				old.getHistoryIndex())) {
			if (!util.remove(historyIndexToId, cnvL(old.getHistoryIndex())))
				return false;
			if (!util.put(historyIndexToId, cnvL(updated.getHistoryIndex()),
					cnvL(updated.getRecycleId())))
				return false;
		}
		return true;
	}
}
