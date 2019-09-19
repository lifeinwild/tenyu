package bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import io.netty.channel.*;

/**
 * 指定したユーザがオンラインかを確認する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@RequestSequenceStart
public class IsOnline extends UserRequest {
	private static final int rndSize = 32;

	public static boolean send(NodeIdentifierUser identifier) {
		if (identifier == null)
			return false;

		IsOnline req = new IsOnline();
		Message reqM = Message.build(req)
				.packaging(req.createPackage(identifier)).finish();
		Message resM = Glb.getP2p().requestSync(reqM, identifier);
		if (resM == null || !(resM.getContent() instanceof IsOnlineResponse))
			return false;
		IsOnlineResponse res = (IsOnlineResponse) resM.getContent();

		return res.isUserMessageServerMain()
				&& Arrays.equals(req.getRnd(), res.getRnd());
	}

	/**
	 * ランダム値
	 * 返信者はこれを含めて署名しなければならない
	 * これがないと過去のメッセージをコピーしておいて送信できてしまうので。
	 */
	private byte[] rnd = new byte[rndSize];

	public IsOnline() {
		Glb.getRnd().nextBytes(rnd);
	}

	public byte[] getRnd() {
		return rnd;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof IsOnlineResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		NodeIdentifierUser identifier = validated.getMessage()
				.getIdentifierUser();
		if (identifier == null) {
			Glb.getLogger().error("identifier is null.", new Exception());
			return false;
		}
		IsOnlineResponse res = new IsOnlineResponse(rnd);
		Message m = Message.build(res).packaging(res.createPackage(identifier))
				.finish();
		return Glb.getP2p().response(m, ctx);
	}

	public void setRnd(byte[] rnd) {
		this.rnd = rnd;
	}

	@Override
	protected final boolean validateRequestConcrete(Message m) {
		return rnd != null && rnd.length == rndSize;
	}

	/**
	 * このレスポンスが来たらオンライン
	 * UserPackageなので署名からUserIdも分かる
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class IsOnlineResponse extends UserResponse {
		private byte[] rnd = new byte[rndSize];
		//ここにこんな感じで特別な役割のノードとして動作しているかを記していく
		//特別な役割は客観に設定されているが、ここに記されるのは実際のノードの動作状況である
		private boolean userMessageServerMain = Glb.getMiddle()
				.getUserMessageListServer().isStarted();

		public IsOnlineResponse() {
		}

		public IsOnlineResponse(byte[] rnd) {
			this.rnd = rnd;
		}

		public byte[] getRnd() {
			return rnd;
		}

		public boolean isUserMessageServerMain() {
			return userMessageServerMain;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof IsOnline;
		}

		@Override
		public boolean received(ChannelHandlerContext con, Received validated) {
			return true;
		}

		public void setRnd(byte[] rnd) {
			this.rnd = rnd;
		}

		public void setUserMessageServerMain(boolean userMessageServerMain) {
			this.userMessageServerMain = userMessageServerMain;
		}

		@Override
		protected final boolean validateResponseConcrete(Message m) {
			return rnd != null && rnd.length == rndSize;
		}
	}
}
