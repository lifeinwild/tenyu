package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.role;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.urlprovement.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.dtradable.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;

public interface RoleI extends IndividualityObjectI {

	/**
	 * 初期権限一覧
	 * アプリケーション起動時にこれらの名前に対応する{@link Role}オブジェクトが
	 * DBにセットされる。
	 *
	 * インターフェースは基本的に不変でなければならないが、
	 * 初期ロール名はinterfaceに定義されているが
	 * バージョンアップに伴って追加しても問題無い。
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
