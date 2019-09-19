package bei7473p5254d69jcuat.tenyu.release1.communication.request;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * RequestはResponseと対になる。同じファイルに定義される。
 * Request送信時処理（static send()）、Request受信時処理、
 * Response送信時処理（Request受信時処理の一部）、Response受信時処理がある。
 * リクエスト系はこのような構成で記述される。
 *
 * なおsend()はメソッドシグネチャを統一できないので抽象化されていない。
 * RequestSequenceStartという空インターフェースを実装している。
 *
 * received()はそのメッセージを受け取った側で実行される。
 *
 * {@link Request#received(io.netty.channel.ChannelHandlerContext, Received)}は、
 * 大抵、リクエストによって送られてきた情報に関する処理を行った後、
 * レスポンスのデータを作成し返信する流れになる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class Request extends Content {
	protected abstract boolean validateRequestConcrete(Message m);

	@Override
	protected final boolean validateConcreteContent(Message m) {
		return validateRequestConcrete(m);
	}

	public void exceptionCaught(StandardResponse res) {
		Glb.getLogger().warn("ResultCode:" + res.getCode().name());
	}

	/**
	 * 対応するレスポンスメッセージならtrueを返す。
	 */
	public abstract boolean isValid(Response res);

	/**
	 * レスポンスを受信すると設定される
	 * リクエストの通信が完了してresがnullなら返信失敗、
	 * nullでなければ通信成功を意味する。
	 */
	private transient Message res;

	/**
	 * 梱包やデテクタや内容を含んだ最大サイズ。
	 * 1MBくらいまで性能劣化が無いのでデフォルトで大きめにする。
	 * もしこれを超える場合、子クラスでオーバーライドする。
	 */
	public int getResponseTotalSize() {
		return 1000 * 5;
	};

	/**
	 * @return	接続失敗またはresponder側でResultCodeを決定できずに例外が発生したらnull、
	 * 成功したらその通信シーケンスに依存した返信または返信無し、
	 * それ以外の場合{@link StandardResponse}
	 */
	public Message getRes() {
		return res;
	}

	public boolean setRes(Message res) {
		if (this.res != null) {
			Glb.debug(new Exception(
					"レスポンスがnullじゃない。リクエストのインスタンスを使いまわしていないかチェック resContent="
							+ res.getContent() + " req=" + this));
			return false;
		} else {
			Glb.debug("res=" + res);
		}
		this.res = res;
		return true;
	}

}
