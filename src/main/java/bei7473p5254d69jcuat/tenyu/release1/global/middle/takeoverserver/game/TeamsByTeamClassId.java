package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.util.*;

public class TeamsByTeamClassId {
	/**
	 * チーム名：チーム
	 */
	private Map<String, Team> teams = new ConcurrentHashMap<>();
	/**
	 * できるだけ多くのチームないしプレイヤーが登録されている状況でマッチングしたほうが
	 * マッチングが予測不可になるので、
	 * 少し待つだけで増えると思われる場合は待ちたいということで作成。
	 */
	protected DateList dates = new DateList();

	private List<String> keyListCache = null;
	private long lastCache = 0;

	public int size() {
		return teams.size();
	}

	public boolean remove(Team t) {
		return teams.remove(t.getName()) != null;
	}

	/**
	 * キャッシュが使用され負荷が限定的なキー一覧の取得
	 * 必ずしも最新のキー一覧にならない。
	 * @return
	 */
	public List<String> getKeyList() {
		//5秒に1回更新する。過剰に更新すると負荷が気になるので
		long dif = System.currentTimeMillis() - lastCache;
		if (keyListCache == null || dif > 5000) {
			keyListCache = new ArrayList<>(teams.keySet());
			lastCache = System.currentTimeMillis();
		}
		return keyListCache;
	}

	public TeamsByTeamClassId clone() {
		TeamsByTeamClassId clone = new TeamsByTeamClassId();
		clone.getTeams().putAll(teams);
		dates.copyTo(clone.getDates());
		return clone;
	}

	public DateList getDates() {
		return dates;
	}

	public void setDates(DateList dates) {
		this.dates = dates;
	}

	public Map<String, Team> getTeams() {
		return teams;
	}

	public void setTeams(Map<String, Team> teams) {
		this.teams = teams;
	}

}