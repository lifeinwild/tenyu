package bei7473p5254d69jcuat.tenyu.communication.request.useredge;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game.*;
import glb.*;

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
	private Map<Long, MatchingStateByGameSingle> singleStateByGame;

	/**
	 * チーム申請型の情報
	 */
	private Map<Long, MatchingStateByGameTeam> teamStateByGame;

	@Override
	protected boolean validateRequestConcrete(Message m) {
		return singleStateByGame != null && teamStateByGame != null;
	}

	public static boolean send(NodeIdentifierUser nextServer,
			Map<Long, MatchingStateByGameSingle> singleStateByGame,
			Map<Long, MatchingStateByGameTeam> teamStateByGame) {
		//引継ぎ先
		TakeOverMessageRatingGameMatchingServer req = new TakeOverMessageRatingGameMatchingServer();
		req.setSingleStateByGame(singleStateByGame);
		req.setTeamStateByGame(teamStateByGame);
		//送信
		Message reqM = Message.build(req)
				.packaging(req.createPackage(nextServer)).finish();
		return Response.success(Glb.getP2p().requestSync(reqM, nextServer));
	}

	public Map<Long,
			MatchingStateByGameSingle> getSingleStateByGame() {
		return singleStateByGame;
	}

	public void setSingleStateByGame(
			Map<Long, MatchingStateByGameSingle> singleStateByGame) {
		this.singleStateByGame = singleStateByGame;
	}

	public Map<Long, MatchingStateByGameTeam> getTeamStateByGame() {
		return teamStateByGame;
	}

	public void setTeamStateByGame(
			Map<Long, MatchingStateByGameTeam> teamStateByGame) {
		this.teamStateByGame = teamStateByGame;
	}
}
