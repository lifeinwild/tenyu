package bei7473p5254d69jcuat.tenyu.release1.global.middle;

import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;

/**
 * 各ユーザーと自分の間に作られた情報を蓄積する。
 * この情報は非統一値で、C/S的である。基本的にユーザーがFQDNを持っている場合のみ機能する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserEdgeList {
	private static final int max = 1000 * 1000 * 1000;

	/**
	 * この時間を過ぎた情報が削除される
	 */
	private static final long timeLimit = 1000L * 60 * 60 * 6;

	public static long getTimelimit() {
		return timeLimit;
	}

	/**
	 * 仮
	 * transientでありchainversionupの必要性が無い
	 */
	private transient Map<NodeIdentifierUser,
			UserEdge> edgesUnsecure = new ConcurrentHashMap<>();

	/**
	 * 定期的にgc()を呼び出す
	 */
	private transient ScheduledFuture<?> gc = null;

	/**
	 * 共通鍵を再交換する。相手側で鍵が削除されていた場合等。
	 * @param identifier
	 */
	public boolean commonKeyExchange(NodeIdentifierUser identifier,
			InetSocketAddress addr) {
		return CommonKeyExchangeUser.send(identifier, addr);
	}

	/**
	 * @param identifier
	 * @return	ノード0番のこのユーザーのUserEdge
	 */
	public UserEdge getEdge(NodeIdentifierUser identifier) {
		return UserEdgeStore.getSimple(identifier);
	}

	/**
	 * UserEdgeのアドレスを更新する
	 * userIdとnodeNumberでUserEdgeが特定されアドレスが更新される
	 * @param userId
	 * @param addr		これに更新される
	 * @param nodeNumber
	 * @return	更新に成功したか
	 */
	public boolean updateAddr(Long userId, AddrInfo addr, int nodeNumber) {
		return Glb.getObje().compute(txn -> {
			try {
				UserEdgeStore ues = new UserEdgeStore(txn);
				UserEdge ue = ues.get(userId, nodeNumber);
				ue.setAddr(addr);
				return ues.update(userId, nodeNumber, ue);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	/**
	 * ユーザー間で共通鍵交換を確認したら呼ぶ
	 * UserEdgeが本リストに入る
	 * @param userId
	 * @return
	 */
	public synchronized boolean confirmation(NodeIdentifierUser identifier) {
		UserEdge edge = edgesUnsecure.get(identifier);
		if (edge == null)
			return false;
		boolean r = Glb.getDb(Glb.getFile().getMiddleDBPath())
				.computeInTransaction(txn -> {
					try {
						UserEdgeStore ues = new UserEdgeStore(txn);
						if (ues.count() > max)
							return false;
						return ues.create(identifier, edge);
					} catch (Exception e) {
						Glb.getLogger().error("", e);
						return false;
					}
				});
		if (!r)
			return false;
		edgesUnsecure.remove(identifier);
		return true;
	}

	/**
	 * 使われていない情報を削除する
	 */
	private void gc() {
		//gc(edges);
		gc(edgesUnsecure);
	}

	private void gc(Map<NodeIdentifierUser, UserEdge> edges) {
		//削除されるライン
		long line = System.currentTimeMillis() - timeLimit;
		for (Entry<NodeIdentifierUser, UserEdge> e : edges.entrySet()) {
			//十分に古ければ削除
			if (e.getValue().getAccessDate() < line) {
				edges.remove(e.getKey());
			}
		}
	}

	public UserEdge getFromUnsecure(NodeIdentifierUser identifier) {
		return edgesUnsecure.get(identifier);
	}

	public CommonKeyExchangeState getReceiveKey(NodeIdentifierUser from) {
		UserEdge ue = UserEdgeStore.getSimple(from);
		return ue.getReceiveKey();
	}

	public CommonKeyExchangeState getSendKey(NodeIdentifierUser to) {
		UserEdge ue = UserEdgeStore.getSimple(to);
		return ue.getSendKey();
	}

	/**
	 * @param identifier
	 * @return	userIdとUserEdgeによる通信が可能か
	 */
	public boolean isEstablished(NodeIdentifierUser identifier) {
		if (getReceiveKey(identifier) != null
				&& getSendKey(identifier) != null) {
			return true;
		}
		return false;
	}

	/**
	 * @param identifier
	 * @return			最近このユーザーと共通鍵交換が開始されたか
	 */
	public synchronized boolean isStartedRecently(
			NodeIdentifierUser identifier) {
		UserEdge e1 = edgesUnsecure.get(identifier);
		if (e1 != null && e1.isStartedIn1Minute())
			return true;
		UserEdge e2 = UserEdgeStore.getSimple(identifier);
		if (e2 != null && e2.isStartedIn1Minute())
			return true;
		return false;
	}

	/**
	 * ユーザーから共通鍵を受信したら呼ぶ
	 * @param from	ここから共通鍵を受信した
	 * @param cki			受信した共通鍵
	 */
	public boolean receive(NodeIdentifierUser from, CommonKeyInfo cki) {
		if (edgesUnsecure.size() > max)
			return false;

		UserEdge edge = edgesUnsecure.get(from);
		if (edge == null) {
			edge = new UserEdge();
			edgesUnsecure.put(from, edge);
		}
		edge.updateAccessDate();
		edge.getReceiveKey().setCommonKeyInfo(cki);
		return true;
	}

	/**
	 * ユーザーへ共通鍵を送信したら呼ぶ
	 * @param to		ここへ共通鍵を送信した
	 * @param cki			送信した共通鍵
	 */
	public boolean sendReport(NodeIdentifierUser to, CommonKeyInfo cki) {
		if (edgesUnsecure.size() > max)
			return false;

		UserEdge edge = edgesUnsecure.get(to);
		if (edge == null) {
			edge = new UserEdge();
			edgesUnsecure.put(to, edge);
		}
		edge.updateAccessDate();
		edge.getSendKey().setCommonKeyInfo(cki);
		return true;
	}

	public void start() {
		if (gc != null && !gc.isCancelled())
			gc.cancel(false);

		gc = Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> gc(),
				timeLimit, timeLimit, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		if (gc != null && !gc.isCancelled())
			gc.cancel(false);
	}
}
