package bei7473p5254d69jcuat.tenyu.model.release1.subjectivity;

import java.net.*;
import java.nio.*;
import java.security.*;
import java.util.*;

import com.maxmind.geoip2.model.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

public class P2PNode implements Storable {
	/**
	 * thisは後進地域ノードか
	 */
	protected boolean discriminated = Glb.getConf().isDiscriminated();

	/**
	 * 最後に全体運営者投票に自分の投票値を設定した日時
	 * 自称値
	 * この値は自称だが、改ざんするより投票値を設定する方が労力的に簡単だろうから問題無い。
	 * この値がtrueだと定期的な近傍へのプレゼントの対象になるなど恩恵がある。
	 */
	private long lastVoteDateTenyuManger;

	/**
	 * 同じ公開鍵で複数のノードをP2Pネットワーク上で実行できる。
	 * ある公開鍵の元でnodeNumberは一意である。
	 * その一意性はノード実行者が設定ファイルTenyu.confでnodeIdを一意に設定する
	 * 事によって保証される。
	 */
	private int nodeNumber;

	/**
	 * addrInfoはConfで作られネットワークに出回る。
	 * 認識処理がaddr+p2pPortのみで呼び出せるようになっているので、
	 * addrInfoは一部のメンバーが未設定の場合がある。
	 * しかし認識に成功した場合、相手のノードのConfが作成する情報と一致する。
	 * ただしグローバルIPアドレスの設定値はConfが作れないので別途設定される。
	 * これを利用する場合常にメンバー変数についてnullを警戒すべきだし、
	 * 定期的に再設定するような仕様を持つ必要がある。
	 */
	private AddrInfo addrInfo = new AddrInfo();

	/**
	 * このノードの公開鍵
	 * P2PノードIDの一部であり、これがnullならP2PノードIDは一意性が無く、
	 * ほぼセットアップされていないノードであることを意味する。
	 */
	private ByteArrayWrapper pubKey;

	private AssignedRange range = new AssignedRange();
	/**
	 * 公開鍵タイプ、と同時に
	 * このノードがどのような環境で動作しているかを意味する。
	 */
	private KeyType type;

	/**
	 * ノード情報の最終更新日時
	 */
	protected long updateDate;

