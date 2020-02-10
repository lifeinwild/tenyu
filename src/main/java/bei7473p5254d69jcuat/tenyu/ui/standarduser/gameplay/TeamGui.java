package bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame.*;
import glb.*;
import glb.util.*;
import javafx.concurrent.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

public class TeamGui extends ObjectGui<Team> {

	public TeamGui(String name, String id) {
		super(name, id);
	}

	/**
	 * 検索ペース
	 */
	private DateList searchDates;
	private Label teamNameSearchLabel;
	private TextField teamNameSearchInput;
	private TableView<RatingGameTeamTableItem> teamTable;
	private TableView<RatingGameStateByUserTableItem> stateByUserTable;

	public GridPane buildSearchFromServer() {
		searchDates = new DateList();
		//IdObject系の検索と違う概念だがreadとも違う
		//サーバーにアクセスして現在登録されているチーム一覧を取得する等

		//チーム名検索
		teamNameSearchLabel = new Label(
				Lang.GAMEPLAY_RATINGGAME_TEAM_NAME_SEARCH.toString());
		teamNameSearchLabel.setId(idPrefix + "TeamNameSearchLabel");
		teamNameSearchLabel.setFocusTraversable(false);
		teamNameSearchInput = new TextField();
		teamNameSearchInput.setId(idPrefix + "TeamNameSearchInput");
		teamNameSearchInput.setFocusTraversable(false);
		teamNameSearchInput.setPromptText(Lang.ENTER_SEARCH.toString());
		teamNameSearchInput.setOnKeyReleased((ev) -> {
			try {
				//エンターキーが押されたら検索する
				if (!ev.getCode().equals(KeyCode.ENTER))
					return;

				//過剰な通信を防ぐ
				double pace = searchDates.pace();
				if (pace > 5.0)
					return;

				//通信とGUIへの設定処理
				//空ならランダム10件表示

				//通信処理が行われた場合ペースを更新する
				searchDates.add();

				//TODO
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
			return;
		});
		grid.add(teamNameSearchLabel, 0, elapsed);
		grid.add(teamNameSearchInput, 1, elapsed);
		elapsed += 1;

		//チーム一覧	名前とパスワード有無等を表示
		teamTable = new TableView<>();
		teamTable.setId(idPrefix + "TeamTable");
		TableColumn<RatingGameTeamTableItem,
				String> teamClassNameHead = new TableColumn<>(
						Lang.RATINGGAME_TEAMCLASS_NAME.toString());
		teamClassNameHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamTableItem, String>(
						"teamClassName"));
		teamClassNameHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameTeamTableItem,
				String> nameHead = new TableColumn<>(
						Lang.GAMEPLAY_MATCHING_TEAM_CLASSNAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamTableItem, String>(
						"teamName"));
		nameHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameTeamTableItem,
				Boolean> usePasswordHead = new TableColumn<>(
						Lang.RATINGGAME_TEAM_USEPASSWORD.toString());
		usePasswordHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamTableItem, Boolean>(
						"usePassword"));
		usePasswordHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameTeamTableItem,
				Integer> currentMemberSizeHead = new TableColumn<>(
						Lang.RATINGGAME_TEAM_CURRENT_MEMBER_COUNT.toString());
		currentMemberSizeHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamTableItem, Integer>(
						"currentMemberCount"));
		currentMemberSizeHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameTeamTableItem,
				Integer> teamClassMemberCountHead = new TableColumn<>(
						Lang.RATINGGAME_TEAMCLASS_MEMBER_COUNT.toString());
		teamClassMemberCountHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamTableItem, Integer>(
						"teamClassMemberCount"));
		teamClassMemberCountHead.setMinWidth(tableWidth * 0.2);
		teamTable.getColumns().addAll(teamClassNameHead, nameHead,
				usePasswordHead, currentMemberSizeHead,
				teamClassMemberCountHead);
		grid.add(teamTable, 0, elapsed, 2, 4);
		elapsed += 4;

		//初期値としてランダムに10件登録
		//TODO

		//チーム一覧及び詳細を定期更新
		Glb.getGui().polling(grid, () -> {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					if (isCancelled())
						return null;
					//TODO
					return null;
				}
			};
		});

		//選択されたチームのメンバー一覧を表示
		stateByUserTable = new TableView<>();
		stateByUserTable.setId(idPrefix + "RatingGameStateByUserTable");

		TableColumn<RatingGameStateByUserTableItem,
				String> userNameHead = new TableColumn<>(
						Lang.USER_NAME.toString());
		userNameHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameStateByUserTableItem,
						String>("userName"));
		userNameHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameStateByUserTableItem,
				Integer> ratingTeamHead = new TableColumn<>(
						Lang.RATINGGAME_STATEBYUSER_TEAM_RATING.toString());
		ratingTeamHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameStateByUserTableItem,
						Integer>("ratingTeam"));
		ratingTeamHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameStateByUserTableItem,
				Integer> ratingSingleHead = new TableColumn<>(
						Lang.RATINGGAME_STATEBYUSER_SINGLE_RATING.toString());
		ratingSingleHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameStateByUserTableItem,
						Integer>("ratingSingle"));
		ratingSingleHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameStateByUserTableItem,
				Integer> matchCountTeamHead = new TableColumn<>(
						Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_TEAM.toString());
		matchCountTeamHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameStateByUserTableItem,
						Integer>("matchCountTeam"));
		matchCountTeamHead.setMinWidth(tableWidth * 0.2);
		TableColumn<RatingGameStateByUserTableItem,
				Integer> matchCountSingleHead = new TableColumn<>(
						Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_SINGLE
								.toString());
		matchCountSingleHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameStateByUserTableItem,
						Integer>("matchCountSingle"));
		matchCountSingleHead.setMinWidth(tableWidth * 0.2);
		stateByUserTable.getColumns().addAll(userNameHead, ratingTeamHead,
				ratingSingleHead, matchCountTeamHead, matchCountSingleHead);
		grid.add(stateByUserTable, 0, elapsed, 2, 4);
		elapsed += 4;

		return grid;
	}

	private TableView<RatingGameTeamCountTableItem> countTable;
	private Label choiceClassLabel;
	private Label addTeamTeamNameLabel;
	private Label addTeamPasswordLabel;
	private ChoiceBox<TeamClass> choiceClass;
	private TextField teamNameInput;
	private TextField passwordInput;

	@Override
	public GridPane buildCreate() {
		//チームクラスID別総数
		countTable = new TableView<>();
		countTable.setId(idPrefix + "RatingGameCountTable");
		TableColumn<RatingGameTeamCountTableItem,
				String> countTeamClassNameHead = new TableColumn<>(
						Lang.RATINGGAME_TEAMCLASS_NAME.toString());
		countTeamClassNameHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamCountTableItem, String>(
						"teamClassName"));
		countTeamClassNameHead.setMinWidth(tableWidth * 0.5);
		TableColumn<RatingGameTeamCountTableItem,
				Integer> teamCountHead = new TableColumn<>(
						Lang.RATINGGAME_COUNT_BY_TEAMCLASS.toString());
		teamCountHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamCountTableItem, Integer>(
						"fullTeamCount"));
		teamCountHead.setMinWidth(tableWidth * 0.5);
		countTable.getColumns().addAll(countTeamClassNameHead, teamCountHead);
		grid.add(countTable, 0, elapsed, 2, 4);
		elapsed += 4;

		//チーム作成
		choiceClassLabel = new Label(Lang.RATINGGAME_TEAMCLASS.toString());
		choiceClassLabel.setId(idPrefix + "AddTeamTeamClassLabel");
		choiceClass = new ChoiceBox<>();
		choiceClass.setId(idPrefix + "AddTeamTeamClassChoice");
		grid.add(choiceClassLabel, 0, elapsed);
		grid.add(choiceClass, 1, elapsed);
		elapsed += 1;

		addTeamTeamNameLabel = new Label(
				Lang.GAMEPLAY_MATCHING_TEAM_CLASSNAME.toString());
		addTeamTeamNameLabel.setId(idPrefix + "AddTeamTeamNameLabel");
		teamNameInput = new TextField();
		teamNameInput.setId(idPrefix + "AddTeamTeamNameInput");
		grid.add(addTeamTeamNameLabel, 0, elapsed);
		grid.add(teamNameInput, 1, elapsed);
		elapsed += 1;

		addTeamPasswordLabel = new Label(
				Lang.GAMEPLAY_MATCHING_TEAM_PASSWORD.toString());
		addTeamPasswordLabel.setId(idPrefix + "AddTeamPasswordLabel");
		passwordInput = new TextField();
		passwordInput.setId(idPrefix + "AddTeamPasswordInput");
		grid.add(addTeamPasswordLabel, 0, elapsed);
		grid.add(passwordInput, 1, elapsed);
		elapsed += 1;

		return grid;
	}

	@Override
	public void set(Team o) {
		if (choiceClass != null) {
			choiceClass.getSelectionModel().select(o.getTeamClassId());
		}
		if (teamNameInput != null) {
			teamNameInput.setText(o.getName());
		}
		if (passwordInput != null) {
			//passwordInput.setText("" + (o.getPasswordHash() != null));
		}
		if (stateByUserTable != null) {
			setMembers(o.getRatingGameId(), o.getMembers());
		}
	}

	public void setMembers(Long gameId, List<NodeIdentifierUser> members) {
		for (NodeIdentifierUser member : members) {
			if (member.getUserId() == null)
				continue;
			try {
				RatingGameStateByUser state = Glb.getObje()
						.getRatingGameStateByUser(rgsbus -> rgsbus
								.getByGameIdUserId(gameId, member.getUserId()));
				User u = Glb.getObje()
						.getUser(us -> us.get(member.getUserId()));
				stateByUserTable.getItems()
						.add(new RatingGameStateByUserTableItem(state, u));
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				continue;
			}
		}
	}

}
