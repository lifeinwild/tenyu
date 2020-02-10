package bei7473p5254d69jcuat.tenyu.ui.admin;

import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.admin.ObjectivityCoreGui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import jetbrains.exodus.env.*;

public class ObjectivityCoreGui extends
		IndividualityObjectGui<ObjectivityCoreDBI,
				ObjectivityCore,
				ObjectivityCore,
				ObjectivityCoreStore,
				ObjectivityCoreGui,
				ObjectivityCoreTableItem> {
	public ObjectivityCoreGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class ObjectivityCoreTableItem
			extends IndividualityObjectTableItem<ObjectivityCoreDBI, ObjectivityCore> {

		public ObjectivityCoreTableItem(ObjectivityCore src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected ObjectivityCoreTableItem createTableItem(ObjectivityCore o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected ObjectivityCoreGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public ObjectivityCoreStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
