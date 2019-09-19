package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class UserSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		UserGui searchGui = new UserGui(name(), id());
		searchGui.buildSearch(null);

		//修正ボタン
		searchGui.buildExternalButton(Lang.USER_UPDATE.toString(),
				id() + "Update", (a) -> {
					//FXスレッドで動作させるため検証処理として登録
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long userId = Long.valueOf(idStr);
						UserUpdateBuilder builder = new UserUpdateBuilder();
						Glb.getGui().createTab(builder.build(userId),
								builder.name());
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null);

		if (Glb.getObje().getCore().getManagerList().isIm51PerAdmin()) {
			searchGui.buildExternalButton(Lang.USER_BAN_SIMPLE.toString(),
					id() + "SimpleBAN", (a) -> {
						try {
							String idStr = searchGui.getDetailGui().getIdInput()
									.getText();
							Long userId = Long.valueOf(idStr);
							UserBANSimple builder = new UserBANSimple();
							Glb.getGui().createTab(builder.build(userId),
									builder.name());
							return true;
						} catch (Exception e) {
							Glb.debug(e);
							return false;
						}
					}, null, null, null);
		}
		return searchGui.getGrid();
	}

	@Override
	public String id() {
		return "userSearch";
	}

	@Override
	public String name() {
		return Lang.USER_SEARCH.toString();
	}

}
