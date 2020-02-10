package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import glb.*;
import jetbrains.exodus.env.*;

/**
 * 客観を更新するオブジェクトが実装すべきインターフェース
 *
 * UserMessageListの要素であるユーザーメッセージや他モジュールから登録された処理は
 * このインターフェースを実装する必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface ObjectivityUpdateDataElement
		extends Comparable<ObjectivityUpdateDataElement>, ChainVersionup {
	/**
	 * @param txn
	 * @param nextHistoryIndex	このHIで更新されたという事になる
	 * @return	メッセージの内容で客観DBを更新する
	 * @throws Exception	例外が発生したらトランザクションが破棄される
	 */
	boolean apply(Transaction txn, long nextHistoryIndex) throws Exception;

	/**
	 * このメソッドは純粋P2P型を想定したもので、
	 * メッセージ受付サーバを置いている現状使用する事はない。
	 *
	 * 必ず偶数分を境目に判定する
	 * @return	メッセージがネットワーク全体に行き渡るのに十分な時間が経過したか
	 */
	boolean isDiffused();

	/**
	 * @return	拡散を開始した日時
	 */
	long getCreateDate();

	/**
	 * 古過ぎた場合無効なメッセージとみなす
	 * @return	古過ぎるメッセージか
	 */
	boolean isOld();

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

	@Override
	default int compareTo(ObjectivityUpdateDataElement o) {
		if (o == null)
			return 1;

		//クラス名比較
		String o2 = o.getClass().getName();
		String o1 = this.getClass().getName();
		if (!o1.equals(o2))
			return o1.compareTo(o2);

		//作成日時比較
		int date = Long.compare(getCreateDate(), o.getCreateDate());
		if (date != 0)
			return date;

		//子クラスでオーバーライドして子クラスの値で比較を継続する
		return 0;
	}
}
