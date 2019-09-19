package bei7473p5254d69jcuat.tenyu.release1.communication;

import java.net.*;

import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

/**
 * 受信毎に入手可能な情報を置いていく。
 * Message+アドレス
 * nettyのctxもアドレスを提供しうるが、
 * ctx依存のコードが増えるとNettyから分離できなくなる。
 * ctxは結局receivedに渡されるが、返信のために再度P2Pクラスに渡されるだけで、
 * 他の用途を持たず、分離可能な状況を保つ。
 *
 * TODO：ctxをReceivedのメンバーにすべきか？
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Received {
	private byte[] addr;
	private Message message;

	public Received(InetSocketAddress isa, Message m) {
		addr = isa.getAddress().getAddress();
		this.message = m;
	}

	public byte[] getAddr() {
		return addr;
	}

	public Message getMessage() {
		return message;
	}

	/**
	 * Message#getEdgeByInnermostPackage()の透過
	 */
	public P2PEdge getEdgeByInnermostPackage() {
		return message.getEdgeByInnermostPackage();
	}
}