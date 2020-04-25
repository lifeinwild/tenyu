package bei7473p5254d69jcuat.tenyu.ui.standarduser.web;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.web.WebGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class WebGui extends
		IndividualityObjectGui<WebI, Web, Web, WebStore, WebGui, WebTableItem> {
	public WebGui(String name, String id) {
		super(name, id);
	}

	public static class WebTableItem extends IndividualityObjectTableItem<WebI, Web> {

		public WebTableItem(Web src) {
			super(src);
		}

	}

	@Override
	protected WebTableItem createTableItem(Web o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected WebGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public WebStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
