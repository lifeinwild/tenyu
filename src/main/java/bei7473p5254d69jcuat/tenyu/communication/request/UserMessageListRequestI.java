package bei7473p5254d69jcuat.tenyu.communication.request;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.SignedPackage.*;
import jetbrains.exodus.env.*;

/**
 * {@link UserMessageList}の要素になりうるメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface UserMessageListRequestI extends SignedPackageContent {
	/**
	 * メッセージリスト反映処理から呼び出される。
	 * 呼ばれる前に検証される。検証はメッセージリスト受信時に行われている。
	 *
	 * 例外が投げられるとその回の全てのUserRightMessageの反映処理が失敗する。
	 * falseなら他のメッセージは反映される。
	 * 書き込み処理そのもので例外が出る可能性もあるが、大多数のノードで起こらないなら問題ない。
	 *
	 * @param txn				その回の他のUserRightMessageと共通のトランザクション
	 * @param historyIndex		その回のhistoryIndex
	 * @return					成功したか
	 */
	boolean apply(Transaction txn, long historyIndex) throws Exception;
}
