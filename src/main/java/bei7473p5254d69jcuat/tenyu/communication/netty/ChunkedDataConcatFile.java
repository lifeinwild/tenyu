package bei7473p5254d69jcuat.tenyu.communication.netty;

import java.io.*;
import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import glb.*;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.util.*;

/**
 * 分割されたデータをファイルに受信する。
 * このような通信方式の必要性は、数GB以上のファイルを受信するときに
 * 全体をメモリ上に置くとメモリを占有しすぎる事にある。
 *
 * ファイル受信時ゼロコピーの方法が無い。
 * https://stackoverflow.com/questions/30322957/is-there-transferfrom-like-functionality-in-netty-for-zero-copy
 * >A Netty channel does not provide an operation that zero-copies the inbound traffic into a file.
 * ゼロコピーではないということは、おおよそ5-10Gbps程度のファイル受信で
 * 1コアを使い切ってしまうだろう。
 *
 * ゼロコピーが無いならRandomAccessFileで並列書き込みするにあたって性能的ロスが無い。
 * ということでそうしている。
 * 各スレッドが1ファイルに対して異なる区間に書き込む。
 *
 * このクラスの書き込み処理が呼ばれる前にファイル全体が作成されている必要がある。
 * リクエスト時点でファイルサイズが判明している前提であり（ファイル名を伝える時に同時に伝えられる）
 * 最終的なサイズのファイルをDL前に作成しておく必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ChunkedDataConcatFile extends ChannelInboundHandlerAdapter {
	/**
	 * 送受信のタイムアウト時間
	 */
	public static final int transferTimeout = 60 * 60 * 6;//second

	/**
	 * Javaで作成できる配列の要素数の上限は規定されていない。VM毎に違う。
	 * 出来るだけ多くのVMに対応するため、多めにトレランスを見る。
	 */
	private static final int jvmTolerance = 1000;
	/**
	 * 受信可能な最大サイズ
	 */
	public static final long chunkedDataTotalSizeMax = 1000L * 1000L * 1000L
			* 3L - jvmTolerance;

	/**
	 * 書き込んだサイズ
	 */
	private long count;

	/**
	 * 受信開始位置。レジュームのため
	 */
	private RequestFileHandler req;
	/**
	 * リクエストしたサイズ
	 */
	private final long requestedSize;
	/**
	 * リクエストしたDL開始位置
	 */
	private final long requestedPosition;

	/**
	 * 初回チャンクか
	 */
	private boolean first = true;

	/**
	 * pathへ順次書き込む機能
	 */
	private RandomAccessFile stream;

	/**
	 * ファイルパス。ここに受信する
	 */
	private Path path;

	/**
	 * 受信完了済みか
	 */
	private boolean finish = false;

	public ChunkedDataConcatFile(RequestFileHandler req) {
		this.req = req;
		this.requestedSize = req.getSize();
		this.requestedPosition = req.getPosition();

	}

	public static final int fileNameLenMax = 150;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Glb.debug("file receiving channelRead");
		try {
			//分割されたデータ
			ByteBuf chunk = (ByteBuf) msg;

			//初回の場合、ヘッダを解釈する
			if (first) {
				//二度実行しないようにする
				first = false;

				/*
				//プロトコル3では次の1バイトが種別
				byte kind = chunk.readByte();
				//次の4バイトがファイル名のサイズ
				int fileNameLength = chunk.readInt();
				if (fileNameLength > fileNameLenMax) {
					throw new IllegalArgumentException(
							"fileNameLength is too big");
				}

				byte[] fileNameB = chunk.readBytes(fileNameLength).array();
				*/
				String pathStr = req.getMeta().getRelativePathStr();
				if (pathStr == null || !Glb.getFile().isAppPathRelative(pathStr)
						|| !Glb.getUtil().validatePath(pathStr,
								fileNameLenMax)) {
					ctx.close();
					throw new Exception("invalid path");
				}
				path = Paths.get(pathStr);

				//受信ストリームを作成
				//事前にファイルが作成されている前提
				stream = new RandomAccessFile(pathStr, "rw");

				//最初の書き込み位置
				stream.seek(requestedPosition);

				Glb.debug("start receive file requestedSize=" + requestedSize
						+ " position=" + requestedPosition + " path="
						+ pathStr);
			}

			//今回受信したデータのサイズ
			int chunkSize = chunk.readableBytes();

			//サイズが超過したら例外を投げる
			if (count + chunkSize > requestedSize || chunkSize < 0)
				throw new IllegalArgumentException(
						"count:" + count + " chunkSize:" + chunkSize
								+ " requestedSize:" + requestedSize);

			//受信したデータ
			byte[] bytes = new byte[chunkSize];
			chunk.readBytes(bytes);

			//ファイルに書き込む
			stream.write(bytes, 0, bytes.length);
			//chunk.getBytes(chunk.readerIndex(), ch, chunkSize);

			//書き込んだ分countを加算
			count += chunkSize;

			//次の書き込み位置を設定
			stream.seek(requestedPosition + count);
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
		 * https://github.com/netty/netty/issues/9208
		 */
		if (first) {
			return;
		}

		if (count != requestedSize) {
			/*	発生しすぎる
			Glb.getLogger()
					.warn("count != requestedSize count=" + count
							+ " requestedSize=" + requestedSize,
							new Exception());
							*/
			return;
		}

		Glb.debug("req=" + req + "count=" + count + " requestedSize="
				+ requestedSize);

		if (finish)
			return;
		finish = true;

		close(true);

		//大抵、この直後アプリ固有ハンドラがあり、それを呼び出す
		ctx.fireChannelRead(path);
	}

	/**
	 * ストリームを閉じる
	 */
	private void close(boolean flush) {
		if (stream != null) {
			try {
				/*
				 * https://stackoverflow.com/questions/7550190/how-do-i-flush-a-randomaccessfile-java
				if (flush)
					stream.flush();
					*/
				stream.close();
			} catch (Exception e) {
				Glb.getLogger().warn("", e);
			}
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		close(false);
		cause.printStackTrace();
		ctx.close();
	}

}
