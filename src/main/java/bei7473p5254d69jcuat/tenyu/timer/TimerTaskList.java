package bei7473p5254d69jcuat.tenyu.timer;

import java.util.function.*;

import org.quartz.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import glb.*;
import glb.Glb.*;

/**
 * Glbから呼び出されて一部の定期処理をセットアップする。
 *
 * 定期タスクは以下がある。
 * ・主観系、その他各モジュール内部で使用する定期処理
 * ・P2PSequence系	ここに網羅される
 * ・客観に影響する非通信系	ここに網羅される
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TimerTaskList implements GlbMemberDynamicState {
	@Override
	public void start() {
		//p2pシーケンス系
		Glb.getP2p().addSeq(ProcessorProvementSequence.getJob());

		//		P2PSequence powerDecision = new PowerDecisionSequence();
		//		powerDecision.setChannel(Glb.getConst().getPowerDecisionChannel());
		//		p2p.addSeq(powerDecision);

		Glb.getP2p().addSeq(ObjectivityUpdateSequence.getJob());

		//非通信系 timerパッケージのもの
	}

	@Override
	public void stop() {
	}

	/**
	 * @return	そのシーケンス1回にかかる最長時間。ミリ秒
	 */
	//abstract public long getSequenceTimeMillis();

	public static <C extends Job> JobAndTrigger getJob(String name,
			Class<C> c, String startSchedule) {
		return getJob(name, c, startSchedule, null, null);
	}

	public static <C extends Job> TimerTaskList.JobAndTrigger getJob(String name,
			Class<C> c, String startSchedule,
			Function<JobBuilder, JobBuilder> setupJobConcrete,
			Function<TriggerBuilder<CronTrigger>,
					TriggerBuilder<CronTrigger>> setupTriggerConcrete) {
		if (name == null)
			name = "defaultName";
		Trigger trigger = getTrigger(name, c, startSchedule,
				setupTriggerConcrete);
		TriggerKey tKey = trigger.getKey();
		JobBuilder builder = JobBuilder.newJob(c).withIdentity(tKey.getName(),
				tKey.getGroup());
		if (setupJobConcrete != null)
			builder = setupJobConcrete.apply(builder);
		JobDetail job = builder.build();
		TimerTaskList.JobAndTrigger r = new TimerTaskList.JobAndTrigger();
		r.setJob(job);
		r.setTrigger(trigger);
		return r;
	}

	/**
	 * @return	このジョブの起動タイミングを決定するトリガーオブジェクト
	 */
	private static <C extends Job> Trigger getTrigger(String name,
			Class<C> c, String cron, Function<TriggerBuilder<CronTrigger>,
					TriggerBuilder<CronTrigger>> setupTriggerConcrete) {
		TriggerBuilder<CronTrigger> builder = TriggerBuilder.newTrigger()
				.withIdentity(name, c.getSimpleName())
				.withSchedule(CronScheduleBuilder.cronSchedule(cron)
						.withMisfireHandlingInstructionDoNothing());
		if (setupTriggerConcrete != null)
			builder = setupTriggerConcrete.apply(builder);
		Trigger trigger = builder.build();
		return trigger;
	}

	public static class JobAndTrigger {
		private JobDetail job;
		private Trigger trigger;

		public JobDetail getJob() {
			return job;
		}

		public Trigger getTrigger() {
			return trigger;
		}

		public void setJob(JobDetail job) {
			this.job = job;
		}

		public void setTrigger(Trigger trigger) {
			this.trigger = trigger;
		}
	}
}
