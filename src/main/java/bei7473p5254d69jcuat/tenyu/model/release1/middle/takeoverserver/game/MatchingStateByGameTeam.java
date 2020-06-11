package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.MatchingStateByGameTeam.TeamCount.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.Team.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import glb.*;
import glb.util.*;

public class MatchingStateByGameTeam extends AbstractMatchingStateByGame {
	/**
	 * メンバーを受付中のチーム
	 * チームクラスID：チームクラス毎のメンバーを受付中のチーム一覧
	 */
	private Teams acceptingTeams = new Teams();
	/**
	 * メンバーが揃ったチーム
	 * チームクラスID：チームクラス毎のメンバーが揃ったチーム一覧
	 */
	private Teams fullTeams = new Teams();

	public boolean addMember(TeamReference tref, NodeIdentifierUser node,
			byte[] passwordHash) {
		if (node == null)
			return false;
		Team t = get(tref);
		if (t == null)
			return false;

		if (!isSupportTimezone(node.getUserId()))
			return false;

		if (!t.addMember(node, passwordHash))
			return false;

		return true;
	}

	/**
	 * チーム作成者が特定のメンバーをチームから追放する
	 * @param ref		どのチームか
	 * @param requestor	追放要求している人
	 * @param target	追放される人
	 * @return	追放に成功したか
	 */
	public boolean explusion(TeamReference ref, NodeIdentifierUser requestor,
			NodeIdentifierUser target) {
		if (ref == null || !ref.validate() || requestor == null
				|| target == null)
			return false;
		MatchingStateByGameTeam state = this;
		if (state == null || state.getAcceptingTeams() == null
				|| state.getAcceptingTeams().getTeams() == null)
			return false;
		TeamsByTeamClassId ts = state.getAcceptingTeams().getTeams()
				.get(ref.getTeamClassId());
		if (ts == null || ts.getTeams() == null)
			return false;
		Team t = ts.getTeams().get(ref.getName());
		if (t == null)
			return false;
		if (t.getAdmin() == null || t.getAdmin().equals(requestor)) {
			return false;
		}
		if (t.getMembers() == null || !t.getMembers().contains(target)) {
			return false;
		}

		return t.getMembers().remove(target);
	}

	/**
	 * メンバー募集を打ち切り、マッチング申請する
	 * @param ref
	 * @param requestor
	 * @return
	 */
	public boolean finish(TeamReference ref, NodeIdentifierUser requestor) {
		if (ref == null || !ref.validate() || requestor == null)
			return false;
		MatchingStateByGameTeam state = this;
		if (state == null || state.getAcceptingTeams() == null
				|| state.getAcceptingTeams().getTeams() == null)
			return false;
		TeamsByTeamClassId ts = state.getAcceptingTeams().getTeams()
				.get(ref.getTeamClassId());
		if (ts == null || ts.getTeams() == null)
			return false;
		Team t = ts.getTeams().get(ref.getName());
		if (t == null)
			return false;
		if (t.getAdmin() == null || !t.getAdmin().equals(requestor)) {
			return false;
		}
		if (!t.finish())
			return false;

		//fullに移す
		if (!removeTeam(t)) {
			Glb.getLogger().warn("Failed to remove finished team",
					new Exception());
		}
		return addTeamToFull(t);
	}

	public boolean addTeamToAccepting(Team t) {
		if (t == null)
			return false;
		ValidationResult vr = new ValidationResult();
		if (!t.validateAtCreate(vr)) {
			Glb.getLogger().warn("Failed to add by validation. " + vr,
					new Exception());
			return false;
		}
		if (t.getState() != TeamState.ACCEPTING)
			return false;

		return acceptingTeams.addTeam(t);
	}

	private boolean addTeamToFull(Team t) {
		if (t == null)
			return false;
		if (t.getState() != TeamState.CLOSED)
			return false;

		return fullTeams.addTeam(t);
	}

	/**
	 * acceptingかfullどちらかからgetする
	 * @param tref
	 * @return	trefで指定されたTeamあるいはnull
	 */
	public Team get(TeamReference tref) {
		Team exist = null;
		TeamsByTeamClassId teams = acceptingTeams.getTeams()
				.get(tref.getTeamClassId());
		if (teams != null) {
			exist = teams.getTeams().get(tref.getName());
		}
		if (exist == null) {
			teams = fullTeams.getTeams().get(tref.getTeamClassId());
			if (teams != null) {
				exist = teams.getTeams().get(tref.getName());
			}
		}
		return exist;
	}

	public Teams getAcceptingTeams() {
		return acceptingTeams;
	}

	/**
	 * @param m
	 * @return		mをチームクラスID:チーム数という形式に変える
	 */
	private TeamCount getCounts(Map<Integer, TeamsByTeamClassId> m) {
		TeamCount counts = new TeamCount();
		for (Entry<Integer, TeamsByTeamClassId> e : m.entrySet()) {
			TeamCountEntry count = new TeamCountEntry();
			count.setGameId(gameId);
			count.setCount(e.getValue().getTeams().size());
			count.setTeamClassId(e.getKey());
			counts.getTeamClassIdToCount().put(e.getKey(), count);
		}
		return counts;
	}

