package bei7473p5254d69jcuat.tenyu.release1.global.middle;

import java.net.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;
import jetbrains.exodus.env.*;

/**
 * ノードのアドレスやポートの情報。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AddrInfo implements Storable {
	private static int fqdnMax = 50;

	public static int getFqdnMax() {
		return fqdnMax;
	}

	public static int getMaxSize() {
		return fqdnMax + 16 + 4 + Glb.getConst().getRsaKeySizeByteSecure() + 8
				+ AssignedRange.getMaxSize();
	}

	public static void setFqdnMax(int fqdnMax) {
		AddrInfo.fqdnMax = fqdnMax;
	}

	public AddrInfo() {
	}

	public AddrInfo(byte[] address, String fqdn, int p2pPort, int gamePort,
			int tenyutalkPort) {
		super();
		this.address = address;
		this.fqdn = fqdn;
		this.p2pPort = p2pPort;
		this.gamePort = gamePort;
	}

	@Override
	public AddrInfo clone() {
		AddrInfo r = new AddrInfo();
		r.setAddress(address);
		r.setFqdn(fqdn);
		r.setGamePort(gamePort);
		r.setP2pPort(p2pPort);
		return r;
	}

	@Override
	public String toString() {
		return "AddrInfo [address=" + Arrays.toString(address) + ", fqdn="
				+ fqdn + ", p2pPort=" + p2pPort + ", gamePort=" + gamePort
				+ ", tenyutalkPort=" + tenyutalkPort + "]";
	}

	/**
	 * ホスト名から最新のアドレスを設定する
	 */
	public void updateAddrByFqdn() {
		if (fqdn == null)
			return;
		try {
			InetAddress ia = InetAddress.getByName(fqdn);
			address = ia.getAddress();
		} catch (UnknownHostException e) {
			Glb.getLogger().warn("", e);
		}
	}

	/**
	 * IPアドレス
	 */
	private byte[] address;

	/**
	 * 一部のノードはDNSに登録し近傍にDNSを通知、
	 * 他ノードは継続的にそこから他ノードのアドレスを得る。
	 * 悪意あるドメイン名しか知らないノードは分離されたネットワークに入れられる可能性がある。
	 * しかし、CPU証明と分散合意の近傍数制限をクリアしなければならないので、
	 * 汚染できるノード数は結局CPUの性能量によって制限される。
	 * ネットワーク全体ではなく一部ノードが不正なデータを見せられるリスクに過ぎない。
	 * 管理者等の重要ノードはTenyu.confの手動ドメイン設定で安全を確保する。
	 */
	private String fqdn;
	/**
	 * P2P通信における受信用ポート
	 */
	private int p2pPort;

	/**
	 * ゲーム用ポート
	 */
	private int gamePort;

	/**
	 * Tenyutalk用ポート
	 */
	private int tenyutalkPort;

	private byte[] getAddr() {
		byte[] addr = null;
		if (address == null) {
			try {
				addr = InetAddress.getByName(fqdn).getAddress();
			} catch (UnknownHostException e) {
				Glb.getLogger().error("", e);
			}
		} else {
			addr = address;
		}
		if (addr == null) {
			Glb.getLogger().error("No address", new Exception());
			return null;
		}
		return addr;
	}

	private InetSocketAddress getISA(int port) {
		if (port <= 0)
			throw new IllegalArgumentException();

		try {
			return new InetSocketAddress(InetAddress.getByAddress(getAddr()),
					port);
		} catch (UnknownHostException e) {
			Glb.getLogger().error("", e);
		}
		return null;
	}

	/**
	 * ゲームポートはゲームクライアントが起動されないと接続できないので、
	 * 接続確認等は基盤ソフトウェア側で出来ない。
	 * @return
	 */
	public InetSocketAddress getISAGamePort() {
		return getISA(gamePort);
	}

	/**
	 * 基本的にp2pポートは基盤ソフトウェアが起動している間接続可能
	 * @return
	 */
	public InetSocketAddress getISAP2PPort() {
		return getISA(p2pPort);
	}

	public int getP2pPort() {
		return p2pPort;
	}

	public void setP2pPort(int p2pPort) {
		this.p2pPort = p2pPort;
	}

	public int getGamePort() {
		return gamePort;
	}

	public void setGamePort(int gamePort) {
		this.gamePort = gamePort;
	}

	public String getFqdn() {
		return fqdn;
	}

	public void setFqdn(String fqdn) {
		//極端に長いFQDNを拒否する
		//if (fqdn == null || fqdn.length() > fqdnMax)
		//return;

		this.fqdn = fqdn;
	}

	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public boolean validateAtCommon(ValidationResult r) {
		boolean b = true;

		if (fqdn != null) {
			if (fqdn.length() > Glb.getConst().getFqdnMax()) {
				r.add(Lang.FQDN, Lang.ERROR_TOO_LONG,
						fqdn.length() + " / " + Glb.getConst().getFqdnMax());
				b = false;
			} else {
				if (!Naturality.validateTextAllCtrlChar(Lang.FQDN, fqdn, r)) {
					b = false;
				} else if (fqdn.contains("..") || fqdn.startsWith("/")) {
					r.add(Lang.FQDN, Lang.ERROR_PARENT_PATH, "fqdn=" + fqdn);
					b = false;
				}
			}
		}

		if (p2pPort <= 0) {
			r.add(Lang.P2PPORT, Lang.ERROR_INVALID, "p2pPort=" + p2pPort);
			b = false;
		}
		if (gamePort <= 0) {
			r.add(Lang.GAMEPORT, Lang.ERROR_INVALID, "gamePort=" + gamePort);
			b = false;
		}
		if (tenyutalkPort <= 0) {
			r.add(Lang.TENYUTALKPORT, Lang.ERROR_INVALID,
					"tenyutalkPort=" + tenyutalkPort);
			b = false;
		}

		if (!validateAddr(address, r)) {
			b = false;
		}

		return b;
	}

	public static boolean validateAddr(byte[] address, ValidationResult r) {
		boolean b = true;
		if (address == null) {
			//必ずしもあるわけではない。例えばFQDNがあれば必要無い
			//r.add(Lang.IPADDR, Lang.ERROR_EMPTY);
			//b = false;
		} else {
			if (address.length > Glb.getConst().getAddrMax()) {
				r.add(Lang.IPADDR, Lang.ERROR_TOO_LONG,
						"size=" + address.length);
				b = false;
			}
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
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(address);
		result = prime * result + ((fqdn == null) ? 0 : fqdn.hashCode());
		result = prime * result + gamePort;
		result = prime * result + p2pPort;
		result = prime * result + tenyutalkPort;
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
		AddrInfo other = (AddrInfo) obj;
		if (!Arrays.equals(address, other.address))
			return false;
		if (fqdn == null) {
			if (other.fqdn != null)
				return false;
		} else if (!fqdn.equals(other.fqdn))
			return false;
		if (gamePort != other.gamePort)
			return false;
		if (p2pPort != other.p2pPort)
			return false;
		if (tenyutalkPort != other.tenyutalkPort)
			return false;
		return true;
	}

	public int getTenyutalkPort() {
		return tenyutalkPort;
	}

	public void setTenyutalkPort(int tenyutalkPort) {
		this.tenyutalkPort = tenyutalkPort;
	}
}