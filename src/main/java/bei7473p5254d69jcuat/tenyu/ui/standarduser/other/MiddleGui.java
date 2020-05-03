package bei7473p5254d69jcuat.tenyu.ui.standarduser.other;

import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.other.MiddleGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class MiddleGui extends
		ModelGui<MiddleI,
				Middle,
				Middle,
				MiddleStore,
				MiddleGui,
				MiddleTableItem> {
	public MiddleGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class MiddleTableItem
			extends ModelTableItem<MiddleI, Middle> {

		public MiddleTableItem(Middle src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected MiddleTableItem createTableItem(Middle o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected MiddleGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public MiddleStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
