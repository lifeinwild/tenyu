package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import java.net.*;
import java.nio.*;
import java.security.interfaces.*;
import java.time.*;
import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import bei7473p5254d69jcuat.tenyu.release1.util.Util.*;
import jetbrains.exodus.env.*;

public class User extends Naturality implements ChainVersionup, UserDBI {
	/*	カスタムシリアライザを作ってもほとんど速度が変わらなかった
	public static class UserSerializer extends Serializer<User>{

		@Override
		public void write(Kryo kryo, Output output, User object) {
			output.writeBytes(object.getPcPublicKey());
			output.writeBytes(object.getMobilePublicKey());
			output.writeBytes(object.getOfflinePublicKey());
			//TODO	抽象クラスとかカントリーとか
		}

		@Override
		public User read(Kryo kryo, Input input, Class<? extends User> type) {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

	}*/

	public static final int memoByTenyuManagerMax = 1000 * 5;
	public static final NodeType nodeType = NodeType.USER;

	/**
	 * @param roleId
	 * @param userId
	 * @return			指定された役割とユーザーからノード番号を特定し
	 * NodeIdentifierUserにして返す。
	 */
	public static NodeIdentifierUser getNodeIdentifierUser(Long roleId,
			Long userId) {
		User u = Glb.getObje().getUser(us->us.get(userId));
		if (u == null)
			return null;
		return u.getNodeIdentifierByRole(roleId);
	}

