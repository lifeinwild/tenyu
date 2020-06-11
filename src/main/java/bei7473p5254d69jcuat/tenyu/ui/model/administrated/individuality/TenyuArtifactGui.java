package bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import jetbrains.exodus.env.*;

public class TenyuArtifactGui extends
		IndividualityObjectGui<TenyuArtifactI,
				TenyuArtifact,
				TenyuArtifact,
				TenyuArtifactStore,
				TenyuArtifactGui,
				TenyuArtifactTableItem> {

	public TenyuArtifactGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected TenyuArtifactGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected TenyuArtifactTableItem createTableItem(TenyuArtifact o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