	/**
	 * 自分は最新の客観を持っていると自称するか
	 * 2通りの設定経路がある。
	 * 同調処理における整合性情報取得処理または定期的な通信を通じて最新情報が設定される
	 */
	private boolean veteran = false;

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof P2PNode))
			return false;
		P2PNode n = (P2PNode) arg0;

		return getP2PNodeId().equals(n.getP2PNodeId());
	}

	public byte[] getAddr() {
		if (addrInfo == null)
			return null;
		return addrInfo.getAddress();
	}

	public void setAddr(byte[] addr) {
		if (addrInfo == null)
			addrInfo = new AddrInfo();
		addrInfo.setAddress(addr);
	}

	public void setP2pPort(int p2pPort) {
		if (addrInfo == null)
			addrInfo = new AddrInfo();
		addrInfo.setP2pPort(p2pPort);
	}

	public void setFqdn(String fqdn) {
		if (addrInfo == null)
			addrInfo = new AddrInfo();
		addrInfo.setFqdn(fqdn);
	}

	public String getFqdn() {
		if (addrInfo == null)
			return null;
		return addrInfo.getFqdn();
	}

	public InetSocketAddress getISAP2PPort() {
		return addrInfo.getISAP2PPort();
	}

	/**
	 * このオブジェクトのaddrInfoの状態を変えさせたくないので
	 * cloneメソッドを作成した。
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public AddrInfo getAddrInfoClone() {
		return addrInfo.clone();
	}

	public long getLastVoteDateTenyuManger() {
		return lastVoteDateTenyuManger;
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	public NodeIdentifierP2PEdge getP2PNodeId() {
		return new NodeIdentifierP2PEdge(pubKey, nodeNumber);
	}

	public int getP2pPort() {
		if (addrInfo == null)
			throw new IllegalStateException();
		return addrInfo.getP2pPort();
	}

	public ByteArrayWrapper getPubKey() {
		return pubKey;
	}

	public AssignedRange getRange() {
		return range;
	}

	public KeyType getType() {
		return type;
	}

	public long getUpdateDate() {
		return updateDate;
	}

	/**
	 * 条件に一致するか
	 */
	public boolean is(P2PNode condition) {
		if (condition != null && condition.getRange() != null
				&& getRange() != null) {
			//条件に指定された分担範囲と関連があるか
			if (!condition.getRange().isAncestorOrDescendants(getRange())) {
				return false;
			}
		}

		/*
				if (condition.getAddr() != null) {
					if (!Arrays.equals(condition.getAddr(), getAddr()))
						return false;
				}

				if (condition.getCommonKeyInfo() != null) {
					if (!condition.getCommonKeyInfo().equals(getCommonKeyInfo()))
						return false;
				}

				if (condition.getConnectionCount() != 0) {
					if (condition.getConnectionCount() != getConnectionCount())
						return false;
				}

				if (condition.getConnectionFailedCount() != 0) {
					if (condition
							.getConnectionFailedCount() != getConnectionFailedCount())
						return false;
				}

				if (condition.getFqdn() != null) {
					if (condition.getFqdn().equals(getFqdn()))
						return false;
				}

				if (condition.getEdgeId() != 0) {
					if (condition.getEdgeId() != getEdgeId())
						return false;
				}

				if (condition.getFromOther() != null) {
					if (!condition.getFromOther().equals(getFromOther()))
						return false;
				}

				if (condition.getImpression() != 0) {
					if (condition.getImpression() != getImpression())
						return false;
				}

				if (condition.getIntroducer() != null) {
					if (condition.getIntroducer().equals(getIntroducer()))
						return false;
				}
		*/

		return true;
	}

	public boolean isDiscriminated() {
		return discriminated;
	}

	/**
	 * 自分か
	 */
	public boolean isMe() {
		return equals(Glb.getSubje().getMe());
	}

	public boolean isVeteran() {
		return veteran;
	}

	public void setAddrInfo(AddrInfo addrInfo) {
		this.addrInfo = addrInfo;
		updateDiscriminated();
	}

	public void setAddrInfo(P2PNode n) {
		setAddrInfo(n.getAddrInfo());
	}

	public void setDiscriminated(boolean discriminated) {
		this.discriminated = discriminated;
	}

	public void setLastVoteDateTenyuManger(long lastVoteDateTenyuManger) {
		this.lastVoteDateTenyuManger = lastVoteDateTenyuManger;
	}

	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	public void setPubKey(byte[] pubKey) {
		this.pubKey = new ByteArrayWrapper(pubKey);
	}

	public void setPubKey(ByteArrayWrapper pubKey) {
		this.pubKey = pubKey;
	}

	public void setRange(AssignedRange range) {
		this.range = range;
	}

	public void setType(KeyType type) {
		this.type = type;
	}

	public void setVeteran(boolean veteran) {
		this.veteran = veteran;
	}

	@Override
	public String toString() {
		return addrInfo == null ? "" : addrInfo.toString();
	}

	/**
	 * このノードの客観情報を更新する。
	 * @param latest	更新情報。主に近傍から通信を通じて与えられる。
	 *
	 * TODO:全情報をここで更新しているわけではない。むしろnodeIdの元情報は
	 * 更新できない。typeもここで更新していない。この設計は妥当か？
	 * 大きな問題は生じそうにない。これで更新されないものは呼び出し元で適宜更新している。
	 */
	public void update(P2PNode latest) {
		if (latest == null || !latest.validateAtCommon()) {
			Glb.getLogger().warn("", new Exception());
			return;
		}
		if (!getP2PNodeId().equals(latest.getP2PNodeId()))
			return;
		if (latest.getType() != null)
			setType(latest.getType());
		if (latest.getAddrInfo() != null)
			setAddrInfo(latest.getAddrInfo());
		if (latest.getRange() != null)
			setRange(latest.getRange());
		setVeteran(latest.isVeteran());
		updateDate = System.currentTimeMillis();
	}

	private AddrInfo getAddrInfo() {
		return addrInfo;
	}

	/**
	 * ホスト名から最新のアドレスを設定する
	 */
	public void updateAddrByFqdn() {
		if (addrInfo != null)
			addrInfo.updateAddrByFqdn();
	}

	public void updateDiscriminated() {
		//ITインフラが低性能な地域か
		if (Glb.getGeo() != null && getAddr() != null) {
			try {
				InetAddress a = InetAddress.getByAddress(getAddr());
				if (a.isLoopbackAddress() || a.isAnyLocalAddress()
						|| a.isLinkLocalAddress() || a.isSiteLocalAddress()) {
					discriminated = false;
					return;
				}

				//IPアドレスから国を特定
				CountryResponse c = Glb.getGeo().country(a);
				if (c != null) {
					//国からITインフラの水準を決定、それに応じてdiscriminatedを決定
					discriminated = !Glb.getConst()
							.isDevelopedRegions(c.getCountry().getIsoCode());
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}
	}

	/**
	 * @return	一通りの情報が設定されているか
	 */
	public final boolean validateAtCommon() {
		return true;
		//必ず設定されていると言える情報は無い。P2PNodeの各状態は実行時に様々なタイミングで設定されていく
		//		return addr != null && addr.length > 0 && p2pPort > 0 && pubKey != null
		//				&& pubKey.getByteArray().length > 0 && range != null;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCommon();
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCommon();
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

	/**
	 * P2Pネットワーク上のノードの識別子。
	 * 公開鍵とノード番号に依存する。
	 * 客観形成の前でも使用できる。
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	public static class NodeIdentifierP2PEdge implements NodeIdentifier {
		/**
		 * 公開鍵とノード番号に依存したハッシュ値をノードIDとする
		 * @param pubKey
		 * @param nodeNumber
		 * @return	ノードID
		 */
		public static byte[] getP2PNodeIdRow(byte[] pubKey, int nodeNumber) {
			if (pubKey == null) {
				//ここで得られるサイズは実際の鍵サイズより小さいが、ハッシュ値を取るので問題無い
				//鍵サイズ≠鍵オブジェクトのbyte[]符号化時のサイズ
				pubKey = new byte[User.getRsaKeySizeByteBySecure(
						Glb.getConf().isSecureUser())];
			}
			int nodeNumberSize = Integer.BYTES;

			ByteBuffer buf = ByteBuffer
					.allocate(pubKey.length + nodeNumberSize);
			buf.put(pubKey).putInt(nodeNumber);

			MessageDigest md = Glb.getUtil().getMDSecure();
			return md.digest(buf.array());
		}

		/**
		 * 公開鍵とnodeNumberのハッシュ値で、サイズ長が保証される。
		 * サイズ長は{@link Const#getHashSize()}で定義される。
		 * 公開鍵の重複確率の低さと、同じ公開鍵で動作しているノードは
		 * その公開鍵の保有者の責任によってノードIDが分けられる事から、
		 * P2Pネットワーク全体で一意なノード識別子になる。
		 * @return	P2Pネットワーク全体で一意なノード識別子
		 */
		private byte[] identifier;

		@SuppressWarnings("unused")
		private NodeIdentifierP2PEdge() {
		}

		public NodeIdentifierP2PEdge(byte[] identifier) {
			this.identifier = identifier;
		}

		public NodeIdentifierP2PEdge(byte[] pubKey, int nodeNumber) {
			identifier = getP2PNodeIdRow(pubKey, nodeNumber);
		}

		public NodeIdentifierP2PEdge(ByteArrayWrapper pubKey, int nodeNumber) {
			identifier = getP2PNodeIdRow(
					pubKey != null ? pubKey.getByteArray() : null, nodeNumber);
		}

		@Override
		public AddrInfo getAddrWithCommunication() {
			return getAddr();//TODO 他ノードに問い合わせて探す
		}

		@Override
		public AddrInfo getAddr() {
			P2PEdge e = Glb.getSubje().getNeighborList().getNeighbor(this);
			if (e == null)
				return null;
			return e.getNode().getAddrInfo();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeIdentifierP2PEdge other = (NodeIdentifierP2PEdge) obj;
			if (!Arrays.equals(identifier, other.identifier))
				return false;
			return true;
		}

		@Override
		public byte[] getIdentifier() {
			return identifier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(identifier);
			return result;
		}

		@Override
		public String toString() {
			return "NodeIdentifierP2PEdge [identifier="
					+ Arrays.toString(identifier) + "]";
		}

	}

}