	public static boolean createSequence(Transaction txn, User u,
			boolean specifiedId, long historyIndex) throws Exception {
		if (!Glb.getObje().getCore().getConfig().isUserRegistrationActivate()) {
			return false;
		}
		u.setCreateHistoryIndex(historyIndex);
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, new UserStore(txn), null, null, NodeType.USER);
	}

	public static boolean deleteSequence(Transaction txn, User u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u, new UserStore(txn),
				NodeType.USER);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return null;
	}

	private static final String getMatchingServerPortKey() {
		return RatingGameMatchingServer.class.getSimpleName() + "Port";
	}

	private static final String getMessageListServerPortKey() {
		return UserMessageListServer.class.getSimpleName() + "Port";
	}

	public static NodeType getNodetype() {
		return nodeType;
	}

	/**
	 * @return	secureUser設定に応じたRSA鍵長のビット数
	 */
	public static int getRsaKeySizeBitBySecure(boolean secure) {
		if (secure) {
			return Glb.getConst().getRsaKeySizeBitSecure();
		} else {
			return Glb.getConst().getRsaKeySizeBit();
		}
	}

	/**
	 * @return	secureUser設定に応じたRSA鍵長のバイト数
	 */
	public static int getRsaKeySizeByteBySecure(boolean secure) {
		return getRsaKeySizeBitBySecure(secure) / 8;
	}

	/**
	 * ノード番号：アドレス関係情報
	 *
	 * アドレスは日々変化するので設定されない。しかしFQDNを設定できる。
	 *
	 * FQDNは未設定も可能だが、特別なサーバーを起動する一部ユーザーは設定する必要がある。
	 * fqdnを設定するならp2pportも設定する必要がある。
	 * fqdn+p2pportで接続可能になる。
	 */
	private Map<Integer, AddrInfo> nodeNumberToAddr;

	public static final int nodeNumberToAddrMax = 100;

	/**
	 * 役割 : ノード番号
	 * 1ユーザーが多数のノードを起動しノード毎に担当する役割を変えるため。
	 * 未設定なら0番が全ての役割を行う事を意味する。
	 * これを設定する必要があるのは何らかのRoleに割り当てられているユーザーで
	 * 多数のノードを起動してノード毎に役割を変えたい人。
	 */
	private Map<Long, Integer> roleToNodeNumber = null;

	/**
	 * ユーザーがプロフィールの一部としてアバターを設定する
	 */
	private AvatarConfig avatarConfig = new AvatarConfig();

	/**
	 * ユーザー登録日時
	 * 自己申告で良いが、あまりに大きな誤差は登録時に拒否される。
	 * この情報は認可系の検証処理など重要なものには使えない
	 */
	private long createDate;
	/**
	 * ユーザー登録日時
	 * 登録からの期間に応じて権限を変更可能に
	 * 日時ではなくhistoryIndexでなければ全ノードで一致させれない
	 */
	private long createHistoryIndex;

	/**
	 * 全体運営者からの疑い。BANにどれだけ近いか
	 */
	private int dougt;

	/**
	 * 全体運営者によるメモ。
	 */
	private String memoByTenyuManager;

	private byte[] mobilePublicKey;

	private byte[] offlinePublicKey;

	private byte[] pcPublicKey;

	/**
	 * セキュアユーザー
	 * RSA鍵長が4096ビットになる
	 * ただしセキュアユーザーとして登録するには運営者の承認が必要
	 *
	 * この概念を置いた理由は、ユーザーによって必要なセキュリティ強度が異なる事と、
	 * RSA鍵長に電子署名のサイズが比例し、さらにメッセージ処理ペースが比例し、
	 * 対応可能最大ノード数が指数関数的に変化するから。
	 */
	private boolean secure = false;

	/**
	 * タイムゾーンが同じなら地理的に近い地域である。
	 * サーバー選択等で利用できる。
	 * ZoneIdのtimezoneId。TimeZoneではない。
	 */
	private String timezoneId;

	/**
	 * @return	1つもFQDNが設定されていないならtrue
	 */
	public boolean isNoFqdn() {
		if (nodeNumberToAddr == null)
			return true;
		for (AddrInfo e : nodeNumberToAddr.values()) {
			if (e != null && e.getFqdn() != null && e.getFqdn().length() > 0)
				return true;
		}
		return false;
	}

	public NodeIdentifierUser getNodeIdentifierByRole(Long roleId) {
		int nodeNumber = 0;
		if (getRoleToNodeNumber() != null) {
			Integer tmp = getRoleToNodeNumber().get(roleId);
			if (tmp != null)
				nodeNumber = tmp;
		}
		return new NodeIdentifierUser(getRecycleId(), nodeNumber);
	}

	/**
	 * @param nodeNumber
	 * @return	このノード番号は何らかの役割に対応付けられているか
	 */
	public boolean isRelatedToRole(int nodeNumber) {
		if (roleToNodeNumber == null)
			return false;
		for (Integer e : roleToNodeNumber.values()) {
			if (e.equals(nodeNumber))
				return true;
		}
		return false;
	}

	public NodeIdentifierUser getIdentifier(Long roleId) {
		int nodeNumber = getNodeNumberByRole(roleId);
		return new NodeIdentifierUser(getRecycleId(), nodeNumber);
	}

	public int getNodeNumberByRole(Long roleId) {
		if (roleToNodeNumber == null)
			return 0;
		Integer nodeNumber = roleToNodeNumber.get(roleId);
		if (nodeNumber == null)
			return 0;
		return nodeNumber;
	}

	public Map<Long, Integer> getRoleToNodeNumber() {
		if (roleToNodeNumber == null)
			return null;
		return Collections.unmodifiableMap(roleToNodeNumber);
	}

	public void setRoleToNodeNumber(Map<Long, Integer> roleToNodeNumber) {
		this.roleToNodeNumber = roleToNodeNumber;
	}

	public Integer addRoleToNodeNumber(Long roleId, int nodeNumber) {
		if (roleToNodeNumber == null) {
			roleToNodeNumber = new HashMap<>();
		}
		return roleToNodeNumber.put(roleId, nodeNumber);
	}

	public void addNodeNumberToAddr(int nodeNumber, AddrInfo addr) {
		if (nodeNumberToAddr == null) {
			nodeNumberToAddr = new HashMap<>();
		}
		nodeNumberToAddr.put(nodeNumber, addr);
	}

	/**
	 * 登録から約一週間が経過すると他の人を紹介可能になる
	 * @return	他のユーザー登録希望者を紹介可能か
	 */
	public boolean canIntroduce() {
		//作者なら許可
		if (Glb.getConst().getAuthor().getRecycleId().equals(recycleId))
			return true;

		long current = Glb.getObje().getCore().getHistoryIndex();
		//開始後間もない間は許可
		if (current <= 2000)
			return true;

		long week = Glb.getObje().getCore().getConfig()
				.getHistoryIndexWeekRough();
		long dif = current - createHistoryIndex;
		return dif >= week;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (avatarConfig == null) {
			if (other.avatarConfig != null)
				return false;
		} else if (!avatarConfig.equals(other.avatarConfig))
			return false;
		if (createDate != other.createDate)
			return false;
		if (createHistoryIndex != other.createHistoryIndex)
			return false;
		if (dougt != other.dougt)
			return false;
		if (memoByTenyuManager == null) {
			if (other.memoByTenyuManager != null)
				return false;
		} else if (!memoByTenyuManager.equals(other.memoByTenyuManager))
			return false;
		if (!Arrays.equals(mobilePublicKey, other.mobilePublicKey))
			return false;
		if (nodeNumberToAddr == null) {
			if (other.nodeNumberToAddr != null)
				return false;
		} else if (!nodeNumberToAddr.equals(other.nodeNumberToAddr))
			return false;
		if (!Arrays.equals(offlinePublicKey, other.offlinePublicKey))
			return false;
		if (!Arrays.equals(pcPublicKey, other.pcPublicKey))
			return false;
		if (roleToNodeNumber == null) {
			if (other.roleToNodeNumber != null)
				return false;
		} else if (!roleToNodeNumber.equals(other.roleToNodeNumber))
			return false;
		if (secure != other.secure)
			return false;
		if (timezoneId == null) {
			if (other.timezoneId != null)
				return false;
		} else if (!timezoneId.equals(other.timezoneId))
			return false;
		return true;
	}

	/**
	 * @param u
	 * @return	3種全ての鍵が作者と一致しているか
	 */
	public boolean equalsKeys(User u) {
		if (!equalsMobilePubKey(u.getMobilePublicKey()))
			return false;

		if (!equalsOfflinePubKey(u.getOfflinePublicKey()))
			return false;

		if (!equalsPcPubKey(u.getPcPublicKey()))
			return false;
		return true;
	}

	public boolean equalsMobilePubKey(byte[] mobilePubKey) {
		return Arrays.equals(this.mobilePublicKey, mobilePubKey);
	}

	public boolean equalsOfflinePubKey(byte[] offlinePubKey) {
		return Arrays.equals(this.offlinePublicKey, offlinePubKey);
	}

	public boolean equalsPcPubKey(byte[] pcPubKey) {
		return Arrays.equals(this.pcPublicKey, pcPubKey);
	}

	public Map<Integer, AddrInfo> getNodeNumberToAddr() {
		return nodeNumberToAddr;
	}

	public AddrInfo getAddr(int nodeNumber) {
		if (nodeNumberToAddr == null)
			return null;
		return nodeNumberToAddr.get(nodeNumber);
	}

	public void setNodeNumberToAddr(Map<Integer, AddrInfo> nodeNumberToAddr) {
		this.nodeNumberToAddr = nodeNumberToAddr;
	}

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return new ArrayList<>();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		//ユーザーの更新はそのユーザー自身が行う
		List<Long> r = new ArrayList<>();
		r.add(getRecycleId());
		return r;
	}

	public AvatarConfig getAvatarConfig() {
		return avatarConfig;
	}

	public long getCreateDate() {
		return createDate;
	}

	public long getCreateHistoryIndex() {
		return createHistoryIndex;
	}

	public int getDougt() {
		return dougt;
	}

	public Long getInviter() {
		return registererUserId;
	}

	private AddrInfo getISAInternal(int nodeNumber) {
		NodeIdentifierUser identifier = new NodeIdentifierUser(getRecycleId(),
				nodeNumber);

		//User情報に設定されたFQDNから
		if (nodeNumberToAddr != null) {
			AddrInfo addr = nodeNumberToAddr.get(nodeNumber);
			if (addr != null) {
				AddrInfo r = addr.clone();
				r.updateAddrByFqdn();
				return r;
			}
		}

		//既存のデータから返す
		UserEdge ue = Glb.getMiddle().getUserEdgeList().getEdge(identifier);
		if (ue != null && ue.isCommunicatedIn10Minutes()) {
			AddrInfo r = ue.getAddr();
			if (r != null) {
				//接続確認に成功したらそれを返す
				if (UserEdgeGreeting.send(identifier, r, false)) {
					return r;
				}
			}
		}

		//一応近傍から探してみる
		//ネットワークの勃興時にはこのコードは役立つかもしれない。
		//ほとんどのノードが相互に認識し完全グラフを作るから。
		if (getRecycleId() == null) {
			Glb.getLogger().warn("userId is null", new Exception());
			return null;
		}
		P2PEdge e = Glb.getSubje().getNeighborList().getNeighbor(identifier);
		if (e != null && e.getNode() != null) {
			AddrInfo r = e.getNode().getAddrInfoClone();
			if (r != null)
				return r;
		}

		//アドレス解決サーバを使う
		AddrInfo ua = GetUserAddr.get(identifier);
		if (ua != null) {
			return ua;
		}
		return null;
	}

	@Override
	public Long getMainAdministratorUserId() {
		//Userは常に自分のIDをメイン管理者として返す
		return getRecycleId();
	}

	/**
	 * @return	追加的設定のマッチングサーバー用ポート。無ければnull
	 */
	public Integer getMatchingServerPort() {
		return getAdditionalSetting(getMatchingServerPortKey(),
				b -> ByteBuffer.wrap(b).getInt());
	}

	public String getMemoByTenyuManager() {
		return memoByTenyuManager;
	}

	/**
	 * @return	追加的設定のユーザーメッセージリスト受付サーバー用ポート。無ければnull
	 */
	public Integer getMessageListServerPort() {
		return getAdditionalSetting(getMessageListServerPortKey(),
				b -> ByteBuffer.wrap(b).getInt());
	}

	public byte[] getMobilePublicKey() {
		return mobilePublicKey;
	}

	public byte[] getOfflinePublicKey() {
		return offlinePublicKey;
	}

	public byte[] getPcPublicKey() {
		return pcPublicKey;
	}

	public byte[] getPubKey(KeyType type) {
		switch (type) {
		case PC:
			return pcPublicKey;
		case MOBILE:
			return mobilePublicKey;
		case OFFLINE:
			return offlinePublicKey;
		default:
			return null;
		}
	}

	@Override
	public Long getSpecialMainAdministratorId() {
		//Userはメイン管理者を設定する必要が無い
		return IdObjectDBI.getNullId();
	}

	@Override
	public Long getSpecialRegistererId() {
		if (pcPublicKey != null && Glb.getConst()
				.isAuthorPublicKey(new ByteArrayWrapper(pcPublicKey))) {
			return IdObjectDBI.getSystemId();
		}
		return null;
	}

	public ZoneId getTimezone() {
		return ZoneId.of(timezoneId);
	}

	public String getTimezoneId() {
		return timezoneId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((avatarConfig == null) ? 0 : avatarConfig.hashCode());
		result = prime * result + (int) (createDate ^ (createDate >>> 32));
		result = prime * result
				+ (int) (createHistoryIndex ^ (createHistoryIndex >>> 32));
		result = prime * result + dougt;
		result = prime * result + ((memoByTenyuManager == null) ? 0
				: memoByTenyuManager.hashCode());
		result = prime * result + Arrays.hashCode(mobilePublicKey);
		result = prime * result + ((nodeNumberToAddr == null) ? 0
				: nodeNumberToAddr.hashCode());
		result = prime * result + Arrays.hashCode(offlinePublicKey);
		result = prime * result + Arrays.hashCode(pcPublicKey);
		result = prime * result + ((roleToNodeNumber == null) ? 0
				: roleToNodeNumber.hashCode());
		result = prime * result + (secure ? 1231 : 1237);
		result = prime * result
				+ ((timezoneId == null) ? 0 : timezoneId.hashCode());
		return result;
	}

	public boolean isMyKey(byte[] pubKey) {
		if (pubKey == null)
			return false;
		if (Arrays.equals(pubKey, pcPublicKey)
				|| Arrays.equals(pubKey, mobilePublicKey)
				|| Arrays.equals(pubKey, offlinePublicKey)) {
			return true;
		}
		return false;
	}

	public boolean isSecure() {
		return secure;
	}

	/**
	 * 追加的設定のマッチングサーバー用ポートを書き込む
	 * @param port
	 * @return
	 */
	public boolean putMatchingServerPort(Integer port) {
		return putAdditionalSetting(getMatchingServerPortKey(),
				ByteBuffer.allocate(4).putInt(port).array());
	}

	/**
	 * 追加的設定のユーザーメッセージリスト受付サーバー用ポートを書き込む
	 * @param port
	 * @return
	 */
	public boolean putMessageListServerPort(Integer port) {
		return putAdditionalSetting(getMessageListServerPortKey(),
				ByteBuffer.allocate(4).putInt(port).array());
	}

	public boolean removeMatchingServerPort() {
		return removeAdditionalSetting(getMatchingServerPortKey());
	}

	public boolean removeMessageListServerPort() {
		return removeAdditionalSetting(getMessageListServerPortKey());
	}

	public void setAvatarConfig(AvatarConfig avatarConfig) {
		this.avatarConfig = avatarConfig;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public void setCreateHistoryIndex(long createHistoryIndex) {
		this.createHistoryIndex = createHistoryIndex;
	}

	public void setDougt(int dougt) {
		this.dougt = dougt;
	}

	public void setInviter(Long inviter) {
		this.registererUserId = inviter;
	}

	public void setMemoByTenyuManager(String memoByTenyuManager) {
		this.memoByTenyuManager = memoByTenyuManager;
	}

	public void setMobilePublicKey(byte[] mobilePublicKey) {
		this.mobilePublicKey = mobilePublicKey;
	}

	public void setOfflinePublicKey(byte[] offlinePublicKey) {
		this.offlinePublicKey = offlinePublicKey;
	}

	public void setPcPublicKey(byte[] pcPublicKey) {
		this.pcPublicKey = pcPublicKey;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public void setTimezone(ZoneId zoneId) {
		if (zoneId == null) {
			timezoneId = null;
		} else {
			this.timezoneId = zoneId.getId();
		}
	}

	public void setTimezoneId(String timezoneId) {
		this.timezoneId = timezoneId;
	}

	public InetSocketAddress tryToGetISAP2PPort(int nodeNumber) {
		AddrInfo addr = tryToGetAddr(nodeNumber);
		if (addr == null)
			return null;
		return addr.getISAP2PPort();
	}

	/**
	 * @param nodeNumber	対象ノード
	 * @return		thisのIPアドレスまたはnull。
	 * 送信先が自分だったらループバックアドレスを返す
	 */
	public AddrInfo tryToGetAddr(int nodeNumber) {
		//自分
		NodeIdentifierUser me = Glb.getMiddle().getMyNodeIdentifierUser();
		//このノード
		NodeIdentifierUser u = new NodeIdentifierUser(getRecycleId(),
				nodeNumber);

		//送信先は自分か
		if (u.equals(me)) {
			//ループバックアドレスを返す
			return Glb.getConf()
					.getAddrInfo(InetAddress.getLoopbackAddress().getAddress());
		}

		return getISAInternal(nodeNumber);
	}

	/**
	 * プロフィールを更新する。
	 * @param latest	これの一部の状態が使用される。
	 * BAN、鍵、secureフラグは更新されない。
	 */
	/*
	public void updateProfile(User latest) {
		if (latest.avatarConfig != null)
			avatarConfig = latest.avatarConfig;
		fqdn = latest.fqdn;
		port = latest.port;
		timezoneId = latest.timezoneId;
	}
	*/

	@Override
	public boolean validateAtCatchUp() {
		boolean b = true;
		User author = Glb.getConst().getAuthor();
		//作者とオフライン鍵が一致するか
		boolean eqOff = equalsOfflinePubKey(author.getOfflinePublicKey());
		//もし作者と同じIDなら
		if (getRecycleId().equals(author.getRecycleId())) {
			//オフライン鍵が一致している事を確認
			if (!eqOff) {
				//r.add(Lang.USER, Lang.ERROR_INVALID,
				//		"UserId==0 but not equals author offlineKey");
				b = false;
			}
		} else {
			//オフライン鍵が一致しない事を確認
			if (eqOff) {
				//r.add(Lang.USER, Lang.ERROR_INVALID,
				//		"UserId!=0 but equals author offlineKey");
				b = false;
			}
		}

		if (!super.validateAtCatchUp()) {
			b = false;
		}

		return b;
	}

	private final boolean validateAtCommonNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		try {
			//thisは作者か
			boolean author = Glb.getConst().getAuthor().equalsKeys(this);

			//作者以外紹介者が必要
			if (!author) {
				if (registererUserId == null
						|| IdObjectDBI.getNullId().equals(registererUserId)) {
					r.add(Lang.OBJECTIVITYOBJECT_REGISTERER,
							Lang.ERROR_NO_INVITER);
					b = false;
				} else {
					//セキュアユーザーの紹介者は全体運営者である必要がある
					if (secure) {
						double power = Glb.getObje().getCore().getManagerList()
								.getManagerPower(registererUserId);
						if (power < 0.1) {
							r.add(Lang.OBJECTIVITYOBJECT_REGISTERER,
									Lang.ERROR_INVALID,
									"secure=" + secure
											+ " not tenyu manager registererUserId="
											+ registererUserId);
							b = false;
						}
					}
				}
			}

			if (memoByTenyuManager == null) {
				//問題無し
			} else {
				if (memoByTenyuManager.length() > memoByTenyuManagerMax) {
					r.add(Lang.USER_TENYU_MANAGER_MEMO, Lang.ERROR_TOO_LONG,
							"memoByTenyuManager.length="
									+ memoByTenyuManager.length());
					b = false;
				} else if (!Naturality.validateText(
						Lang.USER_TENYU_MANAGER_MEMO, memoByTenyuManager, r)) {
					b = false;
				}
			}

			if (timezoneId == null) {
				r.add(Lang.USER_TIMEZONE, Lang.ERROR_EMPTY);
				b = false;
			} else if (timezoneId.length() > 1000) {
				r.add(Lang.USER_TIMEZONE, Lang.ERROR_TOO_LONG,
						timezoneId.length() + " / 1000");
				b = false;
			} else {
				TimeZone tz = TimeZone.getTimeZone(timezoneId);
				if (tz.equals(TimeZone.getTimeZone("GMT"))) {
					//存在しないタイムゾーンIDはGMTとして返る
					Glb.getLogger().warn("GMT zone userId=" + getRecycleId());
					//エラーにする
					r.add(Lang.USER_TIMEZONE, Lang.ERROR_INVALID);
					b = false;
				}
			}

			Util u = Glb.getUtil();
			//secure設定に応じた適切な鍵長
			int rsaKeyLen = User.getRsaKeySizeBitBySecure(secure);

			//鍵長及び正しい鍵データかを検証

			//異常にでかいサイズを登録させない
			int max = 1000 * 100;
			if (getMobilePublicKey() == null
					|| getMobilePublicKey().length == 0) {
				r.add(Lang.USER_MOBILEKEY, Lang.ERROR_EMPTY);
				b = false;
			} else if (getMobilePublicKey().length > max) {
				r.add(Lang.USER_MOBILEKEY, Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				try {
					RSAPublicKey mob = (RSAPublicKey) u
							.getPub(getMobilePublicKey());
					if (mob.getModulus().bitLength() != rsaKeyLen) {
						r.add(Lang.USER_MOBILEKEY, Lang.ERROR_INVALID);
						b = false;
					}
				} catch (Exception e) {
					r.add(Lang.USER_MOBILEKEY, Lang.ERROR_INVALID);
					b = false;
				}
			}

			if (getPcPublicKey() == null || getPcPublicKey().length == 0) {
				r.add(Lang.USER_PCKEY, Lang.ERROR_EMPTY);
				b = false;
			} else if (getPcPublicKey().length > max) {
				r.add(Lang.USER_PCKEY, Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				try {
					RSAPublicKey pc = (RSAPublicKey) u.getPub(getPcPublicKey());
					if (pc.getModulus().bitLength() != rsaKeyLen) {
						r.add(Lang.USER_PCKEY, Lang.ERROR_INVALID);
						b = false;
					}
				} catch (Exception e) {
					r.add(Lang.USER_PCKEY, Lang.ERROR_INVALID);
					b = false;
				}

			}

			if (getOfflinePublicKey() == null) {
				r.add(Lang.USER_OFFKEY, Lang.ERROR_EMPTY);
				b = false;
			} else if (getOfflinePublicKey().length > max) {
				r.add(Lang.USER_OFFKEY, Lang.ERROR_TOO_LONG);
				b = false;
			} else {
				try {
					RSAPublicKey off = (RSAPublicKey) u
							.getPub(getOfflinePublicKey());
					if (off.getModulus().bitLength() != rsaKeyLen) {
						r.add(Lang.USER_OFFKEY, Lang.ERROR_INVALID);
						b = false;
					}
				} catch (Exception e) {
					r.add(Lang.USER_OFFKEY, Lang.ERROR_INVALID);
					b = false;
				}
			}

			if (avatarConfig == null) {
				r.add(Lang.USER_AVATAR, Lang.ERROR_EMPTY);
				b = false;
			}

			if (createHistoryIndex < ObjectivityCore.firstHistoryIndex
					|| createHistoryIndex > Glb.getObje().getCore()
							.getHistoryIndex()
							+ Glb.getObje().getCore().getConfig()
									.getLoadSetting()
									.getUserMessageListHistoryIndexTolerance()) {
				r.add(Lang.USER_HISTORYINDEX, Lang.ERROR_INVALID,
						"createHistoryIndex=" + createHistoryIndex);
				b = false;
			}

			if (nodeNumberToAddr == null) {
				r.add(Lang.USER_NODENUMBER_TO_ADDR, Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (nodeNumberToAddr.size() > nodeNumberToAddrMax) {
					r.add(Lang.USER_NODENUMBER_TO_ADDR, Lang.ERROR_TOO_MANY,
							"size=" + nodeNumberToAddr.size());
					b = false;
				} else {
					for (Entry<Integer, AddrInfo> e : nodeNumberToAddr
							.entrySet()) {
						if (e.getKey() < 0) {
							r.add(Lang.USER_NODENUMBER, Lang.ERROR_INVALID,
									"nodeNumber=" + e.getKey());
							b = false;
						}
						if (!e.getValue().validateAtCommon(r)) {
							b = false;
						}
					}
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			r.add(Lang.USER, Lang.EXCEPTION);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r)) {
			b = false;
		}

		if (avatarConfig != null) {
			if (!avatarConfig.validateAtCreate(r)) {
				b = false;
			}
		}

		//thisは作者か
		boolean author = Glb.getConst().getAuthor().equalsKeys(this);
		if (!author) {
			long distance = Math.abs(System.currentTimeMillis() - createDate);
			//30日まで誤差を認める
			long limit = 1000L * 60L * 60L * 24L * 30L;
			if (distance > limit) {
				r.add(Lang.USER_CREATEDATE, Lang.ERROR_INVALID,
						"distance=" + distance);
				b = false;
			}
		}
		return b;
	}

	/**
	 * ユーザー登録紹介依頼時の検証
	 */
	public final boolean validateAtOffer(ValidationResult vr) {
		//Userでは、この時点でDBでのcreate直前の検証と同じ
		validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			return false;
		}

		return true;
	}

	@Override
	protected boolean validateAtUpdateChangeNaturalityConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof User)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		User old2 = (User) old;

		boolean b = true;
		if (Glb.getUtil().notEqual(getCreateDate(), old2.getCreateDate())) {
			r.add(Lang.USER_CREATEDATE, Lang.ERROR_UNALTERABLE,
					"createDate=" + getCreateDate() + " oldCreateDate="
							+ old2.getCreateDate());
			b = false;
		}
		if (Glb.getUtil().notEqual(getCreateHistoryIndex(),
				old2.getCreateHistoryIndex())) {
			r.add(Lang.USER_CREATE_HISTORYINDEX, Lang.ERROR_UNALTERABLE,
					"createHistoryIndex=" + getCreateHistoryIndex()
							+ " oldCreateHistoryIndex="
							+ old2.getCreateHistoryIndex());
			b = false;
		}
		if (Glb.getUtil().notEqual(getOfflinePublicKey(),
				old2.getOfflinePublicKey())) {
			r.add(Lang.USER_OFFKEY, Lang.ERROR_UNALTERABLE);
			b = false;
		}
		if (Glb.getUtil().notEqual(isSecure(), old2.isSecure())) {
			r.add(Lang.USER_SECURE, Lang.ERROR_UNALTERABLE,
					"secure=" + isSecure() + " oldSecure=" + old2.isSecure());
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateNaturalityConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonNaturalityConcrete(r)) {
			b = false;
		}

		if (avatarConfig != null) {
			if (!avatarConfig.validateAtUpdate(r)) {
				b = false;
			}
		}

		//抽象クラスでも類似した判定をしているが
		//NullRecycleIdが許容できない事がこの文脈で判明するので
		if (getRecycleId() != null
				&& IdObjectDBI.getNullId().equals(getRecycleId())) {
			r.add(Lang.IDOBJECT_RECYCLE_ID, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReferenceNaturalityConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (avatarConfig != null) {
			if (!avatarConfig.validateReference(r, txn)) {
				b = false;
			}
		}
		return b;
	}
}
