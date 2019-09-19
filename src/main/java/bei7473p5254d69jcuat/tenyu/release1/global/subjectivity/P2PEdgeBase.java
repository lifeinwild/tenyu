package bei7473p5254d69jcuat.tenyu.release1.global.subjectivity;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.env.*;

public class P2PEdgeBase implements Storable {

	/**
	 * 共通鍵交換または共通鍵梱包使用時のロックオブジェクトでもある
	 */
	protected CommonKeyExchangeState commonKeyExchangeState = new CommonKeyExchangeState();

	/**
	 * P2Pネットワークで全体で一意な値を作る事は簡単ではない。
	 * そこで、各ノードの元でlong型の乱数をedgeIdとして設定する。
	 * 各ノードは互いに自分が作成したedgeIdを平文で送り合う。
	 * もしedgeIdが無いと、暗号化されたメッセージをどの鍵で複合化すればいいのか
	 * 分からず、総当たりで調べる事になる。
	 * edgeIdはその総当たりを無くす意味しかない。
	 * 認証はメッセージの暗号化や電子署名によって提供されるべきで、
	 * edgeIdによって提供されるべきではない。
	 * 低確率で重複するが、その場合新しい方は作成できない。
	 *
	 * TODO:ノードIDとの使い分け
	 * そもそもリレーションとかエッジ的なものに一意なIDを振る場合、
	 * 綺麗に設計できない印象がある。IdObject系でもそういうものがある。
	 * 対応していけないほど複雑というわけではない、ただ綺麗ではない。
	 * リレーション中の各データに検索性を与えるのもコストがかかったり、
	 * コードが増える。
	 *
	 * IDに向いている情報の性質とは
	 * ・絶対に更新されない
	 * ・出来るだけ小さい
	 * ・一意か、少なくとも実用上一意な値
	 * ・なりすましや混同などが起こらない、認証問題が生じにくい。
	 *
	 * ノードIDは基本的に変わらないが64バイトで、エッジIDは8バイトである。
	 * ノードIDはエンドユーザーがノード番号を適切に設定しなければ一意ではない。
	 * ノードIDはネットワークに広まるのでなりすましの潜在的可能性があるが、
	 * エッジIDは、特定のノードを標的としてそのノードになりすまそうとした場合、
	 * 各ノード毎に標的ノードに与えられたエッジIDが異なるので困難である。
	 *
	 * 優劣をつけられないし、既にエッジIDとノードIDの混合型で作ったのでこのままでやろうと思う。
	 * 当初ノードIDは512+4バイトだった。ハッシュ値にすればいいというアイデアを得て
	 * 64バイトになったが、今でもエッジIDとのサイズ差はある。
	 *
	 * ちなみにエッジIDがある事でノード番号をP2Pエッジを保ったまま更新できる。
	 * ＞しかしやっていない。必要性が不明
	 */
	protected long edgeId;

	/**
	 * edgeIdが-1ならP2PEdgeを抽出する条件としてのP2PEdgeの場合か、
	 * 更新情報をP2PEdgeで表現している場合などで、近傍を表していない。
	 * そのような場合のP2PEdgeは近傍リストに加えられる事は無い。
	 * つまり、たまたま検索条件においてP2PEdgeと同じ情報の組み合わせを使うから
	 * P2PEdgeを使っている場合等に使う。
	 *
	 * TODO: 継承したP2PEdgeConditionクラスを使うべきか？
	 */
	private static final long specialEdgeId = -1L;

	public static long getSpecialEdgeId() {
		return specialEdgeId;
	}

	/**
	 * 印象値。単に最新の証明スコアを使うのではなく、過去の値が影響するようにする。
	 * そうすることで古参ノードをより信用するという戦略になる。
	 * しかし、過去の値の影響は限定的で、新規ノードは古参ノードにいずれ追いつける。
	 * その他、通信失敗など様々な条件で印象値は低下する。
	 */
	protected int impression = 0;

	/**
	 * 最後に通信した日時
	 */
	protected long lastCommunication;

	/**
	 * 最新の総合スコア
	 */
	protected int latestScore;

	/**
	 * 自分からこのノードに与えたプロセッサ証明スコアの合計値
	 */
	protected int processorScoreTotal;

	public void addLatestScore(int add) {
		latestScore += add;
	}

	public CommonKeyExchangeState getCommonKeyExchangeState() {
		return commonKeyExchangeState;
	}

	public long getEdgeId() {
		return edgeId;
	}

	public int getImpression() {
		return impression;
	}

	public long getLastCommunication() {
		return lastCommunication;
	}

	public int getLatestScore() {
		return latestScore;
	}

	public int getProcessorScoreTotal() {
		return processorScoreTotal;
	}

	/**
	 * @return 最後に通信したのが５分以内か
	 */
	public boolean isConnectedIn5Minute() {
		return System.currentTimeMillis() - lastCommunication < 1000L * 60 * 5;
	}

	/**
	 * @return	最後に通信したのが8時間以内か
	 */
	public boolean isConnectedIn8Hour() {
		return System.currentTimeMillis() - lastCommunication < 1000L * 60 * 60
				* 8;
	}

	/**
	 * @return	最後に通信したのが1日以内か
	 */
	public boolean isCommonKeyExchangeIn1Day() {
		return System.currentTimeMillis()
				- commonKeyExchangeState.getUpdateEnd() < 1000L * 60 * 60 * 24;
	}

	/**
	 * @return　共通鍵交換が最近成功したか
	 */
	public boolean isProved() {
		return commonKeyExchangeState.getUpdateEnd() != 0;
	}

	public void setCommonKeyInfo(CommonKeyExchangeState commonKeyInfo) {
		this.commonKeyExchangeState = commonKeyInfo;
	}

	public void setEdgeId(long edgeId) {
		this.edgeId = edgeId;
	}

	public void setLastCommunication(long lastCommunication) {
		this.lastCommunication = lastCommunication;
	}

	public void setLatestScore(int latestScore) {
		this.latestScore = latestScore;
	}

	/**
	 * 合計値や印象値を変更する
	 * @param processorScore
	 */
	public void updateProcessorScore(int processorScore) {
		processorScoreTotal += processorScore;
		impression = (int) (processorScore + impression * 0.998);
		if (impression > impressionMax)
			impression = impressionMax;
	}

	/**
	 * 印象値の最大値
	 */
	private int impressionMax = 1000 * 50;

	public void setProcessorScoreTotal(int processorScoreTotal) {
		this.processorScoreTotal = processorScoreTotal;
	}

	public void updateLastCommunication() {
		lastCommunication = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (edgeId ^ (edgeId >>> 32));
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
		P2PEdgeBase other = (P2PEdgeBase) obj;
		if (edgeId != other.edgeId)
			return false;
		return true;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (latestScore < 0) {
			r.add(Lang.P2PEDGEBASE_LATESTSCORE, Lang.ERROR_INVALID);
			b = false;
		}
		if (lastCommunication < 0) {
			r.add(Lang.P2PEDGEBASE_LASTCOMMUNICATION, Lang.ERROR_INVALID);
			b = false;
		}
		if (commonKeyExchangeState == null) {
			r.add(Lang.P2PEDGEBASE_COMMONKEYEXCHANGESTATE, Lang.ERROR_EMPTY);
			b = false;
		}
		if (processorScoreTotal < 0) {
			r.add(Lang.P2PEDGEBASE_PROCESSORSCORETOTAL, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

}
