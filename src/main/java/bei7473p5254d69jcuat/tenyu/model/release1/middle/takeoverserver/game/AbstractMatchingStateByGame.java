package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.request.server.ratinggamematchingserver.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.Team.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import glb.*;

public abstract class AbstractMatchingStateByGame {
	/**
	 * このゲームの試合をマッチングする
	 */
	protected RatingGame game;

	protected Long gameId;

	/**
	 * このタイムゾーンのユーザーのみ扱う
	 * nullなら全タイムゾーンを扱う
	 */
	protected String timezoneId;

	/**
	 * @param matchCount
	 * @param pace
	 * @return	マッチングを行うか
	 */
	public boolean canMatching(long matchCount, double pace) {
		//最長待機時間
		long longestWait = 60 * 2;
		if (lastWaitStart != lastWaitStartInit) {
			long elapsed = System.currentTimeMillis() - lastWaitStart;
			if (elapsed >= longestWait)
				return true;
		}
		//チーム数が不十分なら、場合によってマッチングを延期する
		if (matchCount < getThreshold()) {
			//少し待ってどれくらいチーム数が増えるか
			long expected = (long) (pace * longestWait);
			//少し待って十分なチーム数になると思われるなら延期
			if (matchCount + expected > getThreshold()) {
				return false;
			}
			//少し待っても無駄と思われる場合マッチングするが、
			//マッチングを予測不可にしたいという要件からすればできれば避けたい事。
			//参加者数が少ない場合を考えると止むを得ない
		}
		return true;
	}

	/**
	 * @param matchCount	今回マッチングされる試合数
	 * @return	十分なランダム性があるか
	 */
	public boolean enoughRandom(long matchCount) {
		return matchCount > getThreshold();
	}

	/**
	 * @param sorted		各チームクラスID毎のチーム一覧
	 * @param unitIndex		単位番号
	 * @param unit			単位の大きさ
	 * @return				指定された単位番号の各チームクラスID毎のチーム一覧
	 */
	private List<List<Team>> extractShuffledUnit(List<List<Team>> sorted,
			long unitIndex, long unit, long unitThisTime) {
		long min = getMin(sorted);
		List<List<Team>> r = new ArrayList<>();
		for (List<Team> byTeamClass : sorted) {
			long start = unit * unitIndex;
			List<Team> unitByTeamClass = new ArrayList<>();
			for (long i = start; i < start + unitThisTime && i < min; i++) {
				unitByTeamClass.add(byTeamClass.get((int) i));
			}
			Collections.shuffle(unitByTeamClass);
			r.add(unitByTeamClass);
		}
		return r;
	}

	/**
	 * @param listList	一覧の一覧
	 * @return	最小の要素数
	 */
	protected int getMin(List<List<Team>> listList) {
		int min = 0;
		boolean first = true;
		for (List<?> l : listList) {
			if (first) {
				min = l.size();
				first = false;
				continue;
			}
			if (min > l.size()) {
				min = l.size();
			}
		}
		return min;
	}

	/**
	 * できるだけこの試合数を超えてからマッチングする
	 * @return
	 */
	public abstract long getThreshold();

	public String getTimezoneId() {
		return timezoneId;
	}

	public boolean isSupportTimezone(Long userId) {
		if (timezoneId == null)
			return true;
		User u = Glb.getObje().getUser(us->us.get(userId));
		if (u == null)
			return false;
		if (timezoneId.equals(u.getTimezoneId())) {
			return true;
		}
		return false;
	}

	/**
	 * マッチングが延期された回数。
	 * マッチングされるたびにリセットされる。
	 */
	private long lastWaitStart = lastWaitStartInit;

	private static final long lastWaitStartInit = -1;

