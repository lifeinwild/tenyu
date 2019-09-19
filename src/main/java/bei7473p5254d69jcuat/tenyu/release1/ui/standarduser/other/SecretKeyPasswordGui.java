package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.other;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.other.SecretKeyPasswordGui.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SecretKeyPasswordGui extends ModelGui<SecretKeyPassword> {
	public static class SecretKeyPassword {
		private String password;
		private String confirm;

		public boolean validate(ValidationResult vr) {
			if (password == null || password.length() == 0) {
				vr.add(Lang.SECRETKEYPASSWORD, Lang.ERROR_EMPTY);
			}

			Glb.getConf().validateSecretKeyPassword(password, vr);

			if (confirm == null || !password.equals(confirm)) {
				vr.add(Lang.SECRETKEYPASSWORD,
						Lang.ERROR_NOT_EQUAL_CONFIRMATION);
			}
			return vr.isNoError();
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getConfirm() {
			return confirm;
		}

		public void setConfirm(String confirm) {
			this.confirm = confirm;
		}

	}

	@Override
	public void set(SecretKeyPassword o) {
		newPasswordInput.setText(o.getPassword());
		confirm.setText(o.getConfirm());
	}

	public SecretKeyPasswordGui(String name, String id) {
		super(name, id);
	}

	private Label newPasswordLabel;
	private PasswordField newPasswordInput;
	private Label confLabel;
	private PasswordField confirm;
	private SubmitButtonGui changeButton;

	public SecretKeyPassword setupModel() {
		SecretKeyPassword r = new SecretKeyPassword();
		r.setConfirm(confirm.getText());
		r.setPassword(newPasswordInput.getText());
		return r;
	}

	public GridPane build() {
		newPasswordLabel = new Label(name);
		newPasswordInput = new PasswordField();
		grid.add(newPasswordLabel, 0, elapsed);
		grid.add(newPasswordInput, 1, elapsed);
		elapsed += 1;

		confLabel = new Label(Lang.PASSWORD_CONFIRM.toString());
		confirm = new PasswordField();
		grid.add(confLabel, 0, elapsed);
		grid.add(confirm, 1, elapsed);
		elapsed += 1;

		return grid;
	}

	public void clear() {
		newPasswordInput.clear();
		confirm.clear();
	}

	public Label getNewPasswordLabel() {
		return newPasswordLabel;
	}

	public void setNewPasswordLabel(Label newPasswordLabel) {
		this.newPasswordLabel = newPasswordLabel;
	}

	public PasswordField getNewPasswordInput() {
		return newPasswordInput;
	}

	public void setNewPasswordInput(PasswordField newPasswordInput) {
		this.newPasswordInput = newPasswordInput;
	}

	public Label getConfLabel() {
		return confLabel;
	}

	public void setConfLabel(Label confLabel) {
		this.confLabel = confLabel;
	}

	public PasswordField getConfirm() {
		return confirm;
	}

	public void setConfirm(PasswordField confirm) {
		this.confirm = confirm;
	}

	public SubmitButtonGui getChangeButton() {
		return changeButton;
	}

	public void setChangeButton(SubmitButtonGui changeButton) {
		this.changeButton = changeButton;
	}

}