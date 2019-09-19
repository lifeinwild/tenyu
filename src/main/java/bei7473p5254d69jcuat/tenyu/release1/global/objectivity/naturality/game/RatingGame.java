package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * MatchGameはレーティング計算の対象になる試合形式のゲーム
 * 常駐空間型ゲームで試合形式をやれないわけではないが、
 * そのようなものはレーティング計算の対象にならない。
 * レーティング計算の対象になるのが本質である。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGame extends AbstractGame implements RatingGameDBI {
	public static final int teamMax = 2000;

	public static boolean createSequence(Transaction txn, RatingGame rg,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, rg, specifiedId,
				historyIndex, new RatingGameStore(txn),
				rg.getRegistererUserId(), rg.getRegistererUserId(),
				NodeType.RATINGGAME);
	}

	public static boolean deleteSequence(Transaction txn, RatingGame u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				new RatingGameStore(txn), NodeType.RATINGGAME);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(RatingGame.class.getSimpleName()).getAdminUserIds());
	}

	/**
	 * 最長試合時間
	 * 秒
	 */
	private long longestMatchTimeSeconds;

	/**
	 * チーム（役割）一覧
	 * 順序は重要で、各所からint値によってTeamが参照される。
	 * このインデックス==teamId
	 */
	private List<TeamClass> teamClasses = new ArrayList<>();

	@Override
	public String getDir() {
		return Glb.getFile().getRatingGameDirSingle() + getName() + "/";
	}

	public boolean addTeam(TeamClass t) {
		return teamClasses.add(t);
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(RatingGame.class.getSimpleName()).getAdminUserIds());
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(mainAdministratorUserId);
		return r;
	}

	/**
	 * @return	全チームクラス中最大のメンバーカウントを持つもの
	 */
	public TeamClass getBiggestMemberCountTeamClass() {
		int max = 0;
		TeamClass r = null;
		for (TeamClass tc : teamClasses) {
			if (tc.getMemberCount() > max || r == null) {
				max = tc.getMemberCount();
				r = tc;
			}
		}
		return r;
	}

	public long getLongestMatchTimeSeconds() {
		return longestMatchTimeSeconds;
	}

	/**
	 * @return	全チームクラス中最小のメンバー数を持つチームクラス
	 */
	public TeamClass getMinTeamClass() {
		int min = 0;
		TeamClass r = null;
		for (TeamClass c : teamClasses) {
			int size = c.getMemberCount();
			if (min > size || r == null) {
				min = size;
				r = c;
			}
		}
		return r;
	}

	public int getRandomTeamClassId() {
		int max = getTeamClasses().size();
		if (max == 0)
			return 0;
		return Glb.getRnd().nextInt(max);
	}

	public TeamClass getTeamClass(int teamClassId) {
		return teamClasses.get(teamClassId);
	}

	public TeamClass getTeamClass(String teamClassName) {
		for (TeamClass c : teamClasses) {
			if (c.getName().equals(teamClassName)) {
				return c;
			}
		}
		return null;
	}

	public List<TeamClass> getTeamClasses() {
		return Collections.unmodifiableList(teamClasses);
	}

	/**
	 * @return	1試合の最大メンバー数
	 */
	public int getTotalMemberCount() {
		int total = 0;
		for (TeamClass tc : teamClasses) {
			total += tc.getMemberCount();
		}
		return total;
	}

	public void setMatchTimeSeconds(long matchTimeSeconds) {
		this.longestMatchTimeSeconds = matchTimeSeconds;
	}

	private final boolean validateAtCommonAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (teamClasses == null || teamClasses.size() <= 0) {
			r.add(Lang.RATINGGAME_TEAMCLASS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (teamClasses.size() > teamMax) {
				r.add(Lang.RATINGGAME_TEAMCLASS, Lang.ERROR_TOO_MANY,
						teamClasses.size() + " / " + teamMax);
				b = false;
			} else {
				//名前の重複を判定する。チームクラス名は1ゲームタイトル中で一意であるべき
				HashSet<String> teamNames = new HashSet<>();
				for (TeamClass t : teamClasses) {
					if (teamNames.contains(t.getName())) {
						r.add(Lang.RATINGGAME_TEAMCLASS_NAME,
								Lang.ERROR_DUPLICATE, t.getName());
						b = false;
						break;
					}
					teamNames.add(t.getName());
				}
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAbstractGameConcrete(r))
			b = false;
		if (teamClasses != null) {
			for (TeamClass t : teamClasses) {
				if (!t.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAbstractGameConcrete(r))
			b = false;
		if (teamClasses != null) {
			for (TeamClass t : teamClasses) {
				if (!t.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAbstractGameConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof RatingGame)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//RatingGame old2 = (RatingGame) old;

		boolean b = true;
		return b;
	}

	/**
	 * マッチングサーバから利用される事を想定
	 * @param teams
	 * @return	teamsはこのゲームの試合を開催するのに適切な状態にあるか
	 * state等は考慮しない。チームクラスとの整合性を検証する
	 * BAN状態は考慮しない、マッチングサーバで拒否すべき。
	 */
	public boolean validateMatching(RatingGameMatch match) {
		List<Team> teams = match.getMatchedTeams();
		if (teams == null || teams.size() == 0)
			return false;
		if (teams.size() != teamClasses.size()) {
			return false;
		}

		//全チームクラス名が揃っているか
		a: for (int i = 0; i < teamClasses.size(); i++) {
			TeamClass tc = teamClasses.get(i);
			int teamClassId = i;
			for (Team t : teams) {
				if (t.getTeamClassId() == teamClassId) {
					//人数が合っているか
					if (t.getMembers().size() == tc.getMemberCount()) {
						continue a;
					} else {
						return false;
					}
				}
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean validateReferenceAbstractGameConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (teamClasses != null) {
			for (TeamClass t : teamClasses) {
				if (!t.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

}
