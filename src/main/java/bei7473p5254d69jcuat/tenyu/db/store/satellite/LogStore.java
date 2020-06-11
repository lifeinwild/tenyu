package bei7473p5254d69jcuat.tenyu.db.store.satellite;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.ModelStore.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * historyIndex : object
 * 客観を作るデータ、メッセージリストや分散合意の結果等を記録する
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <V>
 */
public abstract class LogStore<V extends ValidatableI>
		extends ByNodeStore<Long, V> {

	public LogStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected ByteIterable cnvKey(Long key) {
		return cnvL(key);
	}

	@Override
	protected Long cnvKey(ByteIterable bi) {
		return cnvL(bi);
	}

	/*
		public boolean create(long historyIndex, V o) throws Exception {
			return putDirect(cnvL(historyIndex), cnvO(o));
		}

		public boolean update(long historyIndex, V o) throws Exception {
			return putDirect(cnvL(historyIndex), cnvO(o));
		}

		public boolean delete(long historyIndex) throws Exception {
			return deleteDirect(cnvL(historyIndex));
		}
	*/
	protected static <R> R simpleReadAccess(StoreFunction<Transaction, R> f) {
		return simpleReadAccess(Glb.getFile().getLogDBDir(), f);
	}

	protected static <R> R simpleAccess(StoreFunction<Transaction, R> f) {
		return simpleAccess(Glb.getFile().getLogDBDir(), f);
	}

}
