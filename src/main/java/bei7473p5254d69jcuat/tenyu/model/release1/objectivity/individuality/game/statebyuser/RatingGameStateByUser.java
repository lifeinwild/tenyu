package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.statebyuser;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.item.instance.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.gameplay.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * レーティングゲームタイトル毎のユーザー毎の状態。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameStateByUser extends AdministratedObject
		implements RatingGameStateByUserI {

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

	/**
	 * このユーザーのこのゲームにおける所持装備一覧
	 */
	protected List<RatingGameEquipmentInstance> equipments = new ArrayList<>();

	/**
	 * このユーザーがこのゲームに支払った合計額
	 */
	protected long payAmount;

	public RatingGameStateByUser() {
	}

	public RatingGameStateByUser(Long ratingGameId, Long ownerUserId) {
		this.ratingGameId = ratingGameId;
		setMainAdministratorUserId(ownerUserId);
		setRegistererUserId(ownerUserId);
	}

	/**
	 * この状態を所持するユーザー
	 */
	public Long getOwnerUserId() {
		return getMainAdministratorUserId();
	}

	public void setOwnerUserId(Long userId) {
		setMainAdministratorUserId(userId);
	}

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

	public static final int equipmentMax = 1000 * 100;

	public static final int stateMax = 1000 * 100;

	private final boolean validateAtCommon(ValidationResult r) {
		boolean b = true;
		if (teamRating < 0) {
			r.add(Lang.RATINGGAME_STATEBYUSER_TEAM_RATING, Lang.ERROR_INVALID);
			b = false;
		}
		if (singleRating < 0) {
			r.add(Lang.RATINGGAME_STATEBYUSER_SINGLE_RATING,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (matchCountSingle < 0) {
			r.add(Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_SINGLE,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (matchCountTeam < 0) {
			r.add(Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_TEAM,
					Lang.ERROR_INVALID);
			b = false;
		}
		if (payAmount < 0) {
			r.add(Lang.RATINGGAME_STATEBYUSER_PAYAMOUNT, Lang.ERROR_INVALID);
			b = false;
		}
		if (equipments == null) {
			r.add(Lang.RATINGGAME_STATEBYUSER_EQUIPMENTS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (equipments.size() > equipmentMax) {
				r.add(Lang.RATINGGAME_STATEBYUSER_EQUIPMENTS,
						Lang.ERROR_TOO_MANY);
				b = false;
			}
		}
		if (ratingGameId == null) {
			r.add(Lang.RATINGGAME_STATEBYUSER_RATINGGAME_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(ratingGameId)) {
				r.add(Lang.RATINGGAME_STATEBYUSER_RATINGGAME_ID,
						Lang.ERROR_INVALID, "ratingGameId=" + ratingGameId);
				b = false;
			}
		}
		return b;
	}

	@Override
	public final boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}

		if (equipments != null) {
			for (RatingGameEquipmentInstance e : equipments) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}

		if (teamRating != initialRating) {
			r.add(Lang.RATINGGAME_STATEBYUSER_TEAM_RATING,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (singleRating != initialRating) {
			r.add(Lang.RATINGGAME_STATEBYUSER_SINGLE_RATING,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (matchCountSingle != initialMatchCount) {
			r.add(Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_SINGLE,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		if (matchCountTeam != initialMatchCount) {
			r.add(Lang.RATINGGAME_STATEBYUSER_MATCHCOUNT_TEAM,
					Lang.ERROR_NOT_DEFAULT);
			b = false;
		}
		return b;
	}

	private Long ratingGameId;

	public Long getRatingGameId() {
		return ratingGameId;
	}

	public void setRatingGameId(Long ratingGameId) {
		this.ratingGameId = ratingGameId;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof RatingGameStateByUser)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		RatingGameStateByUser old2 = (RatingGameStateByUser) old;
		boolean b = true;
		if (Glb.getUtil().notEqual(ratingGameId, old2.getRatingGameId())) {
			r.add(Lang.ID, Lang.ERROR_UNALTERABLE,
					"ratingGameId=" + getRatingGameId() + " old.ratingGameId="
							+ old2.getRatingGameId());
			b = false;
		}

		if (Glb.getUtil().notEqual(getOwnerUserId(), old2.getOwnerUserId())) {
			r.add(Lang.USER_ID, Lang.ERROR_UNALTERABLE, "userId="
					+ getOwnerUserId() + " oldUserId=" + old2.getOwnerUserId());
			b = false;
		}
		return b;
	}

	@Override
	public final boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		}
		if (equipments != null) {
			for (RatingGameEquipmentInstance e : equipments) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}

		return b;
	}

	@Override
	public final boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		RatingGameStore rgs = new RatingGameStore(txn);
		if (rgs.get(ratingGameId) == null) {
			r.add(Lang.RATINGGAME_STATEBYUSER_RATINGGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"ratingGameId=" + ratingGameId);
			b = false;
		}
		//ownerUserIdは抽象クラスでチェックされている

		for (RatingGameEquipmentInstance e : equipments) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		return b;
	}

	@Override
	public RatingGameStateByUserGui getGui(String guiName, String cssIdPrefix) {
		return new RatingGameStateByUserGui(guiName, cssIdPrefix);
	}

	@Override
	public RatingGameStateByUserStore getStore(Transaction txn) {
		return new RatingGameStateByUserStore(txn);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//最初のデータを書き込むときに自動作成される
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(getOwnerUserId());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//一定の手続きが行われた場合に自動的に更新される。
	}

	public RatingGame getGame() {
		return Glb.getObje().getRatingGame(s -> s.get(ratingGameId));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((equipments == null) ? 0 : equipments.hashCode());
		result = prime * result + matchCountSingle;
		result = prime * result + matchCountTeam;
		result = prime * result + (int) (payAmount ^ (payAmount >>> 32));
		result = prime * result
				+ ((ratingGameId == null) ? 0 : ratingGameId.hashCode());
		result = prime * result + singleRating;
		result = prime * result + teamRating;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RatingGameStateByUser other = (RatingGameStateByUser) obj;
		if (equipments == null) {
			if (other.equipments != null)
				return false;
		} else if (!equipments.equals(other.equipments))
			return false;
		if (matchCountSingle != other.matchCountSingle)
			return false;
		if (matchCountTeam != other.matchCountTeam)
			return false;
		if (payAmount != other.payAmount)
			return false;
		if (ratingGameId == null) {
			if (other.ratingGameId != null)
				return false;
		} else if (!ratingGameId.equals(other.ratingGameId))
			return false;
		if (singleRating != other.singleRating)
			return false;
		if (teamRating != other.teamRating)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RatingGameStateByUser [matchCountSingle=" + matchCountSingle
				+ ", matchCountTeam=" + matchCountTeam + ", singleRating="
				+ singleRating + ", teamRating=" + teamRating + ", equipments="
				+ equipments + ", payAmount=" + payAmount + ", ratingGameId="
				+ ratingGameId + "]";
	}

	public List<RatingGameEquipmentInstance> getEquipments() {
		return equipments;
	}

	public void setEquipments(List<RatingGameEquipmentInstance> equipments) {
		this.equipments = equipments;
	}

	public long getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(long payAmount) {
		this.payAmount = payAmount;
	}
	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.RATING_GAME_STATE_BY_USER;
	}

}
