package bei7473p5254d69jcuat.tenyu.communication;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.quartz.*;

import com.esotericsoftware.kryo.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.netty.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.GuiCausedSimpleMessageGui.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import glb.*;
import glb.Glb.*;
import io.netty.bootstrap.*;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.channel.socket.nio.*;
import io.netty.handler.stream.*;
import io.netty.handler.timeout.*;
import io.netty.util.*;

/**
 * P2P系通信処理
 *
 * Netty5は設計がより単純になったそうだが、大きな機能的差異は無いようだ。
 * 5はalpha段階なのでNetty4を使った。
 *
 * コードが長くなっているが内部の複雑性も扱う概念も限定的で、
 * 非同期か同期かなどの性質の組み合わせでインターフェースが増えている。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class P2P implements GlbMemberDynamicState {
	/*各メッセージクラスのreceivedに移動
					if (c instanceof P2PSequenceMessage) {
						con.close();//Client解放
						//分散合意にメッセージを送る
						P2PSequence s = channelToSeq
								.get(((P2PSequenceMessage) c).getChannel());
						s.receive(r);
					} else if (c instanceof Request) {
						//このアプリのリクエストは多数決や並列DLのために
						//ほとんどの場合多数のノードに送られる。
						Request req = (Request) c;
						Response res = null;
						if (req instanceof Greeting) {
						} else if (req instanceof GetAddresses) {
							//自分の近傍を回答する
							GetAddressesResponse tmp = new GetAddressesResponse();
							tmp.init((GetAddresses) req);
							res = tmp;
						} else if (req instanceof FileDownload) {
							res = new FileDownloadResponse();
						}
						if (res != null && req.isValid(res) && res.validate()) {
							con.sendTCP(res);
						}
						con.close();
					} else	{
						throw new Exception(
								"IllegalMessage:" + c.getClass().getName());
					}
					*/

	/**
	 * レイテンシ計測前の初期値
	 * 必ず正の大きな値である事
	 * レイテンシソート等に影響する
	 */
	private static final long initLatency = 99999;

	/**
	 * サーバー終了前の待機時間。この時間の間、新たな接続を受け付けず、
	 * 既存の処理が終了するのを待つ。
	 */
	private static final int shutdownTime = 1000 * 2;

	static {
		System.setProperty("io.netty.tryReflectionSetAccessible", "true");
	}

	public static byte[] addr(InetSocketAddress addr) {
		return addr.getAddress().getAddress();
	}

	private static void connectionFailed(Long userId) {
		Glb.debug("connection failed userId=" + userId);
	}

	/**
	 * 接続失敗時に呼ばれる
	 */
	private static void connectionFailed(P2PEdge e) {
		e.incrementConnectionFailedCount();
	}

	private static void connectionSuccess(Long userId) {
		Glb.debug("connection success userId=" + userId);
	}

	/**
	 * 接続成功時に呼ばれる
	 */
	private static void connectionSuccess(P2PEdge e) {
		e.incrementConnectionCount();
		e.updateLastCommunication();
	}

	private static Object deserialize(Object in) {
		byte[] read = null;
		if (in instanceof ByteBuf) {
			//プロトコル1の場合
			ByteBuf buf = (ByteBuf) in;
			read = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), read);
		} else if (in instanceof byte[]) {
			//プロトコル2の場合
			read = (byte[]) in;
		} else {
			throw new IllegalArgumentException("invalid type");
		}
		return Glb.getUtil().fromKryoBytesForCommunication(read);
	}

	public static long getInitlatency() {
		return initLatency;
	}

	public static int getShutdowntime() {
		return shutdownTime;
	}

	public static InetSocketAddress isa(ChannelHandlerContext ctx) {
		return ((InetSocketAddress) ctx.channel().remoteAddress());
	}

	/**
	 * 実行レベルに応じて通信可能なアドレスを制限する
	 */
	private static boolean isValid(InetSocketAddress a) {
		if (a == null)
			return false;
		if (Glb.getConf().isDevOrTest()) {
			byte[] localhost = InetAddress.getLoopbackAddress().getAddress();
			if (!Arrays.equals(a.getAddress().getAddress(), localhost))
				return false;
		}
		return true;
	}

	private static byte[] serialize(Object o) {
		try {
			return Glb.getUtil().toKryoBytesForCommunication(o);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * 定期的にisDoneか判定してクローズする
	 * TODO 必要性が不明。最初の頃書いた。
	 * パイプラインにタイムアウトがあるから不要？
	 */
	private CopyOnWriteArrayList<
			ChannelFuture> autoClose = new CopyOnWriteArrayList<>();

	/**
	 * 実行中の全分散合意を一元管理する。
	 * channel : p2psequence
	 * channelは、今のところ静的に定義される。
	 * 起動時に一括設定されてそれ以降変化が無いのでHashMapでスレッドセーフである。
	 */
	private Map<String,
			P2PSequence> channelToSeq = new HashMap<String, P2PSequence>();

	private ScheduledFuture<?> closeThread;

	/**
	 * 接続のタイムアウト。ミリ秒
	 */
	private final int connectionTimeout = 3000;

	/**
	 * P2P通信ならではの問題に対応する防御機構
	 */
	private final P2PDefense defense = Glb.getP2pDefense();

	private EventLoopGroup group;

	/**
	 * アイドルによるタイムアウト時間
	 */
	private final int idle = 5000;

	/**
	 * start()からstop()まで常時特定のポートにバインドされ
	 * クライアントからの通信を受け付ける。
	 * 既知のノードに返信以外で送信する場合serverに送信する。
	 */
	@SuppressWarnings("unused")
	private ChannelFuture server;

	/**
	 * 小さなメッセージの場合の読み書きのタイムアウト。
	 */
	private final int transferTimeout = 5000;

	/**
	 * Glb.getDefense()を使うのでDefenseが登録されてから呼ぶ
	 */
	public P2P() {
	}

	/**
	 * P2P全ノードで同時実行する通信タスクを登録する
	 * TODO　再設計の結果、P2Pクラスに置くべきではないコードになった
	 *
	 * @param seq
	 * @return
	 */
	public boolean addSeq(TimerTaskList.JobAndTrigger seq) {
		try {
			if (seq.getTrigger() == null) {
				throw new Exception("getTrigger() is null.");
			}

			return Glb.getScheduler().scheduleJob(seq.getJob(),
					seq.getTrigger()) != null;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	private Message buildGuiRequest(String title, String content, P2PEdge to) {
		User me = Glb.getMiddle().getMe();
		content = me.getId() + " " + me.getName() + " : " + content;

		GuiCausedSimpleMessageGuiP2PEdge req = new GuiCausedSimpleMessageGuiP2PEdge();
		req.setTitle(title);
		req.setContent(content);
		return Message.build(req).packaging(req.createPackage(to)).finish();
	}

	public Map<String, P2PSequence> getChannelToSeq() {
		return channelToSeq;
	}

	private long latencyTest(InetSocketAddress a) {
		if (a == null)
			return P2P.getInitlatency();

		long start = System.currentTimeMillis();
		Bootstrap b = setupClient(null, a, 0, false);
		ChannelFuture cf = null;
		try {
			cf = b.connect().sync();
		} catch (InterruptedException e) {
		} finally {
			if (cf != null && cf.channel() != null)
				cf.channel().close();
		}
		long end = System.currentTimeMillis();

		return end - start;

	}

	/**
	 * 接続から切断までの時間。
	 * low latencyなアプリを作るなら接続したままメッセージをやり取りする
	 * と思うので、この数値はあまり関係無い。
	 * @param a
	 * @param loop
	 * @return
	 */
	public LatencyTestResult latencyTest(InetSocketAddress a, int loop) {
		LatencyTestResult r = new LatencyTestResult();
		for (int i = 0; i < loop; i++) {
			long latency = latencyTest(a);
			if (latency == P2P.getInitlatency())
				r.incrementFailed();
			r.incrementSuccess();
			r.addTotal(latency);
		}
		return r;
	}

	public boolean removeSeq(TimerTaskList.JobAndTrigger seq) {
		try {
			return Glb.getScheduler().unscheduleJob(seq.getTrigger().getKey());
		} catch (SchedulerException e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * 非同期リクエスト。出力に含まれるチャンネルをcloseする必要がある
	 * @param pack		送信する情報
	 * @param req		ここにレスポンスが登録される
	 * @param toAddr	送信先
	 * @return			接続タスクを管理するオブジェクト
	 */
	public RequestFuture requestAsync(final Message reqM,
			InetSocketAddress toAddr) {
		byte[] send = sendRequestCommon(reqM, toAddr);
		if (send == null || send.length == 0)
			return null;

		Object tmp = reqM.getContent();
		if (!(tmp instanceof Request))
			return null;
		Request req = (Request) tmp;

		Glb.debug(() -> "requestAsync:" + req.getClass().getSimpleName());

		//リクエストレスポンス用のハンドラを作成
		RequestHandler handler = new RequestHandler(reqM, send);
		//通信を管理するオブジェクト
		Bootstrap b = setupClient(handler, toAddr, send.length, true);
		ChannelFuture cf = null;
		try {
			//接続。非同期
			cf = b.connect();
			if (cf == null)
				return null;
			return new RequestFuture(cf, handler, toAddr);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	public RequestFutureUser requestAsync(Message m, Long receiverUserId,
			int nodeNumber) {
		return requestAsync(m,
				new NodeIdentifierUser(receiverUserId, nodeNumber));
	}

	/**
	 * 非同期、レスポンス有り、P2PEdge
	 * 最終通信日時が更新される
	 */
	public RequestFutureP2PEdge requestAsync(Message m, P2PEdge to) {
		RequestFuture resState = requestAsync(m, to.getNode().getISAP2PPort());
		if (resState == null) {
			//TODO これは接続タイムアウト等を検出できない。
			//非同期通信は呼び出し元で接続失敗イベントに配慮する必要がある
			//現状接続失敗は雑な値で良いので気にする必要が無い
			connectionFailed(to);
			return null;
		} else {
			connectionSuccess(to);
		}
		return new RequestFutureP2PEdge(resState, to);
	}

	public RequestFutureUser requestAsync(Message m,
			NodeIdentifierUser identifier) {
		User receiver = identifier.getUser();
		AddrInfo addr = receiver.tryToGetAddr(identifier.getNodeNumber());
		if (addr == null) {
			Glb.getLogger().warn("AddrInfo is null. receiver=" + receiver,
					new Exception());
			return null;
		}
		RequestFuture resState = requestAsync(m, addr.getISAP2PPort());
		if (resState == null) {
			//TODO これも同様で接続失敗の計測は完全ではない
			connectionFailed(receiver.getId());
		} else {
			connectionSuccess(receiver.getId());
		}
		return new RequestFutureUser(resState, identifier);
	}

	/**
	 * 送信相手のGUIに単純な文字列メッセージを表示させる
	 * @param title
	 * @param content
	 * @param to
	 * @return
	 */
	public RequestFutureP2PEdge requestAsyncGuiMessage(String title,
			String content, P2PEdge to) {
		return requestAsync(buildGuiRequest(title, content, to), to);
	}

	/**
	 * 非同期にファイルを取得する
	 * @param f			取得するファイル
	 * @param position	この位置から
	 * @param size		このサイズを
	 * @param e			ここから取得する
	 * @return			非同期通信の結果
	 */
	public RequestFutureP2PEdge requestFileAsync(GetFile req, P2PEdge e) {
		if (req == null || e == null)
			return null;
		Message reqM = Message.build(req).packaging(req.createPackage(e))
				.finish();
		InetSocketAddress addr = e.getNode().getISAP2PPort();
		byte[] send = sendRequestCommon(reqM, addr);
		if (send == null)
			return null;
		RequestFileHandler handler = new RequestFileHandler(reqM, send, req);
		Bootstrap b = setupClient(handler, addr, send.length, true);
		ChannelFuture cf = null;
		try {
			//接続。非同期
			cf = b.connect();
			if (cf == null)
				return null;
			return new RequestFutureP2PEdge(
					new RequestFuture(cf, handler, addr), e);
		} catch (Exception ex) {
			Glb.getLogger().error("", ex);
		}
		return null;
	}

	/**
	 * @param pack
	 * @param req
	 * @param addr	このアドレスに送信する。
	 * @param responseBufferSize	送受信に使われるバッファのサイズ
	 * @return
	 */
	public Message requestSync(final Message m, InetSocketAddress addr) {
		if (m == null || addr == null) {
			return null;
		}
		byte[] send = sendRequestCommon(m, addr);
		if (send == null || send.length == 0)
			return null;

		Object tmp = m.getContent();
		if (!(tmp instanceof Request))
			return null;
		Request req = (Request) tmp;

		Glb.debug("Send Request:"
				+ (m.getEdgeByInnermostPackage() == null ? ""
						: m.getEdgeByInnermostPackage())
				+ " " + m.getContent().getClass().getSimpleName() + " "
				+ m.getLoad().getClass().getSimpleName() + " size="
				+ send.length + " " + addr);

		//リクエストレスポンス用のハンドラを作成
		RequestHandler handler = new RequestHandler(m, send);
		//通信を管理するオブジェクト
		Bootstrap b = setupClient(handler, addr, send.length, true);
		ChannelFuture cf = null;
		try {
			//接続。同期
			cf = b.connect().sync();
			//レスポンスが来るまで待つが、一定時間が経過したらレスポンスが来てなくても終わる。
			//レスポンス待機開始日時
			long start = System.currentTimeMillis();
			//レスポンス待機の経過時間
			long elapsed = 0;
			//どの程度の時間待つか
			long responseTimeout = transferTimeout + 5000;
			//待機処理
			while (elapsed < responseTimeout) {
				Thread.sleep(10);
				//レスポンスが来て、処理を終えたか
				if (handler.isDone()) {
					//isDone()になった場合だけオブジェクトを返す。
					//さもないと、ハンドラ実行中にこのメソッドが返って
					//Responseがマルチスレッドで処理される可能性がある。
					return req.getRes();
				}
				//経過時間の更新
				elapsed = System.currentTimeMillis() - start;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		} finally {
			if (cf != null && cf.channel() != null)
				cf.channel().close();
		}
		return null;
	}

	public Message requestSync(Message m, Long receiverUserId, int nodeNumber) {
		return requestSync(m,
				new NodeIdentifierUser(receiverUserId, nodeNumber));
	}

	/**
	 * 同期、レスポンス有り、P2PEdge
	 * 最終通信日時が更新される
	 */
	public Message requestSync(Message m, P2PEdge to) {
		Message resM = requestSync(m, to.getNode().getISAP2PPort());
		if (resM == null) {
			connectionFailed(to);
		} else {
			connectionSuccess(to);
		}
		return resM;
	}

	public Message requestSync(Message m, User receiver, int nodeNumber) {
		return requestSync(m, new NodeIdentifierUser(receiver, nodeNumber));
	}

	public Message requestSync(Message m, NodeIdentifierUser identifier) {
		User receiver = identifier.getUser();
		if (m == null || receiver == null) {
			Glb.debug("m=" + m + " receiver=" + receiver);
			return null;
		}
		Message resM = requestSync(m, identifier.getAddrP2PPort());
		//UserEdgeの場合のみ作動する。現状全く不要
		if (m.getInnermostPack() instanceof UserCommonKeyPackage) {
			if (resM == null) {
				connectionFailed(receiver.getId());
			} else {
				connectionSuccess(receiver.getId());
			}
		}
		return resM;
	}

	public Message requestSyncGuiMessage(String title, String content,
			P2PEdge to) {
		return Glb.getP2p().requestSync(buildGuiRequest(title, content, to),
				to);
	}

	/**
	 * Role系のためのリクエスト送信インターフェース
	 * 1件でも送信に成功すれば終わる
	 *
	 * @param getMessage						送信するメッセージ
	 * @param servers			Role系のサーバー
	 * @param cachedServer	前回通信に成功したサーバー
	 * @param roleId				対象Role
	 * @param cacheAccessFailed		キャッシュでの通信に失敗したら呼び出される。
	 * 								キャッシュを削除することを想定。
	 * @param accessSuccess			通信に成功したら呼び出される。
	 * 								引数は接続先のuserId
	 * @return	レスポンス
	 */
	public Message requestToServer(
			Function<NodeIdentifierUser, Message> getMessage,
			List<NodeIdentifierUser> servers, NodeIdentifierUser cachedServer,
			Long roleId, Consumer<NodeIdentifierUser> cacheAccessFailed,
			Consumer<NodeIdentifierUser> accessSuccess) {
		try {
			//キャッシュされたサーバーIDがあればそれで通信を試みる
			if (cachedServer != null) {
				Message resMCached = requestToServerSimple(
						getMessage.apply(cachedServer), cachedServer);
				boolean cache = true;

				if (Response.failConnection(resMCached)) {
					cache = false;
				}

				if (cache) {
					//通信に成功したらそれを返して終わり
					return resMCached;
				} else {
					//通信に失敗したらキャッシュから消して以降の処理へ
					if (cacheAccessFailed != null) {
						cacheAccessFailed.accept(cachedServer);
					}
					if (servers == null)
						return null;
					servers.remove(cachedServer);
				}
			}

			int count = 2;
			for (int i = 0; i < count; i++) {
				//順に試す。通信に成功したら終わり
				for (NodeIdentifierUser serverUserId : servers) {
					Message resM = requestToServerSimple(
							getMessage.apply(serverUserId), serverUserId);
					if (Response.failConnection(resM)) {
						Glb.getLogger().warn(new IOException(
								"resM is null. serverUserId=" + serverUserId));
						continue;
					}
					if (accessSuccess != null) {
						accessSuccess.accept(serverUserId);
					}
					return resM;
				}

				if (i >= count - 1) {
					break;
				}

				//受付サーバー交代時に数秒間サーバーが１つも稼働していない状態がありうるので
				//少し待ってもう一度だけ頭から試す
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", new Exception());
		}

		return null;
	}

	private Message requestToServerSimple(Message m,
			NodeIdentifierUser identifier) {
		Message resM = requestSync(m, identifier);
		if (resM != null) {
			connectionSuccess(identifier.getUserId());
		} else if (resM == null) {
			connectionFailed(identifier.getUserId());
		}

		return resM;
	}

	/**
	 * ユーザーメッセージ受付サーバリストの最前列から順に送信を試行する。
	 * @param getRequest	送信先ユーザーIDが入力、送信するリクエストが出力
	 * @return		返信
	 */
	public Message requestUserRightMessage(
			Function<NodeIdentifier, UserMessageListRequestI> getRequest) {
		String rName = UserMessageListServer.class.getSimpleName();
		Role role = Glb.getObje().getRole(rs -> rs.getByName(rName));
		if (role == null)
			return null;
		return requestToServer(to -> {
			UserMessageListRequestI req = getRequest.apply(to);
			if (!(req instanceof MessageContent)) {
				Glb.getLogger().error("invalid Request object",
						new Exception());
				return null;
			}
			return Message.build((MessageContent) req)
					.packaging(req.createPackage()).finish();
		}, role.getAdminNodes(), Glb.getMiddle().getCachedServer(rName),
				role.getId(),
				id -> Glb.getMiddle().removeCachedServer(rName, id),
				id -> Glb.getMiddle().putCachedServer(rName, id));
	}

	/**
	 * カーネルへの転送は同期だが通信は非同期な返信
	 *
	 * @param m		送信するメッセージ
	 * @param ctx
	 * @return	カーネルへの転送を終えたか
	 */
	public boolean response(Message m, ChannelHandlerContext ctx) {
		if (m == null || ctx == null)
			return false;

		if (!m.validate()) {
			Glb.debug("invalid content: " + (m.getContent() == null ? ""
					: m.getContent().getClass().getSimpleName()));
			return false;
		}

		Glb.debug(() -> "Send Response: edgeId="
				+ (m.getEdgeByInnermostPackage() == null ? ""
						: m.getEdgeByInnermostPackage())
				+ " userId=" + m.getUserId()
				+ m.getContent().getClass().getSimpleName() + ":addr="
				+ isa(ctx) + ":threadId=" + Thread.currentThread().getId());

		byte[] send = serialize(m);
		if (send == null || send.length == 0)
			return false;

		Glb.debug("response size=" + send.length);

		//返信用パイプライン構築
		byte protocolId = OnePortManyProtocolEncoder.setupPipelineClient(
				ctx.pipeline(), send.length,
				OnePortManyProtocolDecoder.getProtocolId(send.length));

		//Netty用形式に
		Object sendFormatted = null;
		switch (protocolId) {
		case 1:
			sendFormatted = Unpooled.wrappedBuffer(send);
			break;
		case 2:
			sendFormatted = new ChunkedStream(new ByteArrayInputStream(send));
			break;
		}
		if (sendFormatted == null)
			return false;

		//返信
		try {
			ctx.writeAndFlush(sendFormatted);
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return false;
	}

	/**
	 * @param p		送信するファイル
	 * @param pos	開始位置。ファイルの何バイト目から送信するか
	 * @param size	送信するサイズ
	 * @param ctx
	 * @return	nullまたは非同期処理の結果
	 */
	public ChannelFuture responseFile(Path p, long pos, long size,
			ChannelHandlerContext ctx) {
		if (p == null || ctx == null)
			return null;

		//受信可能な最大サイズを超えていたら例外を投げる
		if (size > ChunkedDataConcatFile.chunkedDataTotalSizeMax || size < 0)
			throw new IllegalArgumentException();

		Glb.debug(() -> "responseFile path=" + p + "to=" + isa(ctx));

		//返信用パイプライン構築
		OnePortManyProtocolEncoder.setupPipelineClient(ctx.pipeline(), 0,
				(byte) 3);

		//ゼロコピーファイル送信
		FileRegion fr = new DefaultFileRegion(p.toFile(), pos, size);

		//返信
		ChannelFuture cf = null;
		try {
			cf = ctx.writeAndFlush(fr);
			return cf;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * チャンネルのcloseは内部で自動的に行われる
	 * @return ほぼ意味無し
	 */
	public boolean sendAsync(Message m, InetSocketAddress addr) {
		if (addr == null || m == null)
			return false;
		try {
			Bootstrap b = sendCommon(m, addr);
			if (b == null)
				return false;
			ChannelFuture cf = null;
			cf = b.connect();
			autoClose.add(cf);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
		return true;
	}

	public boolean sendAsync(Message m, Long userId, int nodeNumber) {
		return sendAsync(m, new NodeIdentifierUser(userId, nodeNumber));
	}

	public boolean sendAsync(Message m, P2PEdge e) {
		boolean r = sendAsync(m, e.getNode().getISAP2PPort());
		if (r) {
			connectionSuccess(e);
		} else {
			connectionFailed(e);
		}
		return r;
	}

	public boolean sendAsync(Message m, NodeIdentifierUser identifier) {
		User u = identifier.getUser();
		boolean r = sendAsync(m, identifier.getAddrP2PPort());
		if (r) {
			connectionSuccess(u.getId());
		} else {
			connectionFailed(u.getId());
		}
		return r;
	}

	private Bootstrap sendCommon(Message m, InetSocketAddress addr) {
		byte[] send = sendRequestCommon(m, addr);
		if (send == null || send.length == 0)
			return null;
		return setupClient(new SendHandler(send), addr, send.length, false);
	}

	/**
	 * Messageがセットアップされる
	 * @param m
	 * @param addr
	 * @return
	 */
	private byte[] sendRequestCommon(Message m, InetSocketAddress addr) {
		if (m == null || addr == null || m.getLoad() == null)
			return null;

		if (!m.validate()) {
			Glb.debug("invalid content: " + m.getContent() == null ? ""
					: m.getContent().getClass().getSimpleName());
			return null;
		}

		if (!isValid(addr)) {
			Glb.debug("invalid address: " + addr);
			return null;
		}

		Glb.debug("send:" + m.getContent().getClass().getSimpleName()
				+ ":threadId=" + Thread.currentThread().getId() + " to="
				+ addr);

		return serialize(m);
	}

	/**
	 * @param m
	 * @param addr
	 * @return		接続に成功したか
	 */
	public boolean sendSync(Message m, InetSocketAddress addr) {
		if (addr == null || m == null)
			return false;
		ChannelFuture cf = null;
		try {
			Bootstrap b = sendCommon(m, addr);
			if (b == null)
				return false;
			cf = b.connect().sync();
			return cf.isSuccess();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		} finally {
			if (cf != null && cf.channel() != null)
				cf.channel().close();
		}
		return false;
	}

	public boolean sendSync(Message m, Long userId, int nodeNumber) {
		NodeIdentifierUser identifier = new NodeIdentifierUser(userId,
				nodeNumber);
		return sendSync(m, identifier);
	}

	public boolean sendSync(Message m, P2PEdge e) {
		boolean r = sendSync(m, e.getNode().getISAP2PPort());
		if (r) {
			connectionSuccess(e);
		} else {
			connectionFailed(e);
		}
		return r;
	}

	public boolean sendSync(Message m, NodeIdentifierUser identifier) {
		User u = identifier.getUser();
		if (u == null) {
			return false;
		}
		AddrInfo addr = identifier.getAddrWithCommunication();
		if (addr == null) {
			return false;
		}
		boolean r = sendSync(m, addr.getISAP2PPort());
		if (r) {
			connectionSuccess(u.getId());
		} else {
			connectionFailed(u.getId());
		}
		return r;
	}

	/*
		private static final int maxFrame = 65536;
		private static final int lengthFieldSize = 2;
		private static final int lengthFieldOffset = 0;
		private static final int lengthAdjustment = 0;
		private static final int lengthFieldStrip = 2;
		private static final boolean lengthFieldFailFast = true;
	*/

	/**
	 * プロトコル1，2の場合
	 */
	private Bootstrap setupClient(ChannelInboundHandler handler,
			InetSocketAddress a, int contentSize, boolean response) {
		return setupClient(handler, a, contentSize, response,
				OnePortManyProtocolDecoder.getProtocolId(contentSize));
	}

	/**
	 * プロトコルIDを指定する場合
	 */
	private Bootstrap setupClient(ChannelInboundHandler handler,
			InetSocketAddress a, int contentSize, boolean response,
			byte protocolId) {
		OnePortManyProtocolDecoder dec = new OnePortManyProtocolDecoder(false,
				handler);
		Bootstrap client = new Bootstrap();
		client.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
				.remoteAddress(a)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						Glb.debug("client initChannel");

						ChannelPipeline p = ch.pipeline();
						OnePortManyProtocolEncoder.setupPipelineClient(p,
								contentSize, protocolId);
						if (response) {
							p.addFirst(dec.getClass().getSimpleName(), dec);
						}
						p.addLast(new IdleStateHandler(idle, idle, idle));
						if (handler != null)
							p.addLast(handler);
					}
				});

		return client;
	}

	public void start() {
		Glb.debug(() -> "P2P Server is starting. p2pport="
				+ Glb.getConf().getP2pPort());
		group = new NioEventLoopGroup();
		/*
		ServerBootstrap b = new ServerBootstrap();
		b.group(group).channel(NioServerSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
				.localAddress(new InetSocketAddress(Glb.getConf().getP2pPort()))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						Glb.debug(
								() -> "server initChannel" + ch.localAddress());
						ch.pipeline().addLast(new ReadTimeoutHandler(
								transferTimeout, TimeUnit.MILLISECONDS));
						ch.pipeline().addLast(new WriteTimeoutHandler(
								transferTimeout, TimeUnit.MILLISECONDS));
						ch.pipeline().addLast(
								new LengthFieldPrepender(lengthFieldSize));
						ch.pipeline()
								.addLast(new LengthFieldBasedFrameDecoder(
										maxFrame, lengthFieldOffset,
										lengthFieldSize, lengthAdjustment,
										lengthFieldStrip, lengthFieldFailFast));
						ch.pipeline().addLast(new ServerHandler());
					}
				});
		*/
		ServerBootstrap b = new ServerBootstrap();
		b.group(group).channel(NioServerSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
				.localAddress(new InetSocketAddress(Glb.getConf().getP2pPort()))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						Glb.debug("server initChannel" + ch.localAddress());
						ChannelPipeline p = ch.pipeline();
						p.addLast(
								OnePortManyProtocolDecoder.class
										.getSimpleName(),
								new OnePortManyProtocolDecoder(true));
						p.addLast(new IdleStateHandler(idle, idle, idle));

						p.addLast(new ServerHandler());
						Glb.debug("server init channel 2" + ch.localAddress());
					}
				});

		try {
			defense.start();
			server = b.bind().sync();
		} catch (InterruptedException e) {
			Glb.getLogger().error("", e);
		}

		long period = 1000;
		closeThread = Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> {
			try {
				if (autoClose.size() > 0) {
					for (ChannelFuture cf : autoClose) {
						if (cf.isDone()) {
							autoClose.remove(cf);
							cf.channel().close();
						}
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}

		}, period, period, TimeUnit.MILLISECONDS);

	}

	public void stop() {
		Glb.debug(() -> "P2P Server is stopping.");
		try {
			if (group != null)
				group.shutdownGracefully(500, shutdownTime,
						TimeUnit.MILLISECONDS).sync();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

		try {
			if (closeThread != null && !closeThread.isCancelled())
				closeThread.cancel(false);
			closeThread = null;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	public static class LatencyTestResult {
		private int failed = 0;
		private int success = 0;
		private long total = 0;

		public void addTotal(long add) {
			total += add;
		}

		public long getAverageLatency() {
			return total / success;
		}

		public int getFailed() {
			return failed;
		}

		public double getFailedRate() {
			return failed / (success + failed);
		}

		public int getSuccess() {
			return success;
		}

		public long getTotal() {
			return total;
		}

		public void incrementFailed() {
			failed++;
		}

		public void incrementSuccess() {
			success++;
		}

		@Override
		public String toString() {
			return "count=" + (success + failed) + " failedRate="
					+ getFailedRate() + " averageLatency="
					+ getAverageLatency();
		}
	}

	/**
	 * 非同期リクエストのレスポンス
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class RequestFuture {
		private InetSocketAddress addr;
		private RequestHandler handler;
		private ChannelFuture state;

		public RequestFuture(ChannelFuture state, RequestHandler handler,
				InetSocketAddress addr) {
			this.state = state;
			this.handler = handler;
			this.addr = addr;
		}

		public RequestFuture(RequestFuture o) {
			state = o.getState();
			handler = o.getHandler();
			addr = o.getAddr();
		}

		public InetSocketAddress getAddr() {
			return addr;
		}

		public RequestHandler getHandler() {
			return handler;
		}

		public ChannelFuture getState() {
			return state;
		}

		/**
		 * @return	処理が終了したか
		 */
		public boolean isDone() {
			return state.isDone() && handler.isDone();
		}

		public boolean isFailed() {
			return state.isDone() && !state.isSuccess();
		}

		/**
		 * @return	処理中か
		 * trueが返っても処理が終了している場合もある。
		 */
		public boolean isProc() {
			return !isDone();
		}

		public boolean isSuccess() {
			//TODO このコードを追加したが妥当かどうか
			//追加部分ここから
			if (handler == null || handler.getReq() == null)
				return false;
			if (handler instanceof RequestFileHandler) {
				//RequestFileHandlerの場合、返信が特殊
				RequestFileHandler fileHandler = (RequestFileHandler) handler;
				if (fileHandler.getResponsedFilePath() == null) {
					return false;
				}
			} else {
				if (handler.getReq().getRes() == null) {
					return false;
				}
				if (Response.fail(handler.getReq().getRes())) {
					return false;
				}
			}
			//追加部分ここまで

			return state.isSuccess() && handler.isDone();
		}
	}

	public static class RequestFutureP2PEdge extends RequestFuture {
		private P2PEdge to;

		public RequestFutureP2PEdge(RequestFuture reqF, P2PEdge to) {
			super(reqF);
			this.to = to;
		}

		public P2PEdge getTo() {
			return to;
		}

		public void setTo(P2PEdge to) {
			this.to = to;
		}
	}

	public static class RequestFutureUser extends RequestFuture {
		private NodeIdentifierUser identifier;

		public RequestFutureUser(RequestFuture reqF,
				NodeIdentifierUser identifier) {
			super(reqF);
			this.identifier = identifier;
		}

		public User getUser() {
			return identifier.getUser();
		}

		public NodeIdentifierUser getIdentifier() {
			return identifier;
		}
	}

	/**
	 * ファイル取得リクエスト用
	 * レジュームのために開始位置を返信受信時に使う
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class RequestFileHandler extends RequestHandler {
		public RequestFileHandler(Message reqMessage, byte[] send,
				GetFile req) {
			super(reqMessage, send);
			this.position = req.getPosition();
			this.meta = req.getFile();
			this.size = req.getSize();
		}

		private Path responsedFilePath;

		@Override
		public String toString() {
			return "meta=" + meta + " requestedPosition=" + position
					+ " requestedSize=" + size + super.toString();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx)
				throws Exception {
			done = true;
		}

		@Override
		protected void channelRead0Internal(ChannelHandlerContext ctx,
				Object in) throws Exception {
			if (in == null || !(in instanceof Path)) {
				Glb.getLogger().error("invalid response", new Exception());
				return;
			}

			responsedFilePath = (Path) in;
		}

		public TenyuFile getMeta() {
			return meta;
		}

		/**
		 * 開始位置
		 * 0ならファイルの最初から取得する
		 */
		private long position;

		/**
		 * 受信サイズ
		 * レジュームで部分的DLする場合、ファイルサイズと一致しない
		 * 開始位置0かつ受信サイズ＝ファイルサイズならファイル全体の取得
		 */
		private long size;
		/**
		 * 検証用情報
		 */
		private TenyuFile meta;

		public long getSize() {
			return size;
		}

		public long getPosition() {
			return position;
		}

		public Path getResponsedFilePath() {
			return responsedFilePath;
		}

		public void setResponsedFilePath(Path responsedFilePath) {
			this.responsedFilePath = responsedFilePath;
		}
	}

	/**
	 * レスポンスありの送信。同期
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class RequestHandler
			extends SimpleChannelInboundHandler<Object> {
		/**
		 * ハンドラの処理が終わったか。
		 * ChannelFutureのisDone()はハンドラの処理が開始した時点ですでにtrueなので
		 * ハンドラの処理完了を示すフラグを作った。
		 * 例外発生時もtrue
		 */
		protected boolean done = false;
		protected Message reqM;
		protected byte[] send;

		public RequestHandler(Message reqMessage, byte[] send) {
			if (!(reqMessage.getContent() instanceof Request))
				throw new IllegalArgumentException();
			this.reqM = reqMessage;
			this.send = send;
		}

		public void channelActive(ChannelHandlerContext ctx) {
			ctx.writeAndFlush(Unpooled.wrappedBuffer(send));
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx)
				throws Exception {
			done = true;
			if (getReq().getRes() == null)
				Glb.debug(() -> "channelInactive No Response: " + getReq() + ":"
						+ ctx.channel().remoteAddress() + ":threadId="
						+ Thread.currentThread().getId());
		}

		protected void channelRead0(ChannelHandlerContext ctx, Object in)
				throws Exception {
			try {
				/*
				if (Glb.getConf().isDevOrTest()) {
					Glb.getLogger()
							.info("channelRead0 response:" + in + " request:"
									+ reqM + " send:" + send,
									new Exception());
				}
				*/
				channelRead0Internal(ctx, in);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			} finally {
				done = true;
			}
		}

		protected void channelRead0Internal(ChannelHandlerContext ctx,
				Object in) throws Exception {
			//対応するリクエストが無い場合異常
			Request req = getReq();
			if (req == null)
				return;

			//既に処理済みのレスポンスは処理しない。
			if (req.getRes() != null) {
				//２回channelRead0が呼び出されでもしなければここには来ない
				Glb.debug(new Exception(
						"レスポンスが既にある。リクエストのインスタンスを使いまわしていないかハンドラが２回呼び出されていないかチェック resContent="
								+ req.getRes().getContent() + " req=" + req));
				return;
			} else {
				Glb.debug("res=" + req.getRes());
			}

			Object o = deserialize(in);

			if (o == null || !(o instanceof Message)) {
				Glb.debug("response is not message");
				return;
			}

			Message message = (Message) o;
			message.setMyMessage(false);

			if (Glb.getConf().isDevOrTest() && message != null) {
				P2PEdge n = message.getEdgeByInnermostPackage();
				Glb.debug(() -> "received Response Package: "
						+ o.getClass().getSimpleName() + addr(isa(ctx)) + ":"
						+ n + ":threadId=" + Thread.currentThread().getId());
			}

			if (!message.validateAndSetup()) {
				Glb.debug("Invalid Response."
						+ message.getLoad().getClass().getSimpleName());
				return;
			}

			//非同期通信と同期通信のリクエスト系の接続成功の計測
			P2PEdge e = message.getEdgeByInnermostPackage();
			if (e != null) {
				connectionSuccess(e);
			} else if (message
					.getInnermostPack() instanceof UserCommonKeyPackage) {
				connectionSuccess(message.getUserId());
			}

			MessageContent c = message.getContent();
			if (c == null || !(c instanceof Response)) {
				Glb.debug("Invalid Response."
						+ message.getContent().getClass().getSimpleName());
				return;
			}

			//ロック検査
			//			if (!Glb.getLock().register(c)) {
			//				return;
			//			}

			Glb.debug(() -> "received Response Content: "
					+ c.getClass().getSimpleName() +

					addr(isa(ctx)) + ":threadId="
					+ Thread.currentThread().getId());

			Response res = (Response) c;

			if (req == null || !res.isValid(req) || !req.isValid(res)) {
				if (res instanceof StandardResponse) {
					req.exceptionCaught((StandardResponse) res);
				}

				Glb.debug(() -> "invalid response: "
						+ res.getClass().getSimpleName());
				return;
			}

			res.setReq(reqM);
			if (!req.setRes(message)) {
				return;
			}

			Glb.debug(() -> "call received(): " + c.getClass().getSimpleName()
					+ ":threadId=" + Thread.currentThread().getId());

			Received validated = new Received(isa(ctx), message);

			if (!res.received(ctx, validated))
				Glb.debug(() -> res.getClass().getSimpleName()
						+ "#received() returns false in response");

		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx)
				throws Exception {
			Glb.debug("complete");
		}

		public void exceptionCaught(ChannelHandlerContext ctx,
				Throwable cause) {
			done = true;
			Glb.debug(
					() -> "exceptionCaught" + cause.getClass().getSimpleName());
			ctx.close();
		}

		public Request getReq() {
			MessageContent c = getReqM().getContent();
			if (c instanceof Request)
				return (Request) c;
			return null;
		}

		public Message getReqM() {
			return reqM;
		}

		public Response getRes() {
			Request req = getReq();
			if (req == null || req.getRes() == null)
				return null;
			MessageContent c = req.getRes().getContent();
			if (c instanceof Response)
				return (Response) c;
			return null;
		}

		public boolean isDone() {
			return done;
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
				throws Exception {
			Glb.debug("ev:" + evt);
		}

	}

	/**
	 * レスポンス無しの送信
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private static class SendHandler
			extends SimpleChannelInboundHandler<ByteBuf> {
		private byte[] send;

		public SendHandler(byte[] send) {
			this.send = send;
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			ctx.writeAndFlush(Unpooled.wrappedBuffer(send));
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
				throws Exception {
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			Glb.getLogger().warn("exceptionCaught", cause);
			ctx.close();
		}
	}

	private static class ServerHandler extends ChannelInboundHandlerAdapter {
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			try {
				Glb.debug("Server received: msg class="
						+ msg.getClass().getSimpleName());
				//受信したデータの全体
				byte[] whole = null;
				if (msg instanceof byte[]) {
					whole = (byte[]) msg;
				} else if (msg instanceof ByteBuf) {
					ByteBuf buf = (ByteBuf) msg;
					whole = new byte[buf.readableBytes()];
					buf.getBytes(buf.readerIndex(), whole);
				} else {
					throw new IllegalArgumentException();
				}
				ResultCode code = received(ctx, whole);
				//異常があったらエラーコードを返す
				if (code == null) {
					code = ResultCode.DEFAULT;
				}
				//SUCCESSならメッセージクラスの責任で返信したと想定する
				if (code != ResultCode.SUCCESS) {
					Glb.debug("StandardResponse code=" + code);
					StandardResponse res = new StandardResponse(code);
					Message resM = Message.build(res)
							.packaging(res.createPackage()).finish();
					Glb.getP2p().response(resM, ctx);
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			} finally {
				ReferenceCountUtil.release(msg);
			}
		}

		public void exceptionCaught(ChannelHandlerContext ctx,
				Throwable cause) {
			Glb.debug(cause);
			ctx.close();
		}

		private ResultCode received(ChannelHandlerContext ctx, byte[] data) {
			Glb.debug("Server received: data.length=" + data.length);

			//パッケージ専用Kryo
			Kryo k = Glb.getKryoForCommunication();
			Object o = Glb.getUtil().fromKryoBytes(data, k);

			InetSocketAddress addr = isa(ctx);
			if (!isValid(addr))
				return ResultCode.INVALID_ADDR;
			Glb.debug(() -> "from " + addr);
			MessageContent c = null;
			P2PDefense defense = Glb.getP2pDefense();
			try {
				if (defense.isOverCount(addr))
					return ResultCode.OVER_COUNT;

				if (defense.isOverSize(addr, data.length))
					return ResultCode.OVER_SIZE;

				if (!(o instanceof Message)) {
					Glb.debug(() -> "Not Message");
					return ResultCode.INVALID_CLASS;
				}
				Message message = (Message) o;
				message.setMyMessage(false);
				message.setSize(data.length);
				Glb.debug(() -> "received Message:"
						+ (message.getEdgeByInnermostPackage() == null ? ""
								: message.getEdgeByInnermostPackage())
						+ o.getClass().getSimpleName() + ":" + addr + ":"
						+ message.getEdgeByInnermostPackage() + ":threadId="
						+ Thread.currentThread().getId());

				if (!message.validateAndSetup()) {
					Glb.debug(() -> "invalid message: ");
					return ResultCode.UNPACKAGE_FAILED;
				}

				c = message.getContent();

				if (c == null || c instanceof Response) {
					Glb.debug(() -> "invalid content: ");
					return ResultCode.INVALID_ADDR;
				}

				//メッセージ重複判定
				if (defense.isDup(message)) {
					Glb.debug(() -> "duplicate message: "
							+ message.getClass().getSimpleName());
					return ResultCode.DUPLICATE;
				}

				//サイズ制限
				Long userId = message.getUserId();
				if (userId != null) {
					if (defense.isOverSize(userId, message)) {
						return ResultCode.OVER_SIZE;
					}
				}

				if (Glb.getConf().isDevOrTest()) {
					Glb.debug("received and call received() Content: "
							+ c.getClass().getSimpleName() + ":"
							+ message.getEdgeByInnermostPackage() + ":threadId="
							+ Thread.currentThread().getId());
				}

				//ここでmessageの基本的な検証が終わったと考えられる
				//さらにreceive()内部で様々なチェック処理がある
				Received validated = new Received(isa(ctx), message);

				if (c.received(ctx, validated)) {
					return ResultCode.SUCCESS;
				} else {
					Glb.debug(c.getClass().getSimpleName()
							+ "#received returns false");
					return ResultCode.PROC_FALSE;
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			} finally {
				//通信が終了したら閉じる。
				//addListenerでCLOSEハンドラを登録すると、
				//この通信に対する返信がカーネルに転送されきった後にチャンネルを閉じる。
				//TODO ネットでwriteAndFlushの後にaddListenerをするコードを良く見かけるが、
				//瞬時に送信が終わってaddListenerがラグったらハンドラが実行されないのでは？
				//TODO カーネル転送完了後、カーネルで送信完了前にCLOSEハンドラが呼ばれたら、受信側がチャンネルがCLOSEされたことを全受信完了前に気付くか？
				//https://github.com/netty/netty/issues/1952
				//Netty will notice that the connection has been closed after attempting to read from the channel, and close the connection for you. Calling channel.close() immediately after channel.read() will actually let Netty close the channel even before attempting to read.
				//恐らく受信が中途半端に終わる場合がある？
				ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
						.addListener(ChannelFutureListener.CLOSE);
			}
			return null;
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
				throws Exception {
			super.userEventTriggered(ctx, evt);
			Glb.debug("ev:" + evt);
		}
	}
}
