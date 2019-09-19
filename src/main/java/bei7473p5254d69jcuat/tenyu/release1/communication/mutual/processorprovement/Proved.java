package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement.ProcessorProvementSequence.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

/**
 * 証明成功通知。
 * @author exceptiontenyu@gmail.com
 *
 */
public class Proved extends TurnBaseMessage {
	private int score;

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	@Override
	protected final boolean validateTurnBaseConcrete(Message m) {
		return score > 0;
	}

	public static class ProvedStatement
			extends TurnBaseStatement<ProcessorProvementSequence> {

		public ProvedStatement(ProcessorProvementSequence seq) {
			this.seq = seq;
		}

		@Override
		public boolean isSupport(DistributedVoteMessage c) {
			return c instanceof Proved;
		}

		@Override
		public List<SendData> send() {
			//送信先と送信情報
			List<SendData> r = new ArrayList<>();
			//Answer段階で作成された証明成功者情報からrを作成
			for (Notification e : seq.getScores()) {
				Proved c = new Proved();
				c.setScore(e.getScore());
				r.add(new SendData(e.getEdge(), c));
			}
			return r;
		}

		@Override
		public void interaction(int turn, List<Received> fromNeighbors,
				long interactionStart) {
			//近傍から届いた証明成功通知を処理する
			for (Received r : fromNeighbors) {
				try {
					Communicatable c = r.getMessage().getContent();
					if (!(c instanceof Proved))
						continue;
					Proved p = (Proved) c;
					P2PEdge n = r.getEdgeByInnermostPackage();
					//相手から自分へのスコアを記録
					n.getFromOther().updateProcessorScore(p.getScore());
				} catch (Exception e) {
					Glb.debug(e);
					continue;
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + score;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proved other = (Proved) obj;
		if (score != other.score)
			return false;
		return true;
	}

}
