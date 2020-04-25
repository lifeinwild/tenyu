package bei7473p5254d69jcuat.tenyu.ui.admin;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.ui.admin.RoleGui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import jetbrains.exodus.env.*;

public class RoleGui extends
		IndividualityObjectGui<RoleI, Role, Role, RoleStore, RoleGui, RoleTableItem> {
	public RoleGui(String name, String id) {
		super(name, id);
	}

	public static class RoleTableItem
			extends IndividualityObjectTableItem<RoleI, Role> {

		public RoleTableItem(Role src) {
			super(src);
		}

	}

	@Override
	protected RoleTableItem createTableItem(Role o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected RoleGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public RoleStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
