package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor.P2PDefenseGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class P2PDefenseGui extends
		IdObjectGui<P2PDefenseDBI,
				P2PDefense,
				P2PDefense,
				P2PDefenseStore,
				P2PDefenseGui,
				P2PDefenseTableItem> {
	public P2PDefenseGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class P2PDefenseTableItem
			extends IdObjectTableItem<P2PDefenseDBI, P2PDefense> {

		public P2PDefenseTableItem(P2PDefense src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected P2PDefenseTableItem createTableItem(P2PDefense o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected P2PDefenseGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public P2PDefenseStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
