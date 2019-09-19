package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.MaterialListGui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.MaterialListGui.MaterialList.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import jetbrains.exodus.env.*;

/**
 * 複数の素材をまとめて登録したり更新する
 * @author exceptiontenyu@gmail.com
 *
 */
public class MaterialListGui extends ModelGui<MaterialList> {
	private MaterialGui common;

	private List<UploadFileGui> files;

	private TableView<MaterialTableItem> list;

	private Label materialUpdateNotificationLabel;

	private Path selectedDir;
	private List<Material> tmpObjs = new ArrayList<>();

	public MaterialListGui(String name, String id) {
		super(name, id);
	}

	public static boolean moveFiles(MaterialListGui built, SubmitButtonGui gui)
			throws Exception {
		List<UploadFileGui> r = new ArrayList<>();
		for (MaterialTableItem e : built.getList().getItems()) {
			r.add(e.getInfo());
		}
		return GuiCommon.moveFiles(r, gui);
	}

	public boolean validate(SubmitButtonGui gui) {
		List<MaterialTableItem> items = getList().getItems();
		if (items == null || items.size() == 0)
			return false;

		//以下、検証済みのオブジェクト一覧を作成する
		List<Material> validatedObjs = getTmpObjs();
		validatedObjs.clear();

		//正常なデータのみか
		HashSet<String> names = new HashSet<>();
		for (MaterialTableItem item : items) {
			Material src = item.getSrc();
			if (src == null) {
				gui.message(Lang.EXCEPTION.toString());
				return false;
			}

			String objName = src.getName();
			if (objName == null || objName.length() == 0) {
				gui.message(Lang.EXCEPTION.toString());
				return false;
			}

			ValidationResult r = new ValidationResult();
			src.validateAtCreate(r);
			if (!r.isNoError()) {
				gui.message(objName + " " + r.toString());
				return false;
			}

			Path orig = item.getInfo().getOriginalPath();
			if (orig == null)
				return false;
			File f = orig.toFile();
			if (!f.exists()) {
				gui.message(
						objName + " " + Lang.ERROR_FILE_NOT_FOUND.toString());
				return false;
			}
			//登録されるファイル群の名前が被っていないか
			if (names.contains(item.getName())) {
				gui.message(objName + " " + Lang.ERROR_DUPLICATE.toString());
				return false;
			}
			names.add(item.getName());

			Long exist = Glb.getObje()
					.getMaterial(ms -> ms.getIdByName(objName));
			if (exist != null) {
				gui.message(
						objName + " " + Lang.NAME_ALREADY_EXISTS.toString());
				return false;
			}

			validatedObjs.add(src);
		}

		return true;
	}

	/**
	 * 前段階の操作として、設定されたファイル情報などを中間リストに登録する
	 * @param nameSuffix	これにユーザー名が接頭辞として付加され素材名になる
	 * @param exp
	 * @param choosed
	 * @param limitStr		使用可能ユーザー制限。ユーザー名のスペース区切り
	 * @param builtList
	 * @throws Exception
	 */
	private boolean addItemToList(UploadFileGui f, SubmitButtonGui buttonGui,
			BiFunction<AddItemToListFilter, Transaction, Boolean> filter,
			ValidationResult r) throws Exception {
		Material src = new Material();

		Long myUserId = Glb.getMiddle().getMyUserId();
		if (myUserId == null) {
			return false;
		}

		src.setRegistererUserId(myUserId);
		src.setMainAdministratorUserId(myUserId);

		//ユーザー名がnameの接頭辞になり、最初のフォルダの名前になる
		User me = Glb.getObje().getUser(us -> us.get(myUserId));
		if (me == null)
			return false;
		src.setName(me.getName() + Glb.getConst().getFileSeparator()
				+ f.getDirAndFilenameFromSpecifiedFolder());
		src.setExplanation(common.getExplanationInput().getText());
		src.setFileSize(f.getSize());
		src.setFileHash(f.digestOriginalPath());
		src.setUserLimitation(common.getUserLimitIds());

		AddItemToListFilter arg = new AddItemToListFilter(src, r,
				f.getOriginalPath());
		boolean accept = Glb.getObje().compute(txn -> {
			if (!filter.apply(arg, txn)) {
				buttonGui.message(arg.getR());
				return false;
			}
			return true;
		});
		if (!accept)
			return false;

		String appDir = Glb.getFile().getMaterialDir();
		f.setInAppPath(Paths.get(appDir + src.getPath()));

		getList().getItems().add(new MaterialTableItem(src, f));
		return true;
	}

