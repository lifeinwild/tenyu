package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;

/**
 * マッチング申請を受け付けてマッチングして受付サーバにメッセージを送る。
 * このサーバーによってマッチングされた試合情報は客観になるが、
 * ここで扱っている情報は客観ではない。
 *
 * サーバーと同じタイムゾーンしか扱わない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameMatchingServer
		extends TakeOverServer<TakeOverMessageRatingGameMatchingServer> {
	/**
	 * 単独申請型の情報
	 */
	private Map<GameReference,
			MatchingStateByGameSingle> singleStateByGame = new ConcurrentHashMap<>();

	/**
	 * チーム申請型の情報
	 */
	private Map<GameReference,
			MatchingStateByGameTeam> teamStateByGame = new ConcurrentHashMap<>();

	/**
	 * 全タイムゾーンに対応するか
	 */
	private boolean allTimezone = false;

	public void setAllTimezone(boolean allTimezone) {
		this.allTimezone = allTimezone;
	}

	public boolean isAllTimezone() {
		return allTimezone;
	}

	@Override
	public List<NodeIdentifierUser> getCurrentServers(
			List<NodeIdentifierUser> onlineServerCandidates) {
		//各タイムゾーン毎の最前列ユーザーをサーバーとする
		HashSet<String> tz = new HashSet<String>();
		List<NodeIdentifierUser> r = new ArrayList<>();
		Glb.getObje().getUser(us -> {
			try {
				for (NodeIdentifierUser candidate : onlineServerCandidates) {
					User u = us.get(candidate.getUserId());
					if (u == null)
						continue;
					if (tz.contains(u.getTimezoneId())) {
						tz.add(u.getTimezoneId());
						r.add(candidate);
					}
				}
				return true;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
		return r;
	}

	@Override
	public String getModuleName() {
		return RatingGameMatchingServer.class.getSimpleName();
	}

	@Override
	public NodeIdentifierUser getNextServer(
			List<NodeIdentifierUser> currentServers) {
		//タイムゾーン毎にサーバーが稼働するので同時に稼働するサーバー数が2以上になるのでオーバーライドする
		//自分と同じタイムゾーンのサーバーを引継ぎ先とする
		//currentServersが自分を除いて作られたサーバー一覧である前提
		String myTz = Glb.getMiddle().getMe().getTimezoneId();
		return Glb.getObje().readRet(txn -> {
			try {
				UserStore us = new UserStore(txn);
				for (NodeIdentifierUser server : currentServers) {
					User u = us.get(server.getUserId());
					if (u == null)
						continue;
					if (u.getTimezoneId().equals(myTz))
						return server;
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
			return null;
		});
	}

	public MatchingStateByGameSingle getOrCreateSingle(GameReference ref) {
		if (SocialityStore.isBanStatic(NodeType.RATINGGAME, ref.getGameId())) {
			Glb.getLogger().warn("banned game", new Exception());
			return null;
		}

		MatchingStateByGameSingle state = singleStateByGame.get(ref);
		if (state == null) {
			state = new MatchingStateByGameSingle();
			setupState(state, ref);
			singleStateByGame.put(ref, state);
		}
		return state;
	}

	private void setupState(AbstractMatchingStateByGame state,
			GameReference ref) {
		state.setGameRef(ref);
		if (!allTimezone) {
			String tz = Glb.getMiddle().getMe().getTimezoneId();
			if (tz != null)
				state.setTimezoneId(tz);
		}
	}

	public MatchingStateByGameTeam getOrCreateTeam(GameReference ref) {
		if (SocialityStore.isBanStatic(NodeType.RATINGGAME, ref.getGameId())) {
			Glb.getLogger().warn("banned game", new Exception());
			return null;
		}

		MatchingStateByGameTeam state = teamStateByGame.get(ref);
		if (state == null) {
			state = new MatchingStateByGameTeam();
			setupState(state, ref);
			teamStateByGame.put(ref, state);
		}
		return state;
	}

	@Override
	public List<NodeIdentifierUser> getServerCandidates() {
		return Glb.getObje()
				.getRole(rs -> rs.getByName(getModuleName()).getAdminNodes());
	}

	@Override
	public void registerToOnlineChecker() {
		Glb.getMiddle().getOnlineChecker().register(getModuleName(),
				new OnlineCheckerFuncs(() -> checkAndStartOrStop(),
						() -> getServerCandidates()));
	}

	@Override
	public boolean sendInheritingMessage(NodeIdentifierUser nextServer) {
		return TakeOverMessageRatingGameMatchingServer.send(nextServer,
				singleStateByGame, teamStateByGame);
	}

	@Override
	public void takeover(TakeOverMessageRatingGameMatchingServer message) {
		singleStateByGame = message.getSingleStateByGame();
		teamStateByGame = message.getTeamStateByGame();
	}

}
