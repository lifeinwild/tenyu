package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.sociality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.user.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 社会性
 * 相互評価フローネットワークのノード
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Sociality extends AdministratedObject
		implements Trader, SocialityI {
	public static final int balckListMax = 2000;

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

	public static int getEdgeCountMaxByNodeType(StoreName type) {
		if (type == StoreNameSingle.OBJECTIVITY_CORE) {
			return EdgeManager.edgeMax;
		} else if (type == StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL) {
			return EdgeManager.edgeMax;
		}
		return 2000;
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
	 * 対応オブジェクト
	 *
	 * 社会性は非社会性モデルのオブジェクトと１：１対応する。
	 * 社会性を持ちうるのは少なくとも{@link IndividualityObjectI}以下。
	 */
	protected TenyuReferenceModelI<
			? extends IndividualityObjectI> individualityObjectConcreteRef;

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

	/**
	 * @param o
	 * @return	相互評価フローネットワークのノードが伴うモデルか
	 */
	public static boolean isValidModel(IndividualityObjectI o) {
		if (o instanceof HasSocialityI) {
			return true;
		}
		Glb.debug("not supported class " + o.getClass().getCanonicalName(),
				new IllegalArgumentException());
		return false;
	}

	@Override
	public TenyuReferenceModelI<
			? extends IndividualityObjectI> getIndividualityObjectConcreteRef() {
		return individualityObjectConcreteRef;
	}

	/**
	 * 社会的信用を返す
	 * {@link P2PEdge#credit()}は主観信用なので区別が必要
	 * @return	社会的信用。0-60000程度
	 */
	public int credit() {
		int max = 1000 * 60;
		int socialCredit = 0;
		if (StoreNameObjectivity.USER
				.equals(individualityObjectConcreteRef.getStoreName())) {
			Long authorId = Glb.getConst().getAuthor().getId();
			if (authorId.equals(individualityObjectConcreteRef.getId())) {
				socialCredit += max;
			}
		}
		socialCredit += Glb.getUtil()
				.getScaleForNumber(flowFromCooperativeAccount) * (max / 632);
		if (Glb.getObje().getCore().getHistoryIndex() - createHistoryIndex > 720
				* 700) {
			socialCredit += 3000;
		}
		if (socialCredit < 0)
			socialCredit = 0;
		if (socialCredit > max)
			socialCredit = max;
		return socialCredit;
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
		if (individualityObjectConcreteRef == null) {
			if (other.individualityObjectConcreteRef != null)
				return false;
		} else if (!individualityObjectConcreteRef
				.equals(other.individualityObjectConcreteRef))
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
		if (getNodeType() == StoreNameObjectivity.USER
				&& getIndividualityObjectConcreteRef().getId() == Glb.getConst()
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

	/**
	 * {@link Sociality}は必ず１つの{@link IndividualityObject}と対応づく
	 * {@link Sociality#individualityObjectConcreteId}は一意ではなく
	 * 個性系具象クラスの識別子を加えると一意になる。
	 * @return	NodeTypeのid+オブジェクトのid
	 */
	public byte[] getIndividualityObjectStoreKey() {
		return individualityObjectConcreteRef.getStoreKeyReferenced();
	}

	@Override
	public StoreName getNodeType() {
		return individualityObjectConcreteRef.getStoreName();
	}

	@Override
	public List<Long> getSpecialMainAdministratorIds() {
		List<Long> r = new ArrayList<>();
		if (individualityObjectConcreteRef == null)
			return r;
		StoreName sn = individualityObjectConcreteRef.getStoreName();
		if (sn == null)
			return r;
		if (sn == StoreNameSingle.OBJECTIVITY_CORE) {
			r.add(ModelI.getVoteId());

		} else if (sn == StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL) {
			r.add(ModelI.getNullId());
		} else if (sn == StoreNameObjectivity.WEB) {
			r.add(ModelI.getNullId());
		} else if (sn == StoreNameObjectivity.RATING_GAME) {

		} else if (sn == StoreNameObjectivity.STATIC_GAME) {

		} else if (sn == StoreNameObjectivity.USER) {

		} else if (sn == StoreNameTenyutalk.TENYUTALK_GITREPOSITORY) {

		} else if (sn == StoreNameTenyutalk.TENYUTALK_ARTIFACT) {

		}
		return r;
	}

	@Override
	public Long getSpecialRegistererId() {
		if (individualityObjectConcreteRef == null)
			return null;
		StoreName sn = individualityObjectConcreteRef.getStoreName();
		if (sn == null)
			return null;
		if (sn == StoreNameSingle.OBJECTIVITY_CORE) {
			return ModelI.getSystemId();
		} else if (sn == StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL) {
			return ModelI.getVoteId();
		} else if (sn == StoreNameObjectivity.WEB) {
		} else if (sn == StoreNameObjectivity.RATING_GAME) {

		} else if (sn == StoreNameObjectivity.STATIC_GAME) {

		} else if (sn == StoreNameObjectivity.USER) {

		}
		return null;
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
		result = prime * result + ((individualityObjectConcreteRef == null) ? 0
				: individualityObjectConcreteRef.hashCode());
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

	public void setIndividualityObjectConcreteRef(TenyuReferenceModelI<
			? extends IndividualityObjectI> individualityObjectConcreteRef) {
		this.individualityObjectConcreteRef = individualityObjectConcreteRef;
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

		StoreName sn = individualityObjectConcreteRef.getStoreName();
		if (sn == null) {
			Glb.getLogger().error("sn is null", new IllegalStateException());
		} else {
			if (sn == StoreNameObjectivity.FLOW_NETWORK_ABSTRACT_NOMINAL) {
				double rate = Glb.getObje().getCore().getConfig()
						.getEdgeChangePaceAbstractNominal();
				increaseMax = (long) (Edge.weightMax * rate);
				decreaseMax = (long) -(Edge.weightMax * rate);
				//5日間のみ流量制限を受けない
				freePeriod = Glb.getObje().getCore().getConfig()
						.getHistoryIndexDayRough() * 5;
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
		StoreName sn = null;
		if (individualityObjectConcreteRef == null) {
			r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
					Lang.ERROR_EMPTY);
			b = false;
		} else {
			//客観モデルを単純に参照するものだけ許可する
			//客観モデル→参照→社会性というルートで社会性を特定できる必要があるが、
			//例えばフレキシブル参照があると社会性との１：１対応が維持されない。
			if (!(individualityObjectConcreteRef instanceof SimpleReferenceI)) {
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
						Lang.ERROR_INVALID, "individualityObjectConcreteRef="
								+ individualityObjectConcreteRef);
				b = false;
			}

			sn = individualityObjectConcreteRef.getStoreName();
			if (sn == null) {
				r.add(Lang.SOCIALITY_NODETYPE, Lang.ERROR_EMPTY);
				b = false;
			}
			//具象が適切なクラスである事はvalidateReferenceでチェックする

			if (blackList != null) {
				if (blackList.size() > balckListMax) {
					r.add(Lang.SOCIALITY_BLACKLIST, Lang.ERROR_INVALID);
					b = false;
				} else {
					if (!Model.validateIdStandardNotSpecialId(blackList)) {
						r.add(Lang.SOCIALITY_BLACKLIST, Lang.ERROR_INVALID);
						b = false;
					}
				}
			}
		}

		if (flowFromCooperativeAccount < 0) {
			r.add(Lang.SOCIALITY_FLOW_FROM_COOPERATIVEACCOUNT,
					Lang.ERROR_INVALID,
					"flowFromCooperativeAccount=" + flowFromCooperativeAccount);
			b = false;
		}

		if (edgeManager == null) {
			r.add(Lang.SOCIALITY_EDGES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			int edgeCount = edgeManager.getEdges().size();
			int edgeMax = getEdgeCountMaxByNodeType(sn);
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
			if (!individualityObjectConcreteRef.validateAtCreate(r))
				b = false;
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
		if (Glb.getUtil().notEqual(getIndividualityObjectConcreteRef(),
				old2.getIndividualityObjectConcreteRef())) {
			r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
					Lang.ERROR_UNALTERABLE,
					"individualityObjectConcreteRef="
							+ getIndividualityObjectConcreteRef()
							+ " oldIndividualityObjectConcreteRef="
							+ old2.getIndividualityObjectConcreteRef());
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
			if (!individualityObjectConcreteRef.validateAtUpdate(r))
				b = false;
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

		IndividualityObjectI dbIndividualityObject = null;

		dbIndividualityObject = individualityObjectConcreteRef
				.getReferenced(txn);

		//対応オブジェクトがDB上にあると限らない場合true
		boolean noDBIndividualityObject = false;
		StoreName sn = individualityObjectConcreteRef.getStoreName();
		if (sn == StoreNameSingle.OBJECTIVITY_CORE) {
			//共同主体は管理者無しでIndividualityObjectもDBに記録されない
			noDBIndividualityObject = true;
		} else {
			dbIndividualityObject = individualityObjectConcreteRef
					.getReferenced(txn);
			/*
			//例外系
			r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
					Lang.ERROR_INVALID_REFERENCE,
					"sociality.individualityObjectConcreteRef="
							+ individualityObjectConcreteRef);
			b = false;
			*/
		}

		//共同主体以外は対応オブジェクトを持つ
		if (!noDBIndividualityObject) {
			if (dbIndividualityObject == null) {
				//対応オブジェクトが見つからない場合
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
						Lang.ERROR_DB_NOTFOUND_REFERENCE,
						"sociality.individualityObjectConcreteRef="
								+ individualityObjectConcreteRef);
				b = false;
			} else if (!(dbIndividualityObject instanceof HasSocialityI)) {
				//社会性と対応づかないはずの種類のオブジェクトが得られた場合
				r.add(Lang.SOCIALITY_INDIVIDUALITY_OBJECT_CONCRETE_REF,
						Lang.ERROR_INVALID,
						"sociality.individualityObjectConcreteRef="
								+ individualityObjectConcreteRef
								+ " referenced=" + dbIndividualityObject);
				b = false;
			}
		}

		if (blackList != null) {
			UserStore us = new UserStore(txn);
			for (Long blackListId : blackList) {
				if (us.get(blackListId) == null) {
					r.add(Lang.SOCIALITY_BLACKLIST,
							Lang.ERROR_DB_NOTFOUND_REFERENCE,
							"blackListId=" + blackListId);
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
	public SocialityGui getGuiReferenced(String guiName, String cssIdPrefix) {
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

	@Override
	public String toString() {
		return "Sociality [banned=" + banned + ", blackList=" + blackList
				+ ", createHistoryIndex=" + createHistoryIndex
				+ ", edgeManager=" + edgeManager
				+ ", flowFromCooperativeAccount=" + flowFromCooperativeAccount
				+ ", individualityObjectConcreteRef="
				+ individualityObjectConcreteRef + ", wallet=" + wallet + "]";
	}

}
