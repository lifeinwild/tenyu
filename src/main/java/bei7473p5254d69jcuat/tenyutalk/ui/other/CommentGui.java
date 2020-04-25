package bei7473p5254d69jcuat.tenyutalk.ui.other;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import glb.*;
import jetbrains.exodus.env.*;

public class CommentGui extends
		AdministratedObjectGui<CommentI,
				Comment,
				Comment,
				CommentStore,
				CommentGui,
				CommentTableItem> {

	public CommentGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected CommentGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected CommentTableItem createTableItem(Comment o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public CommentStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
