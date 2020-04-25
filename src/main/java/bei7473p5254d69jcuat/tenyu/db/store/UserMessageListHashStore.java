package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import glb.*;
import glb.util.*;
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
		AdministratedObjectStore<UserMessageListHashI, UserMessageListHash> {

	public static final String modelName = UserMessageListHash.class
			.getSimpleName();
	private static final StoreInfo historyIndexToId = new StoreInfo(
			modelName + "_historyIndexToId");

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	public UserMessageListHashStore(Transaction txn) {
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
	protected boolean createAdministratedObjectConcrete(UserMessageListHashI o)
			throws Exception {
		return util.put(historyIndexToId, cnvL(o.getHistoryIndex()),
				cnvL(o.getId()));
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			UserMessageListHashI updated, UserMessageListHashI old,
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
	protected boolean deleteAdministratedObjectConcrete(UserMessageListHashI h)
			throws Exception {
		return util.remove(historyIndexToId, cnvL(h.getHistoryIndex()));
	}

	@Override
	public boolean existAdministratedObjectConcrete(UserMessageListHashI o,
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
	public List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(historyIndexToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.UserMessageListHashI)
			return true;
		return false;
	}

	@Override
	public boolean noExistAdministratedObjectConcrete(UserMessageListHashI o,
			ValidationResult vr) {
		boolean b = true;
		if (getIdByHistoryIndex(o.getHistoryIndex()) != null) {
			vr.add(Lang.USERMESSAGELISTHASH_HISTORYINDEX, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(
			UserMessageListHashI updated, UserMessageListHashI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getHistoryIndex(),
				old.getHistoryIndex())) {
			if (!util.remove(historyIndexToId, cnvL(old.getHistoryIndex())))
				return false;
			if (!util.put(historyIndexToId, cnvL(updated.getHistoryIndex()),
					cnvL(updated.getId())))
				return false;
		}
		return true;
	}

}
