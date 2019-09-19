package bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;

public class TakeOverMessageRatingGameMatchingServer
		extends AbstractTakeOverMessage {
	@SuppressWarnings("unchecked")
	@Override
	public RatingGameMatchingServer getServer() {
		return Glb.getMiddle().getRatingGameMatchingServer();
	}

	/**
	 * 単独申請型の情報
	 */
	private Map<GameReference, MatchingStateByGameSingle> singleStateByGame;

	/**
	 * チーム申請型の情報
	 */
	private Map<GameReference, MatchingStateByGameTeam> teamStateByGame;

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return singleStateByGame != null && teamStateByGame != null;
	}

	public static boolean send(NodeIdentifierUser nextServer,
			Map<GameReference, MatchingStateByGameSingle> singleStateByGame,
			Map<GameReference, MatchingStateByGameTeam> teamStateByGame) {
		//引継ぎ先
		TakeOverMessageRatingGameMatchingServer req = new TakeOverMessageRatingGameMatchingServer();
		req.setSingleStateByGame(singleStateByGame);
		req.setTeamStateByGame(teamStateByGame);
		//送信
		Message reqM = Message.build(req)
				.packaging(req.createPackage(nextServer)).finish();
		return Response.success(Glb.getP2p().requestSync(reqM, nextServer));
	}

	public Map<GameReference,
			MatchingStateByGameSingle> getSingleStateByGame() {
		return singleStateByGame;
	}

	public void setSingleStateByGame(
			Map<GameReference, MatchingStateByGameSingle> singleStateByGame) {
		this.singleStateByGame = singleStateByGame;
	}

	public Map<GameReference, MatchingStateByGameTeam> getTeamStateByGame() {
		return teamStateByGame;
	}

	public void setTeamStateByGame(
			Map<GameReference, MatchingStateByGameTeam> teamStateByGame) {
		this.teamStateByGame = teamStateByGame;
	}
}
