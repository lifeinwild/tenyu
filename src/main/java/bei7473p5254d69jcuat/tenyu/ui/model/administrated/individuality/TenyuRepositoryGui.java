package bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import jetbrains.exodus.env.*;

public class TenyuRepositoryGui extends
		IndividualityObjectGui<TenyuRepositoryI,
				TenyuRepository,
				TenyuRepository,
				TenyuRepositoryStore,
				TenyuRepositoryGui,
				TenyuRepositoryTableItem> {

	public TenyuRepositoryGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected TenyuRepositoryGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected TenyuRepositoryTableItem createTableItem(TenyuRepository o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuRepositoryStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
