package bei7473p5254d69jcuat.tenyu.ui.model.administrated;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.TenyuArtifactByVersionGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class TenyuArtifactByVersionGui extends
		AdministratedObjectGui<TenyuArtifactByVersionI,
				TenyuArtifactByVersion,
				TenyuArtifactByVersion,
				TenyuArtifactByVersionStore,
				TenyuArtifactByVersionGui,
				TenyuArtifactByVersionTableItem> {

	public TenyuArtifactByVersionGui(String guiName, String cssIdPrefix) {
		super(guiName, cssIdPrefix);
	}

	public static class TenyuArtifactByVersionTableItem
			extends AdministratedObjectTableItem<TenyuArtifactByVersionI,
					TenyuArtifactByVersion> {

		public TenyuArtifactByVersionTableItem(TenyuArtifactByVersion src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}
	}
	/*
	 * 		T2 extends T1,
		T3 extends AdministratedObject,
		S extends AdministratedObjectStore<T1, T2>,
		G extends AdministratedObjectGui,
		TI extends AdministratedObjectTableItem<T1, T2>>

	 *
	 */

	@Override
	protected TenyuArtifactByVersionGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected TenyuArtifactByVersionTableItem createTableItem(
			TenyuArtifactByVersion o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactByVersionStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
