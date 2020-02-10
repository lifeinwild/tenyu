package bei7473p5254d69jcuat.tenyu.communication.request;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * このあたりの設計について。
 * StandardExceptionResponseは通信における例外処理のようなものである。
 * リクエストを受信した時、メッセージ種別すら判別できずに処理が失敗する場合があり、
 * 通信処理の種類に付随してそのような場合の処理を書く事はできないので、
 * そのような場合の共通型として返信される。
 * そのためRequestはStandardExceptionResponseを受け取り例外処理をするメソッドを備える。
 * try catchのcatchに似たイメージ。子クラスでオーバーライドできる。
 *
 * 通常、SUCCESSならその通信処理の種類に応じた型で返信される。
 * しかし、AbstractStandardResponse系が標準的な返信型である通信処理もありえて、
 * SUCCESSも使用される可能性があるので定義されている。
 * 標準型に指定されている場合、それが返信されても
 * Requestの例外処理は呼び出されず、receivedが呼び出される。
 * 標準型はisValid(Resopnse res)でtrueを返すクラス。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AbstractStandardResponse extends Response {
	private static final long max = 1;

	private static final long period = 1000L * 60 * 5;
	private static final ThroughputLimit<
			P2PEdge> p2pEdgeRetryLimit = new ThroughputLimit<>(period, max);

	private static final ThroughputLimit<
			NodeIdentifier> userRetryLimit = new ThroughputLimit<>(period, max);

	protected ResultCode code = ResultCode.DEFAULT;

	public ResultCode getCode() {
		return code;
	}

	public boolean isSuccess() {
		if (getCode() == null) {
			return false;
		}
		return getCode().equals(ResultCode.SUCCESS);
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		Response res = (Response) validated.getMessage().getContent();
		Message reqM = res.getReq();

		//コードに応じて失敗の計測等をする
		switch (code) {
		case FAIL:
			break;
		case OVER_SIZE:
			break;
		case UNPACKAGE_FAILED:
			//共通鍵梱包で開梱に失敗した場合、共通鍵を再交換する
			//ただし前回の交換から十分に時間が経過している場合のみ
			if (reqM.getInnermostPack() instanceof P2PEdgeCommonKeyPackage) {
				P2PEdge e = reqM.getEdgeByInnermostPackage();
				if (e != null) {
					if (!p2pEdgeRetryLimit.isOverCount(e)) {
						Recognition.send(e.getNode().getAddr(),
								e.getNode().getP2pPort());
					}
				}
			} else if (reqM
					.getInnermostPack() instanceof UserCommonKeyPackage) {
				NodeIdentifierUser identifier = reqM.getIdentifierUser();
				if (identifier != null) {
					if (!userRetryLimit.isOverCount(identifier)) {
						CommonKeyExchangeUser.send(identifier);
					}
				}
			}
			break;
		default:
		}
		return true;
	}

	public void setCode(ResultCode code) {
		this.code = code;
	}

	protected abstract boolean validateAbstractStandardResponseConcrete(
			Message m);

	@Override
	protected final boolean validateResponseConcrete(Message m) {
		return code != null && validateAbstractStandardResponseConcrete(m);
	}

	/**
	 * 通信の処理結果。
	 *
	 * これら以外でも接続失敗、接続成功したが一切返信が無かった、
	 * という場合がありうる。
	 *
	 * SUCCESSの場合、P2Pクラスは返信しない。
	 * SUCCESS以外の場合、P2Pクラスが返信するのでハンドラは何も返信してはいけない。
	 * ハンドラのreceived()の返値booleanによってP2Pクラスで返信が行われるかが決まる。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static enum ResultCode {
		DEFAULT(Lang.STANDARDRESPONSE_RESULT_DEFAULT),
		DUPLICATE,
		FAIL(Lang.STANDARDRESPONSE_RESULT_FAIL),
		INVALID_ADDR,
		INVALID_CLASS,
		INVALID_CONTENT,
		OVER_COUNT,
		OVER_SIZE(Lang.STANDARDRESPONSE_RESULT_OVER_SIZE),
		/**
		 * received()で失敗
		 */
		PROC_FALSE,
		SUCCESS(Lang.STANDARDRESPONSE_RESULT_SUCCESS),
		/**
		 * 開梱に失敗した
		 */
		UNPACKAGE_FAILED;

		private Lang lang;

		private ResultCode(Lang lang) {
			this.lang = lang;
		}

		private ResultCode() {
		}

		public String getLang() {
			if (lang == null)
				return name();
			return lang.toString();
		}
	}

}
