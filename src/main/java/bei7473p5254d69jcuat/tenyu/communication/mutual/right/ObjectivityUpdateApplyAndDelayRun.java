package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.P2PStatement.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import glb.*;

/**
 * P2P命令
 * ユーザーメッセージリストの反映処理と余った時間で登録された遅延実行を実行する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateApplyAndDelayRun
		extends P2PStatementNoCommunication<ObjectivityUpdateSequence> {
	/**
	 * 最後にrun()が完了した日時
	 * staticにしているのは現在のスケジューラライブラリが
	 * 実行たびにインスタンスを作るからで、何らかのグローバルな状態に
	 * 最終反映日時を記録する必要があるから。
	 */
	public static long lastProcEnd;

	private ObjectivityUpdateSequence seq;

	public ObjectivityUpdateApplyAndDelayRun(
			ObjectivityUpdateSequence userMessageListSequence) {
		this.seq = userMessageListSequence;
	}

	@Override
	public long getStatementTime() {
		return statementTime;
	}

	private static final long statementTime = 1000L * 60;

	/**
	 * @return	遅延実行以外の情報を取得して返す
	 */
	private ObjectivityUpdateData getData() {
		ObjectivityUpdateData r = new ObjectivityUpdateData();
		r.setProcFromOtherModules(
				Glb.getMiddle().getProcFromOtherModules().pickup());
		r.setMessages(seq.getUserMessageList());
		return r;
	}

	@Override
	public void run(long statementStart, int counter) {
		Glb.debug("run start");

		//ロックはシーケンススタート時にも行われるが、
		//UserMessageListSequenceの反映段階だけ命令レベルでも必要。
		//もしより優先度の高い処理と開始タイミングが被ると、ノードによって
		//このシーケンスを開始するかしないかが分かれてしまう。
		//その優先度の高い処理はこのシーケンス開始直後にロックを取得する。
		//しかしその優先度の高い処理が十分に処理時間が長い場合、
		//このタイミングにおいてもまだロックを保持しているのでここで処理を止めれる。
		if (!P2PSequence.getSequencelock().lock(seq, seq.getPriority(),
				seq.getSequenceTime())) {
			Glb.debug(
					"Failed to lock in UserMessageListSequence#UserMessageListApplyAndDelayRun.");
			return;
		}

		//客観は全ノードで一致しなければならない。
		//その考えからするとトランザクションを全部分で1つにしたほうがいいかもしれない。
		//しかし分けても全ノードでどの部分について成功し失敗するかが一致するなら問題無いし
		//いずれかの部分で障害が発生した場合でも他の部分について処理を進めれるという点でやや耐障害性が高い。

		try {
			//日時更新
			Glb.getObje().updateGlobalCurrentTime();
			//次のHI
			long nextHistoryIndex = Glb.getObje().getCore().getHistoryIndex()
					+ 1;

			//今回の客観更新の全情報
			ObjectivityUpdateData data = getData();
			data.setHistoryIndex(nextHistoryIndex);

			//他モジュールによる客観更新
			List<ObjectivityUpdateDataElement> processedOtherModules = Glb
					.getObje().applyOtherModuleProc(
							data.getProcFromOtherModules(), nextHistoryIndex);
			data.setProcFromOtherModules(processedOtherModules);

			//優先度が高い遅延実行系の客観更新
			//他のトランザクションと分けられる。
			//重要な処理が多く、通常の客観更新の失敗に連れ立って失敗されては困るため。
			List<ObjectivityUpdateDataElement> processedSuperior = Glb.getObje().writeTryW(txn -> {
				AgendaStore as = new AgendaStore(txn);
				return as.applyDelayRuns(2000, nextHistoryIndex);
			});
			data.setProcessedSuperiors(processedSuperior);

			//UserRightRequest系の客観更新
			long applySizeThisTime = Glb.getObje()
					.applyMessageList(data.getMessages(), nextHistoryIndex);

			//優先度が低い遅延実行系の客観更新（現状無し）
			boolean inferiorProcessed = false;

			//客観更新が行われたか
			boolean updated = !processedOtherModules.isEmpty()
					|| !processedSuperior.isEmpty() || applySizeThisTime > 0
					|| inferiorProcessed;
			seq.getSkipApply().set(!updated);
			Glb.getLogger().info("historyIndex updated=" + updated);
			if (updated) {
				//HIを客観DB上で更新する
				updateHistoryIndex(nextHistoryIndex);
				//全客観更新情報のログを記録
				Glb.getObje().writeTryW(txn -> {
					ObjectivityUpdateDataStore s = new ObjectivityUpdateDataStore(
							txn);
					return s.randomWrite(data);
				});
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		lastProcEnd = System.currentTimeMillis();
		Glb.debug("run end");
	}

	private void updateHistoryIndex(long nextHistoryIndex) {
		Glb.getObje().writeTryW(txn -> {
			//HIインクリメント
			return Glb.getObje().updateCore(txn, c -> {
				long newHI = c.incrementHistoryIndex();
				//このようなことが生じた場合DBが想定外の状態になっている事を意味するが
				//後の処理を止めるべきか継続すべきかの判断は難しく、継続する事にしている。
				if (newHI != nextHistoryIndex) {
					Glb.getLogger().error("", new Exception(
							"Fatal error. historyIndex is wrong."));
				}
				Glb.getObje().fireHistoryIndexIncremented(newHI);
			});
		});
	}

	/**
	 * @return	最後に反映処理が完了してから十分に時間が経過したか
	 * 最長処理時間の半分以上が経過するとtrue
	 * 次の反映処理が始まった直後などもtrue
	 * falseが返るのは反映処理が完了してから30秒の間だけ
	 */
	public static boolean isLongApplied() {
		long elapsed = System.currentTimeMillis() - lastProcEnd;
		long half = statementTime / 2;
		return elapsed > half;
	}

	/**
	 * @return	反映処理完了直後かつ反映処理段階か
	 */
	public static boolean isNotLongAppliedAndApplying() {
		long elapsed = System.currentTimeMillis() - lastProcEnd;
		if (elapsed > statementTime)
			return false;
		return Glb.getMiddle().getObjectivityUpdateSequence().isApplying();
	}

	@Override
	public void reset() {
	}
}
