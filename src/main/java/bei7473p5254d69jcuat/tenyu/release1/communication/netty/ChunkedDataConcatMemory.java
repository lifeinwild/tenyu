package bei7473p5254d69jcuat.tenyu.release1.communication.netty;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.util.*;

/**
 * 分割されたデータを受信し、メモリに保持し、結合する
 * 次のInboundHandlerにbyte[]を渡す
 * @author exceptiontenyu@gmail.com
 *
 */
public class ChunkedDataConcatMemory extends ChannelInboundHandlerAdapter {
	/**
	 * Javaで作成できる配列の要素数の上限は規定されていない。VM毎に違う。
	 * 出来るだけ多くのVMに対応するため、多めにトレランスを設定する。
	 */
	//	private static final int jvmTolerance = 1000;
	/**
	 * 受信可能な最大サイズ
	 * 120MBに設定している。
	 * 巨大なファイルの送受信は分割する必要がある。
	 */
	public static final int chunkedDataTotalSizeMax = 1000 * 1000 * 120;
	//	Integer.MAX_VALUE / dev			- jvmTolerance;
	//	private static final int dev =1000;
	/**
	 * 送受信のタイムアウト時間
	 */
	public static final int transferTimeout = 600;//second

	/**
	 * 結合されたデータ
	 * ByteArrayOutputStreamやByteBufferを検討したが、
	 * ByteArrayOutputStreamは中身のbyte[]を取得するAPIが無い。
	 * コンストラクタに渡すbyte[]を持っておいてbyte[]の伸長が起こらない使い方なら
	 * 問題無いだろうがトリッキーなのでやめた。
	 * ByteBufferはByteBuf#getBytes()で動作しなかった。
	 */
	private byte[] whole;
	/**
	 * 書き込んだサイズ
	 */
	private int count;

	/**
	 * ヘッダで宣言されたデータ全体のサイズ
	 * wholeのバッファサイズ
	 */
	private int size;
	/**
	 * 初回チャンクか
	 */
	private boolean first = true;

	/**
	 * 受信完了済みか
	 */
	private boolean finish = false;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			//分割されたデータ
			ByteBuf chunk = (ByteBuf) msg;

			//初回の場合、ヘッダを解釈する
			if (first) {
				//二度実行しないようにする
				first = false;

				//ここに来た段階の次の4バイトがヘッダを除いた全体サイズ
				size = chunk.readInt();

				//受信可能な最大サイズを超えていたら例外を投げる
				if (size > chunkedDataTotalSizeMax || size < 0)
					throw new IllegalArgumentException();

				//受信バッファを作成
				whole = new byte[size];
				Glb.debug("start receive whole size=" + size);
			}

			//今回受信したデータのサイズ
			int chunkSize = chunk.readableBytes();

			//サイズが超過したら例外を投げる
			if (count + chunkSize > size || chunkSize < 0)
				throw new IllegalArgumentException("count:" + count
						+ " chunkSize:" + chunkSize + " size:" + size);

			//分割されたデータを結合
			chunk.getBytes(chunk.readerIndex(), whole, count, chunkSize);

			//書き込んだ分countを加算
			count += chunkSize;

			Glb.debug("size=" + size + " count=" + count + "chunkSize "
					+ chunkSize);

		} catch (Exception e) {
			Glb.getLogger().error("", e);
			throw e;
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	//Chunked系で全体の受信が終わったら呼ばれる
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		Glb.debug("channelReadComplete count=" + count + " size=" + size);

		/*
		 * 理由は不明だが、独自ヘッダ付きでChunkedWriteHandlerを通じてChunked系データを
		 * 送信すると1回目のパケットでchannelReadCompleteが呼ばれてしまう。
		 * 必要なサイズに到達してない場合、無視する
		 *
		 * 恐らく原因はByteToMessageDecoder 242のこの部分である。
		 *      	        numReads = 0;
		 *                  ctx.fireChannelReadComplete();
		 *             }
		 *             handlerRemoved0(ctx);
		 * ハンドラを削除した時にreadCompleteが呼ばれてしまっている。
		 * ポート統合でハンドラ削除は公式のサンプルコードでも書かれているものなので、問題だろう。
		 *
		 * issueを作成して修正された。
		 * https://github.com/netty/netty/issues/9208
		 */
		if (count != size)
			return;

		Glb.debug("channelReadComplete completed");

		if (finish)
			return;

		//大抵、この直後アプリ固有ハンドラがあり、それを呼び出す
		finish = true;
		ctx.fireChannelRead(whole);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Glb.debug("exceptionCaught " + cause);
		//ここでメモリ解放等をすべきだが、解放すべきものが無い
		cause.printStackTrace();
		ctx.close();
	}

}
