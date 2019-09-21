package DistributedVote.P2PApprovalInformationPlatformSample;

import java.util.*;
import java.util.Map.*;

public class DistributedVoteTest {
	public static void main(String[] args) {
		//何度か実行し､高確率で1になる事を確認してほしい｡
		//それは全ﾉｰﾄﾞで多数決をした場合の真の多数派の値になっているということ｡
		//過半数のﾉｰﾄﾞは善意のﾉｰﾄﾞという前提を受け入れるなら､
		//それは善意のﾉｰﾄﾞの値に統一されている事を意味する｡
		Long majorValue = synctest();
		System.out.println("最終ﾀｰﾝ後の多数派の値:" + majorValue);
	}

	/**
	 * 多数決｡
	 */
	private static Long synctest() {
		// 全ﾉｰﾄﾞ作成
		List<Node> nodes = new ArrayList<>();
		Random r = new Random();

		//全ﾉｰﾄﾞ数
		int all = 1000 * 10;
		//多数派｡1を主張｡過半数を超えているので善意の参加者を意味する｡
		for (int i = 0; i < all * 0.55; i++) {
			Node n = new Node(Long.valueOf(1));
			nodes.add(n);
		}
		//0を主張｡何らかの手段で悪意あるﾉｰﾄﾞに偽の情報を教え込まれただけで､
		//正常なﾌﾟﾛｸﾞﾗﾑを実行しているﾉｰﾄﾞ｡
		//分散合意はこの騙されてしまったｸﾞﾙｰﾌﾟを多数派の値1に変える処理｡
		for (int i = 0; i < all * 0.25; i++) {
			Node n = new Node(Long.valueOf(0));
			nodes.add(n);
		}
		//0を主張｡悪意あるﾉｰﾄﾞ｡近傍の多数決に従わず､不正な値を主張し続ける｡
		for (int i = 0; i < all * 0.2; i++) {
			Node n = new Node(Long.valueOf(0), true);//evil設定
			nodes.add(n);
		}

		// 各ﾉｰﾄﾞの近傍をﾗﾝﾀﾞﾑに作成｡
		for (Node n : nodes) {
			for (int j = 0; j < r.nextInt(200) + 20; j++) {
				Node neighbor = nodes.get(r.nextInt(nodes.size() - 1));
				if (neighbor != null)
					n.add(neighbor);
			}
		}
		// ﾓﾆﾀﾘﾝｸﾞ用｡全ﾉｰﾄﾞで ﾃﾞｰﾀ:ﾉｰﾄﾞ数
		// moniは全ﾉｰﾄﾞの状態を把握できる模擬だから可能なことで､
		// 実際P2Pﾈｯﾄﾜｰｸの全ﾉｰﾄﾞの現在状態は把握不可能である｡
		Map<Long, Integer> moni = null;
		// ﾀｰﾝﾍﾞｰｽ｡
		// P2Pﾈｯﾄﾜｰｸでも開始日時と処理間隔を設定すればﾀｰﾝﾍﾞｰｽの処理は可能である｡
		for (int turn = 0; turn < 10; turn++) {
			//ﾓﾆﾀﾘﾝｸﾞ用の集計処理
			moni = new HashMap<Long, Integer>();
			for (Node n : nodes) {
				Long mutual = n.getMutualMessage();
				Integer count = moni.get(mutual);
				if (count == null)
					count = 0;
				count++;
				moni.put(mutual, count);
			}
			//ﾀｰﾝが少ない間は0組,1組双方相当数居る｡
			//ﾀｰﾝが増えてくると1ばかりに￫全ﾉｰﾄﾞで集計した場合の真の多数派の値に
			//もちろん悪意あるﾉｰﾄﾞは最後まで0を主張するのでその分は残る｡
			System.out.println("turn:" + turn);
			//全ﾉｰﾄﾞの現在状況を集計して表示｡0を主張するﾉｰﾄﾞ数､1を主張するﾉｰﾄﾞ数｡
			System.out.println(moni);

			//全ての近傍に自分の相互通信情報を送信
			for (Node n : nodes) {
				for (Node neighbor : n.getNeighbors()) {
					n.send(neighbor);
				}
			}

			// 相互作用関数実行
			for (Node n : nodes) {
				n.interaction(turn);
			}
		}
		//moniの多数派の値を求める｡つまり最終ﾀｰﾝ後の多数派の値である｡
		Long result = -1L;
		int maxCount = -1;
		for (Entry<Long, Integer> e : moni.entrySet()) {
			if (e.getValue() > maxCount) {
				maxCount = e.getValue();
				result = e.getKey();
			}
		}
		//これが実際のP2Pﾈｯﾄﾜｰｸであるとして話すと､
		//ほとんどのﾉｰﾄﾞはresultを収束値として見ていて､
		//それが恣意的改ざんが困難な統一された値であるとみなしてDBに記録する｡
		//この模擬ｺｰﾄﾞはﾌﾟﾛｸﾞﾗﾑを改造していないﾉｰﾄﾞがほぼ正常値になる事を示している｡
		//分散合意直後の値が最も高い確率で正常値である｡

		//第一	正常値かつ正常ﾌﾟﾛｸﾞﾗﾑのﾉｰﾄﾞ			55%
		//第二	不正値かつ正常ﾌﾟﾛｸﾞﾗﾑのﾉｰﾄﾞ			25%
		//第三	不正値かつ悪意ある改造ﾌﾟﾛｸﾞﾗﾑのﾉｰﾄﾞ	20%
		//この条件下で第二ｸﾞﾙｰﾌﾟが第一ｸﾞﾙｰﾌﾟへと復帰した事は騙されていたﾉｰﾄﾞが正常なﾉｰﾄﾞへと回復した事を意味し､
		//定期的にこのような処理が行われる事は､
		//短時間の間に45%を超えるﾉｰﾄﾞを不正値にする必要がある事を意味し､改ざんを成功させるのが困難になる｡

		// P2Pﾈｯﾄﾜｰｸで全ﾉｰﾄﾞの情報を集計して多数決する事は､やったとしても
		// その集計ﾉｰﾄﾞを信用できないので意味が無い｡
		// 分散合意は近傍さえ信用できるならその収束値を採用できる｡
		// ﾌﾟﾛｾｯｻ証明が近傍の信用戦略で､ﾀﾞﾐｰﾉｰﾄﾞによって不正値を持たされる事を防げる｡

		return result;
	}

