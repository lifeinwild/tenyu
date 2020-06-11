package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import glb.*;

/**
 * 同調処理、メッセージリスト拡散、メッセージリスト反映の3種類の処理があり、
 *
 * →拡反拡反拡反拡反拡反→
 * →同同同同同同同同同同→　
 *
 * というように2スレッドで実行される。
 *
 * 反映処理は同調処理が終わった場合のみ実行される。
 * これらの仕様は後発のノードが最新値を持つノードに追いつけるよう構成されている。
 *
 * 同調処理	復帰、新規、微調整処理の総称
 * 拡散	メッセージリストを拡散する
 * 反映	メッセージリストを反映して統一値を更新する
 * 復帰	少しオフラインだったノードが素早く最新値へと同調する
 * 新規	全データを同調する
 * 微調整	最新値かそれに近い値を持っていると期待できるノードが
 * 			近傍とハッシュ照合を行い僅かな違いを埋める。
 * 整合性検証	近傍の多数派と客観が一致しているか調べる
 * 客観	統一値のうち動的に変動する部分。Objectivity系ストアやObjectivityCore
 *
 *
 * BenchmarkTestの結果から、ReadonlyTransactionはいちいちトランザクションを作っても
 * 問題無い。書き込み系はできるだけ1トランザクションで多くの件数を処理すべき。
 * とはいえ、xodusのトランザクション処理は、他のトランザクションから書き込みが
 * 行われた場合にやり直すというものなので、粒度をでかくするなら
 * やり直し処理のコストについて留意する必要がある。
 *
 * MVCCの内部実装は書き込みトランザクションをやり直すようになっているが、
 * 反映処理は巨大トランザクションになるのでやり直しはできない。
 * そこで、必ず直列化する必要がある。反映処理時に
 * 全種類の反映を行う。
 * なおMVCCなので読み取り専用トランザクションであれば、
 * ある時点の状態を常に参照し続けれる。
 * これでフロー計算のような長時間処理の問題を乗り切れる。
 * そのMVCCの性質は実験コードで確認し、正常に動作した。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateSequence extends P2PSequence {
	/**
	 * シーケンスが終わったか
	 */
	private boolean finish = false;

	/**
	 * ユーザーメッセージリストシーケンスの1回分の時間
	 * この時間は将来に渡って不変と想定している
	 * この値は{@link P2PSequence#getSequenceTime()}で別の方法で計算されているが
	 * この値と一致している必要がある。
	 */
	public static final long sequenceTime = 1000L * 60 * 2;

	/**
	 * 分散合意の結果
	 */
	public static final List<DistributedVote> distributedVoteResult = new CopyOnWriteArrayList<>();

	/**
	 * 新しいhistoryIndex
	 * 現在のhistoryIndex+1
	 */
	protected long nextHistoryIndex;

	public AtomicBoolean getSkipApply() {
		return skipApply;
	}

	/**
	 * 拡散段階の前半になるまで待機する。
	 * 最長2分でタイムアウト。
	 */
	public void waitForDiffusionStep1stHalf() {
		Glb.debug("拡散段階になるまで待機");
		long start = System.currentTimeMillis();
		long elapsed = 0;
		//拡散段階の前半になるかメッセージリストシーケンス1回分の時間が過ぎたら待機終了
		while (!(counter == 1 && !isElapsedHalf()) || elapsed > sequenceTime) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			elapsed = System.currentTimeMillis() - start;
		}
	}

	/**
	 * 客観更新シーケンスが終了するまで待機する
	 */
	public void waitForFinish() {
		Glb.debug("客観更新シーケンスが終了するまで待機");
		long start = System.currentTimeMillis();
		long elapsed = 0;
		long tolerance = 1000 * 10;
		//拡散段階の前半になるかメッセージリストシーケンス1回分の時間が過ぎたら待機終了
		while (!isFinish() || elapsed < getSequenceTime() + tolerance) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			elapsed = System.currentTimeMillis() - start;
		}
	}

	/**
	 * 直前の反映処理を飛ばしたか
	 */
	private static final transient AtomicBoolean skipApply = new AtomicBoolean(
			false);

	/**
	 * これに応じて客観が更新される。
	 * 拡散段階で拡散取得して、反映段階で適用する。
	 */
	private UserMessageList userMessageList;

	/**
	 * 2分毎
	 */
	public static String startSchedule = "0 */2 * * * ?";

	public static TimerTaskList.JobAndTrigger getJob() {
		return TimerTaskList.getJob(null, ObjectivityUpdateSequence.class,
				startSchedule);
	}

	/**
	 * 一度回避した反映処理を、反映段階において途中で反映する
	 * @return	反映に成功したか
	 */
	public boolean applyInMiddle() {
		if (!skipApply.get())
			return false;
		skipApply.set(false);
		UserMessageList l = userMessageList;
		//反映処理は長いので反映処理の前にnullを設定する
		//こうすることで同じリストが反映される可能性を無くす
		//この処理は場合によって拡散期間まで及ぶので
		//userMessageListは拡散処理によって新たなリストが設定される可能性があり
		//反映処理の前にnullを入れないといけない
		userMessageList = null;
		return Glb.getObje().applyMessageList(l) > 0;
	}

	@Override
	public void start() {
		super.start();
		Glb.getMiddle().setObjectivityUpdateSequence(this);
	}

	@Override
	public void end() {
		super.end();
		finish = true;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public long getNextHistoryIndex() {
		return nextHistoryIndex;
	}

	@Override
	public String getStartSchedule() {
		return startSchedule;
	}

	public UserMessageList getUserMessageList() {
		return userMessageList;
	}

	public boolean isSkipApply() {
		return skipApply.get();
	}

	@Override
	protected void setupStatements() {
		statements.add(new UserMessageListDiffusion(this));
		statements.add(new ObjectivityUpdateApplyAndDelayRun(this));
	}

	/**
	 * @return	反映処理段階か
	 */
	public boolean isApplying() {
		return counter == 1;
	}

	@Override
	public void resetConcrete() {
		nextHistoryIndex = Glb.getObje().getCore().getHistoryIndex() + 1;
		userMessageList = null;
		skipApply.set(false);
		statements.clear();
		setupStatements();
	}

	public void setHistoryIndex(long historyIndex) {
		this.nextHistoryIndex = historyIndex;
	}

	public void setUserMessageList(UserMessageList userMessageList) {
		this.userMessageList = userMessageList;
	}
}
