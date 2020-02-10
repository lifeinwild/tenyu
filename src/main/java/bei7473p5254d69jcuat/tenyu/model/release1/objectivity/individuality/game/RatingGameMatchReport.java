package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 試合参加者の電子署名つきの各参加者視点の情報
 * 非スレッドセーフ
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameMatchReport implements Storable {

	public static final int commentMax = 2000;

	/**
	 * @param reports
	 * @return	全チームのランキング。あるいはnull
	 * @throws Exception
	 */
	public static Map<Integer, HashSet<Integer>> getFullRanking(
			List<RatingGameMatchReport> reports) throws Exception {
		Map<Integer, HashSet<Integer>> r = new HashMap<>();
		for (RatingGameMatchReport report : reports) {
			for (Entry<Integer, HashSet<Integer>> e : report.getRanking()
					.entrySet()) {
				HashSet<Integer> teamIds = r.get(e.getKey());
				if (teamIds == null)
					teamIds = new HashSet<>();
				teamIds.addAll(e.getValue());
				r.put(e.getKey(), teamIds);
			}
		}
		return r;
	}

	public static String getNominal() {
		return "RatingGameParticipation";
	}

	/**
	 * @param reports
	 * @return	各プレイヤーの報告が矛盾しているか
	 * @throws Exception
	 */
	public static boolean isConflict(RatingGameMatch match,
			List<RatingGameMatchReport> reports) throws Exception {
		try {
			//全報告の合成
			Map<Integer, HashSet<Integer>> mainR = getFullRanking(reports);
			if (mainR == null)
				throw new IllegalStateException("invalid reports");

			//合成された報告の矛盾から各プレイヤーの報告の矛盾を探せる

			//矛盾を探す
			int teamMax = match.getMatchedTeams().size();

			int teamCount = 0;
			for (HashSet<Integer> teamIds : mainR.values()) {
				teamCount += teamIds.size();
			}
			if (teamMax != teamCount) {
				Glb.getLogger()
						.warn("Invalid teamCount. teamCount=" + teamCount
								+ " teamMax=" + teamMax + " match=" + match,
								new Exception());
				return true;
			}

			//0から連番であるか
			for (int i = 0; i < mainR.keySet().size(); i++) {
				if (mainR.get(i) == null) {
					return true;
				}
			}

		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return true;
		}
		return false;
	}

	/**
	 * 報告者が不正行為があったと主張しているか
	 */
	private boolean claim = false;

	/**
	 * 最後まで試合を見たプレイヤーが報告する場合true
	 */
	private boolean finished = false;
	/**
	 * ゲームクライアントが自動的に作成したコメント
	 */
	private String gameClientComment;
	/**
	 * 署名に用いた鍵タイプ
	 */
	private KeyType keyType = KeyType.PC;

	/**
	 * 参加した試合のID
	 */
	private Long matchId;

	/**
	 * この試合に参加したプレイヤーのこの試合へのコメント
	 */
	private String playerComment;

	/**
	 * このオブジェクトはこのプレイヤー視点の情報
	 */
	private NodeIdentifierUser playerNode;

	/**
	 * 順位 : チームID一覧
	 * 0から
	 * 同じ順位のチームがあるかもしれないので値が一覧になっている。
	 * 2位が2チーム居れば3位は無く次は4位
	 */
	private Map<Integer, HashSet<Integer>> ranking = new HashMap<>();

	/**
	 * このプレイヤー視点のリプレイファイルのハッシュ値
	 */
	private byte[] replayFileHash;

	/**
	 * rankingが定義できないような場合
	 * もしゲームクライアントが不正を検出した場合、不正試合として報告できる。
	 * 半数のプレイヤーが不正試合として報告した場合、レーティング計算が行われない。
	 */
	private RatingGameMatchReport.RatingMatchIllegalResult result;

	/**
	 * 電子署名
	 */
	private byte[] sign;

	@SuppressWarnings("unused")
	private RatingGameMatchReport() {
	}

	public RatingGameMatchReport(Long matchId,
			Map<Integer, HashSet<Integer>> ranking) {
		this(matchId, ranking, RatingMatchIllegalResult.NORMAL);
	}

	/**
	 * @param matchId
	 * @param ranking
	 * @param illegalResult
	 */
	public RatingGameMatchReport(Long matchId,
			Map<Integer, HashSet<Integer>> ranking,
			RatingGameMatchReport.RatingMatchIllegalResult illegalResult) {
		if (matchId == null || illegalResult == null)
			throw new IllegalArgumentException();
		this.ranking = ranking;
		playerNode = Glb.getMiddle().getMyNodeIdentifierUser();
		if (playerNode == null)
			throw new IllegalStateException();
		this.matchId = matchId;
		this.result = illegalResult;
		sign = sign();
		if (sign == null)
			throw new IllegalStateException();
	}

	/**
	 * 敗者が出た時
	 * 敗者はその時点で試合を抜けれる
	 *
	 * 他チームが負けた場合や自チームが負けた場合に呼ぶ。
	 *
	 * @param loserTeamId
	 */
	public void addRanking(Integer rankingThisTeam, int loserTeamId) {
		HashSet<Integer> teams = ranking.get(rankingThisTeam);
		if (teams == null)
			teams = new HashSet<>();
		teams.add(loserTeamId);
		ranking.put(rankingThisTeam, teams);
	}

	/**
	 * DBアクセスが発生する検証処理
	 * validateを通過している前提なのでヌルチェック等は行われない
	 * @param r
	 * @return	この検証処理で何かエラーメッセージが追加されたか
	 */
	public boolean dbValidate(ValidationResult r) {
		try {
			int teamCount = ranking.size();
			RatingGameMatch m = Glb.getObje()
					.getRatingGameMatchStore(rgms -> rgms.get(matchId));
			Long rgId = m.getRatingGameId();
			RatingGame g = Glb.getObje().getRatingGame(rgs -> rgs.get(rgId));
			if (teamCount > g.getTeamClasses().size()) {
				r.add(Lang.RATINGGAME_MATCH_REPORT, Lang.ERROR_INVALID);
				return false;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			r.add(Lang.RATINGGAME_MATCH_REPORT, Lang.EXCEPTION);
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RatingGameMatchReport other = (RatingGameMatchReport) obj;
		if (claim != other.claim)
			return false;
		if (finished != other.finished)
			return false;
		if (gameClientComment == null) {
			if (other.gameClientComment != null)
				return false;
		} else if (!gameClientComment.equals(other.gameClientComment))
			return false;
		if (keyType != other.keyType)
			return false;
		if (matchId == null) {
			if (other.matchId != null)
				return false;
		} else if (!matchId.equals(other.matchId))
			return false;
		if (playerComment == null) {
			if (other.playerComment != null)
				return false;
		} else if (!playerComment.equals(other.playerComment))
			return false;
		if (playerNode == null) {
			if (other.playerNode != null)
				return false;
		} else if (!playerNode.equals(other.playerNode))
			return false;
		if (ranking == null) {
			if (other.ranking != null)
				return false;
		} else if (!ranking.equals(other.ranking))
			return false;
		if (!Arrays.equals(replayFileHash, other.replayFileHash))
			return false;
		if (result != other.result)
			return false;
		if (!Arrays.equals(sign, other.sign))
			return false;
		return true;
	}

	public String getGameClientComment() {
		return gameClientComment;
	}

	public KeyType getKeyType() {
		return keyType;
	}

	public Long getMatchId() {
		return matchId;
	}

	public String getPlayerComment() {
		return playerComment;
	}

	public NodeIdentifierUser getPlayerUserId() {
		return playerNode;
	}

	public Map<Integer, HashSet<Integer>> getRanking() {
		return ranking;
	}

	public byte[] getReplayFileHash() {
		return replayFileHash;
	}

	public RatingGameMatchReport.RatingMatchIllegalResult getResult() {
		return result;
	}

	public byte[] getSign() {
		return sign;
	}

	public byte[] getSignTarget() {
		//署名が署名に影響しないようにする
		//2回続けて署名した時にsignの値に依存して返値が変化しないようにしている
		byte[] signBackup = sign;
		sign = null;
		try {
			MessageDigest md = Glb.getUtil().getMDSecure();
			return md.digest(Glb.getUtil().toKryoBytesForCommunication(this));
		} catch (IOException e) {
			Glb.getLogger().error("", e);
		} finally {
			sign = signBackup;
		}
		return null;
	}

	public int getTeamCountOfRanking() {
		int r = 0;
		for (HashSet<Integer> teamIds : ranking.values()) {
			r += teamIds.size();
		}
		return r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (claim ? 1231 : 1237);
		result = prime * result + (finished ? 1231 : 1237);
		result = prime * result + ((gameClientComment == null) ? 0
				: gameClientComment.hashCode());
		result = prime * result + ((keyType == null) ? 0 : keyType.hashCode());
		result = prime * result + ((matchId == null) ? 0 : matchId.hashCode());
		result = prime * result
				+ ((playerComment == null) ? 0 : playerComment.hashCode());
		result = prime * result
				+ ((playerNode == null) ? 0 : playerNode.hashCode());
		result = prime * result + ((ranking == null) ? 0 : ranking.hashCode());
		result = prime * result + Arrays.hashCode(replayFileHash);
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + Arrays.hashCode(sign);
		return result;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setGameClientComment(String gameClientComment) {
		this.gameClientComment = gameClientComment;
	}

	public void setKeyType(KeyType keyType) {
		this.keyType = keyType;
	}

	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	public void setPlayerComment(String playerComment) {
		this.playerComment = playerComment;
	}

	public void setPlayerUserId(NodeIdentifierUser playerUserId) {
		this.playerNode = playerUserId;
	}

	public void setRanking(Map<Integer, HashSet<Integer>> ranking) {
		this.ranking = ranking;
	}

	public void setReplayFileHash(byte[] replayFileHash) {
		this.replayFileHash = replayFileHash;
	}

	public void setResult(
			RatingGameMatchReport.RatingMatchIllegalResult result) {
		this.result = result;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	public byte[] sign() {
		try {
			return Glb.getConf().sign(getNominal(), keyType, getSignTarget());
		} catch (InvalidKeySpecException | NoSuchAlgorithmException
				| IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 署名後に呼ぶ想定
	 * @param r
	 * @return
	 */
	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (keyType == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_KEYTYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (matchId == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_MATCHID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(matchId)) {
				r.add(Lang.RATINGGAME_MATCH_REPORT_MATCHID, Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (playerNode == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_PLAYERUSERID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!playerNode.validate(r)) {
				b = false;
			} else {
				if (!IdObject.validateIdStandardNotSpecialId(
						playerNode.getUserId())) {
					r.add(Lang.RATINGGAME_MATCH_REPORT_PLAYERUSERID,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		if (ranking == null || ranking.size() == 0) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_RANKING, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (ranking.size() > RatingGame.teamMax) {
				r.add(Lang.RATINGGAME_MATCH_REPORT_RANKING, Lang.ERROR_TOO_BIG);
				b = false;
			} else {
				for (Integer e : ranking.keySet()) {
					if (e < 0) {
						r.add(Lang.RATINGGAME_MATCH_REPORT_RANKING,
								Lang.ERROR_TOO_FEW);
						b = false;
						break;
					}
				}
				//ランキングに重複して入っているチームIDがあるか
				HashSet<Integer> dupCheck = new HashSet<>();
				for (HashSet<Integer> teamIds : ranking.values()) {
					for (Integer teamId : teamIds) {
						if (dupCheck.contains(teamId)) {
							r.add(Lang.RATINGGAME_MATCH_REPORT_RANKING,
									Lang.ERROR_DUPLICATE);
							b = false;
							break;
						} else {
							dupCheck.add(teamId);
						}
					}
				}
			}
		}
		if (replayFileHash == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_REPLAYFILEHASH,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (replayFileHash.length > Glb.getConst().getHashSize()) {
				r.add(Lang.RATINGGAME_MATCH_REPORT_REPLAYFILEHASH,
						Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (result == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_RESULT, Lang.ERROR_EMPTY);
			b = false;
		}
		if (sign == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_SIGN, Lang.ERROR_EMPTY);
			b = false;
		}

		if (playerComment != null && playerComment.length() > commentMax) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_PLAYERCOMMENT,
					Lang.ERROR_INVALID);
			b = false;
		}

		if (gameClientComment != null
				&& gameClientComment.length() > commentMax) {
			r.add(Lang.RATINGGAME_MATCH_REPORT_GAMECLIENTCOMMENT,
					Lang.ERROR_INVALID);
			b = false;
		}

		if (b) {
			//署名検証
			User u = Glb.getObje()
					.getUser(us -> us.get(playerNode.getUserId()));
			PublicKey pub;
			try {
				pub = Glb.getUtil().getPub(u.getPubKey(keyType));
				if (!Glb.getUtil().verify(getNominal(), sign, pub,
						getSignTarget())) {
					r.add(Lang.RATINGGAME_MATCH_REPORT_SIGN,
							Lang.ERROR_INVALID);
					b = false;
				}
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				Glb.getLogger().error("", e);
				r.add(Lang.RATINGGAME_MATCH_REPORT_SIGN, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		RatingGameMatchStore rgms = new RatingGameMatchStore(txn);
		UserStore us = new UserStore(txn);
		if (rgms.get(matchId) == null) {
			r.add(Lang.RATINGGAME_MATCH_RATINGGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		if (us.get(playerNode.getUserId()) == null) {
			r.add(Lang.RATINGGAME_MATCH_REPORT,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}

		return b;
	}

	/**
	 * 各プレイヤー視点で試合の結果について報告する場合の結果種別
	 * 勝利、敗北、順位等はrankingで報告されるので
	 * これは試合全体が健全に完了したかという情報を記述する事がメイン
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static enum RatingMatchIllegalResult {
		/**
		 * 決着がつかなかったが不正等でなく正常な場合。ほとんどないだろう
		 */
		DRAW,
		/**
		 * 不正があったなど無効試合の主張
		 */
		INVALID_MATCH,
		/**
		 * 正常な試合結果
		 */
		NORMAL,
		/**
		 * 自分は退室した。
		 */
		WITHDRAWAL,
	}
}