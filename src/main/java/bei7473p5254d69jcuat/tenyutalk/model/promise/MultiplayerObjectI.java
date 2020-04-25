package bei7473p5254d69jcuat.tenyutalk.model.promise;

import java.util.concurrent.locks.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.*;

/**
 * 多人数参加コンテンツ
 *
 * WWWの置き換えのため、
 * 配信や掲示板のような多人数参加コンテンツを実現するための概念。
 *
 * WWWを置き換えるには画像や動画などのファイルを扱えるクラス（{@link CreativeObjectI}）と
 * 多人数参加コンテンツを扱えるクラスがあれば十分と判断した。
 *
 * 多くの場合多人数参加コンテンツの活動結果としてログが作成される。
 * 例えば配信の活動結果としてチャットログつきの動画が作成される。
 * 当初はこの観点を強調してMemoryObjectとしていた。
 *
 * この概念の名前の他の候補はPeoplewareとかRealtimeとかSessionとかMultiCreativeとかだった。
 * Peoplewareは、もっとマクロな観点だと思うので却下した。
 * Realtimeは、掲示板や配信等はリアルタイム性があると思ったが、リアルタイムというほど素早い
 * レスポンスではない場合（特に掲示板で）もあるので却下した。
 * Sessionは、プログラミング界隈では他の意味で使われているので却下した。
 * MultiCreativeは、副産物であるログを目的にやるかのような印象を受けるので却下した。
 * 結局、Multiplayerで多人数参加という本質を強調できると考えた。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <L>	ログのクラス
 *
 */
@LocalHosting
public interface MultiplayerObjectI extends CreativeObjectI {
	/**
	 * 管理者しか見れない情報など、情報の非対称性を実現する。
	 *
	 * データを見たいユーザーはホストにリクエストするが、
	 * 常にこのインターフェースを通じて返信が作成されるので
	 * ユーザーに応じて見れる情報を制限できる。
	 *
	 * @param requestor
	 * @return	リクエストしたユーザーに応じて一部情報が排除されたこのオブジェクトのコピーを返す
	 */
	MultiplayerObjectI getCopyForCommunication(User requestor);

	/**
	 * {@link MultiplayerObjectI}は開催中オンメモリで管理され、終了または中断時DBに記録される。
	 * @return	DBへの記録に成功したか
	 */
	boolean save();

	/**
	 * オンメモリ管理におけるスレッドセーフのため{@link ReentrantLock}を使用する。
	 * その機能はバグが生まれやすく極力使用すべきでないとされているが、
	 * ロックの競合が多発する状況において性能が高い。
	 * https://www.ibm.com/developerworks/jp/java/library/j-jtp10264/
	 * @return
	 */
	ReentrantLock getLock();
}
