package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.PlainPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import glb.*;
import io.netty.channel.*;
import javafx.scene.control.Alert.*;

/**
 * 受信時にGUIのアラートを表示し、通信の成否をエンドユーザーに知らせる。
 * GUIを通じて手動操作で送信されたメッセージはこのクラスで返信される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StandardResponseGui extends AbstractStandardResponse
		implements PlainPackageContent {
	public StandardResponseGui() {
	}

	public StandardResponseGui(ResultCode code) {
		this.code = code;
	}

	@Override
	public boolean isValid(Request req) {
		return req instanceof Request;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		if (!super.received(ctx, validated)) {
			return false;
		}
		String reqName = Lang.STANDARDRESPONSE_RESULT.toString() + " "
				+ req.toString();

		switch (code) {
		case SUCCESS:
			Glb.getGui().alert(AlertType.INFORMATION, reqName, code.getLang());
			break;
		default:
			Glb.getGui().alert(AlertType.ERROR, reqName, code.toString());
			break;
		}
		return true;
	}

	public void setCode(ResultCode code) {
		this.code = code;
	}

	public ResultCode getCode() {
		return code;
	}

	@Override
	protected final boolean validateAbstractStandardResponseConcrete(
			Message m) {
		return true;
	}

}