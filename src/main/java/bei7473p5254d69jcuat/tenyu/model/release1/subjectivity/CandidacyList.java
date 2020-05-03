package bei7473p5254d69jcuat.tenyu.model.release1.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 全体運営者の立候補者リスト
 *
 * 立候補者リストはノード毎にばらばらでいいが、
 * 「立候補者毎の拡散ノード数」がある程度ないと意味がない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CandidacyList implements StorableI {
	/**
	 * 立候補者の増加ペースは制限される
	 * 没案。最大件数の制限と信用に基づく優先で十分と判断した。
	 */
	//private static final long addInterval = 1000 * 60 * 60 * 3;

	/**
	 * 近傍からの取得が行われる間隔
	 * 20回の取得でネットワーク全体に行き渡るという想定
	 * （とはいえ完全に行き渡っていなくても問題ない）
	 */
	public static final long diffusionInterval = 1000 * 60 * 60 * 3 / 20;

	/**
	 * 立候補者リストの最大要素数
	 */
	public static final int max = 100;

	/**
	 * 立候補者リスト
	 */
	private List<Message> candidacyMessages = new ArrayList<>();

	/**
	 * 直前の立候補者
	 */
	private Message previous;

	public synchronized int size() {
		return candidacyMessages.size();
	}

	/**
	 * 信用が低いユーザーから最大件数を超えた分削除
	 */
	private synchronized int removeByCredit() {
		return removeByCredit(candidacyMessages.size() - max);
	}

	/**
	 * 信用が低いユーザーから指定した件数削除
	 * @param n	削除件数
	 * @return 削除された件数
	 */
	private synchronized int removeByCredit(int n) {
		int r = 0;
		if (n <= 0)
			return r;
		sortByCredit();
		//末尾n件削除
		int total = candidacyMessages.size();
		for (int i = 0; i < n && i < total; i++) {
			candidacyMessages.remove(total - i - 1);
			r++;
		}
		return r;
	}

	/**
	 * {@link Sociality#} 信用順でソート
	 * ここでは主観信用と客観信用の合計を用いる
	 */
	private synchronized void sortByCredit() {
		Collections.sort(candidacyMessages, new Comparator<Message>() {
			public int compare(Message o1, Message o2) {
				//昇順ソートにおいて信用が大きなノードが最初に来るようにする
				int c1 = getCredit(o1);
				int c2 = getCredit(o2);
				if (c1 < c2)
					return 1;
				if (c1 > c2)
					return -1;
				Long id1 = o1.getUserId();
				Long id2 = o2.getUserId();
				//IDがある方が最初に来るように（そもそもID無しでは立候補が機能しない）
				if (id1 == null && id2 != null)
					return 1;
				if (id1 != null && id2 == null)
					return -1;
				if (id1 == null && id2 == null)
					return 0;
				//古いIDを持つ方が最初に来るように。IDは一意なのでこれで完全にソートできる
				if (id1 > id2)
					return 1;
				if (id1 < id2)
					return -1;

				return 0;
			};
		});
	}

	/**
	 * @param m
	 * @return	この立候補者の信用。社会的信用と主観信用の合計
	 */
	private int getCredit(Message m) {
		int r = 0;
		Long userId = m.getUserId();
		if (userId == null)
			return 0;
		Sociality s = Glb.getObje().getSociality(
				ss -> ss.getByIndividuality(NodeType.USER, userId));
		if (s == null)
			return 0;
		r += s.credit();

		P2PEdge e = m.getEdgeByInnermostPackage();
		if (e != null)
			r += e.credit();
		return r;
	}

	/**
	 * 立候補者を追加する
	 * @param candidacy	立候補メッセージ
	 * @return	追加されたか
	 */
	public synchronized boolean add(Message candidacy) {
		if (!(candidacy.getContent() instanceof Candidacy)) {
			Glb.getLogger().warn("not Candidacy", new Exception());
			return false;
		}
		if (!addable()) {
			return false;
		}
		if (!candidacyMessages.add(candidacy)) {
			return false;
		}
		previous = candidacy;
		removeByCredit();
		Glb.getLogger().info("Candidacy added.");
		return true;
	}

	/**
	 * @return	新たな立候補者を追加可能か
	 */
	private synchronized boolean addable() {
		if (candidacyMessages.size() > max)
			return false;
		if (previous == null)
			return true;
		MessageContent c = previous.getContent();
		if (!(c instanceof Candidacy)) {
			Glb.getLogger().warn(Lang.ERROR_INVALID);
			return false;
		}
		return true;
		/*
		Candidacy ca = (Candidacy) c;
		long now = Glb.getUtil().getEpochMilli();
		long elapsed = now - ca.getDate();
		return elapsed > addInterval;
		*/
	}

	private synchronized boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (candidacyMessages == null) {
			r.add(Lang.CANDIDACY_LIST, Lang.CANDIDACY_MESSAGES,
					Lang.ERROR_EMPTY);
			b = false;
		}

		//最大件数チェックはしない。変更する可能性があるので

		//Message#validate()は通信における検証であり
		//ここでは永続化における検証をするので適さない
		//例えば通信における検証では期限切れメッセージでfalseになるが
		//永続化では必ずしもfalseにしなくていい
		//そしてMessageの内容が妥当である事の検証は通信時の処理に依存している

		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateCommon(r);
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		//立候補メッセージの中にuserIdがあるが、
		//メッセージクラス側の検証を通じてvalidateCommon()で検証されている
		return true;
	}

	/**
	 * @param userId
	 * @return	このユーザーの立候補メッセージがあるか
	 */
	public synchronized boolean contains(Long userId) {
		if (userId == null)
			return false;
		for (int i = 0; i < candidacyMessages.size(); i++) {
			Message m = candidacyMessages.get(i);
			Long u = m.getUserId();
			if (userId.equals(u)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Note:自分の立候補を削除しても近傍から再度追加されてしまう。
	 * 一定期間が過ぎると全ノードから削除されるし、
	 * 古いメッセージでは登録されないので全ノードから消える。
	 *
	 * @param userId	このユーザーの立候補メッセージを削除する
	 * @return	削除されたか
	 */
	public synchronized boolean remove(Long userId) {
		if (userId == null)
			return false;
		//{@link List#forEach(Consumer)}だと途中で止めるのに例外が必要なので
		for (int i = 0; i < candidacyMessages.size(); i++) {
			Message m = candidacyMessages.get(i);
			Long u = m.getUserId();
			if (userId.equals(u)) {
				candidacyMessages.remove(i);
				if (m == previous)
					previous = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * 自分が立候補する
	 * @return	送信数
	 */
	public int candidacy() {
		Candidacy req = new Candidacy();
		req.setCandidacyDate(Glb.getUtil().getEpochMilli());
		Message m = Message.build(req).packaging(req.createPackage()).finish();
		add(m);
		int r = 0;
		List<P2PEdge> l = Glb.getSubje().getNeighborList().getNeighborRandom(20,
				true);
		if (l == null)
			return r;
		for (P2PEdge to : l) {
			if (Glb.getP2p().sendAsync(m, to))
				r++;
		}
		return r;
	}

	/**
	 * @param m1
	 * @param m2
	 * @return	m1はm2より立候補日時が新しいか
	 */
	private boolean isNew(Message m1, Message m2) {
		Candidacy c1 = (Candidacy) m1.getContent();
		long d1 = c1.getDate();
		Candidacy c2 = (Candidacy) m2.getContent();
		long d2 = c2.getDate();
		return d1 > d2;
	}

	/**
	 * 既存のリストに新しいリストを加える
	 * 同じuserIdがあれば古い方が無視される
	 *
	 * @param add	追加されるリスト
	 * @return	追加された件数。既存のメッセージが削除される場合があるので
	 * 増加件数ではない。
	 */
	public synchronized int update(CandidacyList add) {
		int r = 0;
		if (add == null)
			return r;
		for (Message addM : add.candidacyMessages) {
			if (addM == null)
				continue;
			addM.validateAndSetup();
			Long addUserId = addM.getUserId();
			if (addUserId == null)
				continue;

			//既存のリストにあるか
			Message m = get(addUserId);
			if (m == null) {
				//新規の立候補メッセージなので追加
				if (add(addM))
					r++;
			} else {
				//立候補日時を比較して新しければ置き換え
				if (isNew(addM, m)) {
					if (!candidacyMessages.remove(m)) {
						Glb.getLogger().warn(
								"Failed to remove message of Candidacy. addUserId="
										+ addUserId,
								new Exception());
						continue;
					}
					if (add(addM)) {
						r++;
					} else {
						add(m);
					}
				}
			}
		}
		return r;
	}

	/**
	 * @param userId
	 * @return	このuserIdのユーザーによるメッセージ。なければnull
	 */
	public synchronized Message get(Long userId) {
		if (userId == null)
			return null;
		for (Message m : candidacyMessages) {
			if (m == null)
				continue;
			Long userId1 = m.getUserId();
			if (userId.equals(userId1))
				return m;
		}
		return null;
	}

	/**
	 * 近傍と通信して立候補者リストを取得する。
	 */
	public void updateFromNeighbors() {
		Glb.getLogger().info("CandidacyList update started.");

		CandidacyList l = GetCandidacyList
				.send(Glb.getSubje().getNeighborList());
		if (l == null)
			return;
		Glb.getLogger().info("new CandidacyList " + l.size());
		int updated = update(l);
		Glb.getLogger().info("CandidacyList was updated. " + updated);

		int cleaned = clean();
		Glb.getLogger().info("CandidacyList was cleaned. " + cleaned);
	}

	/**
	 * 期限切れメッセージを削除する
	 * @return	削除件数
	 */
	private synchronized int clean() {
		int r = 0;
		for (int i = 0; i < candidacyMessages.size(); i++) {
			Message m = candidacyMessages.get(i);
			if (m == null)
				continue;
			if (!(m.getContent() instanceof Candidacy))
				continue;
			Candidacy c = (Candidacy) m.getContent();
			if (c.isExpired()) {
				candidacyMessages.remove(i);
				r++;
			}
		}

		r += removeByCredit();

		return r;
	}
}
