package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.GameStateByUser.*;
import jetbrains.exodus.env.*;

/**
 * todoから設計の経緯を抜粋
 * なぜgameIdを共通化してtypeで区別していくか。
 *
 * gameIdの問題もDB周りならではのもので、本来ならStaticGame,RatingGameにインターフェースか抽象クラスで
 * 抽象的な共通性を与えて、そのオブジェクトをメンバー変数に持つという設計にするのだろうが、
 * DB周りであるからそれができず、Long idをメンバー変数に持つ事になり、
 * そのLong idがStaticGame idなのかRatingGame idなのかが分からないという問題である。
 * この、DB周りであるがゆえの、言語機構が本来解決するはずの問題を扱わなければならないという問題は、
 * できるだけ局所的に解決すべきだろうと思う。だからStaticGameEquipment等と派生していくのはまずい。
 * 局所的解決の方法はenumを持つ事だろう。
 * ストアではenumを参照してサブインデックスのストア名を決定する。
 *
 */
public class GameReference implements Storable {
	/**
	 * このゲームのID
	 */
	protected Long gameId;

	protected GameType type;

	@SuppressWarnings("unused")
	private GameReference() {
	}

	public GameReference(Long gameId, GameType type) {
		this.gameId = gameId;
		this.type = type;
	}

	public AbstractGame getGame() {
		return GameReference.getGameStatic(type, gameId);
	}

	public AbstractGame getGame(Transaction txn) {
		return GameReference.getGameStatic(type, gameId, txn);
	}

	/**
	 * @return	このアイテムのゲームの管理者
	 */
	public Long getGameAdministratorUserId() {
		AbstractGame o = getGame();
		if (o == null)
			return null;
		return o.getMainAdministratorUserId();
	}

	public Long getGameId() {
		return gameId;
	}

	public GameType getType() {
		return type;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public void setType(GameType type) {
		this.type = type;
	}

	public static AbstractGame getGameStatic(GameStateByUser.GameType type,
			Long gameId) {
		if (type == null || gameId == null)
			return null;
		AbstractGame s = null;
		if (type == GameStateByUser.GameType.STATICGAME) {
			s = Glb.getObje().getStaticGame(sgs -> sgs.get(gameId));
		} else if (type == GameStateByUser.GameType.RATINGGAME) {
			s = Glb.getObje().getRatingGame(rgs -> rgs.get(gameId));
		}
		return s;
	}

	public static AbstractGame getGameStatic(GameStateByUser.GameType type,
			Long gameId, Transaction txn) {
		if (type == null || gameId == null)
			return null;
		AbstractGame g = null;
		try {
			if (type == GameStateByUser.GameType.STATICGAME) {
				g = new StaticGameStore(txn).get(gameId);
			} else if (type == GameStateByUser.GameType.RATINGGAME) {
				g = new RatingGameStore(txn).get(gameId);
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return g;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameReference other = (GameReference) obj;
		if (gameId == null) {
			if (other.gameId != null)
				return false;
		} else if (!gameId.equals(other.gameId))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "gameId=" + gameId + " type=" + type;
	}

	private boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (gameId == null) {
			r.add(Lang.GAME_REFERENCE_GAME_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(gameId)) {
				r.add(Lang.GAME_REFERENCE_GAME_ID, Lang.ERROR_INVALID,
						toString());
				b = false;
			}
		}
		if (type == null) {
			r.add(Lang.GAME_REFERENCE_TYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (getGame(txn) == null) {
			r.add(Lang.GAME_REFERENCE, Lang.ERROR_DB_NOTFOUND_REFERENCE,
					toString());
			b = false;
		}
		return b;
	}
}