	private void buildButtonMiddleList(
			BiFunction<AddItemToListFilter, Transaction, Boolean> filter) {
		//中間リストへ登録
		SubmitButtonGui submitButtonGui = GuiCommon.buildSubmitButton(grid,
				elapsed, Lang.MATERIAL_LIST_ADD.toString(), idPrefix, gui -> {
					//フォルダ選択時点では説明しか検証できない。
					//実際のモデルデータは中間リストへの登録時に作成される
					Path p = common.getSelectedFile();
					if (p == null)
						return false;

					//親フォルダの検証
					ValidationResult vr = new ValidationResult();
					String exp = common.getExplanationInput().getText();
					Naturality.validateExplanation(exp, vr);
					if (!vr.isNoError()) {
						gui.message(vr);
						return false;
					}

					//各ファイルの検証 メソッド内部で行われている
					List<UploadFileGui> files = GuiCommon.getFilesFromDir(p,
							gui, vr);
					if (files == null || !vr.isNoError()) {
						gui.message(vr);
						return false;
					}
					setFiles(files);

					return true;
				}, gui -> {
					try {
						List<UploadFileGui> files = getFiles();
						if (files == null)
							return false;
						getList().getItems().clear();
						ValidationResult vr = new ValidationResult();
						for (UploadFileGui f : files) {
							if (!addItemToList(f, gui, filter, vr))
								return false;
						}
						return true;
					} catch (Exception e) {
						Glb.debug(e);
						return false;
					}
				}, gui -> common.clear(), null);
		elapsed += 1;

		//中間リスト
		setList(new TableView<>());
		double width = 540;
		getList().setMinWidth(width);
		TableColumn<MaterialTableItem, String> nameHead = new TableColumn<>(
				Lang.NATURALITY_NAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<MaterialTableItem, String>("name"));
		nameHead.setMinWidth(width * 0.5);
		TableColumn<MaterialTableItem, String> expHead = new TableColumn<>(
				Lang.NATURALITY_EXPLANATION.toString());
		expHead.setCellValueFactory(
				new PropertyValueFactory<MaterialTableItem, String>(
						"explanation"));
		expHead.setMinWidth(width * 0.2);
		TableColumn<MaterialTableItem,
				Long> sizeHead = new TableColumn<>(Lang.FILE_SIZE.toString());
		sizeHead.setCellValueFactory(
				new PropertyValueFactory<MaterialTableItem, Long>("fileSize"));
		sizeHead.setMinWidth(width * 0.1);
		TableColumn<MaterialTableItem, String> limitHead = new TableColumn<>(
				Lang.USER_LIMITATION.toString());
		limitHead.setCellValueFactory(
				new PropertyValueFactory<MaterialTableItem, String>(
						"userLimit"));
		limitHead.setMinWidth(width * 0.2);

		getList().getColumns().addAll(nameHead, expHead, sizeHead, limitHead);

		grid.add(getList(), 1, elapsed);
		elapsed += 4;
	}

	@Override
	public GridPane buildCreateBatch() {
		super.buildCreateBatch();
		//ユーザー制限などはまとめて登録される全素材で共通にする
		//※DB上のデータは分かれている。GUI上でまとめるだけ
		common = new MaterialGui(name, idPrefix);
		common.buildCreate();

		Label notificationLabel = new Label(
				Lang.MATERIAL_REGISTER_NOTIFICATION.toString());
		grid.add(notificationLabel, 1, elapsed, 1, 1);
		elapsed += 1;

		buildDirSelect();
		super.buildUpdateBatch();
		add(common);
		buildMaterialBasicInfoForm();
		buildButtonMiddleList((arg, txn) -> {
			arg.getSrc().validateAtCreate(arg.getR());
			try {
				new MaterialStore(txn).noExistIdObjectConcrete(arg.getSrc(),
						arg.getR());
			} catch (Exception e) {
				Glb.debug(e);
			}
			return arg.getR().isNoError();
		});

		return grid;
	}

