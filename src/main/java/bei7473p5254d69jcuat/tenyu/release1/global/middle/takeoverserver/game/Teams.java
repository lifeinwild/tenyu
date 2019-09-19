package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;

public class Teams {
	/**
	 * teamClassId : teamList
	 */
	private Map<Integer, TeamsByTeamClassId> teams = new ConcurrentHashMap<>();

	/**
	 * チームを追加する。
	 *
	 * 複数の要素においてチームを一意にしなければならないのでsynchronized
	 *
	 * @param t
	 * @return
	 */
	public synchronized boolean addTeam(Team t) {
		ValidationResult vr = new ValidationResult();
		if (t == null || !t.validateAtCreate(vr)
				|| getTeams().size() > RatingGame.teamMax) {
			Glb.getLogger().warn(vr.toString(), new Exception());
			return false;
		}

		if (!isUsableName(t)) {
			return false;
		}
		TeamsByTeamClassId teamsByClass = getOrCreate(t.getTeamClassId());
		teamsByClass.getTeams().put(t.getName(), t);

		//書き込みに成功したならチーム追加ペースを更新する
		teamsByClass.getDates().add();

		return true;
	}

	public boolean isUsableName(Team t) {
		for (TeamsByTeamClassId byclass : teams.values()) {
			if (byclass.getTeams().containsKey(t.getName())) {
				Glb.getLogger().warn(new Exception("duplicate team name"));
				return false;
			}
		}
		return true;
	}

	public boolean remove(Team t) {
		if (t == null)
			return false;
		TeamsByTeamClassId byClass = teams.get(t.getTeamClassId());
		if (byClass == null)
			return false;
		return byClass.remove(t);
	}

	/**
	 * @return	全チーム数
	 */
	public long size() {
		long r = 0;
		for (Entry<Integer, TeamsByTeamClassId> e : getTeams().entrySet()) {
			r += e.getValue().size();
		}
		return r;
	}

	/**
	 * @return	チームクラス別チーム一覧のうち最小のチーム数を持つチーム一覧
	 * この返値のチーム数はこのTeamsでマッチング可能な試合数を意味する。
	 */
	protected TeamsByTeamClassId getMin() {
		int min = 0;
		TeamsByTeamClassId r = null;
		for (Entry<Integer, TeamsByTeamClassId> e : getTeams().entrySet()) {
			int size = e.getValue().getTeams().size();
			if (min > size || r == null) {
				min = size;
				r = e.getValue();
			}
		}
		return r;
	}

	/**
	 * @return	thisのディープコピー
	 * ただしIntegerとか不変なものはコピーされない
	 */
	protected Teams copy() {
		Teams r = new Teams();
		for (Entry<Integer, TeamsByTeamClassId> e : teams.entrySet()) {
			r.getTeams().put(e.getKey(), e.getValue().clone());
		}
		return r;
	}

	public TeamsByTeamClassId getOrCreate(Integer teamClassId) {
		TeamsByTeamClassId teamsByClass = teams.get(teamClassId);
		if (teamsByClass == null) {
			teamsByClass = new TeamsByTeamClassId();
			teams.put(teamClassId, teamsByClass);
		}
		return teamsByClass;
	}

	public Map<Integer, TeamsByTeamClassId> getTeams() {
		return teams;
	}

}