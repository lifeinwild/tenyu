package bei7473p5254d69jcuat.tenyu.communication.mutual.vote;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.TurnBaseMessage.*;
import glb.*;
import glb.Conf.*;
import glb.util.*;

public class PowerVoteStatement extends TurnBaseStatement<PowerVoteSequence> {
	public PowerVoteStatement(PowerVoteSequence seq) {
		this.seq = seq;
	}

	@Override
	protected void start(long statementStart) {
		try {
			myValue = seq.getOrCreateMyValue();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
	}

	@Override
	protected void end(long statementStart) {
		seq.setMyValueTmp(myValue);
	}

	@Override
	public int getTurnCountMax() {
		return Glb.getConf().getRunlevel().equals(RunLevel.DEV) ? 6 : 20;
	}

	@Override
	public List<SendData> send() {
		if (myValue == null) {
			return null;
		}

		PowerVoteMessage c = new PowerVoteMessage();
		c.setSenderValue(myValue);
		return send(c, seq.getNeighborList().getNeighborsUnsafe());
	}

	/**
	 * 自分が送信する主張値
	 */
	private PowerVoteValue myValue;

	@Override
	public void interaction(int turn, List<Received> fromNeighbors,
			long interactionStart) {
		if (fromNeighbors == null || fromNeighbors.size() == 0
				|| myValue == null)
			return;

		//投票一覧を扱いやすい構造に整理
		Map<Integer, Map<Double, Integer>> counts = PowerVoteSequence
				.createCounts(fromNeighbors);
		if (counts.size() == 0) {
			return;
		}
		Glb.debug("counts=" + counts);

		//平均信用
		int creditAve = 0;
		for (Received r : fromNeighbors) {
			if (r.getEdgeByInnermostPackage() == null)
				continue;
			creditAve += r.getEdgeByInnermostPackage().credit();
		}
		creditAve /= fromNeighbors.size();
		//自分をどれだけ信用するか
		//自分を平均より信用すると収束値が正確な平均値にならなくなる
		//DEVでは等倍
		int creditSelf = 0;
		try {
			creditSelf = (int) (creditAve * Glb.getMiddle().getObjeCatchUp()
					.getCreditSelfMultiplier());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

		//直前の自分の主張値を入れる。
		for (Entry<Integer, Double> e : myValue.getPowers().entrySet()) {
			Integer choiceId = e.getKey();
			Double power = e.getValue();
			Map<Double, Integer> powers = counts.get(choiceId);
			if (powers == null) {
				powers = new HashMap<>();
				counts.put(choiceId, powers);
			}

			Integer totalCredit = powers.get(power);
			if (totalCredit == null) {
				totalCredit = 0;
			}
			totalCredit += creditSelf;
			powers.put(power, totalCredit);
		}

		//次のターンの自分の主張値
		HashMap<Integer, Double> next = new HashMap<>();

		int turnMax = getTurnCountMax() - 1;
		if (turnMax < 1)
			turnMax = 1;
		//分散を収束させるための、許容範囲を決定する閾値
		//最初1.0、最後0.0
		double threshold = (double) (turnMax - turn) / turnMax;

		//各種計算アルゴリズム
		Util u = Glb.getUtil();
		//各選択肢IDの分散、平均を算出、異常値を排除、その後の平均を算出
		for (Entry<Integer, Map<Double, Integer>> choiceIdPowerWeight : counts
				.entrySet()) {
			try {
				Integer choiceId = choiceIdPowerWeight.getKey();
				//分配割合：信用
				Map<Double,
						Integer> powerCredits = choiceIdPowerWeight.getValue();
				//加重平均
				double ave = u.average(powerCredits);
				//異常値が除外された部分集合
				Map<Double, Integer> filtered = new HashMap<>();
				for (Entry<Double, Integer> powerCredit : powerCredits
						.entrySet()) {
					//影響力
					Double power = powerCredit.getKey();
					//信用
					Integer credit = powerCredit.getValue();
					//加重平均からの差
					double distance = Math.abs(ave - power);
					//異常値が除外される
					if (distance > threshold || Double.isNaN(power))
						continue;

					filtered.put(power, credit);
				}

				//異常値が除外された集合の加重平均
				double filteredAveTmp = u.average(filtered);
				//四捨五入	1000分の1の桁で起こす
				int order = 1000;
				double filteredAve = (double) Math.round(filteredAveTmp * order)
						/ order;

				//NaN除外
				if (Double.isNaN(filteredAve) || filteredAve == 0) {
					Glb.debug(() -> "NaN choiceId=" + choiceId
							+ " powerCredits=" + powerCredits + "ave1=" + ave);
					continue;
				}

				//次のターンの自分の主張値に登録
				next.put(choiceId, filteredAve);
			} catch (Exception ex) {
				continue;
			}
		}

		if (!PowerVoteMessage.validatePowers(next)) {
			Glb.debug(next + "");
		}

		//相互作用関数の出力を次のターンの自分の主張値に
		myValue.setPowers(next);
		Glb.debug("powers=" + myValue.getPowers());
	}

	@Override
	public boolean isSupport(DistributedVoteMessage m) {
		return m instanceof PowerVoteMessage;
	}

}
