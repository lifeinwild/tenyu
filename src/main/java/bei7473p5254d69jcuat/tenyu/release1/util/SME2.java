package bei7473p5254d69jcuat.tenyu.release1.util;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game.*;

/**
 * エンジニアリングメモからコピペ
 * 	・SME2実装。
 * 		・SMEの問題点http://www.tckerrigan.com/Misc/Multiplayer_Elo/
 * 			・同じ順位を扱えない。
 * 			・1位がR1600、2位がR1600、3位がR1600、4位がR2000の場合、2位より3位の方がRが上がる。
 * 		これら反省点を踏まえてSME2を考える。
 * 		・自分より下位の全てのプレイヤーに勝利したものとしてRが増加する。
 * 		・自分より上位のプレイヤーについても同様にRが減少する。
 * 		つまりマルチプレイヤーゲームは1試合で大幅にRが変動しうる。
 * 		同じ順位なら引き分けとして計算する。
 * 		レーティングは引き分けでも変動する。
 * 		なおプレイヤーと言っているが後述するようにチームを単位として計算するよう拡張される。
 *
 * 		順位:List<チームID>　のMAPを報告の形式とする。
 * 		・チーム対応。メンバーの平均RがチームのRとなりR変動量が計算される。
 * 		こうするのはチームが対称的と限らないから。
 * 		例えばゾンビチームｖｓ逃亡者チームで、ゾンビチームは1人、
 * 		逃亡者チームは4人とかの場合があるかもしれない。
 * 		もしチームという概念を置かずにプレイヤーのランキングとすると、
 * 		1人しかいないゾンビ役は勝率50％程度で4人の逃亡者役に勝利できてしまうかもしれない。
 * 		そのようなチーム間が非対称であるゲームに対応するため、チームのランキングとする必要がある。
 *
 * 鬼ごっこのような試合中にチーム間でメンバーの入れ替えが発生するタイプのゲームを
 * どう扱うか？
 * 1チーム1人とする。
 * 1チーム複数人の場合、恐らく対応できない。
 *
 * ゲームの性質やチーム数によってRがなかなか収束しない可能性があるが、
 * 変動域がある程度の幅に収まるだろうし、大した問題ではない。
 * 多くの場合収束する。
 * 大規模マルチチーム対戦を想定した場合、
 * 1位または最下位など極端な順位になった場合だけR変動が大きいのであり、
 * 中位なら変動量は限定的である。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class SME2 {
	/**
	 * @param ranking	順位：チーム	同順位ならチームは複数ある
	 * @return			ユーザーID：レーティング変動量
	 */
	public Map<Long, Integer> sme2(Map<Integer, HashSet<Team>> ranking) {
		Map<Long, Integer> userIdToChange = new HashMap<>();
		List<Integer> keySet = new ArrayList<>(ranking.keySet());
		for (Integer baseTRanking : keySet) {
			for (Team t : ranking.get(baseTRanking)) {
				multiTeamEloRating(baseTRanking, t, ranking, userIdToChange,
						MatchResult.WIN);
				multiTeamEloRating(baseTRanking, t, ranking, userIdToChange,
						MatchResult.DRAW);
				multiTeamEloRating(baseTRanking, t, ranking, userIdToChange,
						MatchResult.LOSE);
			}
		}
		return userIdToChange;
	}

	public static enum MatchResult {
		WIN(1), DRAW(0.5), LOSE(0);
		private double result;

		private MatchResult(double result) {
			this.result = result;
		}

		public double getResult() {
			return result;
		}
	}

	/**
	 * @param baseTRanking
	 * @param baseT		このチームの勝敗に関する処理
	 * @param ranking
	 * @param userIdToChange
	 * @param baseResult
	 */
	private void multiTeamEloRating(int baseTRanking, Team baseT,
			Map<Integer, HashSet<Team>> ranking,
			Map<Long, Integer> userIdToChange, MatchResult baseResult) {
		int baseTRating = baseT.getAveRating();
		//baseTとのレーティング計算の相手となるチーム
		HashSet<Team> targets = null;
		int direction = -1;
		switch (baseResult) {
		case DRAW:
			targets = new HashSet<>(ranking.get(baseTRanking));
			//baseT自身を除外
			targets.remove(baseT);
			break;
		case WIN:
			direction = 1;
		case LOSE:
			targets = new HashSet<>();
			for (int i = baseTRanking + direction; i >= 0
					&& i < ranking.size(); i += direction) {
				HashSet<Team> ts = ranking.get(i);
				if (ts == null)
					continue;
				for (Team t : ts)
					targets.add(t);
			}
			break;
		}
		for (Team t : targets) {
			if (t == null)
				continue;
			//他チームのレーティング
			int tRating = t.getAveRating();

			//baseチームの変動量
			int change = eloChange(baseTRating, tRating,
					baseResult.getResult());
			//メンバー全員に等しい変動量を
			for (NodeIdentifierUser node : baseT.getMembers()) {
				if (node.getUserId() == null)
					continue;
				Integer changeTotal = userIdToChange.get(node.getUserId());
				if (changeTotal == null)
					changeTotal = 0;
				changeTotal += change;
				userIdToChange.put(node.getUserId(), changeTotal);
			}
		}
	}

	private int k = 32;

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public double expected(int rating1, int rating2) {
		return 1 / (1.0 + Math.pow(10.0, (double) (rating2 - rating1) / 400.0));
	}

	/**
	 * 2プレイヤーのイロレーティングによるR変動量
	 * @param rating1
	 * @param rating2
	 * @param result	1=rating1win 0.5=draw 0=rating1lose
	 * @return	R変動量
	 */
	public int eloChange(int rating1, int rating2, double result) {
		int r = (int) Math.round(k * (result - expected(rating1, rating2)));
		if (r <= -33)
			r = -32;
		if (r >= 33)
			r = 32;
		return r;
	}

}
