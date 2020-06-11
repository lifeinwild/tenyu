package glb;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.apache.logging.log4j.*;
import org.apache.tika.*;
import org.quartz.*;
import org.quartz.impl.*;

import com.esotericsoftware.kryo.*;
import com.maxmind.geoip2.*;

import bei7473p5254d69jcuat.tenyu.*;
import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.MessageContent.*;
import bei7473p5254d69jcuat.tenyu.communication.local.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.ResultList.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.vote.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.AbstractStandardResponse.*;
import bei7473p5254d69jcuat.tenyu.communication.request.HasUserMessageList.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetCore.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetHashArray.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetIntegrity.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetObj.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetRecycleHidList.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetUpdatedIDList.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetUserMessageList.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.GuiCausedSimpleMessageGui.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.user.*;
import bei7473p5254d69jcuat.tenyu.communication.request.server.ratinggamematchingserver.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.CommonKeyExchange.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.CommonKeyExchangeConfirmation.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.GetAddresses.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.PeriodicNotification.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.Recognition.*;
import bei7473p5254d69jcuat.tenyu.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.HashStore.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.Integrity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.Team.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyutalk.*;
import glb.Conf.*;
import glb.util.*;
import glb.util.Bits;
import glb.util.Util.*;
import javafx.scene.control.Alert.*;
import jetbrains.exodus.env.*;

