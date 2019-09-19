package bei7473p5254d69jcuat.tenyu.release1.communication.mutual;

/**
 * 通常のシーケンスより優先度が高く、
 * 特殊シーケンスが実行されている間、通常シーケンスの実行がブロックされる。
 * 通常シーケンスが実行されていても特殊シーケンスの実行はブロックされない。
 *
 * 特殊シーケンスは通常シーケンスの影響を排除するために開始直後5分間程度
 * 無処理時間を設ける場合がある。
 *
 * 特殊シーケンスとして想定されるのはフロー計算等。
 *
 * 特殊シーケンス同士の衝突は想定されないので開始時間を十分にずらすなど
 * 手動調節が必要。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class P2PSpecialSequence extends P2PSequence {
	@Override
	public int getPriority() {
		return 7;
	}

}
