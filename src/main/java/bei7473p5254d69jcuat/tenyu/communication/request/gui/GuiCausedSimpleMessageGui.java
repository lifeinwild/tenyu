package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.P2PEdgeCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.UserCommonKeyPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import glb.*;

/**
 * 手動または自動的に作成され、受信者にGUIを表示させるメッセージ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GuiCausedSimpleMessageGui extends GuiCausedRequest {
	/**
	 * アラートの内容
	 */
	protected String content;
	/**
	 * アラートのタイトル
	 */
	protected String title;

	public GuiCausedSimpleMessageGui() {
	}

	public String getContent() {
		return content;
	}

	@Override
	public String getName() {
		return title;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof StandardResponseGui;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	protected boolean validateGuiCausedConcrete(Message m) {
		return title != null && title.length() > 0 && content != null
				&& content.length() > 0;
	}

	public static class GuiCausedSimpleMessageGuiP2PEdge
			extends GuiCausedSimpleMessageGui
			implements P2PEdgeCommonKeyPackageContent {

		@Override
		protected ResultCode receivedConcrete(Received validated) {
			boolean over = Glb.getP2pDefense().isOverCountSimpleMessage(
					validated.getEdgeByInnermostPackage());
			if (over) {
				return ResultCode.OVER_COUNT;
			}

			Glb.getGui().appendLogForUser(title, content);
			return ResultCode.SUCCESS;
		}
	}

	public static class GuiCausedSimpleMessageGuiUser extends
			GuiCausedSimpleMessageGui implements UserCommonKeyPackageContent {

		@Override
		protected ResultCode receivedConcrete(Received validated) {
			boolean over = Glb.getP2pDefense().isOverCountSimpleMessage(
					validated.getMessage().getUserId());
			if (over) {
				return ResultCode.OVER_COUNT;
			}

			Glb.getGui().appendLogForUser(title, content);
			return ResultCode.SUCCESS;
		}
	}
}
