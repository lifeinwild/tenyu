package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.role;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.dtradable.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;

public interface RoleDBI extends IndividualityObjectDBI {

	/**
	 * 初期権限一覧
	 * アプリケーション起動時にこれらの名前に対応するRoleオブジェクトが
	 * DBにセットされる。
	 *
	 * DBI系は基本的に不変でなければならないが、
	 * 初期ロール名はバージョンアップに伴って追加しても問題無い。
	 * 削除はDBの修正が必要になるが短時間で終わる。
	 */
	public static final RoleInitialNames roleInitialNames = new RoleInitialNames();

	/**
	 * interfaceでstatic blockが使えないので作成した。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class RoleInitialNames {
		private List<String> names = new ArrayList<>();

		public RoleInitialNames() {
			names.add(DistributedTradable.class.getSimpleName());
			names.add(FreeKVPair.class.getSimpleName());
			names.add(RatingGameMatchingServer.class.getSimpleName());
			names.add(RatingGame.class.getSimpleName());
			names.add(StaticGame.class.getSimpleName());
			names.add(URLProvementServer.class.getSimpleName());
			names.add(UserMessageListServer.class.getSimpleName());
			names.add(UserAddrServer.class.getSimpleName());
		}

		public List<String> getNames() {
			return names;
		}
	}

}
