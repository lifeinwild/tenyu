package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right;

import jetbrains.exodus.env.*;

/**
 * 1つ1つのユーザーメッセージや他モジュールから登録された処理は
 * このインターフェースを実装する必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface ObjectivityUpdateDataElement
		extends Comparable<ObjectivityUpdateDataElement> {
	/**
	 * @param txn
	 * @param nextHistoryIndex
	 * @return	メッセージの内容で客観DBを更新する
	 * @throws Exception	例外が発生したらトランザクションが破棄される
	 */
	boolean apply(Transaction txn, long nextHistoryIndex) throws Exception;

	/**
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
