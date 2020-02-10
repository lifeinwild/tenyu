package bei7473p5254d69jcuat.tenyu.communication.netty;

import java.util.*;

import glb.*;
import io.netty.channel.*;
import io.netty.handler.codec.*;
import io.netty.handler.stream.*;
import io.netty.handler.timeout.*;
import io.netty.util.*;

/**
 * プロトコルIDを書き込む必要から作成。
 * さらにLengthFieldPrependerを使わず、その機能をこちらに再実装。
 * 独自実装の方がChunkedWriteHandlerへの対応等がしやすいし、どうせ大したコード量にならない。
 *
 * TODO:ほかに良い実装方法があるか？
 *
 * 総称型にObjectを指定するとChunked系の送信でも機能する。
 * ByteBufではそうならない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class OnePortManyProtocolEncoder
		extends MessageToMessageEncoder<Object> {

	/**
	 * TODO:Enumにしたいが、そうすると受信時にEnumの要素を特定するのにループ処理が必要になる
	 *
	 * 1=small message
	 * 2=big message
	 * 3=file
	 */
	protected byte protocolId;

	private int size;

	public OnePortManyProtocolEncoder(byte protocolId, int size) {
		this.protocolId = protocolId;
		this.size = size;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg,
			List<Object> out) throws Exception {
		Glb.debug("called protocolId=" + protocolId);
		//最初のメッセージだけこのコードを実行する
		ctx.pipeline().remove(this);

		//プロトコルIDを書き込む
		out.add(ctx.alloc().buffer(1)//.order(ByteOrder.BIG_ENDIAN)
				.writeByte(protocolId));

		switch (protocolId) {
		case 1:
		case 2:
			//サイズ長を書き込む
			out.add(ctx.alloc()
					.buffer(OnePortManyProtocolDecoder.lengthFieldLength)
					//.order(ByteOrder.BIG_ENDIAN)
					.writeInt(size));
			break;
		case 3:
			//リクエスト時の情報から各種情報を取得する
			Glb.debug("encode by protocol3");
			break;
		default:
			break;
		}

		/*
		 * TODO:MessageToMessageEncoderのJavadocのサンプルコードによると
		 * outに追加する場合retain()の必要は無いように見えるが、
		 * このコードを書かないと参照カウントに関する例外が発生する。
		 */
		if (msg instanceof ReferenceCounted) {
			ReferenceCounted buf = (ReferenceCounted) msg;
			buf.retain();
		}

		//メッセージ本体を書き込む
		out.add(msg);
	}

	/*
		@Override
		protected void encode(ChannelHandlerContext ctx, ByteBuf msg,
				List<Object> out) throws Exception {
			Glb.debug("called");
			//最初のメッセージだけこのコードを実行する
			ctx.pipeline().remove(this);
			//プロトコルIDを書き込む
			out.add(ctx.alloc().buffer(1).order(ByteOrder.BIG_ENDIAN)
					.writeByte(protocolId));
			//ここまでのパイプラインが作成した最初のメッセージを書き込み
			//基本的にLengthField
			out.add(msg.retain());

		}
	public static OnePortManyProtocolEncoder forSmallMessage() {
		return new OnePortManyProtocolEncoder((byte) 1);
	}

	public static OnePortManyProtocolEncoder forBigMessage() {
		return new OnePortManyProtocolEncoder((byte) 2);
	}

	public static OnePortManyProtocolEncoder forFile() {
		return new OnePortManyProtocolEncoder((byte) 3);
	}
	*/

	/**
	 * パイプライン構築
	 * サーバー側は接続してヘッダを受け取ってからじゃないとパイプライン構築できないが、
	 * クライアント側なので接続前でもプロトコルIDが確定していて、
	 * 任意のタイミングで呼び出せる。
	 *
	 * @param ctx
	 * @param size			送信するメッセージのサイズ
	 */
	public static byte setupPipelineClient(ChannelPipeline p, int size,
			byte protocolId) {
		//(byte) (size <= OnePortManyProtocolDecoder.maxFrame
		//? 1				: 2);

		switch (protocolId) {
		case 1:
			//small message

			//TODO:送信時は最後から最初へ、が原則のようだが、不可解な動作をしている。
			//addFirstを3回やれば、3回目に入れたものが最初になるはずだが、そうではないようだ。
			//背景の理解が不足しているだけで、動作に問題は無い。

			//送信時タイムアウト処理
			p.addFirst(new WriteTimeoutHandler(
					OnePortManyProtocolDecoder.transferTimeout));
			//送信時にサイズをヘッダに書き込む
			//p.addFirst(new LengthFieldPrepender(
			//	OnePortManyProtocolDecoder.lengthFieldLength));
			//プロトコルID書き込み
			p.addFirst(new OnePortManyProtocolEncoder(protocolId, size));

			//p.addLast(new RandomWaitHandler());
			//p.addLast(new TestHandler());

			break;
		case 2:
			//big message

			//送信時タイムアウト処理
			int writeTimeout = ChunkedDataConcatMemory.transferTimeout;
			p.addFirst(new WriteTimeoutHandler(writeTimeout));
			//送信時にサイズをヘッダに書き込む
			//			p.addFirst(new LengthFieldPrepender(
			//				OnePortManyProtocolDecoder.lengthFieldLength));
			//プロトコルID書き込み
			p.addFirst(new OnePortManyProtocolEncoder(protocolId, size));
			//大容量データ対応処理
			//ChunkedWriteHandlerは双方向ハンドラ。
			//双方向ハンドラは、送信と受信両方をやる場合、重複して登録されてしまう場合がある。
			//とはいえ、送受信のうち片方をやるだけの場合でも登録が必要なので、条件分岐で重複登録を防ぐしかない。
			if (p.get(ChunkedWriteHandler.class.getSimpleName()) == null) {
				p.addFirst(ChunkedWriteHandler.class.getSimpleName(),
						new ChunkedWriteHandler());
			}
			break;
		case 3:
			//file
			int writeTimeoutFile = ChunkedDataConcatFile.transferTimeout;
			p.addFirst(new WriteTimeoutHandler(writeTimeoutFile));
			p.addFirst(new OnePortManyProtocolEncoder(protocolId, size));
			if (p.get(ChunkedWriteHandler.class.getSimpleName()) == null) {
				p.addFirst(ChunkedWriteHandler.class.getSimpleName(),
						new ChunkedWriteHandler());
			}
			break;
		default:
			//例外
			IllegalArgumentException e = new IllegalArgumentException();
			Glb.getLogger().error("", e);
			throw e;
		}
		return protocolId;

	}
}
