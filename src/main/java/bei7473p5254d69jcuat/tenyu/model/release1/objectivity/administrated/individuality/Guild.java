package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class Guild extends IndividualityObject implements GuildI {
	public static final int guildMembersMax = 1000 * 1000;
	private List<GuildMember> guildMembers = new ArrayList<>();

	/**
	 * ギルマス投票
	 *
	 * ギルマス候補：承認したギルドメンバー
	 */
	private Map<Long, HashSet<Long>> candidateToVotes = new HashMap<>();
	public static final int candidateToVotesMax = 100;
	public static final int votesMax = guildMembersMax;

	/**
	 * @param masterUserId
	 * @return	指定されたユーザーをギルマスとして承認したギルドメンバーのセット
	 */
	public HashSet<Long> getVoters(Long masterUserId) {
		return candidateToVotes.get(masterUserId);
	}

	/**
	 * @return	ギルマスを承認したギルドメンバー
	 */
	public HashSet<Long> getVoters() {
		return getVoters(getMainAdministratorUserId());
	}

	/**
	 * @param memberUserId
	 * @return	指定されたユーザーのギルドメンバーとしての情報。なければnull
	 */
	public GuildMember getGuildMember(Long memberUserId) {
		for (GuildMember e : guildMembers) {
			if (e.getUserId().equals(memberUserId))
				return e;
		}
		return null;
	}

	/**
	 * @param candidateUserId	ギルマス候補
	 * @param voterUserId		投票者
	 * @return	投票できたか。すでに投票済みならfalse
	 */
	public boolean vote(Long candidateUserId, Long voterUserId) {
		List<Long> ids = getGuildMemberIds();
		if (!ids.contains(candidateUserId) || !ids.contains(voterUserId)) {
			return false;
		}

		HashSet<Long> votes = candidateToVotes.get(candidateUserId);
		if (votes == null) {
			votes = new HashSet<>();
		}
		if (!votes.add(voterUserId))
			return false;
		candidateToVotes.put(candidateUserId, votes);
		return true;
	}

	/**
	 * @param candidateUserId	ギルマス候補
	 * @param voterUserId		投票を取り消したい人
	 * @return	取り消した発生したか
	 */
	public boolean voteCancel(Long candidateUserId, Long voterUserId) {
		List<Long> ids = getGuildMemberIds();
		if (!ids.contains(candidateUserId) || !ids.contains(voterUserId)) {
			return false;
		}

		HashSet<Long> votes = candidateToVotes.get(candidateUserId);
		if (votes == null) {
			return false;
		}
		if (!votes.remove(voterUserId))
			return false;
		candidateToVotes.put(candidateUserId, votes);
		return true;
	}

	/**
	 * @return	ギルドマスターが脱退可能か
	 */
	public boolean canLeaveGuildMaster() {
		GuildMember oldest = getOldestMemberExceptGuildMaster();
		return oldest != null;
	}

	/**
	 * @param memberId	このユーザーIDのギルドメンバーを削除
	 * @return	削除されたか
	 */
	public boolean delete(Long memberId) {
		GuildMember deleted = null;
		for (int i = 0; i < guildMembers.size(); i++) {
			GuildMember e = guildMembers.get(i);
			if (e.getUserId().equals(memberId)) {
				deleted = e;
				break;
			}
		}
		if (deleted == null)
			return false;

		//投票を削除
		cleanVote(memberId);

		return guildMembers.remove(deleted);
	}

	/**
	 * このユーザーの投票に関する状態をクリアする
	 * @param voterUserId
	 */
	public void cleanVote(Long voterUserId) {
		candidateToVotes.remove(voterUserId);
		for (HashSet<Long> votes : candidateToVotes.values()) {
			votes.remove(voterUserId);
		}
	}

	public Map<Long, HashSet<Long>> getCandidateToVotes() {
		return candidateToVotes;
	}

	public void setCandidateToVotes(Map<Long, HashSet<Long>> candidateToVotes) {
		this.candidateToVotes = candidateToVotes;
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(getMainAdministratorUserId());
		return r;
	}

	@Override
	public IndividualityObjectGui<?, ?, ?, ?, ?, ?> getGuiReferenced(String guiName,
			String cssIdPrefix) {
		return new GuildGui(guiName, cssIdPrefix);
	}

	@Override
	public List<Long> getGuildMemberIds() {
		List<Long> r = new ArrayList<>();
		for (GuildMember e : getGuildMembers()) {
			r.add(e.getUserId());
		}
		return r;
	}

	public List<GuildMember> getGuildMembers() {
		return guildMembers;
	}

	/**
	 * @return	ギルドマスター以外で最も古参のメンバー
	 */
	public GuildMember getOldestMemberExceptGuildMaster() {
		Long masterId = getMainAdministratorUserId();
		GuildMember r = null;
		for (GuildMember e : getGuildMembers()) {
			if (e.getUserId().equals(masterId))
				continue;
			if (r == null) {
				r = e;
				continue;
			}
			if (r.getJoiningDate() > e.getJoiningDate())
				r = e;
		}
		return r;
	}

	@Override
	public IndividualityObjectStore<? extends AdministratedObjectI,
			? extends AdministratedObjectI> getStore(Transaction txn) {
		return new GuildStore(txn);
	}

	@Override
	public StoreName getStoreName() {
		return StoreNameObjectivity.GUILD;
	}

	public void setGuildMembers(List<GuildMember> guildMembers) {
		this.guildMembers = guildMembers;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (GuildMember e : guildMembers) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (GuildMember e : guildMembers) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (guildMembers == null) {
			r.add(Lang.GUILD_MEMBERS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (guildMembers.size() > guildMembersMax) {
				r.add(Lang.GUILD_MEMBERS, Lang.ERROR_TOO_MANY,
						"guildMembers.size()=" + guildMembers.size());
				b = false;
			} else {
				HashSet<Long> filter = new HashSet<>();
				for (GuildMember member : getGuildMembers()) {
					if (filter.contains(member.getUserId())) {
						r.add(Lang.GUILD_MEMBERS, Lang.ERROR_DUPLICATE,
								"member=" + member + " guildMembers="
										+ guildMembers);
						b = false;
						break;
					}
					filter.add(member.getUserId());
				}
			}
		}

		List<Long> memberIds = getGuildMemberIds();
		if (candidateToVotes == null) {
			r.add(Lang.GUILD_CANDIDATE_TO_VOTES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (candidateToVotes.size() > candidateToVotesMax) {
				r.add(Lang.GUILD_CANDIDATE_TO_VOTES, Lang.ERROR_TOO_MANY,
						"size=" + candidateToVotes.size());
				b = false;
			} else {
				for (Long candidateUserId : candidateToVotes.keySet()) {
					if (!Model.validateIdStandard(candidateUserId)
							|| !memberIds.contains(candidateUserId)) {
						r.add(Lang.GUILD, Lang.GUILD_CANDIDATE_USER_ID,
								Lang.ERROR_INVALID,
								"candidateUserId=" + candidateUserId);
						b = false;
						break;
					}
				}

				votes: for (HashSet<Long> votes : candidateToVotes.values()) {
					if (votes.size() > votesMax) {
						r.add(Lang.GUILD, Lang.GUILD_VOTES, Lang.ERROR_TOO_MANY,
								"size=" + votes.size());
						b = false;
						break;
					} else {
						for (Long voterUserId : votes) {
							if (!Model.validateIdStandard(voterUserId)
									|| !memberIds.contains(voterUserId)) {
								r.add(Lang.GUILD, Lang.GUILD_VOTE,
										Lang.ERROR_INVALID,
										"voterUserId=" + voterUserId);
								b = false;
								break votes;
							}
						}
					}
				}
			}
		}

		//ギルドマスターがギルドメンバーに含まれる事。
		if (!getGuildMemberIds().contains(getMainAdministratorUserId())) {
			r.add(Lang.GUILD_MASTER, Lang.ERROR_INVALID,
					"master=" + getMainAdministratorUserId() + " members="
							+ getGuildMemberIds());
			b = false;
		}

		return b;
	}

	@Override
	protected boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		for (GuildMember e : guildMembers) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	/**
	 * ここで貢献度概念を扱うべきか少し考えたが、
	 * それは各ゲーム毎にあるべきで、
	 * ゲームサーバが管理するもの。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class GuildMember implements ValidatableI {
		public static final long memoByGuildMasterMax = 400;

		public static long getMemobyguildmastermax() {
			return memoByGuildMasterMax;
		}

		/**
		 * 加入日時
		 */
		private long joiningDate;
		/**
		 * ギルドマスターによるメモ
		 */
		private String memoByGuildMaster;

		/**
		 * このギルドメンバーのユーザーID
		 */
		private Long userId;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GuildMember other = (GuildMember) obj;
			if (joiningDate != other.joiningDate)
				return false;
			if (memoByGuildMaster == null) {
				if (other.memoByGuildMaster != null)
					return false;
			} else if (!memoByGuildMaster.equals(other.memoByGuildMaster))
				return false;
			if (userId == null) {
				if (other.userId != null)
					return false;
			} else if (!userId.equals(other.userId))
				return false;
			return true;
		}

		public long getJoiningDate() {
			return joiningDate;
		}

		public String getMemoByGuildMaster() {
			return memoByGuildMaster;
		}

		public Long getUserId() {
			return userId;
		}

		/**
		 * @return	ギルド内投票権
		 */
		public long getVotePower() {
			long votePower = 0;

			//加入日時による
			long elapsed = Glb.getUtil().now() - joiningDate;
			long unit = 1000 * 60 * 60 * 24 * 7;
			votePower += elapsed / unit;

			//上限
			long max = 100;
			if (votePower > max)
				votePower = max;

			return votePower;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ (int) (joiningDate ^ (joiningDate >>> 32));
			result = prime * result + ((memoByGuildMaster == null) ? 0
					: memoByGuildMaster.hashCode());
			result = prime * result
					+ ((userId == null) ? 0 : userId.hashCode());
			return result;
		}

		public void setJoiningDate(long joiningDate) {
			this.joiningDate = joiningDate;
		}

		public void setMemoByGuildMaster(String memoByGuildMaster) {
			this.memoByGuildMaster = memoByGuildMaster;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		@Override
		public String toString() {
			return "GuildMember [userId=" + userId + ", joiningDate="
					+ joiningDate + ", memoByGuildMaster=" + memoByGuildMaster
					+ "]";
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			return validateCommon(r);
		}

		@Override
		public final boolean validateAtUpdate(ValidationResult r) {
			return validateCommon(r);
		}

		@Override
		public boolean validateAtUpdateChange(ValidationResult r, Object old) {
			if (!(old instanceof GuildMember)) {
				r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
						"old.class=" + old.getClass().getSimpleName());
				return false;
			}
			GuildMember o = (GuildMember) old;

			boolean b = true;
			if (!o.getUserId().equals(this.getUserId())) {
				r.add(Lang.USER_ID, Lang.ERROR_UNALTERABLE,
						"userId=" + userId + " old.userId=" + o.getUserId());
				b = false;
			}

			if (o.getJoiningDate() != getJoiningDate()) {
				r.add(Lang.JOINING_DATE, Lang.ERROR_UNALTERABLE,
						"joiningDate=" + joiningDate + " old.joiningDate="
								+ o.getJoiningDate());
				b = false;
			}

			return b;
		}

		private boolean validateCommon(ValidationResult r) {
			boolean b = true;
			if (!Model.validateIdStandard(userId)) {
				r.add(Lang.USER_ID, Lang.ERROR_INVALID,
						"GuildMember.userId=" + userId);
				b = false;
			}
			if (joiningDate <= 0) {
				r.add(Lang.JOINING_DATE, Lang.ERROR_INVALID,
						"joiningDate=" + joiningDate);
				b = false;
			}
			if (memoByGuildMaster != null) {
				if (memoByGuildMaster.length() > memoByGuildMasterMax) {
					r.add(Lang.MEMO_BY_GUILD_MASTER, Lang.ERROR_TOO_LONG,
							"memoByGuildMaster.length="
									+ memoByGuildMaster.length());
					b = false;
				}
			}

			return b;
		}

		@Override
		public final boolean validateReference(ValidationResult r,
				Transaction txn) throws Exception {
			boolean b = true;
			UserStore us = new UserStore(txn);
			if (us.get(userId) == null) {
				r.add(Lang.GUILD_MEMBER, Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"userId=" + userId);
				b = false;
			}
			return b;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((guildMembers == null) ? 0 : guildMembers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Guild other = (Guild) obj;
		if (guildMembers == null) {
			if (other.guildMembers != null)
				return false;
		} else if (!guildMembers.equals(other.guildMembers))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Guild [guildMembers=" + guildMembers + "]";
	}

}
