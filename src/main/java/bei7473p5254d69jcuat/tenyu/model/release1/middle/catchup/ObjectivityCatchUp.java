package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import glb.*;
import glb.Conf.*;
import glb.Glb.*;

/**
 * 同調処理
 * 専用スレッドが随時客観を近傍の多数派に同調させる。
 *
 * 客観は拡散段階と反映段階がある。{@link ObjectivityUpdateSequence#setupStatements}
 *
 * 反映処理中は近傍の客観がばらばらになるので、
 * MessageListSequenceの段階に応じて行っていい反映処理が限定される。
 *
 * このクラスのisCatchUp()等は近傍の多数派のハッシュ値等について
 * 最新値を把握できているという前提が満たされている限り、正しい値を返す。
 *
 * 同調系処理毎の開始条件
 * 処理					その処理を実行していい状態の条件
 * コアハッシュ			APPLYING×
 * 最後のID				APPLYING×
 * 最上位ハッシュ		APPLYING×
 * コア					いつでも
 * 復帰					いつでも
 * ID差					いつでも
 * ハッシュ照合			いつでも
 * 拡散					いつでも
 * 反映					同調済みの場合
 *
 * 仕様を構成するに当たって
 * １つの基本的考え方はとにかくハードウェア性能をできるだけ発揮させるという考えが有力。
 *
 * 新規や復帰が同調処理を終えた時、
 *  - 微調整段階かつ最上位ハッシュが一致。		メッセージ反映へ
 *  - 微調整段階かつ最上位ハッシュが異なる。	反映段階でも微調整続行＆反映後に更新リスト追随
 *  - 反映段階かつ最上位ハッシュが一致。		メッセージ反映へ。反映完了は微調整段階の最中になる場合もあるが
 *  											その時最上位が一致している場合もある。
 *  											一致していなければ微調整＆更新リスト追随へ。
 *  - 反映段階かつ最上位が異なる。					微調整＆更新リスト追随
 *  なお微調整はいずれも1万件程度を上限とする。微調整で大きな処理をしない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityCatchUp implements GlbMemberDynamicState {
	private boolean catchUp = false;

	/**
	 * これより前の全ヒストリーインデックスのメッセージリストが反映済み。
	 * 他にObjectivityCore#historyIndexが類似した情報として存在するが、
	 * ObjectivityCoreは同調処理で上書きされる可能性があるので、
	 * 自分の持っている客観が少なくとも何番までのデータを確実に持っていると言えるか、
	 * という情報はこれだけ。
	 */
	private long catchUpHistoryIndex = -1;

	private ScheduledFuture<?> catchUpThread = null;

	/**
	 * 客観コアの同調状態
	 */
	private CatchUpStateCore coreState = new CatchUpStateCore();

	/**
	 * DBの同調処理に関する状態
	 */
	private transient CatchUpStateDB dbState = new CatchUpStateDB();

	/**
	 * 初期同調
	 * 同調処理無しで、反映処理のみで近傍の多数派に一致しているか
	 */
	private boolean initiallyCatchUp = false;

	/**
	 * 連続して初期同調した回数
	 */
	private long initiallyCatchUpCount = 0;

	/**
	 * 客観コアの同調処理に関する状態
	 */
	private transient CatchUpStateIntegrity integrityState = new CatchUpStateIntegrity();

	/**
	 * 同調スレッドは様々なタイミングでこのフラグを見て
	 * trueなら停止する。
	 */
	private boolean stop = true;

	public boolean isStop() {
		return stop;
	}

	public long getCatchUpHistoryIndex() {
		return catchUpHistoryIndex;
	}

	/**
	 * @return	同調処理から得られた分散合意における自信のような値
	 * 0-10
	 */
	public int getCatchUpImpression() {
		long impressionn = initiallyCatchUpCount;
		if (impressionn > 10)
			impressionn = 10;
		return (int) impressionn;
	}

	/**
	 * 分散合意において自分自身をどれだけ信じるか
	 * 周囲の平均的信用にこの係数がかけられる
	 */
	public double getCreditSelfMultiplier() {
		//基本倍率
		double d = 1.0;
		//アップデート直後じゃなければ倍率変更
		if (!Glb.getConf().isAfterUpdateLaunch()) {
			d = initiallyCatchUpCount / 10;
		} //アップデート直後なら基本倍率のまま

		return Glb.getConf().getRunlevel().equals(RunLevel.DEV) ? 1 : 2.5 * d;
	}

	public CatchUpStateDB getDbState() {
		return dbState;
	}

	public CatchUpStateIntegrity getIntegrityState() {
		return integrityState;
	}

	/**
	 * 没案
	 * @return
	 */
	public Map<Long, UserMessageList> getLog() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * @return	自分は他ノードに客観状態を主張していいようなノードか
	 */
	public boolean imVeteran() {
		if (Glb.getConf().isAfterUpdateLaunch())
			return true;
		if (initiallyCatchUp) {
			return true;
		}
		//ある程度時間が経ったら同調完了していなくても主張し始める
		//この仕様は、もしネットワークで客観状態がバラバラになったとしても、
		//客観状態を収束させる確率を高める。
		long elapsedAfterLaunch = System.currentTimeMillis()
				- Glb.getConf().getLaunchDate();
		if (elapsedAfterLaunch > 1000L * 60 * 30) {
			return true;
		}
		return false;
	}

	public void incrementCatchUpHistoryIndex() {
		catchUpHistoryIndex++;
	}

	public void initiallyCatchUp(boolean catchUp) {
		if (catchUp) {
			//初期同調
			initiallyCatchUp = true;
			initiallyCatchUpCount++;
			catchUp = true;
		} else {
			initiallyCatchUp = false;
			initiallyCatchUpCount = 0;
			catchUp = false;
		}

		if (Glb.getConf().isAfterUpdateLaunch()) {
			//アップデートからしばらく時間が経ったらフラグ解除
			long now = System.currentTimeMillis();
			long elapsed = now - Glb.getConf().getLaunchDate();
			if (elapsed > 1000L * 60 * 20) {
				Glb.getConf().setAfterUpdateLaunch(false);
			}
		}
	}

	/**
	 * @return	全客観が同調完了したか
	 */
	public boolean isCatchUp() {
		return catchUp;
	}

	public boolean isInitiallyCatchUp() {
		return initiallyCatchUp;
	}

	/**
	 * 反映処理直後に呼ばれる
	 */
	public void notifyAfterApplied() {
		integrityState.catchUpAsync();
	}

	public void setCatchUpHistoryIndex(long catchUpHistoryIndex) {
		this.catchUpHistoryIndex = catchUpHistoryIndex;
	}

	public void setDbCatchUpHistoryIndex(long dbCatchUpHistoryIndex) {
		this.catchUpHistoryIndex = dbCatchUpHistoryIndex;
	}

	public void setInitiallyCatchUpCount(long initiallyCatchUpCount) {
		this.initiallyCatchUpCount = initiallyCatchUpCount;
	}

	/**
	 * 同調処理が完了した時に呼ぶ。
	 * すぐに終わる処理か非同期な処理であること。
	 */
	public void fireCatchUpCompleted() {

	}

	public void start() {
		stop = false;
		long period = 1000L * 15;
		if (catchUpThread != null && !catchUpThread.isCancelled())
			catchUpThread.cancel(false);
		catchUpThread = Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> {
			CatchUpTask task = new CatchUpTask();
			task.run();
		}, period, period, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		stop = true;
		if (catchUpThread != null && !catchUpThread.isCancelled()) {
			catchUpThread.cancel(false);
			//キャンセルが完了するまで少し待つ
			long start = System.currentTimeMillis();
			long elapsed = 0;
			while (catchUpThread.isDone() && elapsed < 1000L * 5) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
				elapsed = System.currentTimeMillis() - start;
			}
			catchUpThread = null;
		}
	}

	/**
	 * このモジュール内のグローバル状態みたいな位置付け。
	 * これを配下の様々なオブジェクトが利用する。
	 */
	private CatchUpContext ctx;

	public CatchUpContext getCtx() {
		return ctx;
	}

	/**
	 * ネストが深いので分けた
	 * 各処理単位は停止処理に留意する必要がある。
	 * stop()メソッドに対応すること。
	 *
	 * 全同調処理は1スレッドで行われる。
	 * ctxはモジュール内のグローバル状態として様々な箇所から参照されているが、
	 * ctxへの再代入は1スレッドであるという前提があるから問題が起きない。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private class CatchUpTask implements Runnable {

		//同調処理を完走した整合性情報
		private Integrity majorityAtFinish = null;

		private int step = 0;

		/**
		 * 1回の同調処理の最初に呼ばれる。
		 * 1回の同調処理とは、1個の多数派整合性情報に対して行われる多種類かつ複数回の同調処理。
		 * @return	同調処理を開始するか
		 */
		private boolean firstTimeSetup() {
			//多数派整合性情報を取得できていないなら開始できない
			if (integrityState.getMajorityIntegrity() == null
					|| integrityState.getTeachers() == null) {
				return false;
			}

			//完走済みの整合性情報について再度処理しない
			if (integrityState.getMajorityIntegrity() == majorityAtFinish) {
				return false;
			}

			//以下、同調処理をする場合

			resetTask();

			//同調文脈に多数派整合性情報を設定
			ctx.setMajorityAtStart(integrityState.getMajorityIntegrity());
			ctx.setTeachers(integrityState.getTeachers());

			//同調処理内部の各所でいちいち作るよりここで一度作るだけに留める。
			//性能を考慮した結果
			ctx.setMyAtStart(Glb.getObje().getIntegrity());
			/*
						//同調開始直後、整合性情報が一致しているか
						if (ctx.getMyAtStart().equals(ctx.getMajorityAtStart())) {
							initiallyCatchUp(true);
							resetTask();
							return false;
						} else {
							//これを入れないと同調処理は呼ばれない
							reset();

							initiallyCatchUp(false);
						}
						*/
			return true;
		}

		private void resetTask() {
			ctx = new CatchUpContext();
			majorityAtFinish = ctx.getMajorityAtStart();
			step = 0;

			coreState.reset();
			dbState.reset();
		}

		@Override
		public void run() {
			resetTask();
			long start = -1;
			while (true) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}

				if (stop) {
					//停止処理
					return;
				}

				if (catchUp)
					continue;

				try {
					//起動直後nullなので設定されてから同調開始
					if (integrityState.getMajorityIntegrity() == null)
						continue;

					//1回の同調は同じ整合性情報に対して行われる
					//majorityは同調終了時nullになるので、新たな同調開始時のみnull
					if (ctx.getMajorityAtStart() == null) {
						start = System.currentTimeMillis();
						if (!firstTimeSetup())
							continue;
					}

					stepTask();
				} catch (Exception e) {
					Glb.debug("", e);
					resetTask();
				}
				if (catchUp) {
					if (start != -1) {
						long end = System.currentTimeMillis();
						Glb.getLogger()
								.info("catchUp time:" + (end - start) + "ms");
					}
				}
			}

		}

		/**
		 * ネストが深いので分けた
		 * @throws Exception
		 */
		private void stepTask() throws Exception {
			//同調処理の各部は完走したとしても同調成功を保証しない。
			switch (step) {
			case 0:
				//コア同調
				if (!coreState.isFinish()) {
					Glb.debug("coreState.catchUp(ctx)");
					coreState.catchUp();
				}

				//DB同調
				if (!dbState.isFinish()) {
					Glb.debug("dbState.catchUp(ctx)");
					dbState.catchUp();
				}

				if (coreState.isFinish() && dbState.isFinish()) {
					Glb.debug("catchup finish");
					//同調完走

					//完走時点の自分の整合性情報
					Integrity latestMyIntegrity = Glb.getObje().getIntegrity();

					//完走時点の多数派の整合性情報
					Integrity latestMajorityIntegrity = integrityState
							.getMajorityIntegrity();

					//同調成功したか
					if (latestMyIntegrity.equals(latestMajorityIntegrity)) {
						Glb.debug("catchUp success");
						//同調成功
						catchUp = true;

						//もし反映段階なら最新のメッセージリストを反映する
						ObjectivityUpdateSequence seq = Glb.getMiddle()
								.getObjectivityUpdateSequence();
						if (seq != null && seq.isApplying()
								&& seq.isSkipApply()) {
							seq.applyInMiddle();
						}
					} else {
						Glb.debug("catchup failed");
						//同調失敗
						catchUp = false;
					}

					resetTask();
				}
				break;
			default:
				resetTask();
			}

		}
	}

}
