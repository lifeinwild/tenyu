package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import jetbrains.exodus.env.*;

/**
 * レーティングゲームタイトル毎のユーザー毎の状態。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameStateByUser extends GameStateByUser
		implements RatingGameStateByUserDBI {

	public static final int initialMatchCount = 0;

	public static final int initialRating = 0;

	/**
	 * 最低でもこの数試合をこなさないと仮想通貨分配が生じない
	 */
	public static final int matchCountMin = 5;

	/**
	 * 通算試合数
	 * 単独申請型
	 */
	private int matchCountSingle = initialMatchCount;

	/**
	 * 通算試合数
	 * チーム申請型
	 */
	private int matchCountTeam = initialMatchCount;

	/**
	 * 単独申請型マッチングのレーティング
	 */
	private int singleRating = initialRating;

	/**
	 * チーム申請型マッチングのレーティング
	 */
	private int teamRating = initialRating;

	public int getMatchCountSingle() {
		return matchCountSingle;
	}

	public int getMatchCountTeam() {
		return matchCountTeam;
	}

	public int getSingleRating() {
		return singleRating;
	}

	public int getTeamRating() {
		return teamRating;
	}

	public void setMatchCountSingle(int matchCountSingle) {
		this.matchCountSingle = matchCountSingle;
	}

	public void setMatchCountTeam(int matchCountTeam) {
		this.matchCountTeam = matchCountTeam;
	}

	public void setSingleRating(int singleRating) {
		this.singleRating = singleRating;
	}

	public void setTeamRating(int rating) {
		this.teamRating = rating;
	}

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (teamRating < 0) {
			r.add(Lang.RATINGGAMESTATEBYUSER_TEAM_RATING, Lang.ERROR_INVALID);
			b = false;
		}
		if (singleRating < 0) {
			r.add(Lang.RATINGGAMESTATEBYUSER_SINGLE_RATING, Lang.ERROR_INVALID);
			b = false;
		}
		if (matchCountSingle < 0) {
			r.add(Lang.RATINGGAMESTATEBYUSER_MATCHCOUNT_SINGLE,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (matchCountTeam < 0) {
			r.add(Lang.RATINGGAMESTATEBYUSER_MATCHCOUNT_TEAM,
					Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	public final boolean validateAtCreateGameStateByUserConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;

		if (teamRating != initialRating) {
			r.add(Lang.RATINGGAMESTATEBYUSER_TEAM_RATING,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (singleRating != initialRating) {
			r.add(Lang.RATINGGAMESTATEBYUSER_SINGLE_RATING,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (matchCountSingle != initialMatchCount) {
			r.add(Lang.RATINGGAMESTATEBYUSER_MATCHCOUNT_SINGLE,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (matchCountTeam != initialMatchCount) {
			r.add(Lang.RATINGGAMESTATEBYUSER_MATCHCOUNT_TEAM,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeGameStateByUserConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof RatingGameStateByUser)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//RatingGameStateByUser old2 = (RatingGameStateByUser) old;

		boolean b = true;
		return b;
	}

	@Override
	public final boolean validateAtUpdateGameStateByUserConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r))
			b = false;
		return b;
	}

	@Override
	public final boolean validateReferenceGameStateByUserConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		GameReference gameRef = getGameRef();
		AbstractGame game = gameRef.getGame(txn);
		if (game == null) {
			//抽象クラスでこの検証はしている
			b = false;
		} else {
			if (!(game instanceof RatingGame)) {
				r.add(Lang.RATINGGAMESTATEBYUSER_GAMEREFERENCE,
						Lang.ERROR_INVALID, gameRef.toString());
				b = false;
			}
		}
		return b;
	}

}
