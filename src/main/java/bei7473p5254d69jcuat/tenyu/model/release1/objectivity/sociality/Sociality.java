package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import java.nio.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.user.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 社会性
 * 相互評価フローネットワークのノード
 * @author exceptiontenyu@gmail.com
 *
 */
public class Sociality extends AdministratedObject
		implements Trader, SocialityDBI {
	public static final int balckListMax = 2000;

	public static byte[] createIndividualityObjectId(byte nodeTypeId,
			long individualityObjectId) {
		//ここに値が設置される
		byte[] nodeId = new byte[NodeType.getIdSize()
				+ IdObjectDBI.getIdSize()];
		//頭にNodeTypeを特定する情報が設置される
		nodeId[0] = nodeTypeId;
		//次にIdObjectのidを特定する情報が設置される
		ByteBuffer buf = ByteBuffer.wrap(nodeId, NodeType.getIdSize(),
				IdObjectDBI.getIdSize());
		buf.putLong(individualityObjectId);
		return buf.array();
	}

	/**
	 * {@link Sociality#getId()} ≠ {@link Sociality#individualityObjectConcreteId}
	 * {@link Sociality#individualityObjectConcreteId} == {@link IndividualityObject#getId()}
	 *
	 * @param nodeTypeId	NodeTypeのID
	 * @param individualityObjectId		IdObjectのID
	 * @return				individualityObjectId
	 */
	public static byte[] createIndividualityObjectId(NodeType type,
			long individualityObjectId) {
		return createIndividualityObjectId(type.getId(), individualityObjectId);
	}

	/*
	@Override
	public Long getSpecialMainAdministratorId() {
		if (type == NodeType.COOPERATIVE_ACCOUNT
				|| type == NodeType.FLOWNETWORK_ABSTRACTNOMINAL
				|| type == NodeType.WEB)
			return IdObjectDBI.getNullId();
		return null;
	}
	*/

	/**
	 * @return	単独でBAN操作が可能なユーザーID一覧
	 */
	public static List<Long> getAdministratorUserIdBanSimple() {
		List<Long> r = new ArrayList<>();
		TenyuManagerList l = Glb.getObje().getCore().getManagerList();
		for (Long userId : l.getManagerIds()) {
			if (l.is51PerAdmin(userId)) {
				r.add(userId);
			}
		}
		return r;
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		//Socialityを作るというメッセージクラスは用意しない
		//他のIndividualityObjectを作成した時に同時に作成される
		return null;
	}

	public static int getEdgeCountMaxByNodeType(NodeType type) {
		switch (type) {
		case COOPERATIVE_ACCOUNT:
		case FLOWNETWORK_ABSTRACTNOMINAL:
			return EdgeManager.edgeMax;
		default:
			return 2000;
		}
	}

	public static long parseIndividualityObjectId(byte[] individualityObjectId) {
		return ByteBuffer.wrap(individualityObjectId, 1, Long.BYTES)
				.getLong(NodeType.getIdSize());
	}

	/**
	 * @param individualityObjectId	NodeTypeのid+オブジェクトのid
	 * @return			NodeTypeのid
	 */
	public static NodeType parseNodeType(byte[] individualityObjectId) {
		byte typeId = individualityObjectId[0];
		for (NodeType type : NodeType.values()) {
			if (type.getId() == typeId) {
				return type;
			}
		}
		Glb.getLogger().error("対応するNodeTypeが見つからない", new Exception());
		return null;
	}

	/**
	 * TenyuからのBAN
	 */
	protected boolean banned = false;

	/**
	 * ブラックリスト。フローが流れないし、様々なSociality関連機能が動作しない
	 */
	protected HashSet<Long> blackList = null;

	protected long createHistoryIndex;

	/**
	 * このオブジェクトからのエッジ
	 * SocialityId : Edge
	 */
	protected EdgeManager edgeManager = new EdgeManager();

	/**
	 * 共同主体からのフロー
	 */
	protected double flowFromCooperativeAccount;

	/**
	 * 個性系具象クラスID
	 */
	protected Long individualityObjectConcreteId;

	/**
	 * 対象とするモデルの種類
	 */
	protected NodeType type;

	/**
	 * 送受金したり
	 * 仮想通貨分配を受け取る
	 */
	protected Wallet wallet = new Wallet();

	@SuppressWarnings("unused")
	private Sociality() {
	}

	/**
	 * 新しく作成するとき使用するコンストラクタ
	 * DBから読み出された場合は、既にedgeManagerは設定済みのはずなので
	 * この処理は呼ばれないが問題無い。
	 *
	 * @param historyIndex
	 */
	public Sociality(long historyIndex) {
		edgeManager = new EdgeManager(historyIndex);
		createHistoryIndex = historyIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sociality other = (Sociality) obj;
		if (banned != other.banned)
			return false;
		if (blackList == null) {
			if (other.blackList != null)
				return false;
		} else if (!blackList.equals(other.blackList))
			return false;
		if (createHistoryIndex != other.createHistoryIndex)
			return false;
		if (edgeManager == null) {
			if (other.edgeManager != null)
				return false;
		} else if (!edgeManager.equals(other.edgeManager))
			return false;
		if (Double.doubleToLongBits(flowFromCooperativeAccount) != Double
				.doubleToLongBits(other.flowFromCooperativeAccount))
			return false;
		if (individualityObjectConcreteId == null) {
			if (other.individualityObjectConcreteId != null)
				return false;
		} else if (!individualityObjectConcreteId
				.equals(other.individualityObjectConcreteId))
			return false;
		if (type != other.type)
			return false;
		if (wallet == null) {
			if (other.wallet != null)
				return false;
		} else if (!wallet.equals(other.wallet))
			return false;
		return true;
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		List<Long> r = new ArrayList<>();
		r.add(mainAdministratorUserId);
		return r;
	}

	public List<Long> getAdministratorUserIdSimpleBAN() {
		List<Long> r = new ArrayList<>();
		//作者ユーザーの社会性をBANする事はできない
		if (getNodeType() == NodeType.USER
				&& getIndividualityObjectConcreteId() == Glb.getConst()
						.getAuthor().getId())
			return r;
		//51%全体運営者はあらゆるBANを簡単に行える
		r.addAll(Glb.getObje().getCore().getManagerList().get51PerAdminIds());
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministratorUserIdDelete();
	}

	public HashSet<Long> getBlackList() {
		return blackList;
	}

	public long getSubmitHistoryIndex() {
		return createHistoryIndex;
	}

	public EdgeManager getEdgeManager() {
		return edgeManager;
	}

	public double getFlowFromCooperativeAccount() {
		return flowFromCooperativeAccount;
	}

	@Override
	public Long getIndividualityObjectConcreteId() {
		return individualityObjectConcreteId;
	}

	/**
	 * {@link Sociality}は必ず１つの{@link IndividualityObject}と対応づく
	 * {@link Sociality#individualityObjectConcreteId}は一意ではなく
	 * 個性系具象クラスの識別子を加えると一意になる。
	 * @return	NodeTypeのid+オブジェクトのid
	 */
	public byte[] getIndividualityObjectId() {
		return createIndividualityObjectId(type.getId(), individualityObjectConcreteId);
	}

	@Override
	public NodeType getNodeType() {
		return type;
	}

	@Override
	public List<Long> getSpecialMainAdministratorIds() {
		List<Long> r = new ArrayList<>();
		if (type == null)
			return r;
		switch (type) {
		case COOPERATIVE_ACCOUNT:
			r.add(IdObjectDBI.getVoteId());
			break;
		case FLOWNETWORK_ABSTRACTNOMINAL:
		case WEB:
			r.add(IdObjectDBI.getNullId());
			break;
		case RATINGGAME:
		case STATICGAME:
		case USER:
		case AVATAR:
			break;
		default:
		}
		return r;
	}

	@Override
	public Long getSpecialRegistererId() {
		if (type == null)
			return null;
		switch (type) {
		case COOPERATIVE_ACCOUNT:
			return IdObjectDBI.getSystemId();
		case FLOWNETWORK_ABSTRACTNOMINAL:
			return IdObjectDBI.getVoteId();
		case WEB:
		case RATINGGAME:
		case STATICGAME:
		case USER:
		case AVATAR:
			break;
		default:
		}
		return null;
	}

	public NodeType getType() {
		return type;
	}

	public Wallet getWallet() {
		return wallet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (banned ? 1231 : 1237);
		result = prime * result
				+ ((blackList == null) ? 0 : blackList.hashCode());
		result = prime * result
				+ (int) (createHistoryIndex ^ (createHistoryIndex >>> 32));
		result = prime * result
				+ ((edgeManager == null) ? 0 : edgeManager.hashCode());
		long temp;
		temp = Double.doubleToLongBits(flowFromCooperativeAccount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((individualityObjectConcreteId == null) ? 0
				: individualityObjectConcreteId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((wallet == null) ? 0 : wallet.hashCode());
		return result;
	}

	public boolean isBanned() {
		return banned;
	}

	public void setBanned(boolean banned) {
		this.banned = banned;
	}

	public void setBlackList(HashSet<Long> blackList) {
		this.blackList = blackList;
	}

	public void setSubmitHistoryIndex(long createHistoryIndex) {
		this.createHistoryIndex = createHistoryIndex;
	}

	public void setEdgeManager(EdgeManager edgeManager) {
		this.edgeManager = edgeManager;
	}

	public void setFlowFromCooperativeAccount(
			double flowFromCooperativeAccount) {
		this.flowFromCooperativeAccount = flowFromCooperativeAccount;
	}

	public void setIndividualityObjectConcreteId(
			Long individualityObjectConcreteId) {
		this.individualityObjectConcreteId = individualityObjectConcreteId;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	/**
	 * エッジを追加修正する前にこれを呼ぶ必要がある
	 */
	public void setupForWritingEdge() {
		long increaseMax = Glb.getObje().getCore().getConfig()
				.getEdgeChangePaceIncreaseMaxDefault();
		long decreaseMax = Glb.getObje().getCore().getConfig()
				.getEdgeChangePaceDecreaseMaxDefault();
		long freePeriod = Glb.getObje().getCore().getConfig()
				.getEdgeChangePaceFreePeriodDefault();

		if (type == null) {
			Glb.getLogger().error("type is null", new IllegalStateException());
		} else {
			switch (type) {
			case FLOWNETWORK_ABSTRACTNOMINAL:
				double rate = Glb.getObje().getCore().getConfig()
						.getEdgeChangePaceAbstractNominal();
				increaseMax = (long) (Edge.weightMax * rate);
				decreaseMax = (long) -(Edge.weightMax * rate);
				//5日間のみ流量制限を受けない
				freePeriod = Glb.getObje().getCore().getConfig()
						.getHistoryIndexDayRough() * 5;
				break;
			default:
			}
		}

		edgeManager.init(increaseMax, decreaseMax, createHistoryIndex,
				freePeriod);
	}

	public void setWallet(Wallet wallet) {
		this.wallet = wallet;
	}

	private final boolean validateAtCommonAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (individualityObjectConcreteId == null) {
			r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!IdObject.validateIdStandardNotSpecialId(
					individualityObjectConcreteId)) {
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID,
						Lang.ERROR_INVALID);
				b = false;
			}
		}

		if (flowFromCooperativeAccount < 0) {
			r.add(Lang.SOCIALITY_FLOW_FROM_COOPERATIVEACCOUNT,
					Lang.ERROR_INVALID,
					"flowFromCooperativeAccount=" + flowFromCooperativeAccount);
			b = false;
		}

		if (type == null) {
			r.add(Lang.SOCIALITY_NODETYPE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (blackList != null) {
			if (blackList.size() > balckListMax) {
				r.add(Lang.SOCIALITY_BLACKLIST, Lang.ERROR_INVALID);
				b = false;
			} else {
				if (!IdObject.validateIdStandardNotSpecialId(blackList)) {
					r.add(Lang.SOCIALITY_BLACKLIST, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}

		if (edgeManager == null) {
			r.add(Lang.SOCIALITY_EDGES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			int edgeCount = edgeManager.getEdges().size();
			int edgeMax = getEdgeCountMaxByNodeType(type);
			if (edgeCount > edgeMax) {
				r.add(Lang.SOCIALITY_EDGES, Lang.ERROR_TOO_MANY,
						"edgeCount=" + edgeCount);
				b = false;
			}
		}

		if (wallet == null) {
			r.add(Lang.SOCIALITY_WALLET, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAdministratedObjectConcrete(r)) {
			b = false;
		} else {
			if (!wallet.validateAtCreate(r)) {
				b = false;
			}
		}

		if (edgeManager != null) {
			if (!edgeManager.validateAtCreate(r)) {
				b = false;
			}
		}

		if (banned) {
			r.add(Lang.SOCIALITY_BANNED, Lang.ERROR_SOCIALITY_BANNED);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAdministratedObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Sociality)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		Sociality old2 = (Sociality) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getIndividualityObjectConcreteId(),
				old2.getIndividualityObjectConcreteId())) {
			r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID, Lang.ERROR_UNALTERABLE,
					"individualityObjectConcreteId=" + getIndividualityObjectConcreteId()
							+ " oldIndividualityObjectConcreteId="
							+ old2.getIndividualityObjectConcreteId());
			b = false;
		}
		if (Glb.getUtil().notEqual(getNodeType(), old2.getNodeType())) {
			r.add(Lang.SOCIALITY_NODETYPE, Lang.ERROR_UNALTERABLE, "nodeType="
					+ getNodeType() + " oldNodeType=" + old2.getNodeType());
			b = false;
		}

		if (edgeManager != null) {
			if (!edgeManager.validateAtUpdateChange(r, old2.getEdgeManager())) {
				b = false;
			}
		}

		return b;
	}

	@Override
	protected final boolean validateAtUpdateAdministratedObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAdministratedObjectConcrete(r)) {
			b = false;
		} else {
			if (!wallet.validateAtUpdate(r)) {
				b = false;
			}
		}

		if (edgeManager != null) {
			if (!edgeManager.validateAtUpdate(r)) {
				b = false;
			}
		}

		return b;
	}

	@Override
	public boolean validateReferenceAdministratedObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;

		//対応オブジェクトがDB上に無い場合true
		boolean noDBIndividualityObject = false;

		IdObject dbIndividualityObject = null;
		switch (type) {
		case FLOWNETWORK_ABSTRACTNOMINAL:
			dbIndividualityObject = new FlowNetworkAbstractNominalStore(txn)
					.get(individualityObjectConcreteId);
			/*
			if (!IdObjectDBI.getNullId()
					.equals(mainAdministratorUserId)) {
				r.add(Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR, Lang.ERROR_INVALID);
			}
			*/
			break;
		case COOPERATIVE_ACCOUNT:
			//共同主体は管理者無しでIndividualityObjectもDBに記録されない
			noDBIndividualityObject = true;
			break;
		case RATINGGAME:
			dbIndividualityObject = new RatingGameStore(txn)
					.get(individualityObjectConcreteId);
			break;
		case STATICGAME:
			dbIndividualityObject = new StaticGameStore(txn)
					.get(individualityObjectConcreteId);
			break;
		case USER:
			dbIndividualityObject = new UserStore(txn).get(individualityObjectConcreteId);
			break;
		case WEB:
			dbIndividualityObject = new WebStore(txn).get(individualityObjectConcreteId);
			break;
		default:
			noDBIndividualityObject = true;
			b = false;
		}
		if (!noDBIndividualityObject) {
			if (dbIndividualityObject == null) {
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_ID,
						Lang.ERROR_DB_NOTFOUND_REFERENCE);
				b = false;
			}
		}

		if (blackList != null) {
			UserStore us = new UserStore(txn);
			for (Long id : blackList) {
				if (us.get(id) == null) {
					r.add(Lang.SOCIALITY_BLACKLIST,
							Lang.ERROR_DB_NOTFOUND_REFERENCE);
					b = false;
					break;
				}
			}
		}

		if (wallet != null) {
			if (!wallet.validateReference(r, txn)) {
				b = false;
			}
		}

		if (edgeManager != null) {
			if (!edgeManager.validateReference(r, txn)) {
				b = false;
			}
		}

		return b;
	}

	@Override
	public SocialityGui getGui(String guiName, String cssIdPrefix) {
		return new SocialityGui(guiName, cssIdPrefix);
	}

	@Override
	public SocialityStore getStore(Transaction txn) {
		return new SocialityStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.SOCIALITY;
	}

}
