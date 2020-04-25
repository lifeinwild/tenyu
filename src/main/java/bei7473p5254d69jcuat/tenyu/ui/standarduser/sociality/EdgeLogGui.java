package bei7473p5254d69jcuat.tenyu.ui.standarduser.sociality;

import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.sociality.EdgeLogGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class EdgeLogGui extends
		AdministratedObjectGui<EdgeLogI,
				EdgeLog,
				EdgeLog,
				EdgeLogStore,
				EdgeLogGui,
				EdgeLogTableItem> {
	public EdgeLogGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class EdgeLogTableItem
			extends AdministratedObjectTableItem<EdgeLogI, EdgeLog> {

		public EdgeLogTableItem(EdgeLog src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected EdgeLogTableItem createTableItem(EdgeLog o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected EdgeLogGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public EdgeLogStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
