package bei7473p5254d69jcuat.tenyu.communication.mutual;

import java.time.*;
import java.util.*;

import org.quartz.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import glb.*;

/**
 * 分散合意のシーケンス。counterやcurrentなどプロセッサ的設計にした。
 * statementsは、実行時に徐々に加えられていくが、そのような仕様は
 * 実行時状態に応じて次の命令を変えられる事を意味する。
 * 今のところその必要性は生じていないがそう設計した。
 *
 * シーケンスは2種類あり、
 * 実行タイミングが被らないよう開始日時が手動調整される手動型と、
 * 開始直後他のシーケンスが開始しているかをチェックする事で自分が処理を続行するかを
 * 決定する自動型がある。
 * 現在自動型はUserMessageListSequenceしかない。
 * 手動型は1日3回など十分な間隔がある定期処理で、
 * 自動型は2分に1回など頻繁に繰り返される処理。
 *
 *
 * @author exceptiontenyu@gmail.com
 *
 */
@DisallowConcurrentExecution
public abstract class P2PSequence implements Runnable, org.quartz.Job {
	/**
	 * 様々なシーケンスが実行されるが、同時実行できないものが多い。
	 * UserMessageListSequenceは2分に1回実行され、2分という間隔は不変とする。
	 * 他の定期処理はcron形式でスケジューリングされるが
	 * 分の設定は0,2,4,6,8分スタートなど2の倍数スタートでなければならない。
	 *
	 * P2PSequenceから利用されるロック。他のシーケンスとの同時実行を回避できる。
	 * 設定される数値は処理優先度。
	 *
	 * 8時間に1回とか1週間に1回とかのはpriority=1000を基本とする。
	 * これらのシーケンスは開始日時が手動で調整され同時実行の可能性は無い。
	 * ただし開始直後UserMessageListSequenceのsequenceTime分待機する。
	 *
	 * UserMessageListSequenceはpriority=100を基本とする。
	 * ロックを取得できなければ単に終了する。
	 * 第一段階、第二段階直前にロックを取得する。
	 */
	protected static transient final Lock sequenceLock = new Lock();

	/**
	 * その他のシーケンスの間隔
	 */
	public static final int priorityLongPeriodSequence = 1000;
	/**
	 * UserMessageListSequenceの間隔
	 */
	public static final int priorityBaseSequence = 100;

	/**
	 * 特殊なロックシステム。
	 *
	 * ロックを取得できても既に開始してしまっていた他の処理を止める事はできない。
	 * ロックを取得できたら、それ以降他の処理を開始させない。
	 *
	 * ただし優先度概念がある。
	 * 自分の方が優先度が高ければ他の処理がロックを取得している事を無視して開始する。
	 * 自分より高い優先度を持つ処理がロックしていたらロックを取得できない。
	 *
	 * 通常のsynchronized等を使ったロックは不十分。
	 * なぜならP2Pの事情で全ノードで同時に処理を開始したいという要求があり、
	 * 自分のノードだけだったら他の処理が終わるまで待機してから開始すればいいが、
	 * 他の処理がロックを持っていても開始しなければならない。
	 * そこで、優先度次第でそれが出来るようになっている。
	 *
	 * 優先度が高い処理はどのノードでも必ずロックを取得して開始する。
	 * しかし、それによって排除される処理は、
	 * その優先度が高い処理より先に始まったか後に始まったかがノード毎に違うので、
	 * 留意する必要がある。典型的には処理を開始していざ何か重要なデータを更新する前に
	 * もう1度ロックを取得できるか確認する。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class Lock {
		private boolean lock = false;
		private long lockDate;
		private int priority;
		private long timeout;
		private Object owner;

		public boolean isTimeout() {
			long elapsed = System.currentTimeMillis() - lockDate;
			return elapsed > timeout;
		}

		private void lockSuccess(Object owner, int priority, long timeout) {
			lock = true;
			this.priority = priority;
			this.timeout = timeout;
			this.lockDate = System.currentTimeMillis();
			this.owner = owner;
		}

		public synchronized boolean lock(Object owner, int priority,
				long timeout) {
			if (this.owner == owner)
				return true;
			if (lock) {
				if (isTimeout()) {
					//タイムアウトしていたら無視してtrueを返す
					lockSuccess(owner, priority, timeout);
					return true;
				} else {
					//優先度が自分の方が高ければロックされていても無視してtrueを返す
					if (priority > this.priority) {
						lockSuccess(owner, priority, timeout);
						return true;
					} else {
						return false;
					}
				}
			} else {
				lockSuccess(owner, priority, timeout);
				return true;
			}
		}
	}

	/**
	 * シーケンスが開始した直後のUTCエポックタイムミリ秒
	 */
	private LocalDateTime startDate;

