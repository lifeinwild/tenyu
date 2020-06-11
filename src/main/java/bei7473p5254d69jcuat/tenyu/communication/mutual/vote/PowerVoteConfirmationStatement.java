package bei7473p5254d69jcuat.tenyu.communication.mutual.vote;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.TurnBaseMessage.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.vote.*;
import glb.*;
import glb.Conf.*;
import glb.util.*;

public class PowerVoteConfirmationStatement
		extends TurnBaseStatement<PowerVoteSequence> {
	/**
	 * 自分の主張値
	 */
	private PowerVoteValue myValue;

	public PowerVoteConfirmationStatement(PowerVoteSequence seq) {
		this.seq = seq;
	}

	@Override
	protected void start(long statementStart) {
		this.myValue = seq.getMyValueTmp();
	}

	@Override
	public List<SendData> send() {
		PowerVoteConfirmationMessage c = new PowerVoteConfirmationMessage();
		c.setSenderValue(myValue);
		return send(c, seq.getNeighborList().getNeighborsUnsafe());
	}

	@Override
	public int getTurnCountMax() {
		return Glb.getConf().getRunlevel().equals(RunLevel.DEV) ? 3 : 12;
	}

	@Override
	public void interaction(int turn, List<Received> fromNeighbors,
			long interactionStart) {
		if (myValue == null) {
			Glb.getLogger().error("", new IllegalStateException());
			return;
		} else {
			Glb.debug("counts=" + myValue.getPowers());
		}

		//確認段階では自分の値を加えていない。どういう影響があるか難しいが
		//この段階では収束させたいので、自分の値よりもとにかく多数派への収束を優先する。
		//TODO とはいえそれが収束を早めたり安定させるのか評価できていない

		//扱いやすい構造にする
		Map<Integer, Map<Double, Integer>> counts = PowerVoteSequence
				.createCounts(fromNeighbors);

		//次のターンの自分の主張値
		HashMap<Integer, Double> next = new HashMap<>();

		//各種計算アルゴリズム
		Util u = Glb.getUtil();

		//各ユーザーIDの分散、平均を算出、異常値を排除、その後の平均を算出
		for (Entry<Integer, Map<Double, Integer>> e : counts.entrySet()) {
			try {
				//多数決
				Integer choiceId = e.getKey();
				Double power = u.majority(e.getValue());
				if (power == null || power == 0)
					continue;
				next.put(choiceId, power);
			} catch (Exception ex) {
				continue;
			}
		}

		//相互作用関数の出力を次のターンの自分の主張値に
		myValue.setPowers(next);
		Glb.debug("powers=" + myValue.getPowers());
	}

	@Override
	public boolean isSupport(DistributedVoteMessage m) {
		return m instanceof PowerVoteConfirmationMessage;
	}

	@Override
	protected void end(long statementStart) {
		if (myValue == null)
			return;

		DistributedVoteResult result = seq.getResult();
		if (result == null)
			return;

		DistributedVoteManager man = Glb.getMiddle().getDistributedVoteManager();
		if (man == null)
			return;

		DistributedVote vote = man.get(seq.getDistributedVoteId());
		if (vote == null)
			return;

		result.setMajority(myValue);
		result.setFinished(true);
		man.putResult(result, seq);

		if (!vote.isSustainable()) {
			seq.unschedule();
			vote.setEnable(false);
			man.put(vote);
		}
	}

	@Override
	public long getEndTime() {
		return 1000;
	}

}