	private void buildDirSelect() {
		Label dirSelectLabel = new Label(
				Lang.FILE_SELECT_NOTIFICATION.toString());
		dirSelectLabel.setFocusTraversable(false);

		Button dirSelect = new Button(Lang.DIR_SELECT.toString());
		dirSelect.setId(idPrefix + "DirSelectButton");
		dirSelect.setFocusTraversable(true);
		dirSelect.setDefaultButton(false);
		HBox dirSelectPane = new HBox(10);
		dirSelectPane.setId(idPrefix + "DirSelectPane");
		dirSelectPane.setAlignment(Pos.CENTER);
		dirSelectPane.getChildren().add(dirSelectLabel);
		dirSelectPane.getChildren().add(dirSelect);
		dirSelect.setOnAction((ev) -> {
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle(name);
			Stage sta = Glb.getGui().getPrimary();
			File selectedDir = dirChooser.showDialog(sta);
			if (selectedDir == null || !selectedDir.exists())
				return;
			common.setSelectedFile(selectedDir.toPath());
			common.getNameInput()
					.setText(common.getSelectedFile().getFileName().toString());
		});
		grid.add(dirSelectPane, 1, elapsed);
		elapsed += 2;

	}

	/**
	 * 全素材共通の情報を設定する
	 */
	private void buildMaterialBasicInfoForm() {
		//ファイル名はファイル選択時に自動設定
		common.getNameInput().setEditable(false);
		common.getExplanationInput()
				.setPromptText(Lang.MATERIAL_EXP_PROMPT.toString());

		VBox limitPane = new VBox();
		limitPane.setSpacing(10);
		limitPane.setAlignment(Pos.CENTER_RIGHT);
		limitPane.setId(idPrefix + "UserLimitationPane");
		int limitPaneElapsed = 0;

		//ユーザー制限をするか
		CheckBox userLimitUse = new CheckBox(
				Lang.USER_LIMITATION_USE.toString());
		userLimitUse.setOnAction(ev -> {
			if (userLimitUse.isSelected()) {
				common.getLimitAddButton().getSubmitButton().setDisable(false);
				common.getUserLimit().getNameInput().setEditable(true);
			} else {
				common.getLimitAddButton().getSubmitButton().setDisable(true);
				common.getUserLimitTable().getItems().clear();
				common.getUserLimit().getNameInput().setEditable(false);
			}
		});
		limitPane.getChildren().add(userLimitUse);
		limitPaneElapsed += 1;

		//ユーザー制限
		common.setUserLimit(new UserGui(name, idPrefix + "UserLimitation"));
		common.getUserLimit().buildSearchSimple(null);
		common.getUserLimit()
				.buildExternalButton(new SubmitButtonFuncs(
						Lang.USER_LIMITATION_ADD.toString(),
						idPrefix + "UserLimitation", a -> {
							return true;
						}, null, null, null));

		//ユーザー制限追加
		common.buildExternalButton(new SubmitButtonFuncs(
				Lang.USER_LIMITATION_ADD.toString(), idPrefix + "", gui -> {
					String limitUserName = common.getUserLimit().getNameInput()
							.getText();
					if (limitUserName == null || limitUserName.length() == 0)
						return false;
					ValidationResult vr = new ValidationResult();
					Naturality.validateName(limitUserName, vr);
					if (!vr.isNoError()) {
						gui.message(vr);
						return false;
					}
					User u = Glb.getObje()
							.getUser(us -> us.getByName(limitUserName));
					if (u == null) {
						vr.add(Lang.NATURALITY_NAME, Lang.ERROR_DB_NOTFOUND,
								limitUserName);
						gui.message(vr);
						return false;
					}
					common.getUserLimitTable().getItems()
							.add(new UserTableItem(u));
					return true;
				}, null, null, null));

		common.getExternalButton().getSubmitButton().setDisable(true);

		//ユーザー制限削除
		SubmitButtonGui limitClearButton = GuiCommon.buildSubmitButton(
				limitPane, elapsed + limitPaneElapsed,
				Lang.TABLEVIEW_REMOVE_SELECTED.toString(), idPrefix, gui -> {
					Object selected = common.getUserLimitTable()
							.getSelectionModel().getSelectedItems();
					return common.getUserLimitTable().getItems()
							.remove(selected);
				}, null, null, null);
		limitPaneElapsed += 1;

		//ユーザー制限リスト
		common.setUserLimitTable(new TableView<>());
		double width = 540;
		common.getUserLimitTable().setMinWidth(width);
		TableColumn<UserTableItem, Integer> idHead = new TableColumn<>(
				Lang.IDOBJECT_RECYCLE_ID.toString());
		idHead.setCellValueFactory(
				new PropertyValueFactory<UserTableItem, Integer>("id"));
		idHead.setMinWidth(width * 0.4);
		TableColumn<UserTableItem,
				String> nameHead = new TableColumn<>(Lang.USER_NAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<UserTableItem, String>("name"));
		nameHead.setMinWidth(width * 0.6);

		common.getUserLimitTable().getColumns().addAll(idHead, nameHead);

		limitPane.getChildren().add(common.getUserLimitTable());
		limitPaneElapsed += 4;

		grid.add(limitPane, 0, elapsed, 2, limitPaneElapsed);
		elapsed += limitPaneElapsed;

		add(common.getUserLimit());
	}

