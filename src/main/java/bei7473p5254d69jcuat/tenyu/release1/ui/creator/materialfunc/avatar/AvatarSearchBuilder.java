package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class AvatarSearchBuilder extends GuiBuilder {

	@Override
	public Node build() {
		AvatarGui searchGui = new AvatarGui(name(), id());
		searchGui.buildSearch(null);
		searchGui.buildExternalButton(new SubmitButtonFuncs(
				//修正ボタン
				Lang.AVATAR_UPDATE.toString(), id() + "Update", (a) -> {
					//FXスレッドで動作させるため検証処理として登録
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						AvatarUpdateBuilder builder = new AvatarUpdateBuilder();
						Glb.getGui().createTab(builder.build(id),
								builder.name());
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null));
		return searchGui.getGrid();
	}

	@Override
	public String name() {
		return Lang.AVATAR_SEARCH.toString();
	}

	@Override
	public String id() {
		return "avatarSearch";
	}

}
