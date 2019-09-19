package bei7473p5254d69jcuat.tenyu.release1.db;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * IdObjectでなければHashStoreが機能しないので
 * DBに記録されるという特徴から特定のインターフェースが要求される
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface ObjectivityObjectDBI extends IdObjectDBI {
	/**
	 * @return	この情報を客観に登録したユーザーのID
	 */
	Long getRegistererUserId();

	void setRegistererUserId(Long registererUserId);

	Long getMainAdministratorUserId();

	void setMainAdministratorUserId(Long mainAdministratorUserId);

	/**
	 * @return	作成時期
	 */
	//	public Long getCreatedHistoryIndex();

	//	public void setCreatedHistoryIndex(Long createdHistoryIndex);

	/**
	 * @return	更新時期
	 */
	//	public Long getUpdatedHistoryIndex();

	//	public void setUpdatedHistoryIndex(Long updatedHistoryIndex);

}
