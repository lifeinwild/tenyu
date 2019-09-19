package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame.*;
import javafx.scene.layout.*;

public class TeamParticipationGui extends ModelGui<TeamParticipation> {

	public TeamParticipationGui(String name, String id) {
		super(name, id);
	}

	private void buildParticipation() {
		//チーム加入ボタン
		buildSubmitButton(Lang.GAMEPLAY_RATINGGAME_PERTICIPATE_TEAM.toString(),
				idPrefix + "PaticipateTeam", gui -> {
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null);
		elapsed += 1;

		//チーム脱退ボタン
		buildSubmitButton(Lang.GAMEPLAY_RATINGGAME_RESET_TEAM.toString(),
				idPrefix + "LeaveTeam", gui -> {
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null);
		elapsed += 1;
	}

	private RatingGameGui searchGui;
	private TeamGui list;
	private TeamGui teamCreate;

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		//ゲーム検索
		searchGui = new RatingGameGui(name, idPrefix + "Search");
		searchGui.buildSearch(null);
		add(searchGui);

		//チーム検索
		list = new TeamGui(name, idPrefix + "List");
		list.buildSearchFromServer();
		add(list);

		//参加ボタン
		buildParticipation();

		//チーム作成
		teamCreate = new TeamGui(name, idPrefix + "Create");
		teamCreate.buildCreate();
		teamCreate.buildExternalButton(new SubmitButtonFuncs(
				Lang.GAMEPLAY_RATINGGAME_ADD_TEAM.toString(),
				idPrefix + "AddTeam", gui -> {
					try {
						String idStr = searchGui.getDetailGui().getIdInput()
								.getText();
						Long id = Long.valueOf(idStr);
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, null, null, null));
		elapsed += 1;

		add(teamCreate);

		return grid;
	}

	@Override
	public void set(TeamParticipation o) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
