package bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame.StaticGameGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class StaticGameGui extends
		IndividualityObjectGui<StaticGameI,
				StaticGame,
				StaticGame,
				StaticGameStore,
				StaticGameGui,
				StaticGameTableItem> {
	public StaticGameGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class StaticGameTableItem
			extends IndividualityObjectTableItem<StaticGameI, StaticGame> {

		public StaticGameTableItem(StaticGame src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected StaticGameTableItem createTableItem(StaticGame o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected StaticGameGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public StaticGameStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
