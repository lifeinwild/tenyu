package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.user.SocialityGui.*;
import glb.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class SocialityGui extends
		AdministratedObjectGui<SocialityI,
				Sociality,
				Sociality,
				SocialityStore,
				SocialityGui,
				SocialityTableItem> {
	public static class SocialityTableItem
			extends AdministratedObjectTableItem<SocialityI, Sociality> {

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
			UserGui u = new UserGui(name, idPrefix + "IndividualityObjectUser");
			u.buildRead();
			add(u);

			u.set(Glb.getObje().getUser(
					us -> us.get(exist.getIndividualityObjectConcreteId())));
		}
		//TODO 他の個性系オブジェクトのGUI表示
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
