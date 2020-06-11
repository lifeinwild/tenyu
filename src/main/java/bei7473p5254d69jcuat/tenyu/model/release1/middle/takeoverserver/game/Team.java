package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 試合情報RatingGameMatchが客観に登録されたら、
 * そこに含められたチームは更新されてはいけない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Team implements ValidatableI {
	public static final int nameMax = IndividualityObjectI.getNameMaxStatic()
			+ 50;

	public static final int passwordMax = 50;

	public static byte[] passwordToHash(String password) {
		if (password == null)
			return null;
		return Glb.getUtil()
				.hashSecure(password.getBytes(Glb.getConst().getCharsetNio()));
	}

	/**
	 * チームが揃ったタイミングでの
	 * チームメンバーのこのゲームタイトルにおける平均レーティング
	 */
	private int aveRating;

	/**
	 * どのゲームか
	 */
	private Long ratingGameId;

	/**
	 * メンバー一覧
	 */
	private List<NodeIdentifierUser> members = new CopyOnWriteArrayList<>();

	/**
	 * チーム名
	 */
	private String name;

	/**
	 * パスワードのハッシュ値
	 */
	private byte[] passwordHash;

	/**
	 * チーム管理者。メンバーの追放や募集締め切り等の操作を行う。
	 * membersが1以上ならチーム管理者はmembersに含まれる事が保証される。
	 * チーム登録した人が最初の管理者になり、その後脱退や追加等で変わっていく可能性がある。
	 */
	private NodeIdentifierUser admin;

	/**
	 * チームの段階
	 */
	private Team.TeamState state = TeamState.ACCEPTING;
	/**
	 * どのチームクラスか
	 */
	private int teamClassId = -1;
	private MatchingType type;

	public Team() {
	}

	public Team(Long ratingGameId, String teamName, String password,
			NodeIdentifierUser registerer, MatchingType type, int teamClassId) {
		this.ratingGameId = ratingGameId;
		this.name = teamName;
		this.passwordHash = password == null ? null : passwordToHash(password);
		this.admin = registerer;
		members.add(registerer);
		this.type = type;
		this.teamClassId = teamClassId;
	}

	/**
	 * TODO このメソッドがかなり遅い。
	 * 原因はjvisualvmの結果からするとDBやシリアライザにある。
	 * 解決策は思いつかないが、get系なのでキャッシュで解消される可能性がある。
	 *
	 *
	 * @param node
	 * @param passwordHash	生パスワードをネットワークに流す必要性が無いのでそのハッシュ値のみをやり取りする
	 * @return
	 */
	public synchronized boolean addMember(NodeIdentifierUser node,
			byte[] passwordHash) {
		if (this.passwordHash != null) {
			if (passwordHash == null)
				return false;
			if (!Arrays.equals(this.passwordHash, passwordHash)) {
				return false;
			}
		}

		if (node == null)
			return false;
		if (state != TeamState.ACCEPTING)
			return false;

		if (isFull()) {
			return false;
		}

		if (getMemberUserIds().contains(node.getUserId()))
			return false;

		boolean dbCheck = Glb.getObje().readRet(txn -> {
			try {
				RatingGame game = getGame();
				if (game == null)
					return false;
				if (members.size() > game.getTeamClass(teamClassId)
						.getMemberCount())
					return false;

				TeamClass tc = game.getTeamClass(teamClassId);
				if (members.size() > tc.getMemberCount())
					return false;

				//ゲームからブロックされているユーザーを拒否する
				SocialityStore sos = new SocialityStore(txn);
				if (sos.isBlock(StoreNameObjectivity.RATING_GAME, game.getId(),
						node.getUserId()))
					return false;
				if (sos.isBan(StoreNameObjectivity.USER, node.getUserId())) {
					return false;
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return false;
			}
			return true;
		});
		if (!dbCheck)
			return false;

		if (!members.add(node))
			return false;

		//チーム管理者がメンバー一覧に無い場合、管理者にする
		if (members.size() == 0 || admin == null || !members.contains(admin)) {
			admin = node;
		}

		return true;
	}

	public synchronized int calculateAveTeamRating() {
		if (members == null || members.size() == 0)
			return 0;
		int total = 0;
		RatingGame game = getGame();
		for (NodeIdentifierUser node : members) {
			try {
				RatingGameStateByUser stateByUser = Glb.getObje()
						.getRatingGameStateByUser(
								rgsbus -> rgsbus.getByGameIdUserId(game.getId(),
										node.getUserId()));

				//このユーザーにとってこのゲームでの1試合目はstateが無いがレーティング0で計算すれば良いのでこれで良い
				if (stateByUser == null)
					continue;

				if (type == MatchingType.SINGLE) {
					total += stateByUser.getSingleRating();
				} else {
					total += stateByUser.getTeamRating();
				}
			} catch (Exception e) {
				Glb.getLogger().error(
						"userId=" + node + " ratingGameId=" + ratingGameId, e);
				continue;
			}
		}
		int r = total / members.size();
		return r;
	}

	public int getAveRating() {
		return aveRating;
	}

	public RatingGame getGame() {
		return Glb.getObje().getRatingGame(s -> s.get(ratingGameId));
	}

	public Long getRatingGameId() {
		return ratingGameId;
	}

	public List<NodeIdentifierUser> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public String getName() {
		return name;
	}

	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public TeamReference getReference() {
		TeamReference r = new TeamReference();
		r.setRatingGameId(ratingGameId);
		r.setName(name);
		r.setTeamClassId(teamClassId);
		return r;
	}

	public NodeIdentifierUser getAdmin() {
		return admin;
	}

	public Team.TeamState getState() {
		return state;
	}

	public int getTeamClassId() {
		return teamClassId;
	}

	public MatchingType getType() {
		return type;
	}

	public synchronized boolean isFull() {
		return getMembersMax() <= members.size();
	}

	public int getMembersMax() {
		RatingGame rg = getGame();
		if (rg == null) {
			Glb.getLogger()
					.warn(new IllegalStateException("rating game is null."));
			return 0;
		}
		TeamClass tc = rg.getTeamClass(teamClassId);
		if (tc == null) {
			Glb.getLogger()
					.warn(new IllegalStateException("team class is null."));
			return 0;
		}
		return tc.getMemberCount();
	}

	/**
	 * マッチングされた時に呼ぶ
	 */
	public synchronized void matched() {
		state = TeamState.MATCHED;
	}

	/**
	 * メンバーが揃った時など受付停止するときに呼ぶ
	 */
	public synchronized boolean finish() {
		//フルであることを要求する。
		//こうしないと最大人数を揃えずに試合がスタートする可能性がある
		//仕様上それを認める事もありかもしれないが、現状禁止している。
		if (!isFull()) {
			return false;
		}

		aveRating = calculateAveTeamRating();
		state = TeamState.CLOSED;
		return true;
	}

	public String getAdminName() {
		User u = Glb.getObje().getUser(us -> us.get(admin.getUserId()));
		if (u == null)
			return null;
		return u.getName();
	}

	public synchronized boolean removeMember(NodeIdentifierUser node) {
		if (node == null)
			return false;
		if (state != TeamState.ACCEPTING)
			return false;
		if (members.size() == 0)
			return false;
		//チーム管理者が脱退した場合、他のメンバーにチーム管理者の立場を譲る
		if (node.equals(admin)) {
			for (NodeIdentifierUser member : members) {
				if (!member.equals(admin)) {
					admin = member;
					break;
				}
			}
		}
		if (!members.remove(node))
			return false;
		return true;
	}

	public void setAveRating(int aveRating) {
		this.aveRating = aveRating;
	}

	public void setMembers(List<NodeIdentifierUser> members) {
		this.members = members;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setAdmin(NodeIdentifierUser admin) {
		this.admin = admin;
	}

	public void setState(Team.TeamState state) {
		this.state = state;
	}

	public void setTeamClassId(int teamClassId) {
		this.teamClassId = teamClassId;
	}

	public void setType(MatchingType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Team [aveRating=" + aveRating + ", ratingGameId=" + ratingGameId
				+ ", members=" + members + ", name=" + name + ", passwordHash="
				+ Arrays.toString(passwordHash) + ", admin=" + admin
				+ ", state=" + state + ", teamClassId=" + teamClassId
				+ ", type=" + type + "]";
	}

	public List<Long> getMemberUserIds() {
		List<Long> r = new ArrayList<>();
		for (NodeIdentifierUser node : members) {
			r.add(node.getUserId());
		}
		return r;
	}

	private boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (type == null) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_TYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (state == null) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_STATE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (ratingGameId == null) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_RATINGGAME_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandard(ratingGameId)) {
				vr.add(Lang.GAMEPLAY_MATCHING_TEAM_RATINGGAME_ID,
						Lang.ERROR_INVALID, "ratingGameId=" + ratingGameId);
				b = false;
			}
		}
		if (teamClassId == -1) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_CLASSNAME, Lang.ERROR_EMPTY);
			b = false;
		}

		if (members == null) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_MEMBERS, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (members.size() > RatingGame.teamMax) {
				vr.add(Lang.GAMEPLAY_MATCHING_TEAM_MEMBERS,
						Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				if (!Model.validateIdStandardNotSpecialId(getMemberUserIds())) {
					vr.add(Lang.GAMEPLAY_MATCHING_TEAM_MEMBERS,
							Lang.ERROR_INVALID);
					b = false;
				}
			}
		}
		if (name == null || name.length() == 0) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_NAME, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IndividualityObject.validateName(
					Lang.GAMEPLAY_MATCHING_TEAM_NAME, name, vr, nameMax)) {
				b = false;
			}
		}
		if (admin == null) {
			vr.add(Lang.GAMEPLAY_MATCHING_TEAM_ADMIN, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!Model.validateIdStandardNotSpecialId(admin.getUserId())) {
				vr.add(Lang.GAMEPLAY_MATCHING_TEAM_ADMIN, Lang.ERROR_INVALID);
				b = false;
			}
		}
		if (passwordHash == null || passwordHash.length == 0) {
			//パスワードは無しでもいい
		} else {
			if (passwordHash.length != Glb.getConst().getHashSize()) {
				vr.add(Lang.GAMEPLAY_MATCHING_TEAM_PASSWORD,
						Lang.ERROR_INVALID);
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
		RatingGame rg = getGame();
		if (rg == null) {
			r.add(Lang.GAMEPLAY_MATCHING_TEAM_RATINGGAME_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE,
					"ratingGameId=" + ratingGameId);
			b = false;
		} else {
			TeamClass tc = rg.getTeamClass(teamClassId);
			if (tc == null) {
				r.add(Lang.GAMEPLAY_MATCHING_TEAM_TEAMCLASSID,
						Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"teamClassId=" + teamClassId);
				b = false;
			}
		}

		UserStore us = new UserStore(txn);
		for (NodeIdentifierUser node : members) {
			if (us.get(node.getUserId()) == null) {
				r.add(Lang.GAMEPLAY_MATCHING_TEAM_MEMBER,
						Lang.ERROR_DB_NOTFOUND_REFERENCE, "node=" + node);
				b = false;
				break;
			}
		}
		if (us.get(admin.getUserId()) == null) {
			r.add(Lang.GAMEPLAY_MATCHING_TEAM_ADMIN,
					Lang.ERROR_DB_NOTFOUND_REFERENCE, "admin=" + admin);
			b = false;
		}
		return b;
	}

	public synchronized boolean validateSync(ValidationResult vr) {
		return validateAtCommon(vr);
	}

	public static enum MatchingType {
		/**
		 * 単独申請型
		 */
		SINGLE,
		/**
		 * チーム申請型
		 */
		TEAM
	}

	public static enum TeamState {
		/**
		 * メンバー受付中
		 * 初期状態
		 */
		ACCEPTING,
		/**
		 * メンバー受付停止
		 */
		CLOSED,
		/**
		 * マッチングされた
		 * 対戦相手が決まった。
		 */
		MATCHED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((admin == null) ? 0 : admin.hashCode());
		result = prime * result + aveRating;
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(passwordHash);
		result = prime * result
				+ ((ratingGameId == null) ? 0 : ratingGameId.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + teamClassId;
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
		Team other = (Team) obj;
		if (admin == null) {
			if (other.admin != null)
				return false;
		} else if (!admin.equals(other.admin))
			return false;
		if (aveRating != other.aveRating)
			return false;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(passwordHash, other.passwordHash))
			return false;
		if (ratingGameId == null) {
			if (other.ratingGameId != null)
				return false;
		} else if (!ratingGameId.equals(other.ratingGameId))
			return false;
		if (state != other.state)
			return false;
		if (teamClassId != other.teamClassId)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public void setRatingGameId(Long ratingGameId) {
		this.ratingGameId = ratingGameId;
	}

}