	/**
	 * レーティングを考慮してある程度ランダムにマッチングする
	 * 一度にまとめてマッチングする。その方がマッチングが予測不可能になるから
	 * @return	マッチングされたチーム一覧
	 */
	protected List<RatingGameMatch> matching(Teams m,
			double matchCountIncreasePace, long matchCount, MatchingType type) {
		//最大同時マッチング数の上限
		if (matchCount > RegisterRatingMatches.getMatchesMax()) {
			matchCount = RegisterRatingMatches.getMatchesMax();
		}
		if (!canMatching(matchCount, matchCountIncreasePace)) {
			Glb.debug("cannot matching. pace=" + matchCountIncreasePace
					+ " matchCount=" + matchCount + " threshold="
					+ getThreshold());
			if (lastWaitStart != lastWaitStartInit)
				lastWaitStart = System.currentTimeMillis();
			return null;
		}
		lastWaitStart = lastWaitStartInit;
		Glb.debug("matching start");

		//チームクラス情報を取得
		//RatingGameが更新される可能性もあるので毎回取得しなおす必要がある。
		//ただし更新されていない場合Xodus内部でキャッシュが返される
		if (game == null) {
			Glb.getLogger().error("rg is null", new IllegalStateException());
			return null;
		}

		//この瞬間のfullTeamsのコピー。以下、これに対して処理する
		Teams fullTeamsMoment = m.copy();
		if (game.getTeamClasses().size() > fullTeamsMoment.getTeams().size()) {
			return null;
		}

		List<List<Team>> sorted = ratingSort(fullTeamsMoment);

		List<List<Team>> matchedTeamList = matchingByRating(matchCount, sorted);

		boolean enoughRandom = enoughRandom(matchedTeamList.size());

		Long myUserId = Glb.getMiddle().getMyUserId();
		//試合オブジェクトにして返す
		List<RatingGameMatch> r = new ArrayList<>();
		for (List<Team> matched : matchedTeamList) {
			RatingGameMatch match = new RatingGameMatch();
			match.setEnoughRandom(enoughRandom);
			match.setMatchedTeams(matched);
			match.setRatingGameId(game.getId());
			match.setRegistererUserId(myUserId);
			match.setMainAdministratorUserId(myUserId);
			match.setType(type);
			match.setHost(RatingGameMatch.getRndPlayer(matched));
			r.add(match);
		}

		//反映サーバに送信する
		if (!RegisterRatingMatches.send(r)) {
			//ログだけ出して、失敗扱いにはしない
			Glb.getLogger().error("Failed to send RegisterRatingMatch",
					new Exception());
		}

		return r;
	}

	private List<List<Team>> matchingByRating(long matchCount,
			List<List<Team>> ratingSorted) {
		//チーム一覧を一定単位で区切って各単位の中でシャッフルする事で
		//ある程度同レーティングとマッチングしつつランダム性のあるマッチングをする。
		//単位
		long unit = 10;
		//繰り返し回数
		long loop = matchCount / unit;
		//余り
		long surplus = matchCount % unit;

		List<List<Team>> matchedTeamList = new ArrayList<>();
		for (long unitIndex = 0; unitIndex <= loop; unitIndex++) {
			//単位の大きさ。基本的にunitが使われるが最後の1回は余り分になる
			long unitThisTime = unit;
			//最後の1回は余り分。このため比較演算子が<=で1回多くループさせている
			if (unitIndex == loop)
				unitThisTime = surplus;
			//余りが0の場合がある。この場合、処理する必要無し
			if (unitThisTime == 0)
				break;

			//各チームクラスID毎に単位分を取り出してシャッフルする
			List<List<Team>> shuffledUnits = extractShuffledUnit(ratingSorted,
					unitIndex, unit, unitThisTime);

			//マッチング
			List<List<Team>> matchedsInUnit = matchingSub(shuffledUnits);

			//結果に加える
			for (List<Team> matched : matchedsInUnit) {
				matchedTeamList.add(matched);
			}
		}
		return matchedTeamList;
	}

	/**
	 * シャッフルされてマッチングされる部分
	 * 各チームクラスID毎のソートされたチーム一覧のunit単位で区切られた部分
	 * @param sorted	各チームクラスID毎のチーム一覧
	 * @return			マッチングされたチーム一覧の一覧
	 */
	private List<List<Team>> matchingSub(
			List<List<Team>> sortAndShuffledUnits) {
		List<List<Team>> r = new ArrayList<>();
		for (int i = 0;; i++) {
			List<Team> matched = new ArrayList<>();
			for (List<Team> teams : sortAndShuffledUnits) {
				if (teams.size() - 1 < i) {
					return r;
				}
				matched.add(teams.get(i));
			}
			r.add(matched);
		}
	}

	/**
	 * @param fullTeams
	 * @return	チームクラスID順のチーム一覧。チーム一覧はレーティングソートされる
	 */
	private List<List<Team>> ratingSort(Teams fullTeams) {
		//ランダムで降順か昇順か変える
		boolean reverse = Glb.getRnd().nextBoolean();
		//チームクラスID別でソートされたチーム一覧が格納される
		List<List<Team>> sorted = new ArrayList<>();
		Comparator<Team> ratingComparator = Comparator
				.comparingInt(Team::getAveRating);
		for (Entry<Integer, TeamsByTeamClassId> e : fullTeams.getTeams()
				.entrySet()) {
			//特定のチームクラスのチーム一覧
			List<Team> byTeamClass = new ArrayList<>(
					e.getValue().getTeams().values());
			byTeamClass.sort(ratingComparator);
			if (reverse)
				Collections.reverse(byTeamClass);
			sorted.add(byTeamClass);
		}
		return sorted;
	}

	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
}
