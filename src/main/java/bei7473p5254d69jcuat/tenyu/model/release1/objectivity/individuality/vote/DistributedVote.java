package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.vote.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 分散合意
 * 客観に登録された後、指定された日時が来たら実行される。
 *
 * 分散合意によって書き込まれる情報にUserRightMessageの反映処理は依存してはならない。
 * 分散合意は任意のタイミングで客観を更新する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class DistributedVote extends IndividualityObject implements DistributedVoteDBI {
	public static final int choicesMax = 200;

	public static final int scheduleMax = 50;

	/**
	 * 必ず偶数分の10秒に開始する
	 */
	public static final int startSecond = 10;

	/**
	 * 選択肢一覧
	 * このリストの番号が選択肢ID
	 */
	private List<DistributedVoteChoice> choices = new ArrayList<>();

	/**
	 * 論理削除フラグ
	 */
	protected boolean enable = true;

	/**
	 * cronの形式で記述された分散合意のスケジュール
	 * 必ず偶数分の10秒に開始する。
	 */
	protected String schedule;

	/**
	 * 持続型か
	 * falseなら1回行われたら削除される
	 */
	protected boolean sustainable = true;

	/**
	 * 内部システムが自動作成したものか
	 * 全体運営者等の操作によって開始されたものならfalse
	 */
	protected boolean system = false;

	/**
	 * 投票形式のタイプ
	 * 関連するコード、GUI等はPowerVoteにのみ対応すればいい
	 */
	protected VoteType type = VoteType.POWER_VOTE;

	/**
	 * @param choiceId	選択肢ID
	 * @return	選択肢。無ければnull
	 */
	public DistributedVoteChoice get(Integer choiceId) {
		return choices.get(choiceId);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return new ArrayList<>();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return new ArrayList<>();
	}

	public List<DistributedVoteChoice> getChoices() {
		return choices;
	}

	public String getSchedule() {
		return schedule;
	}

	@Override
	public List<Long> getSpecialMainAdministratorIds() {
		List<Long> r = new ArrayList<>();
		r.add(IdObjectDBI.getSystemId());
		r.add(IdObjectDBI.getVoteId());
		return r;
	}

	@Override
	public List<Long> getSpecialRegistererIds() {
		List<Long> r = new ArrayList<>();
		r.add(IdObjectDBI.getSystemId());
		r.add(IdObjectDBI.getVoteId());
		return r;
	}

	public VoteType getType() {
		return type;
	}

	public boolean isEnable() {
		return enable;
	}

	@Override
	public boolean isRestrictedInSpecialIdAdministrator() {
		return true;
	}

	@Override
	public boolean isRestrictedInSpecialIdRegisterer() {
		return true;
	}

	public boolean isSustainable() {
		return sustainable;
	}

	public boolean isSystem() {
		return system;
	}

	/**
	 * @param c	選択肢一覧に加える選択肢
	 * @return	失敗したらnull
	 */
	public Integer put(DistributedVoteChoice c) {
		if (!choices.add(c))
			return null;
		return choices.indexOf(c);
	}

	public void setChoices(List<DistributedVoteChoice> choices) {
		this.choices = choices;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public void setSustainable(boolean sustainable) {
		this.sustainable = sustainable;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public void setType(VoteType type) {
		this.type = type;
	}

	protected boolean validateAtCommonIndividualityObjectConcrete(ValidationResult r) {
		boolean b = true;
		if (choices == null || choices.size() == 0) {
			r.add(Lang.DISTRIBUTEDVOTE_CHOICES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (choices.size() > choicesMax) {
				r.add(Lang.DISTRIBUTEDVOTE_CHOICES, Lang.ERROR_TOO_BIG);
				b = false;
			} else {

			}
		}
		if (type == null) {
			r.add(Lang.DISTRIBUTEDVOTE_TYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (schedule == null || schedule.length() == 0) {
			r.add(Lang.DISTRIBUTEDVOTE_SCHEDULE, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (schedule.length() > scheduleMax) {
				r.add(Lang.DISTRIBUTEDVOTE_SCHEDULE, Lang.ERROR_TOO_BIG);
				b = false;
			} else {
				if (!org.quartz.CronExpression.isValidExpression(schedule)) {
					r.add(Lang.DISTRIBUTEDVOTE_SCHEDULE, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIndividualityObjectConcrete(r)) {
			b = false;
		} else {
			for (DistributedVoteChoice c : choices) {
				if (!c.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof DistributedVote)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		DistributedVote old2 = (DistributedVote) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(isSystem(), old2.isSystem())) {
			r.add(Lang.DISTRIBUTEDVOTE_SYSTEM, Lang.ERROR_UNALTERABLE,
					"system=" + isSystem() + " oldSystem=" + old2.isSystem());
			b = false;
		}
		if (Glb.getUtil().notEqual(getType(), old2.getType())) {
			r.add(Lang.DISTRIBUTEDVOTE_TYPE, Lang.ERROR_UNALTERABLE,
					"type=" + getType() + " oldType=" + old2.getType());
			b = false;
		}

		//systemな場合のみ選択肢を変更可能
		if (!isSystem()) {
			if (Glb.getUtil().notEqual(getChoices(), old2.getChoices())) {
				r.add(Lang.DISTRIBUTEDVOTE_CHOICES, Lang.ERROR_UNALTERABLE);
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIndividualityObjectConcrete(r)) {
			b = false;
		} else {
			for (DistributedVoteChoice c : choices) {
				if (!c.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (choices != null) {
			for (DistributedVoteChoice c : choices) {
				if (!c.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	public static enum VoteType {
		/**
		 * 複数の選択肢が与えられ1.0を分配するタイプの投票。
		 * 分配投票とでも呼ぶか。
		 * たぶん分配投票1種あれば他の投票タイプは要らない。
		 *
		 * なおアンケート機能がこのようなPowerVote的なただ1種類の方法だけで
		 * 十分であるというアイデアは昔Kさんから聞いたアイデア。
		 */
		POWER_VOTE;
	}

	@Override
	public DistributedVoteGui getGui(String guiName, String cssIdPrefix) {
		return new DistributedVoteGui(guiName, cssIdPrefix);
	}

	@Override
	public DistributedVoteStore getStore(Transaction txn) {
		return new DistributedVoteStore(txn);
	}


	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.DISTRIBUTED_VOTE;
	}


}