	/**
	 * 最大命令数。=counter最大値
	 */
	private static final int max = 50;

	/**
	 * @return	シーケンス開始タイミングのずれの許容誤差
	 */
	public static long getLaunchTimingTolerance() {
		return 1000L * 10;
	}

	/**
	 * 引数を渡していくのが面倒なので
	 */
	protected JobExecutionContext context;

	/**
	 * 現在の行
	 */
	protected volatile int counter = 0;

	/**
	 * 現在の命令
	 */
	protected volatile P2PStatement<?> current;

	/**
	 * シーケンスが対象とする近傍一覧
	 * シーケンス開始時に主観の近傍一覧がコピーされる
	 */
	protected ReadonlyNeighborList neighborList;

	/**
	 * 現在の命令の開始日時。ミリ秒
	 */
	private volatile long startDateCurrentStatement;

	/**
	 * 命令一覧
	 * 起動直後セットアップされる
	 * 実行中に変化しない
	 * イテレータが無いのでsynchronizedListで良い
	 */
	protected List<P2PStatement<?>> statements = Collections
			.synchronizedList(new ArrayList<P2PStatement<?>>());

	/**
	 * statementsにそのシーケンスの命令一覧を設定する
	 */
	abstract protected void setupStatements();

	/**
	 * 繰り返すか	TODO:未実装かつ実装困難かつcron4jで代用できる
	 * 常駐シーケンス、という概念である。まだ具体的には必要性が生じていない
	 */
	//protected volatile boolean loop = false;

	protected P2PSequence() {
		setupStatements();
	}

	/**
	 * シーケンス終了時に呼び出される
	 */
	public void end() {
		Glb.getLogger().info("end() " + this.getClass().getSimpleName());
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		this.context = context;
		run();
	}

	/**
	 * P2PSequenceを同時多数実行した場合に通信上で混線しないようにする識別子
	 * デフォルトでは1クラス1チャンネルだが、
	 * 必要なら具象クラス側でクラス名+追加文字列とすることで
	 * より多くのチャンネルを使える。
	 *
	 * またはタスクの名前でもある。
	 * ほとんどは1具象クラスにつき1インスタンスかつ同時スレッド数1なので
	 * デフォルトでクラス名を与えているが、多数インスタンスや多数同時実行する場合
	 * インスタンス毎に固有の名前を与える必要がある。
	 */
	public String getChannel() {
		return getChannel(0);
	}

	public static Lock getSequencelock() {
		return sequenceLock;
	}

	/**
	 * 多数同時実行する場合、nを実行毎に変える
	 * @param n
	 * @return
	 */
	public String getChannel(Integer n) {
		return this.getClass().getSimpleName() + n;
	}

	public int getCounter() {
		return counter;
	}

	public long getInitTime() {
		return 1000;
	}

	public ReadonlyNeighborList getNeighborList() {
		return neighborList;
	}

	/**
	 * 処理優先度。シーケンスの種類ごとに設定される。
	 * 現在想定している優先度は2種類しかなく、通常シーケンスと
	 * 特殊シーケンスがあると考える事ができる。
	 */
	public int getPriority() {
		return 6;
	}

	/**
	 * 開始日時
	 */
	public abstract String getStartSchedule();

	/**
	 * @return	現在の命令の最長処理時間の半分が過ぎたか
	 */
	public boolean isElapsedHalf() {
		P2PStatement<?> currentStatement = current;
		if (currentStatement == null || startDateCurrentStatement <= 0)
			return false;
		long elapsed = System.currentTimeMillis() - startDateCurrentStatement;
		long half = currentStatement.getStatementTime() / 2;
		return elapsed > half;
	}

	public String log() {
		return " counter=" + counter;
	}

	/**
	 * counterから次の命令を作成して返す。
	 * nullを返す事はシーケンス末尾を超えたことを意味する。
	 */
	public final P2PStatement<?> nextStatementSetup() {
		if (statements.size() <= counter) {
			return null;
		}
		return statements.get(counter);
	}

	/**
	 * P2PSequenceがGlb.P2Pに登録されている間、
	 * アプリが受信した全P2Pメッセージがreceive()に入力される。
	 */
	public boolean receive(Received r) {
		MessageContent c = r.getMessage().getContent();
		Glb.debug("counter=" + counter + " Content="
				+ c.getClass().getSimpleName());

		if (r.getEdgeByInnermostPackage() == null)
			return false;

		if (current == null)
			return false;

		if (!(c instanceof DistributedVoteMessage))
			return false;
		DistributedVoteMessage m = (DistributedVoteMessage) c;

		if (!current.isSupport(m))
			return false;
		return current.receive(r, m);
	}

