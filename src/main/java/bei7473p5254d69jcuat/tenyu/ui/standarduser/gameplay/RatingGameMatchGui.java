package bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay.RatingGameMatchGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class RatingGameMatchGui extends
		AdministratedObjectGui<RatingGameMatchI,
				RatingGameMatch,
				RatingGameMatch,
				RatingGameMatchStore,
				RatingGameMatchGui,
				RatingGameMatchTableItem> {
	public RatingGameMatchGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class RatingGameMatchTableItem extends
			AdministratedObjectTableItem<RatingGameMatchI, RatingGameMatch> {

		public RatingGameMatchTableItem(RatingGameMatch src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected RatingGameMatchTableItem createTableItem(RatingGameMatch o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected RatingGameMatchGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public RatingGameMatchStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