	@Override
	public GridPane buildUpdateBatch() {
		super.buildUpdateBatch();

		buildUpdateNotification();
		buildDirSelect();
		super.buildUpdateBatch();
		buildMaterialBasicInfoForm();
		buildButtonMiddleList((arg, txn) -> {
			try {
				arg.getSrc().validateAtUpdate(arg.getR());
				return new MaterialStore(txn)
						.validateAtUpdateChange(arg.getSrc(), arg.getR());
			} catch (Exception e) {
				Glb.debug(e);
				return false;
			}
		});

		return grid;
	}

	private void buildUpdateNotification() {
		materialUpdateNotificationLabel = new Label(
				Lang.MATERIAL_UPDATE_NOTIFICATION.toString());
		grid.add(materialUpdateNotificationLabel, 1, elapsed, 1, 1);
		elapsed += 1;
	}

	public void clear() {
		if (list != null) {
			list.getItems().clear();
		}
		tmpObjs.clear();
		common.clear();
	}

	public List<UploadFileGui> getFiles() {
		return files;
	}

	public TableView<MaterialTableItem> getList() {
		return list;
	}

	public Path getSelectedDir() {
		return selectedDir;
	}

	public List<Material> getTmpObjs() {
		return tmpObjs;
	}

	@Override
	public void set(MaterialList o) {
		for (MaterialAndUploadFile m : o.getList()) {
			list.getItems().add(
					new MaterialTableItem(m.getMaterial(), m.getUploadFile()));
		}
	}

	public void setFiles(List<UploadFileGui> files) {
		this.files = files;
	}

	public void setList(TableView<MaterialTableItem> list) {
		this.list = list;
	}

	public void setTmpObjs(List<Material> tmpObjs) {
		this.tmpObjs = tmpObjs;
	}

	/**
	 * 素材一覧に対応するクラスが無いのでGUIのためだけに作成
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class MaterialList {
		public static class MaterialAndUploadFile {
			private Material material;
			private UploadFileGui uploadFile;

			public Material getMaterial() {
				return material;
			}

			public void setMaterial(Material material) {
				this.material = material;
			}

			public UploadFileGui getUploadFile() {
				return uploadFile;
			}

			public void setUploadFile(UploadFileGui uploadFile) {
				this.uploadFile = uploadFile;
			}

		}

		private List<MaterialAndUploadFile> list;

		public List<MaterialAndUploadFile> getList() {
			return list;
		}

		public void setList(List<MaterialAndUploadFile> list) {
			this.list = list;
		}
	}
}