	/**
	 * P2Pﾈｯﾄﾜｰｸのﾉｰﾄﾞ｡ﾀﾞﾐｰﾉｰﾄﾞ等の問題を無視すれば､
	 * 1つのｺﾝﾋﾟｭｰﾀｰであると捉えておおよそ問題無い｡
	 */
	public static class Node {
		/**
		 * 主張する値｡各ﾀｰﾝでこれが各ﾉｰﾄﾞの相互通信情報として送信されている
		 * とみなせる｡
		 */
		private Long mutualMessage = 0L;

		/**
		 * 近傍ﾘｽﾄ
		 */
		private List<Node> neighbors = new ArrayList<>();

		/**
		 * 近傍からの相互通信情報を一時的に格納する
		 */
		private List<Long> buffer = new ArrayList<Long>();

		/**
		 * 悪意あるﾉｰﾄﾞか｡近傍の多数決を無視してずっと不正な値を主張する｡
		 */
		private boolean evil = false;

		public Node(Long mutualMessage) {
			this(mutualMessage, false);
		}

		public Node(Long mutualMessage, boolean evil) {
			this.mutualMessage = mutualMessage;
			this.evil = evil;
		}

		public void add(Node n) {
			if (neighbors.indexOf(n) == -1)
				this.neighbors.add(n);
		}

		public List<Node> getNeighbors() {
			return neighbors;
		}

		public void setMutualMessage(Long mutualMessage) {
			if (evil)
				return;//悪意あるﾉｰﾄﾞは主張を変えない
			this.mutualMessage = mutualMessage;
		}

		/**
		 * @return thisが主張する値
		 */
		public Long getMutualMessage() {
			return mutualMessage;
		}

		public boolean isEvil() {
			return evil;
		}

		/**
		 * 近傍から相互通信情報を受信する
		 * @param mutualMessage	近傍から来た相互通信情報
		 */
		public void receive(Long mutualMessage) {
			buffer.add(mutualMessage);
		}

		/**
		 * 自分の相互通信情報を送信
		 * @param to 送信先の近傍
		 */
		public void send(Node to) {
			to.receive(mutualMessage);//本来ここが通信処理
		}

		/**
		 * 相互作用関数
		 */
		public void interaction(int turn) {
			// 多数決の集計用Map
			Map<Long, Integer> counts = new HashMap<Long, Integer>();
			// 最大得票数
			int max = 0;
			buffer.add(mutualMessage);
			for (Long mutualMessage : buffer) {
				Integer count = counts.get(mutualMessage);
				if (count == null)
					count = 0;
				count += 1;
				counts.put(mutualMessage, count);
				if (count > max)
					max = count;
			}

			// 最大得票数のﾃﾞｰﾀをﾘｽﾄ化
			List<Long> top = new ArrayList<Long>();
			for (Entry<Long, Integer> count : counts.entrySet()) {
				if (max == count.getValue()) {
					top.add(count.getKey());
				}
			}
			Long output = top.get(0);
			// 複数のﾃﾞｰﾀが同数の場合､ﾗﾝﾀﾞﾑに選択
			if (top.size() > 1) {
				Random r = new Random();
				output = top.get(r.nextInt(top.size()));
			}

			setMutualMessage(output);
			buffer.clear();
		}
	}
}
