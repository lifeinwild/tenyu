package bei7473p5254d69jcuat.tenyu.release1.ui;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.scene.*;

public class PasswordDialogDecryptBuilder extends GuiBuilder {

	@Override
	public Node build() {
		//鍵作成済みの場合、確認入力不要
		return PasswordDialogEncryptBuilder.passwordDialog(false, id(), grid());
	}

	@Override
	public String name() {
		return Lang.PASSWORD_DECRYPT_TITLE.toString();
	}

	@Override
	public String id() {
		return "passwordDialogDecrypt";
	}

}
