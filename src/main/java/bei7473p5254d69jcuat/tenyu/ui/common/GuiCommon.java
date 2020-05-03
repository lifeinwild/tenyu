package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.TextFormatter.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

public class GuiCommon {
	/**
	 * 総件数を表示するGUI部品を構築
	 *
	 * @param grid
	 * @param elapsed
	 * @param guiId
	 * @param count
	 * @return
	 */
	public static int buildCount(GridPane grid, int elapsed, String guiId,
			long count) {
		Label countLabelGui = new Label(Lang.COUNT.toString());
		countLabelGui.setId(guiId + "CountLabel");
		grid.add(countLabelGui, 0, elapsed);
		Label countGui = new Label("" + count);
		countGui.setId(guiId + "Count");
		grid.add(countGui, 1, elapsed);
		elapsed += 1;
		return elapsed;
	}

	/**
	 * 汎用的な権限警告
	 * @param guiName
	 * @param adminIds
	 */
	public static void onlyAdmin(String guiName, List<Long> adminIds) {
		try {
			Long myUserId = Glb.getMiddle().getMyUserId();
			if (myUserId == null) {
				Glb.getGui().alert(AlertType.WARNING, guiName,
						Lang.ONLY_USER.toString());
			} else if (adminIds == null) {
				//全ユーザー許可
			} else if (!adminIds.contains(myUserId)) {
				//51%全体運営者以外拒否
				if (Glb.getObje().getCore().getManagerList()
						.getManagerPower(myUserId) > 0.51D) {
					//許可
				} else {
					Glb.getGui().alert(AlertType.WARNING, guiName,
							Lang.NOT_ADMINISTRATOR.toString());
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			Glb.getGui().alert(AlertType.ERROR, guiName,
					Lang.EXCEPTION.toString());
		}
	}

	/**
	 * 権限者でなければ警告を出す
	 * 機能によって必要な権限が異なるので、判定メソッドも異なり、
	 * ラムダ式で呼び出し側が与える。
	 * @param guiName
	 * @param validate	自分のユーザーIDを入力されて権限者であるかを判定する
	 */
	/*	public static void onlyRoleAdmin(String guiName, RoleManagerPart admins) {
			Long myUserId = Glb.getMiddle().getMyUserId();
			if (myUserId == null || !admins.isAdmin(myUserId)) {
				Glb.getGui().alert(AlertType.WARNING, guiName,
						Lang.ONLY_USER.toString());
			}
		}*/

	/**
	 * ユーザー登録済みか
	 */
	public static void onlyUser(String guiName) {
		Long myUserId = Glb.getMiddle().getMyUserId();
		if (myUserId == null) {
			Glb.getGui().alert(AlertType.WARNING, guiName,
					Lang.ONLY_USER.toString());
		}
	}

	/**
	 * 51%以上の影響割合を持つ全体運営者限定機能の場合、
	 * そうでないユーザーが操作しようとしたらアラートを出す。
	 */
	public static void only51PerAdmin(String guiName) {
		if (!Glb.getObje().getCore().getManagerList().isIm51PerAdmin()) {
			Glb.getGui().alert(AlertType.WARNING, guiName,
					Lang.ONLY_51PER_ADMINISTRATOR.toString());
		}
	}

	public static void onlyMe(String guiName, Long userId) {
		Long myUserId = Glb.getMiddle().getMyUserId();
		if (myUserId == null || !myUserId.equals(userId)) {
			Glb.getGui().alert(AlertType.WARNING, guiName,
					Lang.ONLY_ME.toString());
		}
	}

	public static boolean isRegisterer(Long registererUserId) {
		Long myUserId = Glb.getMiddle().getMyUserId();
		if (myUserId != null && myUserId.equals(registererUserId)) {
			return true;
		}
		return false;
	}

	public static void onlyRegisterer(String guiName, Long registererUserId) {
		if (!isRegisterer(registererUserId))
			Glb.getGui().alert(AlertType.WARNING, guiName,
					Lang.ONLY_REGISTERER.toString());
	}

	/**
	 * {@link Model}のGUI部品を構築
	 *
	 * @param builder
	 * @param grid
	 * @param init
	 * @param elapsed
	 * @param ctx
	 * @param built
	 * @return
	 */
	public static int buildId(GuiBuilder builder, GridPane grid, int elapsed,
			CRUDContext ctx, ModelGui built) {
		if (ctx != CRUDContext.CREATE) {
			boolean editable = CRUDContext.editableBase(ctx);
			//ID
			String idLabel = Lang.ID.toString();
			Label idLabelGui = new Label(idLabel);
			idLabelGui.setId(builder.id() + "Id");
			grid.add(idLabelGui, 0, elapsed);
			TextField idInput = new TextField();
			idInput.setEditable(editable);
			idInput.setId(builder.id() + "IdInput");
			grid.add(idInput, 1, elapsed);
			elapsed += 1;
			built.setIdInput(idInput);
			idInput.setTextFormatter(getIdFormatter());
		}
		return elapsed;
	}

	public static TextFormatter<Long> getIdFormatter() {
		return new TextFormatter<Long>(change -> {
			if (change.getControlNewText().equals("-"))
				return change;
			if (change.getControlNewText().length() == 0)
				return change;
			try {
				Long id = Long.valueOf(change.getControlNewText());
				if (!Model.validateIdStandard(id))
					return null;
			} catch (Exception e) {
				return null;
			}
			return change;
		});
	}

	public static TextFormatter<String> getTextFormatterValidation(
			Function<Change, ValidationResult> validate) {
		return new TextFormatter<String>(change -> {
			if (change.getControlNewText().length() == 0)
				return change;
			try {
				ValidationResult vr = validate.apply(change);
				if (!vr.isNoError())
					return null;
			} catch (Exception e) {
				return null;
			}
			return change;
		});
	}

	/**
	 * 1行かつ横幅最大のテキストを表示。
	 *
	 * @param grid
	 * @param elapsed
	 * @param subTitle
	 * @param guiId
	 * @return
	 */
	public static int buildSubTitle(GridPane grid, int elapsed, String subTitle,
			String guiId) {
		Label label = new Label(subTitle);

		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().add(label);

		label.setId(guiId);
		label.setFocusTraversable(false);

		grid.add(box, 0, elapsed, 2, 1);
		elapsed += 1;

		return elapsed;
	}

	/**
	 * {@link AdministratedObjectI}のGUI部品を構築
	 *
	 * @param builder
	 * @param grid
	 * @param init
	 * @param elapsed
	 * @param ctx
	 * @param built
	 * @return
	 */
	public static int buildAdministratedObject(GuiBuilder builder, GridPane grid,
			int elapsed, CRUDContext ctx, AdministratedObjectGui built) {
		elapsed = GuiCommon.buildId(builder, grid, elapsed, ctx, built);
		if (ctx != CRUDContext.CREATE) {
			boolean editable = CRUDContext.editableBase(ctx);
			Label label = new Label(
					Lang.ADMINISTRATEDOBJECT_REGISTERER_NAME.toString());
			label.setId(builder.id() + "RegistererName");
			grid.add(label, 0, elapsed);
			TextField input = new TextField();
			input.setEditable(editable);
			input.setId(builder.id() + "RegistererNameInput");
			built.setRegistererNameInput(input);
			grid.add(input, 1, elapsed);
			elapsed += 1;

			Label admLabel = new Label(
					Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR_NAME.toString());
			admLabel.setId(builder.id() + "AdministratorName");
			grid.add(admLabel, 0, elapsed);
			TextField admInput = new TextField();
			admInput.setEditable(editable);
			admInput.setId(builder.id() + "AdministratorNameInput");
			built.setMainAdministratorNameInput(admInput);
			grid.add(admInput, 1, elapsed);
			elapsed += 1;
		}

		return elapsed;
	}

	/*
		public static Function<SubmitButtonGui, Boolean> getTableViewRemoveFunc(
				TableView<?> table) {
			return gui -> {
				Object selected = table.getSelectionModel().getSelectedItems();
				return table.getItems().remove(selected);
			};
		}
	*/
	/**
	 * 呼び出し側でelapsed+2
	 *
	 * @param grid
	 * @param elapsed
	 * @param builder
	 * @return
	 */
	public static SubmitButtonGui buildSubmitButton(GridPane grid, int elapsed,
			GuiBuilder builder, Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildSubmitButton(grid, elapsed, builder.name(), builder.id(),
				validateFunc, sendFunc, successFunc, failedFunc);
	}

	/**
	 * 送信ボタンを構築
	 * @param grid
	 * @param elapsed
	 * @param buttonName
	 * @param guiId
	 * @param validateFunc		FXスレッドで実行される
	 * @param sendFunc			描画を停止させないよう通常スレッドで実行される
	 * @param successFunc		sendFunc実行完了後、通常スレッドから呼び出されたFXスレッドで実行される
	 * @param failedFunc		sendFunc実行完了後、通常スレッドから呼び出されたFXスレッドで実行される
	 * @return
	 */
	public static SubmitButtonGui buildSubmitButton(GridPane grid, int elapsed,
			String buttonName, String guiId,
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildSubmitButton(n -> grid.add(n, 1, elapsed), elapsed,
				buttonName, guiId, validateFunc, sendFunc, successFunc,
				failedFunc);
	}

	/**
	 * VBoxに設置する
	 *
	 * @param box
	 * @param elapsed		VBox上の位置
	 * @param buttonName
	 * @param guiId
	 * @param validateFunc
	 * @param sendFunc
	 * @param successFunc
	 * @param failedFunc
	 * @return				VBox上の位置がいくつ進んだか
	 */
	public static SubmitButtonGui buildSubmitButton(VBox box, int elapsed,
			String buttonName, String guiId,
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildSubmitButton(n -> box.getChildren().add(n), elapsed,
				buttonName, guiId, validateFunc, sendFunc, successFunc,
				failedFunc);
	}

	public static SubmitButtonGui buildSubmitButton(Consumer<Node> setter,
			int elapsed, String buttonName, String guiId,
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {

		SubmitButtonGui r = new SubmitButtonGui();
		HBox submitPane = new HBox(10);
		submitPane.setId(guiId + "SubmitPane");
		submitPane.setAlignment(Pos.BOTTOM_RIGHT);

		final Text submitMessage = new Text();
		submitMessage.setId(guiId + "SubmitMessage");
		submitMessage.setFocusTraversable(true);
		submitPane.getChildren().add(submitMessage);
		r.setSubmitMessage(submitMessage);

		Button submit = new Button(buttonName);
		submit.setId(guiId + "SubmitButton");
		submit.setFocusTraversable(true);
		submit.setDefaultButton(true);
		submitPane.getChildren().add(submit);
		r.setSubmitButton(submit);

		setter.accept(submitPane);
		elapsed += 1;

		submit.setOnAction((ev) -> {
			try {
				//連続送信防止
				if (submit.isDisable())
					return;
				//ボタンが押せなくなる
				submit.setDisable(true);

				r.message("", Color.BLACK);
				if (!validateFunc.apply(r)) {
					submit.setDisable(false);
					return;
				}
				if (sendFunc != null) {
					r.message(Lang.PROCESSING.toString(), Color.BLACK);
					Glb.getGui().executeAsync(() -> {
						boolean send = sendFunc.apply(r);
						Glb.getGui().runByFXThread(() -> {
							//ボタンを押せるようにする
							submit.setDisable(false);
							if (send) {
								if (successFunc != null)
									successFunc.accept(r);
								r.message(Lang.SUCCESS.toString(), Color.BLACK);
							} else {
								if (failedFunc != null)
									failedFunc.accept(r);
								r.message(Lang.FAILED.toString());
							}
						});
					});
				} else {
					//いちいちこのように書かないといけない
					//finallyに書くとexecuteAsyncで戻すという事ができない
					submit.setDisable(false);
				}
			} catch (Exception e) {
				try {
					r.message(Lang.EXCEPTION.toString(), Color.FIREBRICK);
					submit.setDisable(false);
				} catch (Exception e2) {
					Glb.debug(e);
				}
				Glb.debug(e);
				return;
			}
		});

		return r;
	}

	/*
	public static List<UploadFileGui> getFilesFromDir(Path start,
			SubmitButtonGui gui, ValidationResult vr) {
		List<UploadFileGui> r = new ArrayList<>();

		int startLen = start.toString().length() + 1;
		try {
			Files.walk(start).filter(Files::isRegularFile).forEach(arg -> {
				if (!vr.isNoError())
					return;
				String relativePath = arg.toString().substring(startLen);
				IndividualityObject.validateName(relativePath, vr, TenyutalkFile.nameMax);
				if (!vr.isNoError()) {
					gui.message(vr);
					return;
				}
				UploadFileGui f = new UploadFileGui();
				f.setOriginalPath(arg);
				f.setDirAndFilenameFromSpecifiedFolder(relativePath);
				r.add(f);
			});
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		if (!vr.isNoError())
			return null;
		return r;
	}

	public static boolean moveFiles(List<UploadFileGui> items,
			SubmitButtonGui gui) throws Exception {
		//ファイルをアプリケーションフォルダに移動する
		for (UploadFileGui item : items) {
			Path p = item.getInAppPath();
			File parent = Glb.getFile().get(p.getParent());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					gui.message(Lang.ERROR_FAILED_TO_MKDIR.toString());
					return false;
				}
			}
			//既にあれば上書き
			try {
				Files.copy(item.getOriginalPath(), p,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				Glb.debug(e);
				gui.message(Lang.ERROR_FAILED_TO_FILE_MOVE.toString());
				return false;
			}
		}
		return true;
	}

*/
}
