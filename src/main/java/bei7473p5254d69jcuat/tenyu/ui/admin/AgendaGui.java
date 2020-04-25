package bei7473p5254d69jcuat.tenyu.ui.admin;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.*;
import bei7473p5254d69jcuat.tenyu.ui.admin.AgendaGui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import jetbrains.exodus.env.*;

public class AgendaGui extends
		IndividualityObjectGui<AgendaI,
				Agenda,
				Agenda,
				AgendaStore,
				AgendaGui,
				AgendaTableItem> {
	public AgendaGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class AgendaTableItem
			extends IndividualityObjectTableItem<AgendaI, Agenda> {

		public AgendaTableItem(Agenda src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected AgendaTableItem createTableItem(Agenda o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected AgendaGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public AgendaStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
