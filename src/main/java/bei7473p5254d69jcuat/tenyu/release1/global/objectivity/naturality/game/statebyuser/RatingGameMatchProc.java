package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import jetbrains.exodus.env.*;

/**
 * 試合結果をレーティングに反映する
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class RatingGameMatchProc extends DelayRun
		implements RatingGameMatchProcDBI {
	/**
	 * 試合情報のID
	 */
	private Long ratingGameMatchId;

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();//システムが作る
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();//システムが削除
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();//更新は想定されない
	}

	public Long getRatingGameMatchId() {
		return ratingGameMatchId;
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		return IdObjectDBI.getNullId();
	}

	@Override
	public Long getSpecialRegistererId() {
		return IdObjectDBI.getSystemId();
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	@Override
	public boolean run(Transaction txn) throws Exception {
		// TODO SME
		return false;
	}

	public void setRatingGameMatchId(Long ratingGameMatchId) {
		this.ratingGameMatchId = ratingGameMatchId;
	}

	private final boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (ratingGameMatchId == null) {
			vr.add(Lang.RATINGGAMEMATCHPROC_MATCHREFERENCE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(ratingGameMatchId)) {
				vr.add(Lang.RATINGGAMEMATCHPROC_MATCHREFERENCE, Lang.ERROR_INVALID);
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateDelayRunConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	protected boolean validateAtUpdateChangeDelayRunConcrete(ValidationResult r,
			Object old) {
		if (!(old instanceof RatingGameMatchProc)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		RatingGameMatchProc old2 = (RatingGameMatchProc) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getRatingGameMatchId(),
				old2.getRatingGameMatchId())) {
			r.add(Lang.RATINGGAMEMATCHPROC_MATCHREFERENCE,
					Lang.ERROR_UNALTERABLE,
					"ratingGameMatchId=" + getRatingGameMatchId()
							+ " oldRatingGameMatchId="
							+ old2.getRatingGameMatchId());
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateDelayRunConcrete(ValidationResult r) {
		return validateAtCommon(r);
	}

	@Override
	public boolean validateReferenceDelayRunConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		RatingGameMatchStore s = new RatingGameMatchStore(txn);
		if (s.get(ratingGameMatchId) == null) {
			r.add(Lang.RATINGGAMEMATCHPROC_MATCHREFERENCE,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
		}
		return b;
	}

}