/**
 * 0 instance class
 *
 * グローバル変数一覧でインスタンス化不可能。
 *
 * このようなクラスを置くことがソフトウェア開発全体を強力に支えるという
 * アイデアは、おぼろげな記憶だが、
 * 昔某企業でこのようなコードを見かけた記憶がある。
 *
 * DIはGlbのsetterを通してやれば十分で、
 * DIフレームワークのように特別な対応は不要と思う。
 *
 * 実際、テストケースが非常にうまく作れた。
 * Glbは本プログラムの骨子であり、
 * Glb.とタイプすれば本プログラムのほとんどの要素へアクセスできる。
 * 恐らくあらゆるプログラムにおいて採用すべきデザインパターンである。
 *
 * Glbに関する共通規格を作ったらコンポーネントの再利用性は上がるだろうか？
 * Glbをインポート不要のクラスにすると？それは名前の衝突の問題があるか。
 * しかしコンパイル時に使用するGlbを設定すれば？
 * 良く分からない。
 *
 * Glbは基本的に実行時に要素の同一性が保証されるが、
 * このアプリではObjectivityCoreの一意性が保証されない。
 * いちいちDBから読み出しているから。
 * オンメモリのオブジェクトに関してトランザクションとかMVCCが無いからそうせざるを得なかった。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Glb {
	/**
	 * https://qiita.com/q-ikawa/items/3f55089e9081e1a854bc
	 * @param onTry
	 * @param onCatch
	 * @return
	 */
	public static <T, R> Function<T, R> tryW(ThrowableFunction<T, R> onTry,
			BiFunction<Exception, T, R> onCatch) {
		return x -> {
			try {
				return onTry.apply(x);
			} catch (Exception e) {
				debug(e);
				if (onCatch == null)
					return null;
				return onCatch.apply(e, x);
			}
		};
	}

	public static <T1, T2, R> BiFunction<T1, T2, R> tryW2(
			ThrowableBiFunction<T1, T2, R> onTry,
			BiFunction<Exception, T1, R> onCatch) {
		return (t1, t2) -> {
			try {
				return onTry.apply(t1, t2);
			} catch (Exception e) {
				debug(e);
				if (onCatch == null)
					return null;
				return onCatch.apply(e, t1);
			}
		};
	}

	/**
	 * Glbメンバー変数の一部はこれを実装する
	 * db, conf, const, filemanagement等各実行毎の動的状態を持たないものは実装し無い。
	 * scheduler等ライブラリのクラスも実装し無い。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static interface GlbMemberDynamicState {
		/**
		 * スレッドの開始等
		 */
		default public void start() {
		}

		/**
		 * スレッドの停止等
		 */
		default public void stop() {
		}

		/**
		 * 次回起動時にロードしなければならない状態を記録
		 *
		 * @return
		 */
		default public boolean save() {
			return true;
		}
	}

	public static interface ThrowableFunction<T, R> {
		R apply(T t) throws Exception;
	}

	public static interface ThrowableBiFunction<T1, T2, R> {
		R apply(T1 t1, T2 t2) throws Exception;
	}

	/**
	 * 秘密鍵暗号化のパスワード
	 *
	 * パスワードの格納にStringを使用しないほうがいいという議論がある。
	 * https://stackoverflow.com/questions/8881291/why-is-char-preferred-over-string-for-passwords/8889285#8889285
	 * しかし本アプリではもしchar[]を使ってもraw passwordがメモリ上に存在し続けるので
	 * セキュリティの改善は全くない。
	 * そうであれば、不変であるというStringの性質はパスワードという不変な情報に
	 * char[]よりも合致しているとみなせる。
	 * アップデートに際しバージョンアップされたプロセスに旧プロセスから
	 * パスワードを渡して全自動で移行したいから、
	 * パスワードの保持は欠かせない。
	 * さらに言えば秘密鍵もメモリ上に存在しているので、
	 * メモリのダンプを取るような手段の攻撃にこのアプリは全く堅牢性が無い。
	 * この問題の解決策はOSやアンチウイルスソフトが
	 * 他プロセスのメモリを読み書きするプロセス侵襲性があるプログラムについて
	 * 許可制かよりシビアな身元の検証、動作の検証、
	 * 侵襲を許可するプロセスの限定等をする事である。
	 * プロセス内部すら秘密情報の保持が許されないなら作成可能なシステムは
	 * あまりにも限定されてしまうので、
	 * プロセス侵襲性があるプログラムの側が制限を受けるべきである。
	 * あるいは、プロセス毎にそのような侵襲を許可するか設定できるか、
	 * プロセス内部に絶対に侵襲されない領域を作れるべき。
	 * private secret String password;
	 * とでも書けるべき。
	 *
	 */
	private transient static byte[] password;//TODO アップデートモジュールに移動

	/**
	 * エントリーポイント
	 */
	private static Tenyu app;

	/**
	 * ロガー
	 */
	private static Logger logger;
	/**
	 * 定数
	 */
	private static Const cons;
	/**
	 * ファイルパス
	 */
	private static FileManagement file;
	/**
	 * 起動前にエンドユーザーが設定する値、
	 * または起動直後に環境から取得される値
	 */
	private static Conf conf;
	/**
	 * ユーティリティ
	 */
	private static Util util;

	/**
	 * ファイルタイプ識別
	 */
	private static Tika fileTypeDetector;

	/**
	 * レーティング計算アルゴリズム
	 */
	private static SME2 sme2;
	/**
	 * ビット系ユーティリティ
	 */
	private static BitUtil bitUtil;

	/**
	 * DB
	 */
	private static Map<String, Environment> db;

	/**
	 * 主観
	 */
	private static Subjectivity subje;
	/**
	 * 客観
	 */
	private static Objectivity obje;

	/**
	 * Tenyutalk
	 * なおこのインスタンスはコンストラクトメソッドを呼び出すためだけに使用される
	 */
	private static Tenyutalk tenyutalk;

	/**
	 * 主観と客観の中間的なもの
	 * それ自体客観ではないが、後に客観に影響する情報など
	 */
	private static Middle middle;

	/**
	 * P2P通信
	 */
	private static P2P p2p;
	/**
	 * P2P通信における重複チェックなどある種の防御機能
	 */
	private static P2PDefense p2pdefense;
	/**
	 * 同PC上の他のプロセスとの通信
	 */
	private static LocalIpc localIpc;

	/**
	 * 定期処理
	 */
	private static Scheduler scheduler;

	/**
	 * IPアドレスから国コード等を特定
	 */
	private static DatabaseReader geo;

	//	private static GroupLockManager lock;

	/**
	 * GUI。コード全体の様々なところからダイアログを表示させたりできる。
	 */
	private static Gui gui;
	//	private static Visualizer graph;
	/**
	 * 相互評価フローネットワークのフロー計算
	 */
	private static FlowComputationState flow;

	//制御フラグ。getter,setterはpackage-private
	private static Boolean isCpuProvementTime;
	private static Boolean isCpuProvementValidationTime;

	/**
	 * セキュア乱数。システム全体から共有されるので生成される乱数が予測困難に。
	 */
	private static Random rnd;

	/**
	 * 非同期実行したい場合これを使う
	 * 比較的短時間の処理
	 */
	private static ExecutorService executor;

	/**
	 * 様々な処理、例えばファイルDLなどもこれを使うので、
	 * 非常に遅い事を想定しなければならない。
	 *
	 * スレッドが処理中に待機するケースが多いので少し数を多めにしておく。
	 */
	private static ExecutorService executorSlow;

	/**
	 * 定期処理用
	 * 処理をして一定時間待ちまた処理をする。
	 */
	private static ScheduledExecutorService executorPeriodic;

	/**
	 * ファイルをDLする
	 */
	private static Downloader downloader;

	/**
	 * 永続化専用Kryo
	 */
	private static ThreadLocal<Kryo> kryoForPersistence;
	/**
	 * 通信専用Kryo
	 */
	private static ThreadLocal<Kryo> kryoForCommunication;
	/**
	 * LocalのRPC専用Kryo
	 */
	private static ThreadLocal<Kryo> kryoForRPC;
	/**
	 * Tenyutalk永続化専用Kryo
	 */
	private static ThreadLocal<Kryo> kryoForTenyutalk;

	/**
	 * 定期処理一覧
	 * 厳密にはここに網羅されていない定期処理もある
	 */
	private static TimerTaskList timerTaskList;

	private static GuiConst guiConst;

	/**
	 * static参照のみ
	 */
	private Glb() {
	}

	/**
	 * 簡易デバッグログ
	 */
	public static void debug(Supplier<String> s) {
		if (Glb.getConf().isDevOrTest()) {
			debug(s.get());
		}
	}

	public static void debug(Throwable e) {
		if (Glb.getConf().isDevOrTest()) {
			Glb.debug("", e);
		}
	}

	public static void debug(String s, Throwable e) {
		if (Glb.getConf().isDevOrTest()) {
			Glb.getLogger().debug(s, e);
		}
	}

	public static void debug(String log) {
		if (Glb.getConf().isDevOrTest()) {
			long tId = Thread.currentThread().getId();
			StackTraceElement[] ste = new Throwable().getStackTrace();
			Glb.getLogger().debug(
					ste[1] + System.lineSeparator() + log + " threadId=" + tId);
		}
	}

	private static void kryoSetupForPackage(Kryo k) {
		k.register(RunLevel.class);

		k.register(byte[].class);
		k.register(PlainPackage.class);
		//		k.register(CommonKeyPackage.class);
		k.register(P2PEdgeCommonKeyPackage.class);
		k.register(P2PEdgeCommonKeyPackageUnsecure.class);
		k.register(SignedPackage.class);
		k.register(UserCommonKeyPackage.class);
	}

	private static void kryoSetupForContent(Kryo k) {
		//TODO デバッグ用コード　ここから
		k.setRegistrationRequired(false);
		k.setWarnUnregisteredClasses(true);
		//TODO デバッグ用コード	ここまで

		k.addDefaultSerializer(InetSocketAddress.class,
				InetSocketAddressSerializer.class);

		k.register(MessageId.class);
		k.register(PaceLimitAmount.class);

		k.register(TurnBaseMessage.class);
		k.register(PowerVoteMessage.class);
		k.register(PowerVoteConfirmationMessage.class);
		k.register(PowerVoteValue.class);

		k.register(RandomString.class);
		k.register(Answer.class);
		k.register(Proved.class);
		k.register(ProblemSrc.class);
		k.register(Solve.class);
		k.register(ResultList.class);
		k.register(ParallelNumberAndSolve.class);

		k.register(Recognition.class);
		k.register(RecognitionResponse.class);
		k.register(PeriodicNotification.class);
		k.register(PeriodicNotificationResponse.class);
		k.register(GetAddresses.class);
		k.register(GetAddressesResponse.class);

		k.register(CommonKeyExchangeConfirmation.class);
		k.register(CommonKeyExchangeConfirmationResponse.class);
		k.register(CommonKeyExchange.class);
		k.register(CommonKeyExchangeResponse.class);
		k.register(CommonKeyInfo.class);

		k.register(DeleteEdge.class);

		k.register(SendMoney.class);

		k.register(UserRegistrationInfo.class);
		k.register(UserRegistrationIntroduceOffer.class);
		k.register(User.class);
		k.register(Wallet.class);
		k.register(sun.util.calendar.ZoneInfo.class);

		k.register(Sociality.class);
		k.register(Web.class);
		k.register(StaticGame.class);
		k.register(RatingGame.class);
		k.register(TeamClass.class);
		k.register(FlowNetworkAbstractNominal.class);
		k.register(UserMessageListHash.class);
		k.register(FreeKVPair.class);

		k.register(StandardResponse.class);
		k.register(StandardResponseGui.class);
		k.register(ResultCode.class);
		k.register(GuiCausedSimpleMessageGuiP2PEdge.class);
		k.register(GuiCausedSimpleMessageGuiUser.class);

		k.register(TakeOverMessageUserMessageListServer.class);

		k.register(HasUserMessageList.class);
		k.register(HasUserMessageListResponse.class);
		k.register(GetUserMessageList.class);
		k.register(GetUserMessageListResponse.class);
		k.register(UserMessageList.class);

		k.register(UserRegistration.class);
		k.register(UserProfileUpdate.class);

		k.register(GetIntegrity.class);
		k.register(GetIntegrityResponse.class);
		k.register(Integrity.class);
		k.register(IntegrityByStore.class);
		k.register(HashStoreRecordPositioned.class);
		k.register(HashStoreKey.class);
		k.register(HashStoreValue.class);

		k.register(IDList.class);
		k.register(int[].class);
		k.register(GetObj.class);
		k.register(GetObjResponse.class);

		k.register(GetUpdatedIDList.class);
		k.register(GetUpdatedIDListResponse.class);
		k.register(CatchUpUpdatedIDList.class);

		k.register(GetCore.class);
		k.register(GetCoreResponse.class);
		k.register(ObjectivityCore.class);
		k.register(TenyuManager.class);
		k.register(GetHashArray.class);
		k.register(GetHashArrayResponse.class);
		k.register(GetRecycleHidList.class);
		k.register(GetRecycleHidListResponse.class);

		k.register(RegisterRatingMatches.class);
		k.register(RatingGameMatch.class);
		k.register(Team.class);
		k.register(TeamState.class);
		k.register(MatchingType.class);

		k.register(AddrInfo.class);

	}

	/**
	 * シリアライザーを通信用に設定する
	 */
	private static void kryoSetupForCommunication(Kryo k) {
		k.register(Message.class);

		kryoSetupForPackage(k);
		kryoSetupForContent(k);

		k.register(LinkedHashMap.class);
		k.register(HashMap.class);
		k.register(ConcurrentHashMap.class);
		k.register(ArrayList.class);
		k.register(CopyOnWriteArrayList.class);
		k.register(KeyType.class);
		k.register(AtomicLong.class);

		k.register(P2PNode.class);
		k.register(ByteArrayWrapper.class);

		k.register(AssignedRange.class);
		k.register(Bits.class);

		//		k.register(GroupLock.class);

	}

	public static void kryoSetupForPersistence(Kryo k) {
		//Kryoは登録順序に応じて連番のIDをクラスに与えている。クラスID
		//新規クラスを末尾に登録しなければ、IDが狂い、DBから読み出せなくなる
		//同じクラスに同じIDが割り当てられるよう保たなければならない
		//どんな型情報も４バイト以下になる。１バイトで表現できるIDなら１バイトのようだ
		//プリミティブ型はデフォルトで登録されている
		//idは指定する事もできるので、指定すべきだろう。順序に依存すべきでない。
		//Model系のうちSingleObjectでないものは、そのメンバー変数のクラスを含め
		//登録すべきだろう
		//しかしその作業はある程度モデルクラスが整ってからでいいかもしれない
		//現状、クラス登録をしなくても動作する設定になっている。
		//しかしその設定は完全修飾名を記録するのでデータが大きくなる
		//実動作において、必ず連番系のモデルクラスはクラスIDを固定すべきである
		k.addDefaultSerializer(InetSocketAddress.class,
				InetSocketAddressSerializer.class);

		k.setRegistrationRequired(false);
		k.register(byte[].class);

		k.register(Subjectivity.class);
		k.register(UpdatableNeighborList.class);
		k.register(ReadonlyNeighborList.class);
		k.register(P2PEdge.class);
		k.register(ByteArrayWrapper.class);
		k.register(InetSocketAddress.class, new InetSocketAddressSerializer());
		k.register(P2PNode.class);
		k.register(ByteArrayWrapper.class);
		k.register(AssignedRange.class);
		k.register(Bits.class);
		k.register(KeyType.class);
		k.register(HashMap.class);
		k.register(ArrayList.class);
		k.register(ConcurrentHashMap.class);
		k.register(URL.class);

		k.register(User.class);

		k.register(UserMessageListHash.class);
		k.register(Web.class);

		//永続化用は通信用の全クラスを含んでも問題無いが、
		//登録順序に影響が出ないよう同じクラスでも分けて登録すべき
	}

	/**
	 * 最小の初期化処理
	 */
	public static void setupForMin() {
		if (rnd == null)
			rnd = new SecureRandom();//thread safe
		//		if (lock == null)
		//			lock = new GroupLockManager();
		if (util == null)
			util = new Util();
		if (fileTypeDetector == null)
			fileTypeDetector = new Tika();
		if (cons == null)
			cons = new Const();
		if (guiConst == null)
			guiConst = new GuiConst();
		if (logger == null)
			logger = LogManager.getLogger(cons.getAppName() + " Global");
		if (bitUtil == null)
			bitUtil = new BitUtil();
		if (timerTaskList == null)
			timerTaskList = new TimerTaskList();
	}

	private static boolean isInJar() {
		try {
			return Glb.class.getResource("Foo.class").getPath()
					.startsWith("jar");
		} catch (Exception e) {
			return false;
		}
	}

	private static void loadSecurityManager() {
		if (System.getSecurityManager() != null)
			return;
		String codeBase, grantRange, tenyuPolicyPath, tenyuPolicyContent;
		String tenyuPolicyFilename = file.getPolicyFileName();
		boolean inJar = isInJar();
		if (inJar) {
			codeBase = "./" + Glb.getFile().getExecutableJarName();
			grantRange = codeBase;//jarに付与
			tenyuPolicyPath = tenyuPolicyFilename;
			tenyuPolicyContent = cons
					.getTenyuPolicySystemWithFileCodeBase(grantRange);
		} else {
			codeBase = Glb.getFile().getCodeBaseOnIde();
			//grantRange = codeBase + "-";//フォルダ以下再帰的付与
			tenyuPolicyPath = codeBase + tenyuPolicyFilename;
			tenyuPolicyContent = cons.getTenyuPolicySystem("");//全クラス権限付与。開発ツールの変更によって付与すべきファイルがどこにあるか変わるので
		}

		tenyuPolicyContent += cons.getTenyuPolicyOther("./");

		try {
			//実行時にjarの中で実行されているかide上で実行されているかを判別して
			//適宜ポリシーファイルを作成する。
			File tenyuPolicyFile = new File(tenyuPolicyPath);
			//既にあるなら削除
			if (tenyuPolicyFile.exists()
					&& file.isAppPath(tenyuPolicyFile.toPath())) {
				file.remove(tenyuPolicyFile.toPath());
			}
			tenyuPolicyFile.createNewFile();
			FileWriter fw = new FileWriter(tenyuPolicyFile);
			fw.write(tenyuPolicyContent);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			stopApplication();
		}

		//作成されたポリシーファイルを読み込む
		String policyFileAbsolutePath = Glb.class.getClassLoader()
				.getResource(tenyuPolicyFilename).getPath();
		System.out.println("policyFileAbsolutePath=" + policyFileAbsolutePath);
		System.setProperty("java.security.policy", policyFileAbsolutePath);
		Policy.getPolicy().refresh();
		System.setSecurityManager(new TenyuSecurityManager());//これ以降権限の問題が生じる
	}

	/**
	 *
	 */
	public static void setupForCommon() {
		if (executor == null)
			executor = Executors.newFixedThreadPool(4);
		if (executorSlow == null)
			executorSlow = Executors.newFixedThreadPool(8);
		if (executorPeriodic == null)
			executorPeriodic = Executors.newScheduledThreadPool(8);

		if (kryoForPersistence == null) {
			kryoForPersistence = new ThreadLocal<Kryo>() {
				protected Kryo initialValue() {
					Kryo kryo = new Kryo();
					//永続化用Kryoはregister不要の設定が妥当
					kryo.setRegistrationRequired(false);
					//これは必須
					kryo.addDefaultSerializer(InetSocketAddress.class,
							InetSocketAddressSerializer.class);

					return kryo;
				};
			};
		}
		if (kryoForCommunication == null) {
			kryoForCommunication = new ThreadLocal<Kryo>() {
				protected Kryo initialValue() {
					Kryo kryo = new Kryo();
					kryoSetupForCommunication(kryo);
					return kryo;
				};
			};
		}

		if (kryoForTenyutalk == null) {
			kryoForTenyutalk = new ThreadLocal<Kryo>() {
				protected Kryo initialValue() {
					Kryo kryo = new Kryo();
					//永続化用Kryoはregister不要の設定が妥当
					kryo.setRegistrationRequired(false);
					//これは必須
					kryo.addDefaultSerializer(InetSocketAddress.class,
							InetSocketAddressSerializer.class);

					return kryo;
				};
			};
		}
		setupForMin();

		if (db == null)
			db = new ConcurrentHashMap<>();
		if (geo == null) {
			try {
				geo = new DatabaseReader.Builder(util.getLoader()
						.getResource("GeoLite2-Country.mmdb").openStream())
								.build();
			} catch (Exception e) {
				Glb.getLogger().error("Failed to load geoDB", e);
			}
		}

		//TODO:デバッグ
		//boolean r = cons.validateCreatorPublicKeys();
		//System.out.println("validateCreatorPublicKeys:"+r);
	}

	/**
	 * メイン向け。一部テストでも使用する
	 */
	public static void setupForMainCore() {
		setupForCommon();
		if (file == null) {
			file = new FileManagement();
			file.open();
		}

		if (conf == null) {
			conf = new Conf();
			conf.init();
		}

		file.dirSetupAfterConf();

		loadSecurityManager();
	}

	public static void setupStores() {
		//各ストアを作成しておく
		//読み取り専用トランザクションにおいてストアを作成できないので
		//客観だけやっているが客観しか読み取り専用アクセスは無い
		Glb.getObje().execute(txn -> {
			for (StoreNameObjectivity storeName : StoreNameObjectivity
					.values()) {
				ModelStore<?, ?> s = storeName.getStore(txn);
				s.initStores();
				/*
				try {
					RecycleHidStore recycle = new RecycleHidStore(storeName,
							txn);
					recycle.initStores();

					HashStore hashStore = new HashStore(storeName, txn);
					hashStore.initStores();

					s.getCatchUpUpdatedIDListStore().initStores();

				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
				*/
			}
			new ObjectivityUpdateDataStore(txn).initStores();

			StoreNameSingle.OBJECTIVITY_CORE.getStore(txn).initStores();
		});
	}

	/**
	 * 基本的にこれを実行すると全ての標準機能が利用できるようになる
	 */
	public static void setupForStandard() {
		setupForMainCore();
		if (subje == null) {
			subje = Subjectivity.loadOrCreate();
		}
		if (obje == null) {
			obje = new Objectivity();
		}
		if (middle == null) {
			middle = Middle.loadOrCreate();
		}

		setupStores();

		if (tenyutalk == null) {
			tenyutalk = new Tenyutalk();
		}

		if (flow == null) {
			flow = new FlowComputationState();
		}
		if (gui == null) {
			gui = new Gui();
		}

		//通信処理は様々なモデルに依存するので、最後に開始
		if (p2pdefense == null) {
			p2pdefense = P2PDefense.loadOrCreate();
		}
		if (p2p == null) {
			p2p = new P2P();
		}
		if (localIpc == null) {
			localIpc = new LocalIpc();
		}
		if (scheduler == null) {
			try {
				scheduler = StdSchedulerFactory.getDefaultScheduler();
			} catch (SchedulerException e) {
				Glb.getLogger().error("", e);
			}
		}
		if (downloader == null) {
			downloader = new Downloader();
		}

		isCpuProvementTime = false;
		isCpuProvementValidationTime = false;

	}

	public static Boolean isCpuProvementTime() {
		return isCpuProvementTime;
	}

	public static void setCpuProvementTime(Boolean isCpuProvementTime) {
		Glb.isCpuProvementTime = isCpuProvementTime;
	}

	public static Boolean isCpuProvementValidationTime() {
		return isCpuProvementValidationTime;
	}

	public static void setCpuProvementValidationTime(
			Boolean isCpuProvementValidationTime) {
		Glb.isCpuProvementValidationTime = isCpuProvementValidationTime;
	}

	/**
	 * アプリ開始時に呼ばれる事を想定
	 * 各オブジェクトのstart()を呼ぶ
	 */
	public static void startApplication() {
		conf.createUserHomeFile();
		if (subje != null) {
			subje.start();
		}
		if (obje != null) {
			obje.start();
			obje.initForRise();
		}
		if (middle != null) {
			middle.start();
		}
		if (flow != null) {
			flow.start();
		}
		if (gui != null) {
			gui.start();
		}

		//通信処理は様々なモデルに依存するので、最後に開始
		if (p2pdefense != null) {
			p2pdefense.start();
		}
		if (p2p != null) {
			p2p.start();
		}

		if (downloader != null) {
			downloader.start();
		}

		if (localIpc != null) {
			localIpc.start();
		}

		if (scheduler != null) {
			try {
				scheduler.start();
			} catch (SchedulerException e) {
				Glb.getLogger().error("", e);
			}
		}

		ThroughputLimit.start();

		timerTaskList.start();

		//作者鍵の検証
		boolean authorVerify = cons.validateAuthorPublicKeys();
		if (!authorVerify)
			Glb.getLogger().error("authorVerify=" + authorVerify);

		Glb.getLogger().info(Lang.LAUNCH_LOG);

		if (Glb.getMiddle().getOnlineChecker().imServer()) {
			//サーバーかつFQDNを登録してなかったら警告
			NodeIdentifierUser myIdentifier = Glb.getMiddle()
					.getMyNodeIdentifierUser();
			User me = myIdentifier.getUser();
			if (me.isNoFqdn()) {
				Glb.getGui().alert(AlertType.WARNING,
						Lang.USER_MESSAGE_SERVER.toString(),
						Lang.USER_NO_FQDN.toString());
			}
		}

		//アドレス解決サーバにUserEdgeを作る
		getExecutorSlow().execute(() -> {
			UserEdgeGreeting.send();
		});

	}

	public static void periodicSave() {
		//TODO
	}

	/**
	 * アプリ終了時に呼ばれる事を想定
	 */
	public static void stopApplication() {
		try {
			if (scheduler != null && scheduler.isStarted()) {
				scheduler.shutdown();
			}
		} catch (SchedulerException e) {
			Glb.getLogger().error("", e);
		}
		if (downloader != null) {
			downloader.stop();
		}
		if (p2p != null)
			p2p.stop();

		if (p2pdefense != null) {
			p2pdefense.stop();
		}
		if (localIpc != null)
			localIpc.stop();
		if (gui != null) {
			gui.removeTypeSelectorIdLog();
			gui.stop();
		}
		if (timerTaskList != null)
			timerTaskList.stop();
		if (subje != null) {
			subje.stop();
		}
		if (middle != null)
			middle.stop();
		if (obje != null)
			obje.stop();

		if (db != null) {
			for (Environment e : db.values()) {
				if (e.isOpen()) {
					e.getEnvironmentConfig().setEnvCloseForcedly(true);
					e.close();
				}
			}
		}

		ThroughputLimit.stop();
	}

	public static Gui getGui() {
		return gui;
	}

	public static P2P getP2p() {
		return p2p;
	}

	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static Conf getConf() {
		return conf;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static FlowComputationState getFlow() {
		return flow;
	}

	public static FileManagement getFile() {
		return file;
	}

	public static void setFile(FileManagement file) {
		Glb.file = file;
	}

	public static Const getConst() {
		return cons;
	}

	public static LocalIpc getLocalIpc() {
		return localIpc;
	}

	public static void setLocalIpc(LocalIpc localIpc) {
		Glb.localIpc = localIpc;
	}

	public static Boolean getIsCpuProvementTime() {
		return isCpuProvementTime;
	}

	public static void setIsCpuProvementTime(Boolean isCpuProvementTime) {
		Glb.isCpuProvementTime = isCpuProvementTime;
	}

	public static Boolean getIsCpuProvementValidationTime() {
		return isCpuProvementValidationTime;
	}

	public static void setIsCpuProvementValidationTime(
			Boolean isCpuProvementValidationTime) {
		Glb.isCpuProvementValidationTime = isCpuProvementValidationTime;
	}

	public static void setLogger(Logger logger) {
		Glb.logger = logger;
	}

	public static void setConst(Const con) {
		Glb.cons = con;
	}

	public static void setConf(Conf conf) {
		Glb.conf = conf;
	}

	public static void setP2p(P2P p2p) {
		Glb.p2p = p2p;
	}

	public static void setScheduler(Scheduler scheduler) {
		Glb.scheduler = scheduler;
	}

	public static void setGui(Gui gui) {
		Glb.gui = gui;
	}

	public static void setFlow(FlowComputationState flow) {
		Glb.flow = flow;
	}

	public static Environment getDb(String path) {
		Environment e = db.get(path);
		if (e == null) {
			//デフォルトの最大メモリ使用量は-Xmxで決定されるようだ
			//Xmxはデフォルトで物理メモリの4分の1または1GBのうち小さい方を使用するようだ
			//1GBはこのアプリで少し足りないので、2GBを指定すべきだろう。
			//2GBを指定すると大規模書き込みテストが10％程度高速化する
			e = Environments.newInstance(path);
			//1GBのキャッシュを設定する。TODO テストケースが通らなくなる
			e.getEnvironmentConfig()
					.setEnvStoreGetCacheSize(1000 * 1000 * 1000);
			db.put(path, e);
		}

		return e;
	}

	public static void setDb(ConcurrentMap<String, Environment> db) {
		Glb.db = db;
	}

	public static void setPlatform(Subjectivity platform) {
		Glb.subje = platform;
	}

	public static Util getUtil() {
		return util;
	}

	public static void setUtil(Util util) {
		Glb.util = util;
	}

	public static Tika getFileTypeDetector() {
		return fileTypeDetector;
	}

	public static void setFileTypeDetector(Tika fileTypeDetector) {
		Glb.fileTypeDetector = fileTypeDetector;
	}

	public static Subjectivity getSubje() {
		return subje;
	}

	public static void setSubje(Subjectivity subje) {
		Glb.subje = subje;
	}

	public static Objectivity getObje() {
		return obje;
	}

	/**
	 * @return	Tenyutalk情報へのアクセス
	 */
	public static Tenyutalk getTenyutalk() {
		return tenyutalk;
	}

	/**
	 * 必要ならテスト用インスタンスをこのメソッドで設定できる
	 * @param tenyutalk	オーバーライドされたテスト用クラスによるインスタンス
	 */
	public static void setTenyutalk(Tenyutalk tenyutalk) {
		Glb.tenyutalk = tenyutalk;
	}

	public static void setObje(Objectivity obje) {
		Glb.obje = obje;
	}

	public static P2PDefense getP2pDefense() {
		return p2pdefense;
	}

	public static void setP2pDefense(P2PDefense p2pDefense) {
		Glb.p2pdefense = p2pDefense;
	}

	public static Random getRnd() {
		return rnd;
	}

	public static void setRnd(Random rnd) {
		Glb.rnd = rnd;
	}

	public static Kryo getKryoForPersistence() {
		return kryoForPersistence.get();
	}

	public static Kryo getKryoForCommunication() {
		return kryoForCommunication.get();
	}

	public static Kryo getKryoForRPC() {
		return kryoForRPC.get();
	}

	public static Kryo getKryoForTenyutalk() {
		return kryoForTenyutalk.get();
	}

	/*
		public static Kryo getKryoForPackage() {
			return kryoForPackage.get();
		}
	*/
	public static BitUtil getBitUtil() {
		return bitUtil;
	}

	public static void setBitUtil(BitUtil bitUtil) {
		Glb.bitUtil = bitUtil;
	}

	/*
		public static GroupLockManager getLock() {
			return lock;
		}

		public static void setLock(GroupLockManager lock) {
			Glb.lock = lock;
		}
	*/
	public static Tenyu getApp() {
		return app;
	}

	public static void setApp(Tenyu app) {
		Glb.app = app;
	}

	public static Middle getMiddle() {
		return middle;
	}

	public static void setMiddle(Middle middle) {
		Glb.middle = middle;
	}

	public static DatabaseReader getGeo() {
		return geo;
	}

	public static void setGeo(DatabaseReader geo) {
		Glb.geo = geo;
	}

	public static SME2 getSme2() {
		return sme2;
	}

	public static void setSme2(SME2 sme2) {
		Glb.sme2 = sme2;
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	public static void setExecutor(ExecutorService executor) {
		Glb.executor = executor;
	}

	public static ExecutorService getExecutorSlow() {
		return executorSlow;
	}

	public static Downloader getDownloader() {
		return downloader;
	}

	public static void setDownloader(Downloader downloader) {
		Glb.downloader = downloader;
	}

	public static ScheduledExecutorService getExecutorPeriodic() {
		return executorPeriodic;
	}

	public static void setExecutorPeriodic(
			ScheduledExecutorService executorPeriodic) {
		Glb.executorPeriodic = executorPeriodic;
	}

	public static void setExecutorSlow(ExecutorService executorSlow) {
		Glb.executorSlow = executorSlow;
	}

	public static TimerTaskList getTimerTaskList() {
		return timerTaskList;
	}

	public static void setTimerTaskList(TimerTaskList timerTaskList) {
		Glb.timerTaskList = timerTaskList;
	}

	public static byte[] getPassword() {
		return password;
	}

	public static void setPassword(byte[] password) {
		Glb.password = password;
	}

	public static GuiConst getGuiConst() {
		return guiConst;
	}

	public static void setGuiConst(GuiConst guiConst) {
		Glb.guiConst = guiConst;
	}
}
