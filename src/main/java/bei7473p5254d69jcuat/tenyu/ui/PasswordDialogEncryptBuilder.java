package bei7473p5254d69jcuat.tenyu.ui;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class PasswordDialogEncryptBuilder extends GuiBuilder {
	public static Node passwordDialog(boolean encrypt, String id,
			GridPane passwordForm) {
		Dialog<ButtonType> passwordDialog = new Dialog<>();
		passwordDialog.setTitle(encrypt ? Lang.PASSWORD_ENCRYPT_TITLE.toString()
				: Lang.PASSWORD_DECRYPT_TITLE.toString());
		passwordDialog.setHeaderText(Lang.PASSWORD_HEADER.toString());
		ObservableList<ButtonType> buttons = passwordDialog.getDialogPane()
				.getButtonTypes();
		try {
			Stage dialog = (Stage) passwordDialog.getDialogPane().getScene()
					.getWindow();
			dialog.getIcons().add(Glb.getFile().getIcon());
		} catch (Exception e) {

		}

		PasswordField passwordInput = new PasswordField();
		passwordInput.setId(id + "PasswordInput");
		passwordInput.setPromptText(Lang.PASSWORD.toString());

		PasswordField confirmInput = new PasswordField();
		confirmInput.setId(id + "ConfirmInput");
		confirmInput.setPromptText(Lang.PASSWORD.toString());

		ButtonType ok = ButtonType.OK;
		buttons.add(ok);
		Node okNode = passwordDialog.getDialogPane().lookupButton(ok);
		if (okNode != null)
			okNode.setId(id + "Ok");
		if (encrypt && okNode != null) {
			okNode.addEventFilter(ActionEvent.ACTION, event -> {
				String in1 = passwordInput.getText();
				String in2 = confirmInput.getText();
				ValidationResult vr = Glb.getConf()
						.validateSecretKeyPassword(in1);
				if (!vr.isNoError()) {
					passwordDialog.setHeaderText(vr.toString());
					event.consume();
					return;
				}
				if (!in1.equals(in2)) {
					passwordDialog
							.setHeaderText(Lang.PASSWORD_NOT_EQUAL.toString());
					event.consume();
					return;
				}
			});
		}

		ButtonType cancel = ButtonType.CANCEL;
		buttons.add(cancel);
		Node cancelNode = passwordDialog.getDialogPane().lookupButton(cancel);
		cancelNode.setId(id + "Cancel");
		cancelNode.addEventFilter(ActionEvent.ACTION, event -> {
			try {
				event.consume();
				cancelNode.setVisible(false);
				Glb.getApp().stop();
			} catch (Exception e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
		});

		passwordForm.setId(id + "PasswordForm");
		passwordForm.setHgap(10);
		passwordForm.setVgap(10);
		passwordForm.setPadding(new Insets(20, 150, 10, 10));
		int elapsed = 0;
		Label passwordLabel = new Label(Lang.PASSWORD.toString());
		passwordLabel.setId(id + "PasswordLabel");
		passwordForm.add(passwordLabel, 0, elapsed);
		passwordForm.add(passwordInput, 1, elapsed);
		elapsed += 1;

		if (encrypt) {
			Label confirmLabel = new Label(Lang.PASSWORD_CONFIRM.toString());
			confirmLabel.setId(id + "ConfirmLabel");
			passwordForm.add(confirmLabel, 0, elapsed);
			passwordForm.add(confirmInput, 1, elapsed);
			elapsed += 1;
		}

		passwordDialog.getDialogPane().setContent(passwordForm);

		while (true) {
			try {
				Optional<ButtonType> r = passwordDialog.showAndWait();
				Glb.debug("" + r.isPresent());

				ButtonType bt = r.orElseGet(() -> null);
				if (bt == null
						|| bt.getButtonData() == ButtonData.CANCEL_CLOSE) {
					Glb.getApp().stop();
					break;
				}
				if (r.isPresent()) {
					String password = passwordInput.getText();

					//現在のタイトルをバックアップする
					Gui gui = Glb.getGui();
					String backup = gui.getPrimary().getTitle();

					//もし待機メッセージが渡されたならタイトルに設定
					if (encrypt)
						gui.setTitle(null,
								Lang.KEY_GENERATION_WAIT_1MINUTE.toString());

					//この処理は時間がかかる場合がある
					Glb.getConf().init2(password);

					//待機メッセージが渡されたなら元のタイトルに戻す
					if (encrypt)
						gui.getPrimary().setTitle(backup);
				}

				//鍵のセットアップが出来ていなければ繰り返し
				if (Glb.getConf().getMyStandardPublicKey() == null) {
					passwordDialog.setHeaderText(
							Lang.PASSWORD_KEY_SETUP_FAILED.toString());
				} else {
					break;
				}
			} catch (Exception e) {
				Glb.debug(e);
			}
		}
		return passwordForm;
	}

	@Override
	public Node build() {
		//鍵未作成の場合、確認入力を求める
		return passwordDialog(true, id(), grid());
	}

	@Override
	public String name() {
		return Lang.PASSWORD_ENCRYPT_TITLE.toString();
	}

	@Override
	public String id() {
		return "passwordDialogEncrypt";
	}

}
