package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * 議題
 * 投票権を持つ人が全体運営者に限られ、その影響度割合によって投票権が差別化される。
 * 投票対象はP2Pネットワーク全体に影響する何らかの重要な処理をするかしないかの決定。
 * 多くの場合、可決によって自動的に何らかの処理が発生する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Agenda extends Naturality implements AgendaDBI {

	/**
	 * 議題の内容。
	 * 議題が可決された場合にどのような処理をするかを定義する。
	 */
	private AgendaContentI content = null;

	/**
	 * 議題登録日時
	 * 登録者が設定
	 */
	private long createDate;

	/**
	 * 投票受付が締め切られるヒストリーインデックス
	 */
	private long endHistoryIndex;

	/**
	 * 反対票がこの割合に達したら賛成票の割合に拠らず優先して否決
	 */
	private double falseThreshold = 0;

	/**
	 * 登録時ヒストリーインデックス
	 */
	private long startHistoryIndex;

	/**
	 * 議題の経過、または段階
	 */
	private AgendaStatus status = AgendaStatus.START;

	/**
	 * 賛成票がこの割合に達したら可決
	 */
	private double trueThreshold = 0;

	/**
	 * 全体運営者による投票一覧
	 * ユーザーID:真偽
	 */
	private Map<Long, Boolean> votes = new HashMap<>();

	public synchronized void addVote(Long userId, Boolean vote) {
		votes.put(userId, vote);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return Glb.getObje().getCore().getManagerList().getManagerIds();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//削除は考えられない
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministratorUserIdCreate();
	}

	public AgendaContentI getContent() {
		return content;
	}

	public long getCreateDate() {
		return createDate;
	}

	/**
	 * 全体運営者の影響度割合によって投票権を差別化し、集計する
	 * @return	現時点の集計
	 */
	public VoteResult getCurrentVoteResult() {
		VoteResult r = new VoteResult();
		for (Entry<Long, Boolean> e : votes.entrySet()) {
			try {
				Double power = Glb.getObje().getCore().getManagerList()
						.getManagerPower(e.getKey());
				if (power == null || power <= 0)
					continue;
				if (e.getValue()) {
					r.addTrueTotal(power);
				} else {
					r.addFalseTotal(power);
				}
			} catch (Exception ex) {
				Glb.getLogger().error("", ex);
			}
		}
		return r;
	}

	public long getEndHistoryIndex() {
		return endHistoryIndex;
	}

	public double getFalseThreshold() {
		return falseThreshold;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectDBI.getVoteId();
	}

	public long getStartHistoryIndex() {
		return startHistoryIndex;
	}

	public AgendaStatus getStatus() {
		return status;
	}

	public double getTrueThreshold() {
		return trueThreshold;
	}

	public synchronized Map<Long, Boolean> getVotes() {
		return Collections.unmodifiableMap(votes);
	}

	/**
	 * @return	可決され実行準備が開始されたか
	 */
	public boolean isAccepted() {
		return status == AgendaStatus.ACCEPTED;
	}

	/**
	 * @return	現在の投票状況で可決または否決できるか
	 */
	public boolean isOverThreshold() {
		VoteResult vr = getCurrentVoteResult();

		if (vr.getFalseTotal() >= falseThreshold) {
			return true;
		}

		if (vr.getTrueTotal() >= trueThreshold) {
			return true;
		}

		//まだ可決も否決もできない
		return false;
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;//登録は全体運営者ならだれでもできるが更新だけ議決が必要
	}

	/**
	 * @return	現在の集計結果において可決か
	 */
	public boolean isTrue() {
		VoteResult vr = getCurrentVoteResult();

		if (vr.getFalseTotal() >= falseThreshold) {
			return false;
		}

		if (vr.getTrueTotal() >= trueThreshold) {
			return true;
		}

		return false;
	}

	public void setContent(AgendaContentI content) {
		this.content = content;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setEndHistoryIndex(long endHistoryIndex) {
		this.endHistoryIndex = endHistoryIndex;
	}

	public void setFalseThreshold(double falseThreshold) {
		this.falseThreshold = falseThreshold;
	}

	public void setStartHistoryIndex(long startHistoryIndex) {
		this.startHistoryIndex = startHistoryIndex;
	}

	public void setStatus(AgendaStatus status) {
		this.status = status;
	}

	public void setTrueThreshold(double trueThreshold) {
		this.trueThreshold = trueThreshold;
	}

	public void setVotes(Map<Long, Boolean> votes) {
		this.votes = votes;
	}

	public boolean validate() {
		return false;
	}

	protected boolean validateAtCommonNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (votes == null) {
			r.add(Lang.AGENDA_VOTES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (votes.size() > TenyuManagerList.managerMax) {
				r.add(Lang.AGENDA_VOTES, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				if (!IdObject.validateIdStandardNotSpecialId(votes.keySet())) {
					r.add(Lang.AGENDA_VOTES, Lang.ERROR_INVALID,
							Lang.IDOBJECT_RECYCLE_ID.toString());
					b = false;
				}
				for (Boolean e : votes.values()) {
					if (e == null) {
						r.add(Lang.AGENDA_VOTES, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
		}
		if (status == null) {
			r.add(Lang.AGENDA_STATUS, Lang.ERROR_EMPTY);
			b = false;
		}
		if (content == null) {
			r.add(Lang.AGENDA_CONTENT, Lang.ERROR_EMPTY);
			b = false;
		}
		if (trueThreshold < 0) {
			r.add(Lang.AGENDA_TRUE_THRESHOLD, Lang.ERROR_INVALID,
					"trueThreshold=" + trueThreshold);
			b = false;
		}
		if (falseThreshold < 0) {
			r.add(Lang.AGENDA_FALSE_THRESHOLD, Lang.ERROR_INVALID,
					"falseThreshold=" + falseThreshold);
			b = false;
		}
		if (endHistoryIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.AGENDA_END_HISTORYINDEX, Lang.ERROR_INVALID,
					"endHistoryIndex=" + endHistoryIndex);
			b = false;
		}
		if (startHistoryIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.AGENDA_START_HISTORYINDEX, Lang.ERROR_INVALID,
					"startHistoryIndex=" + startHistoryIndex);
			b = false;
		}
		if (createDate <= 0) {
			r.add(Lang.AGENDA_CREATEDATE, Lang.ERROR_INVALID,
					"createDate=" + createDate);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (validateAtCommonNaturalityConcrete(r)) {
			if (votes.size() != 0) {
				r.add(Lang.AGENDA_VOTES, Lang.ERROR_NOT_DEFAULT);
				b = false;
			}
			if (status != AgendaStatus.START) {
				r.add(Lang.AGENDA_STATUS, Lang.ERROR_NOT_DEFAULT);
				b = false;
			}
		}
		if (content != null) {
			if (!content.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Agenda)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Agenda old2 = (Agenda) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getContent(), old2.getContent())) {
			r.add(Lang.AGENDA_CONTENT, Lang.ERROR_UNALTERABLE, "content="
					+ getContent() + " old2Content=" + old2.getContent());
			b = false;
		}
		if (Glb.getUtil().notEqual(getTrueThreshold(),
				old2.getTrueThreshold())) {
			r.add(Lang.AGENDA_TRUE_THRESHOLD, Lang.ERROR_UNALTERABLE,
					"trueThreshold2=" + getTrueThreshold()
							+ " old2TrueThreshold2=" + old2.getTrueThreshold());
			b = false;
		}
		if (Glb.getUtil().notEqual(getFalseThreshold(),
				old2.getFalseThreshold())) {
			r.add(Lang.AGENDA_FALSE_THRESHOLD, Lang.ERROR_UNALTERABLE,
					"falseThreshold2=" + getFalseThreshold()
							+ " old2FalseThreshold2="
							+ old2.getFalseThreshold());
			b = false;
		}
		if (Glb.getUtil().notEqual(getEndHistoryIndex(),
				old2.getEndHistoryIndex())) {
			r.add(Lang.AGENDA_END_HISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"endHistoryIndex=" + getEndHistoryIndex()
							+ " old2EndHistoryIndex="
							+ old2.getEndHistoryIndex());
			b = false;
		}
		if (Glb.getUtil().notEqual(getStartHistoryIndex(),
				old2.getStartHistoryIndex())) {
			r.add(Lang.AGENDA_START_HISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"startHistoryIndex=" + getStartHistoryIndex()
							+ " old2StartHistoryIndex="
							+ old2.getStartHistoryIndex());
			b = false;
		}
		if (Glb.getUtil().notEqual(getCreateDate(), old2.getCreateDate())) {
			r.add(Lang.AGENDA_CREATEDATE, Lang.ERROR_UNALTERABLE,
					"createDate=" + getCreateDate() + " old2CreateDate="
							+ old2.getCreateDate());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateNaturalityConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r))
			b = false;
		if (content != null) {
			if (!content.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		UserStore us = new UserStore(txn);
		for (Long id : votes.keySet()) {
			if (us.get(id) == null) {
				r.add(Lang.AGENDA_VOTES, Lang.ERROR_DB_NOTFOUND_REFERENCE);
				b = false;
				break;
			}
		}

		if (content != null) {
			if (!content.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}

	/**
	 * 賛成と反対の割合を集計する
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class VoteResult {
		private double falseTotal = 0;
		private double trueTotal = 0;

		public void addFalseTotal(double add) {
			this.falseTotal += add;
		}

		public void addTrueTotal(double add) {
			this.trueTotal += add;
		}

		public double getFalseTotal() {
			return falseTotal;
		}

		public double getTrueTotal() {
			return trueTotal;
		}

		public void setFalseTotal(double falseTotal) {
			this.falseTotal = falseTotal;
		}

		public void setTrueTotal(double trueTotal) {
			this.trueTotal = trueTotal;
		}
	}

}