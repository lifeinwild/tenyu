package bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame.StaticGameMaterialClassGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class StaticGameMaterialClassGui extends
		IndividualityObjectGui<StaticGameMaterialClassI,
				StaticGameMaterialClass,
				StaticGameMaterialClass,
				StaticGameMaterialClassStore,
				StaticGameMaterialClassGui,
				GameMaterialClassTableItem> {
	public StaticGameMaterialClassGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static class GameMaterialClassTableItem
			extends IndividualityObjectTableItem<StaticGameMaterialClassI,
					StaticGameMaterialClass> {

		public GameMaterialClassTableItem(StaticGameMaterialClass src) {
			super(src);
			// TODO 自動生成されたコンストラクター・スタブ
		}

	}

	@Override
	protected GameMaterialClassTableItem createTableItem(
			StaticGameMaterialClass o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected StaticGameMaterialClassGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public StaticGameMaterialClassStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
