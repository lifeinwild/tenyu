package bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.GuildGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class GuildGui extends
		IndividualityObjectGui<GuildI,
				Guild,
				Guild,
				GuildStore,
				GuildGui,
				GuildTableItem> {
	public static class GuildTableItem
			extends IndividualityObjectTableItem<GuildI, Guild> {

		public GuildTableItem(Guild src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	public GuildGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected GuildGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected GuildTableItem createTableItem(Guild o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public GuildStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
