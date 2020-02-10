package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.Team.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * マッチングサーバが作成する試合の参加者等の情報。
 * 結託等の不正行為を防ぐためマッチングサーバによるランダムマッチ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameMatch extends AdministratedObject
		implements RatingGameMatchDBI, Unreferenciable {

	public static NodeIdentifierUser getRndPlayer(List<Team> teams) {
		List<NodeIdentifierUser> players = new ArrayList<>();
		for (Team t : teams) {
			players.addAll(t.getMembers());
		}
		if (players.size() == 0)
			return null;
		return players.get(Glb.getRnd().nextInt(players.size()));
	}

	public static final int matchedTeamsMax = 1000;

	/**
	 * レーティングへの反映処理を受けたか。
	 * 反映処理の中で反映が行われなかったとしてもtrueを設定する。
	 *
	 * DBアクセスを減らしたいので無くした
	 */
	//private boolean applyProcessed = false;

	public static final int reportsMax = 1000;

	/**
	 * プレイヤーによって報告が食い違っているか
	 */
	private boolean conflict = false;

	/**
	 * 作成日時
	 * マッチングサーバが設定するのでミリ秒指定可能
	 */
	private long createDate;

	/**
	 * 十分にランダム性のあるマッチングだったか
	 * falseならリプレイファイルを提出させるなどチート対策の必要性が高まる
	 */
	private boolean enoughRandom = true;

	/**
	 * 対戦するチーム一覧
	 * teamIdはこれのインデックス
	 */
	private List<Team> matchedTeams;

	/**
	 * レーティングゲームのID
	 */
	private Long ratingGameId;

	/**
	 * この試合に参加したプレイヤーの情報
	 * 試合後に設定されるので最初は空
	 * 途中で試合を抜けたプレイヤーも途中までの情報を報告するので
	 * 1つ1つの報告は試合結果について完全な情報を持たない
	 */
	private List<RatingGameMatchReport> reports = new ArrayList<>();

	private MatchingType type;

	/**
	 * ホスト役
	 * マッチングサーバがランダムに選択する
	 */
	private NodeIdentifierUser host;

	public boolean addReport(RatingGameMatchReport report) {
		return reports.add(report);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<>();
		r.addAll(Glb.getObje().getRole(
				rs -> rs.getByName(RatingGameMatch.class.getSimpleName()))
				.getAdminUserIds());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//削除は想定されない。しかし削除されないことを前提にできるわけでもない
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//一定の手続きに応じて行われる
	}

	public int getCountOfFinishReport() {
		int r = 0;
		for (RatingGameMatchReport repo : reports) {
			if (repo.isFinished())
				r++;
		}
		return r;
	}

	public long getSubmitDate() {
		return createDate;
	}

	public List<Team> getMatchedTeams() {
		return matchedTeams;
	}

	public Team getMatchedTeam(int teamId) {
		return matchedTeams.get(teamId);
	}

	/**
	 * @return	全プレイヤーUserIdのHashSet
	 */
	public HashSet<Long> getPlayers() {
		HashSet<Long> r = new HashSet<>();
		if (matchedTeams != null) {
			for (Team t : matchedTeams) {
				r.addAll(t.getMemberUserIds());
			}
		}
		return r;
	}

	public HashSet<NodeIdentifierUser> getPlayerNodes() {
		HashSet<NodeIdentifierUser> r = new HashSet<>();
		if (matchedTeams != null) {
			for (Team t : matchedTeams) {
				r.addAll(t.getMembers());
			}
		}
		return r;
	}

	public Long getRatingGameId() {
		return ratingGameId;
	}

	/**
	 * @param userId
	 * @return	このプレイヤーの報告
	 */
	public RatingGameMatchReport getReport(Long userId) {
		for (RatingGameMatchReport repo : reports) {
			if (userId.equals(repo.getPlayerUserId())) {
				return repo;
			}
		}
		return null;
	}

	public List<RatingGameMatchReport> getReports() {
		return reports;
	}

	/*
		public boolean isApplyProcessed() {
			return applyProcessed;
		}
	*/
	public boolean isConflict() {
		return conflict;
	}

	public boolean isEnoughRandom() {
		return enoughRandom;
	}

	/**
	 * @return	試合が終わったか
	 * trueならレーティングに反映可能であることを意味する
	 * レーティングゲームに設定された1試合の最長時間に
	 * 少し猶予を入れて判定する。
	 */
	public boolean isFinished() {
		RatingGame g = Glb.getObje()
				.getRatingGame(rgs -> rgs.get(ratingGameId));
		//猶予
		long tolerance = 1000L * 60 * 10;
		//最長試合時間
		long matchTime = g.getLongestMatchTimeSeconds() * 1000 + tolerance;
		//試合開始からの経過時間
		long elapsed = System.currentTimeMillis() - createDate;
		if (matchTime < elapsed)
			return true;
		return false;
	}

	/**
	 * @param userId
	 * @return	このプレイヤーは既に試合を報告したか
	 */
	public boolean isReported(NodeIdentifierUser userId) {
		if (userId == null)
			return false;
		for (RatingGameMatchReport repo : reports) {
			if (userId.equals(repo.getPlayerUserId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return 全プレイヤーが報告したか
	 */
	public boolean isReportedAll() {
		if (matchedTeams == null)
			return false;
		for (NodeIdentifierUser player : getPlayerNodes()) {
			if (!isReported(player)) {
				return false;
			}
		}
		return true;
	}

	/*
		public void setApplyProcessed(boolean applyProcessed) {
			this.applyProcessed = applyProcessed;
		}
	*/
	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}

	public void setSubmitDate(long createDate) {
		this.createDate = createDate;
	}

	public void setEnoughRandom(boolean enoughRandom) {
		this.enoughRandom = enoughRandom;
	}

	public void setMatchedTeams(List<Team> matchedTeams) {
		this.matchedTeams = matchedTeams;
	}

	public void setRatingGameId(Long ratingGameId) {
		this.ratingGameId = ratingGameId;
	}

	public void setReports(List<RatingGameMatchReport> reports) {
		this.reports = reports;
	}

	protected boolean validateAtCommonAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (ratingGameId == null) {
			r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(ratingGameId)) {
				r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (matchedTeams == null || matchedTeams.size() == 0) {
			r.add(Lang.RATINGGAME_MATCH_MATCHEDTEAMS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (matchedTeams.size() > matchedTeamsMax) {
				r.add(Lang.RATINGGAME_MATCH_MATCHEDTEAMS, Lang.ERROR_TOO_MANY,
						"matchedTeams.size=" + matchedTeams.size());
				b = false;
			}
		}
		if (reports == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORTS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (reports.size() > reportsMax) {
				r.add(Lang.RATINGGAME_MATCH_REPORTS, Lang.ERROR_TOO_MANY,
						"reports.size=" + reports.size());
				b = false;
			}
		}
		if (createDate < 0) {
			r.add(Lang.RATINGGAME_MATCH_CREATEDATE, Lang.ERROR_INVALID);
			b = false;
		}

		if (type == null) {
			r.add(Lang.RATINGGAME_MATCH_TYPE, Lang.ERROR_EMPTY);
			b = false;
		}

		if (host == null) {
			r.add(Lang.RATINGGAME_MATCH_HOST, Lang.ERROR_EMPTY);
			b = false;
		} else {
			boolean contain = false;
			for (Team t : matchedTeams) {
				List<NodeIdentifierUser> players = t.getMembers();
				if (players == null)
					continue;
				if (players.contains(host)) {
					contain = true;
					break;
				}
			}
			if (!contain) {
				r.add(Lang.RATINGGAME_MATCH_HOST, Lang.ERROR_INVALID);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAdministratedObjectConcrete(r)) {
			b = false;
		}
		/*
		if (applyProcessed) {
			r.add(Lang.RATINGGAME_MATCH_APPLYPROCESSED, Lang.ERROR_INVALID);
		}
		*/
		if (conflict) {
			r.add(Lang.RATINGGAME_MATCH_CONFLICT, Lang.ERROR_INVALID);
			b = false;
		}
		if (matchedTeams != null) {
			for (Team t : matchedTeams) {
				if (!t.validateAtCreate(r)) {
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof RatingGameMatch)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		RatingGameMatch old2 = (RatingGameMatch) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getSubmitDate(), old2.getSubmitDate())) {
			r.add(Lang.RATINGGAME_MATCH_CREATEDATE, Lang.ERROR_UNALTERABLE,
					"createDate=" + getSubmitDate() + " oldCreateDate="
							+ old2.getSubmitDate());
			b = false;
		}
		if (Glb.getUtil().notEqual(isEnoughRandom(), old2.isEnoughRandom())) {
			r.add(Lang.RATINGGAME_MATCH_ENOUGHRANDOM, Lang.ERROR_UNALTERABLE,
					"enoughRandom=" + isEnoughRandom() + " oldEnoughRandom="
							+ old2.isEnoughRandom());
			b = false;
		}
		if (Glb.getUtil().notEqual(getRatingGameId(), old2.getRatingGameId())) {
			r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID, Lang.ERROR_UNALTERABLE,
					"ratingGameId=" + getRatingGameId() + " oldRatingGameId="
							+ old2.getRatingGameId());
			b = false;
		}

		if (Glb.getUtil().notEqual(getMatchedTeams(), old2.getMatchedTeams())) {
			r.add(Lang.RATINGGAME_MATCH_MATCHEDTEAMS, Lang.ERROR_UNALTERABLE,
					toString());
			b = false;
		}

		return b;
	}

	@Override
	public String toString() {
		return "RatingGameMatch [conflict=" + conflict + ", createDate="
				+ createDate + ", enoughRandom=" + enoughRandom
				+ ", matchedTeams=" + matchedTeams + ", ratingGameId="
				+ ratingGameId + ", reports=" + reports + ", type=" + type
				+ ", host=" + host + "] " + "id=" + getId()
				+ " createDate=" + getSubmitDate() + " enoughRandom="
				+ isEnoughRandom() + " ratingGameId=" + getRatingGameId();
	}

	@Override
	protected boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;

		if (!validateAtCommonAdministratedObjectConcrete(r))
			b = false;
		if (reports != null) {
			for (RatingGameMatchReport p : reports) {
				if (p.validateAtUpdate(r)) {
					if (!p.dbValidate(r))
						b = false;
				}
			}
		}
		if (matchedTeams != null) {
			for (Team t : matchedTeams) {
				if (!t.validateAtUpdate(r)) {
					b = false;
				}
			}
		}

		return b;
	}

	@Override
	public boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (new RatingGameStore(txn).get(ratingGameId) == null) {
			r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		//RatingGameMatchStore rgms = new RatingGameMatchStore(txn);
		//UserStore us = new UserStore(txn);
		for (RatingGameMatchReport p : reports) {
			if (!p.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		if (matchedTeams != null) {
			for (Team t : matchedTeams) {
				if (!t.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	/**
	 * 各プレイヤーのレーティング変動量を計算する。
	 * 試合が終わり報告が揃ってから呼ぶ
	 * @return	userId : ratingChange
	 */
	public Map<Long, Integer> calculateRatingChange() {
		Map<Long, Integer> empty = new HashMap<>();
		Map<Integer, HashSet<Integer>> ranking;
		try {
			conflict = RatingGameMatchReport.isConflict(this, reports);
			ranking = RatingGameMatchReport.getFullRanking(reports);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return empty;
		}
		if (ranking == null || ranking.size() == 0 || conflict)
			return empty;

		Map<Integer, HashSet<Team>> teamRanking = new HashMap<>();
		for (Entry<Integer, HashSet<Integer>> e : ranking.entrySet()) {
			HashSet<Team> v = new HashSet<>();
			for (Integer teamId : e.getValue()) {
				Team t = getMatchedTeam(teamId);
				if (t == null) {
					Glb.getLogger().warn("", new Exception());
					continue;
				}
				v.add(t);
			}
			teamRanking.put(e.getKey(), v);
		}

		return Glb.getSme2().sme2(teamRanking);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (conflict ? 1231 : 1237);
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result + (enoughRandom ? 1231 : 1237);
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result
				+ ((matchedTeams == null) ? 0 : matchedTeams.hashCode());
		result = prime * result
				+ ((ratingGameId == null) ? 0 : ratingGameId.hashCode());
		result = prime * result + ((reports == null) ? 0 : reports.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		RatingGameMatch other = (RatingGameMatch) obj;
		if (conflict != other.conflict)
			return false;
		if (createDate != other.createDate)
			return false;
		if (enoughRandom != other.enoughRandom)
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (matchedTeams == null) {
			if (other.matchedTeams != null)
				return false;
		} else if (!matchedTeams.equals(other.matchedTeams))
			return false;
		if (ratingGameId == null) {
			if (other.ratingGameId != null)
				return false;
		} else if (!ratingGameId.equals(other.ratingGameId))
			return false;
		if (reports == null) {
			if (other.reports != null)
				return false;
		} else if (!reports.equals(other.reports))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public NodeIdentifierUser getHost() {
		return host;
	}

	public void setHost(NodeIdentifierUser host) {
		this.host = host;
	}

	public MatchingType getType() {
		return type;
	}

	public void setType(MatchingType type) {
		this.type = type;
	}

	@Override
	public RatingGameMatchGui getGui(String guiName, String cssIdPrefix) {
		return new RatingGameMatchGui(guiName, cssIdPrefix);
	}

	@Override
	public RatingGameMatchStore getStore(Transaction txn) {
		return new RatingGameMatchStore(txn);
	}
	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.RATING_GAME_MATCH;
	}

}
