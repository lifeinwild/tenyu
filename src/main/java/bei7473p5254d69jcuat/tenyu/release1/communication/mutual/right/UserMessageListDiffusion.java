package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.P2PStatement.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.HasUserMessageList.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.catchup.GetUserMessageList.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.Middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

public class UserMessageListDiffusion
		extends P2PStatementFreeTiming<ObjectivityUpdateSequence> {
	private ObjectivityUpdateSequence seq;

	public UserMessageListDiffusion(ObjectivityUpdateSequence userMessages) {
		seq = userMessages;
	}

	private void diffusion() {
		Glb.debug("diffusion start");
		long start = System.currentTimeMillis();
		long elapsed = 0;
		long tolerance = 5000;
		long time = getStatementTime() - tolerance;
		//メッセージ受付サーバの起動等を待つため少し待機
		//やはり待つ必要無し。拡散の時間もシビア	Glb.getUtil().sleepUntil(start + 1000 * 10);

		//メッセージリストを持っていなければ近傍から取得。取得できるまで繰り返す
		while (seq.getUserMessageList() == null) {
			elapsed = System.currentTimeMillis() - start;
			if (elapsed >= time) {
				Glb.getLogger().error(
						"Failed to get UserMessageList. time over elapsed="
								+ elapsed,
						new Exception());
				break;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			try {
				//レイテンシ昇順の近傍一覧
				List<P2PEdge> neighbors = Glb.getSubje().getNeighborList()
						.getNeighborsLowLatencySortConnectedIn5Minute();

				ReadonlyNeighborList neighborList = new ReadonlyNeighborList(
						neighbors);

				//全ノードにリストを持っているか問い合わせる
				//各リクエストの状態一覧
				AsyncRequestStatesFreeSharding<
						HasUserMessageList> states = new AsyncRequestStatesFreeSharding<>(
								() -> neighborList);
				states.setRetryMax(0);
				Glb.debug("neighbors=" + neighbors);

				states.requestToAll(to -> new HasUserMessageList(
						Glb.getObje().getCore().getHistoryIndex() + 1));

				P2PEdge to = waitAndGetMessageList(states);
				if (to == null)
					continue;
				UserMessageList list = getMessageList(to);
				if (list != null) {
					//信用を高める。なお問い合わせ先に選ばれたことはレイテンシが短い事を意味し、
					//ここで信用を高める事は低レイテンシな近傍を増やす事に繋がる
					to.updateImpression(1.02);
					seq.setUserMessageList(list);
					//この検証処理をset前にやると処理時間がかかるので拡散が遅れる
					//すぐに拡散させつつ、検証に失敗すればnullに設定する。この動作は各ノードが行うので
					//間違ったリストは必ず排除される。
					//この検証処理は非常に時間がかかるので反映処理の開始時間を超える可能性があるが、
					//その場合でもできるだけ追いつくように動作する。それで問題無いと判断した。
					if (!list.validateAndRemove()) {
						Glb.getLogger().error(
								"validateAndRemove() returns false",
								new Exception());
						seq.setUserMessageList(null);
					}
					break;
				} else {
					//持っていると言ったのに取得できなかったら信用を低下させる
					//たとえそれが嘘でなかったとしてもそのノードを
					//近傍から排除した方が良いという判断が強まる
					//validateに成功した場合しか持っていると言わないはずなので
					//受付サーバが間違ったデータを流しても一般ノードが間違って持っていると主張する事はない
					//10%減少
					to.updateImpression(0.9);
				}
			} catch (Exception e) {
				Glb.debug(e);
				continue;
			}
		}
	}

	/**
	 * ユーザーメッセージリストを同期通信で取得する
	 * @return	ユーザーメッセージリスト、取得できなかったらnull
	 */
	private UserMessageList getMessageList(P2PEdge to) {
		try {
			GetUserMessageList req = new GetUserMessageList();
			Message reqM = Message.build(req).packaging(req.createPackage(to))
					.finish();
			Message resM = Glb.getP2p().requestSync(reqM, to);
			if (Response.fail(resM))
				return null;
			GetUserMessageListResponse res = (GetUserMessageListResponse) resM
					.getContent();
			UserMessageList list = res.getList();
			if (list != null) {
				return list;
			}
		} catch (Exception e) {
			Glb.debug(e);
		}
		return null;
	}

	@Override
	public long getStatementTime() {
		return 1000L * 60;
	}

	@Override
	public void reset() {
	}

	@Override
	public void run(long statementStart, int counter) {
		//自分がメインサーバならメッセージリストを設定する
		//ここからP2Pネットワークに拡散されていく
		UserMessageListServer main = Glb.getMiddle().getUserMessageListServer();
		//同調状況が混沌の場合、サーバが新たなメッセージリストを流さない。
		//一旦同調状況が落ち着いてから再度更新をスタートさせる。
		if (main.isStarted() && Glb.getMiddle().getObjeCatchUp()
				.getCurrentCircumstance() != ObjectivityCircumstance.CHAOS) {
			UserMessageList l = main.getAndNewMessageList();
			if (l == null) {
				Glb.getLogger()
						.warn("UserMessageList is null. getCurrentCircumstance="
								+ Glb.getMiddle().getObjeCatchUp()
										.getCurrentCircumstance());
			} else if (l.size() == 0) {
				Glb.getLogger().info("UserMessageList#count() is zero.");
			} else {
				Glb.getLogger().info("UserMessageList#count() is " + l.size());
				seq.setUserMessageList(l);
			}
		} else {
			diffusion();
		}
	}

	/**
	 * @param states	メッセージリストを持っているかの問い合わせの通信状態一覧
	 * @return	メッセージリストを持つ近傍
	 */
	private P2PEdge waitAndGetMessageList(
			AsyncRequestStatesFreeSharding<HasUserMessageList> states) {
		//TODO こんな方法しかないか？
		List<P2PEdge> r = new ArrayList<P2PEdge>();
		//回答待ち
		long hasStart = System.currentTimeMillis();
		long hasElapsed = 0;
		while (hasElapsed < 1000L * 15) {//15秒程度で返信が来なければその後のメッセージリストを受信しきれる確率は低い
			try {
				Thread.sleep(100);
				states.checkAndFireRetry((hasState) -> {
					//返信を取得
					if (hasState == null || hasState.getReq() == null
							|| hasState.getReq().getRes() == null
							|| hasState.getReq().getRes().getContent() == null
							|| !(hasState.getReq().getRes()
									.getContent() instanceof HasUserMessageListResponse)) {
						return true;
					}
					HasUserMessageListResponse res = (HasUserMessageListResponse) hasState
							.getReq().getRes().getContent();
					//メッセージリストを持っていたら
					if (res.isHas()) {
						r.add(hasState.getTo());
						Glb.debug("hasState.getTo()=" + hasState.getTo());
					}
					return true;
				});

				hasElapsed = System.currentTimeMillis() - hasStart;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
		states.clear();
		if (r.size() == 0) {
			return null;
		} else {
			return r.get(0);
		}
	}
}
