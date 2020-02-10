package glb.util;

import java.util.function.*;

import glb.*;
import glb.Glb.*;
import jetbrains.exodus.env.*;

public interface DBObj {
	default <T> T readTryW(ThrowableFunction<Transaction, T> getStore) {
		return readRet(txn -> {
			try {
				return getStore.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	default <T> T writeTryW(ThrowableFunction<Transaction, T> getStore) {
		return compute(txn -> {
			try {
				return getStore.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	/**
	 * 読み取り専用トランザクションでDBから読み取る
	 * @param dbProc
	 * @return
	 */
	default <T> T readRet(Function<Transaction, T> dbProc) {
		return getEnv().computeInReadonlyTransaction(txn -> dbProc.apply(txn));
	}

	Environment getEnv();

	/**
	 * DBに指定された処理を行う。
	 *
	 * @param dbProc	処理内容
	 * @return	処理内容によって規定される返値
	 */
	default <T> T compute(Function<Transaction, T> dbProc) {
		return getEnv().computeInTransaction(txn -> dbProc.apply(txn));
	}

	default void execute(Consumer<Transaction> dbProc) {
		getEnv().executeInTransaction(txn -> dbProc.accept(txn));
	}

	default void read(Consumer<Transaction> dbProc) {
		getEnv().executeInReadonlyTransaction(txn -> dbProc.accept(txn));
	}
}
