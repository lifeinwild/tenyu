package bei7473p5254d69jcuat.tenyu.ui.other;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.other.FreeKVPairGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class FreeKVPairGui extends
		AdministratedObjectGui<FreeKVPairI,
				FreeKVPair,
				FreeKVPair,
				FreeKVPairStore,
				FreeKVPairGui,
				FreeKVPairTableItem> {
	public FreeKVPairGui(String name, String id) {
		super(name, id);
	}

	public static class FreeKVPairTableItem
			extends AdministratedObjectTableItem<FreeKVPairI, FreeKVPair> {

		public FreeKVPairTableItem(FreeKVPair src) {
			super(src);
		}

	}

	@Override
	protected FreeKVPairTableItem createTableItem(FreeKVPair o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected FreeKVPairGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public FreeKVPairStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
