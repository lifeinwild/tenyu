package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game;

import java.util.*;
import java.util.concurrent.atomic.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.Team.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import glb.*;
import glb.util.*;

public class MatchingStateByGameSingle extends AbstractMatchingStateByGame {
	/**
	 * 最大同時申請数
	 */
	public static final int max = 1000 * 1000 * 10;

	/**
	 * ゲーム：マッチング申請したプレイヤー一覧
	 * removeの性能のためHashSetを使う。
	 */
	private Set<NodeIdentifierUser> applicants = Collections
			.synchronizedSet(new HashSet<>());

	private DateList dates = new DateList();

	private AtomicLong teamNameIncrement = new AtomicLong();

	public boolean contains(Long userId) {
		for (NodeIdentifierUser applicant : applicants) {
			Long id = applicant.getUserId();
			if (id == null)
				continue;
			if (id.equals(userId))
				return true;
		}
		return false;
	}

	public boolean addApplicant(NodeIdentifierUser node) {
		if (node == null)
			return false;
		if (!Model.validateIdStandardNotSpecialId(node.getUserId()))
			return false;
		if (applicants.size() >= max)
			return false;
		if (applicants.contains(node))
			return false;
		if (!isSupportTimezone(node.getUserId()))
			return false;
		boolean b = applicants.add(node);
		if (b)
			dates.add();
		return b;
	}

	/**
	 * @param game		grefと対応するgame
	 * @param gameId		gameと対応するid
	 * @param teamName	チーム名
	 * @return			作成されたチーム
	 */
	protected Team createTeam(RatingGame game, String teamName) {
		Team t = new Team(game.getId(), teamName, null,
				Glb.getMiddle().getMyNodeIdentifierUser(), MatchingType.SINGLE,
				game.getRandomTeamClassId());
		return t;
	}

	private Teams createTeams(List<NodeIdentifierUser> applicants,
			String teamNamePrefix) {
		Team t = null;
		//作成されたチーム一覧
		Teams teams = new Teams();
		for (NodeIdentifierUser applicant : applicants) {
			//初回ケースでnullの場合、またはフルになったら次のチームの構築に移る
			if (t == null || t.isFull()) {
				//フルの場合、チーム一覧に加える
				if (t != null) {
					t.finish();
					ValidationResult vr = new ValidationResult();
					if (t.validateAtCreate(vr)) {
						teams.addTeam(t);
					} else {
						Glb.getLogger().error(
								"Invalid team. t=" + t + " vr=" + vr,
								new Exception());
					}
				}
				String teamName = teamNamePrefix
						+ teamNameIncrement.incrementAndGet();
				Glb.debug("teamName=" + teamName);
				t = createTeam(game, teamName);
			}
			//メンバーを加える
			t.addMember(applicant, null);
		}
		return teams;
	}

	public List<NodeIdentifierUser> getApplicantsCopy() {
		synchronized (applicants) {
			return new ArrayList<>(applicants);
		}
	}

	@Override
	public long getThreshold() {
		return Glb.getConf().isDevOrTest() ? 50 : 10;
	}

	public double matchCountIncreasePace() {
		//1試合の最大メンバー数
		int totalMemberCount = game.getTotalMemberCount();
		if (totalMemberCount == 0)
			totalMemberCount = 1;
		//1秒あたり申請者増加ペース
		double pace = dates.pace();
		double matchCountIncreasePace = pace / totalMemberCount;
		return matchCountIncreasePace;
	}

	/**
	 * このメソッドはマルチスレッドでアクセスされる事を想定していない
	 * @return
	 */
	public List<RatingGameMatch> matching() {
		//ゲームの設定が更新される事を考えるとある一時点の設定で
		//一連のマッチング関連の処理をする必要がある。
		game = Glb.getObje().getRatingGame(s -> s.get(gameId));

		//この瞬間のコピーを作成し、以下このリストを使って処理する
		List<NodeIdentifierUser> applicantsMoment = getApplicantsCopy();
		Glb.debug("applicantsMoment.size=" + applicantsMoment.size());

		//これでランダムマッチングになる
		Collections.shuffle(applicantsMoment);

		//今回作成されるチームの名前の接頭辞
		String teamNamePrefix = game.getName() + " Single RandomTeam "
				+ System.currentTimeMillis() + " ";

		//作れる限りチームをランダムに作る
		Teams teams = createTeams(applicantsMoment, teamNamePrefix);

		Glb.debug("teams.size=" + teams.getTeams().size());
		if (teams.getTeams().size() == 0) {
			return null;
		}

		List<RatingGameMatch> matched = matching(teams,
				matchCountIncreasePace(), teams.getMin().size(),
				MatchingType.SINGLE);
		if (matched == null)
			return matched;
		Glb.debug("matched.size=" + matched.size());
		for (RatingGameMatch match : matched) {
			//マッチング待ちリストから削除
			for (Team matchedTeam : match.getMatchedTeams()) {
				/*
				 * https://codepumpkin.com/removeall-method-abstractset/
					 * removeAllは渡されたコレクションの方がSetより大きいと性能が悪化する場合があるが、
					 * このクラスでの使用法ではそれは生じないものの、
					 * Setと渡されたコレクションの要素数が等しい場合も遅くなるので、
					 * そしてそれはありうるので使わなかった。
					 *
					 * シャッフル前のリストがあれば、それで削除すると最速かもしれない。
					 * しかしリストを2つ用意するコストとの比較などがある
					 */
				for (NodeIdentifierUser node : matchedTeam.getMembers()) {
					applicants.remove(node);
				}
			}
		}
		return matched;
	}

	public boolean removeApplicant(Long userId) {
		return applicants.remove(userId);
	}

}
