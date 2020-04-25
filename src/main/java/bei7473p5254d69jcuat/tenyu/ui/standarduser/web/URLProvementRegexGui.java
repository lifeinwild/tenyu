package bei7473p5254d69jcuat.tenyu.ui.standarduser.web;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.web.URLProvementRegexGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class URLProvementRegexGui extends
		IndividualityObjectGui<URLProvementRegexI,
				URLProvementRegex,
				URLProvementRegex,
				URLProvementRegexStore,
				URLProvementRegexGui,
				URLProvementRegexTableItem> {
	public URLProvementRegexGui(String name, String id) {
		super(name, id);
	}

	public static class URLProvementRegexTableItem extends
			IndividualityObjectTableItem<URLProvementRegexI, URLProvementRegex> {

		public URLProvementRegexTableItem(URLProvementRegex src) {
			super(src);
		}

	}

	@Override
	protected URLProvementRegexTableItem createTableItem(URLProvementRegex o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected URLProvementRegexGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public URLProvementRegexStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
