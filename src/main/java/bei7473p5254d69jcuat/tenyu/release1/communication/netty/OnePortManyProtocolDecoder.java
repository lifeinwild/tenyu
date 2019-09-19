package bei7473p5254d69jcuat.tenyu.release1.communication.netty;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.*;
import io.netty.handler.stream.*;
import io.netty.handler.timeout.*;

/**
 * Port Unification
 * 1ポート多プロトコルを実現する
 * 最初の1バイト目がプロトコルID
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class OnePortManyProtocolDecoder extends ByteToMessageDecoder {
	/**
	 * プロトコル１の送受信可能最大サイズ
	 */
	public static final int maxFrame = 1000 * 65;
	/**
	 * 小さなメッセージを送受信する場合のタイムアウト時間
	 */
	public static final int transferTimeout = 7;//second
	public static final int lengthFieldLength = 4;

	/**
	 * このハンドラが使用されるのはサーバー側か。
	 * 常駐して任意のデータを受信するサーバーと
	 * リクエストに対するレスポンスを受信するクライアントでは
	 * セキュリティへの影響が異なる。
	 */
	private final boolean server;

	/**
	 * リクエスト情報が受信時に使えた方が良い場合があるので用意した。
	 * ファイル受信時にレジュームの開始位置情報を相手側に依存せず決定する等。
	 * Decoderでリクエスト情報が存在するのはリクエスト側が返信を受信する場合。
	 */
	private ChannelInboundHandler req;

	public OnePortManyProtocolDecoder(boolean server) {
		this.server = server;
	}

	/**
	 * プロトコルがリクエスト情報を必要とする場合
	 * @param server
	 * @param req
	 */
	public OnePortManyProtocolDecoder(boolean server,
			ChannelInboundHandler req) {
		this.server = server;
		this.req = req;
	}

	/**
	 * @param contentSize	送信するメッセージのサイズ
	 * @return				プロトコルID
	 */
	public static byte getProtocolId(int contentSize) {
		if (contentSize <= OnePortManyProtocolDecoder.maxFrame) {
			return 1;
		} else {
			return 2;
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		//パイプラインを動的に設定する
		ChannelPipeline p = ctx.pipeline();

		/*
		byte b = in.readByte();
		System.out.println(b);
		int size = in.readInt();
		System.out.println(size);
		in.resetReaderIndex();
		*/
		byte protocolId = in.readByte();
		Glb.debug("called protocolId=" + protocolId);
		switch (protocolId) {
		case 1:
			//small message
			//読み込みタイムアウト。プロトコルによって異なるのでここで登録する
			p.addAfter(OnePortManyProtocolDecoder.class.getSimpleName(),
					ReadTimeoutHandler.class.getSimpleName(),
					new ReadTimeoutHandler(transferTimeout));
			//送信時にヘッダを書き込む
			//			p.addAfter(ReadTimeoutHandler.class.getSimpleName(),
			//				LengthFieldPrepender.class.getSimpleName(),
			//			new LengthFieldPrepender(lengthFieldLength));
			//受信時にヘッダを読み込み、かつメッセージを復元する。
			p.addAfter(ReadTimeoutHandler.class.getSimpleName(),
					LengthFieldBasedFrameDecoder.class.getSimpleName(),
					new LengthFieldBasedFrameDecoder(maxFrame, 0,
							lengthFieldLength, 0, lengthFieldLength));
			break;
		case 2:
			//big message
			int readTimeout = ChunkedDataConcatMemory.transferTimeout;
			p.addAfter(OnePortManyProtocolDecoder.class.getSimpleName(),
					ReadTimeoutHandler.class.getSimpleName(),
					new ReadTimeoutHandler(readTimeout));
			//ChunkedWriteHandlerは双方向ハンドラ。
			//双方向ハンドラは、送信と受信両方をやる場合、重複して登録されてしまう場合がある。
			//とはいえ、送受信のうち片方をやるだけの場合でも登録が必要なので、条件分岐で重複登録を防ぐしかない。
			if (p.get(ChunkedWriteHandler.class.getSimpleName()) == null) {
				p.addAfter(ReadTimeoutHandler.class.getSimpleName(),
						ChunkedWriteHandler.class.getSimpleName(),
						new ChunkedWriteHandler());
			}
			p.addAfter(ChunkedWriteHandler.class.getSimpleName(),
					ChunkedDataConcatMemory.class.getSimpleName(),
					new ChunkedDataConcatMemory());
			break;
		case 3:
			//file
			//クライアント側のみファイル受信を許可
			//リクエストに対するレスポンスの場合のみファイルを受信するという事
			//常駐したサーバーが任意のファイルを受信する事はややリスクが高い。
			if (server) {
				Exception e = new IllegalArgumentException();
				Glb.getLogger().error("protocol3 file pushing to server", e);
				in.clear();
				ctx.close();
				throw e;
			}

			//ファイル受信ではリクエスト情報が必要
			if (req == null || !(req instanceof RequestFileHandler)) {
				throw new Exception("not RequestFileHandler");
			}
			RequestFileHandler rfh = (RequestFileHandler) req;

			p.addAfter(OnePortManyProtocolDecoder.class.getSimpleName(),
					ReadTimeoutHandler.class.getSimpleName(),
					new ReadTimeoutHandler(
							ChunkedDataConcatFile.transferTimeout));
			if (p.get(ChunkedWriteHandler.class.getSimpleName()) == null) {
				p.addAfter(ReadTimeoutHandler.class.getSimpleName(),
						ChunkedWriteHandler.class.getSimpleName(),
						new ChunkedWriteHandler());
			}
			p.addAfter(ChunkedWriteHandler.class.getSimpleName(),
					ChunkedDataConcatFile.class.getSimpleName(),
					new ChunkedDataConcatFile(rfh));
			break;
		default:
			//例外
			Exception e = new IllegalArgumentException();
			Glb.getLogger().error("", e);
			in.clear();
			ctx.close();
			throw e;
		}
		//1回の接続で多数のメッセージが送受信されるが
		//パイプライン構築は1回の接続につき1回でいい
		//削除しないとメッセージ送受信のたびにここのコードが呼ばれてしまう
		p.remove(this);

		//out.add(in.retain());
		//	ctx.fireChannelRead(in);
	}

}
