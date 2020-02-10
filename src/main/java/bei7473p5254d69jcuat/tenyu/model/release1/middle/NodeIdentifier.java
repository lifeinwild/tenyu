package bei7473p5254d69jcuat.tenyu.model.release1.middle;

/**
 * ノード識別子
 * ノードとはP2Pネットワーク上で動作しているプロセス。
 * 1ユーザーが多数のノードを実行する場合もある。
 * その場合ノード番号をユーザーが設定する事で区別する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface NodeIdentifier {
	/**
	 * 自分が持っている情報から探す
	 * @return	このノード識別子で特定されるノードのアドレスとポート
	 */
	AddrInfo getAddr();
	/**
	 * 他ノードが持っている情報からも探す
	 * @return	このノード識別子で特定されるノードのアドレスとポート
	 */
	AddrInfo getAddrWithCommunication();

	byte[] getIdentifier();
}
