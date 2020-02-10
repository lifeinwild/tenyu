package bei7473p5254d69jcuat.tenyu.communication.request.gui.manager;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.SignedPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import io.netty.channel.*;

/**
 * 全体運営者によるManagerAgendaに対する投票。
 * @author exceptiontenyu@gmail.com
 *
 */
public class ManagerVote extends Request implements SignedPackageContent {

	@Override
	protected boolean validateRequestConcrete(Message m) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isValid(Response res) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
