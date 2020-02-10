package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor.SubjectivityGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class SubjectivityGui extends
		IdObjectGui<SubjectivityDBI,
				Subjectivity,
				Subjectivity,
				SubjectivityStore,
				SubjectivityGui,
				SubjectivityTableItem> {

	public static class SubjectivityTableItem
			extends IdObjectTableItem<SubjectivityDBI, Subjectivity> {

		public SubjectivityTableItem(Subjectivity src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	public SubjectivityGui(String name, String cssIdPrefix) {
		super(name, cssIdPrefix);
	}

	@Override
	public void set(Subjectivity o) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	protected SubjectivityTableItem createTableItem(Subjectivity o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected SubjectivityGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public SubjectivityStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
