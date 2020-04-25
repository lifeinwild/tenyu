package bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.Answer.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.Proved.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.RandomString.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.timer.*;
import glb.*;

/**
 * プロセッサ証明の一連の処理
 * @author exceptiontenyu@gmail.com
 *
 */
public class ProcessorProvementSequence extends P2PSequence {
	/**
	 * 最大いくつのノードに証明するか
	 */
	private final int nodeMax = Glb.getSubje().getNeighborList()
			.getNeighborMax();

	/**
	 * 自分の答え。
	 */
	private ResultList result;

	/**
	 * 自分が送るランダム文字列
	 */
	private String rndStr;
	/**
	 * 検証結果
	 */
	private List<Notification> scores = new ArrayList<>();

	public void addScore(P2PEdge n, Integer score) {
		scores.add(new Notification(n, score));
	}

	@Override
	public void end() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator() + "ProcessorProvement end:");
		for (P2PEdge n : neighborList.getNeighborsUnsafe()) {
			sb.append(" addr=" + n.getNode().getISAP2PPort() + " : port="
					+ n.getNode().getP2pPort() + " : edgeId=" + n.getEdgeId()
					+ " : impression=" + n.getImpression()
					+ " : processorScoreTotal=" + n.getProcessorScoreTotal()
					+ " : credit=" + n.credit()
					+ " : As far as I know impressionFromOther="
					+ n.getFromOther().getImpression()
					+ System.lineSeparator());
		}
		Glb.getLogger().info(sb);
	}

	public int getNodeMax() {
		return nodeMax;
	}

	public ResultList getResult() {
		return result;
	}

	/**
	 * @return	問題作成に使われるランダムな文字列
	 */
	public String getRndStr() {
		return rndStr;
	}

	public List<Notification> getScores() {
		return Collections.unmodifiableList(scores);
	}

	@Override
	public String getStartSchedule() {
		return startSchedule;
	}

	/**
	 * 8時間ごと0分開始。
	 */
	public static String startSchedule = "0 0 */8 * * ?";

	public static TimerTaskList.JobAndTrigger getJob() {
		return TimerTaskList.getJob(null, ProcessorProvementSequence.class,
				startSchedule);
	}

	@Override
	protected void setupStatements() {
		//問題を与える
		statements.add(new RandomStringStatement(this));
		//回答を送信する
		statements.add(new AnswerStatement(this));
		//証明成功を通知する
		statements.add(new ProvedStatement(this));
	}

	@Override
	public void resetConcrete() {
		rndStr = null;
		result = null;
		scores = new ArrayList<>();
		statements.clear();
		setupStatements();
	}

	public void setResult(ResultList result) {
		this.result = result;
	}

	public void setRndStr(String rndStr) {
		this.rndStr = rndStr;
	}

	@Override
	public void start() {
		//IPアドレスを１つ以上知っているか
		if (neighborList.size() == 0) {
			Glb.getLogger().info(Lang.NO_IPADDRESS);
			return;
		}
	}

	public static class Notification {
		private P2PEdge edge;
		private Integer score;

		public Notification(P2PEdge edge, Integer score) {
			this.edge = edge;
			this.score = score;
		}

		public P2PEdge getEdge() {
			return edge;
		}

		public Integer getScore() {
			return score;
		}
	}
}
