package bei7473p5254d69jcuat.tenyutalk.model.promise.other;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyutalk.*;

/**
 * 創作物{@link CreativeObjectI}につけられるコメント
 *
 * {@link ChatMessageI}との違いは、
 * 他のコメントへの返信として作成できる事と評価（いいね）できる事、
 * カテゴリが無い事。
 *
 * {@link Tenyutalk}側のDBに保存される。
 *
 * ノード別管理だがメイン系列のモデルを使用している。
 * {@link AdministratedObjectI}はもともとObjectivityObjectという名前であり
 * それ以下の全クラスは客観である事が想定された。
 * 実際このモデルを除けばそれは達成されている。
 * しかしこのモデルはその規則性を破壊している。
 * とはいえコード上無理な負担は生じていない。
 * というのは、メイン系列のモデルやストアクラスをTenyutalk側DBに保存する事は、
 * 新たなコード上の特殊な対応無くできている。
 *
 * 将来的に客観DBの更新性能が十分に高まれば、
 * コメントを客観上で扱う設計が考えられる。
 * コンテンツを見る前にそのコメントが分かる、という事。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@LocalHosting
public interface CommentI extends AdministratedObjectI {
	/**
	 * ツリーの最大次数
	 */
	public static final int parentMax = 10;
	/**
	 * コメントの最大長
	 */
	public static final int contentMax = 2000;
	/**
	 * 高評価の最大数
	 */
	public static final int goodMax = 1000 * 1000 * 1000;
	/**
	 * 低評価の最大数
	 */
	public static final int badMax = 1000 * 1000 * 1000;

	/**
	 * @return	返信先
	 */
	CommentI getParent();

	/**
	 * @return	内容
	 */
	String getContent();

	/**
	 * @return	高評価したユーザーIDリスト
	 */
	List<Long> getGoodUserIds();

	/**
	 * @param userId	新たに高評価したユーザーのID
	 * @return	追加に成功したか
	 */
	boolean addGoodUserId(Long userId);

	/**
	 * @param userId	高評価を辞めたユーザーのID
	 * @return	削除に成功したか
	 */
	boolean removeGoodUserId(Long userId);

	/**
	 * @return	低評価したユーザーIDリスト
	 */
	List<Long> getBadUserIds();

	/**
	 * @param userId	新たに低評価したユーザーのID
	 * @return	追加に成功したか
	 */
	boolean addBadUserId(Long userId);

	/**
	 * @param userId	低評価を辞めたユーザーのID
	 * @return	削除に成功したか
	 */
	boolean removeBadUserId(Long userId);

	/**
	 * @return	親コメントのID。これによってコメントはツリー構造を取る
	 */
	Long getParentCommentId();

	void setGoodUserIds(List<Long> goodUserIds);

	void setBadUserIds(List<Long> badUserIds);

	void setContent(String content);

	void setParentCommentId(Long parentCommentId);

	/**
	 * @return	どの創作物へのコメントか
	 */
	Long getCreativeObjectId();

	void setCreativeObjectId(Long creativeObjectId);
}
