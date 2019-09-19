package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style.StyleGui.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class StyleGui extends
		NaturalityGui<StyleDBI,
				Style,
				Style,
				StyleStore,
				StyleGui,
				StyleTableItem> {
	public static class StyleTableItem
			extends NaturalityTableItem<StyleDBI, Style> {

		public StyleTableItem(Style src) {
			super(src);
		}

	}

	@Override
	protected StyleGui createDetailGui() {
		return new StyleGui(Lang.DETAIL.toString(), idPrefix + detail);
	}

	@Override
	protected StyleTableItem createTableItem(Style o) {
		return new StyleTableItem(o);
	}

	public StyleGui(String name, String id) {
		super(name, id);
	}

	@Override
	public GridPane buildDelete(Style exist) {
		super.buildDelete(exist);

		return grid;
	}

	@Override
	public GridPane buildUpdate(Style exist) {
		super.buildUpdate(exist);

		set(exist);

		getExplanationInput().setPromptText(Lang.STYLE_EXP_PROMPT.toString());
		getNameInput().setPromptText(Lang.STYLE_NAME_PROMPT.toString());

		return grid;
	}

	@Override
	public GridPane buildSearch(SearchFuncs<StyleDBI, Style> sf) {
		//総件数
		buildCount(DBUtil.countStatic(StyleStore.getMainStoreInfoStatic()));

		super.buildSearch(sf);

		return grid;
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		getExplanationInput().setPromptText(Lang.STYLE_EXP_PROMPT.toString());
		getNameInput().setPromptText(Lang.STYLE_NAME_PROMPT.toString());

		return grid;
	}

	@Override
	public void set(Style n) {
		super.set(n);
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.STYLE;
	}

	@Override
	public StyleStore getStore(Transaction txn) {
		try {
			return new StyleStore(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

}