package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.*;
import bei7473p5254d69jcuat.tenyu.communication.request.catchup.GetIntegrity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

public class CatchUpStateIntegrity extends AbstractCatchUpState {
	/**
	 * 近傍の多数派の整合性情報
	 * 代入と読み取りのみにする。内部に書き込まない
	 * 起動直後null、一度代入されればnullにならない
	 */
	private transient Integrity majorityIntegrity = null;

	/**
	 * 正しい整合性情報を知っていた近傍一覧
	 */
	private ReadonlyNeighborList teachers;

	/**
	 * 非同期に整合性情報を近傍から取得し、多数決し、多数派の値として設定する
	 */
	public void catchUpAsync() {
		Glb.getExecutor().execute(() -> {
			Integrity my = Glb.getObje().getIntegrity();
			Integrity majority = getMajorityIntegrityAndSetTeachersSync(my);

			if (majority == null) {
				//古い多数派整合性情報が残る
				finish = false;
				return;
			} else {
				finish = true;
			}

			majorityIntegrity = majority;

			//整合性情報の差異をチェックし、同調成功状態を更新する
			//反映処理直後に呼び出される想定なのでここでmyは同調処理無しでの自分の整合性情報になる
			if (my != null && majority != null) {
				Glb.getMiddle().getObjeCatchUp()
						.initiallyCatchUp(my.equals(majority));
			}
		});
	}

	public Integrity getMajorityIntegrity() {
		return majorityIntegrity;
	}

	private Integrity getMajorityIntegrityAndSetTeachersSync(
			Integrity myIntegrity) {
		//整合性情報を取得するリクエスト
		AsyncRequestStatesNoRetry<
				GetIntegrity> states = GetIntegrity.sendBatch();
		if (states.size() == 0) {
			return null;
		}

		//リクエストの完了を待機する
		long start = System.currentTimeMillis();
		long max = 1000L * 60;
		long elapsed = 0;
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			if (states.isAllDone()) {
				break;
			}
			elapsed = System.currentTimeMillis() - start;
			if (elapsed > max) {
				return null;
			}
		}

		//多数派の整合性情報
		Integrity r = states.majority(myIntegrity,
				res -> ((GetIntegrityResponse) res).getIntegrity());

		//正しい整合性情報を持っていた近傍を記録しておく
		List<P2PEdge> tmp = new ArrayList<>();
		for (AsyncRequestState<GetIntegrity> e : states.getRequests()) {
			if (Response.fail(e.getReq().getRes()))
				continue;
			GetIntegrityResponse res = (GetIntegrityResponse) e.getReq()
					.getRes().getContent();
			if (res.getIntegrity().equals(r))
				tmp.add(e.getTo());
			Glb.debug("teacher " + e.getTo());
		}
		teachers = new ReadonlyNeighborList(tmp);

		//正しくない整合性情報を持っていた近傍の数を計測
		try {
			double invalidRate = teachers.size() / states.getRequests().size();
			Glb.getSubje().getObservation().addNeighborChaos(invalidRate);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		return r;
	}

	public ReadonlyNeighborList getTeachers() {
		return teachers;
	}

	@Override
	protected void resetConcrete() {
		majorityIntegrity = null;
		teachers = null;
	}

	public void setTeachers(ReadonlyNeighborList teachers) {
		this.teachers = teachers;
	}
}