package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * あるモデルを作るときに必ず別のモデルを作る必要があるなど、
 * モデル作成はシーケンスになる場合がある。
 * そのうち客観に関するロジックをここに書く。
 *
 * ・ユーザー物理削除の仕様について
 * もし異常データのみを作成したユーザーなら、
 * ユーザーごと物理削除してそのユーザーが登録した全てのデータを物理削除する。
 * もし正常データのみを作成したユーザーなら、
 * BANはありえても物理削除は無い。
 * もし異常データと正常データを作成したユーザーなら、
 * BANで留めて、異常データを選択的に物理削除する。
 *
 * 選択的削除は難しいが、管理ツールが完成すれば可能。
 * 削除されるデータがどこかから参照されていたりすると問題が出るので、
 * そのような事が無いようチェックする必要がある。
 * 管理ツールによってそのようなチェックを行う。
 * 管理ツールもモデルクラスを意識した複雑な設計を持つだろう。
 *
 * 各IdObjectはID削除通知を受け取れる必要がある
 * その通知に対してIDを単に削除するか削除済みIDへ置き換えるかは各モデルクラスで定義
 *
 * 運営上の削除更新計画の要素一覧
 * まず問題のある全てのユーザーをBANする
 * このメソッドによるユーザー削除とそれに伴う社会性削除
 * 削除するIDとストア名の一覧
 * ID削除通知を出すオブジェクトのID一覧
 *
 * ユーザーID参照個所一覧。これらへの対応もしないと、削除後に参照元オブジェクトがDB検証を通過しなくなる
 * 対応は、参照元オブジェクトの削除または参照の削除または削除済みIDへの変換になるだろう
 *
 * Role系の管理者ID
 * 登録者
 * {@link ManagedGame}の運営者
 * Coreの全体運営者
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivitySequence {
	public static boolean deleteSequence(Transaction txn, IndividualityObject n,
			IndividualityObjectStore<?, ?> ns, NodeType type) throws Exception {
		if (n == null) {
			Glb.getLogger().warn("IndividualityObject is null");
			return false;
		}

		//個性系オブジェクト
		if (!ns.delete(n.getId()))
			throw new IllegalStateException();

		//社会性
		SocialityStore ss = new SocialityStore(txn);
		Long sId = ss.getIdByIndividualityObject(type, n.getId());
		if (!ss.delete(sId))
			throw new IllegalStateException();
		return true;
	}

	/**
	 * 個性系オブジェクト登録に続き社会性も登録する。コミットは行われない
	 * @param txn			外部で作成、コミットされるトランザクション
	 * @param n				登録されるユーザー情報
	 * @param specifiedId	IDが外部で指定される場合。
	 * @param admin 		Userかつnullの場合、作成された個性系オブジェクトのIDを用いる。
	 * 						その他nullの場合、nullIdを用いる。
	 * @param registerer	nullならnの登録者が使われる。Userかつnullの場合、作成されたUserのIDが使用される
	 * @return				作成処理に成功したか
	 * @throws Exception
	 */
	public static <T1 extends IndividualityObjectI,
			T2 extends T1> boolean createSequence(Transaction txn, T1 n,
					boolean specifiedId, long historyIndex,
					IndividualityObjectStore<T1, T2> ns, Long admin, Long registerer,
					NodeType type) throws Exception {
		if (n == null)
			return false;
		//個性系オブジェクト登録
		Long nid = null;
		if (specifiedId) {
			nid = ns.createSpecifiedId(n);
		} else {
			nid = ns.create(n);
		}
		if (nid == null)
			throw new IllegalStateException();

		if (type == NodeType.USER && admin == null)
			admin = nid;

		//この場合管理者不在になるので、後から管理者を設定する手段が用意されている必要がある
		if (admin == null)
			admin = IdObjectI.getNullId();

		if (type == NodeType.USER && registerer == null)
			registerer = nid;

		if (registerer == null)
			registerer = n.getRegistererUserId();

		//社会性登録
		Sociality sociality = new Sociality(historyIndex);
		sociality.setMainAdministratorUserId(admin);
		sociality.setBanned(false);
		sociality.setFlowFromCooperativeAccount(0);
		sociality.setType(type);
		sociality.setIndividualityObjectConcreteId(nid);
		sociality.setRegistererUserId(registerer);
		SocialityStore s2 = new SocialityStore(txn);
		Long socialityId = s2.create(sociality);
		if (socialityId == null)
			throw new IllegalStateException();

		return true;
	}

}
