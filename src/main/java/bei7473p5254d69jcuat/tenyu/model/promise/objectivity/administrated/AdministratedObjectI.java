package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;

/**
 * 「特定の{@link UserI}のみが管理するオブジェクト」が本質。
 * 管理者や登録者が設定されるオブジェクト。
 * 管理者はこのオブジェクトの更新権限等を持つ。
 *
 * 客観に記録されるオブジェクトは必ずこの抽象クラスを継承している。
 * さらにTenyutalk上で扱われるコンテンツも同様。
 *
 * 元の名前はObjectivityObjectであり、これを継承する全クラスが
 * 客観オブジェクトであると想定していた。
 * しかし、内容（メンバー変数）に基づくより堅実な名前に変更した。
 *
 * TODO:メンバー変数にHashMap等コンテナ系クラスがあった場合、
 * 同じ値が同じ順序で入力されるなら、
 * どの環境でもシリアライズ時のbyte[]は同値になるか？
 * それが保証されないなら、それを保証するコンテナ実装で置き換える事になる。
 * これは最大の問題だが調査するのに時間がかかりそうで保留している。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface AdministratedObjectI extends ModelI {
	/**
	 * 基本的に、この情報を登録したユーザーのID。
	 * しかし{@link Web}などで最初の登録者から変更される場合がある。
	 * 客観コア、抽象ノード名目等一部のオブジェクトは特殊な登録者が設定される。
	 *
	 * @return	この情報を客観に登録したユーザーのID
	 */
	Long getRegistererUserId();

	void setRegistererUserId(Long registererUserId);
	/**
	 * 管理者のユーザーID
	 *
	 * 登録者と異なり交代する可能性がある。
	 *
	 * この管理者以外にもこの客観オブジェクトを管理可能なユーザーが存在する
	 * 場合もある。この管理者はメイン管理者と考えられる。
	 *
	 * 管理者は当然この客観オブジェクトの編集権限を持つ。
	 * しかしこの管理者と社会性の管理者は、多くの場合同じだが、データとしては分けられている。
	 */
	Long getMainAdministratorUserId();

	void setMainAdministratorUserId(Long mainAdministratorUserId);

	/**
	 * @return	メイン管理者を変更可能か
	 */
	default boolean isMainAdministratorChangable() {
		return false;
	}
}
