package bei7473p5254d69jcuat.tenyu.release1.ui.common;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * 送信ボタン関連のGUI部品
 * 送信ボタンは主に1つの画面に1つだが、
 * 1画面に2つ以上送信ボタンがある場合もある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class SubmitButtonGui {
	private Button submitButton;
	private Text submitMessage;

	public void message(final String message) {
		message(message, Color.FIREBRICK);
	}

	public void message(final Lang message) {
		message(message.toString(), Color.FIREBRICK);
	}

	public void message(final ValidationResult message) {
		message(message.toString(), Color.FIREBRICK);
	}

	/**
	 * @param message	表示するメッセージ
	 * @param black		trueならblack, falseならred
	 */
	public void message(final String message, Color c) {
		if (submitMessage == null)
			return;
		Glb.getGui().runByFXThread(() -> {
			submitMessage.setFill(c);
			submitMessage.setText(message);
		});
	}

	public Button getSubmitButton() {
		return submitButton;
	}

	public void setSubmitButton(Button submitButton) {
		this.submitButton = submitButton;
	}

	public Text getSubmitMessage() {
		return submitMessage;
	}

	public void setSubmitMessage(Text submitMessage) {
		this.submitMessage = submitMessage;
	}

}