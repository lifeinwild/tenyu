package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user.SocialityGui.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class SocialityGui extends
		ObjectivityObjectGui<SocialityDBI,
				Sociality,
				Sociality,
				SocialityStore,
				SocialityGui,
				SocialityTableItem> {
	public static class SocialityTableItem
			extends ObjectivityObjectTableItem<SocialityDBI, Sociality> {

		public SocialityTableItem(Sociality src) {
			super(src);
		}

	}

	@Override
	protected SocialityGui createDetailGui() {
		return new SocialityGui(Lang.DETAIL.toString(), idPrefix + detail);
	}

	@Override
	protected SocialityTableItem createTableItem(Sociality o) {
		return new SocialityTableItem(o);
	}

	public SocialityGui(String name, String id) {
		super(name, id);
	}

	@Override
	public SocialityStore getStore(Transaction txn) {
		try {
			return new SocialityStore(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	private Sociality sociality;

	@Override
	public void set(Sociality n) {
		// TODO 自動生成されたメソッド・スタブ
		super.set(n);
	}

	public GridPane buildSimpleBan(Sociality exist) {
		sociality = exist;
		if (sociality.getNodeType() == NodeType.USER) {
			UserGui u = new UserGui(name, idPrefix + "NaturalityUser");
			u.buildRead();
			add(u);

			u.set(Glb.getObje().getUser(
					us -> us.get(exist.getNaturalityConcreteRecycleId())));
		}
		//TODO 他の自然性のGUI表示
		set(exist);

		buildSubmitButton(gui -> {
			try {
				Long id = Long.valueOf(getIdInput().getText());
				return IdObject.validateIdStandard(id);
			} catch (Exception e) {
				Glb.debug(e);
				return false;
			}
		}, gui -> true, null, null);

		return grid;
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.SOCIALITY;
	}

	public Sociality getSociality() {
		return sociality;
	}

	public void setSociality(Sociality sociality) {
		this.sociality = sociality;
	}

}
