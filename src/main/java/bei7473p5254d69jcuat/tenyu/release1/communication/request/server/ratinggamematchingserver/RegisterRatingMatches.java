package bei7473p5254d69jcuat.tenyu.release1.communication.request.server.ratinggamematchingserver;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.server.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.GameStateByUser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.role.*;
import jetbrains.exodus.env.*;

/**
 * マッチングサーバが作成し反映サーバに送信する。
 * 1メッセージで複数の試合情報を送信できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RegisterRatingMatches extends ServerObjectivityMessage {
	public static int getMatchesMax() {
		return (int) (Glb.getObje().getCore().getConfig().getLoadSetting()
				.getUserMessageListApplySizeMax() / 2);
	}

	public static boolean send(List<RatingGameMatch> match) {
		return Response.success(Glb.getP2p().requestUserRightMessage(to -> {
			RegisterRatingMatches req = new RegisterRatingMatches();
			req.setMatches(match);
			return req;
		}));
	}

	private List<RatingGameMatch> matches;

	@Override
	public boolean apply(Transaction txn, long historyIndex) throws Exception {
		for (RatingGameMatch m : matches) {
			applyMatch(txn, historyIndex, m);
		}

		return true;
	}

	private void applyMatch(Transaction txn, long historyIndex,
			RatingGameMatch match) throws Exception {
		//プレイヤー全員についてゲーム状態が無ければ作成する。
		RatingGameStateByUserStore rgsus = new RatingGameStateByUserStore(txn);
		Long gameId = match.getRatingGameId();
		for (Long playerUserId : match.getPlayers()) {
			if (rgsus.getIdByGameIdUserId(gameId, playerUserId) == null) {
				RatingGameStateByUser state = new RatingGameStateByUser();
				state.setGameRef(
						new GameReference(gameId, GameType.RATINGGAME));
				state.setMainAdministratorUserId(playerUserId);
				state.setRegistererUserId(playerUserId);
				if (rgsus.create(state) == null) {
					Glb.getLogger()
							.error("Failed to create state gameId=" + gameId
									+ " userId=" + playerUserId,
									new Exception());
				}
			}
		}

		//試合を登録する。これによってイベントが通知されゲームクライアントが起動される
		RatingGameMatchStore rgms = new RatingGameMatchStore(txn);
		if (rgms.create(match) == null) {
			Glb.getLogger().error("Failed to create match=" + match,
					new Exception());
		}
	}

	@Override
	public int getApplySize() {
		return matches.size();
	}

	@Override
	protected List<Long> getServers() {
		Role r = Glb.getObje().getRole(rs -> rs
				.getByName(RatingGameMatchingServer.class.getSimpleName()));
		if (r == null)
			return null;
		return r.getAdminUserIds();
	}

	public void setMatches(List<RatingGameMatch> matches) {
		this.matches = matches;
	}

	@Override
	protected boolean validateServerObjectivityMessageConcrete(Message m) {
		if (matches == null) {
			return false;
		}
		if (matches.size() > getMatchesMax()) {
			return false;
		}
		ValidationResult vr = new ValidationResult();
		for (RatingGameMatch match : matches) {
			if (!match.validateAtCreate(vr)) {
				Glb.getLogger().warn(vr.toString(), new Exception());
				return false;
			}
		}
		return true;
	}

}
