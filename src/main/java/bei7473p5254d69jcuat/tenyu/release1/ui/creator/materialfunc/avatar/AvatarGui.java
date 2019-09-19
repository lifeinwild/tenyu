package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar;

import java.security.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar.AvatarGui.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class AvatarGui extends
		NaturalityGui<AvatarDBI,
				Avatar,
				Avatar,
				AvatarStore,
				AvatarGui,
				AvatarTableItem> {
	public static class AvatarTableItem
			extends NaturalityTableItem<AvatarDBI, Avatar> {

		public AvatarTableItem(Avatar src) {
			super(src);
		}

	}

	@Override
	protected AvatarGui createDetailGui() {
		return new AvatarGui(Lang.DETAIL.toString(), idPrefix + "Detail");
	}

	@Override
	protected AvatarTableItem createTableItem(Avatar o) {
		return new AvatarTableItem(o);
	}

	public AvatarGui(String name, String id) {
		super(name, id);
	}

	@Override
	public GridPane buildUpdate(Avatar exist) {
		super.buildUpdate(exist);

		set(exist);

		getExplanationInput().setPromptText(Lang.AVATAR_EXP_PROMPT.toString());
		getNameInput().setPromptText(Lang.AVATAR_NAME_PROMPT.toString());
		return grid;
	}

	@Override
	public GridPane buildSearch(SearchFuncs<AvatarDBI, Avatar> sf) {
		//総件数
		buildCount(DBUtil.countStatic(AvatarStore.getMainStoreInfoStatic()));

		super.buildSearch(null);

		add(detailGui);

		return grid;
	}

	@Override
	public GridPane buildDelete(Avatar exist) {
		super.buildDelete(exist);

		set(exist);
		return grid;
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		getExplanationInput().setPromptText(Lang.AVATAR_EXP_PROMPT.toString());
		getNameInput().setPromptText(Lang.AVATAR_NAME_PROMPT.toString());

		return grid;

	}

	public Avatar setupModelCreate() {
		modelCache = new Avatar();
		return super.setupModelCreate();
	}

	public void set(AvatarDBI n) {
		set((Avatar) n);
	}

	public Avatar setupModelUpdateOrDelete(Avatar o) {
		modelCache = o;
		return super.setupModelUpdateOrDelete(o);
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.AVATAR;
	}

	@Override
	public AvatarStore getStore(Transaction txn) {
		try {
			return new AvatarStore(txn);
		} catch (NoSuchAlgorithmException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

}