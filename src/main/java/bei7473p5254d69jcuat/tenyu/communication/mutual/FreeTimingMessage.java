package bei7473p5254d69jcuat.tenyu.communication.mutual;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

/**
 * 現在この概念は使用されていない。
 *
 * 非ターンベース
 * TODO:このあたりの設計は少し奇妙だという感覚がある。
 *
 * 通常、メッセージはreceived()でそのメッセージを受信した時の処理を書くが、
 * DistributedVoteMessage系はDistributedVoteMessage#received()に汎用実装があり
 * シーケンスにメッセージを伝えるだけである。
 * シーケンスは定められたタイミングでP2P命令を呼び出していくが、
 * それとは別にreceive()があり、受け取ったメッセージが命令に伝えられる。
 * シーケンスから命令への伝達はreceive()とrun()の2種ある。
 * そして、TurnBase系はreceive()の時にvotesにメッセージをためていって
 * run()で処理する。FreeTiming系はreceive()で具体的なロジックを実行して
 * 返信までする。run()は何もしない。
 * 一方で、run呼び出しの前後の時間を計測しながらP2Pネットワーク全体で
 * 実行するP2P命令を一致させる処理はどちらも必要としている。
 * FreeTimingは与えられた時間内でメッセージの送信や受信のタイミングが自由だが
 * 命令自体はいつ始まりいつ終わるかP2Pネットワーク全体で同じである。
 * やはりこの設計は今のところ妥当だと考えている。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class FreeTimingMessage extends DistributedVoteMessage {

	@Override
	protected final boolean validateDistributedVoteConcrete(Message m) {
		return true;
	}

	public static abstract class FreeTimingStatement<T extends P2PSequence>
			extends P2PStatement<T> {
		@Override
		public boolean receive(Received r, DistributedVoteMessage c) {
			if (!isSupport(c))
				return false;

			//ここで具体的なロジックが実行される
			DistributedVoteMessage res = createMessage(r, c);
			if (res == null)
				return false;
			P2PEdge to = r.getEdgeByInnermostPackage();
			if (to == null)
				return false;

			//このように個別に応答するのがFreeTiming
			Message m = Message.build(c).packaging(res.createPackage(to))
					.finish();
			return Glb.getP2p().sendSync(m, to);
		}

		@Override
		public void run(long statementStart, int counter) {
		}

		/**
		 * 相互作用関数の代わり。FreeTimingは典型的な相互作用という形態にならない。
		 * しかし、FreeTimingStatementの内部状態等を通じた相互作用はありうる。
		 * @param r
		 * @param c
		 * @return
		 */
		public abstract FreeTimingMessage createMessage(Received r,
				DistributedVoteMessage c);
	}
}
