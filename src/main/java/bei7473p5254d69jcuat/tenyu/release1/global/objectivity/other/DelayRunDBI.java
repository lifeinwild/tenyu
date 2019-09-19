package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import jetbrains.exodus.env.*;

public interface DelayRunDBI extends ObjectivityObjectDBI, Unreferenciable {
	long getRunHistoryIndex();

	/**
	 * 遅延実行の処理内容。
	 * 検証処理は内部で行う想定。
	 * 引数に渡されたトランザクションにおいて、
	 * thisは遅延実行ストアから削除済みになっている。
	 *
	 * @return	処理に成功したか
	 * @throws Exception	例外が発生したか。呼び出し側でトランザクションが破棄される。
	 */
	boolean run(Transaction txn) throws Exception;

	/**
	 * @return	実行の重さ。ユーザーメッセージの標準的な重さを1として設定する
	 */
	default int getApplySize() {
		return 1;
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