	/**
	 * シーケンスをリセットする。
	 * 繰り返し呼び出せるようにする。
	 */
	public void reset() {
		Glb.debug(() -> "");
		counter = 0;
		startDateCurrentStatement = 0;
		neighborList = null;
		current = null;
		statements.clear();
		resetConcrete();
	}

	/**
	 * 子クラスのリセット処理。
	 * reset()をオーバーライドさせるよりこうした方がリセット処理の実装を忘れない。
	 */
	public abstract void resetConcrete();

	/**
	 * @return	シーケンス開始から終了までの時間
	 */
	public long getSequenceTime() {
		long t = 0;
		for (P2PStatement<?> s : statements) {
			t += s.getStatementTime();
		}
		Glb.debug(this.getClass().getSimpleName() + " sequenceTime=" + t);
		return t;
	}

	@Override
	public void run() {
		try {
			//TODO:スケジュールからsequenceStartを計算して全ノードで完全一致させる
			//全ノードの処理タイミングを一致させる待機処理に使われる
			long sequenceStart = System.currentTimeMillis();

			if (!sequenceLock.lock(this, getPriority(), getSequenceTime())) {
				Glb.getLogger().info(
						"Failed to lock. " + this.getClass().getSimpleName());
				return;
			}

			//受信用。メッセージはチャンネルを頼りにどのP2PSequenceインスタンスに
			//届けられるかが決まる
			Glb.getP2p().getChannelToSeq().put(getChannel(), this);

			reset();
			long elapsed = 0;
			if (getPriority() != Thread.currentThread().getPriority()) {
				Thread.currentThread().setPriority(getPriority());
				Glb.debug(() -> "priority changed: " + getPriority());
			}
			//この瞬間の近傍一覧を対象とする
			//シーケンス途中で認識が発生して近傍が変化すると大体例外になる
			neighborList = Glb.getSubje().getNeighborList().copy();

			start();

			//初期化処理分待機 start()も兼ねている
			elapsed += getInitTime();
			Glb.getUtil().sleepUntil(sequenceStart + elapsed,
					"初期化処理の同期" + log());
			do {
				Glb.debug(() -> "counter=" + counter + " statement setup");
				current = nextStatementSetup();
				if (current == null)
					break;//シーケンス完了時ここからブロックを抜ける

				elapsed += 1000;
				Glb.getUtil().sleepUntil(sequenceStart + elapsed,
						"シーケンスループの雑多な処理の同期" + log());
				if (Glb.getConf().isDevOrTest()) {
					Glb.debug("before run() elapsed=" + elapsed);
				}
				startDateCurrentStatement = sequenceStart + elapsed;
				current.run(startDateCurrentStatement, counter);
				counter++;

				elapsed += current.getStatementTime();
				if (Glb.getConf().isDevOrTest())
					Glb.debug("after run() elapsed=" + elapsed);
				Glb.getUtil().sleepUntil(sequenceStart + elapsed,
						"run()の同期" + log());
			} while (counter < max);

			// シーケンスが完了した
			end();

			//end後は同期の必要が無い

			/*	loopの場合、loop全体の時間を計測
					if (loop) {
						long endWhole = System.currentTimeMillis();
						long tolerance = 1000L * 120;
						long timeWhole = endWhole - startWhole + tolerance;
						// 繰り返し処理
						//TODO:timeWholeから動的に次のstartScheduleを設定しスケジューラに設定する。
						//単純に上のwhileを繰り返すとずれが大きくなっていくので
						//可変長時間の繰り返しや固定長時間でも膨大な繰り返しは出来ない。
						//とはいえ、十分に長い時間をとってstartScheduleを静的設定すれば
						//大抵の用途に耐える。
						reset();
					}
					*/
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		} finally {
			Glb.getP2p().getChannelToSeq().remove(getChannel(), this);
		}

	}

	public void setNeighborList(ReadonlyNeighborList neighbors) {
		this.neighborList = neighbors;
	}

	/**
	 * シーケンス開始時に呼び出される
	 */
	public void start() {
		startDate = Glb.getUtil().nowDate();
		Glb.getLogger().info("start() " + this.getClass().getSimpleName());
	}

	/**
	 * このシーケンスをスケジュールから外す。
	 * @return	外せたか
	 */
	public boolean unschedule() {
		if (context == null)
			return false;
		TimerTaskList.JobAndTrigger seq = new TimerTaskList.JobAndTrigger();
		seq.setJob(context.getJobDetail());
		seq.setTrigger(context.getTrigger());
		return Glb.getP2p().removeSeq(seq);
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}
}