	/**
	 * @return	チームクラス別のメンバーが揃ったチームの数
	 */
	public TeamCount getFullTeamCountsByTeamClass() {
		return getCounts(fullTeams.getTeams());
	}

	public List<Team> getRandomFullTeam(int n) {
		//大量取得を想定していない
		if (n > 1000)
			return null;
		List<Team> r = new ArrayList<>();
		int tryMax = n * 2;
		for (int tryCount = 0; tryCount < tryMax; tryCount++) {
			try {
				Map<Integer, TeamsByTeamClassId> teams = fullTeams.getTeams();
				TeamsByTeamClassId byC = teams
						.get(Glb.getRnd().nextInt(teams.size()));
				if (byC == null)
					continue;

				//この取得処理は処理中にレコードが削除される可能性があり
				//keyを取得できてもtがnullになる可能性等がある
				List<String> keyList = byC.getKeyList();
				String key = keyList.get(Glb.getRnd().nextInt(keyList.size()));
				if (key == null)
					continue;
				Team t = byC.getTeams().get(key);
				r.add(t);
				//必要件数に達したら終わり
				if (r.size() == n) {
					return r;
				}
			} catch (Exception e) {
				Glb.debug(e);
				continue;
			}
		}
		//試行回数が上限に達したら終わり
		return r;
	}

	@Override
	public long getThreshold() {
		return 10;
	}

	/**
	 * このメソッドはマルチスレッドでアクセスされる事を想定していない
	 * @return
	 */
	public List<RatingGameMatch> matching() {
		game = Glb.getObje().getRatingGame(s -> s.get(gameId));

		if (fullTeams == null) {
			Glb.getLogger().error("fullTeams is null",
					new IllegalStateException());
			return null;
		}

		if (fullTeams.getTeams().size() == 0) {
			Glb.debug("fullTeams.size=" + fullTeams.size());
			return null;
		}
		//各チームクラスIDのフルメンバーのチーム数のうち最小のもの
		TeamsByTeamClassId minByClass = fullTeams.getMin();
		List<RatingGameMatch> matched = matching(fullTeams,
				minByClass.getDates().pace(), minByClass.size(),
				MatchingType.TEAM);
		if (matched == null)
			return matched;
		Glb.debug("matched.size=" + matched.size());
		for (RatingGameMatch match : matched) {
			//マッチング待ちリストから削除
			for (Team t : match.getMatchedTeams()) {
				if (!fullTeams.remove(t)) {
					Glb.getLogger().warn("Failed to remove team=" + t.getName(),
							new Exception());
				}
			}
		}
		return matched;
	}

	public boolean removeMember(TeamReference tref, NodeIdentifierUser node) {
		try {
			Team exist = get(tref);
			if (exist == null)
				return false;
			if (!exist.removeMember(node))
				return false;
			//メンバーが0になったら削除
			if (exist.getMembers().size() == 0) {
				removeTeam(exist);
			}
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	/**
	 * 削除元のmapを特定できない場合に使用する
	 * 両方から削除を試みる
	 * @param t	削除対象のTeam
	 * @return	1つ以上のmapから削除されたらtrue,両方から削除されなかったらfalse
	 */
	private boolean removeTeam(Team t) {
		if (t == null || t.getName() == null)
			return false;
		boolean b = false;
		if (acceptingTeams.remove(t))
			b = true;
		if (fullTeams.remove(t))
			b = true;
		return b;
	}

	/**
	 * チームクラスID別の総数をまとめる
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class TeamCount {
		private Map<Integer,
				TeamCountEntry> teamClassIdToCount = new HashMap<>();

		public Integer getMin() {
			int min = 0;
			for (Entry<Integer, TeamCountEntry> e : teamClassIdToCount
					.entrySet()) {
				if (min > e.getValue().getCount())
					min = e.getValue().getCount();
			}
			return min;
		}

		public Map<Integer, TeamCountEntry> getTeamClassIdToCount() {
			return teamClassIdToCount;
		}

		public static class TeamCountEntry {
			private Integer count;
			private Long gameId;
			private Integer teamClassId;

			public RatingGame getGame() {
				return Glb.getObje().getRatingGame(s -> s.get(gameId));
			}

			public Integer getCount() {
				return count;
			}

			public Integer getTeamClassId() {
				return teamClassId;
			}

			public void setCount(Integer count) {
				this.count = count;
			}

			public void setTeamClassId(Integer teamClassId) {
				this.teamClassId = teamClassId;
			}

			public Long getGameId() {
				return gameId;
			}

			public void setGameId(Long gameId) {
				this.gameId = gameId;
			}
		}
	}

}