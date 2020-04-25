package bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame;

import java.io.*;
import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame.RatingGameGui.*;
import glb.*;
import glb.util.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import jetbrains.exodus.env.*;

public class RatingGameGui extends
		IndividualityObjectGui<RatingGameI,
				RatingGame,
				RatingGame,
				RatingGameStore,
				RatingGameGui,
				RatingGameTableItem> {
	public static class RatingGameTableItem
			extends IndividualityObjectTableItem<RatingGameI, RatingGame> {

		public RatingGameTableItem(RatingGame src) {
			super(src);
		}
	}

	@Override
	protected RatingGameGui createDetailGui() {
		return new RatingGameGui(Lang.DETAIL.toString(), idPrefix + detail);
	}

	public RatingGameGui(String name, String id) {
		super(name, id);
	}

	private TableView<RatingGameTeamClassTableItem> teamTable;
	//private TableView<GameClientFileTableItem> filesTable;
	private Path clientFilesDir;
//	private List<UploadFileGui> files;

	private boolean addClientFileButtonValidation(SubmitButtonGui gui) {
		return false;//TODO
		/*
		try {
			//親フォルダ検証
			ValidationResult vr = new ValidationResult();
			Path clientFilesDir = getClientFilesDir();
			IndividualityObject.validateName(clientFilesDir.toString(), vr,
					TenyutalkFile.nameMax);
			if (!vr.isNoError()) {
				gui.message(vr);
				return false;
			}

			//各ファイルを一覧化
			List<UploadFileGui> files = GuiCommon
					.getFilesFromDir(clientFilesDir, gui, vr);
			if (files == null || !vr.isNoError()) {
				gui.message(vr);
				return false;
			}
			setFiles(files);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
		*/
	}

	private boolean addClientFileButtonAdd(SubmitButtonGui gui) {
		return false;//TODO
		/*
		try {
			List<UploadFileGui> files = getFiles();
			if (files == null)
				return false;
			TableView<GameClientFileTableItem> t = getClientFilesTable();
			t.getItems().clear();
			ValidationResult vr = new ValidationResult();
			for (UploadFileGui f : files) {
				TenyuFile src = new TenyuFile();
				src.setFileHash(f.digestOriginalPath());
				src.setFileSize(f.getSize());
				src.setDirAndFilename(f.getDirAndFilenameFromSpecifiedFolder());
				t.getItems().add(new GameClientFileTableItem(src, f));
			}

			return true;
		} catch (Exception e) {
			Glb.debug(e);
			return false;
		}
		*/
	}

	private void buildClientFileChooser() {
		//クライアントソフトウェアのフォルダを指定

		//注意書き
		Label onlyStaticLabel = new Label(Lang.ONLY_STATIC_FILE.toString());
		onlyStaticLabel.setFocusTraversable(false);
		grid.add(onlyStaticLabel, 1, elapsed);
		elapsed += 1;

		Label notification = new Label(
				Lang.FILE_SELECT_NOTIFICATION.toString());
		notification.setFocusTraversable(false);
		grid.add(notification, 1, elapsed);
		elapsed += 1;

		//ファイルリストへ登録
		SubmitButtonGui clientFilesAddGui = GuiCommon.buildSubmitButton(grid,
				elapsed, Lang.GAME_CLIENTFILES_ADD.toString(), idPrefix, null,
				null, null, null);
		elapsed += 1;

		//フォルダ選択
		clientFilesAddGui.getSubmitButton().setId(idPrefix + "DirSelectButton");
		clientFilesAddGui.getSubmitButton().setFocusTraversable(true);
		clientFilesAddGui.getSubmitButton().setDefaultButton(false);
		clientFilesAddGui.getSubmitButton().setOnAction((ev) -> {
			DirectoryChooser fileChooser = new DirectoryChooser();
			fileChooser.setTitle(name);
			Stage sta = Glb.getGui().getPrimary();
			File selected = fileChooser.showDialog(sta);
			if (selected == null || !selected.exists()
					|| !selected.isDirectory())
				return;
			setClientFilesDir(selected.toPath());

			if (!addClientFileButtonValidation(clientFilesAddGui)) {
				clear();
				return;
			}

			if (!addClientFileButtonAdd(clientFilesAddGui)) {
				clear();
			}
		});
	}

	private void buildClientFileTable() {
		/*
		//ファイルリスト
		setClientFilesTable(new TableView<>());
		double width = 540;
		getClientFilesTable().setMinWidth(width);
		TableColumn<GameClientFileTableItem,
				String> nameHead = new TableColumn<>(
						Lang.FILE_RELATIVE_PATH.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<GameClientFileTableItem, String>(
						"relativeFilePath"));
		nameHead.setMinWidth(width * 0.6);
		TableColumn<GameClientFileTableItem,
				Long> sizeHead = new TableColumn<>(Lang.FILE_SIZE.toString());
		sizeHead.setCellValueFactory(
				new PropertyValueFactory<GameClientFileTableItem, Long>(
						"fileSize"));
		sizeHead.setMinWidth(width * 0.4);

		getClientFilesTable().getColumns().addAll(nameHead, sizeHead);

		grid.add(getClientFilesTable(), 1, elapsed);
		elapsed += 4;
		*/
	}

	private void buildTeamClass() {
		//チームクラス設定
		String idPrefix = this.idPrefix + "TeamClass";
		TeamClassGui teamBuilt = new TeamClassGui(name, idPrefix);
		teamBuilt.buildCreate();
		SubmitButtonFuncs sbf = new SubmitButtonFuncs(
				Lang.RATINGGAME_TEAMCLASS_REGISTER.toString(), idPrefix,
				gui -> {
					TeamClass tc = teamBuilt.setupModel();
					//検証
					ValidationResult vr = new ValidationResult();
					tc.validateAtCreate(vr);
					if (!vr.isNoError()) {
						gui.message(vr);
						return false;
					}
					return true;
				}, gui -> {
					//登録処理
					try {
						TeamClass tc = teamBuilt.setupModel();
						//テーブルに設定
						RatingGameTeamClassTableItem e = new RatingGameTeamClassTableItem(
								tc);
						getTeamTable().getItems().add(e);
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, gui -> teamBuilt.clear(), null);
		teamBuilt.buildExternalButton(sbf);
		add(teamBuilt);
	}

	private void buildTeamClassDisplay() {
		//チームリスト
		setTeamTable(new TableView<>());
		double width = 540;
		getTeamTable().setMinWidth(width);
		TableColumn<RatingGameTeamClassTableItem,
				String> teamNameHead = new TableColumn<>(
						Lang.RATINGGAME_TEAMCLASS_NAME.toString());
		teamNameHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamClassTableItem, String>(
						"teamName"));
		teamNameHead.setMinWidth(width * 0.6);
		TableColumn<RatingGameTeamClassTableItem,
				Integer> teamMemberCountHead = new TableColumn<>(
						Lang.RATINGGAME_TEAMCLASS_MEMBER_COUNT.toString());
		teamMemberCountHead.setCellValueFactory(
				new PropertyValueFactory<RatingGameTeamClassTableItem, Integer>(
						"teamMemberCount"));
		teamMemberCountHead.setMinWidth(width * 0.4);

		getTeamTable().getColumns().addAll(teamNameHead, teamMemberCountHead);

		grid.add(getTeamTable(), 1, elapsed);
		elapsed += 4;
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		buildClientFileChooser();
		buildClientFileTable();
		buildTeamClass();
		buildTeamClassDisplay();

		buildSubmitButton(gui -> validateAtCreate(gui), gui -> true,
				gui -> clear(), null);
		elapsed += 2;

		return grid;
	}

	@Override
	public GridPane buildUpdate(RatingGame exist) {
		super.buildUpdate(exist);

		buildClientFileChooser();
		buildClientFileTable();
		buildTeamClass();
		buildTeamClassDisplay();

		buildSubmitButton(gui -> validateAtUpdate(gui, exist), gui -> true,
				gui -> clear(), null);
		elapsed += 2;
		set(exist);

		return grid;
	}

	@Override
	public GridPane buildSearch(SearchFuncs<RatingGameI, RatingGame> sf) {
		//総件数
		buildCount(
				DBUtil.countStatic(RatingGameStore.getMainStoreInfoStatic()));

		//検索GUI
		super.buildSearch(sf);

		//詳細表示
		add(detailGui);

		return grid;
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.RATINGGAME;
	}

	@Override
	protected RatingGameTableItem createTableItem(RatingGame o) {
		return new RatingGameTableItem(o);
	}

	@Override
	public void set(RatingGame n) {
		super.set(n);
		for (TeamClass t : n.getTeamClasses()) {
			teamTable.getItems().add(new RatingGameTeamClassTableItem(t));
		}
	}

	public TableView<RatingGameTeamClassTableItem> getTeamTable() {
		return teamTable;
	}

	public void setTeamTable(
			TableView<RatingGameTeamClassTableItem> teamTable) {
		this.teamTable = teamTable;
	}

	@Override
	public RatingGame setupModelCreate() {
		super.setupModelCreate();
		RatingGame o = new RatingGame();
		setupModelByGui(o);
		return o;
	}

	private void setupModelByGui(RatingGame o) {
		for (RatingGameTeamClassTableItem e : teamTable.getItems()) {
			o.addTeam(e.getSrc());
		}
	}

	@Override
	public RatingGame setupModelUpdateOrDelete(RatingGame o) {
		super.setupModelUpdateOrDelete(o);
		setupModelByGui(o);
		return o;
	}

	public Path getClientFilesDir() {
		return clientFilesDir;
	}

	public void setClientFilesDir(Path clientFilesDir) {
		this.clientFilesDir = clientFilesDir;
	}


	@Override
	public RatingGameStore getStore(Transaction txn) {
		return new RatingGameStore(txn);
	}

	public RatingGameGui getDetailGui() {
		return detailGui;
	}

	public void setDetailGui(RatingGameGui detailGui) {
		this.detailGui = detailGui;
	}

}
