package bei7473p5254d69jcuat.tenyu.communication.request.server.urlprovementserver;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.server.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * URL証明サーバが証明を確認した時にメッセージ受付サーバに送信する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class URLProved extends ServerObjectivityMessage {
	/**
	 * 証明対象のURL
	 * 予めストアに登録されている前提
	 */
	private Long provedWebId;
	/**
	 * そのURLの管理者であることを証明したユーザーのID
	 */
	private Long proverUserId;

	@Override
	public String toString() {
		return URLProved.class.getSimpleName() + " provedWebId=" + provedWebId
				+ " proverUserId=" + proverUserId;
	}

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		WebStore ws = new WebStore(txn);
		Web w = ws.get(provedWebId);
		if (w == null)
			return false;
		SocialityStore sos = new SocialityStore(txn);
		Sociality so = sos.getByIndividualityObject(new TenyuReferenceModelSimple<>(provedWebId, StoreNameObjectivity.WEB));
		if (so == null)
			return false;

		//個性系オブジェクトの登録者及び管理者を更新
		w.setRegistererUserId(proverUserId);
		w.setMainAdministratorUserId(proverUserId);

		//社会性の登録者及び管理者を更新
		so.setRegistererUserId(proverUserId);
		so.setMainAdministratorUserId(proverUserId);

		if (!ws.update(w)) {
			throw new Exception();
		}
		if (!sos.update(so)) {
			throw new Exception();
		}

		return true;
	}

	@Override
	protected boolean validateServerObjectivityMessageConcrete(Message m) {
		if (!Model.validateIdStandardNotSpecialId(provedWebId))
			return false;
		if (!Model.validateIdStandardNotSpecialId(proverUserId))
			return false;

		return true;
	}

	@Override
	protected List<Long> getServers() {
		Role r = Glb.getObje().getRole(
				rs -> rs.getByName(URLProvementServer.class.getSimpleName()));
		if (r == null)
			return null;
		return r.getAdminUserIds();
	}

}
