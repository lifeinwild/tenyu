package bei7473p5254d69jcuat.tenyu.model.promise.objectivity;

import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;

/**
 * {@link Model}でなければ{@link HashStore}が機能しないので
 * DBに記録されるという特徴から特定のインターフェースが要求される
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface AdministratedObjectI extends ModelI {
	/**
	 * @return	この情報を客観に登録したユーザーのID
	 */
	Long getRegistererUserId();

	void setRegistererUserId(Long registererUserId);

	Long getMainAdministratorUserId();

	void setMainAdministratorUserId(Long mainAdministratorUserId);

	/**
	 * @return	メイン管理者を変更可能か
	 */
	default boolean isMainAdministratorChangable() {
		return false;
	}
}
